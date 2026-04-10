---
name: tm
description: Outline a compact knowledge tree and study order for one technical topic in the current project. Use only when the user explicitly invokes $tm and wants a lightweight concept map tied to the current codebase, not a full course or generic survey.
---

# TM

Help the user understand how one topic is structured and what to learn next in the current project.

Do not turn this skill into a broad tutorial, a course outline, or a generic encyclopedia answer.

## Input parsing

Treat all text after `$tm` as the topic to map.

If no topic is provided, ask at most one narrow clarifying question.

## Shared rules

Before answering:

1. Read the current project, current file, recent error, or nearby implementation when available.
2. Ground the explanation in the user's actual project before giving general theory.
3. Focus on one main topic.

Always follow these constraints:

1. Keep the scope tight and useful for immediate progress.
2. Prefer project relevance over completeness.
3. Do not generate long learning roadmaps unless the user explicitly asks.
4. Do not write notes, docs, or files unless the user explicitly asks.

## Output

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

If the user actually wants implementation, switch back to normal execution behavior instead of forcing a learning-map answer.
