import { JsonResponseViewer } from "@/components/json-response-viewer";
import type { PublishPayload } from "@/lib/gateway-types";

interface PublishPayloadPreviewProps {
  payload: Partial<PublishPayload>;
  errors: string[];
}

export function PublishPayloadPreview({ payload, errors }: PublishPayloadPreviewProps) {
  return (
    <div className="space-y-3">
      {errors.length ? (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-sm text-amber-950">
          {errors.map((error) => <div key={error}>{error}</div>)}
        </div>
      ) : null}
      <JsonResponseViewer value={payload} emptyLabel="Payload preview appears here." />
    </div>
  );
}
