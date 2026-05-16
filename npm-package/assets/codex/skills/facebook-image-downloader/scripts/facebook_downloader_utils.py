#!/usr/bin/env python3
"""Helper utilities for facebook_image_downloader.py."""

import argparse
import hashlib
import json
import re
import sys
import time
import urllib.parse
from pathlib import Path

import requests

USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/124.0.0.0 Safari/537.36"
)
VALID_HOSTS = {"facebook.com", "www.facebook.com", "m.facebook.com", "fb.watch"}
UNWANTED_URL_MARKERS = (
    "emoji.php",
    "safe_image.php",
    "profile_pic",
    "rsrc.php",
    "static.xx.fbcdn.net",
    "platform-lookaside",
    "scontent.xx.fbcdn.net/v/t39.30808-6/",
)
UNWANTED_ALT_MARKERS = ("profile picture", "icon", "emoji", "avatar", "cover photo")
DEFAULT_TIMEOUT = 45
DEFAULT_DELAY = 0.8
DEFAULT_SCROLLS = 6
DEFAULT_SCROLL_PAUSE_MS = 1200


def configure_output():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")


def sanitize_name(value, fallback="facebook_images"):
    value = re.sub(r"\s+", " ", value or "").strip()
    value = re.sub(r'[\\/:*?"<>|]', "_", value)
    value = value.strip(" ._")
    return value[:100] or fallback


def is_supported_url(url):
    parsed = urllib.parse.urlparse(url)
    host = parsed.netloc.lower()
    if host.startswith("www.") and host not in VALID_HOSTS:
        host = host[4:]
    return parsed.scheme in {"http", "https"} and (
        host in VALID_HOSTS or host.endswith(".facebook.com")
    )


def load_urls(args):
    urls = [u.strip() for u in args.urls if u.strip()]
    if args.urls_file:
        file_path = Path(args.urls_file).expanduser()
        for line in file_path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if line and not line.startswith("#"):
                urls.append(line)
    deduped = []
    seen = set()
    for url in urls:
        if url not in seen:
            deduped.append(url)
            seen.add(url)
    return deduped


def load_cache(cache_path):
    if not cache_path or not Path(cache_path).exists():
        return {}
    try:
        data = json.loads(Path(cache_path).read_text(encoding="utf-8"))
        return data if isinstance(data, dict) else {}
    except Exception:
        return {}


def save_cache(cache_path, cache):
    if not cache_path:
        return
    Path(cache_path).write_text(
        json.dumps(cache, indent=2, ensure_ascii=False), encoding="utf-8"
    )


def dismiss_overlays(page):
    labels = [
        "Allow all cookies",
        "Accept all",
        "Allow all",
        "Only allow essential cookies",
        "Decline optional cookies",
        "Close",
        "Dong",
        "Cho phep tat ca cookie",
        "Tu choi cookie khong bat buoc",
    ]
    for label in labels:
        try:
            locator = page.get_by_role("button", name=label)
            if locator.count() > 0 and locator.first.is_visible(timeout=1200):
                locator.first.click(timeout=1200)
                page.wait_for_timeout(500)
        except Exception:
            pass


def looks_like_login_wall(page):
    current_url = page.url.lower()
    if any(token in current_url for token in ("/login", "/checkpoint", "recover/initiate")):
        return True
    try:
        body_text = page.locator("body").inner_text(timeout=2000).lower()
    except Exception:
        return False
    markers = (
        "log in or sign up",
        "log in to facebook",
        "login to facebook",
        "dang nhap hoac dang ky",
        "ban phai dang nhap",
    )
    return any(marker in body_text for marker in markers)


def collect_images(page, min_width, min_height):
    raw_items = page.evaluate(
        """
        () => Array.from(document.images).map((img) => ({
            url: img.currentSrc || img.src || "",
            width: img.naturalWidth || img.width || 0,
            height: img.naturalHeight || img.height || 0,
            alt: img.alt || ""
        }))
        """
    )
    results = []
    seen = set()
    for item in raw_items:
        url = (item.get("url") or "").strip()
        alt = (item.get("alt") or "").strip().lower()
        width = int(item.get("width") or 0)
        height = int(item.get("height") or 0)
        if not url or url.startswith("data:"):
            continue
        if width < min_width or height < min_height:
            continue
        lowered_url = url.lower()
        if any(marker in lowered_url for marker in UNWANTED_URL_MARKERS):
            continue
        if any(marker in alt for marker in UNWANTED_ALT_MARKERS):
            continue
        if url in seen:
            continue
        seen.add(url)
        results.append({"url": url, "width": width, "height": height, "alt": alt})
    return results


def auto_scroll(page, steps, pause_ms):
    for _ in range(steps):
        page.mouse.wheel(0, 2200)
        page.wait_for_timeout(pause_ms)


def guess_extension(url, content_type=""):
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


def build_filename(url, index, content_type=""):
    parsed = urllib.parse.urlparse(url)
    basename = Path(urllib.parse.unquote(parsed.path)).name
    basename = re.sub(r"[^\w\-.]", "_", basename)
    if not basename or basename.startswith("."):
        digest = hashlib.md5(url.encode("utf-8")).hexdigest()[:12]
        return f"img_{index:04d}_{digest}{guess_extension(url, content_type)}"
    if not Path(basename).suffix:
        return basename + guess_extension(url, content_type)
    return basename


def download_image(url, output_dir, index, referer, timeout):
    headers = {
        "User-Agent": USER_AGENT,
        "Accept": "image/webp,image/apng,image/*,*/*;q=0.8",
        "Referer": referer,
    }
    response = requests.get(url, headers=headers, timeout=timeout, stream=True)
    response.raise_for_status()
    filename = build_filename(url, index, response.headers.get("Content-Type", ""))
    path = output_dir / filename
    counter = 1
    while path.exists():
        path = output_dir / f"{Path(filename).stem}_{counter:03d}{Path(filename).suffix}"
        counter += 1
    with path.open("wb") as handle:
        for chunk in response.iter_content(chunk_size=8192):
            if chunk:
                handle.write(chunk)
    return path


def resolve_output_dir(base_output, page_title, url, make_subdir):
    if make_subdir:
        return base_output / sanitize_name(page_title, fallback=sanitize_name(url))
    return base_output


def build_parser():
    parser = argparse.ArgumentParser(
        description="Download images from public Facebook post/page URLs with Playwright."
    )
    parser.add_argument("urls", nargs="*", help="One or more Facebook URLs.")
    parser.add_argument("--urls-file", help="Text file with one Facebook URL per line.")
    parser.add_argument("--state-dir", help="Persistent browser context directory.")
    parser.add_argument("--cache", help="Incremental JSON cache file path.")
    parser.add_argument("-o", "--output", default="facebook_downloads", help="Output directory.")
    parser.add_argument("--delay", type=float, default=DEFAULT_DELAY, help="Delay between downloads in seconds.")
    parser.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT, help="Request/page timeout in seconds.")
    parser.add_argument("--scrolls", type=int, default=DEFAULT_SCROLLS, help="How many times to scroll after opening each page.")
    parser.add_argument("--scroll-pause-ms", type=int, default=DEFAULT_SCROLL_PAUSE_MS, help="Pause after each scroll in milliseconds.")
    parser.add_argument("--min-width", type=int, default=300, help="Minimum image width to keep.")
    parser.add_argument("--min-height", type=int, default=300, help="Minimum image height to keep.")
    parser.add_argument("--headed", action="store_true", help="Show the browser window.")
    return parser
