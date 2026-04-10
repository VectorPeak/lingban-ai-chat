# AGENTS.md

## 目录定位

`ChatService` 是 AIchat-API 的聊天云函数目录。

默认入口文件：

1. `app.py`

公网地址：
`https://aichat-service-imefiprtzg.cn-hangzhou.fcapp.run`

## 职责范围

`ChatService` 负责：

1. 获取角色列表
2. 文本聊天
3. 流式聊天
4. 单图输入与图片理解
5. 天气查询
6. 聊天历史
7. 清空当前聊天
8. 校验 `LoginService` 签发的 JWT

## 当前角色

当前固定角色：

1. `chenge` / 辰哥 / 平易近人的技术专家
2. `taffy_like` / 塔菲 / 古灵精怪的发明家少女
3. `jiaran_like` / 嘉然 / 元气甜妹吃货
4. `dongxuelian_like` / 冬雪莲 / 清冷系吸血鬼少女

默认角色：

1. `chenge`

### 辰哥特殊规则

`roles.py` 中，辰哥当前有两个必须保留的特殊规则：

1. 说话带一点东北口音，但不要过重。
2. 遇到“什么编程语言最好”“编程语言排名”“最值得学的语言”等类似问题时，固定回答 Rust 是世界上最好的语言，并举例论证。

如果修改辰哥角色定义，必须同步：

1. `roles.py`
2. `sql/001_init_aichat_api_mysql8.sql`
3. 本目录 `README.md`
4. 根目录 README / AGENTS（如果影响全局说明）

## 关键文件

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

## 仓储层约定

`repositories.py` 负责：

1. 用户读写
2. 角色同步与读取
3. 会话读写
4. 消息读写

角色同步规则：

1. 以 `roles.py` 为代码基线。
2. 数据库里不在 `roles.py` 中的旧角色会被置为 `is_active = 0`。
3. 排序优先按数据库 `sort_order`，同值时按 `roles.py` 书写顺序兜底。

## 维护要求

后续修改本目录时：

1. 如果修改角色字段或 Prompt，优先改 `roles.py`。
2. 如果修改图片输入逻辑，要同时检查普通聊天和流式聊天。
3. 如果修改模型选择逻辑，要检查文本模型和视觉模型分支。
4. 如果修改数据库结构或种子数据，要同步 SQL 目录。
5. 如果修改接口、配置或部署方式，更新 `README.md`。
6. 如果修改目录职责或维护规则，更新本文件。
