# 内部任务管理系统（MVP）

实习生与导师之间的任务分配、跟踪管理系统。2 天内完成的可运行 MVP 版本。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 + JWT(jjwt 0.12.5) |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3.4 + Vite 5 + Element Plus 2.7 + Pinia + Vue Router 4 + Echarts 5.5 + Axios |
| 其他 | RESTful API、CORS、BCrypt 密码加密 |

## 功能清单

### 必做功能
- [x] 用户登录/注册（JWT 鉴权，支持 Mock 演示账号）
- [x] 任务 CRUD：创建、查看、编辑、删除
- [x] 任务状态流转：待办 → 进行中 → 已完成（看板拖拽 + 表格下拉切换）
- [x] 任务筛选/搜索（按状态、优先级、负责人、关键字）
- [x] 实时资讯模块（**Skill 构建与应用能力**）：
  - [x] 调用公开第三方 **API**（Hacker News Firebase）+ **RSS**（hnrss.org/frontpage）获取最新科技资讯
  - [x] 任务详情页按**标签关联**显示相关资讯（如 Java/Spring Boot 任务自动关联相关新闻）
  - [x] 资讯展示、搜索、刷新（5 分钟内存缓存）

### 核心页面
- [x] 登录页（登录/注册双 Tab）
- [x] 任务列表页（看板卡片 / 表格两种视图切换）
- [x] 任务详情抽屉 + 编辑弹窗
- [x] 个人仪表盘（待办/已完成统计 + Echarts 图表）

### 加分项
- [x] 任务优先级（高/中/低）+ 颜色区分
- [x] 统计图表（Echarts：状态饼图 + 优先级柱状图）
- [x] 截止日期提醒（前端倒计时 + 逾期红色高亮）
- [x] 导师可查看所有任务，实习生只能看分配给自己或自己创建的
- [x] 导出任务列表为 CSV

## AI 辅助说明

> 本项目允许使用 AI 工具辅助开发，以下为 AI 协助部分说明：

- **数据库设计**：`schema.sql` 表结构与初始数据由 AI 工具协助生成。
- **后端代码**：Spring Boot 项目骨架、实体/Mapper/Service/Controller、JWT 认证、全局异常处理、资讯模块（Hacker News API + RSS 对接）由 AI 工具（Trae / Claude）依据需求文档生成实现。
- **前端代码**：Vue3 项目骨架、路由/状态管理、Element Plus 页面（登录、任务看板、仪表盘、资讯页）、Echarts 图表、拖拽交互、CSV 导出由 AI 工具协助生成。
- **业务逻辑**：任务状态流转规则、角色权限控制（导师/实习生）、标签关联资讯等业务设计由 AI 依据需求产出。

人工主要负责需求梳理、环境配置、联调验证与代码审查。

## 项目结构

```
d:\hxkj\
├── schema.sql                          # 数据库初始化脚本
├── README.md                           # 本文档
├── task-management-backend\            # 后端（用 IDEA 打开）
│   ├── pom.xml
│   └── src\main\
│       ├── java\com\company\task\
│       │   ├── TaskManagementApplication.java   # 启动类
│       │   ├── config\        # CORS / JWT / 过滤器 / 数据初始化
│       │   ├── controller\    # Auth / Task / User / News
│       │   ├── service\       # 业务逻辑
│       │   ├── mapper\        # MyBatis-Plus Mapper
│       │   ├── entity\        # 实体
│       │   ├── dto\           # 数据传输对象
│       │   ├── common\        # 统一响应
│       │   └── exception\     # 全局异常
│       └── resources\
│           ├── application.yml         # 配置（含数据库连接）
│           └── mapper\TaskMapper.xml
└── task-management-frontend\           # 前端（用 VSCode 打开）
    ├── package.json
    ├── vite.config.js                  # 含 /api 代理到后端
    └── src\
        ├── main.js
        ├── api\            # Axios 请求封装
        ├── router\         # 路由 + 权限守卫
        ├── store\          # Pinia 用户状态
        ├── utils\          # 常量 / 工具
        ├── components\     # TaskDialog 组件
        └── views\          # Login / Layout / TaskList / Dashboard / News
```

## 启动步骤

### 前置条件
- JDK 17+
- Node.js 18+（已验证 v24）
- MySQL 8.0（本机已运行，端口 3306）
- Maven 3.6+（或使用 IDEA 内置 Maven）

### 1. 初始化数据库
MySQL 已在本机 3306 端口运行（root / 123456）。执行：

```bash
mysql -u root -p123456 < d:\hxkj\schema.sql
```

或用 Navicat 打开 `d:\hxkj\schema.sql` 执行。会创建 `task_db` 库 + `users`/`tasks` 表 + 示例任务数据。

> 默认账号（密码均为 `123456`）由后端启动时自动创建，无需手动插入：
> - `mentor` / `123456`（导师）
> - `intern01` / `123456`（实习生）
> - `intern02` / `123456`（实习生）

### 2. 启动后端（IDEA 打开 `task-management-backend`）
1. 用 IntelliJ IDEA 打开 `d:\hxkj\task-management-backend` 目录。
2. 等待 Maven 自动下载依赖（`pom.xml`）。
3. 运行 `TaskManagementApplication.java` 主类。
4. 后端启动在 `http://localhost:8080`，控制台会打印默认账号信息。

> 如需命令行启动：`mvn spring-boot:run`

### 3. 启动前端（VSCode 打开 `task-management-frontend`）
1. 用 VSCode 打开 `d:\hxkj\task-management-frontend` 目录。
2. 终端执行：
   ```bash
   npm install
   npm run dev
   ```
3. 浏览器访问 `http://localhost:5173/`，使用演示账号登录。

> Vite 已配置 `/api` 代理到后端 `http://localhost:8080`，无需关心跨域。

## 主要接口

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | /api/auth/login | 登录 | 否 |
| POST | /api/auth/register | 注册 | 否 |
| GET | /api/tasks | 任务列表（筛选/搜索） | 是 |
| GET | /api/tasks/{id} | 任务详情 | 是 |
| POST | /api/tasks | 创建任务 | 是 |
| PUT | /api/tasks/{id} | 编辑任务 | 是 |
| PATCH | /api/tasks/{id}/status | 状态流转 | 是 |
| DELETE | /api/tasks/{id} | 删除任务 | 是 |
| GET | /api/tasks/dashboard | 仪表盘统计 | 是 |
| GET | /api/users | 用户列表 | 是 |
| GET | /api/news | 最新资讯（支持 keyword 搜索） | 否 |
| GET | /api/news/by-tags | 按标签关联资讯 | 否 |

## 资讯模块说明（Skill 构建与应用能力）

资讯模块满足需求中的 3 条要求：
1. **调用第三方 API + RSS**：同时对接 Hacker News Firebase API（`hacker-news.firebaseio.com`）和 RSS 源（`hnrss.org/frontpage`），合并去重后返回。
2. **任务关联资讯**：任务详情页根据任务 `tags` 字段（如 `Java,Spring Boot`）自动调用 `/api/news/by-tags` 获取并展示匹配的新闻。
3. **展示/搜索/刷新**：独立的「实时资讯」页面支持关键词搜索与手动刷新；带 5 分钟内存缓存避免频繁请求被限流。

> 若网络无法访问外部 API，可在 `application.yml` 设置 `app.news.enabled: false` 关闭资讯抓取（接口返回空列表，不影响其他功能）。
