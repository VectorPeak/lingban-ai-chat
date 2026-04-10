# AGENTS.md

## 目录定位

`LoginService` 是 AIchat-API 的登录云函数目录。

默认入口文件：

1. `app.py`

公网地址：
`https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run`

## 职责范围

`LoginService` 负责：

1. 获取短信验证码
2. 校验短信验证码
3. 手机号验证码登录
4. 登录成功后签发 JWT

`LoginService` 不负责：

1. 聊天
2. LLM 调用
3. MySQL 读写
4. 天气查询
5. 角色管理

## 关键文件

1. `app.py`
   Flask 路由入口、健康检查、发送验证码、登录接口。
2. `auth.py`
   JWT 生成逻辑。
3. `config.py`
   环境变量读取与测试账号开关配置。
4. `sms_service.py`
   阿里云号码认证服务封装。
5. `requirements.txt`
   依赖列表。
6. `.env.example`
   环境变量示例。
7. `README.md`
   使用说明。
8. `AGENTS.md`
   维护约定。

## 测试账号约定

当前目录支持一个仅用于调试的隐藏测试账号逻辑。

规则：

1. 默认关闭，只有 `ENABLE_TEST_LOGIN_ACCOUNT=true` 时才启用。
2. 测试账号逻辑不能出现在任何前端测试页面中。
3. 开启后，测试账号可以跳过真实短信服务，直接返回本地模拟验证码结果并签发 JWT。
4. 即使开启测试账号，`LOGIN_TOKEN_SECRET` 仍然必须正确配置。
5. 如果修改测试账号机制，要同步更新 `.env.example` 和 `README.md`。

## 维护要求

后续修改本目录时：

1. 如果修改 JWT 逻辑，必须确认 `ChatService` 的校验逻辑仍然兼容。
2. 如果修改测试账号逻辑，不能把测试手机号或验证码写进前端测试页。
3. 如果修改接口、配置或部署方式，更新 `README.md`。
4. 如果修改目录职责或维护规则，更新本文件。
