CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`    VARCHAR(50)           DEFAULT '' COMMENT '昵称',
    `phone`       VARCHAR(20)            DEFAULT '' COMMENT '手机号',
    `email`       VARCHAR(100)           DEFAULT '' COMMENT '邮箱',
    `avatar`      VARCHAR(255)           DEFAULT '' COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    `sort_order`  INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE IF NOT EXISTS `product` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id` BIGINT         NOT NULL COMMENT '分类ID',
    `name`        VARCHAR(100)   NOT NULL COMMENT '商品名称',
    `description` VARCHAR(500)            DEFAULT '' COMMENT '商品简介',
    `detail`      TEXT                    COMMENT '商品详情（富文本）',
    `price`       DECIMAL(10,2)  NOT NULL COMMENT '价格',
    `stock`       INT            NOT NULL DEFAULT 0 COMMENT '库存',
    `sales`       INT            NOT NULL DEFAULT 0 COMMENT '销量',
    `main_image`  VARCHAR(255)            DEFAULT '' COMMENT '主图URL',
    `images`      VARCHAR(1000)           DEFAULT '[]' COMMENT '图片列表（JSON数组）',
    `status`      TINYINT        NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `deleted`     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `cart_item` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '购物车明细ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
    `quantity`    INT      NOT NULL DEFAULT 1 COMMENT '数量',
    `selected`    TINYINT  NOT NULL DEFAULT 1 COMMENT '是否选中：0-未选 1-选中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车明细表';

CREATE TABLE IF NOT EXISTS `order_t` (
    `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no`        VARCHAR(32)    NOT NULL COMMENT '订单编号',
    `user_id`         BIGINT         NOT NULL COMMENT '用户ID',
    `total_amount`    DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    `status`          TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消',
    `receiver_name`   VARCHAR(50)    NOT NULL DEFAULT '' COMMENT '收货人',
    `receiver_phone`  VARCHAR(20)    NOT NULL DEFAULT '' COMMENT '收货电话',
    `receiver_address` VARCHAR(200)  NOT NULL DEFAULT '' COMMENT '收货地址',
    `remark`          VARCHAR(500)            DEFAULT '' COMMENT '备注',
    `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `order_item` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
    `order_id`      BIGINT         NOT NULL COMMENT '订单ID',
    `product_id`    BIGINT         NOT NULL COMMENT '商品ID',
    `product_name`  VARCHAR(100)   NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(255)            DEFAULT '' COMMENT '商品图片（快照）',
    `price`         DECIMAL(10,2)  NOT NULL COMMENT '单价（快照）',
    `quantity`      INT            NOT NULL COMMENT '数量',
    `total_amount`  DECIMAL(10,2)  NOT NULL COMMENT '小计',
    `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `payment` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
    `payment_no`  VARCHAR(32)    NOT NULL COMMENT '支付流水号',
    `order_id`    BIGINT         NOT NULL COMMENT '订单ID',
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `amount`      DECIMAL(10,2)  NOT NULL COMMENT '支付金额',
    `pay_method`  TINYINT        NOT NULL DEFAULT 0 COMMENT '支付方式：0-支付宝 1-微信',
    `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0-待支付 1-支付成功 2-支付失败',
    `paid_time`   DATETIME                DEFAULT NULL COMMENT '支付完成时间',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
