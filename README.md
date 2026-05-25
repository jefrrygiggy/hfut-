# 考试报名管理系统

合肥工业大学计算机科学与技术 — 计网课设项目。一个基于 **Vue 3 + Spring Boot** 的考试报名与成绩管理系统，支持支付宝沙箱支付。

## 功能概览

### 学生端
- **用户注册/登录** — 支持邮箱验证码找回密码
- **考试浏览与报名** — 查看考试列表，通过支付宝沙箱完成缴费
- **成绩查询** — 查看已报名考试的分数

### 管理端
- **数据仪表盘** — 学生总数、总收入、各考试报名热度图表（ECharts）
- **考试管理** — 发布/删除考试项目
- **成绩管理** — 按考试录入学生成绩（0-100 分）

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 (Composition API) + Vue Router 4 + Element Plus + ECharts |
| 构建工具 | Vite 6 |
| 后端 | Spring Boot 3.4 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8 |
| 认证 | JWT |
| 支付 | 支付宝沙箱 (EasySDK) |
| 邮件 | Spring Boot Mail (QQ SMTP) |
| CI | GitHub Actions |

## 目录结构

```
├── backend/keshe/           # Spring Boot 后端
│   └── src/main/java/com/tayutadeshi/keshe/
│       ├── controller/      # API 控制器
│       ├── service/         # 业务逻辑层
│       ├── mapper/          # MyBatis-Plus Mapper
│       ├── pojo/            # 数据库实体
│       ├── config/          # 配置类 (Alipay、Web拦截器)
│       ├── interceptor/     # JWT 登录拦截器
│       ├── utils/           # JWT 工具类
│       └── common/          # 统一返回 Result
├── frontend/demo/           # Vue 3 前端
│   └── src/
│       ├── views/           # 页面组件
│       ├── layout/          # 布局组件
│       ├── router/          # 路由配置
│       └── utils/           # Axios 封装
├── sql/                     # 数据库 DDL
└── .github/workflows/       # CI 配置
```

## 快速开始

### 环境要求

- **JDK 21**
- **Maven 3.8+**（或使用项目自带的 `mvnw`）
- **Node.js 18+**
- **MySQL 8.0+**

### 1. 创建数据库

执行 `sql` 目录下的 DDL 语句建表：

```sql
-- 依次创建 sys_user、exam_item、sys_enrollment 三张表
```

### 2. 配置并启动后端

编辑 `backend/keshe/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/你的数据库名?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的密码
  mail:
    username: 你的QQ邮箱@qq.com
    password: QQ邮箱SMTP授权码

alipay:
  appId: 沙箱AppId
  privateKey: 商户私钥
  notifyUrl: 支付宝异步回调地址（需公网可达，可用 ngrok 穿透）
```

然后启动：

```bash
cd backend/keshe
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`。

### 3. 配置并启动前端

```bash
cd frontend/demo
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求自动代理到 `localhost:8080`。

### 4. 构建生产版本

```bash
# 后端
cd backend/keshe && ./mvnw clean package

# 前端
cd frontend/demo && npm run build   # 产出在 dist/ 目录
```

## API 接口

所有接口前缀 `/api`，需登录的接口在 Header 中携带 `token: <JWT>`。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/register` | 用户注册 |
| POST | `/api/user/login` | 登录，返回 JWT |
| POST | `/api/user/send-code` | 发送邮箱验证码 |
| POST | `/api/user/reset-password` | 重置密码 |
| GET | `/api/exam/list?userId=` | 考试列表（标记是否已报名） |
| POST | `/api/exam/add` | 管理员：新增考试 |
| DELETE | `/api/exam/delete/{id}` | 管理员：删除考试 |
| PUT | `/api/exam/update` | 管理员：更新考试 |
| POST | `/api/enrollment/apply` | 创建报名订单 |
| POST | `/api/enrollment/alipay/pay` | 发起支付宝支付 |
| POST | `/api/enrollment/notify` | 支付宝异步回调 |
| GET | `/api/enrollment/my-scores?userId=` | 查询我的成绩 |
| GET | `/api/admin/stats` | 仪表盘统计数据 |
| GET | `/api/admin/score/student-list?examId=` | 某考试已支付学生列表 |
| POST | `/api/admin/score/update` | 录入/更新成绩 |

## 数据库表

| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表（username、password、email、role） |
| `exam_item` | 考试项目表（name、fee、exam_time、description） |
| `sys_enrollment` | 报名/成绩表（user_id、exam_id、status、score） |

## 注意事项

- 密码存储为明文，实际部署需替换为 BCrypt 等加密方式
- 支付宝回调需要公网可达的 URL，本地开发建议使用 ngrok 进行内网穿透
- 邮件发送需开通 QQ 邮箱 SMTP 服务并获取授权码
- 项目为课设性质，代码质量和安全性不作为生产参考

## License

[Apache 2.0](LICENSE)
