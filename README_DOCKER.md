# VideoConverter Docker Setup

## Thành phần
- `app`: Ứng dụng Java (Tomcat 10 + FFmpeg) build từ mã nguồn bằng Maven.
- `db`: MySQL 8 lưu dữ liệu người dùng, activity logs.

## File chính
- `Dockerfile`: Multi-stage (Maven build -> Tomcat runtime). Có cài FFmpeg.
- `docker-compose.yml`: Khởi chạy cả database và ứng dụng.
- `.dockerignore`: Giảm kích thước context build.

## Chạy nhanh
```bash
# Build và chạy nền
docker compose up -d --build

# Xem logs ứng dụng
docker compose logs -f app

# Dừng
docker compose down
```

## Volumes
- `db_data`: dữ liệu MySQL (persist).
- `uploads`: chứa ảnh avatar và các file xử lý: `/usr/local/tomcat/webapps/uploads`.

Muốn copy avatar từ host: đặt file vào thư mục volume của Docker (tra cứu bằng `docker volume inspect`).

## Thiết lập DB ban đầu
Sau khi container lên, chạy:
```bash
docker compose exec db mysql -uvideouser -pvideopass videodb -e "SHOW TABLES;"
```
Nếu cần import schema:
```bash
docker compose exec -i db mysql -uvideouser -pvideopass videodb < database.sql
```

## Biến môi trường
App container nhận:
- `DB_HOST=db`
- `DB_PORT=3306`
- `DB_NAME=videodb`
- `DB_USER=videouser`
- `DB_PASSWORD=videopass`

Đảm bảo code đọc các biến này (nếu hiện tại đang hardcode `localhost`, cần sửa thành đọc từ env). Ví dụ JDBC URL:
```
jdbc:mysql://" + System.getenv("DB_HOST") + ":" + System.getenv("DB_PORT") + "/" + System.getenv("DB_NAME") + "?useSSL=false&characterEncoding=UTF-8"
```

## Healthcheck
App có healthcheck HTTP đơn giản trên `/` (Tomcat root). Nếu muốn sâu hơn có thể thêm servlet `/health` trả về 200.

## FFmpeg
Đã cài `ffmpeg` bằng `apt-get` trong runtime image. Kiểm tra:
```bash
docker compose exec app ffmpeg -version
```

## Reset sạch
```bash
docker compose down -v --remove-orphans
```

## Nâng cao
- Thêm reverse proxy (Nginx) nếu cần HTTPS.
- Thêm `watchtower` để auto update images.
- Dùng `.env` để ẩn mật khẩu.

## Ghi chú bảo mật
Mật khẩu hiện tại chỉ phục vụ phát triển (`rootpass`, `videopass`). Đổi trong production và bật backup định kỳ.

---
Nếu cần thêm Redis queue hoặc Nginx proxy cứ yêu cầu tiếp nhé.
