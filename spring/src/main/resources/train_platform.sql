CREATE DATABASE IF NOT EXISTS train_platform;
USE train_platform;

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50)  NOT NULL COMMENT '学号/工号',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    real_name VARCHAR(50)  NOT NULL COMMENT '姓名',
    role VARCHAR(20)  NOT NULL DEFAULT 'student' COMMENT '角色: admin/teacher/student',
    class_name  VARCHAR(50)  DEFAULT NULL COMMENT '班级',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) COMMENT='用户表';

CREATE TABLE IF NOT EXISTS train_task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL COMMENT '任务标题',
    knowledge_point VARCHAR(100) DEFAULT NULL COMMENT '所属知识点',
    content TEXT COMMENT '任务详情',
    attachment_url VARCHAR(255) DEFAULT NULL COMMENT '任务附件',
    class_name VARCHAR(50)  DEFAULT NULL COMMENT '目标班级(NULL=全部班级)',
    deadline DATETIME DEFAULT NULL COMMENT '截止时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-未开始 1-进行中 2-已截止',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_publisher (publisher_id),
    KEY idx_status_deadline (status, deadline),
    CONSTRAINT fk_task_publisher FOREIGN KEY (publisher_id) REFERENCES sys_user (id)
) COMMENT='实训任务表';

CREATE TABLE IF NOT EXISTS task_submission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL COMMENT '任务ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    content TEXT COMMENT '作业文本',
    attachment_url VARCHAR(255) DEFAULT NULL COMMENT '附件路径',
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    score DECIMAL(5,1) DEFAULT NULL COMMENT '分数(NULL=未批阅)',
    comment VARCHAR(500) DEFAULT NULL COMMENT '评语',
    reviewer_id BIGINT DEFAULT NULL COMMENT '批阅人ID',
    review_time DATETIME DEFAULT NULL COMMENT '批阅时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待批阅 1-已批阅',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_student (task_id, student_id),
    KEY idx_student (student_id),
    CONSTRAINT fk_sub_task    FOREIGN KEY (task_id)    REFERENCES train_task (id),
    CONSTRAINT fk_sub_student FOREIGN KEY (student_id) REFERENCES sys_user (id)
) COMMENT='作业提交表';

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operation VARCHAR(100) DEFAULT NULL COMMENT '操作描述',
    method VARCHAR(255) DEFAULT NULL COMMENT '请求方式+路径',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50)  DEFAULT NULL COMMENT 'IP',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_create_time (create_time)
) COMMENT='操作日志表';

-- 初始管理员：admin / admin123（BCrypt 密文，可用 BCryptPasswordEncoder 校验）
INSERT INTO sys_user (username, password, real_name, role)
VALUES ('admin', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '系统管理员', 'admin');

-- ============================================================
-- 测试数据（重复执行前请先清空各表并重置自增）
-- 密码统一为 admin123 的 BCrypt 密文
-- id 规划：1=admin, 2~4=教师, 5~24=软件2301, 25~44=软件2302, 45~64=软件2303
-- ============================================================

-- 教师 3 名
INSERT INTO sys_user (username, password, real_name, role, class_name, email, status) VALUES
('T001', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '李老师', 'teacher', NULL, 'li@example.com',   1),
('T002', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '王老师', 'teacher', NULL, 'wang@example.com', 1),
('T003', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '陈老师', 'teacher', NULL, 'chen@example.com', 1);

-- 软件2301 班 20 人（id 5~24）
INSERT INTO sys_user (username, password, real_name, role, class_name, email, status) VALUES
('2023001', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '陈明',  'student', '软件2301', '2023001@example.com', 1),
('2023002', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '林峰',  'student', '软件2301', '2023002@example.com', 1),
('2023003', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '赵强',  'student', '软件2301', '2023003@example.com', 1),
('2023004', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '周杰',  'student', '软件2301', '2023004@example.com', 1),
('2023005', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '吴昊',  'student', '软件2301', '2023005@example.com', 1),
('2023006', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '郑涛',  'student', '软件2301', '2023006@example.com', 1),
('2023007', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '孙伟',  'student', '软件2301', '2023007@example.com', 1),
('2023008', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '钱坤',  'student', '软件2301', '2023008@example.com', 1),
('2023009', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '杨帆',  'student', '软件2301', '2023009@example.com', 1),
('2023010', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '朱辰',  'student', '软件2301', '2023010@example.com', 1),
('2023011', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '秦朗',  'student', '软件2301', '2023011@example.com', 1),
('2023012', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '尤洋',  'student', '软件2301', '2023012@example.com', 1),
('2023013', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '许睿',  'student', '软件2301', '2023013@example.com', 1),
('2023014', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '何宇',  'student', '软件2301', '2023014@example.com', 1),
('2023015', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '吕航',  'student', '软件2301', '2023015@example.com', 1),
('2023016', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '施远',  'student', '软件2301', '2023016@example.com', 1),
('2023017', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '张恒',  'student', '软件2301', '2023017@example.com', 1),
('2023018', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '孔辉',  'student', '软件2301', '2023018@example.com', 1),
('2023019', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '曹阳',  'student', '软件2301', '2023019@example.com', 1),
('2023020', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '严旭',  'student', '软件2301', '2023020@example.com', 1);

-- 软件2302 班 20 人（id 25~44）
INSERT INTO sys_user (username, password, real_name, role, class_name, email, status) VALUES
('2023101', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '刘畅',  'student', '软件2302', '2023101@example.com', 1),
('2023102', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '李然',  'student', '软件2302', '2023102@example.com', 1),
('2023103', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '王毅',  'student', '软件2302', '2023103@example.com', 1),
('2023104', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '赵雷',  'student', '软件2302', '2023104@example.com', 1),
('2023105', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '陈默',  'student', '软件2302', '2023105@example.com', 1),
('2023106', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '林夕',  'student', '软件2302', '2023106@example.com', 1),
('2023107', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '周云',  'student', '软件2302', '2023107@example.com', 1),
('2023108', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '吴霜',  'student', '软件2302', '2023108@example.com', 1),
('2023109', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '郑晨',  'student', '软件2302', '2023109@example.com', 1),
('2023110', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '孙皓',  'student', '软件2302', '2023110@example.com', 1),
('2023111', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '钱途',  'student', '软件2302', '2023111@example.com', 1),
('2023112', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '杨逍',  'student', '软件2302', '2023112@example.com', 1),
('2023113', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '朱迪',  'student', '软件2302', '2023113@example.com', 1),
('2023114', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '秦风',  'student', '软件2302', '2023114@example.com', 1),
('2023115', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '于谦',  'student', '软件2302', '2023115@example.com', 1),
('2023116', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '罗翔',  'student', '软件2302', '2023116@example.com', 1),
('2023117', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '彭博',  'student', '软件2302', '2023117@example.com', 1),
('2023118', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '韩寒',  'student', '软件2302', '2023118@example.com', 1),
('2023119', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '沈腾',  'student', '软件2302', '2023119@example.com', 1),
('2023120', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '黄渤',  'student', '软件2302', '2023120@example.com', 1);

-- 软件2303 班 20 人（id 45~64）
INSERT INTO sys_user (username, password, real_name, role, class_name, email, status) VALUES
('2023201', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '刘星',  'student', '软件2303', '2023201@example.com', 1),
('2023202', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '李想',  'student', '软件2303', '2023202@example.com', 1),
('2023203', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '王凯',  'student', '软件2303', '2023203@example.com', 1),
('2023204', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '赵云',  'student', '软件2303', '2023204@example.com', 1),
('2023205', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '陈晨',  'student', '软件2303', '2023205@example.com', 1),
('2023206', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '林涛',  'student', '软件2303', '2023206@example.com', 1),
('2023207', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '周正',  'student', '软件2303', '2023207@example.com', 1),
('2023208', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '吴迪',  'student', '软件2303', '2023208@example.com', 1),
('2023209', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '郑凯',  'student', '软件2303', '2023209@example.com', 1),
('2023210', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '孙凡',  'student', '软件2303', '2023210@example.com', 1),
('2023211', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '钱龙',  'student', '软件2303', '2023211@example.com', 1),
('2023212', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '杨洋',  'student', '软件2303', '2023212@example.com', 1),
('2023213', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '朱珠',  'student', '软件2303', '2023213@example.com', 1),
('2023214', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '秦浩',  'student', '软件2303', '2023214@example.com', 1),
('2023215', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '许松',  'student', '软件2303', '2023215@example.com', 1),
('2023216', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '何炯',  'student', '软件2303', '2023216@example.com', 1),
('2023217', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '吕品',  'student', '软件2303', '2023217@example.com', 1),
('2023218', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '施宁',  'student', '软件2303', '2023218@example.com', 1),
('2023219', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '孔捷',  'student', '软件2303', '2023219@example.com', 1),
('2023220', '$2b$10$M57meXkjfqsdTl.XmMZpouUw.CGNrrN/iHDlKMxupD2qXcVVmkdky', '曹敏',  'student', '软件2303', '2023220@example.com', 1);

-- 实训任务（class_name=NULL 表示全部班级可见）
-- status: 0-未开始(草稿) 1-进行中 2-已截止
INSERT INTO train_task (title, knowledge_point, content, class_name, deadline, status, publisher_id, create_time) VALUES
('Vue3 组件化实训',          'Vue3 组件通信',   '完成登录页与任务列表页开发，要求使用 setup 语法糖与 props/emit。', '软件2301', '2026-11-01 23:59:59', 1, 2, '2026-10-15 09:00:00'),
('MyBatis-Plus 分页查询',    'ORM 与分页插件',  '基于 MyBatis-Plus 实现任务分页接口，要求支持关键字模糊查询。',     NULL,       '2026-10-30 23:59:59', 1, 2, '2026-10-14 10:00:00'),
('RESTful 接口设计',         'HTTP 与接口规范', '为作业提交模块设计 RESTful 接口，撰写接口文档并用 Postman 自测。', '软件2302', '2026-10-10 23:59:59', 2, 3, '2026-10-01 08:30:00'),
('JWT 登录认证实现',         '无状态认证',      '实现登录签发 Token、拦截器校验 Token、401 统一返回。',             '软件2303', '2026-10-08 23:59:59', 2, 4, '2026-09-28 14:00:00'),
('数据库表结构设计(草稿)',    'MySQL 建模',      '根据需求画出 ER 图并给出建表 SQL（草稿，暂不发布）。',              '软件2301', NULL,                  0, 2, '2026-10-16 16:00:00'),
('Spring Boot 全局异常处理', '异常与统一返回',  '自定义 BizException 与 @RestControllerAdvice 全局异常处理器。',    '软件2302', '2026-10-25 23:59:59', 1, 3, '2026-10-10 14:00:00');

-- 作业提交：学生 5=陈明(2301) 6=林峰(2301) 25=刘畅(2302) 26=李然(2302) 45=刘星(2303)
INSERT INTO task_submission (task_id, student_id, content, attachment_url, submit_time, score, comment, reviewer_id, review_time, status) VALUES
(1, 5,  'Vue3 登录页已完成，列表页待完善。',     '/upload/2026/10/task1-chenming.zip', '2026-10-16 20:00:00', NULL, NULL,                          NULL, NULL,               0),
(1, 6,  '两个页面全部完成，组件已拆分。',         '/upload/2026/10/task1-linfeng.zip',  '2026-10-17 09:10:00', NULL, NULL,                          NULL, NULL,               0),
(3, 25, 'RESTful 接口文档见附件。',              '/upload/2026/10/task3-liuchang.zip', '2026-10-09 19:30:00', 90.0, '接口规范清晰，建议补充幂等处理', 3,    '2026-10-10 10:00:00', 1),
(3, 26, '接口设计已完成并自测。',                NULL,                                '2026-10-10 14:20:00', 76.0, '缺少统一错误码说明',             3,    '2026-10-11 09:00:00', 1),
(4, 45, 'JWT 拦截器已实现，Token 刷新未处理。',   NULL,                                '2026-10-07 21:00:00', 82.0, '基础功能完整，缺少刷新机制',       4,    '2026-10-08 09:30:00', 1),
(6, 25, '全局异常处理器编写完成。',              NULL,                                '2026-10-17 10:20:00', NULL, NULL,                          NULL, NULL,               0),
(6, 26, '自定义异常与全局处理器均已实现。',       '/upload/2026/10/task6-liran.zip',   '2026-10-17 11:00:00', NULL, NULL,                          NULL, NULL,               0);

-- 补充提交数据：已批阅 10 条 + 待批阅 9 条（分数覆盖 90+/80-89/70-79/60-69/60- 各分数段）
-- 学生 id：5~24=软件2301, 25~44=软件2302, 45~64=软件2303
INSERT INTO task_submission (task_id, student_id, content, attachment_url, submit_time, score, comment, reviewer_id, review_time, status) VALUES
-- 任务1（软件2301, 批阅人李老师 id=2）
(1, 7,  '页面完成，组件通信已实现。',             NULL,                                '2026-10-16 21:00:00', 95.0, '完成度非常高，代码结构清晰',   2, '2026-10-17 09:00:00', 1),
(1, 8,  '登录页和列表页已完成。',                 '/upload/2026/10/task1-sunwei.zip',   '2026-10-17 08:30:00', 85.5, '功能完整，样式可再优化',       2, '2026-10-17 10:00:00', 1),
(1, 9,  '基本完成，请老师批阅。',                 NULL,                                '2026-10-17 09:40:00', NULL, NULL,                          NULL, NULL,               0),
(1, 10, '按要求完成两页开发。',                   '/upload/2026/10/task1-zhuchen.zip',  '2026-10-17 10:05:00', NULL, NULL,                          NULL, NULL,               0),
(1, 11, '页面已完成，部分交互待完善。',            NULL,                                '2026-10-17 11:20:00', 72.0, 'props 使用有误，已指出',       2, '2026-10-17 14:00:00', 1),
-- 任务2（全部班级, 批阅人李老师 id=2）
(2, 5,  '分页接口已实现，支持模糊查询。',          '/upload/2026/10/task2-chenming.zip', '2026-10-16 18:00:00', 91.0, '分页插件使用正确',             2, '2026-10-17 09:30:00', 1),
(2, 6,  '分页已实现，关键字查询未完成。',          NULL,                                '2026-10-17 09:00:00', 68.0, '缺少关键字模糊查询',           2, '2026-10-17 10:30:00', 1),
(2, 7,  '只完成了基础分页。',                    NULL,                                '2026-10-17 10:10:00', 55.0, '未达到基本要求，请补充',       2, '2026-10-17 15:00:00', 1),
(2, 25, '分页与模糊查询均完成。',                 NULL,                                '2026-10-17 11:00:00', NULL, NULL,                          NULL, NULL,               0),
(2, 45, '接口完成并自测通过。',                  '/upload/2026/10/task2-liuxing.zip',  '2026-10-17 11:30:00', 88.0, '整体不错，注意参数校验',       2, '2026-10-17 15:30:00', 1),
(2, 46, '分页接口已完成。',                      NULL,                                '2026-10-17 12:00:00', NULL, NULL,                          NULL, NULL,               0),
-- 任务3（软件2302, 批阅人王老师 id=3）
(3, 27, 'RESTful 接口设计完成。',                NULL,                                '2026-10-10 16:00:00', 87.0, '规范清晰',                    3, '2026-10-11 10:00:00', 1),
(3, 28, '接口文档已完成。',                      '/upload/2026/10/task3-wushuang.zip', '2026-10-10 20:00:00', NULL, NULL,                          NULL, NULL,               0),
(3, 29, '设计完成，自测通过。',                  NULL,                                '2026-10-10 21:30:00', 63.5, '资源命名不规范',               3, '2026-10-11 11:00:00', 1),
-- 任务4（软件2303, 批阅人陈老师 id=4）
(4, 46, '登录与拦截器均完成。',                  '/upload/2026/10/task4-lintao.zip',   '2026-10-08 15:00:00', 93.0, '实现完整，含过期处理',         4, '2026-10-08 17:00:00', 1),
(4, 47, 'JWT 认证已实现。',                      NULL,                                '2026-10-08 18:00:00', NULL, NULL,                          NULL, NULL,               0),
(4, 48, '拦截器完成，签发待优化。',               NULL,                                '2026-10-08 20:00:00', 71.0, '未做 401 统一返回',            4, '2026-10-09 09:00:00', 1),
-- 任务6（软件2302, 待批阅）
(6, 27, '全局异常处理完成。',                    NULL,                                '2026-10-17 12:30:00', NULL, NULL,                          NULL, NULL,               0),
(6, 28, 'BizException 与处理器均完成。',          NULL,                                '2026-10-17 13:00:00', NULL, NULL,                          NULL, NULL,               0);

-- 操作日志示例
INSERT INTO operation_log (user_id, operation, method, params, ip, create_time) VALUES
(1, '管理员登录',   'POST /api/user/login',  '{"username":"admin"}',        '127.0.0.1', '2026-10-17 08:37:41'),
(2, '发布班级任务', 'POST /api/task',        '{"title":"Vue3 组件化实训"}',  '127.0.0.1', '2026-10-15 09:00:01'),
(3, '发布班级任务', 'POST /api/task',        '{"title":"RESTful 接口设计"}', '127.0.0.1', '2026-10-01 08:30:01'),
(5, '提交作业',     'POST /api/submission',  'taskId=1',                     '127.0.0.1', '2026-10-16 20:00:01');
