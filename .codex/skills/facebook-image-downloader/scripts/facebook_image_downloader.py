#!/usr/bin/env python3
"""
Download images from public Facebook post/page URLs with Playwright.

Usage:
    python facebook_image_downloader.py "https://www.facebook.com/..."
    python facebook_image_downloader.py --urls-file ./facebook_urls.txt --state-dir ./fb_state --cache ./fb_cache.json -o ./downloads

Notes:
    - Intended for public Facebook content or content you are authorized to access.
    - This script does not automate login or bypass access controls.
    - Install deps first: pip install playwright requests && playwright install chromium
"""

import sys
import time
from pathlib import Path

import requests

try:
    from playwright.sync_api import TimeoutError as PlaywrightTimeoutError
    from playwright.sync_api import sync_playwright
except ImportError:
    PlaywrightTimeoutError = None
    sync_playwright = None

from facebook_downloader_utils import (
    auto_scroll,
    build_parser,
    collect_images,
    configure_output,
    dismiss_overlays,
    download_image,
    is_supported_url,
    load_cache,
    load_urls,
    looks_like_login_wall,
    resolve_output_dir,
    sanitize_name,
    save_cache,
)


def process_url(context, url, base_output, args, cache, make_subdir):
    page = context.new_page()
    try:
        page.goto(url, wait_until="domcontentloaded", timeout=args.timeout * 1000)
        page.wait_for_timeout(1800)
        dismiss_overlays(page)
        page.wait_for_timeout(1000)

        if looks_like_login_wall(page):
            print(f"SKIP Login wall detected: {url}")
            if args.state_dir:
                print("WARN Your session may have expired. Re-login with --headed and try again.")
            return 0, 0

        auto_scroll(page, args.scrolls, args.scroll_pause_ms)
        images = collect_images(page, args.min_width, args.min_height)
        page_title = sanitize_name(page.title(), fallback="facebook_images")
        output_dir = resolve_output_dir(base_output, page_title, url, make_subdir)
        output_dir.mkdir(parents=True, exist_ok=True)

        print(f"\nPage: {url}")
        print(f"Title: {page_title}")
        print(f"Found {len(images)} candidate image(s)")
        print(f"Saving to: {output_dir.resolve()}")

        success = 0
        failed = 0
        for index, item in enumerate(images, start=1):
            img_url = item["url"]
            if img_url in cache:
                print(f"[{index}] SKIP (cached): {img_url}")
                continue
            print(f"[{index}] Downloading: {img_url}")
            try:
                saved_path = download_image(img_url, output_dir, index, page.url, args.timeout)
                print(f"     -> Saved: {saved_path.name}")
                cache[img_url] = {
                    "url": img_url,
                    "downloaded_at": time.strftime("%Y-%m-%dT%H:%M:%S"),
                    "source_page": url,
                }
                success += 1
            except requests.RequestException as exc:
                print(f"     -> Failed: {exc}")
                failed += 1
            except OSError as exc:
                print(f"     -> Failed: {exc}")
                failed += 1

            if args.delay > 0:
                time.sleep(args.delay)

        return success, failed
    except PlaywrightTimeoutError as exc:
        print(f"SKIP Timed out loading {url}: {exc}")
        return 0, 0
    finally:
        page.close()


def main():
    configure_output()
    parser = build_parser()
    args = parser.parse_args()

    if sync_playwright is None:
        print(
            "ERROR Playwright is not installed. Run: pip install playwright requests && playwright install chromium",
            file=sys.stderr,
        )
        return 1

    urls = load_urls(args)
    if not urls:
        print("ERROR Please provide at least one Facebook URL or --urls-file.", file=sys.stderr)
        return 1

    invalid_urls = [url for url in urls if not is_supported_url(url)]
    if invalid_urls:
        print("ERROR Unsupported URL(s):", file=sys.stderr)
        for url in invalid_urls:
            print(f"  - {url}", file=sys.stderr)
        return 1

    output_root = Path(args.output).expanduser().resolve()
    output_root.mkdir(parents=True, exist_ok=True)
    make_subdir = len(urls) > 1

    cache = load_cache(args.cache)
    total_success = 0
    total_failed = 0

    with sync_playwright() as playwright:
        browser_type = playwright.chromium
        if args.state_dir:
            state_dir = Path(args.state_dir).expanduser().resolve()
            state_dir.mkdir(parents=True, exist_ok=True)
            context = browser_type.launch_persistent_context(
                state_dir, headless=not args.headed
            )
        else:
            browser = browser_type.launch(headless=not args.headed)
            context = browser.new_context(
                user_agent=(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    "AppleWebKit/537.36 (KHTML, like Gecko) "
                    "Chrome/124.0.0.0 Safari/537.36"
                ),
                viewport={"width": 1400, "height": 2200},
            )
        try:
            for url in urls:
                success, failed = process_url(context, url, output_root, args, cache, make_subdir)
                total_success += success
                total_failed += failed
        finally:
            context.close()
            if not args.state_dir:
                browser.close()

    if args.cache:
        save_cache(args.cache, cache)

    print("\n" + "=" * 50)
    print(f"Done. Success: {total_success} | Failed: {total_failed} | Total: {total_success + total_failed}")
    print(f"Output root: {output_root}")
    if args.cache:
        print(f"Cache: {Path(args.cache).resolve()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
