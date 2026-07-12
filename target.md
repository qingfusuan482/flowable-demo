# Flowable 业务系统 Demo — 目标与实施计划

## 项目背景

基于现有 `flowable-demo` Maven 项目（Java 17），从零搭建一个完整的 Flowable 流程引擎业务系统 demo，覆盖流程全生命周期管理、组织架构权限、前后端分离架构。

## 核心目标

### 1. 流程新建
- 提供流程定义的新增入口（REST API）
- 支持通过 BPMN XML 创建流程定义
- 流程定义包含基本元信息：名称、标识 Key、描述、分类等
- 前端使用 bpmn.js 画布进行流程可视化设计
- 流程模型（Model）的创建与存储

### 2. 流程业务数据绑定
- 流程启动时绑定业务表单数据（如请假天数、申请人、合同甲乙方等）
- 流程变量（Process Variables）的存取机制
- 支持流程实例与业务主键（businessKey）关联
- 表单数据的序列化/反序列化

### 3. 流程编辑
- 流程模型（Model）的编辑更新能力
- 前端 bpmn.js 画布可视化编辑流程图
- 流程节点配置修改（审批人、条件表达式、服务任务等）
- **审批人来源支持**：
  - 按角色分配（如：部门经理角色、人事角色）
  - 按岗位分配（如：财务总监岗位）
  - 按表单字段变量动态分配（如：`${starter}` 发起人上级）
- 编辑后保存为新版本草稿

### 4. 流程版本发布及控制
- 流程定义版本管理：每次发布自动生成新版本号
- 版本状态控制：草稿 → 已发布 → 已停用
- 已发布版本的流程实例使用版本保护
- 支持指定版本启动流程
- 流程定义挂起与激活控制

### 5. 流程留痕
- 流程实例全生命周期操作日志记录
- 每个节点的审批记录：操作人、时间、意见、结果
- 流程变量变更历史追踪
- 操作记录支持时间线查询

### 6. 推送待办已办消息
- 待办任务到达时自动推送消息给审批人
- 任务完成后推送消息给相关方
- 消息类型：待办提醒、已办通知、流程完成通知
- 待办/已办列表查询 API

### 7. 组织架构与权限（新增）
- **部门管理**：部门树形结构 CRUD
- **岗位管理**：岗位 CRUD，关联部门
- **角色管理**：角色 CRUD，角色关联菜单权限
- **用户管理**：用户 CRUD，关联部门、岗位、角色
- **菜单权限**：菜单树，通过角色控制页面/按钮级权限
- **登录认证**：JWT + Spring Security 登录
- 审批节点可配置：按角色审批、按岗位审批、按指定人审批、按表单变量动态审批

---

## 技术选型

| 层面 | 选型 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 3.x | Java 17 |
| 流程引擎 | Flowable 7.x | 最新稳定版 |
| 数据库 | MySQL 8.x | 用户指定 |
| ORM | MyBatis-Plus 3.5+ | 业务表 CRUD |
| 认证 | Spring Security + JWT | 登录态管理 |
| API 文档 | SpringDoc / Knife4j | Swagger UI |
| 前端框架 | Vue 3 + Vite + TypeScript | 用户指定 |
| 流程设计器 | bpmn.js | BPMN 2.0 可视化编辑 |
| UI 组件库 | Element Plus | 快速构建界面 |
| 前端路由 | Vue Router 4 | 菜单权限路由 |
| 前端状态 | Pinia | 状态管理 |
| HTTP 客户端 | Axios | 前后端通信 |

## 后端模块设计

```
flowable-demo
├── src/main/java/org/example
│   ├── FlowableDemoApplication.java
│   ├── config/
│   │   ├── FlowableConfig.java              # Flowable 引擎配置
│   │   ├── SecurityConfig.java              # Spring Security + JWT 配置
│   │   └── CorsConfig.java                  # 跨域配置
│   ├── controller/
│   │   ├── AuthController.java              # 登录/登出
│   │   ├── UserController.java              # 用户管理
│   │   ├── DeptController.java              # 部门管理
│   │   ├── PostController.java              # 岗位管理
│   │   ├── RoleController.java              # 角色管理
│   │   ├── MenuController.java              # 菜单管理
│   │   ├── ProcessDefinitionController.java # 流程定义 CRUD
│   │   ├── ProcessInstanceController.java   # 流程实例管理
│   │   ├── TaskController.java              # 任务管理（待办/已办）
│   │   └── MessageController.java           # 消息查询
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── DeptService.java
│   │   ├── PostService.java
│   │   ├── RoleService.java
│   │   ├── MenuService.java
│   │   ├── ProcessDefinitionService.java
│   │   ├── ProcessInstanceService.java
│   │   ├── TaskService.java
│   │   └── MessageService.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java                    # 用户
│   │   │   ├── Dept.java                    # 部门
│   │   │   ├── Post.java                    # 岗位
│   │   │   ├── Role.java                    # 角色
│   │   │   ├── Menu.java                    # 菜单
│   │   │   ├── UserRole.java                # 用户-角色关联
│   │   │   ├── RoleMenu.java                # 角色-菜单关联
│   │   │   ├── LeaveRequest.java            # 请假业务
│   │   │   ├── ContractRequest.java         # 合同签订业务
│   │   │   ├── ApprovalRecord.java          # 审批留痕
│   │   │   └── NotificationMessage.java     # 消息
│   │   ├── dto/                              # 数据传输对象
│   │   └── vo/                               # 视图对象
│   ├── repository/                           # MyBatis-Plus Mapper
│   ├── listener/
│   │   ├── ProcessLifecycleListener.java    # 流程生命周期监听
│   │   └── TaskEventListener.java           # 任务事件监听（推送消息+留痕）
│   ├── security/
│   │   ├── JwtTokenProvider.java            # JWT 工具
│   │   ├── JwtAuthenticationFilter.java     # JWT 过滤器
│   │   └── UserDetailsServiceImpl.java      # 用户详情加载
│   └── util/
│       └── SecurityUtils.java               # 安全工具类
├── src/main/resources/
│   ├── application.yml
│   ├── sql/
│   │   └── init-data.sql                    # 初始化数据脚本
│   └── processes/                           # BPMN 流程定义文件
│       ├── leave-process.bpmn20.xml         # 请假流程
│       └── contract-process.bpmn20.xml      # 合同签订流程
└── pom.xml
```

## 前端模块设计

```
flowable-demo-frontend/
├── src/
│   ├── views/
│   │   ├── login/                           # 登录页
│   │   ├── dashboard/                       # 工作台首页
│   │   ├── system/
│   │   │   ├── user/                        # 用户管理
│   │   │   ├── dept/                        # 部门管理
│   │   │   ├── post/                        # 岗位管理
│   │   │   ├── role/                        # 角色管理
│   │   │   └── menu/                        # 菜单管理
│   │   ├── process/
│   │   │   ├── definition/                  # 流程定义管理
│   │   │   │   ├── list.vue                 # 流程列表
│   │   │   │   └── editor.vue               # 流程编辑器（bpmn.js 画布）
│   │   │   └── instance/                    # 流程实例管理
│   │   │       ├── start.vue                # 发起流程（含业务表单）
│   │   │       ├── list.vue                 # 流程实例列表
│   │   │       └── detail.vue               # 流程实例详情（含流程图追踪+留痕）
│   │   ├── task/
│   │   │   ├── todo.vue                     # 待办任务
│   │   │   └── done.vue                     # 已办任务
│   │   └── message/
│   │       └── list.vue                     # 消息列表
│   ├── api/                                 # API 请求封装
│   ├── router/                              # 路由配置
│   ├── store/                               # Pinia 状态管理
│   ├── components/                          # 公共组件
│   │   ├── BpmnDesigner.vue                 # bpmn.js 设计器封装
│   │   ├── BpmnViewer.vue                   # bpmn.js 查看器（流程追踪）
│   │   └── ProcessHistory.vue               # 流程留痕时间线
│   └── utils/
│       └── request.js                       # Axios 封装
├── package.json
└── vite.config.ts
```

## REST API 设计

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录，返回 JWT |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/user-info` | 获取当前用户信息（含菜单权限） |

### 组织管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/users` | 用户 CRUD |
| GET/POST/PUT/DELETE | `/api/depts` | 部门 CRUD（树形） |
| GET/POST/PUT/DELETE | `/api/posts` | 岗位 CRUD |
| GET/POST/PUT/DELETE | `/api/roles` | 角色 CRUD |
| GET/POST/PUT/DELETE | `/api/menus` | 菜单 CRUD（树形） |
| GET | `/api/roles/{id}/menus` | 获取角色菜单权限 |
| PUT | `/api/roles/{id}/menus` | 分配角色菜单权限 |

### 流程定义
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/process-definitions` | 导入/新建流程定义 |
| PUT | `/api/process-definitions/{id}` | 编辑流程定义 |
| GET | `/api/process-definitions` | 查询流程定义列表 |
| GET | `/api/process-definitions/{id}` | 查看流程定义详情（含 BPMN XML） |
| GET | `/api/process-definitions/{id}/versions` | 查看版本历史 |
| POST | `/api/process-definitions/{id}/publish` | 发布新版本 |
| POST | `/api/process-definitions/{id}/suspend` | 挂起 |
| POST | `/api/process-definitions/{id}/activate` | 激活 |

### 流程实例
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/process-instances` | 启动流程（绑定业务数据+表单变量） |
| GET | `/api/process-instances` | 查询流程实例列表 |
| GET | `/api/process-instances/{id}` | 查看流程实例详情 |
| GET | `/api/process-instances/{id}/diagram` | 获取流程追踪图（高亮当前节点） |
| GET | `/api/process-instances/{id}/history` | 查看留痕/审批记录 |
| DELETE | `/api/process-instances/{id}` | 终止流程实例 |

### 任务
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/tasks/todo` | 查询待办任务 |
| GET | `/api/tasks/done` | 查询已办任务 |
| POST | `/api/tasks/{id}/complete` | 完成任务（含审批意见） |
| POST | `/api/tasks/{id}/delegate` | 转办 |
| POST | `/api/tasks/{id}/add-sign` | 加签 |

### 消息
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/messages` | 查询消息列表 |
| PUT | `/api/messages/{id}/read` | 标记已读 |
| GET | `/api/messages/unread-count` | 未读消息数 |

## 业务流程设计

### 流程一：请假流程
- **节点**：员工提交请假申请 → 部门经理审批 → 人事审批 → 归档
- **条件**：请假天数 ≤ 3 天跳过人事审批
- **表单字段**：申请人、请假类型、开始日期、结束日期、天数、原因
- **审批人配置**：部门经理 = 发起人所在部门负责人（岗位变量），人事 = 人事角色成员

### 流程二：合同签订流程
- **节点**：发起人填写合同 → 乙方审批 → 指定人审批 → 甲方审批 → 归档
- **表单字段**：合同名称、甲方（选择用户）、乙方（选择用户）、合同金额、合同内容、签订日期
- **审批人配置**：乙方 = 表单字段 `${partyB}`，指定人 = 表单字段 `${designatedApprover}`，甲方 = 表单字段 `${partyA}`

## 数据库业务表设计（Flowable 自带表除外）

### sys_user（用户）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（登录用） |
| password | VARCHAR(255) | 密码（BCrypt 加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| dept_id | BIGINT | 所属部门 ID |
| post_id | BIGINT | 岗位 ID |
| status | TINYINT | 状态（0禁用 1启用） |
| create_time | DATETIME | 创建时间 |

### sys_dept（部门）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 部门名称 |
| parent_id | BIGINT | 父部门 ID |
| sort | INT | 排序 |
| leader | VARCHAR(50) | 负责人 |
| status | TINYINT | 状态 |

### sys_post（岗位）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 岗位名称 |
| code | VARCHAR(50) | 岗位编码 |
| sort | INT | 排序 |
| status | TINYINT | 状态 |

### sys_role（角色）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 角色名称 |
| code | VARCHAR(50) | 角色编码 |
| sort | INT | 排序 |
| status | TINYINT | 状态 |

### sys_menu（菜单）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 菜单名称 |
| parent_id | BIGINT | 父菜单 ID |
| type | VARCHAR(20) | 类型（DIRECTORY/MENU/BUTTON） |
| path | VARCHAR(200) | 路由路径 |
| component | VARCHAR(200) | 前端组件路径 |
| icon | VARCHAR(50) | 图标 |
| permission | VARCHAR(100) | 权限标识 |
| sort | INT | 排序 |
| visible | TINYINT | 是否可见 |

### sys_user_role（用户-角色关联）
| 字段 | 类型 |
|------|------|
| user_id | BIGINT |
| role_id | BIGINT |

### sys_role_menu（角色-菜单关联）
| 字段 | 类型 |
|------|------|
| role_id | BIGINT |
| menu_id | BIGINT |

### biz_leave（请假业务）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| process_instance_id | VARCHAR(64) | 流程实例 ID |
| applicant | VARCHAR(50) | 申请人 |
| leave_type | VARCHAR(20) | 请假类型（年假/事假/病假） |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| days | INT | 天数 |
| reason | TEXT | 原因 |
| status | VARCHAR(20) | 状态 |

### biz_contract（合同签订业务）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| process_instance_id | VARCHAR(64) | 流程实例 ID |
| contract_name | VARCHAR(200) | 合同名称 |
| party_a | VARCHAR(50) | 甲方用户 ID |
| party_b | VARCHAR(50) | 乙方用户 ID |
| designated_approver | VARCHAR(50) | 指定审批人用户 ID |
| amount | DECIMAL(15,2) | 合同金额 |
| content | TEXT | 合同内容 |
| sign_date | DATE | 签订日期 |
| status | VARCHAR(20) | 状态 |

### biz_approval_record（审批留痕）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| process_instance_id | VARCHAR(64) | 流程实例 ID |
| task_id | VARCHAR(64) | 任务 ID |
| task_name | VARCHAR(100) | 任务名称 |
| assignee | VARCHAR(50) | 审批人 |
| action | VARCHAR(20) | 操作（APPROVE/REJECT/DELEGATE/ADD_SIGN） |
| comment | TEXT | 审批意见 |
| start_time | DATETIME | 任务到达时间 |
| end_time | DATETIME | 任务完成时间 |
| duration_ms | BIGINT | 耗时（毫秒） |

### biz_notification_message（消息通知）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 接收人用户 ID |
| type | VARCHAR(20) | 类型（TODO/DONE/NOTIFICATION） |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容 |
| related_task_id | VARCHAR(64) | 关联任务 ID |
| related_instance_id | VARCHAR(64) | 关联流程实例 ID |
| is_read | TINYINT | 是否已读 |
| create_time | DATETIME | 创建时间 |

## 实施步骤

### 第一阶段：后端基础环境搭建
1. 配置 pom.xml（Spring Boot 3 + Flowable 7 + MySQL + MyBatis-Plus + Spring Security + JWT）
2. 创建启动类 + application.yml
3. 创建 FlowableConfig、SecurityConfig、CorsConfig
4. 初始化 MySQL 数据库，验证 Flowable 自动建表

### 第二阶段：组织架构与权限
1. 建表 + 实体类（User/Dept/Post/Role/Menu + 关联表）
2. MyBatis-Plus Mapper + Service + Controller（CRUD）
3. Spring Security + JWT 登录认证
4. 菜单权限树 + 角色授权
5. 初始化数据脚本（部门、岗位、角色、菜单、测试用户）

### 第三阶段：流程定义管理
1. 编写请假流程 + 合同签订流程 BPMN 文件
2. 流程定义导入/查询/版本管理 API
3. 发布/挂起/激活控制

### 第四阶段：业务数据绑定与流程实例
1. 业务表建表（biz_leave、biz_contract）
2. 流程启动 API（绑定 businessKey + 表单变量）
3. 流程实例查询/详情/终止

### 第五阶段：审批留痕
1. biz_approval_record 建表
2. Flowable 事件监听器（TaskEventListener）
3. 审批记录自动写入 + 查询 API

### 第六阶段：任务管理
1. 待办/已办查询 API
2. 任务完成/转办/加签
3. 审批意见记录

### 第七阶段：消息推送
1. biz_notification_message 建表
2. 任务事件监听 → 自动生成消息
3. 消息查询/标记已读/未读数

### 第八阶段：Vue3 前端搭建
1. Vite + Vue3 + Element Plus 脚手架
2. 登录页 + 动态路由 + 菜单渲染
3. 系统管理页面（用户/部门/岗位/角色/菜单）
4. 流程定义列表 + bpmn.js 编辑器集成
5. 流程发起页（含业务表单） + 实例列表 + 实例详情（流程图追踪 + 留痕时间线）
6. 待办/已办任务页 + 审批处理弹窗
7. 消息通知页
8. 工作台首页（待办概览 + 消息概览）


---

## 关键技术决策

| 决策点 | 方案 | 理由 |
|--------|------|------|
| 审批人分配 | BPMN表达式 `${assigneeResolver.resolveByXxx()}` + Spring Bean | 可可视化配置，无需改 BPMN 源码即可调整审批规则 |
| 流程留痕 | 全局 FlowableEventListener，`getOnTransaction=committed` | 解耦业务代码，失败不影响主流程事务 |
| 消息推送 | 数据库消息表 + 前端轮询（15s） | Demo 规模不需要 WebSocket 复杂度 |
| 版本控制 | Flowable Model 机制 + 手动管理 deploy | 兼容 Flowable API，实例自动绑定启动时版本 |
| 前端路由 | 动态注册 addRoute，后端返回菜单树 | 权限变更无需重新打包部署 |
| bpmn.js 集成 | `markRaw()` 包裹 Modeler 实例 | 防止 Vue3 响应式代理破坏 bpmn.js 拖拽功能 |
| JWT 存储 | localStorage，Bearer Header 传输 | 前后端分离标准方案 |
| ID 生成器 | StrongUuidGenerator | 128 位 UUID，全局唯一，避免集群冲突 |
| 中文乱码 | `activityFontName="宋体"` | Flowable 流程图渲染中文必须设为中文字体 |
| 权限模型 | RBAC + 菜单树 + 按钮权限标识 `v-permission` | 与 Element Plus 菜单体系天然匹配 |

## 审批人动态分配架构

```
BPMN UserTask
  assignee="${assigneeResolver.resolveByRole('dept_manager', execution)}"
       │
       ▼
AssigneeResolver (门面 Spring Bean)
  ├── RoleBasedAssigneeHandler     → 按角色编码查用户表
  ├── PostBasedAssigneeHandler     → 按岗位编码查用户表
  ├── FixedAssigneeHandler         → 按指定用户 ID
  └── VariableBasedAssigneeHandler → 按表单字段变量（如 partyA）
       │
       ▼
  返回审批人用户名 → Flowable 设置任务办理人
```

## Flowable 事件监听横切设计

```
BPMN 流程运行
  ├── TASK_CREATED  → GlobalTaskCreateListener    → 写 biz_notification_message (待办推送)
  ├── TASK_COMPLETED → GlobalTaskCompleteListener  → 写 biz_approval_record (留痕)
  │                  │                              → 写 biz_notification_message (已办通知)
  └── PROCESS_COMPLETED → GlobalProcessCompleteListener → 更新业务表状态 + 完成通知
```

## 前端路由守卫与菜单加载流程

```
登录成功
  → 存储 token
  → GET /api/auth/user-info → { user, menus:[], permissions:[] }
  → Pinia store 存储 permissions 数组
  → 递归 menus 树，type!=BUTTON 的节点转为 RouteRecordRaw
  → router.addRoute() 动态注册
  → 侧边栏渲染基于 route.meta
  → 按钮权限通过 v-permission 指令控制
```

## 关键代码骨架（实施时参考）

### FlowableConfig

```java
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        config.setIdGenerator(new StrongUuidGenerator());
        config.setActivityFontName("宋体");
        config.setLabelFontName("宋体");
        config.setAnnotationFontName("宋体");
        config.setAsyncHistoryEnabled(true);
        config.setAsyncHistoryExecutorActivate(true);
    }
}
```

### SecurityConfig（启用方法级权限注解）

```java
@Configuration
@EnableMethodSecurity  // Spring Security 6 新注解
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/doc.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

### Controller 权限注解示例

```java
@PreAuthorize("hasAuthority('system:user:list')")
@GetMapping("/api/users")
public Result<PageResult<UserVO>> list(...) { }

@PreAuthorize("hasAuthority('system:user:add')")
@PostMapping("/api/users")
public Result<Void> add(@Validated @RequestBody UserDTO dto) { }
```

### 前端 BpmnDesigner 关键注意

```typescript
// Vue3 必须用 markRaw() 防止响应式代理破坏 bpmn.js 拖拽
import { markRaw } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'

const modeler = markRaw(new BpmnModeler({ container: containerRef.value! }))
```

## 实施顺序与依赖

```
第一阶段: 后端骨架 (pom.xml + application.yml + Config 类 + 统一响应)
    ↓
第二阶段: 组织架构 (7张sys表 + Mapper + Service + Controller + JWT 登录)
    ↓
第三阶段: 流程定义 (BPMN 文件 + AssigneeResolver + 定义管理 API)
    ↓
第四阶段: 流程实例 (biz_leave/biz_contract 表 + 启动/查询/终止 API)
    ↓
第五阶段: 留痕+消息 (biz_approval_record/biz_notification_message 表 + 事件监听器)
    ↓
第六阶段: 任务管理 (待办/已办 + 完成/转办/加签)
    ↓
第七阶段: Vue3 前端 (登录→动态路由→系统管理→流程管理→任务→消息→工作台)
```

1. 启动后端，数据库自动建表
2. 执行初始化脚本，创建测试用户（admin/张三/李四/王五等）
3. 用 admin 登录，创建部门、岗位、角色，分配权限和用户
4. 通过 API 导入请假流程和合同签订流程 BPMN
5. 发布流程定义，验证版本号递增
6. 前端发起请假流程：填写表单 → 审批人收到待办 → 审批 → 留痕记录生成
7. 前端发起合同签订流程：选择甲乙方 → 乙方审批 → 指定人审批 → 甲方审批 → 归档
8. 验证待办/已办消息推送
9. 验证流程图追踪（高亮当前节点）和留痕时间线
10. 验证角色权限控制（不同角色看到不同菜单）
