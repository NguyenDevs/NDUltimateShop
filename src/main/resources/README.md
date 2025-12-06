# NDUltimateShop

Plugin cửa hàng tất cả trong một dành cho máy chủ Minecraft (Spigot/Paper). Plugin bao gồm hệ thống Cửa hàng admin (Shop), Chợ đấu giá (Auction House), Hệ thống bán đồ (Sell GUI), và Chợ đêm (Black Market).

## 🌟 Tính năng chính

1.  **Shop Admin**:
    * Mua vật phẩm từ admin (server) với GUI đẹp mắt.
    * Hỗ trợ giảm giá bằng Coupon.
    * Quản lý kho hàng (Stock) hoặc bán vô hạn.
2.  **Auction House (Chợ trời)**:
    * Người chơi tự đăng bán vật phẩm (`/ah sell <giá>`).
    * Tính phí hoa hồng (Tax).
    * Hệ thống hết hạn vật phẩm và trả lại cho người bán.
    * Xem danh sách vật phẩm của bản thân.
3.  **Sell System (Bán đồ)**:
    * Bỏ đồ vào GUI để bán nhanh (`/sell`).
    * Tự động tính giá dựa trên config hoặc giá trị set riêng.
    * Hỗ trợ bán cả các block/item cơ bản và item custom.
4.  **Night Shop (Chợ đêm/Chợ đen)**:
    * Chỉ mở cửa vào khung giờ nhất định trong ngày (theo giờ thực).
    * Nơi bán các vật phẩm hiếm hoặc cấm.
    * Thông báo tự động khi mở/đóng cửa.
5.  **Coupon System**:
    * Tạo mã giảm giá cho người chơi (theo số lần dùng hoặc thời gian).
    * Áp dụng giảm giá trực tiếp vào giá mua Shop hoặc Auction.

## 📂 Cấu trúc dữ liệu

Plugin tách biệt hoàn toàn giữa cấu hình giao diện và dữ liệu người dùng:
* `config.yml`, `itemsell.yml`, `language.yml`: Cấu hình chung.
* `gui/*.yml`: Chỉ chứa cấu hình giao diện (Title, Slot, Item trang trí).
* `data/*.yml`: Chứa dữ liệu động (Vật phẩm trong shop, Đấu giá, Coupon, Giá custom...). **Không chỉnh sửa thủ công các file này khi server đang chạy.**

## 🛠 Lệnh và Quyền hạn (Permissions)

### Admin Commands (`ndshop.admin`)
Lệnh chính: `/ndshop` (hoặc `/ndus`, `/shopadmin`)

| Lệnh | Mô tả |
| :--- | :--- |
| `/ndshop reload` | Tải lại toàn bộ config và data. |
| `/ndshop shop add <giá> [kho]` | Thêm item trên tay vào Shop. (Kho = -1 là vô hạn). |
| `/ndshop shop remove <id>` | Xóa item khỏi Shop theo ID. |
| `/ndshop nightshop add <giá> <kho>` | Thêm item trên tay vào Chợ Đêm. |
| `/ndshop nightshop toggle` | Bật/Tắt chế độ Chợ Đêm thủ công. |
| `/ndshop sell setprice <giá>` | Set giá bán custom cho item trên tay. |
| `/ndshop coupon create <code> <%> <type> <val>` | Tạo mã giảm giá. <br>Type: `time` (thời gian) hoặc `uses` (lượt dùng). |
| `/ndshop coupon remove <code>` | Xóa mã giảm giá. |

### Player Commands

| Lệnh | Quyền hạn | Mô tả |
| :--- | :--- | :--- |
| `/shop` | `ndshop.shop.use` | Mở cửa hàng Admin. |
| `/ah` | `ndshop.auction.use` | Mở chợ đấu giá. |
| `/ah sell <giá>` | `ndshop.auction.use` | Đăng bán vật phẩm trên tay. |
| `/sell` | `ndshop.sell.use` | Mở GUI bán đồ. |
| `/nightshop` | `ndshop.nightshop.use` | Mở chợ đêm (nếu đang mở cửa). |
| `/coupon <code>` | `ndshop.coupon.use` | Nhập mã giảm giá. |

**Quyền hạn bổ sung:**
* `ndshop.auction.limit.<số_lượng>`: Giới hạn số lượng vật phẩm tối đa người chơi được treo trên chợ (Ví dụ: `ndshop.auction.limit.20`).

## 📦 Cài đặt

1.  Tải plugin và bỏ vào thư mục `plugins/`.
2.  Cài đặt **Vault** và một plugin kinh tế (EssentialsX, CMI, etc.).
3.  (Tùy chọn) Cài đặt **PlaceholderAPI** để hiển thị thông tin đẹp hơn.
4.  Khởi động lại server.
5.  Config tại `plugins/NDUltimateShop/config.yml` và `itemsell.yml`.

## 📞 Hỗ trợ
Developed by NguyenDevs. Mọi thắc mắc xin vui lòng liên hệ qua Discord **@NguyenDevs**.