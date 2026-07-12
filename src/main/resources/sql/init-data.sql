-- ============================================================
-- Flowable Demo 初始化数据脚本
-- 密码均为 123456（BCrypt 加密）
-- ============================================================

-- 部门数据
INSERT INTO sys_dept (dept_id, parent_id, dept_name, leader, sort) VALUES
(1, NULL, '总公司', 'admin', 1),
(2, 1, '技术部', 'zhangsan', 2),
(3, 1, '人事部', 'lisi', 3),
(4, 1, '财务部', 'wangwu', 4);

-- 岗位数据
INSERT INTO sys_post (post_id, post_name, post_code, sort) VALUES
(1, '总经理', 'general_manager', 1),
(2, '部门经理', 'dept_manager', 2),
(3, '普通员工', 'staff', 3),
(4, '人事专员', 'hr_specialist', 4),
(5, '财务总监', 'finance_director', 5);

-- 角色数据
INSERT INTO sys_role (role_id, role_name, role_code, sort) VALUES
(1, '系统管理员', 'admin', 1),
(2, '部门经理', 'dept_manager', 2),
(3, '人事角色', 'hr_role', 3),
(4, '普通用户', 'user', 4);

-- 菜单数据
INSERT INTO sys_menu (menu_id, parent_id, menu_name, menu_type, path, component, icon, permission, sort, visible) VALUES
-- 一级目录
(1, NULL, '系统管理', 'DIRECTORY', '/system', NULL, 'Setting', NULL, 1, 1),
(2, NULL, '流程管理', 'DIRECTORY', '/process', NULL, 'Guide', NULL, 2, 1),
(3, NULL, '任务管理', 'DIRECTORY', '/task', NULL, 'List', NULL, 3, 1),

-- 系统管理子菜单
(11, 1, '用户管理', 'MENU', '/system/user', 'system/user/index', 'User', 'system:user:list', 1, 1),
(12, 1, '部门管理', 'MENU', '/system/dept', 'system/dept/index', 'OfficeBuilding', 'system:dept:list', 2, 1),
(13, 1, '岗位管理', 'MENU', '/system/post', 'system/post/index', 'Briefcase', 'system:post:list', 3, 1),
(14, 1, '角色管理', 'MENU', '/system/role', 'system/role/index', 'Avatar', 'system:role:list', 4, 1),
(15, 1, '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'Menu', 'system:menu:list', 5, 1),

-- 系统管理按钮
(111, 11, '新增用户', 'BUTTON', NULL, NULL, NULL, 'system:user:add', 1, 1),
(112, 11, '编辑用户', 'BUTTON', NULL, NULL, NULL, 'system:user:edit', 2, 1),
(113, 11, '删除用户', 'BUTTON', NULL, NULL, NULL, 'system:user:delete', 3, 1),
(121, 12, '新增部门', 'BUTTON', NULL, NULL, NULL, 'system:dept:add', 1, 1),
(122, 12, '编辑部门', 'BUTTON', NULL, NULL, NULL, 'system:dept:edit', 2, 1),
(123, 12, '删除部门', 'BUTTON', NULL, NULL, NULL, 'system:dept:delete', 3, 1),
(131, 13, '新增岗位', 'BUTTON', NULL, NULL, NULL, 'system:post:add', 1, 1),
(132, 13, '编辑岗位', 'BUTTON', NULL, NULL, NULL, 'system:post:edit', 2, 1),
(133, 13, '删除岗位', 'BUTTON', NULL, NULL, NULL, 'system:post:delete', 3, 1),
(141, 14, '新增角色', 'BUTTON', NULL, NULL, NULL, 'system:role:add', 1, 1),
(142, 14, '编辑角色', 'BUTTON', NULL, NULL, NULL, 'system:role:edit', 2, 1),
(143, 14, '删除角色', 'BUTTON', NULL, NULL, NULL, 'system:role:delete', 3, 1),
(151, 15, '新增菜单', 'BUTTON', NULL, NULL, NULL, 'system:menu:add', 1, 1),
(152, 15, '编辑菜单', 'BUTTON', NULL, NULL, NULL, 'system:menu:edit', 2, 1),
(153, 15, '删除菜单', 'BUTTON', NULL, NULL, NULL, 'system:menu:delete', 3, 1),

-- 流程管理子菜单
(21, 2, '流程定义', 'MENU', '/process/definition', 'process/definition/list', 'Document', 'process:definition:list', 1, 1),
(22, 2, '流程实例', 'MENU', '/process/instance', 'process/instance/list', 'Postcard', 'process:instance:list', 2, 1),
-- 流程管理按钮
(211, 21, '发布流程', 'BUTTON', NULL, NULL, NULL, 'process:definition:publish', 1, 1),
(212, 21, '挂起激活', 'BUTTON', NULL, NULL, NULL, 'process:definition:suspend', 2, 1),
(221, 22, '发起流程', 'BUTTON', NULL, NULL, NULL, 'process:instance:start', 1, 1),
(222, 22, '终止流程', 'BUTTON', NULL, NULL, NULL, 'process:instance:delete', 2, 1),

-- 任务管理子菜单
(31, 3, '待办任务', 'MENU', '/task/todo', 'task/todo/index', 'Clock', 'task:todo:list', 1, 1),
(32, 3, '已办任务', 'MENU', '/task/done', 'task/done/index', 'Check', 'task:done:list', 2, 1),
(33, 3, '消息通知', 'MENU', '/message', 'message/list', 'Bell', 'message:list', 3, 1);

-- 用户数据（密码: 123456）
-- BCrypt($2a$10$...) 在线生成
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, dept_id, post_id, status) VALUES
(1, 'admin', '$2a$10$WJbwWCYtclfRzYXeVlBSvufITGx8bFiUOwHFJBwGB0IomBkK5JYke', '系统管理员', 'admin@example.com', '13800000001', 1, 1, 1),
(2, 'zhangsan', '$2a$10$WJbwWCYtclfRzYXeVlBSvufITGx8bFiUOwHFJBwGB0IomBkK5JYke', '张三', 'zhangsan@example.com', '13800000002', 2, 2, 1),
(3, 'lisi', '$2a$10$WJbwWCYtclfRzYXeVlBSvufITGx8bFiUOwHFJBwGB0IomBkK5JYke', '李四', 'lisi@example.com', '13800000003', 3, 4, 1),
(4, 'wangwu', '$2a$10$WJbwWCYtclfRzYXeVlBSvufITGx8bFiUOwHFJBwGB0IomBkK5JYke', '王五', 'wangwu@example.com', '13800000004', 4, 5, 1),
(5, 'zhaoliu', '$2a$10$WJbwWCYtclfRzYXeVlBSvufITGx8bFiUOwHFJBwGB0IomBkK5JYke', '赵六', 'zhaoliu@example.com', '13800000005', 2, 3, 1);

-- 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 2),
(5, 4);

-- 角色-菜单关联（admin 拥有全部权限）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- 部门经理角色的菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 2), (2, 21), (2, 22), (2, 221),  -- 流程管理
(2, 3), (2, 31), (2, 32), (2, 33);   -- 任务管理

-- 人事角色的菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 2), (3, 21), (3, 22), (3, 221),  -- 流程管理
(3, 3), (3, 31), (3, 32), (3, 33);   -- 任务管理

-- 普通用户的菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 2), (4, 22), (4, 221),  -- 流程管理（只能发起和查看实例）
(4, 3), (4, 31), (4, 32), (4, 33);  -- 任务管理
