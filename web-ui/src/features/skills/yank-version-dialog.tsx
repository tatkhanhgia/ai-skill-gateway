import { useState } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { useGatewayRequest } from "@/hooks/use-gateway-request";
import { useAppConfig } from "@/providers/app-config-provider";

interface YankVersionDialogProps {
  skillName: string;
  version: string;
  onClose: () => void;
  onYanked: () => void;
}

export function YankVersionDialog({ skillName, version, onClose, onYanked }: YankVersionDialogProps) {
  const { client, apiKeyConfigured } = useAppConfig();
  const [reason, setReason] = useState("");
  const request = useGatewayRequest<boolean>();

  const submit = async () => {
    const result = await request.run(async () => {
      await client.yankVersion(skillName, version, reason);
      return true;
    });
    if (result) {
      onYanked();
    }
  };

  return (
    <div className="fixed inset-0 z-20 flex items-center justify-center bg-zinc-950/40 p-4">
      <div className="w-full max-w-lg rounded-lg bg-white shadow-xl">
        <div className="border-b border-zinc-200 p-4">
          <h2 className="flex items-center gap-2 text-base font-semibold text-red-900">
            <AlertTriangle className="h-5 w-5" />
            Yank version
          </h2>
        </div>
        <div className="space-y-3 p-4 text-sm">
          <p>
            Confirm yanking <code>{skillName}@{version}</code>. This is a protected registry action.
          </p>
          <Textarea value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Reason, for example: deprecated or broken release" />
          {!apiKeyConfigured ? <div className="rounded-md bg-amber-50 p-2 text-amber-900">Set an API key in the top bar before submitting.</div> : null}
          {request.error ? <div className="rounded-md bg-red-50 p-2 text-red-900">{request.error.message}</div> : null}
        </div>
        <div className="flex justify-end gap-2 border-t border-zinc-200 p-4">
          <Button onClick={onClose}>Cancel</Button>
          <Button variant="danger" onClick={submit} disabled={!apiKeyConfigured || request.loading}>
            Confirm yank
          </Button>
        </div>
      </div>
    </div>
  );
}
