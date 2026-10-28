# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Ngôn ngữ

Luôn trả lời và trao đổi với người dùng bằng **tiếng Việt** trong mọi phiên làm việc. Mã nguồn, tên biến, tên file và commit message giữ theo quy ước của dự án.

## Tổng quan dự án

ElectricalWarehouse là ứng dụng web quản lý kho thiết bị điện. Hai thư mục độc lập:

- `backend/`: Java 17, Spring Boot 3.5.6, Maven (có `mvnw`), Spring Security + JWT (jjwt), JPA/Hibernate, MapStruct, Lombok, SQL Server.
- `frontend/`: React 18 + Vite 5 (JavaScript/JSX, không TypeScript), Redux Toolkit, React Router 6, Axios.

## Lệnh thường dùng

Backend (chạy trong `backend/`, Windows dùng `mvnw.cmd`):

```bash
./mvnw spring-boot:run                     # cần DB_URL/DB_USERNAME/DB_PASSWORD, JWT_SECRET (xem README)
./mvnw test                                # toàn bộ test, dùng H2 (profile "test"), không cần SQL Server
./mvnw test -Dtest=FifoFefoCalculatorTest  # một class test
./mvnw test -Dtest=ApprovalPolicyTest#creatorCannotApproveOwnDocumentEvenAsAdmin  # một test method
./mvnw compile                             # MapStruct sinh DTOMapperImpl trong target/generated-sources
```

Frontend (chạy trong `frontend/`):

```bash
npm run dev     # :5180, proxy /api và /uploads sang :8090 (vite.config.js)
npm run lint    # ESLint 9 flat config
npm run build
```

Frontend chưa có test runner.

## Kiến trúc backend (`com.electricalwarehouse`)

Phân tầng: `controller` → `service` (interface) / `service/impl` → `repository` → `entity`.

- **Lõi tồn kho:** chỉ `InventoryService` được thay đổi bảng `inventories`. Các thao tác `increase`, `decreaseAt`, `decrease(movement, Pool)`, `reserve`, `releaseReservation` nhận một `StockMovement`, tự ghi `StockTransaction` (thẻ kho, có `balanceAfter`) và phát `StockChangedEvent`. Báo cáo XNT được tính hoàn toàn từ `stock_transactions`, nên mọi thay đổi tồn phải đi qua `InventoryService`.
- **Một dòng `Inventory`** = kho + sản phẩm + vị trí + lô. `reservedQuantity` giữ hàng cho phiếu xuất đã duyệt. `FifoFefoCalculator` chọn dòng theo FEFO (sản phẩm `expiryManaged`) hoặc FIFO (`receivedAt`).
- **Phiếu cần duyệt** (`StockInReceipt`, `StockOutReceipt`, `StockTransfer`, `InventoryAdjustment`) kế thừa `ApprovableDocument`. Mọi chuyển trạng thái đi qua `ApprovalService` (gọi `ApprovalPolicy`, ghi `AuditLog`, phát `ReceiptStatusChangedEvent`). `ApprovalPolicy` thực thi phân tách nhiệm vụ: người lập không được duyệt.
- **Sự kiện:** `NotificationEventListener` và `InventoryAlertJob` lắng nghe `@TransactionalEventListener(AFTER_COMMIT)` và ghi trong transaction `REQUIRES_NEW`. `InventoryAlertJob` còn chạy theo cron `app.alert.cron`.
- **Phân quyền:** vai trò thành authority `ROLE_<RoleCode>`, permission code (seed trong `DataInitializer`) thành authority thường. Controller dùng `@PreAuthorize("hasAuthority('STOCK_IN_CREATE')")`, ADMIN được seed mọi permission. Trong service lấy user hiện tại bằng `CurrentUser.get()`.
- **Response** luôn bọc trong `ApiResponse<T>`, danh sách phân trang dùng `PagedResult<T>`. Lỗi nghiệp vụ ném `BusinessException`/`NotFoundException`, `GlobalExceptionHandler` chuyển thành 4xx.
- `Lookup` tải entity theo id (ném 404). `DTOMapper` (MapStruct) phải được gọi bên trong transaction vì `open-in-view=false`.
- `integration/`: `EmailService` (@Async, tắt mặc định), `BarcodeService` (ZXing), `ExcelExportService` (POI), `PdfExportService` (OpenPDF, cần font Unicode).

## Kiến trúc frontend (`frontend/src`)

- `api/*Api.js` gọi qua `httpClient.js`. Client này gắn Bearer token, tự refresh khi gặp 401 (refresh token được xoay vòng) và dùng `unwrap()` để trả về `data` của `ApiResponse`.
- `store/`: `AuthStore` (user, `hasPermission`, `hasRole`), `CartStore` (dòng hàng tạm khi lập phiếu), `NotificationStore` (số chưa đọc + toast).
- `components/`: `DataTable`, `BaseForm` (form khai báo theo `fields`, hiển thị lỗi field từ API), `ConfirmDialog`, `BarcodeScanner` (máy quét USB + camera qua html5-qrcode), `DocumentActions` (gửi/duyệt/từ chối/hủy dùng chung).
- `pages/<module>/`: route khai báo trong `App.jsx`. Route cần quyền được bọc bằng `guard(element, permission)`.

## Giao tiếp và cấu hình

- API base: `/api` (đổi qua `VITE_API_BASE_URL`). Ở môi trường dev, Vite proxy sang `http://localhost:8090`, nên không cần CORS. CORS cho origin khác cấu hình bằng `CORS_ALLOWED_ORIGINS`.
- Xác thực: `POST /api/auth/login` trả access token (JWT, mặc định 30 phút) và refresh token. Server chỉ lưu SHA-256 của refresh token trong `user_sessions`.
- CSDL: cấu hình trong `backend/src/main/resources/application.yml`, mọi giá trị lấy từ biến môi trường. Schema do Hibernate `ddl-auto=update` tạo, chưa có Flyway/Liquibase. Cột chữ tiếng Việt dùng `NVARCHAR`.
- Lưu ý SQL Server: ràng buộc UNIQUE chỉ cho phép một giá trị NULL. Với cột vừa nullable vừa cần unique (vd. `products.barcode`), phải kiểm tra trùng ở tầng service.
- Test tích hợp dùng `src/test/resources/application-test.yml` (H2 ở chế độ MSSQLServer, có sẵn JWT secret chỉ dùng cho test).
