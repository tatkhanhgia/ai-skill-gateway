---
name: gpt-image-2-pro-max
description: "Production prompt-engineering pipeline for GPT-Image-2 / OpenAI image generation. Pairs a 'media-designer' agent with a hosted searchable corpus of 3,238 community-vetted prompts, decomposed across 10 controlled vocabularies (subjects, styles, lighting, cameras, moods, palettes, compositions, mediums, techniques, usecases). Each record carries: full prompt body, twitter/X attribution link, downloaded reference image. Workflow: agent diagnoses the user brief â†’ searches the corpus â†’ picks a mood-aligned base â†’ refactors the chosen prompt into a parameterised {argument} template â†’ resolves arguments from user intent â†’ returns the final paste-ready prompt with attribution + reference image. Use when the user wants a polished image-generation prompt for ads, posters, product shots, portraits, character sheets, UI mockups, infographics, exploded-view diagrams, or any other GPT-Image-2 / OpenAI image task."
category: ai
keywords: [gpt-image-2, image-prompts, prompt-library, openai-image, ai-image, prompt-engineering, ad-creative, product-photography, media-designer]
metadata:
  author: Richard Ng
  version: "1.0.0"
  endpoint: "https://gpt-image-2-prompts.goclawoffice.com"
---

# GPT-Image-2 Prompt Library + Media Designer Agent

Two-piece skill:

1. **`scripts/search.py`** â€” thin HTTP client over a hosted corpus of 3,238 community-vetted prompts. BM25-ranked, tagged across 10 facets.
2. **`~/.codex/agents/media_designer.toml`** â€” agent profile that *uses* the search tool to turn a user brief into a paste-ready GPT-Image-2 prompt.

The tool finds candidates. The agent owns the judgement (which base, which slots to parameterise, which to keep literal, mood/palette fit).

## When to Apply

### Must use
- User wants a GPT-Image-2 / OpenAI image-generation prompt for a real production task (ad, poster, product shot, character sheet, UI mockup, portrait)
- User describes a brief and wants a polished prompt back, not just inspiration
- User is studying how top creators structure prompts and wants attributed examples

### Skip
- User wants the image **rendered** â€” route to `ai-multimodal` or `ai-artist`
- Task unrelated to image prompts
- User already has a finished prompt and just wants it run

## Recommended Workflow

For any production prompt request:

```
1. Read ~/.codex/agents/media_designer.toml
2. Run the 6-step workflow it defines
3. Return the 4-block output (Base Â· Parameterised Â· Resolved Â· Rationale)
```

## Hosted backend

```
Endpoint: https://gpt-image-2-prompts.goclawoffice.com
```

Thousands of community-vetted prompts indexed across 10 facets (subjects, styles, lighting, cameras, moods, palettes, compositions, mediums, techniques, usecases). Each record carries the prompt body, attribution, and a reference image. Rate-limited per IP â€” fair-use friendly, but please don't scrape.

## CLI

```
search.py [query] [--shape SHAPE] [--has-image] [-n N] [--full] [--persist PATH]
```

```bash
# Use the shared skill venv on Windows / POSIX:
#   Windows : %USERPROFILE%\.codex\skills\.venv\Scripts\python.exe
#   POSIX   : ~/.codex/skills/.venv/bin/python3
# Examples below use the Windows path; swap on POSIX.

# Free-text search (this is what the agent calls)
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\gpt-image-2-pro-max\scripts\search.py "luxury shoe ecommerce ad cream pastel" -n 5

# Narrow by shape when the brief is specific about format
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\gpt-image-2-pro-max\scripts\search.py "perfume bottle" --shape ecommerce -n 3

# Persist top hits as a markdown reference deck (with embedded images)
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\gpt-image-2-pro-max\scripts\search.py "neon ui" --persist plans\neon-refs.md
```

Filter knobs:
- `--shape` â€” portrait | poster | ui | character | comparison | ecommerce | ad | thumbnail | infographic | comic
- `--has-image` â€” only records with a reference image
- `-n N` â€” top N (default 5)
- `--full` â€” don't truncate prompt body
- `--persist PATH` â€” write top results to a markdown file with embedded reference images

## Output Anatomy

```
#1  bm25=-15.59  shape=ecommerce
  id    : z9q36mnc
  title : Futuristic Bionic Super Shoe
  author: @<creator>
  tweet : https://x.com/<creator>/status/<tweet_id>
  image : <reference image URL>
  tags  : subjects=product,fashion-item | styles=cinematic | cameras=low-angle |
          moods=luxurious,intense,futuristic | palettes=gold-black |
          techniques=parameterised-template
  prompt:
    Extreme futuristic {argument name="subject" default="cheetah bionic super shoe"} ...
```

## Agent Profile

`~/.codex/agents/media_designer.toml` defines the canonical workflow. Headline contents:

| Section | Purpose |
|---|---|
| Mental model | `brief â†’ diagnose â†’ search â†’ pick (mood-aware) â†’ refactor â†’ resolve â†’ output` |
| Step 1 â€” Diagnose | Extract product, brand, shape, mood, palette, technique signals from the brief |
| Step 2 â€” Search | Synthesise tokens, run `%USERPROFILE%/.codex/skills/.venv/Scripts/python.exe %USERPROFILE%/.codex/skills/gpt-image-2-pro-max/scripts/search.py "<tokens>" -n 5` |
| Step 3 â€” Pick | Mood-mismatch rejection table â€” pastel briefs reject `moody/gritty/dark-amber`, etc. |
| Step 4 â€” Refactor | Replace product-specifics with `{argument name="X" default="Y"}` slots; keep mood/lighting/style words literal |
| Step 5 â€” Resolve | Fill slots from user intent; default-fallback when ambiguous; never invent |
| Step 6 â€” Output | 4 blocks: Base (cite author + tweet) Â· Parameterised Â· Resolved Â· Rationale (â‰¤80 words) |

