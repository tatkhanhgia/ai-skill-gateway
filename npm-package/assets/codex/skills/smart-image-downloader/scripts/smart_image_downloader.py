#!/usr/bin/env python3
"""
Smart generic image downloader with incremental JSON cache.

Skips already-downloaded images on subsequent runs using a JSON-based
URL cache keyed by normalized image URLs.

Usage:
    python smart_image_downloader.py
    python smart_image_downloader.py --urls-file ./urls.txt -o ./downloads
    python smart_image_downloader.py --stop-on-hit --use-playwright

Install deps first:
    pip install requests beautifulsoup4 playwright
    playwright install chromium
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
import time
import urllib.parse
from datetime import datetime, timezone
from pathlib import Path

import requests
from bs4 import BeautifulSoup
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

try:
    from playwright.sync_api import sync_playwright
except ImportError:
    sync_playwright = None

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/124.0.0.0 Safari/537.36"
)

DEFAULT_TIMEOUT = 30
DEFAULT_DELAY = 0.8
DEFAULT_MIN_WIDTH = 300
DEFAULT_MIN_HEIGHT = 300

TRACKING_PARAMS = {
    "utm_source",
    "utm_medium",
    "utm_campaign",
    "utm_term",
    "utm_content",
    "fbclid",
    "igshid",
    "ref",
    "source",
}


def configure_output() -> None:
    """Ensure UTF-8 stdout/stderr on Windows."""
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")


def normalize_url(url: str) -> str:
    """Strip fragments and known tracking query params."""
    parsed = urllib.parse.urlparse(url)
    qs = urllib.parse.parse_qsl(parsed.query)
    filtered = [(k, v) for k, v in qs if k.lower() not in TRACKING_PARAMS]
    query = urllib.parse.urlencode(filtered)
    netloc = parsed.netloc.lower()
    return urllib.parse.urlunparse(
        (parsed.scheme, netloc, parsed.path, parsed.params, query, "")
    )


def load_cache(path: Path) -> dict[str, dict]:
    """Load cache JSON; return empty dict if missing or corrupt."""
    if not path.exists():
        return {}
    try:
        text = path.read_text(encoding="utf-8")
        data = json.loads(text)
        if isinstance(data, dict):
            return data
    except (json.JSONDecodeError, OSError):
        pass
    return {}


def save_cache(path: Path, data: dict) -> None:
    """Atomic write: temp file then os.replace()."""
    tmp = path.with_suffix(".tmp")
    tmp.write_text(
        json.dumps(data, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    os.replace(str(tmp), str(path))


def sanitize_name(value: str | None, fallback: str = "images") -> str:
    """Make a string safe for use as a directory or file name."""
    value = re.sub(r"\s+", " ", value or "").strip()
    value = re.sub(r'[\\/:*?"<>|]', "_", value)
    value = value.strip(" ._")
    return value[:100] or fallback


def guess_extension(url: str, content_type: str = "") -> str:
    """Best-effort extension from URL path or Content-Type header."""
    path = urllib.parse.urlparse(url).path
    suffix = Path(path).suffix.lower()
    if suffix in {".jpg", ".jpeg", ".png", ".webp", ".gif", ".bmp"}:
        return suffix

    mime_to_ext = {
        "image/jpeg": ".jpg",
        "image/jpg": ".jpg",
        "image/png": ".png",
        "image/webp": ".webp",
        "image/gif": ".gif",
        "image/bmp": ".bmp",
    }
    content_type = content_type.split(";", 1)[0].strip().lower()
    return mime_to_ext.get(content_type, ".jpg")


def build_filename(url: str, index: int, content_type: str = "") -> str:
    """Create a safe filename from a URL."""
    parsed = urllib.parse.urlparse(url)
    basename = Path(urllib.parse.unquote(parsed.path)).name
    basename = re.sub(r"[^\w\-.]", "_", basename)
    if not basename or basename.startswith("."):
        digest = hashlib.md5(url.encode("utf-8")).hexdigest()[:12]
        return f"img_{index:04d}_{digest}{guess_extension(url, content_type)}"
    if not Path(basename).suffix:
        return basename + guess_extension(url, content_type)
    return basename


def requests_session() -> requests.Session:
    """Create a requests session with retry logic."""
    session = requests.Session()
    retries = Retry(
        total=3,
        backoff_factor=0.5,
        status_forcelist=[500, 502, 503, 504],
        allowed_methods=["HEAD", "GET", "OPTIONS"],
    )
    session.mount("https://", HTTPAdapter(max_retries=retries))
    session.mount("http://", HTTPAdapter(max_retries=retries))
    return session


def fetch_page(url: str, timeout: int, use_playwright: bool, session: requests.Session) -> str:
    """Fetch page HTML. Requests first; Playwright fallback if requested."""
    headers = {
        "User-Agent": USER_AGENT,
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    }
    try:
        resp = session.get(url, headers=headers, timeout=timeout)
        resp.raise_for_status()
        return resp.text
    except requests.RequestException as exc:
        if use_playwright and sync_playwright is not None:
            try:
                with sync_playwright() as p:
                    browser = p.chromium.launch(headless=True)
                    page = browser.new_page(user_agent=USER_AGENT)
                    try:
                        page.goto(url, wait_until="domcontentloaded", timeout=timeout * 1000)
                        page.wait_for_timeout(2000)
                        html = page.content()
                        return html
                    finally:
                        browser.close()
            except Exception as pw_exc:
                raise pw_exc from exc
        raise


def extract_images(html: str, base_url: str) -> list[dict]:
    """Extract candidate <img> URLs from HTML using BeautifulSoup."""
    soup = BeautifulSoup(html, "html.parser")
    seen: set[str] = set()
    results: list[dict] = []

    for img in soup.find_all("img"):
        src = (
            img.get("src")
            or img.get("data-src")
            or img.get("data-original")
            or img.get("data-lazy-src")
        )
        if not src:
            continue

        url = urllib.parse.urljoin(base_url, src.strip())
        if not url or url.startswith("data:"):
            continue

        parsed = urllib.parse.urlparse(url)
        if parsed.scheme not in {"http", "https"}:
            continue

        norm = normalize_url(url)
        if norm in seen:
            continue
        seen.add(norm)

        width = _parse_dim(img.get("width"))
        height = _parse_dim(img.get("height"))

        results.append({"url": url, "norm_url": norm, "width": width, "height": height})

    return results


def _parse_dim(value: str | int | None) -> int:
    """Parse width/height attribute to int; return 0 if missing/invalid."""
    if value is None:
        return 0
    try:
        return int(value)
    except (ValueError, TypeError):
        return 0


def download_image(
    img_url: str,
    output_dir: Path,
    index: int,
    referer: str,
    timeout: int,
    session: requests.Session,
) -> Path:
    """Stream-download an image and return the saved Path."""
    headers = {
        "User-Agent": USER_AGENT,
        "Accept": "image/webp,image/apng,image/*,*/*;q=0.8",
        "Referer": referer,
    }
    resp = session.get(img_url, headers=headers, timeout=timeout, stream=True)
    resp.raise_for_status()

    filename = build_filename(img_url, index, resp.headers.get("Content-Type", ""))
    # Prevent path traversal: keep only the basename
    filename = os.path.basename(filename)
    path = output_dir / filename

    stem = Path(filename).stem
    suffix = Path(filename).suffix
    counter = 1
    while path.exists():
        path = output_dir / f"{stem}_{counter:03d}{suffix}"
        counter += 1

    with path.open("wb") as handle:
        for chunk in resp.iter_content(chunk_size=8192):
            if chunk:
                handle.write(chunk)

    return path


def build_output_dir(base_output: Path, page_url: str) -> Path:
    """Build and create {output}/{host}/{page_slug}/ directory."""
    parsed = urllib.parse.urlparse(page_url)
    host = parsed.netloc.lower()
    if host.startswith("www."):
        host = host[4:]
    host = sanitize_name(host, fallback="site")

    path = urllib.parse.unquote(parsed.path).strip("/")
    if path:
        slug = path.replace("/", "_")
        slug = sanitize_name(slug, fallback="page")
    else:
        slug = "index"

    out = base_output / host / slug
    out.mkdir(parents=True, exist_ok=True)
    return out


def load_urls(urls_file: Path) -> list[str]:
    """Read one URL per line, skip blanks and comments."""
    urls: list[str] = []
    if not urls_file.exists():
        return urls
    for line in urls_file.read_text(encoding="utf-8-sig").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            urls.append(line)
    return urls


def main() -> int:
    configure_output()

    parser = argparse.ArgumentParser(
        description="Smart generic image downloader with incremental JSON cache."
    )
    parser.add_argument(
        "--urls-file", default="urlImage.txt", help="Text file with one URL per line."
    )
    parser.add_argument(
        "-o", "--output", default="downloads", help="Root output directory."
    )
    parser.add_argument(
        "--cache",
        default=None,
        help="Path to cache JSON (default: {output}/cache.json).",
    )
    parser.add_argument(
        "--stop-on-hit",
        action="store_true",
        help="Stop processing current page when first cached image is found.",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=DEFAULT_DELAY,
        help="Seconds between requests (default: 0.8).",
    )
    parser.add_argument(
        "--timeout",
        type=int,
        default=DEFAULT_TIMEOUT,
        help="Request timeout in seconds (default: 30).",
    )
    parser.add_argument(
        "--min-width",
        type=int,
        default=DEFAULT_MIN_WIDTH,
        help="Minimum image width in pixels (default: 300).",
    )
    parser.add_argument(
        "--min-height",
        type=int,
        default=DEFAULT_MIN_HEIGHT,
        help="Minimum image height in pixels (default: 300).",
    )
    parser.add_argument(
        "--use-playwright",
        action="store_true",
        help="Force Playwright JS rendering fallback.",
    )
    parser.add_argument(
        "--no-size-filter",
        action="store_true",
        help="Disable min-width/min-height filtering.",
    )
    args = parser.parse_args()

    output_root = Path(args.output).expanduser().resolve()
    output_root.mkdir(parents=True, exist_ok=True)

    cache_path = Path(args.cache).expanduser().resolve() if args.cache else output_root / "cache.json"
    cache = load_cache(cache_path)

    urls_file = Path(args.urls_file).expanduser()
    urls = load_urls(urls_file)

    if not urls:
        print(f"ERROR No URLs found in: {urls_file}", file=sys.stderr)
        return 1

    total_found = 0
    total_skipped = 0
    total_downloaded = 0
    total_failed = 0

    session = requests_session()

    for page_url in urls:
        print(f"\nPage: {page_url}")
        try:
            html = fetch_page(page_url, args.timeout, args.use_playwright, session)
        except Exception as exc:
            print(f"  -> Failed to fetch page: {exc}")
            continue

        images = extract_images(html, page_url)
        if not images:
            print("  -> No images found.")
            continue

        output_dir = build_output_dir(output_root, page_url)
        print(f"  -> Output dir: {output_dir}")

        for index, item in enumerate(images, start=1):
            norm_url = item["norm_url"]
            total_found += 1

            if norm_url in cache:
                total_skipped += 1
                if args.stop_on_hit:
                    print(f"  [{index}] Cached (stop-on-hit): {norm_url}")
                    break
                continue

            if not args.no_size_filter:
                if item["width"] and item["width"] < args.min_width:
                    continue
                if item["height"] and item["height"] < args.min_height:
                    continue

            print(f"  [{index}] Downloading: {norm_url}")
            try:
                saved = download_image(item["url"], output_dir, index, page_url, args.timeout, session)
                rel_path = saved.relative_to(output_root.resolve())
                cache[norm_url] = {
                    "source": page_url,
                    "downloaded_at": datetime.now(timezone.utc).isoformat(),
                    "local_path": str(rel_path).replace("\\", "/"),
                }
                total_downloaded += 1
                print(f"       -> Saved: {rel_path}")
            except requests.RequestException as exc:
                print(f"       -> Failed: {exc}")
                total_failed += 1
            except OSError as exc:
                print(f"       -> Failed: {exc}")
                total_failed += 1

            if args.delay > 0:
                time.sleep(args.delay)

        try:
            save_cache(cache_path, cache)
        except Exception as exc:
            print(f"  -> Warning: failed to save cache: {exc}")

    print("\n" + "=" * 50)
    print(
        f"Done. Found: {total_found} | Downloaded: {total_downloaded} | "
        f"Skipped: {total_skipped} | Failed: {total_failed}"
    )
    print(f"Cache: {cache_path}")
    print(f"Output root: {output_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
