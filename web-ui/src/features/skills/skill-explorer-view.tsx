import { useEffect, useState } from "react";
import { Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Panel, PanelBody, PanelHeader, PanelTitle } from "@/components/ui/panel";
import { useGatewayRequest } from "@/hooks/use-gateway-request";
import { useAppConfig } from "@/providers/app-config-provider";
import { SkillDetailPanel } from "@/features/skills/skill-detail-panel";
import type { SkillSummary } from "@/lib/gateway-types";

interface SkillExplorerViewProps {
  onPublishFirst?: () => void;
}

export function SkillExplorerView({ onPublishFirst }: SkillExplorerViewProps) {
  const { client, baseUrl } = useAppConfig();
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [tags, setTags] = useState("");
  const [selectedName, setSelectedName] = useState<string>();
  const skills = useGatewayRequest<SkillSummary[]>();
  const hasFilter = [query, category, tags].some((value) => value.trim().length > 0);

  const load = () => {
    const filter = { category, tags, page: 0, size: 20, limit: 20 };
    void skills.run(() => (query.trim() ? client.searchSkills({ ...filter, query }) : client.listSkills(filter)));
  };

  useEffect(() => {
    load();
  }, [baseUrl]);

  return (
    <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_440px]">
      <Panel>
        <PanelHeader>
          <PanelTitle>Skill Explorer</PanelTitle>
        </PanelHeader>
        <PanelBody className="space-y-4">
          <div className="grid gap-2 md:grid-cols-[1fr_160px_180px_auto]">
            <Input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search query" />
            <Input value={category} onChange={(event) => setCategory(event.target.value)} placeholder="Category" />
            <Input value={tags} onChange={(event) => setTags(event.target.value)} placeholder="Tags comma-separated" />
            <Button variant="primary" onClick={load} disabled={skills.loading}>
              <Search className="h-4 w-4" />
              Search
            </Button>
          </div>
          {skills.error ? <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-900">{skills.error.message}</div> : null}
          <div className="overflow-auto rounded-md border border-zinc-200">
            <table className="w-full min-w-[680px] border-collapse text-sm">
              <thead className="sticky top-0 bg-zinc-50 text-left text-xs uppercase text-zinc-500">
                <tr>
                  <th className="border-b p-2">Name</th>
                  <th className="border-b p-2">Category</th>
                  <th className="border-b p-2">Tags</th>
                  <th className="border-b p-2 text-right">Downloads</th>
                  <th className="border-b p-2 text-right">Score</th>
                </tr>
              </thead>
              <tbody>
                {skills.loading ? (
                  <tr>
                    <td className="p-4 text-center text-zinc-500" colSpan={5}>
                      Searching...
                    </td>
                  </tr>
                ) : null}
                {(skills.data ?? []).map((skill) => (
                  <tr
                    key={skill.name}
                    className="h-10 cursor-pointer border-b last:border-b-0 hover:bg-emerald-50"
                    onClick={() => setSelectedName(skill.name)}
                  >
                    <td className="p-2 font-medium">{skill.name}</td>
                    <td className="p-2">{skill.category ?? "-"}</td>
                    <td className="p-2 text-zinc-600">{skill.tags?.join(", ") || "-"}</td>
                    <td className="p-2 text-right">{skill.downloadCount ?? 0}</td>
                    <td className="p-2 text-right">{skill.score?.toFixed(3) ?? "-"}</td>
                  </tr>
                ))}
                {!skills.loading && skills.data?.length === 0 ? (
                  <tr>
                    <td className="p-4 text-center text-zinc-500" colSpan={5}>
                      <div className="flex flex-col items-center gap-3">
                        <span>{hasFilter ? "No matching skills found." : "No skills yet. Go to Publish to add first skill."}</span>
                        {!hasFilter && onPublishFirst ? (
                          <Button variant="secondary" onClick={onPublishFirst}>
                            Go to Publish
                          </Button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
        </PanelBody>
      </Panel>
      <SkillDetailPanel key={selectedName ?? "empty"} skillName={selectedName} />
    </div>
  );
}
