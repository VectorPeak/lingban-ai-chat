# AGENTS.md

## 适用范围

本文件适用于项目根目录 `AIchat_app`。
处理该目录内任务时，应先识别真实存在的工程，再决定工作落点。

## 当前目录检测结果

当前根目录主要内容：

1. `AIchat-android`
2. `AIchat-API`
3. `AGENTS.md`
4. `README.md`

## 默认工作落点

1. 未明确指定平台时，默认在 `AIchat-android` 实现需求。
2. 涉及接口、角色、聊天流程、登录流程时，优先参考 `AIchat-API` 文档与服务代码。
3. 根目录是一个公开单仓库根目录，Android 代码应落在 `AIchat-android`，后端相关调整才进入 `AIchat-API`。

## Android 工程现状

`AIchat-android` 是当前主要客户端工程，已不是空白模板，而是已经进入业务开发中的 Compose 项目。

已确认现状：

1. 使用 Kotlin + Jetpack Compose。
2. 当前只有单个 `:app` 模块。
3. `namespace` 和 `applicationId` 为 `cn.vectorpeak.AIchat_projects`。
4. `minSdk = 26`，`targetSdk = 36`，`compileSdk = 36`。
5. `compileOptions` 使用 Java 17，Kotlin `jvmToolchain(21)`。
6. 已配置 `AIchatApplication`、`MainActivity`、Navigation、ViewModel、DataStore、Repository、Remote、Model、Theme 等结构。
7. `AndroidManifest.xml` 已声明 `INTERNET` 权限。
8. 已接入 Coil、Retrofit、OkHttp、Kotlinx Serialization、Navigation Compose、ViewModel Compose、DataStore、SplashScreen 等依赖。

## Android 目录理解

当前 Android 端主要结构如下：

1. `data`
2. `data/local`
3. `data/model`
4. `data/remote`
5. `data/repository`
6. `ui/component`
7. `ui/navigation`
8. `ui/screen/*`
9. `ui/theme`
10. `viewmodel`

处理 Android 任务时，默认应遵循现有分层，不要把新逻辑重新塞回 `MainActivity`。

## Android 开发约束

1. 使用 Kotlin + Compose 开发。
2. 遵循 Material 3 设计规范。
3. 状态管理优先使用 `ViewModel + StateFlow / MutableStateFlow`。
4. 网络图片加载统一使用 Coil。
5. 网络请求、序列化、存储、导航等能力，优先复用当前已存在依赖和目录结构。
6. 保持代码结构清晰，便于后续继续模块化。
7. 使用 Android Studio 最新稳定版编译运行，并以 Pixel 9 模拟器为主要适配验证设备。

## Android 实施规则

1. 新页面优先放在 `ui/screen` 下按功能分目录组织。
2. 可复用 UI 组件优先放在 `ui/component`。
3. 接口定义、解析、错误处理放在 `data/remote`。
4. 业务数据拼装和调用编排优先放在 `data/repository`。
5. 持久化状态和会话相关逻辑优先放在 `data/local`。
6. 页面状态和交互调度优先放在 `viewmodel`。
7. 简单局部 UI 状态可以使用 `remember` / `rememberSaveable`，但不要用它替代页面级业务状态。
8. 如需新增依赖，优先维护 `AIchat-android/gradle/libs.versions.toml` 和对应 `build.gradle.kts`。

## 不应编辑的目录

除非任务明确要求，否则不要把时间花在以下目录或产物上：

1. `AIchat-android/.gradle`
2. `AIchat-android/app/build`
3. 其他构建生成目录、缓存目录、IDE 临时文件

## 后端参考规则

`AIchat-API` 是当前 Android 端联调和业务语义的主要参考来源。

优先参考：

1. 线上 API 文档：`https://xunmengwinter.github.io/ai-chat-api-wiki/`
2. 本地文档：`AIchat-API/API_DOCUMENTATION.md`
3. 服务代码：`AIchat-API/LoginService`、`AIchat-API/ChatService`
4. 后端子目录内已有的 `AGENTS.md`

如果 Android 端的字段、流程、角色设定与直觉不一致，应优先以后端文档和服务实现为准，而不是自行猜测。

## 设计还原要求

1. 页面还原以提供的 Figma 设计稿为依据。
2. 设计稿未覆盖的细节，优先与现有 Android 视觉语言保持一致。
3. 如设计稿与 Android 平台能力存在冲突，优先保证信息层级、核心体验和视觉一致性。

## 文档维护要求

如果后续仓库结构再次变化，例如新增模块、重构 Android 包结构或替换主要依赖，应同步更新本文件，避免说明与实际工程状态脱节。
