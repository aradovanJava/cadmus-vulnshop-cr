# CR1 findings — <your name>

Date: ____________  Environment: VM1 (Windows 11)

---

## Task 1 — Orientation

| Question | Answer |
|---|---|
| Distinct HTTP entry points found by browsing | |
| Pages whose behaviour depends on who is logged in | |
| Page that displays something another user could have written | |

---

## Task 2 — Dependencies

| | Count |
|---|---|
| Declared in `pom.xml` | |
| Resolved in `lab/dependency-tree.txt` | |

| Component | Advisory | CVSS | Reachable? | Evidence (how you checked) |
|---|---|---|---|---|
| | | | | |
| | | | | |
| | | | | |

**My remediation order:** ____________________________________________

**It differs from severity order because:**

>

---

## Task 3 — Tool findings triaged

Verdicts: `TP` · `FP-scope` · `FP-context` · `DIS` · `NOT-SEC`

| # | Rule | Location | Tool severity | Verdict | My severity | Why they differ |
|---|---|---|---|---|---|---|
| 1 | | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |
| 4 | | | | | | |
| 5 | | | | | | |
| 6 | | | | | | |
| 7 | | | | | | |
| 8 | | | | | | |
| 9 | | | | | | |
| 10 | | | | | | |

**Findings I re-rated upward, and why:**

>

---

## Task 4 — Found by manual inspection only

| # | Weakness | File | OWASP 2025 | Which question found it |
|---|---|---|---|---|
| 1 | | | | |
| 2 | | | | |
| 3 | | | | |
| 4 | | | | |
| 5 | | | | |
| 6 | | | | |

---

## Task 5 — One finding in full

**ID** VULN-____ · **Category** A____ ______________________ · **Severity** ________

**Title** (the weakness, not the symptom)

>

**1 · Location** — file, line, who can reach it, and where else the same shape occurs

>

**2 · Root cause, stated as a decision** — not "the check is missing"

>

**3 · Impact in this application** — what, with what access, to what data

>

**4 · Proposed remediation** — name the pattern, and why that one

>

**5 · Cost** — what must change outside this file

>

**6 · Proof** — the test that fails now and passes after

>

---

## Tally

| | Count |
|---|---|
| Weaknesses the tools reported (true positives) | |
| Weaknesses I found that the tools did not | |
| Tool findings I disposed of as false positives | |
| Tool findings I re-rated | |
