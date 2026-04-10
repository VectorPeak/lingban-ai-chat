# Codex Skills Index

这个目录是当前机器上的 Codex skills 根目录。

你的自定义学习型 skill 套件目前围绕一个核心闭环组织：

1. 遇到阻塞时快速补课
2. 需要时看轻量知识树
3. 学完后出题检验
4. 最后做评分和补强

## 目录说明

当前目录下主要有三类内容：

1. `.system`
   Codex 自带的系统 skill
2. `claude-to-im`
   独立的消息桥接 skill
3. 你的学习型 skill 套件

## 学习型 Skill 套件

### 1. 学习输入

#### `$top-down-learning`

完整入口，适合你想显式区分模式时使用。

支持：

1. `$top-down-learning teach <topic>`
2. `$top-down-learning t <topic>`
3. `$top-down-learning map <topic>`
4. `$top-down-learning m <topic>`

作用：

1. `teach / t`
   围绕一个当前卡点做小范围讲解、案例说明和相关知识扩充
2. `map / m`
   围绕一个主题输出轻量知识点目录树和学习顺序

文件：

- [top-down-learning/SKILL.md](C:/Users/ZXY/.codex/skills/top-down-learning/SKILL.md)

#### `$tt`

教学模式快捷入口。

等价定位：

`$top-down-learning teach ...`

适合：

1. 我卡在一个概念
2. 我看不懂当前项目某个实现点
3. 我需要一个贴近项目的小范围解释

文件：

- [tt/SKILL.md](C:/Users/ZXY/.codex/skills/tt/SKILL.md)

#### `$tm`

知识树模式快捷入口。

等价定位：

`$top-down-learning map ...`

适合：

1. 我想知道一个主题往上往下连着什么
2. 我想看轻量学习顺序
3. 我不想直接展开成完整教程

文件：

- [tm/SKILL.md](C:/Users/ZXY/.codex/skills/tm/SKILL.md)

### 2. 学习检验

#### `$learning-exam`

完整考试入口，适合你想显式写出题型时使用。

支持：

1. `$learning-exam quiz <topic>`
2. `$learning-exam q <topic>`
3. `$learning-exam code <topic>`
4. `$learning-exam c <topic>`
5. `$learning-exam grade <answer>`
6. `$learning-exam g <answer>`

作用：

1. `quiz / q`
   出概念小测
2. `code / c`
   出小型代码题
3. `grade / g`
   对最近的小测答案评分并给补强建议

文件：

- [learning-exam/SKILL.md](C:/Users/ZXY/.codex/skills/learning-exam/SKILL.md)

#### `$eq`

概念小测快捷入口。

等价定位：

`$learning-exam quiz ...`

适合：

1. 检验概念区分
2. 检验原理理解
3. 检验能不能用自己的话解释

文件：

- [eq/SKILL.md](C:/Users/ZXY/.codex/skills/eq/SKILL.md)

#### `$ec`

代码小测快捷入口。

等价定位：

`$learning-exam code ...`

适合：

1. 基于当前项目出读代码题
2. 出一个 10 到 30 分钟可完成的小代码题
3. 检验是否真的会应用而不是只会背概念

文件：

- [ec/SKILL.md](C:/Users/ZXY/.codex/skills/ec/SKILL.md)

#### `$eg`

评分快捷入口。

等价定位：

`$learning-exam grade ...`

适合：

1. 给最近的小测答案打分
2. 找出错因
3. 给 1 到 3 个补强建议

评分特点：

1. 使用 100 分制
2. 保持粗粒度
3. 不做特别细的评分拆分

文件：

- [eg/SKILL.md](C:/Users/ZXY/.codex/skills/eg/SKILL.md)

## 推荐使用顺序

最自然的一套流程是：

1. `$tt`
   先补一个阻塞点
2. `$tm`
   如果还想知道这个点在知识体系中的位置，再看知识树
3. `$eq` 或 `$ec`
   学完后马上出题检验
4. `$eg`
   看评分、错因和补强建议

如果你想保留完整语义，也可以始终使用：

1. `$top-down-learning`
2. `$learning-exam`

## 常用示例命令

下面这些例子可以直接作为常用模板使用。

### 学习输入

1. `$tt 我卡在 StateFlow 和 MutableStateFlow 的区别了`
2. `$tt 这个项目里为什么要 repository 这一层`
3. `$tm Android 状态管理`
4. `$tm Kotlin 协程`
5. `$top-down-learning t 我没看懂 ViewModel 为什么要存在`
6. `$top-down-learning m Compose 状态管理`

### 学习检验

1. `$eq Kotlin 协程`
2. `$eq 这个项目里的 JWT 登录流程`
3. `$ec 这个项目里的 repository 分层`
4. `$ec ChatViewModel 的状态流转`
5. `$learning-exam q Android 状态管理`
6. `$learning-exam c 登录页验证码倒计时的状态处理`

### 评分

1. `$eg 1:A 2:C 简答: StateFlow 更适合持续状态，SharedFlow 更适合事件`
2. `$learning-exam g 1:B 2:A 简答: repository 负责把 remote 和 local 统一成上层可用的数据入口`

### 一套完整流程示例

1. `$tt 我没看懂 repository 为什么要单独抽一层`
2. `$tm Android 分层架构`
3. `$eq repository 分层`
4. `$eg 1:B 2:C 简答: repository 是为了隔离数据来源并统一给 ViewModel 使用`

## 命名规则

当前这套命名采用“两层命名”：

1. 完整语义名
   用于长期维护和理解
   例如：
   - `top-down-learning`
   - `learning-exam`
2. 极短快捷名
   用于高频调用
   例如：
   - `tt`
   - `tm`
   - `eq`
   - `ec`
   - `eg`

这样既保留语义清晰度，也保留输入效率。

## 补充说明

1. 这些 skill 目前都是本地 skill，跟随当前机器和当前 Codex 环境生效。
2. 这些 skill 都是显式调用型，不会默认隐式注入。
3. 如果后续继续扩展，建议继续沿用“完整语义名 + 极短快捷名”的命名方式。
