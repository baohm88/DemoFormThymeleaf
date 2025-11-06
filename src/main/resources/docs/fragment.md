### 1. Khái niệm cốt lõi

*   **Fragment là gì?** Là một mẩu HTML có thể tái sử dụng, được định nghĩa bằng thuộc tính `th:fragment`. Hãy nghĩ về nó như một "component" hoặc một "partial" view.
*   **`th:replace` là gì?** Là một thuộc tính dùng để **thay thế hoàn toàn** thẻ hiện tại bằng nội dung của một fragment. Đây là cách phổ biến và mạnh mẽ nhất để xây dựng layout.
*   **Mục tiêu:** Chúng ta sẽ tạo ra một file `layout.html` chứa cấu trúc chung của trang (thẻ `<html>`, `<head>`, `<body>`, header, footer). Sau đó, các trang con như `list-students.html` hay `add-student.html` sẽ chỉ cần định nghĩa phần nội dung chính của chúng và "nhét" vào layout chung đó.

---

### Bước 1: Tạo các Fragments (Thành phần tái sử dụng)

Đầu tiên, hãy tạo một file để chứa các fragment chung. Việc này giúp quản lý code gọn gàng.

Tạo file `src/main/resources/templates/fragments/common.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

    <!--
        FRAGMENT 1: HEADER
        - th:fragment="header": Đặt tên cho mẩu HTML này là "header".
        - Chúng ta có thể gọi đến fragment này từ bất kỳ file template nào khác.
    -->
    <nav th:fragment="header" class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" th:href="@{/students}">Quản lý Sinh viên</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" th:href="@{/students}">Danh sách</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" th:href="@{/students/new}">Thêm mới</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!--
        FRAGMENT 2: FOOTER
        - Đặt tên cho mẩu HTML này là "footer".
    -->
    <footer th:fragment="footer" class="bg-light text-center text-lg-start mt-auto">
        <div class="text-center p-3" style="background-color: rgba(0, 0, 0, 0.2);">
            © 2024 Copyright:
            <a class="text-dark" href="#">MyWebApp.com</a>
        </div>
    </footer>

</body>
</html>
```

---

### Bước 2: Tạo Layout chính (Bộ khung của trang)

File này sẽ định nghĩa cấu trúc HTML chung và có một "chỗ trống" để các trang con điền nội dung vào.

Tạo file `src/main/resources/templates/layout.html`
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <!-- 
        Sử dụng một biến 'pageTitle' để đặt tiêu đề động cho mỗi trang.
        Nếu trang con không cung cấp, tiêu đề mặc định sẽ là 'Ứng dụng Quản lý Sinh viên'.
    -->
    <title th:text="${pageTitle} ?: 'Ứng dụng Quản lý Sinh viên'">Tiêu đề</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body class="d-flex flex-column min-vh-100">

    <!--
        PHẦN 1: THAY THẾ HEADER
        - th:replace="~{fragments/common :: header}": Tìm đến file "fragments/common.html",
          tìm fragment có tên là "header", và THAY THẾ TOÀN BỘ thẻ <header> này bằng nội dung của fragment đó.
        - Cú pháp: ~{đường_dẫn_template :: tên_fragment}
    -->
    <header th:replace="~{fragments/common :: header}"></header>

    <!--
        PHẦN 2: CHỖ TRỐNG CHO NỘI DUNG CHÍNH
        - Đây là phần quan trọng nhất. Thẻ <main> này sẽ được thay thế bởi nội dung
          cụ thể của từng trang (trang danh sách, trang thêm mới,...).
        - Chúng ta sẽ dùng một kỹ thuật gọi là "Layout Dialect" (mô phỏng) hoặc truyền tham số fragment.
    -->
    <main class="container mt-4 flex-grow-1" th:insert="${content}">
        <!-- NỘI DUNG CỦA TRANG CON SẼ ĐƯỢC CHÈN VÀO ĐÂY -->
    </main>

    <!--
        PHẦN 3: THAY THẾ FOOTER
        - Tương tự như header, thay thế thẻ <footer> này bằng fragment "footer".
    -->
    <footer th:replace="~{fragments/common :: footer}"></footer>

</body>
</html>
```
*(Lưu ý: Chúng ta dùng `th:insert="${content}"` ở đây. Lát nữa, các trang con sẽ định nghĩa `content` là gì và truyền vào cho layout.)*

---

### Bước 3: Áp dụng Layout vào trang `list-students.html`

Bây giờ, file `list-students.html` sẽ trở nên cực kỳ gọn gàng. Nó không cần quan tâm đến `<html>`, `<head>`, `<body>` nữa, mà chỉ tập trung vào phần nội dung chính của nó.

Sửa lại file `src/main/resources/templates/list-students.html`
```html
<!DOCTYPE html>
<!--
    KỸ THUẬT LAYOUT VỚI THAM SỐ:
    1. th:replace="~{layout :: html}": Dòng này nói rằng: "Hãy quên hết nội dung của file này đi,
       thay vào đó, hãy lấy toàn bộ thẻ <html> từ file layout.html".
    2. (pageTitle='Danh sách Sinh viên', content=~{::content}): Đây là phần truyền tham số.
       - 'pageTitle': Truyền một chuỗi làm tiêu đề cho trang.
       - 'content=~{::content}': Tìm một fragment tên là "content" TRONG CHÍNH FILE NÀY (ký hiệu ::),
         và truyền nó vào biến 'content' của file layout.
-->
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout(pageTitle='Danh sách Sinh viên', content=~{::content})}">
<body>

    <!--
        Đây chính là fragment "content" mà chúng ta sẽ truyền vào layout.
        Nó chứa toàn bộ nội dung duy nhất của trang danh sách.
    -->
    <th:block th:fragment="content">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h1>Danh sách Sinh viên</h1>
            <a th:href="@{/students/new}" class="btn btn-primary">Thêm sinh viên mới</a>
        </div>

        <div th:if="${#lists.isEmpty(students)}" class="alert alert-warning">
            Không tìm thấy sinh viên nào.
        </div>

        <div th:unless="${#lists.isEmpty(students)}">
            <table class="table table-bordered table-striped">
                <thead class="table-dark">
                <tr>
                    <th>STT</th>
                    <th>Họ và tên</th>
                    <th>Chuyên ngành</th>
                    <th>Hành động</th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="student, iterStat : ${students}">
                    <td th:text="${iterStat.count}"></td>
                    <td th:text="${student.name}"></td>
                    <td th:text="${student.major.displayName}"></td>
                    <td>
                        <a th:href="@{/students/edit/{id}(id=${student.id})}" class="btn btn-warning btn-sm">Sửa</a>
                        <a th:href="@{/students/delete/{id}(id=${student.id})}" class="btn btn-danger btn-sm"
                           onclick="return confirm('Bạn có chắc chắn muốn xóa?')">Xóa</a>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </th:block>

</body>
</html>
```
*(Để làm được kỹ thuật truyền tham số, bạn cần sửa lại `layout.html` một chút:)*

**Sửa `layout.html` để nhận tham số:**
```html
<!DOCTYPE html>
<!--
    th:fragment="layout(pageTitle, content)": Định nghĩa layout này như một fragment
    có thể nhận 2 tham số là pageTitle và content.
-->
<html lang="en" xmlns:th="http://www.thymeleaf.org" th:fragment="layout(pageTitle, content)">
<head>
    <meta charset="UTF-8">
    <title th:text="${pageTitle} ?: 'Mặc định'">Tiêu đề</title>
    ...
</head>
<body class="d-flex flex-column min-vh-100">
    <header th:replace="~{fragments/common :: header}"></header>

    <!-- Thay thế thẻ main này bằng fragment 'content' được truyền vào -->
    <main class="container mt-4 flex-grow-1" th:replace="${content}"></main>

    <footer th:replace="~{fragments/common :: footer}"></footer>
</body>
</html>
```

### 4. So sánh `th:replace`, `th:insert`, và `th:include`

Để hiểu rõ sự khác biệt, hãy xem một ví dụ đơn giản. Giả sử chúng ta có fragment sau:

**Fragment:** `<p th:fragment="message">Xin chào!</p>`

Bây giờ, chúng ta sẽ áp dụng 3 thuộc tính khác nhau lên thẻ host: `<div id="host">...</div>`

| Thuộc tính | Code sử dụng | Kết quả HTML cuối cùng | Giải thích |
| :--- | :--- | :--- | :--- |
| **`th:replace`** | `<div id="host" th:replace="~{:: message}"></div>` | `<p>Xin chào!</p>` | Thẻ `div#host` **bị xóa bỏ** và được thay thế hoàn toàn bởi thẻ `p` của fragment. |
| **`th:insert`** | `<div id="host" th:insert="~{:: message}"></div>` | `<div id="host"><p>Xin chào!</p></div>` | Thẻ `div#host` **được giữ lại**, và nội dung của fragment (thẻ `p`) được chèn **vào bên trong** nó. |
| **`th:include`** (ít dùng) | `<div id="host" th:include="~{:: message}"></div>` | `<div id="host">Xin chào!</div>` | Thẻ `div#host` **được giữ lại**, nhưng chỉ có **nội dung bên trong** fragment (chữ "Xin chào!") được chèn vào, thẻ `p` của fragment bị bỏ đi. |

**Kết luận:**
*   Dùng `th:replace` khi bạn muốn thay thế hoàn toàn một placeholder bằng một component (phổ biến nhất cho layout).
*   Dùng `th:insert` khi bạn muốn bọc một fragment bên trong một thẻ khác.

Bằng cách sử dụng `th:fragment` và `th:replace`, bạn đã tạo ra một hệ thống layout mạnh mẽ, giúp ứng dụng của bạn trở nên chuyên nghiệp, dễ quản lý và mở rộng hơn rất nhiều.