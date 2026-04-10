---
name: top-down-learning
description: Explain one technical blocker in a project-driven, top-down way, or outline a compact knowledge tree and study order for a topic. Use only when the user explicitly invokes $top-down-learning, especially for requests like "$top-down-learning teach ...", "$top-down-learning t ...", "$top-down-learning map ...", "$top-down-learning m ...", or when they want small-scope concept expansion tied to the current project instead of a full tutorial.
---

# Top-Down Learning

Help the user learn just enough to keep moving in the current project.

Do not turn this skill into a broad tutorial, a course outline, or a generic encyclopedia answer.

## Mode parsing

Parse the text after `$top-down-learning`.

- If the first token is `teach`, use `teach` mode.
- If the first token is `t`, use `teach` mode.
- If the first token is `map`, use `map` mode.
- If the first token is `m`, use `map` mode.
- If no mode token is provided, default to `teach`.

Treat the remaining text as the topic, blocker, concept, error, or implementation point to explain.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the explanation in the user's actual project before giving general theory.
3. Focus on one main blocker or one main topic.

Always follow these constraints:

1. Keep the scope tight and useful for immediate progress.
2. Prefer project relevance over completeness.
3. If the context is insufficient, ask at most one narrow clarifying question.
4. Do not generate long learning roadmaps unless the user explicitly asks.
5. Do not write notes, docs, or files unless the user explicitly asks.

## Teach mode

Use this mode when the user is blocked on one concept, pattern, error, or design choice.

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

## Map mode

Use this mode when the user wants a compact knowledge tree and a lightweight study order for one topic.

Output with exactly these sections:

### 核心主题

State the topic in one short paragraph and define its role in the current project context.

### 知识点目录树

Provide a compact knowledge tree, not a filesystem tree.

Rules:

1. Keep the tree depth to 2 or 3 levels.
2. Keep the breadth narrow.
3. Do not fully explain every node.

### 推荐学习顺序

Give 4 to 7 ordered learning steps.

Each step should be short and practical.

### 和当前项目的连接点

List the most relevant links between the topic and the user's current project, files, architecture, or code paths.

## Refusals and boundaries

Do not use this skill for:

1. pure feature implementation requests
2. full-course teaching requests
3. unrelated general-interest knowledge dumps
4. broad surveys that are not tied to the current project

If the user actually wants implementation, switch back to normal execution behavior instead of forcing a teaching answer.
