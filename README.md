# CADMUS VulnShop (CR1)

A deliberately vulnerable Spring Boot 4 / Java 25 storefront, used in Cyber
Range node **CR1 — Vulnerability Identification & Code Analysis**.

> **Training system.** Deliberately exploitable, including at least one remote
> code execution path. Binds to `127.0.0.1` only. Sandbox use only, no real
> data, never deployed. Read `SAFETY.md` before running it.

---

## Architecture

Two tiers, deliberately, so that a review has to cover both:

```
eu.cadmus.vulnshop
├── web/     REST API      JSON endpoints under /api/**   (mobile client)
├── mvc/     MVC           Thymeleaf storefront, account area, admin, support
├── service/               crypto, authorisation, audit
├── domain/  repo/         JPA entities and repositories
└── config/  SecurityConfig
```

Weaknesses are injected in both tiers, and several live in the **Thymeleaf
templates** rather than in any `.java` file. If `sonar.inclusions` is edited to
drop `**/*.html`, three of them stop being reported at all.

| Route | Tier | Notes |
|---|---|---|
| `/shop`, `/shop/product/{id}` | MVC | storefront |
| `/account/login`, `/account/profile`, `/account/orders`, `/account/orders/{id}` | MVC | account area |
| `/cart`, `/cart/checkout` | MVC | checkout |
| `/support/ticket`, `/support/attachment` | MVC | support desk |
| `/admin/dashboard`, `/admin/users`, `/admin/impersonate` | MVC | administration |
| `/api/**` | REST | orders, products, search, reports, import, password reset |
| `/actuator/**` | — | exposed, deliberately |

---

## Running it

```powershell
docker compose up -d sonarqube                                    # analysis server
powershell -ExecutionPolicy Bypass -File tools\verify-env.ps1     # check the toolchain
powershell -ExecutionPolicy Bypass -File tools\run-app.ps1        # start the app
```

<http://127.0.0.1:8080/shop>

| Account | Role | Tenant |
|---|---|---|
| `alice` | USER | acme |
| `bob` | USER | globex |
| `carol` | SUPPORT | acme |
| `adminjoe` | ADMIN | acme |

All four share the same password, shown on the login page. The seed data is
synthetic and every address is `.invalid`.

---

## Analysis toolchain

| Tool | Script | What it sees |
|---|---|---|
| SonarQube | `tools\scan-sonar.ps1` | your code — Java and templates |
| Snyk Open Source | `tools\scan-snyk.ps1` | other people's code — the resolved dependency graph |
| Snyk Code | `tools\scan-snyk.ps1` | your code again, from a different rule set |
| Manual review | you | what is **not** there |

The fourth row is the one CR1 is actually assessing. Roughly half of what is
wrong with this application produces no finding in any of the first three.

Two tabs in SonarQube matter and they are different kinds of claim:
**Issues** assert a defect; **Security Hotspots** ask whether the context makes
a security-sensitive API dangerous. Accepting a hotspot's default severity
means not answering the question it asked.

---

## Lab material

```
lab/LAB_GUIDE.md     the 60-minute exercise — start here
lab/FINDINGS.md      the template you complete and submit
```

Instructors: `docs/ANSWER_KEY.md` and `docs/FIXES.md` must be removed before
distribution.

---

## What comes next

| Node | Uses this repository for |
|---|---|
| **CR1** | finding and classifying the weaknesses |
| **CR2** | refactoring them, against automated validation |
| **CR3** | dependency remediation and SBOM generation |
| **CR4** | completing the `TODO(CR4)` steps in the CI workflow |

`.github/workflows/security-pipeline.yml` is deliberately incomplete.
