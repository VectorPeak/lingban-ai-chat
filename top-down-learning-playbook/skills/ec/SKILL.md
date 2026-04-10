---
name: ec
description: Generate a small code reading or writing task for one technical topic in the current project. Use only when the user explicitly invokes $ec and wants a compact code-based learning check rather than a real feature implementation task.
---

# EC

Help the user check whether they can apply one concept in code.

Do not turn this skill into a broad tutorial, a real interview loop, or a full feature implementation task.

## Input parsing

Treat all text after `$ec` as the topic, blocker, or implementation point to test.

If no topic is provided, ask at most one narrow clarifying question.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the task in the user's actual project before giving generic coding questions.
3. Focus on one main topic.

Always follow these constraints:

1. Keep the scope tight and useful for checking real understanding.
2. Prefer project relevance over completeness.
3. Keep the task solvable in about 10 to 30 minutes.
4. Do not drift into full teaching mode unless the user explicitly asks.
5. Do not write notes, docs, or files unless the user explicitly asks.

## Output

Output with exactly these sections:

### 考察点

State the one main idea being tested.

### 题目

Describe a small code reading or writing task.

Allowed task types:

1. read-code explanation
2. trace behavior through one small flow
3. write a tiny function, branch, state update, or small local change

Disallowed task types:

1. full feature implementation
2. multi-file large refactors
3. architecture redesign
4. large debugging sessions

When useful, point to the most relevant file or module.

### 作答要求

Tell the user what kind of answer is expected.

Examples:

1. explain the behavior
2. describe the bug
3. provide a small patch or code snippet

### 评分标准

State what a strong answer should include, without giving the final answer.

## Refusals and boundaries

Do not use this skill for:

1. pure feature implementation requests
2. broad tutorial generation
3. unrelated general-interest knowledge dumps
4. coding tasks that are too large to count as a learning check

If the user actually wants implementation, switch back to normal execution behavior instead of forcing an exam workflow.
