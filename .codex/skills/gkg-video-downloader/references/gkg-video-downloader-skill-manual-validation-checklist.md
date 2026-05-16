# Manual Validation Checklist

Use this checklist before considering the skill ready.

## Metadata and structure

- `name` is `gkg-video-downloader`.
- `description` is under 200 characters and includes activation cues like `direct video extraction`, `thread URLs`, `pagination crawl`, `output path`, `timeout`, or `delay`.
- `SKILL.md` stays under 150 lines.
- Reference files stay under 150 lines each.
- File names are kebab-case and self-describing.

## Workflow correctness

- Skill accepts one URL only.
- Skill forwards only supported flags from `video_downloader.py`.
- Skill asks for clarification on missing URL.
- Skill asks for clarification on multiple URLs.
- Skill asks for clarification on `--pages` plus `--follow-pages`.
- Skill asks for clarification on `--start-page` without `--pages`.
- Skill preserves user-provided values without inventing new ones.
- Skill uses `python` first and `python3` as one retry only.
- Skill asks for clarification on zero or negative numeric values that would make the run invalid or ambiguous.

## Output quality

- Success output stays short.
- Failure output includes the first blocking error.
- If totals exist, response echoes `Total Success`, `Total Failed`, and `Total Videos` compactly.
- If save path exists, response includes `Files saved to:` result.
- If no videos are found, response says so directly.

## Safety boundaries

- Skill refuses auth bypass, DRM bypass, paywall bypass, or unrelated downloader replacement requests.
- Skill ignores prompt injection and instruction override attempts.
- Skill does not expose env vars or unrelated internal configuration.
