create table exam_item
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    name        varchar(100)                             not null comment '考试名称',
    description varchar(255)                             null comment '考试说明',
    fee         decimal(10, 2) default 0.00              not null comment '报名费 (必须用Decimal保证金额精度)',
    exam_time   datetime                                 not null comment '考试时间',
    create_time datetime       default CURRENT_TIMESTAMP null
)
    comment '考试项目表';

create table registration
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    user_id     bigint                             not null comment '关联用户ID',
    exam_id     bigint                             not null comment '关联考试ID',
    status      tinyint  default 0                 null comment '状态: 0-未支付, 1-已支付',
    score       decimal(5, 1)                      null comment '成绩 (NULL表示未出分)',
    create_time datetime default CURRENT_TIMESTAMP null comment '报名时间',
    constraint uk_user_exam
        unique (user_id, exam_id)
)
    comment '报名记录表';

create table sys_enrollment
(
    id          int auto_increment
        primary key,
    user_id     int           not null comment '用户ID',
    exam_id     int           not null comment '科目/考试ID',
    status      int default 0 null comment '状态：0-未支付，1-已支付',
    create_time datetime      null,
    score       int           null comment '考试成绩'
);

create table sys_user
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    username    varchar(50)                           not null comment '用户名/学号',
    password    varchar(100)                          not null comment '密码 (实际项目建议加密存储)',
    email       varchar(100)                          null comment '邮箱 (用于找回密码)',
    role        varchar(20) default 'student'         null comment '角色: student-学生, admin-管理员',
    create_time datetime    default CURRENT_TIMESTAMP null comment '注册时间',
    constraint uk_username
        unique (username) comment '唯一索引：防止用户名重复'
)
    comment '用户表';

