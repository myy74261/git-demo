# 执笔 · 学术写作教练

面向高校学术写作的写作过程引导工具。它不做 AI 检测，而是在写作过程中指出证据、引用和论证上的缺口，给出可以直接动手修改的建议。

## 当前阶段

第一阶段：Spring Boot 工程骨架与响应式写作工作台。

本阶段完成的内容：

- 可直接启动的 Maven 工程
- PC 端三栏工作台（大纲、写作、引导）
- 平板端大纲抽屉与双栏布局
- 手机端单栏视图与底部导航
- 本机草稿自动保存、字数与段落统计
- 引导卡片交互示例

> 右侧引导卡片目前是静态示例，规则引擎与实时分析将在下一阶段接入。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 运行环境 | Java 17 |
| 后端 | Spring Boot 3.3、Spring MVC、Thymeleaf |
| 数据 | Spring Data JPA、H2（文件模式） |
| 前端 | 原生 HTML / CSS / JavaScript，无 Node 构建链 |
| 构建 | Maven 3.10 |

## 本地运行

本机工具已安装在 D 盘，无需额外下载：

- JDK：`D:\Project\jdk`
- Maven：`D:\Project\apache-maven-3.10.0`

在 VS Code 终端中执行：

```powershell
cd D:\workspace\Web\writing-coach
$env:JAVA_HOME = "D:\Project\jdk"
& "D:\Project\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
```

浏览器访问 <http://localhost:8080>。

手机与电脑连接同一 Wi-Fi 后，访问 `http://<电脑局域网 IP>:8080`。首次启动时 Windows 防火墙可能弹窗，请允许 Java 的专用网络访问。

常用命令：

```powershell
# 运行测试
& "D:\Project\apache-maven-3.10.0\bin\mvn.cmd" test

# 打包
& "D:\Project\apache-maven-3.10.0\bin\mvn.cmd" package
```

## 代码结构

```text
src/main/java/com/writingcoach
├── WritingCoachApplication.java   应用入口
└── web
    ├── WorkspaceController.java   页面路由与视图模型
    └── OutlineItem.java           大纲条目视图对象

src/main/resources
├── application.yml                应用配置
├── templates/workspace.html       工作台页面
└── static
    ├── css/app.css                响应式样式
    └── js/workspace.js            页面交互与本机草稿
```

后续阶段会按 `domain`、`service`、`repository`、`config` 继续分层，保持 Controller 只承担请求编排。

## 代码约定

- 类名使用名词，方法名使用动宾结构，常量使用全大写。
- 公共类型和公共方法写 Javadoc；复杂逻辑解释“为什么”，不复述代码表面含义。
- Java 缩进 4 空格，HTML / CSS / JS 缩进 2 空格，统一 UTF-8 与 LF 换行。
- 配置分环境管理，密钥只通过环境变量或本地配置注入，不进入仓库。
- 提交信息使用 `feat:`、`fix:`、`refactor:`、`docs:`、`chore:` 前缀。

## 路线图

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| P0 | 工程骨架与响应式工作台 | 已完成 |
| P1 | 服务端保存、文档版本与对比 | 待开始 |
| P2 | 写作过程事件采集与段落指标 | 待开始 |
| P3 | 规则引擎、实时引导卡片 | 待开始 |
| P4 | 写作过程档案、教师看板 | 待开始 |
