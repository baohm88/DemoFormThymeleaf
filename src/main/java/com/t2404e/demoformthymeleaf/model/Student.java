package com.t2404e.demoformthymeleaf.model;

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