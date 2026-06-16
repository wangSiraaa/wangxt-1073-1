-- ============================================================
-- 海鲜市场电子秤监管系统 - 演示数据初始化脚本
-- 使用方式：启动系统并确认表创建后，在MySQL中执行 source demo-data.sql
-- 或者：mysql -uroot -proot scale_regulatory < demo-data.sql
-- ============================================================

-- ---------------------------
-- 场景1：校准过期阻断营业
-- ---------------------------
-- 1. 创建摊位
INSERT INTO stall (stall_code, stall_name, location, owner_name, owner_phone, business_license, business_qualified, status, created_at, updated_at)
VALUES ('ST-A001', 'A区01号海鲜摊', '海鲜市场A区01号位', '张三', '13800138001', 'LIC-2024-A001', 1, 'OPEN', NOW(), NOW());

SET @stall_a001 = LAST_INSERT_ID();

-- 2. 创建秤具（校准有效期在2020年，已过期，演示过期自动阻断）
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-88001', 'ACS-30', '上海耀华', @stall_a001, '2020-01-01', 'INTACT', 'SEAL-88001', 1, 'NORMAL', 0, NOW(), NOW());

SET @scale_expired = LAST_INSERT_ID();

-- 3. 另创建一把正常秤具（供其他场景使用）
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-88002', 'ACS-30', '上海耀华', @stall_a001, DATE_ADD(NOW(), INTERVAL 90 DAY), 'INTACT', 'SEAL-88002', 1, 'NORMAL', 0, NOW(), NOW());

SET @scale_normal = LAST_INSERT_ID();

-- 4. 给正常秤具添加一条校准记录
INSERT INTO calibration (scale_id, calibration_date, metrologist_id, metrologist_name, result, next_calibration_date, rectification_deadline, created_at, updated_at)
VALUES (@scale_normal, DATE(NOW()), 1, '李计量', 'PASS', DATE_ADD(NOW(), INTERVAL 90 DAY), NULL, NOW(), NOW());

-- ---------------------------
-- 场景2：投诉 -> 复检 -> 暂停
-- ---------------------------
-- 1. 再创建一个摊位（演示完整投诉复检流程）
INSERT INTO stall (stall_code, stall_name, location, owner_name, owner_phone, business_license, business_qualified, status, created_at, updated_at)
VALUES ('ST-B002', 'B区02号水产摊', '海鲜市场B区02号位', '李四', '13800138002', 'LIC-2024-B002', 1, 'OPEN', NOW(), NOW());

SET @stall_b002 = LAST_INSERT_ID();

-- 2. 为该摊位创建秤具
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-99001', 'ACS-15', '杭州四方', @stall_b002, DATE_ADD(NOW(), INTERVAL 60 DAY), 'INTACT', 'SEAL-99001', 1, 'NORMAL', 0, NOW(), NOW());

SET @scale_b002 = LAST_INSERT_ID();

-- 3. 给该秤具添加校准通过记录
INSERT INTO calibration (scale_id, calibration_date, metrologist_id, metrologist_name, result, next_calibration_date, rectification_deadline, created_at, updated_at)
VALUES (@scale_b002, DATE_SUB(NOW(), INTERVAL 30 DAY), 1, '李计量', 'PASS', DATE_ADD(NOW(), INTERVAL 60 DAY), NULL, NOW(), NOW());

-- 4. 创建投诉（短斤少两投诉）
INSERT INTO complaint (complaint_no, complainant_name, complainant_phone, stall_id, scale_id, transaction_time, goods_name, display_weight, actual_weight, price_per_unit, overcharged_amount, complaint_remark, status, created_at, updated_at)
VALUES ('CMP-DEMO-001', '王五', '13900139001', @stall_b002, @scale_b002, DATE_SUB(NOW(), INTERVAL 2 HOUR),
        '大闸蟹', 2.500, 2.000, 88.00, 44.00, '买大闸蟹2.5斤回家称重只有2斤', 'PENDING', NOW(), NOW());

SET @complaint_demo = LAST_INSERT_ID();

-- ---------------------------
-- 辅助数据：备用秤（用于临时借秤场景）
-- ---------------------------
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-SPARE-01', 'ACS-30', '上海耀华', NULL, DATE_ADD(NOW(), INTERVAL 180 DAY), 'INTACT', 'SEAL-SPARE01', 1, 'NORMAL', 0, NOW(), NOW());

-- ============================================================
-- 使用说明：
-- 1. 场景1演示：
--    - 启动后端后，点击 秤具管理 -> "检查过期" 按钮（或等待定时任务）
--    - SC-88001 会被标记为 CALIBRATION_EXPIRED
--    - 访问 GET /api/stalls/{id}/can-operate 会返回false（不能营业）
--
-- 2. 场景2演示：
--    - 投诉管理中找到 CMP-DEMO-001
--    - 点击"调查" -> 点击"投诉成立"
--    - 系统自动生成复检任务
--    - 进入复检管理，点击"预约" -> "开始复检"
--    - 填写标准重量1.000，测量重量1.050（误差>1%）-> "完成复检"
--    - 系统自动将 B区02号水产摊 标记为暂停营业
-- ============================================================
