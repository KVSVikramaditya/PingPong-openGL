I want to build a skill or agent—whatever you call it—for this workflow.
You should now have a basic understanding of what the project is about. We generate a feed file using a Spring Boot project. The file contains columns for various accounts, and the data must be correct.
We know how to implement functionality and run the project to generate a feed file. We have a reference feed file as well as a comparison script that checks the generated feed file against the reference file to provide a clear list of differences. From there, we determine whether the discrepancies stem from data retrieval, business logic, or request/response differences from an endpoint. We must analyze why discrepancies happen and eliminate or minimize them. This is where testing comes in.
Each feed file is different, so we must always have a comparison script to verify correctness. When I say "correct," the column names, data values, account counts, and naming conventions must match across both files.
After implementing changes, the script must generate a comparison file. Based on those diffs, analyze why differences occurred, update the implementation plan, rebuild, rerun, and retest until the output matches expectations.
Whenever I initiate this data feed workflow, follow these stages:
 Requirement Gathering & Verification: First, confirm whether all necessary inputs are present (e.g., implementation requirements, reference feed files, and clear target criteria). If a reference file is missing, do not proceed on assumptions—flag it immediately.
 Execution Plan: Provide a concise, step-by-step loop outlining what you will do so I can verify your approach before you proceed.
 OpenSpec Process: Generate your proposal and design artifacts under the OpenSpec standard, clearly defining what needs to be done.
 Autonomous Implementation & Testing: Our standard developer workflow requires three manual checkpoints. This workflow must eliminate manual intervention by having you test your own changes. Implement the code, run the comparison script, analyze failures, and iterate autonomously until you achieve 100% accuracy.
 Documentation & Submission: Document test verifications in the design spec per OpenSpec guidelines, archive artifacts, commit, push to the Git branch, and open a PR.
Available Tooling & Testing Standards
 Database Probe: Use ⁠SELECT⁠ queries via the probe to inspect table schemas and extract raw data.
 Perflink Endpoint: Query Perflink endpoints to inspect request/response payloads for given accounts and dates to cross-reference against file data.
 Thorough Verification: Never rely on only one or two accounts. Test thoroughly across randomized accounts, multiple dates, and broader data samples. Aim for 100% parity with the reference feed file.
 Blocker Escalation: If you hit a blocking issue that cannot be resolved independently, ask immediately.
Use our existing skills, workflows, and documentation to build this generic, automated feed file workflow so we can review it.
