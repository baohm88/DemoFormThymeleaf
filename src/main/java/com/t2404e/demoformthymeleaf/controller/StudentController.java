package com.t2404e.demoformthymeleaf.controller;


import com.t2404e.demoformthymeleaf.model.Major;
import com.t2404e.demoformthymeleaf.model.Student;
import com.t2404e.demoformthymeleaf.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/students") // Đặt tiền tố "/students" cho tất cả các request trong controller này
public class StudentController {

    private StudentRepository studentRepository;

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
        return "student-form"; // Trả về file /resources/templates/student-form.html
    }

    // --- PHƯƠNG THỨC 2: XỬ LÝ SUBMIT FORM ---
    // Xử lý request POST tới "/students/save"
    @PostMapping()
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

            // 2. Trả về lại view "student-form". Thymeleaf sẽ tự động hiển thị lại
            //    các giá trị người dùng đã nhập và các thông báo lỗi tương ứng.
            return "student-form";
        }

        // 3. Nếu không có lỗi, lưu sinh viên vào database
        studentRepository.save(student);

        // 4. Chuyển hướng (redirect) người dùng về trang danh sách sinh viên
        //    Redirect giúp tránh việc người dùng F5 trình duyệt và gửi lại form một lần nữa.
        return "redirect:/students"; // URL của trang danh sách (giả sử có)
    }

    // (Các phương thức khác như list, edit, delete...)
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

    @GetMapping("/edit/{id}")
    public String showEditStudentForm(@PathVariable("id") Long id, Model model) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên ID: " + id));
        model.addAttribute("student", student);
        model.addAttribute("majors", Major.values());
        return "student-form";
    }

    // UPDATE - Xử lý việc cập nhật sinh viên
    @PostMapping("/{id}")
    public String updateStudent(@PathVariable("id") Long id, @Valid Student student, BindingResult result) {
        if (result.hasErrors()) {
            student.setId(id); // Giữ lại id khi trả về form
            return "student-form";
        }
        studentRepository.save(student);
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentRepository.deleteById(id);
        return "redirect:/students";
    }
}