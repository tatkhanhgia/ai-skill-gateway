# Phase 05: Version Management

## Context Links

- [Plan Overview](./plan.md)
- [Architecture Patterns - Version Resolution](../ARCHITECTURE-PATTERNS.md)
- [Skill Repository Design - SemVer](../skill-repository-design-research.md)
- [Quick Reference - SemVer Rules](../QUICK-REFERENCE.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Description:** Implement SemVer parsing, version constraint resolution (^, ~, ranges), dependency graph traversal (DAG), and prerelease lifecycle management.

## Key Insights

- SemVer: MAJOR.MINOR.PATCH[-prerelease][+build]
- Constraint operators: `^` (compatible major), `~` (patch only), `>=X <Y` (range), `latest`
- Prerelease excluded by default; require explicit opt-in
- DAG traversal for dependency resolution; detect circular deps
- Yank = soft-delete; yanked versions excluded from resolution
- npm-style constraint resolution is battle-tested pattern

## Requirements

### Functional
- **Parse SemVer**: Strict parsing of version strings including prerelease + build metadata
- **Resolve constraint**: Given skill name + constraint, return best matching version
- **List versions**: All versions of a skill with prerelease/yank flags
- **Dependency resolution**: Given skill + version, resolve transitive dependency tree
- **Circular dependency detection**: Fail fast with descriptive error
- **MCP tools**: `resolve_version`, `list_versions` callable from clients

### Non-Functional
- Version resolution < 50ms for single skill
- Dependency graph resolution < 500ms for 10 transitive deps
- Clear error messages for constraint conflicts

## Architecture

```
resolve_version(skill_name, constraint)
    |
    v
VersionService
    |
    +-- 1. Parse constraint string
    |     SemVerConstraint.parse("^1.2.3")
    |       -> {operator: CARET, base: {1, 2, 3}}
    |
    +-- 2. Fetch available versions from DB
    |     SELECT version_semver, is_prerelease, is_yanked
    |     FROM skill_versions WHERE skill_id = ?
    |
    +-- 3. Filter by constraint rules
    |     ^1.2.3 -> >=1.2.3 AND <2.0.0
    |     ~1.2.3 -> >=1.2.3 AND <1.3.0
    |     Exclude prerelease (unless explicit)
    |     Exclude yanked
    |
    +-- 4. Return highest matching version
    |
    v
DependencyResolver (for full tree)
    |
    +-- 1. Start from root skill@version
    +-- 2. Fetch requires JSONB from version record
    +-- 3. For each dependency: resolve_version recursively
    +-- 4. Track visited nodes (detect cycles)
    +-- 5. Return flat dependency list or error
```

## Related Code Files

### Create
- `src/main/java/com/skillgateway/version/SemVer.java` (Record + Comparable)
- `src/main/java/com/skillgateway/version/SemVerParser.java`
- `src/main/java/com/skillgateway/version/SemVerConstraint.java`
- `src/main/java/com/skillgateway/version/ConstraintOperator.java` (Enum)
- `src/main/java/com/skillgateway/service/VersionService.java`
- `src/main/java/com/skillgateway/service/DependencyResolver.java`
- `src/main/java/com/skillgateway/model/dto/VersionResolution.java` (Record)
- `src/main/java/com/skillgateway/model/dto/DependencyTree.java` (Record)
- `src/main/java/com/skillgateway/mcp/tools/VersionToolHandler.java`

## Implementation Steps

1. **SemVer Record**
   ```java
   public record SemVer(
     int major, int minor, int patch,
     String prerelease, String build
   ) implements Comparable<SemVer> {

     public boolean isPrerelease() { return prerelease != null && !prerelease.isEmpty(); }

     @Override
     public int compareTo(SemVer other) {
       // Compare major -> minor -> patch -> prerelease
       // Prerelease has lower precedence than release
     }
   }
   ```

2. **SemVerParser** - Regex-based parsing
   ```java
   public class SemVerParser {
     // Pattern: MAJOR.MINOR.PATCH(-prerelease)?(+build)?
     private static final Pattern SEMVER = Pattern.compile(
       "^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([-\\w.]+))?(?:\\+([\\w.]+))?$");

     public static SemVer parse(String version) { ... }
   }
   ```

3. **SemVerConstraint** - Constraint matching logic
   ```java
   public record SemVerConstraint(ConstraintOperator operator, SemVer base) {

     public static SemVerConstraint parse(String constraint) {
       if ("latest".equals(constraint)) return new SemVerConstraint(LATEST, null);
       if (constraint.startsWith("^")) return new SemVerConstraint(CARET, SemVerParser.parse(constraint.substring(1)));
       if (constraint.startsWith("~")) return new SemVerConstraint(TILDE, SemVerParser.parse(constraint.substring(1)));
       // Handle >=X <Y range syntax
       return new SemVerConstraint(EXACT, SemVerParser.parse(constraint));
     }

     public boolean matches(SemVer version) {
       return switch (operator) {
         case LATEST -> !version.isPrerelease();
         case CARET -> version.major() == base.major() && version.compareTo(base) >= 0;
         case TILDE -> version.major() == base.major()
                    && version.minor() == base.minor()
                    && version.compareTo(base) >= 0;
         case EXACT -> version.equals(base);
         case RANGE -> /* range logic */ true;
       };
     }
   }
   ```

4. **VersionService**
   ```java
   @ApplicationScoped
   public class VersionService {
     @Inject SkillVersionRepository versionRepo;

     public VersionResolution resolve(String skillName, String constraint) {
       SemVerConstraint parsed = SemVerConstraint.parse(constraint);
       List<SkillVersion> versions = versionRepo.findBySkillName(skillName);

       List<SemVer> matching = versions.stream()
         .filter(v -> !v.isYanked())
         .map(v -> SemVerParser.parse(v.getVersionSemver()))
         .filter(parsed::matches)
         .sorted(Comparator.reverseOrder())
         .toList();

       if (matching.isEmpty()) throw new VersionConstraintException(...);
       return new VersionResolution(skillName, constraint, matching.get(0), matching);
     }
   }
   ```

5. **DependencyResolver** - DAG traversal
   ```java
   @ApplicationScoped
   public class DependencyResolver {
     @Inject VersionService versionService;

     public DependencyTree resolve(String skillName, String version) {
       Set<String> visited = new HashSet<>();
       return resolveRecursive(skillName, version, visited);
     }

     private DependencyTree resolveRecursive(String name, String ver, Set<String> visited) {
       String key = name + "@" + ver;
       if (visited.contains(key)) throw new CircularDependencyException(key);
       visited.add(key);

       SkillVersion sv = versionRepo.findByNameAndVersion(name, ver);
       List<DependencyTree> children = new ArrayList<>();
       for (var dep : sv.getRequires()) {
         if (dep.startsWith("skill:")) {
           // Parse "skill:name@constraint"
           VersionResolution resolved = versionService.resolve(depName, depConstraint);
           children.add(resolveRecursive(depName, resolved.version().toString(), visited));
         }
       }
       return new DependencyTree(name, ver, children);
     }
   }
   ```

6. **MCP Tool Handlers**
   ```java
   @Tool(description = "Resolve best version matching a constraint")
   public VersionResolution resolveVersion(
     @ToolArg(description = "Skill name") String name,
     @ToolArg(description = "Version constraint (^1.0.0, ~1.2.3, latest)") String constraint
   ) { ... }

   @Tool(description = "List all versions of a skill")
   public List<VersionInfo> listVersions(
     @ToolArg(description = "Skill name") String name
   ) { ... }
   ```

## Todo List

- [x] Implement SemVer record with Comparable
- [x] Implement SemVerParser with regex validation
- [x] Implement SemVerConstraint with match logic
- [x] Implement ConstraintOperator enum (LATEST, CARET, TILDE, EXACT, RANGE)
- [x] Implement VersionService.resolve()
- [x] Implement DependencyResolver with cycle detection
- [x] Create VersionToolHandler MCP tools
- [x] Add REST endpoints (GET /skills/{name}/versions, GET /skills/{name}/resolve)
- [x] Unit test SemVer parsing (valid + invalid strings)
- [x] Unit test constraint matching (all operators)
- [x] Unit test dependency resolution (happy path + circular)
- [x] Integration test with DB

## Success Criteria

- `SemVerParser.parse("1.2.3-beta.1+build.42")` returns correct record
- `^1.2.3` matches 1.3.0 but not 2.0.0
- `~1.2.3` matches 1.2.5 but not 1.3.0
- `latest` returns highest non-prerelease version
- Yanked versions excluded from resolution
- Circular dependency detected and reported with cycle path
- MCP `resolve_version` tool returns correct result

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Complex range syntax (>=1.0 <2.0 \|\| >=3.0) | Parser complexity | Start with ^, ~, exact, latest; add ranges later |
| Deep dependency trees | Stack overflow | Set max depth (10); fail with descriptive error |
| Version ordering edge cases | Wrong resolution | Comprehensive unit tests; follow SemVer spec strictly |
| Prerelease sorting rules | Incorrect precedence | alpha < beta < rc; numeric segments compared numerically |

## Security Considerations

- Constraint strings validated before parsing (reject malformed input)
- Dependency resolution depth-limited (prevent DoS via deep trees)
- Yanked versions carry reason field (security advisory support)

## Next Steps

- Phase 06: Comprehensive testing and deployment pipeline
