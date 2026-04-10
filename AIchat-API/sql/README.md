# sql

## 简介

`sql` 目录用于存放 AIchat-API 的数据库初始化和结构基线脚本。

当前主文件：

1. `001_init_aichat_api_mysql8.sql`

对应数据库：

1. `aichat-database`

## 核心表

1. `app_users`
2. `chat_roles`
3. `chat_conversations`
4. `chat_messages`

## 角色种子约定

`chat_roles` 的种子数据必须和 `ChatService/roles.py` 保持一致。

当前固定角色：

1. `chenge`
2. `taffy_like`
3. `jiaran_like`
4. `dongxuelian_like`

说明：

1. 角色昵称、archetype、头像、背景、简介、开场白、系统 Prompt 都应同步。
2. 辰哥当前包含“轻微东北口音”和“编程语言排名类问题固定站 Rust”两条特殊规则，SQL 种子也要跟上。

## 执行方式

```bash
mysql -h <host> -P 3306 -u <user> -p -D "aichat-database" < 001_init_aichat_api_mysql8.sql
```

## 维护要求

1. 保持 MySQL 8.0 兼容。
2. 如果 `roles.py` 变化，优先同步 SQL。
3. 如果表结构变化，同步本目录 `README.md` 和 `AGENTS.md`。
