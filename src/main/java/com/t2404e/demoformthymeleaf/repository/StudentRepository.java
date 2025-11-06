package com.t2404e.demoformthymeleaf.repository;

import com.t2404e.demoformthymeleaf.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
