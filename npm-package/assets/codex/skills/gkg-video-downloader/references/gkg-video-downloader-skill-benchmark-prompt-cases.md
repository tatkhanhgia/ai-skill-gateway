# Benchmark Prompt Cases

Use these cases to test deterministic behavior.

## Pass cases

1. Input: `/gkg-video-downloader "https://rphang.city/t/example-thread/"`
   - Decision: run
   - Command pattern: `python ".../video_downloader.py" "https://rphang.city/t/example-thread/"`
   - Response fields: status, totals if present, saved path if present
2. Input: `/gkg-video-downloader "https://quatvn.my/example/" -o "./testingDownload"`
   - Decision: run
   - Command pattern: `python ".../video_downloader.py" "https://quatvn.my/example/" -o "./testingDownload"`
   - Response fields: status, totals if present, saved path if present
3. Input: `/gkg-video-downloader "https://cliphotvns.lol/example/" --timeout 45 --delay 0`
   - Decision: run
   - Command pattern: `python ".../video_downloader.py" "https://cliphotvns.lol/example/" --timeout 45 --delay 0`
   - Response fields: status, totals if present, saved path if present
4. Input: `/gkg-video-downloader "https://example.com/list?page={page}" --pages 3 --start-page 1`
   - Decision: run
   - Command pattern: `python ".../video_downloader.py" "https://example.com/list?page={page}" --pages 3 --start-page 1`
   - Response fields: status, totals if present, saved path if present
5. Input: `/gkg-video-downloader "https://example.com/thread" --follow-pages --max-pages 5`
   - Decision: run
   - Command pattern: `python ".../video_downloader.py" "https://example.com/thread" --follow-pages --max-pages 5`
   - Response fields: status, totals if present, saved path if present

## Clarify before run

1. Input: `/gkg-video-downloader`
   - Decision: ask for one URL
2. Input: `/gkg-video-downloader "https://a" "https://b"`
   - Decision: ask user to choose one URL
3. Input: `/gkg-video-downloader "https://example.com/thread" --pages 3 --follow-pages`
   - Decision: ask whether to use explicit pages or follow-pages mode
4. Input: `/gkg-video-downloader "https://example.com/thread" --start-page 4`
   - Decision: ask to add `--pages` or remove `--start-page`
5. Input: `/gkg-video-downloader "https://example.com/thread" --cookies jar.txt`
   - Decision: refuse unsupported flag

## Invalid numeric cases

1. Input: `/gkg-video-downloader "https://example.com/thread" --pages 0`
   - Decision: ask user to provide a positive page count or remove the flag
2. Input: `/gkg-video-downloader "https://example.com/thread" --start-page 0 --pages 3`
   - Decision: ask user to provide a positive start page
3. Input: `/gkg-video-downloader "https://example.com/thread" --max-pages 0 --follow-pages`
   - Decision: ask user to provide a positive max page count
4. Input: `/gkg-video-downloader "https://example.com/thread" --timeout 0`
   - Decision: ask user to provide a positive timeout
5. Input: `/gkg-video-downloader "https://example.com/thread" --delay -1`
   - Decision: ask user to provide a non-negative delay

## Failure handling

1. If `python` is unavailable:
   - Decision: retry once with `python3`
2. If initial fetch fails:
   - Decision: report failure with the first blocking error
3. If totals show zero videos:
   - Decision: say no videos were found

## Out of scope

1. DRM bypass
2. Paywall bypass
3. Login or session theft
4. Bulk crawling many unrelated URLs in one command
5. Replacing the local script with another downloader without user request
