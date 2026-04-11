# AIchat-API 前后端联调 API 文档

更新时间：2026-04-11

本文档面向前端联调，内容以当前仓库代码和线上云函数实际返回为准。

## 1. 基础信息

### 1.1 服务地址

| 服务 | 基础地址 | 说明 |
| --- | --- | --- |
| `LoginService` | `https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run` | 发送验证码、手机号验证码登录、签发 JWT |
| `ChatService` | `https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run` | 角色列表、聊天、流式聊天、历史记录、清空当前聊天 |

### 1.2 当前线上联调状态

基于 2026-04-11 的最新联调结果：

1. `LoginService GET /health` 返回 `200`，`configured=true`。
2. `ChatService GET /health` 返回 `200`，`configured=true`。
3. `ChatService` 当前要求 JWT 鉴权：`authRequired=true`。
4. `ChatService GET /api/chat/roles` 返回 4 个有效角色。
5. `ChatService` 当前数据库和天气能力均已联通：`databaseReachable=true`、`weatherConfigured=true`。
6. 本地联通验证已确认 `LoginService` 测试账号模式可签发 JWT。
7. 本地联通验证已确认 `ChatService /api/chat/history`、`/api/chat/clear` 可访问 MySQL。
8. 本地联通验证已确认在真实 `DASHSCOPE_API_KEY` 下，`ChatService /api/chat/completions` 可返回模型回复。
9. 线上联通验证已确认 `LoginService /api/auth/send-code`、`/api/auth/login`、`ChatService /api/chat/history`、`/api/chat/clear`、`/api/chat/completions` 全链路可用。

### 1.3 当前已验证可用的 MySQL 业务配置

```text
MYSQL_HOST=118.25.150.154
MYSQL_PORT=3306
MYSQL_USER=AIchat_app_SQL_admin
MYSQL_PASSWORD=AIchat123456
MYSQL_DATABASE=aichat-database
```

## 2. 鉴权约定

### 2.1 JWT 传递方式

推荐只使用请求头传递 JWT：

```http
Authorization: Bearer <token>
```

`ChatService` 也兼容在 Body 中传 `token`，但前端联调时仍建议统一使用请求头。

### 2.2 不需要 JWT 的接口

1. `LoginService` 的全部接口
2. `ChatService GET /`
3. `ChatService GET /health`
4. `ChatService GET /api/chat/roles`

### 2.3 需要 JWT 的接口

1. `GET /api/chat/history`
2. `POST /api/chat/clear`
3. `POST /api/chat/completions`
4. `POST /api/chat/stream`

### 2.4 JWT 来源

JWT 由 `LoginService POST /api/auth/login` 返回，取值位置：

```json
{
  "data": {
    "token": "..."
  }
}
```

## 3. 角色列表

当前线上固定返回 4 个预设角色，前端应始终以 `GET /api/chat/roles` 返回结果为准，不要在前端硬编码角色素材地址。

| roleKey | nickname | archetype |
| --- | --- | --- |
| `chenge` | 辰哥 | 平易近人的技术专家 |
| `taffy_like` | 塔菲 | 古灵精怪的发明家少女 |
| `jiaran_like` | 嘉然 | 元气甜妹吃货 |
| `dongxuelian_like` | 冬雪莲 | 清冷系吸血鬼少女 |

### 3.1 `GET /api/chat/roles`

- 方法：`GET`
- 鉴权：不需要 JWT
- 作用：获取角色列表、头像、背景图、简介、开场白

成功返回示例：

```json
{
  "success": true,
  "data": [
    {
      "roleKey": "chenge",
      "nickname": "辰哥",
      "archetype": "平易近人的技术专家",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_chenge.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_chenge.png",
      "personaSummary": "一位平易近人、擅长把复杂技术问题讲明白的技术专家型老师。",
      "openingMessage": "我是辰哥。技术问题不用急着堆术语，你把现象、报错、代码或者思路告诉我，我会陪你一步一步拆开来看，尽量讲清原理，也帮你找到可落地的解法。"
    },
    {
      "roleKey": "taffy_like",
      "nickname": "塔菲",
      "archetype": "古灵精怪的发明家少女",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_tafei.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_tafei.png",
      "personaSummary": "一位很有节目效果、带点嘴硬和傲气、偶尔会喵一下的古灵精怪陪伴角色。",
      "openingMessage": "喂，你来啦？塔菲今天心情还不错，就勉强接待你一下……有话快说，别磨磨蹭蹭的，笨蛋喵。"
    },
    {
      "roleKey": "jiaran_like",
      "nickname": "嘉然",
      "archetype": "元气甜妹吃货",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_jiaran.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_jiaran.png",
      "personaSummary": "一位元气满满、甜甜软软、很会撒娇也很会接话的吃货系陪伴角色。",
      "openingMessage": "这里是嘉然！别看嘉然小小的，嘉然可是超能吃、超可爱、也超会陪你聊天的哦！所以今天想和嘉然聊什么呀？"
    },
    {
      "roleKey": "dongxuelian_like",
      "nickname": "冬雪莲",
      "archetype": "清冷系吸血鬼少女",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_dongxuelian.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_dongxuelian.png",
      "personaSummary": "一位表面清冷文静、实际细腻又有点反差萌的吸血鬼少女型陪伴角色。",
      "openingMessage": "这里是冬雪莲。你可以慢慢说，不必着急。塔菲……不是，冬雪莲会认真听。嗯，前提是不要突然端来番茄。"
    }
  ]
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `roleKey` | `string` | 角色唯一标识，前端应以此作为角色 ID |
| `nickname` | `string` | 角色昵称 |
| `archetype` | `string` | 角色类型简介 |
| `avatarUrl` | `string` | 头像 URL |
| `backgroundUrl` | `string` | 聊天背景图 URL |
| `personaSummary` | `string` | 角色简介 |
| `openingMessage` | `string` | 开场白 |

前端建议：

1. 角色列表、头像、背景图、简介全部从接口读，不要写死。
2. 角色切换后，后续历史记录、清空聊天、聊天请求都使用同一个 `roleKey`。

## 4. LoginService

### 4.1 `GET /`

- 方法：`GET`
- 鉴权：不需要
- 作用：基础探活，返回服务名称和可用接口

返回示例：

```json
{
  "service": "LoginService",
  "message": "手机号验证码登录云函数已就绪。",
  "tokenFormat": "JWT",
  "endpoints": {
    "health": "GET /health",
    "sendCode": "POST /api/auth/send-code",
    "login": "POST /api/auth/login"
  }
}
```

### 4.2 `GET /health`

- 方法：`GET`
- 鉴权：不需要
- 作用：检查环境变量是否配置完整

返回示例：

```json
{
  "success": true,
  "service": "LoginService",
  "configured": true,
  "missingSettings": [],
  "tokenFormat": "JWT"
}
```

### 4.3 `POST /api/auth/send-code`

- 方法：`POST`
- 鉴权：不需要
- 作用：发送短信验证码

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 手机号，只允许 6 到 15 位数字 |
| `countryCode` | `string` | 否 | 国家码，默认 `86` |
| `outId` | `string` | 否 | 业务侧请求 ID，便于链路追踪 |

请求示例：

```json
{
  "phoneNumber": "15209500318",
  "countryCode": "86",
  "outId": "login-20260409-0001"
}
```

成功返回示例：

```json
{
  "success": true,
  "message": "验证码发送成功。",
  "data": {
    "requestId": "12bcc716-4615-464d-81fd-59eb9c2e5dbf",
    "bizId": "778608475566212268^0",
    "outId": "login-20260409-0001",
    "validTimeSeconds": 300,
    "codeLength": 6
  }
}
```

失败示例，非法手机号：

```json
{
  "success": false,
  "message": "phoneNumber 格式不正确，必须是 6 到 15 位数字。"
}
```

### 4.4 `POST /api/auth/login`

- 方法：`POST`
- 鉴权：不需要
- 作用：使用手机号 + 验证码登录，并返回 JWT

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 手机号，只允许 6 到 15 位数字 |
| `verifyCode` | `string` | 是 | 短信验证码 |
| `countryCode` | `string` | 否 | 国家码，默认 `86` |
| `outId` | `string` | 否 | 业务侧请求 ID |

请求示例：

```json
{
  "phoneNumber": "15209500318",
  "countryCode": "86",
  "verifyCode": "123456"
}
```

成功返回示例：

```json
{
  "success": true,
  "message": "登录成功。",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "tokenFormat": "JWT",
    "expiresIn": 2592000,
    "expiresAt": "2026-05-09T01:23:45+00:00",
    "user": {
      "phoneNumber": "15209500318",
      "countryCode": "86"
    }
  }
}
```

说明：

1. 前端后续请求 `ChatService` 时，使用 `data.token` 作为 Bearer Token。
2. 当前默认 JWT 有效期为 `2592000` 秒，即 30 天。
3. 验证码校验失败时，接口会返回 `401`。

## 5. ChatService

### 5.1 `GET /`

- 方法：`GET`
- 鉴权：不需要
- 作用：基础探活，返回服务名称和接口清单

返回示例：

```json
{
  "service": "ChatService",
  "message": "AI 聊天云函数已就绪。",
  "endpoints": {
    "health": "GET /health",
    "roles": "GET /api/chat/roles",
    "chat": "POST /api/chat/completions",
    "stream": "POST /api/chat/stream",
    "history": "GET /api/chat/history",
    "clear": "POST /api/chat/clear"
  }
}
```

### 5.2 `GET /health`

- 方法：`GET`
- 鉴权：不需要
- 作用：检查模型、数据库、天气配置和鉴权状态

返回示例：

```json
{
  "success": true,
  "service": "ChatService",
  "configured": true,
  "missingSettings": [],
  "missingOptionalSettings": [],
  "authRequired": true,
  "databaseConfigured": true,
  "databaseReachable": true,
  "weatherConfigured": true,
  "model": "qwen-plus",
  "models": {
    "text": "qwen-plus",
    "vision": "qwen-vl-plus-latest"
  }
}
```

### 5.3 `GET /api/chat/history`

- 方法：`GET`
- 鉴权：需要 JWT
- 作用：读取当前用户在某个角色下的当前活跃会话历史

查询参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `roleKey` | `string` | 否 | 角色 Key，默认 `chenge` |
| `limit` | `number` | 否 | 返回消息条数，默认 100，最大 200 |

请求示例：

```text
GET /api/chat/history?roleKey=chenge&limit=50
```

成功返回示例：

```json
{
  "success": true,
  "data": {
    "role": {
      "roleKey": "chenge",
      "nickname": "辰哥",
      "archetype": "平易近人的技术专家",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_chenge.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_chenge.png",
      "personaSummary": "一位平易近人、擅长把复杂技术问题讲明白的技术专家型老师。",
      "openingMessage": "我是辰哥。技术问题不用急着堆术语，你把现象、报错、代码或者思路告诉我，我会陪你一步一步拆开来看，尽量讲清原理，也帮你找到可落地的解法。"
    },
    "conversationId": 12,
    "messages": [
      {
        "id": 101,
        "role": "user",
        "content": "帮我解释一下 Python 装饰器。",
        "hasImage": false,
        "model": null,
        "createdAt": "2026-04-09T01:23:45+00:00"
      },
      {
        "id": 102,
        "role": "assistant",
        "content": "可以，先把它理解成“给函数包一层额外逻辑”。",
        "hasImage": false,
        "model": "qwen-plus",
        "createdAt": "2026-04-09T01:23:46+00:00"
      }
    ]
  }
}
```

说明：

1. `messages[].role` 只会返回 `user` 或 `assistant`。
2. `conversationId` 在没有历史时可能为 `null`。

### 5.4 `POST /api/chat/clear`

- 方法：`POST`
- 鉴权：需要 JWT
- 作用：清空当前用户在指定角色下的当前活跃会话

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `roleKey` | `string` | 否 | 角色 Key，默认 `chenge` |

请求示例：

```json
{
  "roleKey": "chenge"
}
```

成功返回示例：

```json
{
  "success": true,
  "message": "当前聊天已清空。",
  "data": {
    "role": {
      "roleKey": "chenge",
      "nickname": "辰哥",
      "archetype": "平易近人的技术专家",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_chenge.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_chenge.png",
      "personaSummary": "一位平易近人、擅长把复杂技术问题讲明白的技术专家型老师。",
      "openingMessage": "我是辰哥。技术问题不用急着堆术语，你把现象、报错、代码或者思路告诉我，我会陪你一步一步拆开来看，尽量讲清原理，也帮你找到可落地的解法。"
    },
    "clearedConversationCount": 1
  }
}
```

### 5.5 `POST /api/chat/completions`

- 方法：`POST`
- 鉴权：需要 JWT
- 作用：普通聊天接口，返回完整 JSON 结果

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `roleKey` | `string` | 否 | 角色 Key，默认 `chenge` |
| `message` | `string` | 否 | 当前用户输入文本，优先级最高 |
| `messages` | `array` | 否 | 可选的消息数组；服务端会提取最后一个 `role=user` 的文本内容 |
| `model` | `string` | 否 | 指定模型；纯文本默认 `qwen-plus`，图片默认 `qwen-vl-plus-latest` |
| `temperature` | `number` | 否 | 温度参数 |
| `topP` | `number` | 否 | Top P |
| `maxTokens` | `number` | 否 | 最大输出 Token 数 |
| `presencePenalty` | `number` | 否 | presence penalty |
| `frequencyPenalty` | `number` | 否 | frequency penalty |
| `weatherCity` | `string` | 否 | 显式指定天气查询城市；传入后会强制先查询该城市实时天气 |
| `imageUrl` | `string` | 否 | 图片 URL，只支持单张 |
| `imageDataUrl` | `string` | 否 | Base64 Data URL，只支持单张，优先级高于 `imageUrl` |
| `token` | `string` | 否 | Body 中传 JWT 的兼容字段，不推荐 |
| `stream` | `boolean` | 否 | 若为 `true`，行为等价于走流式接口 |

请求规则：

1. `message` 有值时，优先使用 `message`。
2. `message` 为空时，服务端会尝试从 `messages` 中提取最后一个用户文本。
3. `message` 和 `messages` 都为空时，只要存在 `imageUrl` 或 `imageDataUrl` 仍可发起请求。
4. 当前只支持单张图片，不支持多图。
5. 图片不做持久化，不保存到数据库。
6. 如果显式传 `weatherCity`，服务会强制查询该城市实时天气。
7. 如果不传 `weatherCity`，只有当用户问题明显是在问天气时，服务才会自动走天气工具链。

基础请求示例：

```json
{
  "roleKey": "chenge",
  "message": "帮我解释一下 Python 装饰器。",
  "model": "qwen-plus",
  "temperature": 0.7
}
```

带天气请求示例：

```json
{
  "roleKey": "taffy_like",
  "message": "杭州现在适合出门吗？",
  "weatherCity": "杭州"
}
```

带图片请求示例：

```json
{
  "roleKey": "dongxuelian_like",
  "message": "帮我看一下这张图里的内容。",
  "imageDataUrl": "data:image/jpeg;base64,/9j/4AAQSk..."
}
```

成功返回示例：

```json
{
  "success": true,
  "data": {
    "id": "chatcmpl-xxx",
    "object": "chat.completion",
    "model": "qwen-plus",
    "choices": [
      {
        "index": 0,
        "message": {
          "role": "assistant",
          "content": "可以。先把装饰器理解成“给函数再包一层逻辑”的语法糖。"
        },
        "finish_reason": "stop"
      }
    ],
    "usage": {
      "prompt_tokens": 120,
      "completion_tokens": 28,
      "total_tokens": 148
    }
  },
  "meta": {
    "role": {
      "roleKey": "chenge",
      "nickname": "辰哥",
      "archetype": "平易近人的技术专家",
      "avatarUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_avatar_chenge.png",
      "backgroundUrl": "https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_image/chat_bg_chenge.png",
      "personaSummary": "一位平易近人、擅长把复杂技术问题讲明白的技术专家型老师。",
      "openingMessage": "我是辰哥。技术问题不用急着堆术语，你把现象、报错、代码或者思路告诉我，我会陪你一步一步拆开来看，尽量讲清原理，也帮你找到可落地的解法。"
    },
    "conversationId": 12,
    "assistantMessage": "可以。先把装饰器理解成“给函数再包一层逻辑”的语法糖。",
    "weather": null,
    "usedToolCall": false
  }
}
```

`meta` 字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `role` | `object` | 本次使用的角色公开信息 |
| `conversationId` | `number` | 当前活跃会话 ID |
| `assistantMessage` | `string` | 助手最终文本 |
| `weather` | `object \| null` | 若使用了天气能力，会返回天气上下文 |
| `usedToolCall` | `boolean` | 是否发生了工具调用 |

`meta.weather` 示例：

```json
{
  "queryCity": "杭州",
  "locationId": "101210101",
  "cityName": "杭州",
  "adm1": "浙江省",
  "adm2": "杭州",
  "country": "中国",
  "tz": "+08:00",
  "obsTime": "2026-04-09T09:00+08:00",
  "temp": "21",
  "feelsLike": "16",
  "text": "阴",
  "windDir": "东南风",
  "windScale": "1",
  "windSpeed": "5",
  "humidity": "21",
  "precip": "0.0",
  "pressure": "1012",
  "vis": "25",
  "summary": "杭州当前天气阴，气温21℃，体感16℃，湿度21%，风向东南风，风力1级。"
}
```

### 5.6 `POST /api/chat/stream`

- 方法：`POST`
- 鉴权：需要 JWT
- 作用：流式聊天接口，返回 `text/event-stream`
- 请求参数：与 `POST /api/chat/completions` 完全一致

返回格式说明：

1. 每一帧都以 `data: ` 开头。
2. 正常帧是 JSON。
3. 结束帧固定为 `data: [DONE]`。
4. 前端主要读取 `choices[0].delta.content`。
5. 如果走了天气工具链，服务可能返回“合成流式事件”，但解析方式不变。

流式返回示例：

```text
data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","choices":[{"delta":{"role":"assistant","content":""},"index":0,"finish_reason":null}],"usage":null}

data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","choices":[{"delta":{"content":"可以"},"index":0,"finish_reason":null}],"usage":null}

data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","choices":[{"delta":{"content":"，先把它理解成语法糖。"},"index":0,"finish_reason":null}],"usage":null}

data: {"id":"chatcmpl-xxx","object":"chat.completion.chunk","choices":[],"usage":{"prompt_tokens":120,"completion_tokens":28,"total_tokens":148}}

data: [DONE]
```

## 6. 错误码和返回规范

### 6.1 通用错误结构

```json
{
  "success": false,
  "message": "错误说明"
}
```

### 6.2 常见状态码

| 状态码 | 场景 | 说明 |
| --- | --- | --- |
| `400` | 参数错误 | 例如手机号格式不正确、缺少必要参数 |
| `401` | 未登录或登录态无效 | 例如缺少 JWT、伪造 JWT、验证码校验失败 |
| `500` | 服务内部错误或必填环境变量缺失 | 例如数据库未配置、内部异常 |
| `502` | 上游服务失败 | 例如 Qwen 或天气服务调用失败 |
| `504` | 模型请求超时 | 例如大模型请求超出服务端超时阈值 |

实际错误示例：

非法手机号：

```json
{
  "success": false,
  "message": "phoneNumber 格式不正确，必须是 6 到 15 位数字。"
}
```

缺少 JWT：

```json
{
  "success": false,
  "message": "缺少登录凭证。"
}
```

无效 JWT：

```json
{
  "success": false,
  "message": "登录凭证无效。"
}
```

环境变量缺失时，可能还会返回：

```json
{
  "success": false,
  "message": "服务缺少必要环境变量配置。",
  "missingSettings": [
    "LOGIN_TOKEN_SECRET"
  ]
}
```

## 7. 前端接入示例

### 7.1 登录并获取 JWT

```js
const loginRes = await fetch('https://aichat-login-itosyagchs.cn-hangzhou.fcapp.run/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    phoneNumber: '15209500318',
    countryCode: '86',
    verifyCode: '123456'
  })
});

const loginJson = await loginRes.json();
const token = loginJson.data.token;
```

### 7.2 加载角色列表

```js
const rolesRes = await fetch('https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run/api/chat/roles');
const rolesJson = await rolesRes.json();
const roles = rolesJson.data;
```

### 7.3 普通聊天

```js
const res = await fetch('https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run/api/chat/completions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`
  },
  body: JSON.stringify({
    roleKey: 'chenge',
    message: '帮我解释一下 Python 装饰器。'
  })
});

const json = await res.json();
console.log(json.meta.assistantMessage);
```

### 7.4 带天气的聊天

```js
const res = await fetch('https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run/api/chat/completions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`
  },
  body: JSON.stringify({
    roleKey: 'taffy_like',
    message: '杭州现在适合出门吗？',
    weatherCity: '杭州'
  })
});

const json = await res.json();
console.log(json.meta.weather);
console.log(json.meta.assistantMessage);
```

### 7.5 流式聊天

```js
const res = await fetch('https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run/api/chat/stream', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`
  },
  body: JSON.stringify({
    roleKey: 'dongxuelian_like',
    message: '陪我聊聊天吧。'
  })
});

const reader = res.body.getReader();
const decoder = new TextDecoder();
let buffer = '';

while (true) {
  const { value, done } = await reader.read();
  if (done) break;

  buffer += decoder.decode(value, { stream: true });
  const frames = buffer.split('\n\n');
  buffer = frames.pop() || '';

  for (const frame of frames) {
    if (!frame.startsWith('data: ')) continue;
    const raw = frame.slice(6);
    if (raw === '[DONE]') continue;

    const event = JSON.parse(raw);
    const delta = event.choices?.[0]?.delta?.content || '';
    if (delta) {
      console.log(delta);
    }
  }
}
```

## 8. 前端对接建议

1. 角色相关素材全部走 `GET /api/chat/roles`，不要在前端保存静态副本。
2. 所有受保护接口统一走 `Authorization: Bearer <token>`。
3. 收到 `401` 时，前端应清除本地 JWT 并跳转登录。
4. 需要天气时，前端最好显式传 `weatherCity`，不要完全依赖模型自己判断。
5. 图片聊天只支持单图，建议前端上传前压缩，避免请求体过大。
6. 对 `502` 和 `504` 做重试提示，不要直接把上游错误原样暴露给用户。
