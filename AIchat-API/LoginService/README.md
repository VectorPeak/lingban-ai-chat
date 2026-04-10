# LoginService

## 简介

`LoginService` 是 AIchat App 的登录云函数，负责：

1. 获取短信验证码
2. 校验短信验证码
3. 手机号验证码登录
4. 登录成功后签发 JWT

默认入口文件：

1. `app.py`

公网地址：
`https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run`

## 目录说明

1. `app.py`
   Flask 入口、健康检查、发送验证码、登录接口。
2. `auth.py`
   JWT 生成逻辑。
3. `config.py`
   环境变量读取、短信配置、测试账号开关配置。
4. `sms_service.py`
   阿里云号码认证服务封装。
5. `requirements.txt`
   依赖列表。
6. `.env.example`
   环境变量示例。

## 接口

1. `GET /`
2. `GET /health`
3. `POST /api/auth/send-code`
4. `POST /api/auth/login`

## 关键环境变量

必填：

1. `ALIYUN_ACCESS_KEY_ID`
2. `ALIYUN_ACCESS_KEY_SECRET`
3. `ALIYUN_SMS_SIGN_NAME`
4. `ALIYUN_SMS_TEMPLATE_CODE`
5. `LOGIN_TOKEN_SECRET`
6. `LOGIN_TOKEN_ALGORITHM`
7. `LOGIN_TOKEN_ISSUER`

可选：

1. `ALIYUN_SMS_REGION_ID`
2. `ALIYUN_SMS_ENDPOINT`
3. `ALIYUN_SMS_COUNTRY_CODE`
4. `ALIYUN_SMS_TEMPLATE_CODE_FIELD`
5. `ALIYUN_SMS_TEMPLATE_MIN_FIELD`
6. `ALIYUN_SMS_TEMPLATE_MIN_VALUE`
7. `ALIYUN_SMS_CODE_LENGTH`
8. `ALIYUN_SMS_VALID_TIME_SECONDS`
9. `ALIYUN_SMS_DUPLICATE_POLICY`
10. `ALIYUN_SMS_CASE_AUTH_POLICY`
11. `ALIYUN_SMS_SCHEME_NAME`
12. `LOGIN_TOKEN_EXPIRES_SECONDS`
13. `CORS_ALLOW_ORIGIN`

## 隐藏测试账号机制

当前支持一个仅用于调试的隐藏测试账号机制。

规则：

1. 默认关闭。
2. 只有显式开启 `ENABLE_TEST_LOGIN_ACCOUNT=true` 时才生效。
3. 测试账号逻辑不会写入任何前端测试页面。
4. 开启后，测试账号可以跳过真实短信服务，直接返回本地模拟验证码结果并签发 JWT。

相关环境变量：

1. `ENABLE_TEST_LOGIN_ACCOUNT`
2. `TEST_LOGIN_PHONE_NUMBER`
3. `TEST_LOGIN_VERIFY_CODE`
4. `TEST_LOGIN_COUNTRY_CODE`

说明：

1. 测试账号仍然要求 `LOGIN_TOKEN_SECRET` 正确配置。
2. 测试账号用于本地或受控环境调试，不建议在线上环境开启。

## 部署

阿里云函数计算 WebIDE 中安装依赖时，必须执行：

```bash
pip install -r requirements.txt -t .
```

安装后需要重新部署，线上实例才会更新。

## 与 ChatService 的关系

`LoginService` 返回的 JWT 需要被 `ChatService` 校验，所以两个服务必须共享：

1. `LOGIN_TOKEN_SECRET`
2. `LOGIN_TOKEN_ALGORITHM`
3. `LOGIN_TOKEN_ISSUER`
