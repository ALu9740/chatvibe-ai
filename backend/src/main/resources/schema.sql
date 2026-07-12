-- ChatVibe-AI 数据库初始化脚本
-- 作者：Alu  日期：2026-07-07
-- 描述：建库 + 建表 + 索引；向量存储使用 SimpleVectorStore（文件持久化），无需建向量表

CREATE DATABASE IF NOT EXISTS chatvibe_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chatvibe_ai;

-- 用户表（个人开发者备案场景，仅硬编码单用户）
CREATE TABLE IF NOT EXISTS users (
  id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL UNIQUE              COMMENT '用户名',
  password_hash VARCHAR(255) NOT NULL                     COMMENT '密码（{noop} 或 BCrypt 哈希）',
  role          VARCHAR(16)  NOT NULL DEFAULT 'OWNER'     COMMENT '角色',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' ,
  deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 聊天会话表（统一承载 chat/pdf/comfort 三种类型）
CREATE TABLE IF NOT EXISTS chat_session (
  id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT       NOT NULL                     COMMENT '所属用户',
  type         VARCHAR(16)  NOT NULL                     COMMENT '类型：chat|pdf|comfort',
  title        VARCHAR(255)                              COMMENT '会话标题',
  scenario_key VARCHAR(64)                               COMMENT '学会哄人场景 key（仅 type=comfort）',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' ,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
  INDEX idx_user_type (user_id, type),
  INDEX idx_updated (updated_at),
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS chat_message (
  id         BIGINT       PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT       NOT NULL                       COMMENT '所属会话',
  role       VARCHAR(16)  NOT NULL                       COMMENT '角色：user|assistant',
  content    MEDIUMTEXT   NOT NULL                       COMMENT '消息内容',
  metadata   JSON                                        COMMENT '元数据（文件、评分等）',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' ,
  deleted    TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
  INDEX idx_session (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- PDF 文件表（一个 pdf 会话对应一个文件）
CREATE TABLE IF NOT EXISTS pdf_file (
  id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
  session_id   BIGINT       NOT NULL UNIQUE              COMMENT '所属会话',
  stored_name  VARCHAR(255) NOT NULL                     COMMENT '存储文件名',
  original_name VARCHAR(255) NOT NULL                    COMMENT '原文件名',
  size         BIGINT       NOT NULL                     COMMENT '字节数',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' ,
  deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
  FOREIGN KEY (session_id) REFERENCES chat_session (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDF 文件表';

-- 学会哄人场景表（由 data.sql 初始化内置场景）
CREATE TABLE IF NOT EXISTS comfort_scenario (
  `key`         VARCHAR(64)  PRIMARY KEY                 COMMENT '场景 key',
  label         VARCHAR(64)  NOT NULL                    COMMENT '场景名称',
  description   VARCHAR(255)                             COMMENT '场景描述',
  system_prompt TEXT         NOT NULL                    COMMENT '系统提示词',
  sort          INT          NOT NULL DEFAULT 0          COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学会哄人场景表';
