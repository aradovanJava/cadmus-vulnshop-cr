# CR1 · Vulnerability Identification & Code Analysis

**60 minutes · individual work · CADMUS-CR-14 Secure Coding Java**

You are a member of the Blue Team. A vulnerable Java web application has been
handed to you before it goes to production. Your task is not to fix it — that
is CR2. Your task is to **find what is wrong, classify it, and prove you
looked**.

---

## What you are looking at

`cadmus-vulnshop-cr` is a Spring Boot 4 / Java 25 storefront with two tiers:

| Tier | Package | What it is |
|---|---|---|
| REST API | `eu.cadmus.vulnshop.web` | JSON endpoints under `/api/**`, used by a mobile client |
| MVC + Thymeleaf | `eu.cadmus.vulnshop.mvc` | The server-rendered storefront, account area, admin panel and support desk |

Weaknesses are injected in **both tiers**, and several of them live in the
Thymeleaf templates rather than in any `.java` file. A reviewer who only reads
controllers will miss them.

---

## Rules

- **Sandbox only.** The application binds to `127.0.0.1`. Do not change
  `server.address`, do not expose it, do not point it at real data. It contains
  at least one remote code execution path.
- Work **individually**. You may ask the instructor questions; you may not
  compare findings until the debrief.
- Do not open `lab/ANSWER_KEY.md`. It is not in your copy of the repository.

---

## Timeline

| Time | Task | Output |
|---|---|---|
| 00:00–00:05 | 0 · Environment check and orientation | toolchain confirmed |
| 00:05–00:15 | 1 · Build, run, and start the SonarQube scan | scan running |
| 00:15–00:25 | 2 · Dependency analysis with Snyk | 3 advisories assessed |
| 00:25–00:40 | 3 · Triage the static analysis findings | classified findings |
| 00:40–00:55 | 4 · Manual inspection — find what the tools missed | ≥ 4 further findings |
| 00:55–01:00 | 5 · Submit | `FINDINGS.md` |

The scan in Task 1 takes two to four minutes. **Start it and then read code
while it runs.** Do not watch the progress bar.

---

## Task 0 — Environment check (5 min)

```powershell
powershell -ExecutionPolicy Bypass -File tools\verify-env.ps1
```

Everything should report `OK`. If SonarQube is not running:

```powershell
docker compose up -d sonarqube
```

Give it 60–90 seconds, then open <http://localhost:9000>. Log in with
`admin` / `admin` and set a throwaway password — this instance is disposable.

Create an analysis token: **My Account → Security → Generate Token**, type
*Global Analysis Token*. Copy it; you need it in Task 1.

---

## Task 1 — Build, run, and scan (10 min)

Start the application in one terminal:

```powershell
powershell -ExecutionPolicy Bypass -File tools\run-app.ps1
```

Open <http://127.0.0.1:8080/shop> and log in as `alice`. All four training
accounts (`alice`, `bob`, `carol`, `adminjoe`) share the same password, which
is on the login page.

Spend three minutes **using the application as a user would**. Click through
the shop, place an order, open your order list, submit a support ticket. You
are building a mental map of the request surface — you cannot review what you
cannot picture.

In a second terminal, start the scan:

```powershell
powershell -ExecutionPolicy Bypass -File tools\scan-sonar.ps1 -Token sqp_your_token
```

While it runs, answer these three questions in `FINDINGS.md`:

1. How many distinct HTTP entry points did you find by clicking around?
2. Which pages change behaviour depending on who is logged in?
3. Which page displays something that another user could have written?

Question 3 is doing real work. Hold on to your answer.

---

## Task 2 — Dependency analysis (10 min)

```powershell
powershell -ExecutionPolicy Bypass -File tools\scan-snyk.ps1
```

This produces three things: the resolved dependency tree, Snyk's dependency
findings, and Snyk Code's static analysis of the first-party source.

**Record these two numbers.** Count the dependencies declared in `pom.xml`, and
count the artefacts in `lab/dependency-tree.txt`. The ratio is the point.

Then take the dependency advisories. For each one, do **not** simply copy the
severity. Answer the four questions from N4, stopping at the first *no*:

1. Is the vulnerable artefact present in the resolved graph?
2. Is the vulnerable code path present in that version?
3. Does application code call it, directly or transitively?
4. Can an attacker control the input that reaches it?

Question 3 is answerable in about ninety seconds per advisory with a project
-wide search in IntelliJ. Do it — do not guess.

> Record, for each advisory: the CVSS severity, your reachability verdict, and
> **the order in which you would actually fix them**. If your order differs
> from the severity order, say why. That sentence is the finding.

---

## Task 3 — Triage the static analysis findings (15 min)

Open <http://localhost:9000> and select **cadmus-vulnshop-cr**.

Two tabs matter, and they are different kinds of claim:

- **Issues** — the analyser asserts a defect.
- **Security Hotspots** — the analyser found a security-sensitive API and is
  *asking you* whether the context makes it dangerous. Accepting the default
  severity means not answering the question.

For each finding, record a verdict and **your own severity, decided before you
read the tool's**:

| Code | Meaning |
|---|---|
| `TP` | Real and reachable |
| `FP-scope` | Real pattern, wrong place — test code, generated code, dead code |
| `FP-context` | Safe here because of something the tool cannot see. Say what. |
| `DIS` | Disputed — you need a fact you do not have. Name the fact. |
| `NOT-SEC` | Real issue, not a security issue |

Two things to watch for specifically:

- At least one finding is reported at a **much lower severity than it deserves**,
  because the rule cannot know what the method it sits in is *for*. Read the
  surrounding code, not the snippet.
- At least one finding is reported in a location where it **does not matter**.
  Deciding the correct disposition is more interesting than the verdict.

---

## Task 4 — Manual inspection (15 min)

Close the scanner. This is the part that decides the exercise.

Every weakness below is real and exploitable, and **no rule in either tool
reports any of them**. Use the review checklist from N3 and the four absence
questions:

- *How many attempts are allowed here?*
- *What is the base rule for an endpoint nobody annotated?*
- *What alerts when this happens?*
- *What happens when this dependency is slow?*

Where to look, in order of yield:

| File | Ask |
|---|---|
| `config/SecurityConfig.java` | Read the **last line** of the filter chain. What does it mean for an endpoint added next week? |
| `mvc/AdminMvcController.java` | The Admin link is hidden from the navigation for non-admins. What happens if you type the URL? |
| `mvc/AccountMvcController.java` | Two order methods. One is scoped to the caller. Compare them, then look at what an order id is. |
| `templates/account/profile.html` | Which fields reach the browser that are not displayed on the page? |
| `templates/shop/product.html` | Who writes a product description, and how is it rendered? |
| `mvc/CartMvcController.java` | Which values does the server decide, and which does it accept from the form? |
| `templates/fragments/layout.html` | How is the external script loaded, and what would happen if the CDN served something else? |

**Try things in the browser.** Log in as `alice`, note your order number, then
change the number in the URL. Log in as a non-admin and navigate directly to
`/admin/users`. The application is running — use it.

Target: **at least four** weaknesses that produced no tool finding. There are
more than ten.

---

## Task 5 — Submit (5 min)

Complete `lab/FINDINGS.md` and save it. It must contain:

1. The two dependency counts, and your remediation order with a reason.
2. Every tool finding you triaged, with your verdict and your severity.
3. Every weakness you found manually, with an OWASP Top 10:2025 category.
4. **One finding written up in full** using the six-field record — your choice,
   but choose the one you think is most serious.
5. Your final tally:
   - weaknesses the tools found: ____
   - weaknesses you found that the tools did not: ____

Point 5 is the number the debrief opens with.

---

## What CR1 is assessing

Not how many weaknesses you find. Whether you can:

- **classify** a weakness into the right 2025 category and defend the choice;
- **dispose** of a tool finding with a reason someone else could check;
- **re-rate** a finding against what the code is actually for;
- notice what is **not there**.
