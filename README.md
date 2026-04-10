# 灵伴-AI聊天

`灵伴-AI聊天` 是一个 AI 陪伴聊天应用仓库，当前以 Android 客户端和 Python 后端 API 为主。

## APK 下载

当前可直接下载的已签名 Android 安装包：

- [Download APK](https://github.com/VectorPeak/lingban-ai-chat/raw/main/lingban-ai-chat-v1.0-release-signed.apk)

## 仓库结构

```text
AIchat_app/
├─ AIchat-android/
├─ AIchat-API/
└─ AGENTS.md
```

## 模块说明

### `AIchat-android`

Android 客户端，基于：

1. Kotlin
2. Jetpack Compose
3. Material 3
4. Coil
5. Retrofit + OkHttp
6. ViewModel + StateFlow

当前已包含：

1. Entry
2. Onboarding
3. Login
4. Home
5. Chat
6. Splash 启动过渡
7. 会话持久化
8. 流式聊天

### `AIchat-API`

后端服务目录，包含：

1. `LoginService`
2. `ChatService`
3. `sql`

用于提供短信登录、JWT、角色列表、聊天、流式回复、聊天历史和天气问答等能力。

## 角色设定

`灵伴-AI聊天` 不只是一个功能型 AI 工具，更希望做成一款有陪伴感、有性格差异的聊天产品。

当前可选的 4 个角色如下：

1. `辰哥`
   平易近人的技术专家，适合在你想认真聊技术、生活思考或者需要稳定回应时出现。
   他不是冷冰冰地给答案，而是更像一个愿意耐心陪你把问题聊透的人。

2. `塔菲`
   古灵精怪的发明家少女，整体气质更轻快、跳脱，也更有一点脑洞感。
   适合想要轻松聊天、听一些有趣表达，或者想让对话更有活力的时候。

3. `嘉然`
   元气甜妹吃货，氛围更柔和、更热闹，也更偏日常陪伴。
   她适合接住情绪、分享碎碎念，让聊天更像朋友之间自然的来回。

4. `冬雪莲`
   清冷系吸血鬼少女，气质克制、安静，但并不疏离。
   她更适合夜晚氛围、偏情绪化表达，或者想要一种更特别、更有距离美感的陪伴体验。

## 设计与接口

1. Figma 设计稿：
   [AI 陪伴聊天 app 设计](https://www.figma.com/make/4CHusQ3BWl19YpAZQrR3XK/AI%E9%99%AA%E4%BC%B4%E8%81%8A%E5%A4%A9app%E8%AE%BE%E8%AE%A1?t=z7bBBJZnqFW1H76l-1&preview-route=%2Fhome)
2. 仓库内 API 文档：
   [AIchat-API/API_DOCUMENTATION.md](./AIchat-API/API_DOCUMENTATION.md)
3. 在线 API 文档：
   [AI Chat API Wiki](https://xunmengwinter.github.io/ai-chat-api-wiki/)

## 本地运行

### Android

在 `AIchat-android` 目录中运行：

```powershell
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:testDebugUnitTest
```

推荐环境：

1. Android Studio 最新稳定版
2. JDK 21
3. Pixel 9 模拟器

### API

`AIchat-API` 下的两个服务均为 Python 项目，可分别按各自目录中的 `README.md` 运行。

## 说明

1. 本仓库为公开仓库版本，已排除本机缓存、构建产物、子仓库 `.git` 目录和本地配置文件。
2. 后续若仓库结构变化，应同步更新根目录 `AGENTS.md` 和相关模块文档。
