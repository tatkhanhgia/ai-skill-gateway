import { Upload } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface BundlePublishFormProps {
  file?: File;
  loading?: boolean;
  apiKeyConfigured: boolean;
  onFileChange: (file?: File) => void;
  onSubmit: () => void;
}

export function BundlePublishForm({ file, loading, apiKeyConfigured, onFileChange, onSubmit }: BundlePublishFormProps) {
  return (
    <div className="space-y-4">
      <Input
        type="file"
        accept=".zip,application/zip"
        onChange={(event) => onFileChange(event.target.files?.[0])}
      />
      {file ? (
        <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3 text-sm text-zinc-700">
          <div className="font-medium text-zinc-950">{file.name}</div>
          <div>{formatBytes(file.size)}</div>
        </div>
      ) : null}
      <Button
        variant="primary"
        className="w-full"
        onClick={onSubmit}
        disabled={!apiKeyConfigured || !file || loading}
      >
        <Upload className="h-4 w-4" />
        Publish Bundle
      </Button>
    </div>
  );
}

function formatBytes(size: number): string {
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}
