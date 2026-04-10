# 灵伴*AI聊天 Android

`灵伴*AI聊天` 是一个基于 `Kotlin + Jetpack Compose` 的 Android AI 陪伴聊天应用。

当前模块已经完成首版核心闭环：
- `Entry / Onboarding / Login / Home / Chat`
- 系统 Splash 启动过渡
- 手机验证码登录
- JWT 持久化
- 角色列表拉取
- 聊天历史加载
- 流式文本聊天
- 清空当前会话
- 自定义 launcher icon

## 设计与接口

- Figma 设计稿：
  [AI 陪伴聊天 app 设计](https://www.figma.com/make/4CHusQ3BWl19YpAZQrR3XK/AI%E9%99%AA%E4%BC%B4%E8%81%8A%E5%A4%A9app%E8%AE%BE%E8%AE%A1?t=z7bBBJZnqFW1H76l-1&preview-route=%2Fhome)
- 后端 API 文档：
  [AIchat API Documentation](https://vectorpeak-1318670795.cos.ap-guangzhou.myqcloud.com/AIchat_app/AIchat_API/API_DOCUMENTATION.html)

## 环境要求

- Android Studio 最新稳定版
- JDK 21
- Android SDK / Emulator
- 推荐使用 `Pixel 9` 模拟器

## 运行方式

1. 用 Android Studio 打开目录：
   `AIchat-android`
2. 在 `Gradle JDK` 中选择 JDK 21
3. 等待 Gradle Sync 完成
4. 启动模拟器
5. 运行 `app`

命令行构建示例：

```powershell
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:assembleRelease
```

## 当前项目结构

核心目录如下：

- `app/src/main/java/cn/vectorpeak/AIchat_projects/ui/screen`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/ui/component`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/ui/navigation`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/viewmodel`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/data/model`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/data/local`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/data/remote`
- `app/src/main/java/cn/vectorpeak/AIchat_projects/data/repository`
- `app/src/main/res`

## 目前已实现

### 启动与导航

- 系统 Splash + 最小化 Entry 页
- 首次进入 Onboarding
- 已登录冷启动直达 Home
- 未登录跳转 Login

### 登录

- 发送验证码
- 验证码登录
- 登录态持久化
- 协议勾选与基础错误提示

### 首页

- 当前陪伴角色卡片
- 其他角色列表
- 点击进入对应聊天页

### 聊天

- 历史记录加载
- 流式文本回复
- 停止生成
- 生成中动画
- 清空聊天确认弹窗

## 当前资源

- 应用名称：
  `灵伴*AI聊天`
- 当前 launcher icon 前景图：
  `app/src/main/res/drawable/app_icon_photo.png`

## 注意事项

- 当前项目仍保持单模块简单分层，不使用复杂 DI
- 图片聊天 UI 仍未完整接入
- Android 13/14 themed monochrome icon 仍未单独实现
