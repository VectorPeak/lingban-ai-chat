---
name: eq
description: Generate a compact concept quiz for one technical topic or current-project blocker. Use only when the user explicitly invokes $eq and wants a small learning check with multiple-choice and short-answer questions tied to the current project.
---

# EQ

Help the user check whether they actually understood one topic after learning it.

Do not turn this skill into a broad tutorial, a real interview loop, or a full feature implementation task.

## Input parsing

Treat all text after `$eq` as the topic, blocker, or concept to test.

If no topic is provided, ask at most one narrow clarifying question.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the quiz in the user's actual project before giving generic questions.
3. Focus on one main topic.

Always follow these constraints:

1. Keep the scope tight and useful for checking real understanding.
2. Prefer project relevance over completeness.
3. Keep the quiz compact.
4. Do not drift into full teaching mode unless the user explicitly asks.
5. Do not write notes, docs, or files unless the user explicitly asks.

## Output

Output with exactly these sections:

### 考察点

State the one main concept or blocker being tested.

### 题目

Provide a compact mixed quiz:

1. 2 to 3 multiple-choice questions
2. 1 short-answer question

Rules:

1. Multiple-choice questions must have clear options.
2. The short-answer question must require explanation in the user's own words.
3. The questions should test concept distinction, mechanism understanding, and project relevance.

### 作答要求

Tell the user how to answer.

Use a simple format such as:

1. `1:A`
2. `2:C`
3. `简答: ...`

### 评分标准

State what good answers should demonstrate, without giving the final answers.

## Refusals and boundaries

Do not use this skill for:

1. pure feature implementation requests
2. broad tutorial generation
3. unrelated general-interest knowledge dumps
4. coding tasks that are too large to count as a learning check

If the user actually wants implementation, switch back to normal execution behavior instead of forcing an exam workflow.
