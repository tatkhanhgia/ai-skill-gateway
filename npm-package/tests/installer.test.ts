import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, mkdir, readFile, rm, stat, writeFile } from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import type { AssetManifestFile } from '../src/types.js';
import { sha256File } from '../src/manifest/checksum.js';
import { planOperations } from '../src/installer/plan-operations.js';
import { executeOperations } from '../src/installer/execute-operations.js';
import { installAssets } from '../src/installer/install-assets.js';

async function tempDir(): Promise<string> {
  return mkdtemp(path.join(os.tmpdir(), 'gtk-skill-test-'));
}

test('planOperations creates missing files', async () => {
  const root = await tempDir();
  try {
    const files: AssetManifestFile[] = [{ source: 'package.json', target: '.claude/demo.txt', type: 'claude-skill', sha256: 'x', size: 1 }];
    const operations = await planOperations(root, files, 'skip');
    assert.equal(operations[0]?.action, 'create');
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});

test('planOperations reports conflicts by default', async () => {
  const root = await tempDir();
  try {
    const target = path.join(root, '.claude', 'demo.txt');
    await mkdir(path.dirname(target), { recursive: true });
    await writeFile(target, 'local');
    const files: AssetManifestFile[] = [{ source: 'package.json', target: '.claude/demo.txt', type: 'claude-skill', sha256: 'different', size: 1 }];
    const operations = await planOperations(root, files, 'skip');
    assert.equal(operations[0]?.action, 'conflict');
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});

test('executeOperations copies files', async () => {
  const root = await tempDir();
  try {
    const source = path.join(root, 'source.txt');
    const target = path.join(root, '.claude', 'demo.txt');
    await writeFile(source, 'content');
    const sha256 = await sha256File(source);
    const errors = await executeOperations([{ action: 'create', source, target, relativeTarget: '.claude/demo.txt', type: 'claude-skill', sha256 }]);
    assert.deepEqual(errors, []);
    assert.equal(await readFile(target, 'utf8'), 'content');
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});

test('installAssets fails before writing into non-project directories', async () => {
  const root = await tempDir();
  try {
    const result = await installAssets({ cwd: root });
    assert.deepEqual(result.errors, ['target has no common project marker']);
    assert.equal(result.operations.length, 0);
    await assert.rejects(() => stat(path.join(root, '.gtk-skill')));
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});
