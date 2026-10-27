# Plan: `vigiang-tools-app`

A web UI (React) and a REST backend (Spring Boot) that expose the existing tools in
[`cognyte/vigiang`](./cognyte/vigiang) and [`commons`](./commons).

## 1. Context

| Module | Coordinates | Notes |
|---|---|---|
| `commons` | `workspace:commons:1.0` | Java 17, Lombok, Jackson, JSch. DAOs, models, file/SSH/schema services. |
| `cognyte/vigiang` | `workspace.cognyte:vigiang:1.0` | Depends on `commons`, Oracle and Postgres JDBC drivers. |

Facts that shape the design:

- **No parent POM.** The modules are linked through the local Maven repo, so you have to run `mvn install` on `commons` and then on `vigiang` before the backend can build.
- **The logic is in static methods.** Examples are `EnvironmentService.getVersions()`, `getLaboratoriesVigiaNgActive()`, `getDatabasesVigiaNgActive()` and `ContainersService.getFrontendTags()`.
- **The "actions" are `main()` methods.** These are `UpdateLaboratories`, `UpdateDatabases`, `UpdateSchemas`, `UpdateMybatis`, `UpdateProjectsByVersion`, `UpdateBackendProjects`, `UpdateFrontendProjects` and `checkers/Check*`.
- **The models hold secrets.** `DatabaseCredentials` contains `password`, and `Laboratory` contains `sshPassword`. Never serialize these models directly; map them to DTOs.

### Product decisions

- **Internal dev tool.** The app is only for developers on the team. There is **no login, no user management and no authorization**:
  - Do not add `spring-boot-starter-security`, JWT/OAuth or session handling to the backend.
  - The frontend has no login page, auth context, route guards or token handling.
  - Every endpoint is open to anyone who can reach the server.
  - DTOs still leave out passwords. This costs nothing and keeps secrets out of browser dev tools and logs.
- **Dark layout only.** The UI always uses a dark theme. There is no light mode and no theme toggle (see "Dark theme" in section 4).

## 2. Toolchain constraints

### Node 20.18.0 (fixed)

The frontend must install and run on **Node v20.18.0 / npm 10.8.2**. Several current
major versions need `^20.19.0 || >=22.12.0` and are therefore **excluded**:

| Package | Allowed version | Engine range | Excluded |
|---|---|---|---|
| `react`, `react-dom` | `~19.2.0` | none (any Node) | n/a |
| `vite` | `~6.4.0` | `^18 \|\| ^20 \|\| >=22` | `7.x` (`^20.19`) |
| `@vitejs/plugin-react` | `~4.7.0` | `>=16` | `5.x` (`^20.19`) |
| `react-router` | `~7.9.0` | `>=20.0.0` | n/a |
| `@tanstack/react-query` | `^5` | n/a | n/a |
| `antd` | `~6.6.0` | none (peer `react >=18`) | n/a |
| `@ant-design/icons` | `~6.3.0` | `>=8` | n/a |
| `typescript` | `~5.9.0` | `>=14.17` | n/a |
| `vitest` | `~3.2.0` | `^18 \|\| ^20 \|\| >=22` | `4.x` (verify before upgrading) |
| `eslint` | `~9.39.0` | `^20.9` | n/a |

Engine ranges were checked with `npm view <pkg> engines` on 2026-10-27.

How the restriction is enforced in `frontend/`:

- `.nvmrc` → `20.18.0`
- `package.json` → `"engines": { "node": ">=20.18.0 <20.19.0" }`
- `.npmrc` → `engine-strict=true`, so `npm install` fails on a mismatched Node or on a dependency that needs a newer one.
- Use **tilde (`~`) ranges** for Vite and the Vite plugin so `npm update` cannot jump to a version that needs Node 20.19+.
- **Do not** scaffold with `npm create vite@latest`, because it pulls templates for Vite 7. Use `npm create vite@6` or write the files by hand (section 4).

### Java

- Spring Boot 3.x needs **Java 17+**. The existing modules already target 17.
- ⚠️ The only local JDK is **11.0.2** (`C:\opt\jdks\jdk-11.0.2`), and Maven runs on it too. **Install JDK 17 or 21 first** and point `JAVA_HOME` and Maven at it. Without that, `commons` and `vigiang` cannot build either.
- Spring Boot version: **3.5.x**, the latest 3.x line, which supports Java 17.

## 3. Target structure

```text
workspace/
├── pom.xml                         # (optional, phase 1) aggregator: commons, cognyte/vigiang, vigiang-tools-app/backend
├── commons/
├── cognyte/vigiang/
└── vigiang-tools-app/
    ├── README.md
    ├── backend/                                    # Spring Boot 3.5, Java 17
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/workspace/vigiangtools/
    │       │   ├── VigiangToolsApplication.java
    │       │   ├── config/
    │       │   │   └── WebConfig.java              # CORS for http://localhost:5173 (dev only)
    │       │   ├── controller/
    │       │   │   ├── VersionController.java      # GET /api/versions
    │       │   │   ├── LaboratoryController.java   # GET /api/laboratories?status=active|inactive
    │       │   │   ├── DatabaseController.java     # GET /api/databases?status=active|inactive
    │       │   │   └── ContainerController.java    # GET /api/containers?laboratory={name}
    │       │   ├── service/
    │       │   │   └── VigiangFacade.java          # Spring bean wrapping the vigiang/commons statics
    │       │   ├── dto/
    │       │   │   ├── LaboratoryDto.java          # name, alias, sshHost, sshPort, carrier, active (no password)
    │       │   │   ├── DatabaseDto.java            # name, database, url, username, carrier, active (no password)
    │       │   │   └── ContainerDto.java           # name, version
    │       │   └── error/ApiExceptionHandler.java  # @RestControllerAdvice → ProblemDetail
    │       ├── main/resources/application.yml      # server.port=8080
    │       └── test/java/workspace/vigiangtools/
    │           └── controller/*Test.java           # @WebMvcTest with a mocked VigiangFacade
    └── frontend/                                   # React 19 + Vite 6 + TypeScript + Ant Design 6
        ├── .nvmrc
        ├── .npmrc
        ├── package.json
        ├── vite.config.ts                          # proxies /api → http://localhost:8080
        ├── tsconfig.json
        ├── eslint.config.js
        ├── index.html
        └── src/
            ├── main.tsx                            # antd ConfigProvider/App, QueryClientProvider, RouterProvider
            ├── App.tsx                             # antd Layout: Sider + Menu, Content with <Outlet/>
            ├── theme.ts                            # antd ThemeConfig: darkAlgorithm + tokens (dark only)
            ├── api/
            │   ├── client.ts                       # fetch wrapper, error → ProblemDetail
            │   └── vigiang.ts                      # typed endpoint functions
            ├── types/                              # mirrors the backend DTOs
            ├── hooks/                              # useVersions, useLaboratories, useDatabases, useContainers
            ├── components/                         # thin wrappers over antd: StatusTag, CarrierTag, LaboratorySelect
            └── pages/
                ├── VersionsPage.tsx
                ├── LaboratoriesPage.tsx
                ├── DatabasesPage.tsx
                └── ContainersPage.tsx              # pick a laboratory → its containers
```

## 4. Key file sketches

### `frontend/package.json`

```json
{
  "name": "vigiang-tools-app",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "engines": { "node": ">=20.18.0 <20.19.0" },
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "test": "vitest"
  },
  "dependencies": {
    "react": "~19.2.0",
    "react-dom": "~19.2.0",
    "react-router": "~7.9.0",
    "@tanstack/react-query": "^5.90.0",
    "antd": "~6.6.0",
    "@ant-design/icons": "~6.3.0"
  },
  "devDependencies": {
    "@types/react": "~19.2.0",
    "@types/react-dom": "~19.2.0",
    "@vitejs/plugin-react": "~4.7.0",
    "vite": "~6.4.0",
    "typescript": "~5.9.0",
    "vitest": "~3.2.0",
    "@testing-library/react": "^16.3.0",
    "jsdom": "^26.1.0",
    "eslint": "~9.39.0",
    "@eslint/js": "~9.39.0",
    "typescript-eslint": "^8.46.0",
    "eslint-plugin-react-hooks": "^5.2.0",
    "globals": "^16.4.0"
  }
}
```

> Before committing, check every devDependency with `npm view <pkg>@<range> engines`, especially `jsdom`. Newer majors may need Node `>=20.19`/`>=22`. `engine-strict=true` will catch any mismatch at install time.

### `frontend/vite.config.ts`

```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: { '/api': 'http://localhost:8080' } },
  build: { outDir: 'dist' },
});
```

### UI library: Ant Design 6

Ant Design was chosen because it is built for admin tools. Its `Table` already handles sorting, filtering and paging, so no separate table library is needed.

| Need | antd component |
|---|---|
| App shell and navigation | `Layout`, `Layout.Sider`, `Menu` (items linked to react-router routes) |
| Version, laboratory, database and container lists | `Table` with `columns` (`sorter`, `filters`), `Input.Search`, `Segmented` for the active/inactive switch |
| Picking a laboratory on the containers page | `Select` (`showSearch`), filled from `/api/laboratories` |
| Carrier and status display | `Tag` / `Badge` |
| Loading a lab's containers over SSH (slow) | `Table` `loading`, `Button` with `ReloadOutlined` to refetch |
| Feedback | `App.useApp()` → `message`, `notification` (instead of the static `message.*`, so the theme is applied) |
| Errors and empty states | `Result`, `Empty`, `Alert` |

Conventions:
- Wrap the app in `<ConfigProvider theme={theme}><App>…</App></ConfigProvider>` in `main.tsx`.
- antd 6 uses CSS-in-JS with no global CSS import. Don't add `antd/dist/reset.css` unless a CSS reset is needed.
- Import components by name (`import { Table } from 'antd'`). Vite tree-shakes them, so no babel import plugin is needed.
- Import icons one at a time (`import { ReloadOutlined } from '@ant-design/icons'`).
- Type table columns as `TableProps<LaboratoryDto>['columns']`, using types from `src/types/`.
- React Query owns the server data; antd components only render it. Don't store fetched data in component state.

### Dark theme

The whole app is dark. It uses antd's `theme.darkAlgorithm`, configured once in `theme.ts`, and does not offer a light mode.

`frontend/src/theme.ts`:

```ts
import { theme, type ThemeConfig } from 'antd';

export const appTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: '#1677ff',
    colorBgBase: '#141414',
    borderRadius: 6,
  },
  components: {
    Layout: { siderBg: '#1f1f1f', headerBg: '#1f1f1f', bodyBg: '#141414' },
    Menu: { darkItemBg: '#1f1f1f' },
  },
};
```

`frontend/index.html` sets the background before React loads, so the page doesn't flash white:

```html
<html lang="en" style="color-scheme: dark">
  <head>
    <meta name="color-scheme" content="dark" />
    <style>html, body, #root { margin: 0; min-height: 100%; background: #141414; }</style>
  </head>
  ...
```

Rules:
- `main.tsx` passes `appTheme` to `<ConfigProvider theme={appTheme}>`. Don't wrap any subtree in a second `ConfigProvider` that switches back to the default algorithm.
- `App.tsx` uses `<Layout.Sider theme="dark">` and `<Menu theme="dark" mode="inline">`.
- Don't hard-code light colors (`#fff`, `white`, `#000` text) in components. Read colors from `theme.useToken()` when a custom style needs one.
- `Tag` and `Badge` colors for carrier and status use antd preset colors (`green`, `red`, `blue`, …). These stay readable on a dark background.

### `backend/pom.xml` (excerpt)

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.5.7</version>
</parent>

<groupId>workspace.cognyte</groupId>
<artifactId>vigiang-tools-app</artifactId>
<version>1.0</version>

<properties>
  <java.version>17</java.version>
</properties>

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  <dependency>
    <groupId>workspace.cognyte</groupId>
    <artifactId>vigiang</artifactId>
    <version>1.0</version>
    <!-- brings workspace:commons:1.0 transitively -->
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

> `commons` pins `jackson-databind:2.17.3`, while Spring Boot manages its own Jackson version. The Boot BOM wins in the backend. Check that `commons` JSON code still behaves correctly with that version.

### No Lombok in the backend

`vigiang-tools-app/backend` must **not** use Lombok: no dependency and no annotation processor.

- **DTOs** are Java `record`s with a static `from(...)` factory.
- **Beans** use explicit constructor injection. Spring autowires a single constructor without `@Autowired`.
- **Logging** uses `private static final Logger log = LoggerFactory.getLogger(X.class);` (SLF4J).
- `commons` and `vigiang` still use Lombok internally with `provided` scope. That doesn't leak into the backend: their compiled jars already contain the generated getters and setters, so the backend calls `getName()` etc. normally.

### DTO example

```java
public record LaboratoryDto(String name, String alias, String sshHost, int sshPort,
                            String carrier, boolean active) {

    public static LaboratoryDto from(LaboratoryVigiaNg lab) {
        return new LaboratoryDto(lab.getName(), lab.getAlias(), lab.getSshHost(), lab.getSshPort(),
                lab.getCarrier() == null ? null : lab.getCarrier().name(), lab.isActive());
    }
}
```

### Controller example

```java
@RestController
@RequestMapping("/api/laboratories")
class LaboratoryController {
    private final VigiangFacade vigiang;

    LaboratoryController(VigiangFacade vigiang) {
        this.vigiang = vigiang;
    }

    @GetMapping
    List<LaboratoryDto> list(@RequestParam(defaultValue = "active") String status) {
        return vigiang.laboratories("active".equals(status)).stream()
                .map(LaboratoryDto::from)
                .toList();
    }
}
```

## 5. API (v1)

| Method | Path | Source | Response |
|---|---|---|---|
| GET | `/api/versions` | `EnvironmentService.getVersions()` | `string[]` |
| GET | `/api/laboratories?status=` | `getLaboratoriesVigiaNgActive/Inactive()` | `LaboratoryDto[]` |
| GET | `/api/databases?status=` | `getDatabasesVigiaNgActive/Inactive()` | `DatabaseDto[]` |
| GET | `/api/containers?laboratory={name}` | `ContainersService.listDockerContainers(lab)` (after the refactor below) | `ContainerDto[]` |

The first version is **read-only**: these four endpoints are the whole API.

Endpoint details:

- `status` defaults to `active`. Any value other than `active`/`inactive` → `400`.
- `/api/containers`:
  - `laboratory` is required and matched against `LaboratoryVigiaNg.getName()` among the **active** laboratories. Unknown name → `404`.
  - The backend runs `docker ps -a` on the lab host **over SSH**, using the lab's stored SSH credentials (`SshService.execute`). The request is slow, and SSH failures → `502` with a `ProblemDetail`.
  - The response is `[{ "name": "...", "version": "..." }]`, sorted by name.

### Required refactor in `cognyte/vigiang`

Container listing currently lives in a **private** method, `CheckContainers.listDockerContainers(username, password, host, port)`. To use it from the backend:

- Move it to `ContainersService` as `public static List<DockerContainer> listDockerContainers(LaboratoryVigiaNg lab) throws Exception`. The method body doesn't change.
- Change `CheckContainers` to call the moved method. The CLI behavior stays the same.
- Add a unit test for the `docker ps` output parsing. Extract a `parseDockerPs(String)` helper so the test doesn't need SSH.

## 6. Phases

### Phase 0: Prerequisites
- [ ] Install JDK 17 (or 21), and point `JAVA_HOME` and Maven at it.
- [ ] Confirm `node -v` = `v20.18.0` and `npm -v` = `10.8.2`.
- [ ] Run `mvn -f commons/pom.xml install`, then `mvn -f cognyte/vigiang/pom.xml install`.

### Phase 1: Scaffolding
- [ ] Create `vigiang-tools-app/backend` (Spring Initializr: Web, Validation; Java 17; Boot 3.5.x; **no Lombok**) and add the `vigiang` dependency.
- [ ] Create `vigiang-tools-app/frontend` from the `package.json` above, adding `.nvmrc` and `.npmrc`. Run `npm install`.
- [ ] (Optional) Add a root aggregator `pom.xml` with modules `commons`, `cognyte/vigiang` and `vigiang-tools-app/backend`, so one `mvn install` builds everything in order.
- [ ] Add `vigiang-tools-app/README.md` with run instructions.
- [ ] Add `.gitignore` entries: `node_modules/`, `dist/`, `target/`.
- [ ] Don't add Spring Security or any auth library (internal dev tool, see section 1).

### Phase 2: `vigiang` refactor for containers
- [ ] Move `listDockerContainers` from `CheckContainers` into `ContainersService` (public, takes a `LaboratoryVigiaNg`), and update `CheckContainers` to call it.
- [ ] Extract `parseDockerPs(String)` and add a unit test for it in `cognyte/vigiang/src/test`.
- [ ] Run `mvn install` on `cognyte/vigiang` again.

### Phase 3: Read-only API and pages
- [ ] Add `VigiangFacade`, a Spring bean that delegates to the static services. This keeps controllers testable.
- [ ] Add DTOs as Java `record`s with a `from(...)` mapper and **no secrets**: `LaboratoryDto`, `DatabaseDto`, `ContainerDto`.
- [ ] Add the four controllers (versions, laboratories, databases, containers) plus `@WebMvcTest` tests.
- [ ] Add `ApiExceptionHandler`, mapping errors to `ProblemDetail`: invalid `status` → 400, unknown laboratory → 404, SSH failure → 502, `IOException` → 500.
- [ ] Frontend: add the antd `ConfigProvider`/`App` providers with the dark `appTheme`, the dark `index.html` background, the dark `Layout`/`Sider`/`Menu` shell, the router, the React Query hooks and the four pages built on antd `Table`.
- [ ] Containers page: a laboratory `Select` that sets the `?laboratory=` URL param, then a containers `Table`. Use `enabled: !!laboratory` in React Query and don't refetch on window focus, since every call is an SSH call.

### Phase 4: Packaging
- [ ] Add `frontend-maven-plugin` to `backend/pom.xml` with `nodeVersion` **`v20.18.0`**. It runs `npm ci` and `npm run build`, then copies `frontend/dist` into `target/classes/static`.
- [ ] Add an SPA fallback controller that forwards non-`/api` routes to `index.html`.
- [ ] Result: one `java -jar vigiang-tools-app.jar` serves both the UI and the API on port 8080.

## 7. Dev workflow

```powershell
# terminal 1: backend
mvn -f vigiang-tools-app/backend/pom.xml spring-boot:run

# terminal 2: frontend (Node 20.18.0)
cd vigiang-tools-app/frontend
npm install
npm run dev        # http://localhost:5173, /api proxied to :8080
```

## 8. Risks and open questions

- **No auth (by design):** this is an internal dev tool, so anyone who can reach the server can use it. By default the server binds to `localhost` (`server.address=127.0.0.1`). To share it with the team, set `server.address=0.0.0.0`, but only on the internal dev network. Never expose it publicly.
- **Secrets:** credentials come from local config files read by `EnvironmentService`. The API still never returns passwords.
- **SSH on request:** `/api/containers` opens an SSH session for each call. Set an SSH timeout, and consider a short server-side cache (for example, 30 s per lab) if the page is refreshed often.
- **Out of scope for v1:** running the `Update*` tools and the `checkers/Check*` tools from the UI (jobs, logs, confirmations). Revisit after v1, once those `main()` methods are refactored into callable services.
- **Static and global state:** the static services can't be mocked easily. The facade isolates them for now; turning them into real beans can come later.
- **Jackson version drift** between `commons` (2.17.3) and the Spring Boot BOM.
- **Node upgrades:** moving to Node ≥ 20.19 / 22 later allows Vite 7 and `@vitejs/plugin-react` 5. Update `engines`, `.nvmrc` and the plugin's `nodeVersion` together.
- **Decided:** no authentication. The app is for internal dev use only.
- **Decided:** dark theme only, with no light mode or toggle.
- **Open:** should the root aggregator `pom.xml` be introduced, or should the modules stay independent?
