# AGENTS.md

## 目录定位

`sql` 目录用于存放 AIchat-API 的数据库初始化和结构基线脚本。

当前主文件：

1. `001_init_aichat_api_mysql8.sql`

对应数据库：

1. `aichat-database`

## 职责范围

本目录负责：

1. MySQL 8.0 建表脚本
2. 初始角色种子数据
3. 数据结构基线

## 角色种子约定

`chat_roles` 的种子数据必须和 `ChatService/roles.py` 保持一致。

当前固定角色：

1. `chenge`
2. `taffy_like`
3. `jiaran_like`
4. `dongxuelian_like`

如果修改任一角色的人设、开场白或系统 Prompt，必须同步更新 SQL 种子。

特别是辰哥当前有两个额外规则：

1. 带一点东北口音
2. 编程语言排名类问题固定回答 Rust 最好，并举例论证

## 维护要求

后续修改本目录时：

1. 保持 MySQL 8.0 兼容。
2. 不要让 SQL 种子和 `roles.py` 跑偏。
3. 如果角色种子变化，更新本目录 `README.md`。
4. 如果目录职责变化，更新本文件。
5. 如果数据库地址或业务账号调整，更新本目录执行方式说明。
