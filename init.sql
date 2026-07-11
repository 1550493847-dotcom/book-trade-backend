-- ============================================================
-- 淘籍籍 数据库初始化脚本 (MySQL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS book_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE book_trade;

-- ============================================================
-- 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
  id              INT          AUTO_INCREMENT PRIMARY KEY,
  username        VARCHAR(50)  NOT NULL UNIQUE,
  password        VARCHAR(255) NOT NULL,
  
ickname        VARCHAR(50)  DEFAULT NULL,
  vatar          VARCHAR(255) DEFAULT NULL,
  phone           VARCHAR(20)  DEFAULT NULL,
  school_name     VARCHAR(100) DEFAULT NULL,
  create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  credit_score    INT          DEFAULT 100,
  last_login_time DATETIME     DEFAULT NULL,
  last_login_ip   VARCHAR(50)  DEFAULT NULL,
  INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 图书表
-- ============================================================
CREATE TABLE IF NOT EXISTS ook (
  id             INT          AUTO_INCREMENT PRIMARY KEY,
  user_id        INT          NOT NULL,
  	itle          VARCHAR(200) NOT NULL,
  uthor         VARCHAR(100) DEFAULT NULL,
  isbn           VARCHAR(50)  DEFAULT NULL,
  publisher      VARCHAR(200) DEFAULT NULL,
  ook_condition VARCHAR(50)  DEFAULT NULL,
  category       VARCHAR(50)  DEFAULT NULL,
  original_price DECIMAL(10,2) DEFAULT NULL,
  sell_price     DECIMAL(10,2) NOT NULL,
  description    TEXT         DEFAULT NULL,
  images         VARCHAR(1000) DEFAULT NULL,
  iew_count     INT          DEFAULT 0,
  status         TINYINT      DEFAULT 0,
  create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_category (category),
  INDEX idx_status (status),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
  id           INT          AUTO_INCREMENT PRIMARY KEY,
  order_no     VARCHAR(50)  NOT NULL UNIQUE,
  ook_id      INT          NOT NULL,
  uyer_id     INT          NOT NULL,
  seller_id    INT          NOT NULL,
  	otal_price  DECIMAL(10,2) NOT NULL,
  status       TINYINT      DEFAULT 0,
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  pay_time     DATETIME     DEFAULT NULL,
  ship_time    DATETIME     DEFAULT NULL,
  confirm_time DATETIME     DEFAULT NULL,
  INDEX idx_buyer_id (uyer_id),
  INDEX idx_seller_id (seller_id),
  INDEX idx_book_id (ook_id),
  INDEX idx_status (status),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 聊天消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_message (
  id          INT      AUTO_INCREMENT PRIMARY KEY,
  rom_user_id INT  NOT NULL,
  	o_user_id   INT  NOT NULL,
  content     TEXT     NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_from_to (rom_user_id, 	o_user_id),
  INDEX idx_to_from (	o_user_id, rom_user_id),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS avorite (
  id          INT      AUTO_INCREMENT PRIMARY KEY,
  user_id     INT      NOT NULL,
  ook_id     INT      NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_book (user_id, ook_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 通知表
-- ============================================================
CREATE TABLE IF NOT EXISTS 
otification (
  id          INT          AUTO_INCREMENT PRIMARY KEY,
  user_id     INT          NOT NULL,
  	ype        VARCHAR(20)  NOT NULL DEFAULT 'system',
  	itle       VARCHAR(200) DEFAULT NULL,
  content     TEXT         DEFAULT NULL,
  is_read     TINYINT      DEFAULT 0,
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_type (	ype),
  INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
