-- ============================================================
-- 淘籍籍 数据库初始化脚本 (MySQL)
-- 用法：mysql -u root -p < init.sql
-- 如果是其他数据库（H2/PostgreSQL），需调整数据类型
-- ============================================================

CREATE DATABASE IF NOT EXISTS book_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_trade;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`          INT          AUTO_INCREMENT PRIMARY KEY,
  `username`    VARCHAR(50)  NOT NULL UNIQUE,
  `password`    VARCHAR(255) NOT NULL COMMENT 'for login/register',
  `nickname`    VARCHAR(50)  DEFAULT NULL,
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT 'avatar URL',
  `phone`       VARCHAR(20)  DEFAULT NULL,
  `school_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 图书表
-- 状态: 0=在售  1=已售出  2=已下架
-- ============================================================
CREATE TABLE IF NOT EXISTS `book` (
  `id`             INT          AUTO_INCREMENT PRIMARY KEY,
  `user_id`        INT          NOT NULL COMMENT 'seller ID',
  `title`          VARCHAR(200) NOT NULL,
  `author`         VARCHAR(100) DEFAULT NULL,
  `category`       VARCHAR(50)  DEFAULT NULL COMMENT 'category',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT 'original price',
  `sell_price`     DECIMAL(10,2) NOT NULL COMMENT 'selling price',
  `description`    TEXT         DEFAULT NULL,
  `images`         VARCHAR(1000) DEFAULT NULL COMMENT 'comma-separated image paths',
  `view_count`     INT          DEFAULT 0 COMMENT 'view count',
  `status`         TINYINT      DEFAULT 0 COMMENT '0=onSale 1=sold 2=offShelf',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`),
  INDEX idx_category (`category`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 订单表
-- 状态: 0=待付款  1=待发货  2=待收货  3=已完成  4=已取消
-- ============================================================
CREATE TABLE IF NOT EXISTS `orders` (
  `id`           INT          AUTO_INCREMENT PRIMARY KEY,
  `order_no`     VARCHAR(50)  NOT NULL UNIQUE COMMENT 'order number',
  `book_id`      INT          NOT NULL,
  `book_title`   VARCHAR(200) DEFAULT NULL COMMENT 'redundant for list display',
  `book_image`   VARCHAR(255) DEFAULT NULL COMMENT 'redundant',
  `total_price`  DECIMAL(10,2) NOT NULL,
  `buyer_id`     INT          NOT NULL,
  `seller_id`    INT          NOT NULL,
  `status`       TINYINT      DEFAULT 0 COMMENT 'order status',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `pay_time`     DATETIME     DEFAULT NULL,
  `ship_time`    DATETIME     DEFAULT NULL,
  `confirm_time` DATETIME     DEFAULT NULL,
  INDEX idx_buyer_id (`buyer_id`),
  INDEX idx_seller_id (`seller_id`),
  INDEX idx_book_id (`book_id`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 聊天消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`          INT      AUTO_INCREMENT PRIMARY KEY,
  `from_user_id` INT  NOT NULL,
  `to_user_id`   INT  NOT NULL,
  `content`     TEXT     NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_from_to (`from_user_id`, `to_user_id`),
  INDEX idx_to_from (`to_user_id`, `from_user_id`),
  INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 会话列表视图（方便前端查询最近聊天）
-- 如果后端用 SQL 查询会话列表，可参考：
-- SELECT
--   LEAST(sender_id, receiver_id) AS user_a,
--   GREATEST(sender_id, receiver_id) AS user_b,
--   MAX(id) AS last_msg_id
-- FROM chat_message
-- WHERE sender_id = ? OR receiver_id = ?
-- GROUP BY user_a, user_b

-- ============================================================
-- 收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS `favorite` (
  `id`          INT      AUTO_INCREMENT PRIMARY KEY,
  `user_id`     INT      NOT NULL,
  `book_id`     INT      NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_book (`user_id`, `book_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 通知表
-- type: 'system'=系统通知  'transaction'=交易通知
-- ============================================================
CREATE TABLE IF NOT EXISTS `notification` (
  `id`          INT          AUTO_INCREMENT PRIMARY KEY,
  `user_id`     INT          NOT NULL,
  `type`        VARCHAR(20)  NOT NULL DEFAULT 'system' COMMENT 'system / transaction',
  `title`       VARCHAR(200) DEFAULT NULL,
  `content`     TEXT         DEFAULT NULL,
  `is_read`     TINYINT      DEFAULT 0 COMMENT '0=unread 1=read',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`),
  INDEX idx_type (`type`),
  INDEX idx_is_read (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 测试数据（可选）
-- ============================================================
-- INSERT INTO `user` (username, password, nickname) VALUES
-- ('testuser', '123456', '测试用户'),
-- ('seller1', '123456', '书虫小明');

-- INSERT INTO `book` (user_id, title, author, category, sell_price, description, status) VALUES
-- (2, '三体', '刘慈欣', '科技', 25.00, '经典科幻巨作', 0),
-- (2, '活着', '余华', '文学', 15.00, '余华代表作', 0);
