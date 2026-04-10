---
name: learning-exam
description: Generate compact learning checks for one technical topic or current-project blocker, then grade the user's answer. Use only when the user explicitly invokes $learning-exam for quiz-style concept checks, small code reading or writing tasks, or follow-up grading with commands like "$learning-exam quiz ...", "$learning-exam q ...", "$learning-exam code ...", "$learning-exam c ...", "$learning-exam grade ...", or "$learning-exam g ...".
---

# Learning Exam

Help the user check whether they actually understood a topic after learning it.

Do not turn this skill into a broad tutorial, a real interview loop, or a full feature implementation task.

## Mode parsing

Parse the text after `$learning-exam`.

- If the first token is `quiz`, use `quiz` mode.
- If the first token is `q`, use `quiz` mode.
- If the first token is `code`, use `code` mode.
- If the first token is `c`, use `code` mode.
- If the first token is `grade`, use `grade` mode.
- If the first token is `g`, use `grade` mode.
- If no mode token is provided, default to `quiz`.

Treat the remaining text as the topic, blocker, concept, answer submission, or task context.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the question or grading in the user's actual project before giving generic content.
3. Focus on one main topic or one main learning target.

Always follow these constraints:

1. Keep the scope tight and useful for checking real understanding.
2. Prefer project relevance over completeness.
3. Keep the exam small enough to finish quickly.
4. Do not silently drift into full teaching mode unless the user explicitly asks to switch.
5. Do not write notes, docs, or files unless the user explicitly asks.

## Quiz mode

Use this mode to generate a compact concept check.

The default format is mixed:

1. 2 to 3 multiple-choice questions
2. 1 short-answer question

The questions should test:

1. concept distinction
2. mechanism understanding
3. why the concept matters in the current project

Output with exactly these sections:

### 考察点

State the one main concept or blocker being tested.

### 题目

Provide the questions.

Rules:

1. Keep the whole quiz compact.
2. Multiple-choice questions must have clear options.
3. The short-answer question must require the user to explain in their own words.

### 作答要求

Tell the user how to answer.

Use a simple format such as:

1. `1:A`
2. `2:C`
3. `简答: ...`

### 评分标准

State what good answers should demonstrate, without giving the final answers.

## Code mode

Use this mode to generate a small code reading or writing task based on the current project.

The task must be solvable in about 10 to 30 minutes.

Allowed task types:

1. read-code explanation
2. trace behavior through one small flow
3. write a tiny function, branch, state update, or small local change

Disallowed task types:

1. full feature implementation
2. multi-file large refactors
3. architecture redesign
4. large debugging sessions

Output with exactly these sections:

### 考察点

State the one main idea being tested.

### 题目

Describe the task.

When useful, point to the most relevant file or module.

### 作答要求

Tell the user what kind of answer is expected.

Examples:

1. explain the behavior
2. describe the bug
3. provide a small patch or code snippet

### 评分标准

State what a strong answer should include, without giving the final answer.

## Grade mode

Use this mode after the user answers a quiz or code task.

Grade the user's answer against the most recent relevant exam in the current conversation.

If there is no clear prior exam in context, ask for the exam content and the user's answer in one compact prompt.

Output with exactly these sections:

### 得分

Give a score on a 100-point scale.

Keep it coarse, practical, and easy to read.

Use rough whole-number scoring instead of overly fine-grained judging.

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
4. coding tasks that are too large to count as a learning check

If the user actually wants implementation, switch back to normal execution behavior instead of forcing an exam workflow.
