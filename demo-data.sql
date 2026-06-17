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
INSERT INTO complaint (complaint_no, complainant_name, complainant_phone, stall_id, scale_id, transaction_time, goods_name, display_weight, actual_weight, price_per_unit, overcharged_amount, description, status, created_at, updated_at)
VALUES ('CMP-DEMO-001', '王五', '13900139001', @stall_b002, @scale_b002, DATE_SUB(NOW(), INTERVAL 2 HOUR),
        '大闸蟹', 2.500, 2.000, 88.00, 44.00, '买大闸蟹2.5斤回家称重只有2斤', 'PENDING', NOW(), NOW());

SET @complaint_demo = LAST_INSERT_ID();

-- ---------------------------
-- 辅助数据：备用秤（用于临时借秤场景）
-- ---------------------------
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-SPARE-01', 'ACS-30', '上海耀华', NULL, DATE_ADD(NOW(), INTERVAL 180 DAY), 'INTACT', 'SEAL-SPARE01', 1, 'NORMAL', 0, NOW(), NOW());

SET @scale_spare_01 = LAST_INSERT_ID();

-- ---------------------------
-- 场景3：限期整改 -> 整改提醒闭环
-- ---------------------------
-- 1. 创建摊位（限期整改场景）
INSERT INTO stall (stall_code, stall_name, location, owner_name, owner_phone, business_license, business_qualified, status, created_at, updated_at)
VALUES ('ST-C003', 'C区03号贝类摊', '海鲜市场C区03号位', '赵六', '13800138003', 'LIC-2024-C003', 1, 'OPEN', NOW(), NOW());

SET @stall_c003 = LAST_INSERT_ID();

-- 2. 创建需要整改的秤具
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-77001', 'ACS-15', '杭州四方', @stall_c003, DATE_ADD(NOW(), INTERVAL 30 DAY), 'DAMAGED', 'SEAL-77001', 1, 'NEEDS_RECTIFICATION', 0, NOW(), NOW());

SET @scale_rectify = LAST_INSERT_ID();

-- 3. 添加限期整改校准记录（7天内截止）
INSERT INTO calibration (scale_id, calibration_date, metrologist_id, metrologist_name, result, next_calibration_date, rectification_deadline, created_at, updated_at)
VALUES (@scale_rectify, DATE(NOW()), 1, '李计量', 'RECTIFICATION_REQUIRED', DATE_ADD(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), NOW());

SET @cal_rectify = LAST_INSERT_ID();

-- 4. 手动插入整改提醒（截止前3天、1天、当天各两条：摊主+管理员）
INSERT INTO rectification_reminder (calibration_id, scale_id, stall_id, target_type, reminder_date, deadline, is_sent, content, created_at, updated_at)
VALUES
  (@cal_rectify, @scale_rectify, @stall_c003, 'STALL_OWNER', DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, '您的电子秤SC-77001需在7天内完成整改', NOW(), NOW()),
  (@cal_rectify, @scale_rectify, @stall_c003, 'ADMIN', DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, 'C区03号贝类摊SC-77001需在7天内完成整改', NOW(), NOW()),
  (@cal_rectify, @scale_rectify, @stall_c003, 'STALL_OWNER', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, '您的电子秤SC-77001明天整改截止', NOW(), NOW()),
  (@cal_rectify, @scale_rectify, @stall_c003, 'ADMIN', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, 'C区03号贝类摊SC-77001明天整改截止', NOW(), NOW()),
  (@cal_rectify, @scale_rectify, @stall_c003, 'STALL_OWNER', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, '您的电子秤SC-77001今日整改截止！', NOW(), NOW()),
  (@cal_rectify, @scale_rectify, @stall_c003, 'ADMIN', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, 'C区03号贝类摊SC-77001今日整改截止！', NOW(), NOW());

-- ---------------------------
-- 场景4：投诉升级 -> 复检优先级升级
-- ---------------------------
-- 1. 创建摊位（高频投诉场景）
INSERT INTO stall (stall_code, stall_name, location, owner_name, owner_phone, business_license, business_qualified, status, created_at, updated_at)
VALUES ('ST-D004', 'D区04号虾蟹摊', '海鲜市场D区04号位', '孙七', '13800138004', 'LIC-2024-D004', 1, 'OPEN', NOW(), NOW());

SET @stall_d004 = LAST_INSERT_ID();

-- 2. 为该摊位创建秤具
INSERT INTO scale (scale_code, scale_model, manufacturer, stall_id, calibration_expire_date, seal_status, seal_number, business_qualified, status, is_borrowed, created_at, updated_at)
VALUES ('SC-66001', 'DS-322', '梅特勒-托利多', @stall_d004, DATE_ADD(NOW(), INTERVAL 45 DAY), 'INTACT', 'SEAL-66001', 1, 'NORMAL', 0, NOW(), NOW());

SET @scale_d004 = LAST_INSERT_ID();

-- 3. 添加3条已成立投诉（触发EMERGENCY优先级）
INSERT INTO complaint (complaint_no, complainant_name, complainant_phone, stall_id, scale_id, transaction_time, goods_name, display_weight, actual_weight, price_per_unit, overcharged_amount, complaint_remark, status, handle_time, created_at, updated_at)
VALUES
  ('CMP-DEMO-D01', '甲', '13700001001', @stall_d004, @scale_d004, DATE_SUB(NOW(), INTERVAL 5 DAY), '基围虾', 1.500, 1.350, 55.00, 8.25, '缺斤少两', 'ESTABLISHED', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
  ('CMP-DEMO-D02', '乙', '13700001002', @stall_d004, @scale_d004, DATE_SUB(NOW(), INTERVAL 3 DAY), '梭子蟹', 2.000, 1.700, 120.00, 36.00, '严重短斤少两', 'ESTABLISHED', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
  ('CMP-DEMO-D03', '丙', '13700001003', @stall_d004, @scale_d004, DATE_SUB(NOW(), INTERVAL 1 DAY), '大闸蟹', 3.000, 2.600, 88.00, 35.20, '投诉三次了', 'ESTABLISHED', NOW(), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());

-- 4. 添加一条待处理投诉（新投诉，触发优先级判定）
INSERT INTO complaint (complaint_no, complainant_name, complainant_phone, stall_id, scale_id, transaction_time, goods_name, display_weight, actual_weight, price_per_unit, overcharged_amount, complaint_remark, status, created_at, updated_at)
VALUES ('CMP-DEMO-D04', '丁', '13700001004', @stall_d004, @scale_d004, NOW(), '花蛤', 1.000, 0.850, 15.00, 2.25, '又是这家', 'PENDING', NOW(), NOW());

-- ---------------------------
-- 场景5：借用备用秤 + 原秤追溯
-- ---------------------------
-- 1. 为C区03号摊位借用备用秤（替代整改中的原秤）
INSERT INTO scale_borrow (scale_id, original_scale_id, borrowed_to_stall_id, borrower_name, borrow_reason, borrow_context, borrowed_at, expected_return_at, is_returned, created_at, updated_at)
VALUES (@scale_spare_01, @scale_rectify, @stall_c003, '赵六', '原秤SC-77001铅封损坏限期整改，借用备用秤营业', 'RECTIFICATION_REPLACEMENT', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 0, NOW(), NOW());

-- 2. 标记备用秤为已借出
UPDATE scale SET is_borrowed = 1 WHERE id = @scale_spare_01;

-- ============================================================
-- 使用说明：
-- 1. 场景1演示：校准过期阻断营业
--    - 启动后端后，点击 秤具管理 -> "检查过期" 按钮
--    - SC-88001 会被标记为 CALIBRATION_EXPIRED
--    - 访问 GET /api/stalls/{id}/can-operate 会返回false
--
-- 2. 场景2演示：投诉 -> 复检 -> 暂停
--    - 投诉管理中找到 CMP-DEMO-001
--    - 点击"调查" -> 点击"投诉成立"
--    - 系统自动生成复检任务
--    - 进入复检管理，完成复检流程
--
-- 3. 场景3演示：限期整改 -> 整改提醒闭环
--    - 查看整改提醒列表（Dashboard或提醒管理）
--    - 定时任务每天8点检查并发送到期提醒
--    - 手动触发：POST /api/rectification-reminders/process
--    - 校准通过后提醒自动取消
--
-- 4. 场景4演示：投诉升级 -> 复检优先级升级
--    - 在投诉管理中对 CMP-DEMO-D04 点击"投诉成立"
--    - 系统检测到D区04号摊位近一个月已有3次已成立投诉
--    - 自动生成的复检任务优先级为 EMERGENCY（特级）
--    - 复检列表中按优先级排序，EMERGENCY排在最前
--
-- 5. 场景5演示：借用备用秤 + 原秤追溯
--    - 在摊位管理中点击C区03号摊位的"秤具状态"按钮
--    - 可以看到原秤SC-77001（限期整改）和借用秤SC-SPARE-01（整改替代）
--    - 在Dashboard"当前借用中的备用秤"面板可以看到借用关系
--    - 原秤的投诉和整改记录完整保留，不会被删除
-- ============================================================
