### Mục tiêu của ví dụ này:
*   Sử dụng `th:each` để lặp và hiển thị một danh sách các đối tượng.
*   Hiển thị thông báo thân thiện khi danh sách rỗng.
*   Định dạng các loại dữ liệu khác nhau (ngày tháng, enum, boolean) để hiển thị đẹp hơn.
*   Tạo các link "Sửa" và "Xóa" động cho mỗi hàng.
*   Sử dụng biến trạng thái `iterStat` của vòng lặp để đánh số thứ tự và làm nổi bật hàng.

---

### Bước 1: Model và Repository (Không đổi)

Chúng ta vẫn sử dụng `Student.java`, `Major.java`, và `StudentRepository.java` từ ví dụ trước. Chúng là nền tảng dữ liệu cho trang danh sách của chúng ta.

---

### Bước 2: Controller - Chuẩn bị dữ liệu cho View

Chúng ta cần một phương thức trong `StudentController` để lấy tất cả sinh viên từ database và truyền danh sách đó vào `Model` để Thymeleaf có thể truy cập.

`src/main/java/com/example/thymeleafcrud/controller/StudentController.java`
```java
package com.example.thymeleafcrud.controller;

// ... (các import khác)
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepository;

    // ... (constructor và các phương thức create/save từ ví dụ trước) ...
    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * PHƯƠNG THỨC HIỂN THỊ DANH SÁCH SINH VIÊN
     * Xử lý request GET tới "/students"
     */
    @GetMapping
    public String listStudents(Model model) {
        // 1. Lấy tất cả các đối tượng Student từ database.
        //    Kết quả là một List<Student>.
        List<Student> studentList = studentRepository.findAll();

        // 2. Thêm danh sách này vào đối tượng Model với tên là "students".
        //    Thymeleaf sẽ sử dụng tên "students" này để truy cập vào danh sách.
        model.addAttribute("students", studentList);

        // 3. Trả về tên của file template HTML để render.
        return "list-students"; // Trả về file /resources/templates/list-students.html
    }
    
    // ... (các phương thức khác) ...

}
```

---

### Bước 3: View `list-students.html` - Hiển thị danh sách chi tiết

Đây là file Thymeleaf, nơi chúng ta sẽ biến danh sách đối tượng Java thành một bảng HTML đẹp mắt và đầy đủ chức năng.

`src/main/resources/templates/list-students.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Danh sách Sinh viên</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1>Danh sách Sinh viên</h1>
        <!-- Nút để điều hướng đến trang tạo mới sinh viên -->
        <a th:href="@{/students/new}" class="btn btn-primary">Thêm sinh viên mới</a>
    </div>

    <!-- ===== PHẦN 1: XỬ LÝ KHI DANH SÁCH RỖNG ===== -->
    <!--
        #lists.isEmpty(...) là một utility function của Thymeleaf để kiểm tra
        xem một collection có rỗng hay không.
        th:if: Chỉ render khối div này nếu danh sách 'students' rỗng.
    -->
    <div th:if="${#lists.isEmpty(students)}" class="alert alert-warning" role="alert">
        Không tìm thấy sinh viên nào. Hãy thêm sinh viên mới!
    </div>

    <!-- ===== PHẦN 2: HIỂN THỊ BẢNG KHI CÓ DỮ LIỆU ===== -->
    <!--
        th:unless="${#lists.isEmpty(students)}": Ngược lại với th:if.
        Chỉ render bảng này nếu danh sách 'students' KHÔNG rỗng.
    -->
    <div th:unless="${#lists.isEmpty(students)}">
        <table class="table table-bordered table-striped table-hover">
            <thead class="table-dark">
            <tr>
                <th>STT</th>
                <th>Họ và tên</th>
                <th>Mã SV</th>
                <th>Ngày sinh</th>
                <th>Chuyên ngành</th>
                <th>Đã đồng ý ĐK</th>
                <th>Hành động</th>
            </tr>
            </thead>
            <tbody>
            <!--
                th:each="student, iterStat : ${students}" -> Đây là vòng lặp chính.
                - 'student': Biến tạm, đại diện cho mỗi đối tượng Student trong danh sách 'students'.
                - 'iterStat': Biến trạng thái của vòng lặp (iteration status), cung cấp các thông tin hữu ích.
            -->
            <tr th:each="student, iterStat : ${students}">
                <!-- iterStat.count: Trả về số thứ tự của vòng lặp, bắt đầu từ 1. Hoàn hảo cho cột STT. -->
                <td th:text="${iterStat.count}"></td>

                <!-- th:text: Hiển thị giá trị của thuộc tính. An toàn, tự động escape HTML. -->
                <td th:text="${student.name}"></td>
                <td th:text="${student.studentCode}"></td>

                <!-- ĐỊNH DẠNG NGÀY THÁNG:
                     #dates.format(...): Dùng utility object '#dates' để định dạng đối tượng LocalDate.
                -->
                <td th:text="${#dates.format(student.dateOfBirth, 'dd/MM/yyyy')}"></td>

                <!-- HIỂN THỊ ENUM:
                     Truy cập vào phương thức getDisplayName() của Enum để hiển thị tên thân thiện.
                -->
                <td th:text="${student.major.displayName}"></td>
                
                <!-- HIỂN THỊ BOOLEAN:
                     Sử dụng toán tử 3 ngôi để hiển thị văn bản có ý nghĩa hơn thay vì 'true'/'false'.
                     Kết hợp với th:class để thêm màu sắc.
                -->
                <td>
                    <span th:text="${student.agreedToTerms ? 'Đã đồng ý' : 'Chưa đồng ý'}"
                          th:class="${student.agreedToTerms ? 'badge bg-success' : 'badge bg-secondary'}">
                    </span>
                </td>

                <!-- CÁC NÚT HÀNH ĐỘNG (ACTIONS):
                     Tạo URL động bằng cú pháp @{...}
                -->
                <td>
                    <!-- SỬA: URL sẽ có dạng /students/edit/1, /students/edit/2, ... -->
                    <a th:href="@{/students/edit/{id}(id=${student.id})}" class="btn btn-warning btn-sm">Sửa</a>

                    <!-- XÓA: URL sẽ có dạng /students/delete/1, /students/delete/2, ... -->
                    <a th:href="@{/students/delete/{id}(id=${student.id})}" class="btn btn-danger btn-sm"
                       onclick="return confirm('Bạn có chắc chắn muốn xóa sinh viên này?')">Xóa</a>
                </td>
            </tr>
            </tbody>
        </table>

        <!-- Hiển thị tổng số sinh viên -->
        <p class="mt-3">
            <strong>Tổng số sinh viên:</strong>
            <span th:text="${#lists.size(students)}"></span>
        </p>
    </div>
</div>
</body>
</html>
```

### Tóm tắt các điểm chính đã học trong ví dụ này

1.  **Vòng lặp với `th:each`:** Cú pháp `th:each="item, status : ${collection}"` là nền tảng để hiển thị mọi loại danh sách.
2.  **Biến trạng thái `iterStat`:** Cực kỳ hữu ích để lấy thông tin về vòng lặp như `count` (số thứ tự), `index` (chỉ số), `size` (tổng số), `odd`/`even` (chẵn/lẻ),...
3.  **Xử lý điều kiện `th:if`/`th:unless`:** Luôn kiểm tra trường hợp danh sách rỗng để cung cấp trải nghiệm người dùng tốt hơn, thay vì chỉ hiển thị một bảng trống trơn.
4.  **Utility Objects (`#lists`, `#dates`):** Thymeleaf cung cấp nhiều đối tượng tiện ích giúp bạn xử lý dữ liệu ngay trong view mà không cần chuẩn bị trước ở Controller. `#dates.format` là một ví dụ điển hình.
5.  **Tạo URL động với `@{...}`:** Cú pháp này rất mạnh mẽ, cho phép bạn xây dựng các URL phức tạp, bao gồm cả path variables (`{id}`) và request parameters, một cách an toàn và dễ bảo trì.
6.  **Truy cập thuộc tính và phương thức:** Bạn có thể truy cập sâu vào các đối tượng, ví dụ `student.major.displayName` để gọi phương thức `getDisplayName()` của đối tượng `Major` bên trong đối tượng `Student`.