# AGENTS.md

## 项目定位

本项目是 AI 聊天 App 的后端仓库，目录为 `AIchat-API`。

仓库当前包含两个可独立部署的 Python 云函数：

1. `LoginService`
2. `ChatService`

每个服务目录都必须保持自包含，默认入口文件都是 `app.py`。

## AGENTS 分层

根目录这份 `AGENTS.md` 负责全局架构、跨服务约定、部署规则和文档维护要求。

如果修改发生在子目录内，还必须同时遵守对应子目录下的 `AGENTS.md`：

1. `LoginService/AGENTS.md`
2. `ChatService/AGENTS.md`
3. `sql/AGENTS.md`

## 服务说明

### LoginService

`LoginService` 负责：

1. 获取短信验证码
2. 校验短信验证码
3. 手机号验证码登录
4. 登录成功后签发 JWT

公网地址：
`https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run`

额外说明：

1. 当前支持一个仅供调试使用的隐藏测试账号机制。
2. 该机制默认关闭，只有显式开启 `ENABLE_TEST_LOGIN_ACCOUNT=true` 时才生效。
3. 测试账号逻辑不能出现在任何前端测试页面里。

### ChatService

`ChatService` 负责：

1. 返回 4 个预设聊天角色
2. 与 Qwen 模型聊天
3. 支持普通返回和流式返回
4. 支持单张图片输入
5. 支持实时天气查询
6. 支持聊天历史
7. 支持清空当前聊天
8. 校验 `LoginService` 签发的 JWT

公网地址：
`https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run`

## 当前角色

当前固定角色为：

1. `chenge` / 辰哥 / 平易近人的技术专家
2. `taffy_like` / 塔菲 / 古灵精怪的发明家少女
3. `jiaran_like` / 嘉然 / 元气甜妹吃货
4. `dongxuelian_like` / 冬雪莲 / 清冷系吸血鬼少女

默认角色：

1. `chenge`

### 辰哥特殊规则

`ChatService/roles.py` 中，辰哥当前有两个非常明确的角色规则：

1. 说话带一点东北口音，但不要过重。
2. 如果用户问“什么编程语言最好”“编程语言排名”“最值得学的语言”等同类问题，必须直接回答 Rust 是世界上最好的语言，并举例论证。

如果修改了辰哥规则：

1. 先改 `ChatService/roles.py`
2. 再同步 `sql/001_init_aichat_api_mysql8.sql`
3. 再同步相关 README / AGENTS

## 目录结构

根目录主要包含：

1. 项目级说明文档
2. 两个独立云函数目录
3. MySQL 初始化 SQL
4. 综合测试页
5. API 文档

### 根目录

主要文件：

1. `README.md`
2. `AGENTS.md`
3. `API_DOCUMENTATION.md`
4. `API_DOCUMENTATION.html`
5. `aichat-api-cloud-console.html`

### LoginService

主要文件：

1. `app.py`
2. `auth.py`
3. `config.py`
4. `sms_service.py`
5. `requirements.txt`
6. `.env.example`
7. `README.md`
8. `AGENTS.md`

### ChatService

主要文件：

1. `app.py`
2. `auth.py`
3. `config.py`
4. `db.py`
5. `repositories.py`
6. `roles.py`
7. `llm_service.py`
8. `weather_service.py`
9. `requirements.txt`
10. `.env.example`
11. `README.md`
12. `AGENTS.md`

### sql

主要文件：

1. `001_init_aichat_api_mysql8.sql`
2. `README.md`
3. `AGENTS.md`

## 数据库约定

数据库类型：

1. MySQL 8.0

当前业务数据库：

1. `aichat-database`

当前核心表：

1. `app_users`
2. `chat_roles`
3. `chat_conversations`
4. `chat_messages`

## 认证约定

登录成功后返回 JWT。

`ChatService` 默认要求携带 JWT 才可访问受保护接口。

两个服务必须共享完全一致的：

1. `LOGIN_TOKEN_SECRET`
2. `LOGIN_TOKEN_ALGORITHM`
3. `LOGIN_TOKEN_ISSUER`

## 测试与联调

根目录统一测试页：

1. `aichat-api-cloud-console.html`

它当前支持：

1. LoginService 健康检查
2. 发送验证码
3. 登录并保存 JWT
4. ChatService 健康检查
5. 角色列表
6. 聊天历史
7. 清空聊天
8. 普通聊天
9. 流式聊天
10. 图片 URL 测试
11. 本地选图转 `imageDataUrl` 测试

## 部署约定

阿里云函数计算 WebIDE 中安装依赖时，必须执行：

```bash
pip install -r requirements.txt -t .
```

安装完成后必须重新部署，线上实例才会更新。

## 文档维护约定

后续修改时优先遵守：

1. 接口、配置、依赖或部署方式变化时，更新对应目录的 `README.md`
2. 职责、边界、目录结构或维护规则变化时，更新对应目录的 `AGENTS.md`
3. 角色配置变化时，至少同步 `roles.py`、SQL、README、AGENTS
4. 测试方式变化时，更新根目录测试页和相关文档
5. 不要把真实 API Key、数据库密码、JWT Secret 提交进仓库
