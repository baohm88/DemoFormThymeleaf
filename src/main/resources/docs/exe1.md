Skip to main content
Google Classroom
Classroom
T2404E - Sem4
Home
Calendar
Enrolled
To-do
T
T2404E - Sem4
Archived classes
Settings
CRUD Spring boot - thymeleaf
Luyến Đào Hồng
•
8:35 PM
100 points
exe1.md
Text

Class comments
Your work
Assigned
Private comments
### **Bài tập: Xây dựng Ứng dụng Web Quản lý Tuyển dụng (Job Board)**

#### **1. Bối cảnh và Mục tiêu**

Trong bối cảnh thị trường lao động sôi động, các công ty liên tục có nhu cầu đăng tin tuyển dụng để tìm kiếm nhân tài. Bạn được giao nhiệm vụ xây dựng một ứng dụng web nội bộ cho phép phòng nhân sự (HR) quản lý danh sách các công ty và các tin tuyển dụng liên quan đến họ.

**Mục tiêu chính:**
*   Ôn tập và áp dụng toàn bộ luồng CRUD (Tạo, Đọc, Cập nhật, Xóa).
*   Thành thạo việc sử dụng Thymeleaf để xây dựng giao diện web động.
*   Hiểu và triển khai được mối quan hệ `One-to-Many` trong JPA.
*   Xây dựng ứng dụng có cấu trúc tốt, dễ bảo trì bằng cách sử dụng Fragments.

#### **2. Yêu cầu Công nghệ**
*   **Ngôn ngữ:** Java 17+
*   **Framework:** Spring Boot 3.x
*   **View Engine:** Thymeleaf
*   **Database:** Spring Data JPA + H2 (In-memory Database)
*   **Build Tool:** Maven

#### **3. Mô hình Dữ liệu (Entities)**

Cần tạo hai thực thể chính có mối quan hệ với nhau:

**a. Thực thể `Company` (Công ty)**
*   `id` (Long, Khóa chính, Tự động tăng)
*   `name` (String, Bắt buộc, Tối đa 100 ký tự)
*   `address` (String, Bắt buộc)
*   `website` (String, Không bắt buộc, Phải là URL hợp lệ nếu có)
*   `email` (String, Bắt buộc, Phải là email hợp lệ)

**b. Thực thể `JobPosting` (Tin tuyển dụng)**
*   `id` (Long, Khóa chính, Tự động tăng)
*   `title` (String, Bắt buộc, Tối thiểu 5 ký tự)
*   `description` (String, Kiểu TEXT, Bắt buộc)
*   `location` (String, Bắt buộc)
*   `salary` (String, Không bắt buộc)
*   `jobType` (Enum, Bắt buộc, các giá trị: `FULL_TIME`, `PART_TIME`, `REMOTE`, `INTERNSHIP`)
*   `postedDate` (LocalDate, Tự động gán ngày hiện tại khi tạo mới)

**Mối quan hệ quan trọng:**
*   Một `Company` có thể có nhiều `JobPosting`.
*   Một `JobPosting` chỉ thuộc về một `Company`.
*   Đây là mối quan hệ **One-to-Many** từ `Company` đến `JobPosting`.

#### **4. Yêu cầu Chức năng (CRUD)**

**Module 1: Quản lý Công ty (CRUD cho `Company`)**
1.  **Hiển thị danh sách:** Tạo một trang liệt kê tất cả các công ty trong hệ thống dưới dạng bảng.
2.  **Thêm mới:** Tạo một form cho phép người dùng nhập thông tin và thêm một công ty mới.
3.  **Cập nhật:** Từ trang danh sách, mỗi công ty phải có nút "Sửa" để điều hướng đến form chỉnh sửa thông tin của công ty đó.
4.  **Xóa:** Mỗi công ty phải có nút "Xóa" kèm theo một hộp thoại xác nhận (`confirm()`).

**Module 2: Quản lý Tin tuyển dụng (CRUD cho `JobPosting`)**
1.  **Hiển thị danh sách:** Tạo trang chính hiển thị tất cả các tin tuyển dụng. Mỗi tin phải hiển thị cả tên công ty đăng tuyển.
2.  **Thêm mới:**
    *   Tạo một form để thêm tin tuyển dụng mới.
    *   Trong form này, phải có một **dropdown (`<select>`)** cho phép người dùng **chọn công ty** từ danh sách các công ty đã có trong hệ thống.
3.  **Cập nhật:** Tương tự, cho phép sửa thông tin một tin tuyển dụng, bao gồm cả việc thay đổi công ty đăng tuyển.
4.  **Xóa:** Cho phép xóa một tin tuyển dụng.

#### **5. Yêu cầu Giao diện (Thymeleaf)**

1.  **Sử dụng Layout chung:**
    *   Tạo một file `layout.html` chứa cấu trúc `<html>`, `<head>`, `<body>`.
    *   Tạo các **fragments** cho `header` (thanh điều hướng) và `footer`.
    *   Tất cả các trang khác (danh sách, form) phải sử dụng layout này thông qua `th:replace`.

2.  **Trang danh sách (Cả hai module):**
    *   Sử dụng `th:each` để lặp và hiển thị dữ liệu trong bảng.
    *   Sử dụng `iterStat` để hiển thị cột "Số thứ tự".
    *   Nếu danh sách rỗng, phải hiển thị một thông báo thân thiện (ví dụ: "Chưa có công ty nào được tạo.") thay vì một bảng trống. Dùng `th:if`.
    *   Định dạng dữ liệu một cách hợp lý (ví dụ: hiển thị tên thân thiện cho `jobType`).

3.  **Form (Cả hai module):**
    *   Sử dụng `th:object` và `th:field` để liên kết form với đối tượng model.
    *   Triển khai **validation** cho các trường bắt buộc.
    *   Hiển thị các thông báo lỗi validation ngay bên dưới các trường input tương ứng nếu người dùng nhập sai.
    *   Sử dụng các loại input phù hợp: `text` cho tên, `textarea` cho mô tả, `select` cho loại công việc và công ty.

#### **6. Yêu cầu Nâng cao (Không bắt buộc)**

*   **Phân trang:** Áp dụng phân trang cho trang danh sách tin tuyển dụng.
*   **Tìm kiếm:** Thêm một ô tìm kiếm trên trang danh sách tin tuyển dụng, cho phép tìm theo `title`.
*   **Trang chi tiết:** Khi nhấn vào tiêu đề của một tin tuyển dụng, điều hướng đến một trang hiển thị đầy đủ thông tin chi tiết của tin đó.

#### **7. Gợi ý để bắt đầu**

1.  **Bắt đầu từ Model:** Định nghĩa 2 class `Company` và `JobPosting` cùng với mối quan hệ JPA trước tiên.
2.  **Tập trung vào một module:** Hoàn thành toàn bộ luồng CRUD cho `Company` trước. Đây là module đơn giản hơn vì không có phụ thuộc.
3.  **Xây dựng layout:** Tạo `layout.html` và các fragments `header`, `footer` sớm.
4.  **Module `JobPosting`:** Khi làm form thêm mới, hãy nhớ rằng bạn cần lấy danh sách tất cả `Company` từ database và truyền vào `Model` để render dropdown.
5.  **Sử dụng H2 Console:** Thường xuyên truy cập `http://localhost:8080/h2-console` để kiểm tra xem dữ liệu có được lưu đúng như mong đợi không.

Chúc bạn hoàn thành tốt bài tập này
exe1.md
Displaying exe1.md.