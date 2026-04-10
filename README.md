# 灵伴-AI聊天

`灵伴-AI聊天` 是一个 AI 陪伴聊天应用仓库，当前以 Android 客户端和 Python 后端 API 为主。

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
