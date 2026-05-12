import { useEffect, useState } from "react";
import { GitBranch, RefreshCw } from "lucide-react";
import { JsonResponseViewer } from "@/components/json-response-viewer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Panel, PanelBody, PanelHeader, PanelTitle } from "@/components/ui/panel";
import { useGatewayRequest } from "@/hooks/use-gateway-request";
import { useAppConfig } from "@/providers/app-config-provider";
import { YankVersionDialog } from "@/features/skills/yank-version-dialog";
import type { DependencyResolution, SkillDetail, VersionInfo, VersionResolution } from "@/lib/gateway-types";

interface SkillDetailPanelProps {
  skillName?: string;
}

export function SkillDetailPanel({ skillName }: SkillDetailPanelProps) {
  const { client, baseUrl } = useAppConfig();
  const [constraint, setConstraint] = useState("latest");
  const [dependencyVersion, setDependencyVersion] = useState("");
  const [yankTarget, setYankTarget] = useState<VersionInfo>();
  const detail = useGatewayRequest<SkillDetail>();
  const versions = useGatewayRequest<VersionInfo[]>();
  const resolution = useGatewayRequest<VersionResolution>();
  const dependencies = useGatewayRequest<DependencyResolution>();

  const refresh = () => {
    if (!skillName) return;
    void Promise.all([detail.run(() => client.getSkill(skillName)), versions.run(() => client.versions(skillName))]);
  };

  useEffect(() => {
    refresh();
    setDependencyVersion("");
  }, [baseUrl, skillName]);

  if (!skillName) {
    return (
      <Panel>
        <PanelBody className="text-sm text-zinc-500">Select a skill to inspect versions, resolution, and dependencies.</PanelBody>
      </Panel>
    );
  }

  return (
    <Panel>
      <PanelHeader className="flex items-center justify-between gap-3">
        <PanelTitle>{skillName}</PanelTitle>
        <Button onClick={refresh} disabled={detail.loading || versions.loading}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </PanelHeader>
      <PanelBody className="space-y-5">
        <JsonResponseViewer value={detail.data} error={detail.error} emptyLabel="Loading detail..." />
        <div>
          <h3 className="mb-2 text-sm font-semibold">Versions</h3>
          <div className="space-y-2">
            {(versions.data ?? []).map((version) => (
              <div key={version.version} className="flex flex-wrap items-center justify-between gap-2 rounded-md border border-zinc-200 p-2 text-sm">
                <div className="flex flex-wrap items-center gap-2">
                  <code>{version.version}</code>
                  {version.latest ? <span className="rounded bg-emerald-100 px-2 py-0.5 text-xs text-emerald-900">latest</span> : null}
                  {version.yanked ? <span className="rounded bg-red-100 px-2 py-0.5 text-xs text-red-900">yanked</span> : null}
                </div>
                <Button variant="danger" disabled={version.yanked || versions.loading || detail.loading} onClick={() => setYankTarget(version)}>Yank</Button>
              </div>
            ))}
            {versions.data?.length === 0 ? <div className="text-sm text-zinc-500">No versions.</div> : null}
          </div>
        </div>
        <div className="grid gap-3 md:grid-cols-[1fr_auto]">
          <Input value={constraint} onChange={(event) => setConstraint(event.target.value)} placeholder="Version constraint" />
          <Button onClick={() => void resolution.run(() => client.resolve(skillName, constraint))}>
            <GitBranch className="h-4 w-4" />
            Resolve
          </Button>
        </div>
        <JsonResponseViewer value={resolution.data} error={resolution.error} emptyLabel="Resolve response appears here." />
        <div className="grid gap-3 md:grid-cols-[1fr_auto]">
          <Input
            value={dependencyVersion}
            onChange={(event) => setDependencyVersion(event.target.value)}
            placeholder={detail.data?.latestVersion?.version ?? "Version for dependencies"}
          />
          <Button
            onClick={() =>
              void dependencies.run(() =>
                client.dependencies(skillName, dependencyVersion.trim() || detail.data?.latestVersion?.version || "latest"),
              )
            }
          >
            Dependencies
          </Button>
        </div>
        <JsonResponseViewer value={dependencies.data} error={dependencies.error} emptyLabel="Dependency response appears here." />
      </PanelBody>
      {yankTarget ? (
        <YankVersionDialog
          skillName={skillName}
          version={yankTarget.version}
          onClose={() => setYankTarget(undefined)}
          onYanked={() => {
            setYankTarget(undefined);
            refresh();
          }}
        />
      ) : null}
    </Panel>
  );
}
