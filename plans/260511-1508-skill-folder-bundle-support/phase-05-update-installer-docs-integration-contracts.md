# Phase 05 - Update Installer Docs Integration Contracts

## Context Links

- [Plan](./plan.md)
- NPM installer: `npm-package/src/installer/`
- NPM manifest builder: `npm-package/src/manifest/`
- Docs: `docs/system-architecture.md`, `docs/code-standards.md`, `docs/client-agent-integration-guide.md`

## Overview

Priority: P2. Status: Completed. Document the new bundle contract and note future CLI integration without forcing installer rewrite in this feature.

## Key Insights

- Current `gtk-skill` installs packaged local assets, not remote registry bundles.
- Remote install can be a later plan.
- Docs must make API behavior testable for agents.

## Requirements

- Document bundle API examples.
- Document folder layout and validation rules.
- Align architecture docs with artifact storage.
- Leave npm installer behavior unchanged unless tests require type updates.

## Related Code Files

Modify:
- `README.md`
- `docs/system-architecture.md`
- `docs/code-standards.md`
- `docs/client-agent-integration-guide.md`
- Optional `npm-package/README.md`

## Implementation Steps

1. Add curl examples for multipart publish and bundle download.
2. Add skill folder structure contract.
3. Explain local artifact storage config and limits.
4. Note `gtk-skill` remote registry install is future scope.

## Todo List

- [x] Update API docs.
- [x] Update architecture docs.
- [x] Update README usage.
- [x] Validate docs links.

## Success Criteria

- Developers can publish/download bundle from docs alone.
- Docs do not claim unimplemented remote installer behavior.

## Risk Assessment

- Risk: docs overpromise CLI integration. Mitigation: explicitly mark future scope.

## Security Considerations

- Docs must warn that scripts are stored as data, never executed by gateway.

## Next Steps

- Phase 06 verifies code, UI, and docs.

## Unresolved Questions

- None.
