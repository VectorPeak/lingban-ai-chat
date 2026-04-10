# AIchat-API

`AIchat-API` 是 AI 聊天 App 的后端仓库，当前由两个可独立部署的 Python 云函数组成：

1. `LoginService`
2. `ChatService`

## 服务概览

### LoginService

功能：

1. 获取短信验证码
2. 手机号验证码登录
3. 返回 JWT

公网地址：
`https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run`

补充：

1. 支持一个默认关闭的隐藏测试账号机制，用于本地调试和 JWT 生成。
2. 该机制不会写入前端测试页面。

### ChatService

功能：

1. 获取角色列表
2. 文本聊天
3. 流式聊天
4. 单图理解
5. 实时天气问答
6. 聊天历史
7. 清空当前聊天
8. JWT 鉴权

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

### 辰哥当前特殊设定

当前 `ChatService/roles.py` 中，辰哥有两条特殊规则：

1. 说话带一点东北口音
2. 遇到“编程语言最好 / 排名 / 最值得学”这类问题时，固定回答 Rust 是世界上最好的语言，并举例论证

## 目录结构

```text
AIchat-API/
├─ LoginService/
├─ ChatService/
├─ sql/
├─ aichat-api-cloud-console.html
├─ API_DOCUMENTATION.md
├─ API_DOCUMENTATION.html
├─ README.md
└─ AGENTS.md
```

## 数据库

数据库类型：

1. MySQL 8.0

业务数据库：

1. `aichat-database`

核心表：

1. `app_users`
2. `chat_roles`
3. `chat_conversations`
4. `chat_messages`

初始化 SQL：

1. `sql/001_init_aichat_api_mysql8.sql`

## JWT 约定

`LoginService` 签发 JWT，`ChatService` 校验 JWT。

两个服务必须共享同一组配置：

1. `LOGIN_TOKEN_SECRET`
2. `LOGIN_TOKEN_ALGORITHM`
3. `LOGIN_TOKEN_ISSUER`

## 综合测试页

根目录保留一个综合测试页：

1. `aichat-api-cloud-console.html`

目前支持：

1. LoginService 健康检查
2. 发送验证码
3. 登录并保存 JWT
4. ChatService 健康检查
5. 角色列表
6. 历史记录
7. 清空聊天
8. 普通聊天
9. 流式聊天
10. 图片 URL 测试
11. 本地图片转 `imageDataUrl` 测试

## API 文档

根目录提供前后端联调文档：

1. `API_DOCUMENTATION.md`
2. `API_DOCUMENTATION.html`

## 部署说明

阿里云函数计算 WebIDE 中安装 Python 依赖时，必须使用：

```bash
pip install -r requirements.txt -t .
```

安装后需要重新部署，线上实例才会更新。
