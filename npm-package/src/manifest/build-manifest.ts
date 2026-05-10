import { readFile, stat, writeFile } from 'node:fs/promises';
import path from 'node:path';
import type { AssetManifest, AssetManifestFile } from '../types.js';
import { isExcludedAsset, targetPathForAsset } from './asset-rules.js';
import { sha256File } from './checksum.js';
import { packageRoot } from './paths.js';
import { walkFiles } from './walk-files.js';

function assetType(source: string): AssetManifestFile['type'] {
  if (source.startsWith('assets/claude/skills/')) return 'claude-skill';
  if (source.startsWith('assets/claude/agents/')) return 'claude-agent';
  if (source.startsWith('assets/claude/hooks/')) return 'claude-hook';
  if (source.startsWith('assets/claude/rules/')) return 'claude-rule';
  if (source.startsWith('assets/claude/scripts/')) return 'claude-script';
  if (source.startsWith('assets/opencode/skills/')) return 'opencode-skill';
  if (source.startsWith('assets/opencode/agents/')) return 'opencode-agent';
  throw new Error(`Unsupported asset source: ${source}`);
}

function isAssetContainer(source: string): boolean {
  return source === 'assets'
    || source === 'assets/claude'
    || source === 'assets/opencode';
}

async function main(): Promise<void> {
  const root = packageRoot();
  const packageJson = JSON.parse(await readFile(path.join(root, 'package.json'), 'utf8')) as { name: string; version: string };
  const assetsRoot = path.join(root, 'assets');
  const files = await walkFiles(assetsRoot, (directory) => {
    const source = path.relative(root, directory).split(path.sep).join('/');
    if (!source || isAssetContainer(source)) return false;
    if (!source.startsWith('assets/claude/') && !source.startsWith('assets/opencode/')) return true;
    return isExcludedAsset(targetPathForAsset(source));
  });

  const includedFiles = files.filter((file) => {
    const source = path.relative(root, file).split(path.sep).join('/');
    return !isExcludedAsset(targetPathForAsset(source));
  });

  const manifestFiles = await Promise.all(includedFiles.map(async (file) => {
    const source = path.relative(root, file).split(path.sep).join('/');
    const fileStat = await stat(file);
    return {
      source,
      target: targetPathForAsset(source),
      type: assetType(source),
      sha256: await sha256File(file),
      size: fileStat.size
    } satisfies AssetManifestFile;
  }));

  const manifest: AssetManifest = {
    packageName: packageJson.name,
    packageVersion: packageJson.version,
    generatedAt: new Date().toISOString(),
    files: manifestFiles.sort((a, b) => a.target.localeCompare(b.target))
  };

  await writeFile(path.join(root, 'assets-manifest.json'), `${JSON.stringify(manifest, null, 2)}\n`);
}

await main();
