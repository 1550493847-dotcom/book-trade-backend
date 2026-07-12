# 淘籍籍（二手书交易平台）— 后端 AGENTS.md

## 项目概述

Spring Boot 2.7.18 + MyBatis + MySQL + Redis + WebSocket 后端服务。

---

## 部署架构（阿里云服务器）

```
阿里云服务器 (121.199.31.208, 4GB 内存)
    │
    ├── Docker 容器
    │   ├── book-trade-backend  (Java 17, --network host, 8080)
    │   ├── book-trade-redis    (redis:7-alpine, 6379 → 宿主机)
    │   └── book-trade-nginx    (nginx:alpine, --network host, 80)
    │
    ├── 宿主机服务
    │   ├── MySQL 8.0 (3306, 数据库: book_trade)
    │   └── 前端文件 (/root/frontend-static/, Nginx 挂载)
    │
    └── Nginx 配置 (/etc/nginx/conf.d/default.conf)
        ├── /         → /usr/share/nginx/html (前端静态文件)
        ├── /api/*    → 127.0.0.1:8080
        ├── /img/*    → 127.0.0.1:8080
        ├── /uploads/* → 127.0.0.1:8080
        ├── /ws/*     → 127.0.0.1:8080 (WebSocket)
        └── 使用 --network host 模式（勿用 host.docker.internal）
```

---

## Docker 部署命令

```bash
# 构建镜像
cd /root/book-trade-backend
docker build -t book-trade-backend .

# 启动后端（--network host）
docker run -d \
  --name book-trade-backend \
  --restart unless-stopped \
  --network host \
  -e MYSQL_HOST=127.0.0.1 \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=1234 \
  -e REDIS_HOST=127.0.0.1 \
  -e REDIS_PORT=6379 \
  -e JWT_SECRET=your-secret \
  -e CORS_ORIGINS=http://localhost:5173,http://localhost:8080,https://1550493847-dotcom.github.io,http://121.199.31.208 \
  -e FILE_UPLOAD_PATH=/var/lib/uploads \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  -v /root/book-trade-backend/uploads:/var/lib/uploads \
  book-trade-backend

# 启动 Redis
docker run -d --name book-trade-redis --restart unless-stopped -p 6379:6379 redis:7-alpine redis-server --appendonly yes

# 启动 Nginx（--network host）
docker run -d \
  --name book-trade-nginx \
  --restart unless-stopped \
  --network host \
  -v /root/frontend-static:/usr/share/nginx/html:ro \
  -v /etc/nginx/conf.d/default.conf:/etc/nginx/conf.d/default.conf \
  nginx:alpine
```

---

## 环境变量配置

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `MYSQL_HOST` | MySQL 主机 | `localhost` |
| `MYSQL_USER` | MySQL 用户 | `root` |
| `MYSQL_PASSWORD` | MySQL 密码 | `1234` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `JWT_SECRET` | JWT 密钥 | `change-me-in-production` |
| `CORS_ORIGINS` | 允许的跨域来源（逗号分隔） | `http://localhost:5173` |
| `FILE_UPLOAD_PATH` | 图片上传路径 | `/var/lib/uploads` |

---

## 数据库（MySQL）

**数据库名：** `book_trade`

**表结构初始化：** `init.sql`（项目根目录）

### ⚠️ 重要：修改数据库时必须同步更新 init.sql

**每次新增或修改表结构时：**
1. 改 Mapper 接口中的 SQL 注解
2. 同步更新 `init.sql` 中的 CREATE TABLE
3. 确保列名、类型、默认值完全一致

**当前各表的代码关键列名对照：**

**user 表：**
| 后端代码字段 | 数据库列名 | 备注 |
|-------------|-----------|------|
| userId | user_id | |
| nickname | nickname | |
| password | password | |
| avatar | avatar | |
| phone | phone | |
| schoolName | school_name | |
| creditScore | credit_score | 🔴 曾缺失 |
| createTime | create_time | |
| lastLoginTime | last_login_time | 🔴 曾缺失 |
| lastLoginIp | last_login_ip | 🔴 曾缺失 |

**book 表：**
| 后端代码字段 | 数据库列名 | 备注 |
|-------------|-----------|------|
| userId | user_id | |
| title | title | |
| author | author | |
| isbn | isbn | 🔴 曾缺失 |
| publisher | publisher | 🔴 曾缺失 |
| bookCondition | book_condition | 🔴 曾缺失 |
| category | category | |
| originalPrice | original_price | |
| sellPrice | sell_price | |
| description | description | |
| images | images | |
| viewCount | view_count | |
| status | status | |
| createTime | create_time | |

**orders 表：** order_no, book_id, buyer_id, seller_id, total_price, status, create_time, pay_time, ship_time, confirm_time

**chat_message 表：** id, from_user_id, to_user_id, content, create_time

**favorite 表：** id, user_id, book_id, create_time

**notification 表：** id, user_id, type, title, content, is_read, create_time

---

## 新增功能流程

```bash
# 1. 本地修改代码
#    - 后端逻辑
#    - Mapper SQL
#    - 如果需要改表：同步更新 init.sql

# 2. 本地测试
mvn spring-boot:run    # 后端启动在 localhost:8080

# 3. 提交推送
git add -A
git commit -m "feat: 新功能描述"
git push

# 4. 服务器更新
ssh root@121.199.31.208
cd /root/book-trade-backend
git pull
docker build -t book-trade-backend .
docker stop book-trade-backend && docker rm book-trade-backend
# 然后用上文的 docker run 命令启动

# 5. 如果改了数据库结构
#    - 服务器上手动 ALTER TABLE 加列
#    - 或备份数据后重跑 init.sql
mysql -u root -p book_trade < /root/book-trade-backend/init.sql
```

---

## 集成功能

- **Redis 缓存**：图书列表/详情/分类缓存，TTL 5-60 分钟
- **Token 黑名单**：退出登录时将 JWT 加入 Redis，TTL = 剩余有效期
- **在线状态**：Redis `online:user:{id}`，TTL 30 秒
- **WebSocket Pub/Sub**：多实例消息广播
- **浏览计数**：Redis INCR，每 10 次回写 MySQL
- **限流**：Redis 滑动窗口，每 IP 每分钟 60 次
- **通知队列**：Redis List 结构
- **XSS 防护**：`SecurityUtils.sanitize()`
- **文件上传校验**：仅允许 JPG/PNG/GIF/WebP，最大 5MB
- **CORS**：从环境变量读取允许的来源列表

## 图片存储

| 环境 | 路径 |
|------|------|
| Docker 容器内 | `/var/lib/uploads/img/` |
| 服务器宿主机 | `/root/book-trade-backend/uploads/img/` |
| 本地 Windows | `C:\var\lib\uploads\img\`（默认，可改） |
| 访问 URL | `http://121.199.31.208/img/xxx.png` |

**配置方式：** 环境变量 `FILE_UPLOAD_PATH` 控制路径

**Nginx 优化（服务器上已配置）：** 图片由 Nginx 直接 serving，不经过 Java 后端
```
location /img/ {
    alias /var/lib/uploads/img/;
    expires 30d;
    access_log off;
    add_header Cache-Control "public, immutable";
}
```

**UploadController URL 生成规则：**
- 文件存到 `{uploadPath}/img/{uuid}.{ext}`
- 返回 URL: `/img/{uuid}.{ext}`
- 前端拼接完整地址：`{VITE_API_BASE_URL}/img/{uuid}.{ext}`
