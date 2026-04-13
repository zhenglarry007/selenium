# Selenium Java Test Framework

一个基于 `Selenium + TestNG + Maven + Allure` 的轻量级 Web UI 自动化测试框架，内置页面对象模型（POM）、多执行目标（本地 / Selenium Grid / Testcontainers）以及操作过程截图记录。

## 特性

- 基于 Java 21 和 Maven，开箱即用
- 使用 TestNG Suite 管理测试执行
- 支持三种执行目标：`local`、`selenium-grid`、`testcontainers`
- 集成 Allure 报告（包含环境信息）
- 自动记录 Selenium 操作步骤并附加截图

## 目录结构

```text
.
├── assets/project-execution-screenshots/     # README 执行截图
├── grid/                                 # Selenium Grid 相关配置
├── src/main/java/com/larry
│   ├── config/                           # 配置读取（Owner）
│   ├── driver/                           # Driver 工厂与管理
│   ├── page/                             # Page Object
│   └── report/                           # Allure 相关能力
├── src/test/java/com/larry
│   ├── BaseWeb.java                      # 测试基类（启动/销毁驱动）
│   └── test/BookRoomWebTest.java         # 示例测试
├── src/test/resources
│   ├── suites/                           # TestNG suite 文件
│   ├── general.properties                # 通用配置
│   └── selenium-grid.properties          # Grid 配置
└── README.MD
```

## 环境要求

- JDK `21`
- Maven `3.9+`
- Chrome / Firefox / Edge / Safari（按本地执行目标选择）
- Docker（使用 Selenium Grid 或 Testcontainers 时需要）
- 被测应用可访问（默认：`http://localhost:5173/`）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/zhenglarry007/selenium.git
cd selenium
```

### 2. 准备被测应用

默认配置在 `src/test/resources/general.properties`：

```properties
url.base = http://localhost:5173/
```

请先确保该地址可以打开并可正常交互。

### 3. 执行本地测试

```bash
mvn test -Pweb-execution -Dsuite=local
```

## 运行方式

### 方式 A：本地浏览器（local）

默认 `target=local`，浏览器由 `general.properties` 控制：

```properties
target = local
browser = chrome
headless = false
```

执行命令：

```bash
mvn test -Pweb-execution -Dsuite=local
```

可通过系统参数覆盖：

```bash
mvn test -Pweb-execution -Dsuite=local -Dbrowser=firefox -Dheadless=true
```

### 方式 B：Selenium Grid（selenium-grid）

1. 启动 Grid：

```bash
cd grid
docker compose up -d
cd ..
```

2. 执行 Grid Suite：

```bash
mvn test -Pweb-execution -Dsuite=selenium-grid -Dtarget=selenium-grid
```

默认 Grid 地址来源于 `src/test/resources/selenium-grid.properties`：

```properties
grid.url = localhost
grid.port = 4444
```

### 方式 C：Testcontainers（testcontainers）

```bash
mvn test -Pweb-execution -Dsuite=local -Dtarget=testcontainers -Dbrowser=chrome
```

> 说明：`suite=local` 指 TestNG 用例集合；`target=testcontainers` 指执行基础设施。两者可以组合使用。

## 测试报告与截图

### 生成并查看 Allure 报告

```bash
mvn allure:serve
```

或先生成静态报告：

```bash
mvn allure:report
```

报告结果目录：

- `target/allure-results`
- `target/site/allure-maven-plugin`

### Selenium 操作过程记录

框架已自动记录常见 Selenium 动作（如 `open/click/sendKeys`），并在 Allure Step 中附加截图。

### 项目执行截图

下方为真实执行过程截图示例（来自本项目自动化运行）：

![Allure Execution Demo](assets/project-execution-screenshots/allure_report.gif)

![Step 1](assets/project-execution-screenshots/step-01.png)
![Step 2](assets/project-execution-screenshots/step-02.png)
![Step 3](assets/project-execution-screenshots/step-03.png)

## 常用命令速查

```bash
# 本地执行（默认 Chrome）
mvn test -Pweb-execution -Dsuite=local

# 本地无头执行
mvn test -Pweb-execution -Dsuite=local -Dheadless=true

# Grid 执行
mvn test -Pweb-execution -Dsuite=selenium-grid -Dtarget=selenium-grid

# 查看 Allure 报告
mvn allure:serve
```

## 配置说明

配置优先级（高 -> 低）：

1. `-D` 系统参数
2. `src/test/resources/general.properties`
3. `src/test/resources/selenium-grid.properties`

核心配置项：

- `target`: `local | selenium-grid | testcontainers`
- `browser`: `chrome | firefox | edge | safari`
- `headless`: `true | false`
- `url.base`: 被测系统入口
- `timeout`: 全局等待超时（秒）

## 新增测试用例（建议）

1. 在 `src/main/java/com/larry/page` 新增页面对象方法。
2. 在 `src/test/java/com/larry/test` 新增 TestNG 测试类。
3. 在 `src/test/resources/suites/*.xml` 中注册测试类。
4. 执行 `mvn test -Pweb-execution -Dsuite=local` 验证。

## 常见问题

- 浏览器驱动报错：请确认本机浏览器版本与 Selenium 兼容，并确保浏览器可启动。
- `Connection refused`：通常是 `url.base` 未启动或 Grid 未启动。
- 无 Allure 报告：先执行测试，再运行 `mvn allure:serve`。
- 看不到截图：确认测试已执行且 `target/allure-results` 中有 Allure 结果文件。

## 许可证

本项目基于 MIT 协议（如需开源发布，建议在仓库根目录补充 `LICENSE` 文件）。
