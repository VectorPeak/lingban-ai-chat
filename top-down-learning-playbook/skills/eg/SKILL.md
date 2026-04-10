---
name: eg
description: Grade the user's answer to a recent learning quiz or small code task. Use only when the user explicitly invokes $eg and wants concise scoring, mistake analysis, and follow-up improvement advice tied to the current project.
---

# EG

Help the user evaluate whether they actually understood the topic they studied.

Do not turn this skill into a broad tutorial, a real interview loop, or a full feature implementation task.

## Input parsing

Treat all text after `$eg` as the grading request or answer submission.

If no usable answer is provided, ask for the user's answer and, if needed, the exam content in one compact prompt.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, nearby implementation, and recent exam in the conversation when available.
2. Ground the grading in the user's actual project before giving generic feedback.
3. Focus on one main topic or one main exam target.

Always follow these constraints:

1. Keep the grading practical and easy to read.
2. Prefer project relevance over completeness.
3. Use a coarse 100-point scale.
4. Do not drift into full teaching mode unless the user explicitly asks.
5. Do not write notes, docs, or files unless the user explicitly asks.

## Output

Output with exactly these sections:

### 得分

Give a score on a 100-point scale.

Keep it coarse, practical, and easy to read.

### 总评

Give a short overall judgment.

### 错因

List the key mistakes, missing reasoning, or weak spots.

### 补强建议

Give 1 to 3 concrete next steps for improvement.

## Refusals and boundaries

Do not use this skill for:

1. pure feature implementation requests
2. broad tutorial generation
3. unrelated general-interest knowledge dumps
4. grading requests with no usable task context after one clarification

If the user actually wants implementation, switch back to normal execution behavior instead of forcing an exam workflow.
