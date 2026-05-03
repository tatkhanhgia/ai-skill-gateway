import test from 'node:test';
import assert from 'node:assert/strict';
import type { AssetManifest } from '../src/types.js';
import { isExcludedAsset } from '../src/manifest/asset-rules.js';
import { validateManifest } from '../src/manifest/read-manifest.js';

function manifestWithTarget(target: string): AssetManifest {
  return {
    packageName: 'gtk-skill',
    packageVersion: '0.1.0',
    generatedAt: '2026-05-01T00:00:00.000Z',
    files: [{ source: 'assets/claude/skills/demo/SKILL.md', target, type: 'claude-skill', sha256: 'abc', size: 1 }]
  };
}

test('validateManifest accepts safe Claude targets', () => {
  assert.equal(validateManifest(manifestWithTarget('.claude/skills/demo/SKILL.md')).files.length, 1);
});

test('validateManifest rejects traversal targets', () => {
  assert.throws(() => validateManifest(manifestWithTarget('../outside')));
});

test('validateManifest rejects absolute targets', () => {
  assert.throws(() => validateManifest(manifestWithTarget('C:/outside')));
});

test('isExcludedAsset rejects runtime logs', () => {
  assert.equal(isExcludedAsset('.claude/hooks/.logs/hook-log.jsonl'), true);
  assert.equal(isExcludedAsset('.claude/hooks/demo/output.log'), true);
});
