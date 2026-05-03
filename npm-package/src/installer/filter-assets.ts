import type { AssetManifestFile } from '../types.js';

const groupByType = new Map<string, Set<AssetManifestFile['type']>>([
  ['claude', new Set(['claude-skill', 'claude-agent', 'claude-hook', 'claude-rule', 'claude-script'])],
  ['opencode', new Set(['opencode-skill', 'opencode-agent'])],
  ['skills', new Set(['claude-skill', 'opencode-skill'])],
  ['agents', new Set(['claude-agent', 'opencode-agent'])],
  ['hooks', new Set(['claude-hook'])],
  ['rules', new Set(['claude-rule'])],
  ['scripts', new Set(['claude-script'])]
]);

export function filterAssets(files: AssetManifestFile[], groups: string[] = []): AssetManifestFile[] {
  if (groups.length === 0) return files;

  const allowedTypes = new Set<AssetManifestFile['type']>();
  for (const group of groups) {
    const types = groupByType.get(group);
    if (!types) continue;
    for (const type of types) allowedTypes.add(type);
  }

  if (allowedTypes.size === 0) return files;
  return files.filter((file) => allowedTypes.has(file.type));
}
