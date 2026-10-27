# SAFETY NOTICE — read before running

`cadmus-vulnshop` is an **intentionally vulnerable** application. It is built to be
attacked, scanned and repaired as a training exercise for CADMUS-CR-14. Every
weakness in it is deliberate.

## Hard rules

1. **Run it only inside the Cyber Range sandbox**, or an isolated VM with no route
   to any network you care about. It ships bound to `127.0.0.1` — leave it that way.
2. **Never load real data.** No real customers, no real addresses, no real email
   addresses, no production database dumps. The seed data uses the reserved
   `.invalid` TLD for exactly this reason.
3. **Never reuse a real credential or secret**, not even a retired one.
4. **Never deploy it.** Not to a staging server, not to a container platform, not
   behind a VPN, not "just for a demo".
5. **Do not publish it** to a public registry or repository under a name that could
   be mistaken for a real component. It carries a `-TRAINING` version qualifier.
6. **Delete the sandbox afterwards.** Do not keep it running between sessions.

## Why the rules are strict

The application combines an unauthenticated deserialisation sink with an
intentionally outdated `commons-collections` dependency, a command-execution
endpoint, an SSRF primitive and an open H2 console. That combination is a
genuine remote-code-execution path on the host that runs it. It is included
because participants need to learn that a "dormant" vulnerable dependency and a
dangerous sink are only dangerous *together* — that is the reachability lesson
in CR3. It is also the reason this must stay in a sandbox.

## Responsible use

This repository is teaching material for defensive work: finding, understanding
and remediating weaknesses in code you own. It does not contain exploit payloads,
and none should be added to it. If you are adapting it for another course, keep
this notice with it.

## Reporting

If you find an unintended defect (one not listed in `VULNERABILITY_MAP.md`) —
particularly anything that weakens the containment above — raise it with the
course maintainers rather than fixing it silently. Unintended findings are
useful: they are the ones that show up in real code reviews too.
