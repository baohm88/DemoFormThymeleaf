Xây dựng một form **"Thêm sinh viên mới"** với nhiều loại input khác nhau (text, textarea, select, radio, checkbox, date) và xử lý validation một cách rõ ràng.

---

### Bước 1: Model `Student.java` - Nền tảng của Form

Chúng ta sẽ mở rộng model `Student` để có nhiều thuộc tính hơn, mỗi thuộc tính sẽ tương ứng với một loại input trên form. Các annotation validation (`@NotBlank`, `@Size`, `@Past`...) sẽ được sử dụng để kiểm tra dữ liệu.

`src/main/java/com/example/thymeleafcrud/model/Student.java`
```java
package com.example.thymeleafcrud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Tương ứng với <input type="text"> ---
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 3, max = 50, message = "Họ và tên phải từ 3 đến 50 ký tự")
    private String name;

    // --- Tương ứng với <input type="text"> ---
    @NotBlank(message = "Mã sinh viên không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{8}$", message = "Mã sinh viên phải gồm 8 ký tự viết hoa hoặc số")
    private String studentCode;

    // --- Tương ứng với <input type="date"> ---
    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Định dạng date khi Spring bind dữ liệu
    private LocalDate dateOfBirth;

    // --- Tương ứng với <select> (dropdown) ---
    @NotNull(message = "Vui lòng chọn chuyên ngành")
    @Enumerated(EnumType.STRING) // Lưu tên của Enum (e.g., "IT", "BUSINESS") vào DB
    private Major major;

    // --- Tương ứng với <textarea> ---
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;
    
    // --- Tương ứng với radio buttons ---
    @NotBlank(message = "Vui lòng chọn giới tính")
    private String gender; // "Nam", "Nữ", "Khác"
    
    // --- Tương ứng với checkbox ---
    @AssertTrue(message = "Bạn phải đồng ý với điều khoản")
    private boolean agreedToTerms;
}
```

### Bước 2: Enum `Major.java` - Dữ liệu cho Dropdown

Tạo một `Enum` để định nghĩa các lựa chọn cho chuyên ngành. Điều này giúp code sạch sẽ và dễ bảo trì.

`src/main/java/com/example/thymeleafcrud/model/Major.java`
```java
package com.example.thymeleafcrud.model;

// Enum này sẽ là nguồn dữ liệu cho thẻ <select> trong form
public enum Major {
    INFORMATION_TECHNOLOGY("Công nghệ thông tin"),
    BUSINESS_ADMINISTRATION("Quản trị kinh doanh"),
    GRAPHIC_DESIGN("Thiết kế đồ họa"),
    MARKETING("Marketing");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### Bước 3: Controller `StudentController.java` - Xử lý logic

Controller sẽ có hai phương thức quan trọng cho việc tạo mới:
1.  `showNewStudentForm()`: (GET request) Dùng để **hiển thị** form rỗng.
2.  `saveStudent()`: (POST request) Dùng để **xử lý dữ liệu** người dùng gửi lên từ form.

`src/main/java/com/example/thymeleafcrud/controller/StudentController.java`
```java
package com.example.thymeleafcrud.controller;

import com.example.thymeleafcrud.model.Major;
import com.example.thymeleafcrud.model.Student;
import com.example.thymeleafcrud.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/students") // Đặt tiền tố "/students" cho tất cả các request trong controller này
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // --- PHƯƠNG THỨC 1: HIỂN THỊ FORM ---
    // Xử lý request GET tới "/students/new"
    @GetMapping("/new")
    public String showNewStudentForm(Model model) {
        // 1. Tạo một đối tượng Student rỗng để Thymeleaf có thể bind dữ liệu vào
        model.addAttribute("student", new Student());

        // 2. Lấy danh sách tất cả các chuyên ngành từ Enum và đưa vào model
        //    để Thymeleaf có thể render các <option> trong dropdown
        model.addAttribute("majors", Major.values());

        // 3. Trả về tên của file template HTML
        return "add-student"; // Trả về file /resources/templates/add-student.html
    }

    // --- PHƯƠNG THỨC 2: XỬ LÝ SUBMIT FORM ---
    // Xử lý request POST tới "/students/save"
    @PostMapping("/save")
    public String saveStudent(
            // @Valid: Kích hoạt việc kiểm tra validation đã định nghĩa trong model Student
            // @ModelAttribute: Lấy đối tượng "student" từ form đã submit và bind vào biến student
            @Valid @ModelAttribute("student") Student student,
            // BindingResult: Chứa kết quả của việc validation, nó PHẢI đứng ngay sau đối tượng được validate
            BindingResult bindingResult,
            Model model
    ) {
        // 1. Kiểm tra xem có lỗi validation không
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi, chúng ta KHÔNG lưu vào DB mà trả lại form để người dùng sửa
            System.out.println("Có lỗi validation!");

            // !!! QUAN TRỌNG: Khi trả lại form, chúng ta phải cung cấp lại các dữ liệu
            // cần thiết cho form, ví dụ như danh sách chuyên ngành cho dropdown.
            model.addAttribute("majors", Major.values());

            // 2. Trả về lại view "add-student". Thymeleaf sẽ tự động hiển thị lại
            //    các giá trị người dùng đã nhập và các thông báo lỗi tương ứng.
            return "add-student";
        }

        // 3. Nếu không có lỗi, lưu sinh viên vào database
        studentRepository.save(student);

        // 4. Chuyển hướng (redirect) người dùng về trang danh sách sinh viên
        //    Redirect giúp tránh việc người dùng F5 trình duyệt và gửi lại form một lần nữa.
        return "redirect:/students"; // URL của trang danh sách (giả sử có)
    }
    
    // (Các phương thức khác như list, edit, delete...)
    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "students"; // Giả sử có file students.html
    }
}
```

### Bước 4: View `add-student.html` - Trái tim của ví dụ

Đây là file Thymeleaf, nơi chúng ta sử dụng các thuộc tính `th:*` để tạo form động.

`src/main/resources/templates/add-student.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Thêm Sinh viên mới</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header bg-primary text-white">
                    <h3>Thêm Sinh viên mới</h3>
                </div>
                <div class="card-body">
                    <!--
                        th:action="@{/students/save}" -> Form sẽ được submit tới URL /students/save
                        method="post" -> Sử dụng HTTP POST
                        th:object="${student}" -> Rất quan trọng! Liên kết form này với đối tượng 'student' trong Model.
                                                Điều này cho phép chúng ta dùng cú pháp *{...} bên trong.
                    -->
                    <form th:action="@{/students/save}" method="post" th:object="${student}">

                        <!-- 1. Text Input: Họ và tên -->
                        <div class="mb-3">
                            <label for="name" class="form-label">Họ và tên:</label>
                            <!--
                                th:field="*{name}" -> Thẻ ma thuật của Thymeleaf. Nó sẽ tự động tạo:
                                1. id="name"
                                2. name="name"
                                3. value="<giá trị của student.name>" (sẽ rỗng lần đầu, nhưng sẽ giữ lại giá trị nếu validation fail)
                            -->
                            <input type="text" id="name" class="form-control" th:field="*{name}" placeholder="Nguyễn Văn A">
                            <!--
                                #fields.hasErrors('name') -> Kiểm tra xem có lỗi validation cho trường 'name' không.
                                th:errors="*{name}" -> Nếu có, hiển thị message lỗi đã định nghĩa trong model.
                            -->
                            <div class="text-danger" th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></div>
                        </div>

                        <!-- 2. Text Input: Mã sinh viên -->
                        <div class="mb-3">
                            <label for="studentCode" class="form-label">Mã sinh viên:</label>
                            <input type="text" id="studentCode" class="form-control" th:field="*{studentCode}" placeholder="SV240001">
                            <div class="text-danger" th:if="${#fields.hasErrors('studentCode')}" th:errors="*{studentCode}"></div>
                        </div>

                        <!-- 3. Date Input: Ngày sinh -->
                        <div class="mb-3">
                            <label for="dateOfBirth" class="form-label">Ngày sinh:</label>
                            <!-- th:field hoạt động hoàn hảo với input type="date" -->
                            <input type="date" id="dateOfBirth" class="form-control" th:field="*{dateOfBirth}">
                            <div class="text-danger" th:if="${#fields.hasErrors('dateOfBirth')}" th:errors="*{dateOfBirth}"></div>
                        </div>

                        <!-- 4. Select/Dropdown: Chuyên ngành -->
                        <div class="mb-3">
                            <label for="major" class="form-label">Chuyên ngành:</label>
                            <select id="major" class="form-select" th:field="*{major}">
                                <option value="">-- Chọn chuyên ngành --</option>
                                <!--
                                    th:each="m : ${majors}" -> Lặp qua danh sách 'majors' đã được truyền từ Controller.
                                    th:value="${m}" -> Giá trị của option sẽ là tên của Enum (e.g., "INFORMATION_TECHNOLOGY").
                                    th:text="${m.displayName}" -> Văn bản hiển thị cho người dùng (e.g., "Công nghệ thông tin").
                                    th:field ở thẻ <select> sẽ tự động chọn đúng <option> nếu form được load lại sau khi validation fail.
                                -->
                                <option th:each="m : ${majors}" th:value="${m}" th:text="${m.displayName}"></option>
                            </select>
                            <div class="text-danger" th:if="${#fields.hasErrors('major')}" th:errors="*{major}"></div>
                        </div>
                        
                        <!-- 5. Radio Buttons: Giới tính -->
                        <div class="mb-3">
                             <label class="form-label">Giới tính:</label>
                             <div class="form-check form-check-inline">
                                 <!-- Mỗi radio button cũng dùng th:field="*{gender}" để chúng được nhóm lại với nhau -->
                                 <input class="form-check-input" type="radio" th:field="*{gender}" id="male" value="Nam">
                                 <label class="form-check-label" for="male">Nam</label>
                             </div>
                             <div class="form-check form-check-inline">
                                 <input class="form-check-input" type="radio" th:field="*{gender}" id="female" value="Nữ">
                                 <label class="form-check-label" for="female">Nữ</label>
                             </div>
                             <div class="text-danger" th:if="${#fields.hasErrors('gender')}" th:errors="*{gender}"></div>
                        </div>

                        <!-- 6. Textarea: Ghi chú -->
                        <div class="mb-3">
                            <label for="notes" class="form-label">Ghi chú:</label>
                            <textarea id="notes" class="form-control" rows="3" th:field="*{notes}"></textarea>
                            <div class="text-danger" th:if="${#fields.hasErrors('notes')}" th:errors="*{notes}"></div>
                        </div>
                        
                        <!-- 7. Checkbox: Điều khoản -->
                        <div class="mb-3 form-check">
                            <input type="checkbox" class="form-check-input" id="agreedToTerms" th:field="*{agreedToTerms}">
                            <label class="form-check-label" for="agreedToTerms">Tôi đồng ý với các điều khoản</label>
                            <div class="text-danger" th:if="${#fields.hasErrors('agreedToTerms')}" th:errors="*{agreedToTerms}"></div>
                        </div>

                        <!-- Nút Submit và Hủy -->
                        <div class="d-flex justify-content-end">
                            <a th:href="@{/students}" class="btn btn-secondary me-2">Quay lại danh sách</a>
                            <button type="submit" class="btn btn-primary">Lưu Sinh viên</button>
                        </div>

                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
```

### Tóm tắt các điểm chính

1.  **Model-View-Controller:**
    *   **Model (`Student`):** Định nghĩa dữ liệu và các quy tắc validation.
    *   **Controller (`StudentController`):** Chuẩn bị dữ liệu (`new Student()`, `Major.values()`) cho view và xử lý dữ liệu submit từ view.
    *   **View (`add-student.html`):** Hiển thị form và các thông báo lỗi cho người dùng.

2.  **Sức mạnh của `th:field`:** Đây là thuộc tính quan trọng nhất khi làm việc với form. Nó tự động xử lý `id`, `name`, `value` và giữ lại trạng thái của input (giá trị đã nhập, lựa chọn đã chọn) khi form được tải lại do lỗi, mang lại trải nghiệm người dùng tốt hơn rất nhiều.

3.  **Xử lý lỗi Validation:**
    *   Dùng `@Valid` trong Controller để kích hoạt validation.
    *   `BindingResult` ngay sau đó để bắt lỗi.
    *   `#fields.hasErrors(...)` và `th:errors` trong Thymeleaf để hiển thị lỗi một cách có điều kiện.

4.  **Redirect sau khi POST thành công:** Luôn sử dụng `redirect:/...` sau khi xử lý thành công một request POST để tránh việc người dùng vô tình gửi lại dữ liệu bằng cách nhấn F5.