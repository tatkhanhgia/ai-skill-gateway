import { readFile } from 'node:fs/promises';
import path from 'node:path';
import type { AssetManifest, AssetManifestFile } from '../types.js';
import { packageRoot } from './paths.js';

function isSafeRelativeTarget(target: string): boolean {
  if (!target || path.isAbsolute(target)) return false;
  const normalized = target.split('\\').join('/');
  if (normalized.includes('\0')) return false;
  if (normalized.split('/').includes('..')) return false;
  return normalized.startsWith('.claude/') || normalized.startsWith('.codex/') || normalized.startsWith('.opencode/');
}

function validateFile(file: AssetManifestFile): void {
  if (!isSafeRelativeTarget(file.target)) throw new Error(`Unsafe manifest target: ${file.target}`);
  if (path.isAbsolute(file.source) || file.source.split('/').includes('..')) throw new Error(`Unsafe manifest source: ${file.source}`);
}

export function validateManifest(manifest: AssetManifest): AssetManifest {
  for (const file of manifest.files) validateFile(file);
  return manifest;
}

export async function readAssetManifest(): Promise<AssetManifest> {
  const manifestPath = path.join(packageRoot(), 'assets-manifest.json');
  const manifest = JSON.parse(await readFile(manifestPath, 'utf8')) as AssetManifest;
  return validateManifest(manifest);
}
