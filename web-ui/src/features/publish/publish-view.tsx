import { useMemo, useState } from "react";
import { Boxes, Send } from "lucide-react";
import { JsonResponseViewer } from "@/components/json-response-viewer";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Panel, PanelBody, PanelHeader, PanelTitle } from "@/components/ui/panel";
import { Textarea } from "@/components/ui/textarea";
import { BundlePublishForm } from "@/features/publish/bundle-publish-form";
import { PublishPayloadPreview } from "@/features/publish/publish-payload-preview";
import { useGatewayRequest } from "@/hooks/use-gateway-request";
import { normalizePublishPayload } from "@/lib/publish-payload-normalizer";
import { DEFAULT_PUBLISH_CATEGORY, isPublishPayload, validatePublishPayload } from "@/lib/publish-payload-validator";
import { parseSkillMarkdown } from "@/lib/skill-markdown-parser";
import type { PublishPayload, PublishResponse, SkillBundlePublishResponse } from "@/lib/gateway-types";
import { useAppConfig } from "@/providers/app-config-provider";
import { cn } from "@/lib/utils";

type Mode = "form" | "json" | "skill-md" | "skill-bundle";

const emptyForm = {
  name: "",
  version: "1.0.0",
  description: "",
  category: DEFAULT_PUBLISH_CATEGORY,
  tags: "",
  author: "",
  repositoryUrl: "",
  releaseNotes: "",
};

interface PublishViewProps {
  onViewExplorer?: () => void;
}

export function PublishView({ onViewExplorer }: PublishViewProps) {
  const { client, apiKeyConfigured } = useAppConfig();
  const [mode, setMode] = useState<Mode>("form");
  const [form, setForm] = useState(emptyForm);
  const [rawJson, setRawJson] = useState("");
  const [skillMarkdown, setSkillMarkdown] = useState("");
  const [bundleFile, setBundleFile] = useState<File>();
  const publish = useGatewayRequest<PublishResponse>();
  const publishBundle = useGatewayRequest<SkillBundlePublishResponse>();

  const parsed = useMemo<{ payload: Partial<PublishPayload>; parseError?: string }>(() => {
    try {
      if (mode === "form") {
        return { payload: normalizePublishPayload({ ...form, tags: form.tags }) };
      }
      if (mode === "json") {
        return { payload: normalizePublishPayload(rawJson.trim() ? (JSON.parse(rawJson) as Record<string, unknown>) : {}) };
      }
      if (mode === "skill-md") {
        return { payload: normalizePublishPayload(parseSkillMarkdown(skillMarkdown)) };
      }
      return { payload: {} };
    } catch (error) {
      return { payload: {}, parseError: error instanceof Error ? error.message : "Could not parse input" };
    }
  }, [form, mode, rawJson, skillMarkdown]);

  const payload = parsed.payload;
  const validationErrors = [...(parsed.parseError ? [parsed.parseError] : []), ...validatePublishPayload(payload)];

  const submit = () => {
    if (!isPublishPayload(payload)) return;
    void publish.run(() => client.publish(payload));
  };

  const submitBundle = () => {
    if (!bundleFile) return;
    void publishBundle.run(() => client.publishBundle(bundleFile));
  };

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_420px]">
      <Panel>
        <PanelHeader>
          <PanelTitle>Publish Skill</PanelTitle>
        </PanelHeader>
        <PanelBody className="space-y-4">
          <div className="flex flex-wrap gap-2">
            {(["form", "json", "skill-md", "skill-bundle"] as const).map((item) => (
              <Button key={item} variant="ghost" className={cn(mode === item && "bg-emerald-50 text-emerald-900")} onClick={() => setMode(item)}>
                {item === "skill-md" ? "SKILL.md" : item === "skill-bundle" ? "Bundle" : item.toUpperCase()}
              </Button>
            ))}
          </div>
          {mode === "form" ? (
            <div className="grid gap-3 md:grid-cols-2">
              <Input value={form.name} placeholder="Name" onChange={(event) => setForm({ ...form, name: event.target.value })} />
              <Input value={form.version} placeholder="Version" onChange={(event) => setForm({ ...form, version: event.target.value })} />
              <Input value={form.category} placeholder="Category" onChange={(event) => setForm({ ...form, category: event.target.value })} />
              <Input value={form.tags} placeholder="Tags comma-separated" onChange={(event) => setForm({ ...form, tags: event.target.value })} />
              <Input value={form.author} placeholder="Author" onChange={(event) => setForm({ ...form, author: event.target.value })} />
              <Input value={form.repositoryUrl} placeholder="Repository URL" onChange={(event) => setForm({ ...form, repositoryUrl: event.target.value })} />
              <Textarea className="md:col-span-2" value={form.description} placeholder="Description" onChange={(event) => setForm({ ...form, description: event.target.value })} />
              <Textarea className="md:col-span-2" value={form.releaseNotes} placeholder="Release notes" onChange={(event) => setForm({ ...form, releaseNotes: event.target.value })} />
            </div>
          ) : null}
          {mode === "json" ? (
            <Textarea className="min-h-96 font-mono" value={rawJson} onChange={(event) => setRawJson(event.target.value)} placeholder='{"name":"skill-analytics","version":"1.0.0","description":"Analytics helper","category":"data"}' />
          ) : null}
          {mode === "skill-md" ? (
            <Textarea className="min-h-96 font-mono" value={skillMarkdown} onChange={(event) => setSkillMarkdown(event.target.value)} placeholder={"---\nname: my-skill\ndescription: Helper skill\n---\n\nInstructions..."} />
          ) : null}
          {mode === "skill-bundle" ? (
            <BundlePublishForm
              file={bundleFile}
              loading={publishBundle.loading}
              apiKeyConfigured={apiKeyConfigured}
              onFileChange={setBundleFile}
              onSubmit={submitBundle}
            />
          ) : null}
        </PanelBody>
      </Panel>
      <Panel>
        <PanelHeader>
          <PanelTitle>Payload Preview</PanelTitle>
        </PanelHeader>
        <PanelBody className="space-y-4">
          {mode === "skill-bundle" ? (
            <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3 text-sm text-zinc-700">
              Server validates SKILL.md metadata, file paths, checksums, and archive limits.
            </div>
          ) : (
            <PublishPayloadPreview payload={payload} errors={validationErrors} />
          )}
          {!apiKeyConfigured ? <div className="rounded-md bg-amber-50 p-3 text-sm text-amber-950">Set API key in the top bar before publishing.</div> : null}
          {mode !== "skill-bundle" ? (
            <Button variant="primary" className="w-full" onClick={submit} disabled={!apiKeyConfigured || validationErrors.length > 0 || publish.loading}>
            <Send className="h-4 w-4" />
            Publish
            </Button>
          ) : null}
          {(publish.data || publishBundle.data) && onViewExplorer ? (
            <Button variant="secondary" className="w-full" onClick={onViewExplorer}>
              <Boxes className="h-4 w-4" />
              View in Explorer
            </Button>
          ) : null}
          <JsonResponseViewer
            value={mode === "skill-bundle" ? publishBundle.data : publish.data}
            error={mode === "skill-bundle" ? publishBundle.error : publish.error}
            emptyLabel="Publish response appears here."
          />
        </PanelBody>
      </Panel>
    </div>
  );
}
