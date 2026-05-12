import { useEffect } from "react";
import { RefreshCw } from "lucide-react";
import { JsonResponseViewer } from "@/components/json-response-viewer";
import { StatusIndicator } from "@/components/status-indicator";
import { Button } from "@/components/ui/button";
import { Panel, PanelBody, PanelHeader, PanelTitle } from "@/components/ui/panel";
import { useGatewayRequest } from "@/hooks/use-gateway-request";
import { useAppConfig } from "@/providers/app-config-provider";
import type { EmbeddingStatus } from "@/lib/gateway-types";

export function DashboardView() {
  const { client, baseUrl } = useAppConfig();
  const health = useGatewayRequest<unknown>();
  const embedding = useGatewayRequest<EmbeddingStatus>();

  const refresh = () => {
    void Promise.all([health.run(client.health), embedding.run(client.embeddingStatus)]);
  };

  useEffect(() => {
    refresh();
  }, [baseUrl]);

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold">Dashboard</h2>
          <p className="text-sm text-zinc-600">Gateway health and embedding provider state.</p>
        </div>
        <Button onClick={refresh} disabled={health.loading || embedding.loading}>
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        <Panel>
          <PanelHeader className="flex items-center justify-between">
            <PanelTitle>Server health</PanelTitle>
            <StatusIndicator state={health.error ? "error" : health.data ? "ok" : "idle"} label={health.loading ? "Checking" : health.error ? "Error" : health.data ? "Online" : "Idle"} />
          </PanelHeader>
          <PanelBody>
            <JsonResponseViewer value={health.data} error={health.error} />
          </PanelBody>
        </Panel>
        <Panel>
          <PanelHeader className="flex items-center justify-between">
            <PanelTitle>Embedding provider</PanelTitle>
            <StatusIndicator
              state={embedding.error ? "error" : embedding.data?.configured ? "ok" : embedding.data ? "warn" : "idle"}
              label={embedding.loading ? "Checking" : embedding.error ? "Error" : embedding.data?.configured ? "Configured" : "Idle"}
            />
          </PanelHeader>
          <PanelBody>
            <JsonResponseViewer value={embedding.data} error={embedding.error} />
          </PanelBody>
        </Panel>
      </div>
    </div>
  );
}
