# 课程知识点在线实训管理平台 — 项目总结



## 一、项目定位

这是一个**前后端分离的实训教学管理平台**，围绕"教师发布知识点实训任务 → 学生在线提交作业（文本 + 附件）→ 教师批阅打分 → 成绩统计分析"的完整教学闭环，支持 **管理员 / 教师 / 学生** 三种角色。

- 前端工程：`Vue/`（包名 `train-platform-web`）
- 后端工程：`spring/`
- 配套资料：接口文档、数据库设计文档、问题修复记录（均在项目根目录）

## 二、技术栈

| 层 | 技术 | 版本 / 说明 |
|---|---|---|
| 前端 | Vue 3（Composition API + `<script setup>`） | 3.5 |
| | Vite / Vue Router / Pinia | 8 / 4 / 3 |
| | Element Plus、ECharts、Axios | 2.9 / 6.1 / 1.7 |
| 后端 | Spring Boot、Java | 3.0.0 / 17 |
| | MyBatis-Plus（+ 分页插件） | 3.5.12 |
| | JWT（jjwt）、BCrypt（spring-security-crypto） | 0.12.6 |
| | Jakarta Validation、Lombok | — |
| 数据库 | MySQL，库名 `train_platform` | root/root |
| 联调 | 前端 5173 → Vite 代理 → 后端 `:8080/api` | 见 `Vue/vite.config.js` |

## 三、后端架构

标准四层分包，结构清晰（包根：`com.example.spring`）：

- **controller**（4 个）：`UserController`、`TaskController`、`SubmissionController`、`ReviewController`
- **service / mapper / entity**：各 4 个；Service 继承 MyBatis-Plus 的 `ServiceImpl`，单表 CRUD 零 SQL
- **common**：
  - `Result`：统一返回体
  - `ErrorCode`：错误码枚举（401/403/404、1001 用户名已存在、2001 任务不存在、4002 评分超范围等）
  - `BizException` + `GlobalExceptionHandler`：业务异常与全局异常处理
  - `UserContext`：基于 ThreadLocal 的登录上下文
- **认证链路**：
  `AuthInterceptor` 校验 `Authorization: Bearer {token}` → 解析 userId/role 放入 ThreadLocal → Controller 用 `UserContext.requireRole(...)` 做角色鉴权；
  登录、注册、附件静态资源、`/error` 放行（见 `config/WebConfig.java`）
- **配置**：
  - `MybatisPlusConfig`：分页插件 + Mapper 扫描
  - `BeanConfig`：BCrypt 密码编码器
  - `WebConfig`：CORS、拦截器注册、附件目录静态映射 `/upload/** → ./upload`

## 四、数据库设计

共 4 张表，DDL 与测试数据见 `spring/src/main/resources/train_platform.sql`。

| 表 | 说明 | 关键设计 |
|---|---|---|
| `sys_user` | 用户表 | BCrypt 密码、角色（admin/teacher/student）、班级、启用状态；用户名唯一索引 |
| `train_task` | 实训任务表 | 目标班级 NULL = 全部班级；截止时间；状态 0 草稿 / 1 进行中 / 2 已截止；发布人外键；`(status, deadline)` 联合索引 |
| `task_submission` | 作业提交表 | `(task_id, student_id)` 联合唯一，数据库层保证"一人一任务一份提交"，重复提交走覆盖更新；含分数、评语、批阅人、批阅时间、状态 |
| `operation_log` | 操作日志表 | 操作人、请求方法路径、参数、IP（建表与示例数据已就绪） |

内置测试数据：admin + 3 名教师 + 3 个班级共 60 名学生 + 6 个任务 + 约 25 条提交（分数覆盖 90+ / 80-89 / 70-79 / 60-69 / 60 以下各分段，便于统计图表演示）。

初始管理员账号：`admin / admin123`。

## 五、功能模块（按角色）

### 学生

1. 注册（角色固定 student）/ 登录
2. 任务大厅：仅可见**已发布**且**面向全部班级或本班**的任务，支持关键字搜索
3. 任务详情：查看任务内容、截止时间、发布人及本人是否已提交
4. 提交作业：文本 + 附件；过截止时间禁止提交；重复提交覆盖原内容并重置批阅状态（分数/评语清空、状态回到待批阅）
5. 我的作业：分页查看本人提交记录、批阅状态、分数与评语

### 教师

1. 发布任务：标题、知识点、内容、目标班级（可空 = 全部班级）、截止时间、保存草稿 / 立即发布
2. 编辑 / 删除任务：仅发布人本人可操作（admin 除外）
3. 作业批阅：按任务 + 批阅状态 + 班级筛选提交列表，打分（0~100，服务端 `@DecimalMin/@DecimalMax` 校验）+ 评语
4. 成绩统计：学生总数、已提交数、提交率、平均/最高/最低分、五个分数段分布；前端用 ECharts 环形图 + Element Plus 进度条可视化，支持按班级筛选

### 管理员

- 拥有教师全部权限
- 用户管理：按角色 / 班级 / 关键字分页查询用户
- 账号启用 / 禁用（禁用账号无法登录，仅 admin 可操作）

### 前端要点

- `src/utils/request.js`：Axios 实例统一 baseURL `/api`、请求自动携带 Token、响应解包 `Result`、401 自动清除 Token 并跳转登录
- `src/stores/user.js`：Pinia 用户状态，提供 `role / isAdmin / isTeacherOrAdmin` 派生状态
- `src/layout/Layout.vue`：侧边菜单按角色动态渲染
- `src/router/index.js`：路由懒加载 + 登录守卫（未登录跳转 `/login`）
- 所有列表页统一"搜索表单 + 表格 + 分页"模式

## 六、工程亮点

1. **统一规范**：统一返回体、错误码枚举、全局异常处理、`@Valid` 声明式参数校验
2. **数据级权限控制**：
   - 学生任务列表按班级过滤
   - 学生只能查看本人成绩（`ReviewService` 中校验 studentId）
   - 任务仅发布人本人或 admin 可编辑 / 删除
3. **附件上传安全**：扩展名白名单（zip/rar/pdf/doc/docx/md）+ 10MB 大小限制 + UUID 重命名 + 按 `yyyy/MM` 分目录存储
4. **业务约束落到数据库**：联合唯一索引防重复提交、外键约束、状态与截止时间联合索引
5. **前后端双重校验**：例如截止时间必须晚于当前时间 —— 前端日期面板禁选过去日期 + 提交时精确到秒校验，后端 Service 再兜底（防止绕过页面直接调接口）
6. **无状态认证**：JWT + 拦截器 + ThreadLocal，服务端无需 Session
7. **统计计算严谨**：平均分 / 提交率使用 `BigDecimal` 并按 `HALF_UP` 保留两位小数，避免浮点误差

## 七、接口清单

| 模块 | 方法 | 路径 | 权限 |
|---|---|---|---|
| 用户 | POST | `/user/register` | 公开 |
| 用户 | POST | `/user/login` | 公开 |
| 用户 | GET | `/user/info` | 登录 |
| 用户 | GET | `/user/page` | 教师/管理员 |
| 用户 | PUT | `/user/{userId}/status` | 管理员 |
| 任务 | POST | `/task` | 教师/管理员 |
| 任务 | PUT | `/task/{taskId}` | 发布人/管理员 |
| 任务 | DELETE | `/task/{taskId}` | 发布人/管理员 |
| 任务 | GET | `/task/page` | 登录 |
| 任务 | GET | `/task/{taskId}` | 登录 |
| 提交 | POST | `/submission` | 学生 |
| 提交 | GET | `/submission/my` | 学生 |
| 提交 | GET | `/submission/page` | 教师/管理员 |
| 批阅 | POST | `/review` | 教师/管理员 |
| 批阅 | GET | `/review/submission/{submissionId}` | 登录（本人/教师） |
| 批阅 | GET | `/review/statistics` | 教师/管理员 |

## 八、本地运行方式

### 后端（spring/）

1. 本机 MySQL 执行 `src/main/resources/train_platform.sql` 建库建表并导入测试数据
2. 确认 `application.yml` 中数据库账号密码（默认 root/root）
3. 使用 **JDK 17** 启动 `Application.java`，服务端口 `8080`，上下文路径 `/api`

### 前端（Vue/）

```bash
npm install
npm run dev
```

启动后访问 `http://localhost:5173`，`/api` 请求由 Vite 代理到 `8080`。

## 九、当前状态与可改进项

主体功能已闭环、可运行。通读代码后发现以下待完善点：

1. **前端角色守卫未强制**：路由 meta 中声明了 `roles`，但 `router/index.js` 的 `beforeEach` 只校验 Token。学生直接输入 URL 可进入批阅/用户管理页（后端接口会拦截，不会造成数据问题），建议补充前端角色判断
2. **任务状态不会自动流转**：`status = 2（已截止）` 没有定时任务更新，列表状态可能与 deadline 不一致（提交接口是实时按 deadline 判断的，业务安全，仅列表展示可能不准）。可在分页查询时按 deadline 动态计算状态
3. **操作日志尚未接入**：`operation_log` 表与 Mapper 已建，但没有 AOP 或拦截器实际写入日志
4. **配置安全**：JWT 密钥、数据库密码硬编码在 `application.yml`；CORS 全开放且允许 credentials，生产环境建议收紧并改为环境变量注入
5. **附件直链未鉴权**：`/upload/**` 对匿名用户放行，知道文件 UUID 路径即可下载，敏感场景需增加下载鉴权
6. **本机环境注意**：系统默认 JDK 为 26，与 Spring Boot 3.0.0 自带的旧版 Lombok 不兼容，命令行编译需切换到 JDK 17（IDEA 内运行不受影响）

## 十、总结

本项目是一个**分层规范、功能完整、具备真实测试数据的课程实训级全栈项目**，完整覆盖了以下主流 Web 开发知识点：

- 前后端分离架构与跨域、代理联调
- JWT 无状态认证与 RBAC 角色权限
- 统一返回体、全局异常、声明式参数校验
- MyBatis-Plus 分页、条件构造器、数据级过滤
- 文件上传校验与静态资源映射
- ECharts 数据可视化
- 数据库约束设计（唯一索引、外键、联合索引）
- 前后端双重数据校验
