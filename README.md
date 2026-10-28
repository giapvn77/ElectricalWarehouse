# ElectricalWarehouse — Quản lý kho thiết bị điện

Ứng dụng web quản lý kho: danh mục hàng, nhập kho (quét serial), xuất kho (lấy hàng theo FIFO/FEFO), tồn kho thời gian thực, kiểm kê, chuyển kho, hàng lỗi, cảnh báo Min-Max và báo cáo Nhập - Xuất - Tồn.

| Phần | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security + JWT, Spring Data JPA, MapStruct |
| Frontend | React 18, Vite 5, Redux Toolkit, React Router 6, Axios |
| CSDL | SQL Server |

## Chạy nhanh

### 1. Chuẩn bị SQL Server

Tạo database trống `ElectricalWarehouse`. Bảng được Hibernate tự tạo khi khởi động (`ddl-auto=update`).

### 2. Backend (cổng 8090)

```bash
cd backend
export DB_URL="jdbc:sqlserver://localhost:1433;databaseName=ElectricalWarehouse;encrypt=true;trustServerCertificate=true"
export DB_USERNAME=sa
export DB_PASSWORD=...                       # mật khẩu SQL Server
export JWT_SECRET=$(openssl rand -base64 48) # bắt buộc, >= 256 bit
export APP_ADMIN_PASSWORD=...                # tạo tài khoản admin ở lần chạy đầu
./mvnw spring-boot:run                       # Windows: mvnw.cmd spring-boot:run
```

Swagger UI: http://localhost:8090/swagger-ui.html

### 3. Frontend (cổng 5180)

```bash
cd frontend
npm install
npm run dev
```

Mở http://localhost:5180 và đăng nhập bằng `admin` cùng mật khẩu `APP_ADMIN_PASSWORD`. Vite chuyển tiếp `/api` và `/uploads` sang `localhost:8090`.

## Biến môi trường backend

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | localhost / `sa` / rỗng | Kết nối SQL Server |
| `JWT_SECRET` | **bắt buộc** | Khóa ký JWT (base64) |
| `JWT_ACCESS_MINUTES`, `JWT_REFRESH_DAYS` | 30 / 7 | Thời hạn token |
| `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD` | `admin` / rỗng | Tài khoản admin tạo lần đầu |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5180` | Origin frontend khi không dùng proxy |
| `MAIL_ENABLED`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | tắt | Gửi email khi có phiếu chờ duyệt / được duyệt |
| `INVENTORY_ALERT_CRON`, `EXPIRY_WARNING_DAYS` | mỗi giờ / 30 | Lịch quét cảnh báo Min-Max và hạn dùng |
| `UPLOAD_DIR` | `uploads` | Thư mục lưu ảnh sản phẩm |
| `PDF_FONT` | tự dò | Font TTF có tiếng Việt cho file PDF |
| `JPA_DDL_AUTO` | `update` | Nên đổi sang `validate` + công cụ migration khi lên production |

## Quy trình nghiệp vụ

| Phiếu | Luồng trạng thái | Tồn kho thay đổi khi |
|---|---|---|
| Nhập kho | DRAFT → PENDING → APPROVED → (quét serial) → COMPLETED | COMPLETED: cộng tồn, serial chuyển IN_STOCK |
| Xuất kho | DRAFT → PENDING → APPROVED → PICKING → COMPLETED | APPROVED: giữ chỗ; COMPLETED: trừ tồn |
| Chuyển kho | DRAFT → PENDING → APPROVED → COMPLETED | APPROVED: trừ kho nguồn; COMPLETED: cộng kho đích |
| Kiểm kê | PENDING → COMPLETED | Khi được duyệt |
| Hàng lỗi | Ghi nhận → Sửa / Trả NCC / Tiêu hủy | Chuyển sang kho loại DEFECTIVE, xử lý thì xuất ra |

- **Phân tách nhiệm vụ:** người lập phiếu không bao giờ được tự duyệt phiếu của mình (kể cả ADMIN). Quản lý kho gắn với một kho chỉ duyệt được phiếu của kho đó.
- **Vai trò:** `ADMIN`, `QLK` (quản lý kho), `NVTM` (thu mua), `NVK` (nhân viên kho), `NVBH` (bán hàng). Quyền chi tiết chỉnh trong màn hình Phân quyền.
- **FIFO / FEFO:** hàng có quản lý hạn dùng xuất theo hạn gần nhất, hàng khác theo ngày nhập sớm nhất.
- Mọi thay đổi tồn đều ghi vào `stock_transactions` (thẻ kho), từ đó tính báo cáo XNT.

## Kiểm thử

```bash
cd backend && ./mvnw test        # unit + integration (H2 in-memory, không cần SQL Server)
cd frontend && npm run lint && npm run build
```
