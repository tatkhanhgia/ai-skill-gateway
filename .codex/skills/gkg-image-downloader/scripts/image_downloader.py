#!/usr/bin/env python3
"""
Image Downloader - Detect and download images from web pages with pagination support.

Usage:
    # Single page
    python image_downloader.py "https://example.com/gallery"

    # Auto-detect pagination (follow Next links up to 50 pages)
    python image_downloader.py "https://example.com/gallery" --follow-pages

    # Specify exact number of pages via URL template
    python image_downloader.py "https://example.com/gallery?page={page}" --pages 5

    # Combine with output directory and delays
    python image_downloader.py "https://example.com/gallery" -o ./images --follow-pages --delay 1

Note: Respect robots.txt and website Terms of Service.
Commercial use of scraped data may have legal implications.
"""

import argparse
import hashlib
import re
import sys
import time
import urllib.parse
from html import unescape
from pathlib import Path

import requests

DEFAULT_MAX_PAGES = 50
COMMON_PLACEHOLDERS = {
    "timg.gif",
    "blank.gif",
    "placeholder",
    "loading.gif",
    "spinner.gif",
    "lazyload",
    "pixel.gif",
    "transparent.gif",
}


def fetch_html(url, timeout=30):
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/120.0.0.0 Safari/537.36"
        ),
        "Accept": (
            "text/html,application/xhtml+xml,application/xml;"
            "q=0.9,image/webp,*/*;q=0.8"
        ),
        "Accept-Language": "en-US,en;q=0.9",
    }
    resp = requests.get(url, headers=headers, timeout=timeout)
    resp.raise_for_status()
    return resp.text


def extract_image_urls(html, base_url):
    """Extract image URLs from <img> tags, preferring data-src/data-original over src."""
    img_tags = re.findall(r'<img[^>]*?>', html, re.IGNORECASE | re.DOTALL)

    image_urls = []
    for tag in img_tags:
        url = None
        for attr in ('data-src', 'data-original', 'src'):
            m = re.search(rf'\s{attr}=["\']([^"\']+)["\']', tag, re.IGNORECASE)
            if m:
                url = m.group(1).strip()
                break

        if not url or url.startswith('data:'):
            continue

        lowered = url.lower()
        if any(p in lowered for p in COMMON_PLACEHOLDERS):
            continue

        absolute = urllib.parse.urljoin(base_url, url)
        image_urls.append(absolute)

    return list(dict.fromkeys(image_urls))


def find_next_page_url(html, base_url):
    """
    Try to find the next page URL from common pagination patterns.
    Returns absolute URL or None.
    """
    # Pattern 1: <link rel="next" href="...">
    m = re.search(r'<link[^>]+rel=["\']next["\'][^>]+href=["\']([^"\']+)["\']', html, re.IGNORECASE)
    if m:
        return urllib.parse.urljoin(base_url, m.group(1).strip())

    m = re.search(r'<link[^>]+href=["\']([^"\']+)["\'][^>]+rel=["\']next["\']', html, re.IGNORECASE)
    if m:
        return urllib.parse.urljoin(base_url, m.group(1).strip())

    # Pattern 2: <a> with text/aria-label indicating next
    # Capture the whole <a ...> tag first
    a_tags = re.findall(r'<a[^>]*?>.*?</a>', html, re.IGNORECASE | re.DOTALL)
    for tag in a_tags:
        text = re.sub(r'<[^>]+>', '', tag).strip()
        lowered_text = text.lower()
        # Common next indicators
        if any(k in lowered_text for k in ('next', '›', '»', '>>', 'trang sau', '下一页', '次へ')):
            m = re.search(r'href=["\']([^"\']+)["\']', tag, re.IGNORECASE)
            if m:
                href = m.group(1).strip()
                if href not in ('#', 'javascript:void(0)', ''):
                    return urllib.parse.urljoin(base_url, href)

    # Pattern 3: <a> with class containing "next"
    m = re.search(r'<a[^>]+class=["\'][^"\']*\bnext\b[^"\']*["\'][^>]+href=["\']([^"\']+)["\']', html, re.IGNORECASE)
    if m:
        href = m.group(1).strip()
        if href not in ('#', 'javascript:void(0)', ''):
            return urllib.parse.urljoin(base_url, href)

    m = re.search(r'<a[^>]+href=["\']([^"\']+)["\'][^>]+class=["\'][^"\']*\bnext\b[^"\']*["\']', html, re.IGNORECASE)
    if m:
        href = m.group(1).strip()
        if href not in ('#', 'javascript:void(0)', ''):
            return urllib.parse.urljoin(base_url, href)

    return None


def extract_page_title(html, page_url):
    m = re.search(r'<title[^>]*>(.*?)</title>', html, re.IGNORECASE | re.DOTALL)
    if m:
        title = unescape(re.sub(r'\s+', ' ', m.group(1))).strip()
        title = re.sub(r'[\\/:*?"<>|]', '_', title)
        title = re.sub(r'\s+', ' ', title).strip(' .')
        if title:
            return title

    parsed = urllib.parse.urlparse(page_url)
    fallback = Path(parsed.path).name or parsed.netloc or 'downloaded_images'
    fallback = re.sub(r'[\\/:*?"<>|]', '_', fallback)
    return fallback.strip(' .') or 'downloaded_images'


def get_extension_from_url(url, content_type=None):
    path = urllib.parse.urlparse(url).path.lower()
    ext = Path(path).suffix
    if ext and len(ext) <= 5:
        return ext

    mime_to_ext = {
        "image/jpeg": ".jpg",
        "image/jpg": ".jpg",
        "image/png": ".png",
        "image/gif": ".gif",
        "image/webp": ".webp",
        "image/svg+xml": ".svg",
        "image/bmp": ".bmp",
    }
    if content_type:
        ct = content_type.split(";")[0].strip().lower()
        if ct in mime_to_ext:
            return mime_to_ext[ct]
    return ".jpg"


def safe_filename(url, content_type=None, index=0):
    parsed = urllib.parse.urlparse(url)
    path = urllib.parse.unquote(parsed.path)
    basename = Path(path).name

    basename = re.sub(r"[^\w\-.]", "_", basename)
    if not basename or basename.startswith("."):
        h = hashlib.md5(url.encode("utf-8")).hexdigest()[:12]
        ext = get_extension_from_url(url, content_type)
        basename = f"img_{index:04d}_{h}{ext}"
    else:
        if not Path(basename).suffix:
            ext = get_extension_from_url(url, content_type)
            basename = basename + ext

    return basename


def download_image(url, output_dir, index, timeout=30):
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/120.0.0.0 Safari/537.36"
        ),
        "Accept": "image/webp,image/apng,image/*,*/*;q=0.8",
        "Referer": urllib.parse.urljoin(url, "/"),
    }

    try:
        resp = requests.get(url, headers=headers, timeout=timeout, stream=True)
        resp.raise_for_status()
    except requests.RequestException as exc:
        return False, str(exc)

    content_type = resp.headers.get("Content-Type", "")
    filename = safe_filename(url, content_type, index)

    filepath = output_dir / filename
    counter = 1
    stem = Path(filename).stem
    suffix = Path(filename).suffix
    while filepath.exists():
        filepath = output_dir / f"{stem}_{counter:03d}{suffix}"
        counter += 1

    try:
        with open(filepath, "wb") as f:
            for chunk in resp.iter_content(chunk_size=8192):
                if chunk:
                    f.write(chunk)
        return True, filepath.name
    except OSError as exc:
        return False, str(exc)


def process_single_page(url, output_dir, global_index, timeout, delay):
    """Fetch one page and download its images. Returns (success, fail, next_index)."""
    print(f"\nFetching page: {url}")
    try:
        html = fetch_html(url, timeout=timeout)
    except requests.RequestException as exc:
        print(f"ERROR: Failed to fetch page: {exc}", file=sys.stderr)
        return 0, 0, global_index, None

    image_urls = extract_image_urls(html, url)
    print(f"Found {len(image_urls)} image(s) on this page.")

    success_count = 0
    fail_count = 0
    idx = global_index

    for img_url in image_urls:
        idx += 1
        print(f"[{idx}] Downloading: {img_url}")
        ok, info = download_image(img_url, output_dir, idx, timeout=timeout)
        if ok:
            print(f"         -> Saved: {info}")
            success_count += 1
        else:
            print(f"         -> Failed: {info}")
            fail_count += 1

        if delay > 0:
            time.sleep(delay)

    next_url = find_next_page_url(html, url)
    return success_count, fail_count, idx, next_url


def main():
    parser = argparse.ArgumentParser(
        description="Detect and download images from web pages with pagination support."
    )
    parser.add_argument("url", help="Target web page URL. Use {page} placeholder if --pages is set.")
    parser.add_argument(
        "-o", "--output", help="Output directory. If omitted, create a folder from the page title in the current directory."
    )
    parser.add_argument(
        "--delay", type=float, default=0.5, help="Delay between downloads (seconds)"
    )
    parser.add_argument(
        "--timeout", type=int, default=30, help="Request timeout (seconds)"
    )
    parser.add_argument(
        "--follow-pages", action="store_true",
        help="Auto-detect and follow pagination links (Next page)."
    )
    parser.add_argument(
        "--pages", type=int, default=0,
        help=(
            "Number of pages to crawl. If >0 and URL contains {page}, "
            "it will be substituted with 1..pages. Otherwise it appends ?page=N "
            "or crawls via --follow-pages."
        ),
    )
    parser.add_argument(
        "--max-pages", type=int, default=DEFAULT_MAX_PAGES,
        help=f"Maximum pages to crawl when using --follow-pages (default {DEFAULT_MAX_PAGES})."
    )
    parser.add_argument(
        "--start-page", type=int, default=1,
        help="Start page number when using --pages (default 1)."
    )
    args = parser.parse_args()

    try:
        first_html = fetch_html(args.url, timeout=args.timeout)
    except requests.RequestException as exc:
        print(f"ERROR: Failed to fetch initial page: {exc}", file=sys.stderr)
        sys.exit(1)

    output_root = Path(args.output) if args.output else Path.cwd()
    output_root.mkdir(parents=True, exist_ok=True)

    page_title = extract_page_title(first_html, args.url)
    output_dir = output_root / page_title
    output_dir.mkdir(parents=True, exist_ok=True)
    print(f"Saving files to: {output_dir.resolve()}")

    total_success = 0
    total_fail = 0
    global_index = 0
    visited_urls = set()

    if args.pages > 0:
        # Mode: explicit page range
        has_template = "{page}" in args.url
        for page_num in range(args.start_page, args.start_page + args.pages):
            if has_template:
                page_url = args.url.replace("{page}", str(page_num))
            else:
                # Append ?page= or &page= depending on existing query
                sep = "&" if "?" in args.url else "?"
                page_url = f"{args.url}{sep}page={page_num}"

            if page_url in visited_urls:
                continue
            visited_urls.add(page_url)

            s, f, global_index, _ = process_single_page(
                page_url, output_dir, global_index, args.timeout, args.delay
            )
            total_success += s
            total_fail += f
    elif args.follow_pages:
        # Mode: auto-follow next links
        current_url = args.url
        page_count = 0
        while current_url and page_count < args.max_pages:
            if current_url in visited_urls:
                print(f"Already visited {current_url}, stopping.")
                break
            visited_urls.add(current_url)
            page_count += 1

            s, f, global_index, next_url = process_single_page(
                current_url, output_dir, global_index, args.timeout, args.delay
            )
            total_success += s
            total_fail += f

            if not next_url:
                print("No next page found.")
                break
            current_url = next_url
    else:
        # Single page
        s, f, global_index, _ = process_single_page(
            args.url, output_dir, global_index, args.timeout, args.delay
        )
        total_success += s
        total_fail += f

    print("\n" + "=" * 50)
    print("Done!")
    print(f"Total Success: {total_success} | Total Failed: {total_fail} | Grand Total: {total_success + total_fail}")
    print(f"Files saved to: {output_dir.resolve()}")


if __name__ == "__main__":
    main()
