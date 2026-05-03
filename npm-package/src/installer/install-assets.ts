import type { InstallOptions, InstallResult } from '../types.js';
import { readAssetManifest } from '../manifest/read-manifest.js';
import { detectProjectRoot } from '../project/detect-root.js';
import { executeOperations } from './execute-operations.js';
import { filterAssets } from './filter-assets.js';
import { writeInstallState } from './install-state.js';
import { planOperations } from './plan-operations.js';

export async function installAssets(options: InstallOptions = {}): Promise<InstallResult> {
  const project = await detectProjectRoot(options.cwd);
  if (!project.hasMarker && !options.allowNonProjectDir) {
    return { targetRoot: project.root, dryRun: Boolean(options.dryRun), operations: [], errors: ['target has no common project marker'] };
  }

  const manifest = await readAssetManifest();
  const files = filterAssets(manifest.files, options.groups);
  const policy = options.policy ?? 'skip';
  const operations = await planOperations(project.root, files, policy);
  const errors = options.dryRun ? [] : await executeOperations(operations);

  if (!options.dryRun && errors.length === 0) {
    const installedFiles = files.filter((file) => operations.some((operation) => operation.relativeTarget === file.target && operation.action !== 'conflict'));
    await writeInstallState(project.root, manifest, installedFiles);
  }

  return { targetRoot: project.root, dryRun: Boolean(options.dryRun), operations, errors };
}
