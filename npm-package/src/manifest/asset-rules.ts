import path from 'node:path';
import type { AssetType } from '../types.js';

export interface AssetRoot {
  source: string;
  target: string;
  type: AssetType;
}

export const assetRoots: AssetRoot[] = [
  { source: '.claude/skills', target: 'assets/claude/skills', type: 'claude-skill' },
  { source: '.claude/agents', target: 'assets/claude/agents', type: 'claude-agent' },
  { source: '.claude/hooks', target: 'assets/claude/hooks', type: 'claude-hook' },
  { source: '.claude/rules', target: 'assets/claude/rules', type: 'claude-rule' },
  { source: '.claude/scripts', target: 'assets/claude/scripts', type: 'claude-script' },
  { source: '.opencode/skills', target: 'assets/opencode/skills', type: 'opencode-skill' },
  { source: '.opencode/agents', target: 'assets/opencode/agents', type: 'opencode-agent' }
];

const excludedSegments = new Set([
  '.git',
  '.venv',
  'node_modules',
  '__pycache__',
  '.pytest_cache',
  'coverage',
  'session-state',
  '.logs'
]);

export function isExcludedAsset(relativePath: string): boolean {
  const normalized = relativePath.split(path.sep).join('/');
  const segments = normalized.split('/');
  const basename = segments.at(-1) ?? '';

  if (segments.some((segment) => excludedSegments.has(segment))) return true;
  if (basename === '.DS_Store') return true;
  if (basename.endsWith('.log') || basename.endsWith('.tmp') || basename.endsWith('.jsonl')) return true;
  if (basename === 'metadata.json' && normalized.startsWith('.claude/')) return true;
  if (basename === 'settings.local.json') return true;
  if (basename === '.env' || basename.startsWith('.env.')) return true;

  return false;
}

export function targetPathForAsset(source: string): string {
  const normalized = source.split(path.sep).join('/');
  if (normalized.startsWith('assets/claude/')) return `.claude/${normalized.slice('assets/claude/'.length)}`;
  if (normalized.startsWith('assets/opencode/')) return `.opencode/${normalized.slice('assets/opencode/'.length)}`;
  throw new Error(`Unsupported asset source: ${source}`);
}
