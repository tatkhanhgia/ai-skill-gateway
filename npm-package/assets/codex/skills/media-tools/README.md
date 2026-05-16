# media-tools

Two CLI tools for AI image generation and vision analysis. Python ports of [goclaw](https://github.com/) `create_image` / `read_image` with the same provider-chain pattern: priority order â†’ skip if no API key â†’ first success wins â†’ cascade on failure.

> **Looking for the agent-facing spec?** See [`SKILL.md`](./SKILL.md). This README is the human quickstart.

---

## Why this exists

OpenCode host model may have image understanding, but you may want to:

- Generate images (host model may not generate images directly) â€” pick from OpenRouter, Gemini, OpenAI gpt-image-2, MiniMax, DashScope, BytePlus, **or your existing ChatGPT Plus / Codex subscription** (no extra API key needed)
- Force a specific vision model (Gemini for OCR, Anthropic for nuanced reasoning, etc.)
- Embed prompts into PNG metadata for traceability
- Test multi-provider failover behavior

---

## Install

One-time setup of the shared skill venv:

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe -m pip install -r %USERPROFILE%\.codex\skills\media-tools\requirements.txt
```

Only dependency: `requests`.

---

## Configure API keys

Copy the template and fill in whatever providers you have:

```powershell
copy %USERPROFILE%\.codex\skills\media-tools\.env.example %USERPROFILE%\.codex\skills\media-tools\.env
notepad %USERPROFILE%\.codex\skills\media-tools\.env
```

The loader searches in this priority order:

1. `$MEDIA_TOOLS_ENV` (override path)
2. `%USERPROFILE%/.codex/skills/media-tools/.env` â† **default, recommended**
3. `%APPDATA%\media-tools\.env` (Windows) / `~/.config/media-tools/.env` (POSIX)

OS env vars always win over the file (safe for CI). The file is gitignored automatically.

### Or skip API keys entirely â€” use ChatGPT Plus

If you already use the [Codex CLI](https://github.com/openai/codex), media-tools picks up its OAuth session automatically:

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\chatgpt_oauth_login.py
# â†’ "ChatGPT OAuth: already logged in. source: ~/.codex/auth.json"
```

If Codex isn't installed, the same script runs the OAuth flow and saves tokens to `~/.codex/auth.json` (so future Codex installs Just Work).

---

## Generate an image

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\create_image.py `
  --prompt "a cyberpunk cat in neon rain, ukiyo-e style" `
  --aspect-ratio 16:9 `
  --filename-hint neon-cat
```

Common flags:

| Flag | What |
|---|---|
| `--prompt` | text description (or use `--prompt-file`) |
| `--prompt-file` | read prompt from file (UTF-8); use `-` for stdin. Best for long / multi-line / non-ASCII prompts on Windows. |
| `--aspect-ratio` | `1:1` `3:4` `4:3` `9:16` `16:9` (default `1:1`) |
| `--filename-hint` | kebab-slug for output filename. **Auto-derived from first 5 words of prompt if omitted.** |
| `--provider` | force one provider: `chatgpt_oauth\|openrouter\|gemini\|openai\|minimax\|dashscope\|byteplus` |
| `--provider-order` | comma-separated chain override (also reads `$MEDIA_TOOLS_IMAGE_ORDER` / `$MEDIA_TOOLS_PROVIDER_ORDER`) |
| `--model` | override the model for the chosen provider (e.g. `--model gemini-3-pro-image-preview` for Banana Pro) |
| `--image-model` | `chatgpt_oauth` only â€” image model whitelist: `gpt-image-2` (default), `gpt-image-1.5` (legacy) |
| `--workspace` | output dir (default `$WEB_TOOLS_WORKSPACE` or OS temp) |
| `--reference-image` | seed generation with a reference (PNG/JPG/WEBP). Repeatable; up to 4. Only `chatgpt_oauth`, `gemini`, `openai` support this â€” the chain auto-skips others with a clear message. |
| `--list-providers` | print which providers are configured right now (âœ“/âœ—) and exit. Use to diagnose "no providers callable" errors. |

Output lands at `<workspace>/generated/<YYYY-MM-DD>/<slug>-<ts>-<rand>.png`. The full prompt is embedded in the PNG `tEXt` chunk so you can retrieve provenance later.

### Long-prompt example (Windows-friendly)

```powershell
# Save the prompt to a file once, reuse from CLI â€” avoids PowerShell argv quoting hell.
Set-Content -Encoding utf8 prompt.txt "má»™t quÃ¡n cÃ  phÃª Viá»‡t Nam buá»•i sÃ¡ng sá»›m, Ã¡nh náº¯ng vÃ ng xuyÃªn qua cá»­a sá»•, photoreal"

%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\create_image.py `
  --prompt-file prompt.txt --aspect-ratio 1:1
# â†’ filename auto-derived: mt-qun-c-ph-vit-...png
```

### Use a reference image (image-to-image)

Pass one or more reference images to seed generation. Useful for restyling, character-consistency, or product-shot edits.

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\create_image.py `
  --prompt "Same character, now wearing a red trench coat, neon Tokyo street at night" `
  --reference-image .\character.png `
  --aspect-ratio 9:16
```

Repeat the flag for multi-reference (e.g. character + style mood-board):

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\create_image.py `
  --prompt "Hero shot in the style of the moodboard" `
  --reference-image .\character.png `
  --reference-image .\moodboard.jpg
```

Supported by `chatgpt_oauth`, `gemini`, `openai`. PNG/JPG/WEBP only, max 4 images per call. Other providers in the chain are auto-skipped when refs are present.

### Pin a default provider

```powershell
# Always prefer Gemini first, fall back to chatgpt_oauth:
$env:MEDIA_TOOLS_IMAGE_ORDER = "gemini,chatgpt_oauth"
```

The CLI flag wins over env, env wins over the built-in default.

---

## Analyze an image

```powershell
%USERPROFILE%\.codex\skills\.venv\Scripts\python.exe %USERPROFILE%\.codex\skills\media-tools\scripts\read_image.py `
  --path "C:\path\to\image.png" `
  --prompt "Describe this image. What text appears?"
```

Vision chain: `openrouter â†’ gemini â†’ anthropic â†’ dashscope`. Pick a specific one with `--provider`. Override default order via `$MEDIA_TOOLS_VISION_ORDER` or `$MEDIA_TOOLS_PROVIDER_ORDER`.

Also supports `--prompt-file` (long questions), `--list-providers` (diagnostic), and `--max-tokens` / `--temperature` (response shaping).

Accepts JPG / PNG / GIF / WebP / BMP, â‰¤10 MB.

---

## Vietnamese / non-ASCII output

Both entry scripts force UTF-8 stdout/stderr at startup, so prompts with diacritics (`Ä‘`, `Äƒ`, `Æ°`), Chinese, or emoji print correctly on Windows without setting `PYTHONIOENCODING`. If you embed media-tools into your own Python wrapper that bypasses the entry scripts, replicate `sys.stdout.reconfigure(encoding="utf-8", errors="replace")` yourself.

---

## Provider matrix

| Provider | Image gen | Vision | Auth | Default model |
|---|---|---|---|---|
| `chatgpt_oauth` | âœ“ (gpt-image-2) | â€” | OAuth (Codex session) | `gpt-5.4` parent + `gpt-image-2` |
| `openrouter` | âœ“ | âœ“ | `OPENROUTER_API_KEY` | `google/gemini-2.5-flash-image` |
| `gemini` | âœ“ | âœ“ | `GEMINI_API_KEY` | `gemini-2.5-flash-image` (or `gemini-3-pro-image-preview` for Banana Pro) |
| `openai` | âœ“ | â€” | `OPENAI_API_KEY` | `gpt-image-1.5` |
| `anthropic` | â€” | âœ“ | `ANTHROPIC_API_KEY` | `claude-sonnet-4-6` |
| `minimax` | âœ“ | â€” | `MINIMAX_API_KEY` | `image-01` |
| `dashscope` | âœ“ | âœ“ | `DASHSCOPE_API_KEY` | `wan2.6-image` / `qwen3-vl` |
| `byteplus` | âœ“ | â€” | `BYTEPLUS_API_KEY` | `seedream-5-0-260128` |

When no `--provider` is forced, the chain runs in this order: `chatgpt_oauth â†’ openrouter â†’ gemini â†’ openai â†’ minimax â†’ dashscope â†’ byteplus` for image; `openrouter â†’ gemini â†’ anthropic â†’ dashscope` for vision.

---

## File layout

```
%USERPROFILE%/.codex/skills/media-tools/
â”œâ”€â”€ README.md              â† you are here
â”œâ”€â”€ SKILL.md               â† agent-facing spec
â”œâ”€â”€ .env.example           â† template (copy to .env)
â”œâ”€â”€ requirements.txt
â””â”€â”€ scripts/
    â”œâ”€â”€ create_image.py            â† entry CLI
    â”œâ”€â”€ read_image.py              â† entry CLI
    â”œâ”€â”€ chatgpt_oauth_login.py     â† Codex/ChatGPT OAuth flow
    â””â”€â”€ lib/
        â”œâ”€â”€ chain.py               â† provider-chain runner
        â”œâ”€â”€ env_keys.py            â† API-key resolution
        â”œâ”€â”€ dotenv_loader.py       â† .env auto-loader
        â”œâ”€â”€ chatgpt_oauth_token.py â† OAuth storage + refresh (shared with ~/.codex/auth.json)
        â”œâ”€â”€ png_metadata.py        â† PNG tEXt prompt embedding
        â”œâ”€â”€ filename.py            â† date-folder + slug helpers
        â””â”€â”€ providers/
            â”œâ”€â”€ chatgpt_oauth_image.py
            â”œâ”€â”€ openrouter_image.py Â· openrouter_vision.py
            â”œâ”€â”€ gemini_image.py    Â· gemini_vision.py
            â”œâ”€â”€ openai_image.py
            â”œâ”€â”€ anthropic_vision.py
            â”œâ”€â”€ minimax_image.py
            â”œâ”€â”€ dashscope_image.py Â· dashscope_vision.py
            â””â”€â”€ byteplus_image.py
```

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `chain: <provider> failed: API error 401` | Wrong / expired API key â€” re-check `.env` |
| `chain: chatgpt_oauth failed: No ChatGPT OAuth session` | Run `chatgpt_oauth_login.py` (or install Codex CLI and login) â€” error message lists the search paths |
| `chain: all providers skipped (no API keys)` | Run `--list-providers` to see who's configured. Then either set at least one `*_API_KEY` in `.env` or login via Codex. |
| All providers fail with same error | Check internet / firewall / proxy; try `--provider <one>` to isolate |
| Long Vietnamese / Chinese prompt fails to pass via PowerShell argv | Use `--prompt-file path\to\prompt.txt` instead â€” sidesteps shell quoting |
| Filename has weird random suffix | That's the `-<ts>-<rand>` collision-avoidance suffix. Use `--filename-hint my-name` to control the prefix. |

---

## Security notes

- `.env` is gitignored at the repo root (`.env*` + `!.env.example`).
- OAuth tokens live **outside the repo**: `~/.codex/auth.json` and `%APPDATA%\media-tools\` (or `~/.config/media-tools/`).
- Generated images save to your OS temp dir, not the repo.
- No telemetry, no third-party services beyond the providers you explicitly configure.
