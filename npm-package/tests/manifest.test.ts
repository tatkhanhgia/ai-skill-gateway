import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, rm, writeFile } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import type { AssetManifest } from '../src/types.js';
import { isExcludedAsset } from '../src/manifest/asset-rules.js';
import { validateManifest } from '../src/manifest/read-manifest.js';
import { walkFiles } from '../src/manifest/walk-files.js';

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

test('isExcludedAsset rejects test and coverage artifacts', () => {
  assert.equal(isExcludedAsset('.claude/hooks/__tests__/privacy-block.test.cjs'), true);
  assert.equal(isExcludedAsset('.opencode/skills/web-frameworks/scripts/tests/test_nextjs_init.py'), true);
  assert.equal(isExcludedAsset('.opencode/skills/web-frameworks/scripts/.coverage'), true);
});

test('isExcludedAsset allows media-tools env example but rejects real env files', () => {
  assert.equal(isExcludedAsset('.claude/skills/media-tools/.env.example'), false);
  assert.equal(isExcludedAsset('.opencode/skills/media-tools/.env.example'), false);
  assert.equal(isExcludedAsset('.claude/skills/other/.env.example'), true);
  assert.equal(isExcludedAsset('.claude/skills/media-tools/.env'), true);
  assert.equal(isExcludedAsset('.claude/skills/media-tools/.env.local'), true);
});

test('walkFiles skips excluded directories before descending', async () => {
  const root = await mkdtemp(path.join(os.tmpdir(), 'gtk-skill-walk-files-'));
  try {
    await mkdir(path.join(root, 'skill/node_modules/pkg'), { recursive: true });
    await mkdir(path.join(root, 'skill/scripts'), { recursive: true });
    await writeFile(path.join(root, 'skill/node_modules/pkg/ignored.js'), '');
    await writeFile(path.join(root, 'skill/scripts/kept.js'), '');

    const files = await walkFiles(root, (directory) => isExcludedAsset(path.relative(root, directory)));
    assert.deepEqual(files.map((file) => path.relative(root, file)), [path.join('skill', 'scripts', 'kept.js')]);
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});
