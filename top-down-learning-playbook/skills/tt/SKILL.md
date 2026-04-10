---
name: tt
description: Explain one technical blocker in a project-driven, top-down way. Use only when the user explicitly invokes $tt and wants to enter a compact teaching mode tied to the current project, blocker, concept, error, or implementation point, without expanding into a full tutorial.
---

# TT

Help the user learn just enough to keep moving in the current project.

Do not turn this skill into a broad tutorial, a course outline, or a generic encyclopedia answer.

## Input parsing

Treat all text after `$tt` as the blocker, concept, error, or implementation point to explain.

If no topic is provided, ask at most one narrow clarifying question.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the explanation in the user's actual project before giving general theory.
3. Focus on one main blocker.

Always follow these constraints:

1. Keep the scope tight and useful for immediate progress.
2. Prefer project relevance over completeness.
3. Do not generate long learning roadmaps unless the user explicitly asks.
4. Do not write notes, docs, or files unless the user explicitly asks.

## Output

Output with exactly these sections:

### 当前卡点

Restate the blocker in 1 to 3 short sentences, tied to the current project.

### 关键解释

Explain only the minimum theory the user needs to move forward.

### 一个贴近当前项目的例子

Give one concrete example that matches the current repo, file, architecture, or implementation pattern.

### 相关知识点

List 3 to 5 related points.

Each point should be short and should explain why it matters in the current project.

### 下一步建议

Give 1 to 3 concrete next actions, such as:

1. which file to read next
2. which behavior to verify
3. which concept to understand next

## Refusals and boundaries

Do not use this skill for:

1. pure feature implementation requests
2. full-course teaching requests
3. unrelated general-interest knowledge dumps
4. broad surveys that are not tied to the current project

If the user actually wants implementation, switch back to normal execution behavior instead of forcing a teaching answer.
