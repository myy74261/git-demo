# 执笔 · 学术写作教练

面向高校学术写作的写作过程引导工具。它不做 AI 检测，而是在写作过程中指出证据、引用和论证上的缺口，给出可以直接动手修改的建议。

## 当前阶段

第二阶段：服务端自动保存、版本历史与版本对比。

本阶段完成的内容：

- 文档保存到 H2，刷新页面后内容不会丢失
- 停止输入 900 毫秒后自动保存，失败时退回本机草稿
- 手动创建版本快照，记录时间、字数与触发方式
- 版本列表、与当前稿的逐行对比、历史版本恢复
- 保存时检测版本冲突，避免静默覆盖
- PC 三栏、平板双栏与手机单栏布局保持完整

> 右侧引导卡片目前仍是静态示例，规则引擎与实时分析将在第三阶段接入。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 运行环境 | Java 17 |
| 后端 | Spring Boot 3.3、Spring MVC、Thymeleaf |
| 数据 | Spring Data JPA、H2（文件模式） |
| 前端 | 原生 HTML / CSS / JavaScript，无 Node 构建链 |
| 构建 | Maven 3.10 |

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/documents/{id}` | 读取文档 |
| PUT | `/api/documents/{id}` | 保存文档，使用 `baseRevision` 防止覆盖 |
| POST | `/api/documents/{id}/versions` | 为当前内容创建版本快照 |
| GET | `/api/documents/{id}/versions` | 版本列表 |
| GET | `/api/documents/{id}/versions/{versionId}` | 版本详情 |
| GET | `/api/documents/{id}/versions/{versionId}/diff` | 与当前稿逐行对比 |
| POST | `/api/documents/{id}/versions/{versionId}/restore` | 恢复历史版本 |

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
├── domain                         文档与版本实体、领域异常
├── repository                     JPA 数据访问
├── service                        保存、快照、恢复与差异计算
├── bootstrap                      演示数据初始化
└── web                            页面路由、REST 接口、DTO 与异常处理

src/main/resources
├── application.yml                应用配置
├── templates/workspace.html       工作台页面
└── static
    ├── css/app.css                响应式样式
    └── js/workspace.js            页面交互与本机草稿
```

Controller 只承担请求编排，业务规则集中在 service 与 domain，后续阶段继续沿用这一分层。

## 代码约定

- 类名使用名词，方法名使用动宾结构，常量使用全大写。
- 公共类型和公共方法写 Javadoc；复杂逻辑解释“为什么”，不复述代码表面含义。
- Java 缩进 4 空格，HTML / CSS / JS 缩进 2 空格，统一 UTF-8 与 LF 换行。
- 配置分环境管理，密钥只通过环境变量或本地配置注入，不进入仓库。
- 提交信息按 `first`、`second`、`third` 的顺序命名，保持作业进度可追踪。

## 路线图

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| P0 | 工程骨架与响应式工作台 | 已完成 |
| P1 | 服务端保存、文档版本与对比 | 已完成 |
| P2 | 写作过程事件采集与段落指标 | 待开始 |
| P3 | 规则引擎、实时引导卡片 | 待开始 |
| P4 | 写作过程档案、教师看板 | 待开始 |
