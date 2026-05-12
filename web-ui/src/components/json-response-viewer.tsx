import { AlertTriangle } from "lucide-react";
import type { GatewayApiError } from "@/lib/api-errors";

interface JsonResponseViewerProps {
  value?: unknown;
  error?: GatewayApiError;
  emptyLabel?: string;
}

export function JsonResponseViewer({ value, error, emptyLabel = "No response yet." }: JsonResponseViewerProps) {
  if (error) {
    return (
      <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-900">
        <div className="flex items-center gap-2 font-medium">
          <AlertTriangle className="h-4 w-4" />
          {error.message}
        </div>
        {error.details?.length ? <ul className="mt-2 list-disc pl-5">{error.details.map((item) => <li key={item}>{item}</li>)}</ul> : null}
      </div>
    );
  }

  if (value === undefined) {
    return <div className="rounded-md border border-dashed border-zinc-300 p-3 text-sm text-zinc-500">{emptyLabel}</div>;
  }

  return (
    <pre className="max-h-80 overflow-auto rounded-md bg-zinc-950 p-3 text-xs leading-5 text-zinc-100">
      {JSON.stringify(value, null, 2)}
    </pre>
  );
}
