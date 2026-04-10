# AIchat Android 项目 Agents 说明

## 1. 项目识别结果

当前目录 `AIchat-android` 是 `AIchat_app` 单仓库下的 Android 客户端模块，已经完成首版核心闭环，不再是初始模板工程。

- 根模块：`:app`
- `namespace` / `applicationId`：`cn.vectorpeak.AIchat_projects`
- 当前应用名称：`灵伴*AI聊天`
- `minSdk`：26
- `targetSdk`：36
- `compileSdk`：36
- 当前入口文件：`app/src/main/java/cn/vectorpeak/AIchat_projects/MainActivity.kt`
- 当前主题目录：`app/src/main/java/cn/vectorpeak/AIchat_projects/ui/theme/`
- 当前 Launcher Icon：`app/src/main/res/drawable/app_icon_photo.png`
- 当前状态：已完成单模块首版核心闭环，包含 `Entry / Onboarding / Login / Home / Chat` 页面、系统 Splash、DataStore 会话持久化、角色列表、聊天历史、流式文本聊天与清空会话能力

本文件用于约束后续 Agent 或开发协作者在该模块中的实现方式。后续实现必须以 Android 原生 Compose 项目为前提，不得沿用 Web、iOS 或通用伪代码式说明。

## 2. 项目目标

实现一款 Android 版本的 AI 陪伴聊天 App。

核心目标如下：

- 基于提供的 Figma 设计稿还原 UI
- 对接 AI 聊天后端 API
- 支持聊天消息流、会话展示、输入区交互、头像和插图展示
- 保持代码结构清晰，便于后续扩展为更多页面、更多业务模块或多模块工程

## 3. 技术约束

所有后续实现都必须遵守以下约束：

- 使用 `Kotlin + Jetpack Compose` 开发
- 页面实现以 Compose 为主，不回退到 XML 作为主要 UI 方案
- 遵循 `Material 3` 设计规范
- 网络图片加载统一使用 `Coil`
- 涉及状态管理时，优先使用 `ViewModel + StateFlow / MutableStateFlow`
- 使用 Android Studio 最新稳定版进行编译和运行验证
- 使用 `Pixel 9` 模拟器做界面适配与基础交互验证
- 保持代码结构清晰，便于后续模块化扩展

补充约束：

- 不要把业务逻辑直接堆在 `MainActivity` 中
- 不要把网络请求直接写进大型 Composable
- 新增依赖时优先维护 `gradle/libs.versions.toml`
- 页面状态、加载状态、错误状态、空状态都需要显式建模，不要只实现理想路径

## 4. 编码限制

以下内容根据你提供的截图进行 OCR 提取，并已按当前 Android 项目做适配。

- 不要随意引入新依赖；如确有必要，需优先写入 `gradle/libs.versions.toml` 并说明用途
- 不要修改与当前任务无关的大量代码
- 不要破坏已有可运行功能
- 不要使用与当前项目风格冲突的实现方式
- 不要省略必要的错误处理与基础状态处理
- 不要为了追求“高级架构”而增加不必要复杂度

针对当前仓库的补充要求：

- 在单模块阶段优先保持简单清晰的分层，不要过早拆成复杂模块体系
- 若当前任务只涉及单个页面或单条流程，不要顺手重构全项目
- 新增结构必须服务当前需求，而不是为了形式上的“标准架构”

## 5. 外部资源

后端 API 文档：

- [AIchat API Documentation](https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_API/API_DOCUMENTATION.html)

图片加载库：

- [Coil](https://github.com/coil-kt/coil)

设计依据：

- 使用提供的 Figma 设计稿作为页面还原依据
- Figma 设计稿链接：[AI 陪伴聊天 app 设计](https://www.figma.com/make/4CHusQ3BWl19YpAZQrR3XK/AI%E9%99%AA%E4%BC%B4%E8%81%8A%E5%A4%A9app%E8%AE%BE%E8%AE%A1?t=z7bBBJZnqFW1H76l-1&preview-route=%2Fhome)

## 6. Figma 实现要求

以下内容根据你提供的截图进行 OCR 提取，并已按当前 Android 项目做适配。

- 严格参考 Figma 设计稿完成页面实现
- 尽量还原页面结构、层级、间距、圆角、阴影、配色和组件状态
- 优先遵循设计规范，不要随意发挥
- 如设计稿中的部分效果不适合 Android 原生或 Compose 直接实现，应在保证整体风格一致的前提下，采用合理、可落地的 Compose 替代方案
- 不要为了还原设计而写出过于复杂、难以维护的代码

Android/Compose 落地补充：

- 允许为了性能、交互稳定性和可维护性，对极端复杂的视觉细节做等价替代
- 替代实现必须优先保证移动端可读性、触控体验、滚动性能和输入体验
- 聊天气泡、输入框、头像、卡片、按钮等核心组件的状态必须完整，包括默认态、按下态、禁用态、加载态

## 7. 开发原则

以下内容根据你提供的截图进行 OCR 提取，并已按当前项目结构改写。

- 优先实现真实可运行的页面与流程
- 优先保证每一步改动都可以编译通过
- 不要提交或保留无法编译的半成品
- 不要在 View 或 Composable 中堆积过多业务逻辑
- 适当拆分 Screen、ViewModel、Model、Repository、Service
- 命名清晰，目录结构清晰
- 新增代码时尽量保持与现有项目风格一致

针对当前仓库的具体执行要求：

- 每次新增页面时，都优先建立独立 `Screen` 与对应 `ViewModel`
- 网络、数据映射、状态汇总与 UI 展示必须分层
- 如果某一步尚未完成接口联调，也要先提供可编译的占位状态与预览数据
- 当前实现基础上，优先做体验增强、视觉打磨和接口稳定性修复，不要回退到模板式页面结构

## 8. 交付要求

以下内容根据你提供的截图进行 OCR 提取，并已按当前 Android 项目做适配。

每次完成任务时，必须满足以下要求：

1. 完成当前要求的实现任务
2. 确保项目可以编译通过
3. 输出“本次改动日志”
4. 将改动日志存到项目根目录的 `logs/` 文件夹下；如目录不存在，应先创建
5. 日志文件内容需能直接在 Android Studio 中打开阅读，优先使用 `.md` 或 `.txt` 格式

Android 项目补充要求：

- 如果本次任务未完成全部目标，日志中必须明确标注已完成部分与未完成部分
- 若当前环境无法实际完成编译验证，必须在日志和任务说明中明确写出阻塞原因
- 改动日志不是可选项，除非任务仅为纯阅读、纯分析且没有任何文件改动

## 9. 改动日志要求

以下内容根据你提供的截图进行 OCR 提取，并已按当前项目做适配。

每次输出的改动日志至少应包含：

- 本次新增了哪些文件
- 本次修改了哪些文件
- 每个文件的作用
- 本次完成了哪些功能
- 当前仍未完成的内容
- 如有必要，补充运行说明、验证结果或注意事项

建议日志文件命名方式：

- `logs/yyyymmdd-hhmm-task-name.md`
- `logs/yyyymmdd-hhmm-task-name.txt`

日志内容应面向后续协作，避免只写“修复若干问题”这类无效描述。

## 10. 推荐目录结构

当前项目仍为单模块阶段，建议在 `app/src/main/java/cn/vectorpeak/AIchat_projects/` 下按职责组织代码：

- `ui/`
- `ui/screen/`
- `ui/component/`
- `ui/navigation/`
- `viewmodel/`
- `data/model/`
- `data/remote/`
- `data/repository/`
- `domain/`（如后续出现明确业务抽象）

最低要求如下：

- 页面 UI 与可复用组件分离
- Screen 与 ViewModel 分离
- Repository / Service 与 UI 分离
- 主题、颜色、排版继续维护在 `ui/theme/`

## 11. Android 端落地规则

这是一个 Android 项目，后续实现必须考虑以下原生细节：

- 如需访问后端接口或加载网络图片，需确保 `AndroidManifest.xml` 中声明网络访问能力
- 处理系统状态栏、导航栏、沉浸式布局与安全区域
- 长聊天页面优先使用 `LazyColumn`
- 输入区需要考虑键盘弹出后的可见性与滚动联动
- 图片、头像、消息气泡、卡片、加载骨架都应优先适配手机竖屏场景
- 除视觉还原外，还要保证点击区域、文本可读性与滚动流畅性

## 12. 推荐实现顺序

基于当前仓库现状，建议按以下顺序推进：

1. 保留现有 Compose 工程基础，不回退为 XML
2. 替换默认首页内容，建立聊天首页或会话页骨架
3. 接入基础依赖：
   - Lifecycle ViewModel Compose
   - Kotlin Coroutines
   - Coil Compose
4. 如需请求接口，再接入 Retrofit / OkHttp / Kotlin Serialization 或 Moshi
5. 对照 Figma 完成主界面与关键组件还原
6. 接入 API 与消息流转逻辑
7. 在 Pixel 9 模拟器上验证布局、输入、滚动、图片加载与状态切换

## 13. Agent 执行规则

后续任何 Agent 在本仓库工作时，应遵循以下规则：

- 先读取当前代码、Gradle 配置与目录结构，再进行修改
- 新增依赖优先写入 `gradle/libs.versions.toml`
- 不要把大量逻辑继续塞进 `MainActivity.kt`
- 网络图片一律走 Coil
- 页面状态优先使用 `StateFlow`
- 不要为了短期速度破坏后续可维护性
- 如果 Figma 未覆盖完整交互状态，需要按 Android 常规体验补齐空态、错误态、加载态与禁用态
- 发生实际文件改动后，应同步输出并落地一份改动日志到 `logs/`

## 14. 最终目标

以下内容根据你提供的截图进行 OCR 提取，并已按当前 Android 项目语境改写。

最终应产出一个：

- 符合 Figma 设计稿视觉与交互意图的 Android AI 陪伴聊天 App
- 基于后端 API 文档实现核心聊天流程
- 至少兼容当前项目声明的 `minSdk 26` 运行环境
- 可在 Android Studio 最新稳定版中编译，并在 `Pixel 9` 模拟器上完成基础运行验证
- 结构清晰、代码可维护、便于后续继续扩展的项目版本

## 15. 完成标准

一个阶段性可交付版本至少应满足以下条件：

- 能在最新稳定版 Android Studio 中正常编译
- 能在 Pixel 9 模拟器中正常运行
- 主页面视觉风格与 Figma 设计稿基本一致
- 网络图片通过 Coil 正常展示
- 页面状态不是写死在 Composable 中
- 工程结构具备继续扩展为多页面或多模块的可维护性
