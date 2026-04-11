# ChatService

## 简介

`ChatService` 是 AIchat App 的聊天云函数，负责：

1. 获取角色列表
2. 文本聊天
3. 流式聊天
4. 单张图片输入与图片理解
5. 实时天气查询
6. 聊天历史
7. 清空当前聊天
8. 校验 `LoginService` 签发的 JWT

默认入口文件：

1. `app.py`

公网地址：
`https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run`

## 当前角色

当前角色固定为：

1. `chenge` / 辰哥 / 平易近人的技术专家
2. `taffy_like` / 塔菲 / 古灵精怪的发明家少女
3. `jiaran_like` / 嘉然 / 元气甜妹吃货
4. `dongxuelian_like` / 冬雪莲 / 清冷系吸血鬼少女

默认角色：

1. `chenge`

### 辰哥特殊规则

`roles.py` 中，辰哥当前有两个明确规则：

1. 说话带一点东北口音，但不要过重。
2. 如果用户问“什么编程语言最好”“编程语言排名”“最值得学的语言”等类似问题，固定回答 Rust 是世界上最好的语言，并举例论证。

## 目录说明

1. `app.py`
   聊天、角色、历史、清空和健康检查接口。
2. `auth.py`
   JWT 校验逻辑。
3. `config.py`
   模型、数据库、天气、JWT 等配置读取。
4. `db.py`
   MySQL 连接与连通性检查。
5. `repositories.py`
   用户、角色、会话、消息的仓储层。
6. `roles.py`
   4 个角色定义与 Prompt。
7. `llm_service.py`
   Qwen 调用、图片输入、天气工具编排和流式输出。
8. `weather_service.py`
   和风天气城市查询与实时天气封装。
9. `requirements.txt`
   依赖列表。
10. `.env.example`
   环境变量示例。

## 对外接口

1. `GET /`
2. `GET /health`
3. `GET /api/chat/roles`
4. `GET /api/chat/history`
5. `POST /api/chat/clear`
6. `POST /api/chat/completions`
7. `POST /api/chat/stream`

## 数据库

数据库：

1. `aichat-database`

当前已验证可从本机直接联通的业务配置：

1. `MYSQL_HOST=118.25.150.154`
2. `MYSQL_PORT=3306`
3. `MYSQL_USER=AIchat_app_SQL_admin`
4. `MYSQL_PASSWORD=AIchat123456`

核心表：

1. `app_users`
2. `chat_roles`
3. `chat_conversations`
4. `chat_messages`

说明：

1. `chat_roles` 以 `roles.py` 为代码基线。
2. 不在 `roles.py` 中的旧角色会被同步失活为 `is_active = 0`。
3. 当前已验证可从本机直接联通的 MySQL 配置指向 `118.25.150.154:3306`，数据库名为 `aichat-database`。

## 关键环境变量

1. `DASHSCOPE_API_KEY`
2. `DASHSCOPE_BASE_URL`
3. `DASHSCOPE_TEXT_MODEL`
4. `DASHSCOPE_VISION_MODEL`
5. `LOGIN_TOKEN_SECRET`
6. `LOGIN_TOKEN_ALGORITHM`
7. `LOGIN_TOKEN_ISSUER`
8. `MYSQL_HOST`
9. `MYSQL_PORT`
10. `MYSQL_USER`
11. `MYSQL_PASSWORD`
12. `MYSQL_DATABASE`
13. `QWEATHER_API_HOST`
14. `QWEATHER_API_KEY`
15. `CHAT_REQUIRE_LOGIN`

## 运行时说明

1. `DASHSCOPE_REQUEST_TIMEOUT_SECONDS` 当前默认是 `25` 秒，用于减少与函数超时撞车。
2. 图片请求会走视觉模型分支。
3. 普通文本请求默认走文本模型分支。
4. 天气能力只会在显式传入 `weatherCity`，或用户问题明显涉及天气时触发。

## 本地联通验证

基于 2026-04-11 的本地验证结果：

1. `GET /health` 本地返回 `configured=true`、`databaseConfigured=true`、`databaseReachable=true`。
2. `GET /api/chat/roles` 本地返回 4 个角色。
3. `GET /api/chat/history` 本地可正常读取当前活跃会话。
4. `POST /api/chat/clear` 本地可正常清空当前会话。
5. `POST /api/chat/completions` 在真实 `DASHSCOPE_API_KEY` 下，本地已成功返回模型回复。

补充说明：

1. 如果本机设置了 `ALL_PROXY` / `HTTP_PROXY` / `HTTPS_PROXY`，可能导致大模型调用走到本地代理。
2. 如遇到代理相关错误，建议先清理这些环境变量后再测试。

## 部署

阿里云函数计算 WebIDE 中安装依赖时，必须执行：

```bash
pip install -r requirements.txt -t .
```

安装后需要重新部署，线上实例才会更新。
