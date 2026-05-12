export function parseSkillMarkdown(markdown: string): Record<string, unknown> {
  const match = markdown.match(/^---\s*\r?\n([\s\S]*?)\r?\n---/);
  if (!match) {
    return {};
  }

  const output: Record<string, unknown> = {};
  for (const rawLine of match[1].split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;
    const separator = line.indexOf(":");
    if (separator <= 0) continue;
    const key = line.slice(0, separator).trim();
    const value = line.slice(separator + 1).trim();
    output[key] = parseYamlScalar(value);
  }
  return output;
}

function parseYamlScalar(value: string): unknown {
  const unquoted = value.replace(/^["']|["']$/g, "");
  if (unquoted.startsWith("[") && unquoted.endsWith("]")) {
    return unquoted
      .slice(1, -1)
      .split(",")
      .map((item) => item.trim().replace(/^["']|["']$/g, ""))
      .filter(Boolean);
  }
  if (unquoted === "true") return true;
  if (unquoted === "false") return false;
  return unquoted;
}
