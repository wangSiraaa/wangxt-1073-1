# 海鲜市场电子秤监管系统

## 系统概述

本系统用于海鲜市场电子秤的监管，实现市场监管员处理投诉和复检的全流程管理。

## 角色定义

- **市场管理员**：摊位管理、秤具登记、暂停/恢复营业
- **计量员**：校准结果上传、复检执行
- **投诉窗口**：受理短斤少两投诉、处理投诉流程
- **摊主**：相关状态记录（摊位、秤具绑定等）

## 核心功能

### 1. 摊位管理
- 摊位登记、编辑、删除
- 营业资格管理
- 状态跟踪（营业中/已暂停/已关闭）

### 2. 秤具管理
- 秤具登记（绑定摊位、检定有效期、铅封状态、营业资格）
- 秤具绑定/解绑摊位
- 营业资格管理
- 自动检测校准过期状态

### 3. 校准管理
- 计量员上传校准结果
- 结果类型：通过(PASS)、限期整改(RECTIFICATION_REQUIRED)、不合格停用(DISQUALIFIED)
- 不合格自动暂停对应摊位营业

### 4. 投诉管理
- 投诉窗口受理短斤少两投诉
- 关联交易时间、秤具、商品、重量、金额
- 投诉流程：待处理 → 调查中 → 投诉成立/驳回/撤销 → 关闭
- 投诉成立自动生成复检任务

### 5. 复检管理
- 投诉成立自动生成复检任务
- 复检预约、开始、完成、取消
- 复检未通过自动暂停摊位营业
- 支持临时借秤复检

### 6. 暂停营业管理
- 自动暂停：校准过期、复检未通过、投诉成立
- 手动暂停/整改复开
- 完整的暂停历史记录

### 7. 临时借秤
- 秤具借出登记
- 归还管理
- 状态跟踪

### 8. 审计日志
- 全操作审计追踪
- 按模块、操作人、关联ID查询

## 核心业务规则（后端强制执行）

1. **校准过期阻断营业**：校准有效期过期的秤具自动标记为 CALIBRATION_EXPIRED，摊位不能正常营业
2. **投诉成立生成复检**：投诉状态变为 ESTABLISHED 时自动创建复检任务
3. **复检未通过暂停**：复检结果 FAILED 时自动暂停对应摊位
4. **整改复开**：暂停状态的摊位需管理员手动恢复营业

## 技术架构

- **后端**：Spring Boot 3.2 + Spring Data JPA + MySQL 8 + H2 + JDK 17
- **前端**：Vue 3.3 + Vite 4 + Element Plus 2.3 + Pinia + Axios + dayjs
- **部署**：Docker + Docker Compose（MySQL 8 + 后端 + 前端 Nginx）

## 本地开发

### 后端
```bash
cd backend
# 需要先安装 Maven 和 JDK 17+
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
mvn spring-boot:run
```
后端启动在 http://localhost:8080/api

### 前端
```bash
cd frontend
npm install
npm run dev
```
前端启动在 http://localhost:3000

## Docker 部署

```bash
# 1. 先构建后端 jar
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
cd backend && mvn clean package -DskipTests && cd ..

# 2. 启动所有服务
docker-compose up -d --build
```

访问地址：
- 前端：http://localhost
- 后端API：http://localhost:8080/api

## 导入演示数据

系统启动并完成表创建后，执行演示数据脚本：

```bash
# 方式一：使用mysql客户端
mysql -h 127.0.0.1 -uroot -proot scale_regulatory < demo-data.sql

# 方式二：使用docker容器内的mysql
docker exec -i scale-mysql mysql -uroot -proot scale_regulatory < demo-data.sql
```

演示数据包含两个核心复现场景（见下文），以及一把备用秤。

## API 接口

| 模块 | 接口 |
|------|------|
| 摊位 | /api/stalls |
| 秤具 | /api/scales |
| 校准 | /api/calibrations |
| 投诉 | /api/complaints |
| 复检 | /api/reinspections |
| 暂停 | /api/suspensions |
| 借秤 | /api/scale-borrows |
| 审计 | /api/audit-logs |
| 枚举 | /api/enums |

## 复现场景说明

### 场景1：校准过期阻断营业
1. 创建摊位和秤具，设置校准有效期为过去日期
2. 调用 `/api/scales/check-expired` 或等待定时任务
3. 秤具状态变为 CALIBRATION_EXPIRED
4. 调用 `/api/stalls/{id}/can-operate` 返回 false

### 场景2：投诉 → 复检 → 暂停流程
1. 受理投诉 → PENDING
2. 开始调查 → UNDER_INVESTIGATION
3. 认定投诉成立 → ESTABLISHED，系统自动创建复检任务
4. 预约复检 → SCHEDULED
5. 开始复检 → IN_PROGRESS
6. 完成复检（设置不合格结果）→ FAILED，系统自动暂停摊位营业
7. 摊位状态变为 SUSPENDED
