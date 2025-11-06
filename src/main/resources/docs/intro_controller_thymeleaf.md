### Bước 1: Khởi tạo dự án và Dependencies (`pom.xml`)

Cần các dependencies sau:
*   `spring-boot-starter-web`: Cho MVC và `@Controller`.
*   `spring-boot-starter-thymeleaf`: Để tích hợp Thymeleaf.
*   `spring-boot-starter-data-jpa`: Để làm việc với database.
*   `h2`: Một CSDL in-memory để chạy demo nhanh chóng.
*   `lombok`: (Tùy chọn) Để giảm code boilerplate.
*   `spring-boot-starter-validation`: (Nên có) Để xác thực dữ liệu form.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version> <!-- Phiên bản LTS ổn định, hoàn toàn tương thích -->
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>thymeleaf-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>thymeleaf-crud</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Bước 2: Hướng dẫn cấu hình Thymeleaf và Database

#### Cấu hình Thymeleaf

Tin vui là: với `spring-boot-starter-thymeleaf`, bạn **hầu như không cần cấu hình gì cả!** Spring Boot sẽ tự động cấu hình (autoconfiguration) Thymeleaf với các cài đặt mặc định hợp lý:

1.  **Vị trí template:** Nó sẽ tự động tìm các file HTML trong thư mục `src/main/resources/templates/`.
2.  **Hậu tố (Suffix):** Nó mặc định tìm các file có đuôi `.html`.
3.  **Chế độ (Mode):** Mặc định là `HTML5`.

Vì vậy, bạn chỉ cần tạo các file HTML trong đúng thư mục là được.

#### Cấu hình Database (`application.properties`)

Mở file `src/main/resources/application.properties` và thêm cấu hình cho H2 database.

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:studentdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# Enable H2 Console
spring.h2.console.enabled=true

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

### Bước 3: Tạo Model (Entity)

Đây là đối tượng đại diện cho sinh viên trong database.

`src/main/java/com/example/thymeleafcrud/model/Student.java`
```java
package com.example.thymeleafcrud.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
}
```

---

### Bước 4: Tạo Repository

Interface này sẽ giúp chúng ta thực hiện các thao tác CRUD với database một cách dễ dàng.

`src/main/java/com/example/thymeleafcrud/repository/StudentRepository.java`
```java
package com.example.thymeleafcrud.repository;

import com.example.thymeleafcrud.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
```

---

### Bước 5: Tạo Controller

Đây là nơi xử lý các request từ người dùng và trả về các view Thymeleaf.

`src/main/java/com/example/thymeleafcrud/controller/StudentController.java`
```java
package com.example.thymeleafcrud.controller;

import com.example.thymeleafcrud.model.Student;
import com.example.thymeleafcrud.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // READ - Hiển thị danh sách tất cả sinh viên
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        return "students"; // Trả về file templates/students.html
    }

    // CREATE - Hiển thị form để thêm mới sinh viên
    @GetMapping("/students/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        return "student-form"; // Trả về file templates/student-form.html
    }

    // CREATE - Xử lý việc thêm mới sinh viên
    @PostMapping("/students")
    public String createStudent(@Valid Student student, BindingResult result) {
        if (result.hasErrors()) {
            return "student-form"; // Nếu có lỗi, quay lại form và hiển thị lỗi
        }
        studentRepository.save(student);
        return "redirect:/students"; // Chuyển hướng về trang danh sách
    }

    // UPDATE - Hiển thị form để chỉnh sửa sinh viên
    @GetMapping("/students/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("student", student);
        return "student-form"; // Dùng chung form thêm mới và cập nhật
    }

    // UPDATE - Xử lý việc cập nhật sinh viên
    @PostMapping("/students/{id}")
    public String updateStudent(@PathVariable("id") Long id, @Valid Student student, BindingResult result) {
        if (result.hasErrors()) {
            student.setId(id); // Giữ lại id khi trả về form
            return "student-form";
        }
        studentRepository.save(student);
        return "redirect:/students";
    }

    // DELETE - Xóa sinh viên
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        studentRepository.delete(student);
        return "redirect:/students";
    }
}
```

---

### Bước 6: Tạo Views (Thymeleaf Templates)

Tạo thư mục `src/main/resources/templates`. Bên trong đó, tạo 2 file HTML sau:

#### a. `students.html` (Trang danh sách)

`src/main/resources/templates/students.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Danh sách sinh viên</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container mt-4">
    <h1>Danh sách Sinh viên</h1>
    <p>
        <a th:href="@{/students/new}" class="btn btn-primary">Thêm sinh viên mới</a>
    </p>
    <table class="table table-bordered table-striped">
        <thead class="table-dark">
        <tr>
            <th>ID</th>
            <th>Tên</th>
            <th>Email</th>
            <th>Hành động</th>
        </tr>
        </thead>
        <tbody>
        <!-- Dùng th:each để lặp qua danh sách sinh viên -->
        <tr th:each="student : ${students}">
            <td th:text="${student.id}"></td>
            <td th:text="${student.name}"></td>
            <td th:text="${student.email}"></td>
            <td>
                <!-- Dùng @{...} để tạo URL động -->
                <a th:href="@{/students/edit/{id}(id=${student.id})}" class="btn btn-warning btn-sm">Sửa</a>
                <a th:href="@{/students/delete/{id}(id=${student.id})}" class="btn btn-danger btn-sm"
                   onclick="return confirm('Bạn có chắc chắn muốn xóa?')">Xóa</a>
            </td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>
```

#### b. `student-form.html` (Form thêm/sửa)

File này được dùng cho cả việc tạo mới và cập nhật.

`src/main/resources/templates/student-form.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Form Sinh viên</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container mt-4">
    <!-- Thay đổi tiêu đề và action của form dựa vào việc student có id hay chưa -->
    <h1 th:if="${student.id == null}">Thêm Sinh viên mới</h1>
    <h1 th:unless="${student.id == null}">Chỉnh sửa Sinh viên</h1>
    
    <!-- th:object liên kết form với đối tượng 'student' trong Model -->
    <!-- th:action sẽ tự động tạo URL đúng cho create hoặc update -->
    <form th:action="${student.id == null} ? @{/students} : @{/students/{id}(id=${student.id})}"
          th:object="${student}" method="post">
        
        <!-- Input cho 'name' -->
        <div class="mb-3">
            <label for="name" class="form-label">Tên:</label>
            <!-- th:field="*{name}" tự động bind value và name cho input -->
            <input type="text" th:field="*{name}" id="name" class="form-control" />
            <!-- Hiển thị lỗi validation nếu có -->
            <div class="text-danger" th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></div>
        </div>
        
        <!-- Input cho 'email' -->
        <div class="mb-3">
            <label for="email" class="form-label">Email:</label>
            <input type="text" th:field="*{email}" id="email" class="form-control" />
            <div class="text-danger" th:if="${#fields.hasErrors('email')}" th:errors="*{email}"></div>
        </div>
        
        <button type="submit" class="btn btn-success">Lưu</button>
        <a th:href="@{/students}" class="btn btn-secondary">Hủy</a>
    </form>
</div>
</body>
</html>
```

---

### Bước 7: Chạy ứng dụng

1.  Chạy file main `ThymeleafCrudApplication.java`.
2.  Mở trình duyệt và truy cập: `http://localhost:8080/students`.
3.  Bạn sẽ thấy trang danh sách (ban đầu sẽ trống).
4.  Nhấn "Thêm sinh viên mới", điền thông tin và lưu lại.
5.  Thử sửa, xóa các sinh viên bạn vừa tạo.
6.  (Tùy chọn) Truy cập `http://localhost:8080/h2-console` để xem dữ liệu trực tiếp trong database. Nhớ điền thông tin JDBC URL là `jdbc:h2:mem:studentdb`.