#!/usr/bin/env python3
"""
Video Downloader - Detect and download direct video files from web pages.

Usage:
    # Single page
    python video_downloader.py "https://example.com/thread"

    # Auto-detect pagination (follow Next links up to 50 pages)
    python video_downloader.py "https://example.com/thread" --follow-pages

    # Specify exact number of pages via URL template
    python video_downloader.py "https://example.com/thread/page-{page}" --pages 5

    # Save to a custom output directory
    python video_downloader.py "https://example.com/thread" -o ./vids --follow-pages

Note: Respect robots.txt and website Terms of Service.
"""

import argparse
import asyncio
import hashlib
import html
import json
import re
import sys
import time
import urllib.parse
from pathlib import Path

import requests

try:
    from playwright.async_api import async_playwright
except ImportError:
    async_playwright = None

DEFAULT_MAX_PAGES = 50
VIDEO_EXTENSIONS = ("mp4", "webm", "mov", "mkv", "avi", "m4v")
UNWANTED_VIDEO_MARKERS = (
    "catfishpc",
    "sambannerunit",
    "footer_fixed",
    "data-position=\"footer_fixed\"",
    "data-position='footer_fixed'",
)
FORUM_CONTENT_MARKERS = (
    '<article class="message-body',
    '<div class="bbWrapper">',
    '<section class="message-attachments">',
)
FORUM_HOST_MARKERS = (
    'rphang.city',
    'rphang.love',
    'rphang',
)
COMMON_BROWSER_PATHS = (
    r'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe',
    r'C:\Program Files\Microsoft\Edge\Application\msedge.exe',
    r'C:\Program Files\Google\Chrome\Application\chrome.exe',
    r'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe',
)
BLOGGER_MEDIA_REFERER = 'https://youtube.googleapis.com/'
BLOGGER_MEDIA_USER_AGENT = (
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
    'AppleWebKit/537.36 (KHTML, like Gecko) '
    'HeadlessChrome/147.0.0.0 Safari/537.36 Edg/147.0.0.0'
)


def configure_output():
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")



def fetch_html(url, timeout=30):
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/120.0.0.0 Safari/537.36"
        ),
        "Accept": (
            "text/html,application/xhtml+xml,application/xml;"
            "q=0.9,video/webm,video/mp4,*/*;q=0.8"
        ),
        "Accept-Language": "en-US,en;q=0.9",
    }
    resp = requests.get(url, headers=headers, timeout=timeout)
    resp.raise_for_status()
    return resp.text



def find_next_page_url(html, base_url):
    m = re.search(
        r'<link[^>]+rel=["\']next["\'][^>]+href=["\']([^"\']+)["\']',
        html, re.IGNORECASE,
    )
    if m:
        return urllib.parse.urljoin(base_url, m.group(1).strip())

    m = re.search(
        r'<link[^>]+href=["\']([^"\']+)["\'][^>]+rel=["\']next["\']',
        html, re.IGNORECASE,
    )
    if m:
        return urllib.parse.urljoin(base_url, m.group(1).strip())

    a_tags = re.findall(r'<a[^>]*?>.*?</a>', html, re.IGNORECASE | re.DOTALL)
    for tag in a_tags:
        text = re.sub(r'<[^>]+>', '', tag).strip().lower()
        if any(k in text for k in ('next', '›', '»', '>>', 'trang sau', '下一页', '次へ')):
            m = re.search(r'href=["\']([^"\']+)["\']', tag, re.IGNORECASE)
            if m:
                href = m.group(1).strip()
                if href not in ('#', 'javascript:void(0)', ''):
                    return urllib.parse.urljoin(base_url, href)

    m = re.search(
        r'<a[^>]+class=["\'][^"\']*\bnext\b[^"\']*["\'][^>]+href=["\']([^"\']+)["\']',
        html, re.IGNORECASE,
    )
    if m:
        href = m.group(1).strip()
        if href not in ('#', 'javascript:void(0)', ''):
            return urllib.parse.urljoin(base_url, href)

    return None



def sanitize_dirname(name):
    name = re.sub(r'[^\w\-]', '_', name).strip('_')
    if not name:
        name = 'video_thread'
    return name[:80]



def extract_title(html, default='video_thread'):
    m = re.search(r'<title[^>]*>(.*?)</title>', html, re.IGNORECASE | re.DOTALL)
    if m:
        title = re.sub(r'<[^>]+>', '', m.group(1)).strip()
        if title:
            return sanitize_dirname(title)

    m = re.search(r'<h1[^>]*>(.*?)</h1>', html, re.IGNORECASE | re.DOTALL)
    if m:
        title = re.sub(r'<[^>]+>', '', m.group(1)).strip()
        if title:
            return sanitize_dirname(title)

    return default



def get_extension_from_url(url, content_type=None):
    path = urllib.parse.urlparse(url).path.lower()
    ext = Path(path).suffix
    if ext and len(ext) <= 5:
        return ext

    mime_to_ext = {
        'video/mp4': '.mp4',
        'video/webm': '.webm',
        'video/quicktime': '.mov',
        'video/x-msvideo': '.avi',
        'video/x-matroska': '.mkv',
    }
    if content_type:
        ct = content_type.split(';')[0].strip().lower()
        if ct in mime_to_ext:
            return mime_to_ext[ct]
    return '.mp4'



def safe_filename(url, content_type=None, index=0):
    parsed = urllib.parse.urlparse(url)
    path = urllib.parse.unquote(parsed.path)
    basename = Path(path).name
    basename = re.sub(r"[^\w\-.]", "_", basename)

    if not basename or basename.startswith('.'):
        h = hashlib.md5(url.encode('utf-8')).hexdigest()[:12]
        ext = get_extension_from_url(url, content_type)
        basename = f'video_{index:04d}_{h}{ext}'
    elif not Path(basename).suffix:
        basename += get_extension_from_url(url, content_type)

    return basename



def _clean_html_fragment(fragment):
    return re.sub(r'\s+', ' ', fragment or '').strip().lower()



def _looks_like_unwanted_video(fragment):
    lowered = _clean_html_fragment(fragment)
    return any(marker in lowered for marker in UNWANTED_VIDEO_MARKERS)



def resolve_site_profile(base_url):
    netloc = urllib.parse.urlparse(base_url).netloc.lower()
    use_content_ranges = any(marker in netloc for marker in FORUM_HOST_MARKERS)
    return {
        'use_content_ranges': use_content_ranges,
        'content_markers': FORUM_CONTENT_MARKERS,
        'unwanted_markers': UNWANTED_VIDEO_MARKERS,
    }



def _find_content_ranges(html, markers):
    ranges = []
    for marker in markers:
        for match in re.finditer(re.escape(marker), html, re.IGNORECASE):
            start = match.start()
            end = min(len(html), start + 12000)
            ranges.append((start, end))
    return ranges



def _position_in_ranges(position, ranges):
    return any(start <= position < end for start, end in ranges)



def _in_scope(position, ranges, use_content_ranges):
    if not use_content_ranges:
        return True
    if not ranges:
        return True
    return _position_in_ranges(position, ranges)



def _iter_video_block_urls(html, base_url, ranges, use_content_ranges):
    urls = []
    video_pattern = re.compile(r'<video\b[^>]*?>.*?</video>', re.IGNORECASE | re.DOTALL)
    for match in video_pattern.finditer(html):
        block = match.group(0)
        if _looks_like_unwanted_video(block):
            continue
        if not _in_scope(match.start(), ranges, use_content_ranges):
            continue

        video_src = re.search(r'<video\b[^>]*\ssrc=["\']([^"\']+)["\']', block, re.IGNORECASE)
        if video_src:
            candidate = video_src.group(1).strip()
            if candidate and not candidate.startswith('blob:'):
                urls.append(urllib.parse.urljoin(base_url, candidate))

        source_tags = re.findall(r'<source\b[^>]*\ssrc=["\']([^"\']+)["\']', block, re.IGNORECASE)
        for src in source_tags:
            candidate = src.strip()
            if candidate and not candidate.startswith('blob:'):
                urls.append(urllib.parse.urljoin(base_url, candidate))
    return urls



def _decode_data_item(raw_value):
    decoded = html.unescape(raw_value)
    try:
        payload = json.loads(decoded)
        if isinstance(payload, dict):
            return payload
    except json.JSONDecodeError:
        pass
    return None



def _iter_flowplayer_urls(html_text, base_url, ranges, use_content_ranges):
    urls = []
    patterns = [
        re.compile(r'<[^>]+class=["\'][^"\']*flowplayer[^"\']*["\'][^>]*\sdata-item=["\']([^"\']+)["\']', re.IGNORECASE),
        re.compile(r'<[^>]*\sdata-item=["\']([^"\']+)["\'][^>]*class=["\'][^"\']*flowplayer[^"\']*["\']', re.IGNORECASE),
    ]
    seen_raw = set()
    for pattern in patterns:
        for match in pattern.finditer(html_text):
            if not _in_scope(match.start(), ranges, use_content_ranges):
                continue

            raw_value = match.group(1)
            if raw_value in seen_raw:
                continue
            seen_raw.add(raw_value)

            payload = _decode_data_item(raw_value)
            if payload:
                for source in payload.get('sources', []):
                    if not isinstance(source, dict):
                        continue
                    src = source.get('src')
                    if src:
                        urls.append(urllib.parse.urljoin(base_url, src.strip()))
                src = payload.get('src')
                if src:
                    urls.append(urllib.parse.urljoin(base_url, src.strip()))
                continue

            decoded = html.unescape(raw_value).replace('\\/', '/')
            for fallback in re.findall(r'https?://[^\s"\'<>]+\.(?:' + '|'.join(VIDEO_EXTENSIONS) + r')(?:\?[^\s"\'<>]*)?', decoded, re.IGNORECASE):
                urls.append(fallback.strip())
    return urls



def _iter_anchor_urls(html_text, base_url, ranges, use_content_ranges):
    urls = []
    anchor_pattern = re.compile(
        r'<a[^>]+href=["\']([^"\']+\.(?:' + '|'.join(VIDEO_EXTENSIONS) + r')(?:\?[^"\']*)?)["\']',
        re.IGNORECASE,
    )
    for match in anchor_pattern.finditer(html_text):
        if not _in_scope(match.start(), ranges, use_content_ranges):
            continue
        urls.append(urllib.parse.urljoin(base_url, match.group(1).strip()))
    return urls



def _iter_attr_urls(html_text, base_url, ranges, use_content_ranges):
    urls = []
    attr_pattern = re.compile(
        r'(?:src|data-src|data-video|data-url)=["\']([^"\']+\.(?:' + '|'.join(VIDEO_EXTENSIONS) + r')(?:\?[^"\']*)?)["\']',
        re.IGNORECASE,
    )
    for match in attr_pattern.finditer(html_text):
        if not _in_scope(match.start(), ranges, use_content_ranges):
            continue
        urls.append(urllib.parse.urljoin(base_url, match.group(1).strip()))
    return urls



def _iter_text_urls(html_text, ranges, use_content_ranges):
    urls = []
    text_pattern = re.compile(
        r'https?://[^\s"\'<>]+\.(?:' + '|'.join(VIDEO_EXTENSIONS) + r')(?:\?[^\s"\'<>]*)?',
        re.IGNORECASE,
    )
    for match in text_pattern.finditer(html_text):
        if not _in_scope(match.start(), ranges, use_content_ranges):
            continue
        urls.append(match.group(0).strip())
    return urls



def _iter_blogger_iframe_urls(html_text, base_url):
    urls = []
    iframe_pattern = re.compile(
        r'<iframe[^>]+src=["\']([^"\']+blogger\.com/video\.g\?token=[^"\']+)["\']',
        re.IGNORECASE,
    )
    for match in iframe_pattern.finditer(html_text):
        urls.append(urllib.parse.urljoin(base_url, html.unescape(match.group(1).strip())))
    return urls



def _find_browser_executable():
    for browser_path in COMMON_BROWSER_PATHS:
        if Path(browser_path).exists():
            return browser_path
    return None



async def _capture_blogger_media_url(page_url, iframe_url, browser_path, timeout):
    hits = []
    async with async_playwright() as playwright:
        browser = await playwright.chromium.launch(headless=True, executable_path=browser_path)
        try:
            page = await browser.new_page()

            def remember(url):
                lowered = url.lower()
                if any(key in lowered for key in ('googlevideo', 'videoplayback')) or '.mp4' in lowered or '.m3u8' in lowered:
                    hits.append(url)

            page.on('request', lambda req: remember(req.url))
            page.on('response', lambda resp: remember(resp.url))

            await page.goto(page_url, wait_until='networkidle', timeout=timeout * 1000)
            frame = next((frame for frame in page.frames if iframe_url in frame.url), None)
            if not frame:
                return []

            try:
                await frame.click('body', timeout=5000)
            except Exception:
                pass

            await page.wait_for_timeout(8000)
            return hits
        finally:
            await browser.close()



def _extract_blogger_media_urls(html_text, base_url, timeout):
    iframe_urls = _iter_blogger_iframe_urls(html_text, base_url)
    if not iframe_urls or async_playwright is None:
        return []

    browser_path = _find_browser_executable()
    if not browser_path:
        return []

    urls = []
    for iframe_url in iframe_urls:
        try:
            hits = asyncio.run(_capture_blogger_media_url(base_url, iframe_url, browser_path, timeout))
        except Exception:
            continue
        urls.extend(hits)
    return urls



def _is_video_candidate_url(url):
    lowered = url.lower()
    if not lowered.startswith(('http://', 'https://')):
        return False
    if any(lowered.endswith('.' + ext) or f'.{ext}?' in lowered for ext in VIDEO_EXTENSIONS):
        return True
    if any(marker in lowered for marker in ('googlevideo.com/videoplayback', 'mime=video%2fmp4', 'mime=video/mp4', 'mime=video%2fwebm', 'mime=video/webm')):
        return True
    return False



def extract_video_urls(html_text, base_url, timeout=30):
    profile = resolve_site_profile(base_url)
    ranges = _find_content_ranges(html_text, profile['content_markers']) if profile['use_content_ranges'] else []

    urls = []
    urls.extend(_iter_video_block_urls(html_text, base_url, ranges, profile['use_content_ranges']))
    urls.extend(_iter_flowplayer_urls(html_text, base_url, ranges, profile['use_content_ranges']))
    urls.extend(_iter_anchor_urls(html_text, base_url, ranges, profile['use_content_ranges']))
    urls.extend(_iter_attr_urls(html_text, base_url, ranges, profile['use_content_ranges']))
    urls.extend(_iter_text_urls(html_text, ranges, profile['use_content_ranges']))
    if not urls:
        urls.extend(_extract_blogger_media_urls(html_text, base_url, timeout))

    deduped = []
    seen = set()
    for url in urls:
        lowered = url.lower()
        if lowered in seen:
            continue
        if not _is_video_candidate_url(url):
            continue
        seen.add(lowered)
        deduped.append(url)

    return deduped



def download_video(url, output_dir, index, timeout=30):
    lowered = url.lower()
    referer = urllib.parse.urljoin(url, '/')
    user_agent = (
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) '
        'AppleWebKit/537.36 (KHTML, like Gecko) '
        'Chrome/120.0.0.0 Safari/537.36'
    )
    if 'googlevideo.com/videoplayback' in lowered:
        referer = BLOGGER_MEDIA_REFERER
        user_agent = BLOGGER_MEDIA_USER_AGENT

    headers = {
        'User-Agent': user_agent,
        'Accept': 'video/webm,video/mp4,video/*,*/*;q=0.8',
        'Referer': referer,
    }
    if 'googlevideo.com/videoplayback' in lowered:
        headers['Range'] = 'bytes=0-'

    try:
        resp = requests.get(url, headers=headers, timeout=timeout, stream=True)
        resp.raise_for_status()
    except requests.RequestException as exc:
        return False, str(exc)

    content_type = resp.headers.get('Content-Type', '')
    filename = safe_filename(url, content_type, index)
    filepath = output_dir / filename

    counter = 1
    stem = Path(filename).stem
    suffix = Path(filename).suffix
    while filepath.exists():
        filepath = output_dir / f'{stem}_{counter:03d}{suffix}'
        counter += 1

    try:
        with open(filepath, 'wb') as f:
            for chunk in resp.iter_content(chunk_size=1024 * 256):
                if chunk:
                    f.write(chunk)
        return True, filepath.name
    except OSError as exc:
        return False, str(exc)



def process_single_page(url, output_dir, global_index, timeout, delay, seen_video_urls):
    print(f'\nFetching page: {url}')
    try:
        html = fetch_html(url, timeout=timeout)
    except requests.RequestException as exc:
        print(f'ERROR: Failed to fetch page: {exc}', file=sys.stderr)
        return 0, 0, global_index, None

    video_urls = extract_video_urls(html, url, timeout=timeout)
    new_video_urls = [video_url for video_url in video_urls if video_url.lower() not in seen_video_urls]
    print(f'Found {len(new_video_urls)} new video(s) on this page.')

    success_count = 0
    fail_count = 0
    idx = global_index

    for video_url in new_video_urls:
        seen_video_urls.add(video_url.lower())
        idx += 1
        print(f'[{idx}] Downloading: {video_url}')
        ok, info = download_video(video_url, output_dir, idx, timeout=timeout)
        if ok:
            print(f'         -> Saved: {info}')
            success_count += 1
        else:
            print(f'         -> Failed: {info}')
            fail_count += 1

        if delay > 0:
            time.sleep(delay)

    next_url = find_next_page_url(html, url)
    return success_count, fail_count, idx, next_url



def main():
    configure_output()

    parser = argparse.ArgumentParser(
        description='Detect and download direct video files from web pages.'
    )
    parser.add_argument('url', help='Target web page URL. Use {page} placeholder if --pages is set.')
    parser.add_argument('-o', '--output', default='downloaded_videos', help='Output directory')
    parser.add_argument('--delay', type=float, default=0.5, help='Delay between downloads (seconds)')
    parser.add_argument('--timeout', type=int, default=30, help='Request timeout (seconds)')
    parser.add_argument(
        '--follow-pages', action='store_true',
        help='Auto-detect and follow pagination links (Next page).'
    )
    parser.add_argument(
        '--pages', type=int, default=0,
        help=(
            'Number of pages to crawl. If >0 and URL contains {page}, '
            'it will be substituted with page numbers. Otherwise it appends ?page=N.'
        ),
    )
    parser.add_argument(
        '--max-pages', type=int, default=DEFAULT_MAX_PAGES,
        help=f'Maximum pages to crawl when using --follow-pages (default {DEFAULT_MAX_PAGES}).'
    )
    parser.add_argument(
        '--start-page', type=int, default=1,
        help='Start page number when using --pages (default 1).'
    )
    args = parser.parse_args()

    output_root = Path(args.output)
    output_root.mkdir(parents=True, exist_ok=True)

    try:
        first_html = fetch_html(args.url, timeout=args.timeout)
    except requests.RequestException as exc:
        print(f'ERROR: Failed to fetch initial page: {exc}', file=sys.stderr)
        sys.exit(1)

    thread_dir = output_root / extract_title(first_html)
    thread_dir.mkdir(parents=True, exist_ok=True)

    total_success = 0
    total_fail = 0
    global_index = 0
    visited_pages = set()
    seen_video_urls = set()

    if args.pages > 0:
        has_template = '{page}' in args.url
        for page_num in range(args.start_page, args.start_page + args.pages):
            if has_template:
                page_url = args.url.replace('{page}', str(page_num))
            else:
                sep = '&' if '?' in args.url else '?'
                page_url = f'{args.url}{sep}page={page_num}'

            if page_url in visited_pages:
                continue
            visited_pages.add(page_url)

            s, f, global_index, _ = process_single_page(
                page_url, thread_dir, global_index, args.timeout, args.delay, seen_video_urls
            )
            total_success += s
            total_fail += f
    elif args.follow_pages:
        current_url = args.url
        page_count = 0
        while current_url and page_count < args.max_pages:
            if current_url in visited_pages:
                print(f'Already visited {current_url}, stopping.')
                break
            visited_pages.add(current_url)
            page_count += 1

            s, f, global_index, next_url = process_single_page(
                current_url, thread_dir, global_index, args.timeout, args.delay, seen_video_urls
            )
            total_success += s
            total_fail += f

            if not next_url:
                print('No next page found.')
                break
            current_url = next_url
    else:
        s, f, global_index, _ = process_single_page(
            args.url, thread_dir, global_index, args.timeout, args.delay, seen_video_urls
        )
        total_success += s
        total_fail += f

    print('\n' + '=' * 50)
    print('Done!')
    print(f'Total Success: {total_success} | Total Failed: {total_fail} | Total Videos: {total_success + total_fail}')
    print(f'Files saved to: {thread_dir.resolve()}')


if __name__ == '__main__':
    main()
