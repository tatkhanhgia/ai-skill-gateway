import test from 'node:test';
import assert from 'node:assert/strict';
import type { AssetManifestFile } from '../src/types.js';
import { filterAssets } from '../src/installer/filter-assets.js';

const files: AssetManifestFile[] = [
  { source: 'a', target: '.claude/skills/a', type: 'claude-skill', sha256: 'a', size: 1 },
  { source: 'b', target: '.claude/hooks/b', type: 'claude-hook', sha256: 'b', size: 1 },
  { source: 'c', target: '.opencode/agents/c', type: 'opencode-agent', sha256: 'c', size: 1 }
];

test('filterAssets returns all files when no groups selected', () => {
  assert.equal(filterAssets(files).length, 3);
});

test('filterAssets filters by platform and asset group', () => {
  assert.deepEqual(filterAssets(files, ['claude']).map((file) => file.type), ['claude-skill', 'claude-hook']);
  assert.deepEqual(filterAssets(files, ['agents']).map((file) => file.type), ['opencode-agent']);
});
