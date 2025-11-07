### **Giới thiệu Spring Security: Xác thực & Phân quyền với Database**

#### **1. Các khái niệm cốt lõi cần nắm**

Trước khi code cần hiểu rõ 4 khái niệm này:

1.  **Authentication (Xác thực - "Bạn là ai?"):**
    *   Là quá trình xác minh danh tính của người dùng.
    *   Cách phổ biến nhất là qua username và password.
    *   Spring Security sẽ so sánh thông tin người dùng cung cấp với thông tin trong database.

2.  **Authorization (Phân quyền - "Bạn được làm gì?"):**
    *   Là quá trình quyết định xem một người dùng đã được xác thực có quyền truy cập vào một tài nguyên cụ thể hay không.
    *   Ví dụ: Chỉ `ADMIN` mới được vào trang `/admin`, còn `USER` chỉ được vào trang `/dashboard`.

3.  **Principal (Đối tượng chính):**
    *   Là thông tin về người dùng đã đăng nhập thành công. Bạn có thể lấy được username, roles, và các thông tin khác từ Principal.

4.  **GrantedAuthority (Quyền hạn được cấp):**
    *   Là một quyền duy nhất được cấp cho Principal. Nó có thể là một **Role** (vai trò, ví dụ: `ROLE_ADMIN`) hoặc một **Permission** (quyền cụ thể, ví dụ: `USER_DELETE`). Spring Security không phân biệt, chúng đều là các `GrantedAuthority`.

#### **2. Thiết kế CSDL (MySQL Schema)**

Để có hệ thống phân quyền linh hoạt, chúng ta sẽ thiết kế 3 bảng chính và 2 bảng trung gian cho quan hệ nhiều-nhiều.

*   `users`: Lưu thông tin người dùng.
*   `roles`: Lưu các vai trò (ADMIN, USER, EDITOR...).
*   `users_roles`: Bảng nối, một user có thể có nhiều role.
*   `permissions`: Lưu các quyền chi tiết (POST_CREATE, POST_DELETE...).
*   `roles_permissions`: Bảng nối, một role có thể có nhiều permission.



#### **3. Cài đặt Dự án**

**a. Dependencies (`pom.xml`)**

```xml
<dependencies>
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <!-- Thymeleaf & Spring Security integration -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>
    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- Lombok (Optional) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

**b. Cấu hình (`application.properties`)**

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/security_demo?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password # Thay bằng mật khẩu MySQL của bạn
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update # Tự động cập nhật schema
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

#### **4. Triển khai các lớp Model (Entities)**

**`Permission.java`**
```java
@Data @Entity
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
}
```

**`Role.java`**
```java
@Data @Entity
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();
}
```

**`User.java` (Lớp quan trọng nhất)**

Lớp này sẽ implement `UserDetails`, một interface cốt lõi của Spring Security để cung cấp thông tin người dùng.

```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
// ... các import khác

@Data @Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // --- Các phương thức của UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Trả về một danh sách các quyền (roles + permissions)
        Set<GrantedAuthority> authorities = new HashSet<>();
        // Thêm các role
        for (Role role : this.roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            // Thêm các permission của role đó
            authorities.addAll(role.getPermissions().stream()
                    .map(p -> new SimpleGrantedAuthority(p.getName()))
                    .collect(Collectors.toSet()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // Các phương thức khác của UserDetails, ta có thể mặc định là true
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return this.enabled; }
}
```

#### **5. Triển khai các lớp Repository & Service**

**`UserRepository.java`**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA sẽ tự tạo query để tìm user theo username
    Optional<User> findByUsername(String username);
}
```

**`CustomUserDetailsService.java` (Cầu nối giữa Spring Security và Database)**

Đây là lớp cực kỳ quan trọng. Spring Security sẽ gọi lớp này khi cần load thông tin user để xác thực.

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong database bằng username
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
```

#### **6. Cấu hình Spring Security (`SecurityConfig.java`)**

Đây là nơi bạn định nghĩa tất cả các quy tắc bảo mật.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Bean để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Sử dụng BCrypt, một thuật toán mã hóa mạnh và phổ biến
        return new BCryptPasswordEncoder();
    }

    // 2. Bean để cấu hình các quy tắc bảo mật
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Vô hiệu hóa CSRF cho mục đích demo (trong thực tế cần cấu hình cẩn thận)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Định nghĩa các quy tắc phân quyền cho các request
            .authorizeHttpRequests(authorize -> authorize
                // Cho phép tất cả mọi người truy cập vào trang chủ, các file css, js
                .requestMatchers("/", "/home", "/css/**", "/js/**").permitAll()
                
                // Yêu cầu quyền 'POST_CREATE' để truy cập vào /posts/new
                .requestMatchers("/posts/new").hasAuthority("POST_CREATE")
                
                // Yêu cầu vai trò 'ADMIN' để truy cập vào tất cả các URL bắt đầu bằng /admin/
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Bất kỳ request nào khác đều yêu cầu phải xác thực (đăng nhập)
                .anyRequest().authenticated()
            )
            
            // Cấu hình form đăng nhập
            .formLogin(formLogin -> formLogin
                // URL của trang đăng nhập (sẽ tạo sau)
                .loginPage("/login")
                // URL xử lý submit form đăng nhập
                .loginProcessingUrl("/perform_login")
                // URL chuyển hướng đến sau khi đăng nhập thành công
                .defaultSuccessUrl("/home", true)
                // URL chuyển hướng đến sau khi đăng nhập thất bại
                .failureUrl("/login?error=true")
                // Cho phép tất cả mọi người truy cập trang đăng nhập
                .permitAll()
            )
            
            // Cấu hình logout
            .logout(logout -> logout
                // URL để thực hiện logout
                .logoutUrl("/perform_logout")
                // Xóa session sau khi logout
                .invalidateHttpSession(true)
                // Xóa cookie JSESSIONID
                .deleteCookies("JSESSIONID")
                // URL chuyển hướng đến sau khi logout thành công
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
```

#### **7. Controller và View để Demo**

**`WebController.java`**
```java
@Controller
public class WebController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "home"; // Tương ứng file home.html
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/posts/new")
    public String createPost() {
        return "create-post";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```

**`templates/login.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <h1>Login</h1>
    <!-- Hiển thị thông báo lỗi nếu có -->
    <div th:if="${param.error}" class="alert alert-danger">Invalid username or password.</div>
    <div th:if="${param.logout}" class="alert alert-success">You have been logged out.</div>

    <form th:action="@{/perform_login}" method="post">
        <div><label> User Name : <input type="text" name="username"/> </label></div>
        <div><label> Password: <input type="password" name="password"/> </label></div>
        <div><input type="submit" value="Sign In"/></div>
    </form>
</body>
</html>
```

**`templates/home.html`**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<body>
    <h1>Welcome to the Home Page!</h1>

    <!-- Hiển thị thông tin người dùng đã đăng nhập -->
    <div sec:authorize="isAuthenticated()">
        <p>Welcome, <span sec:authentication="name"></span>!</p>
        <p>Your roles: <span sec:authentication="principal.authorities"></span></p>
        <form th:action="@{/perform_logout}" method="post">
            <input type="submit" value="Logout"/>
        </form>
    </div>

    <!-- Hiển thị link nếu chưa đăng nhập -->
    <div sec:authorize="isAnonymous()">
        <p><a th:href="@{/login}">Login</a></p>
    </div>

    <!-- Hiển thị link chỉ dành cho ADMIN -->
    <div sec:authorize="hasRole('ROLE_ADMIN')">
        <p><a th:href="@{/admin/dashboard}">Admin Dashboard</a></p>
    </div>

    <!-- Hiển thị link chỉ cho người có quyền POST_CREATE -->
    <div sec:authorize="hasAuthority('POST_CREATE')">
        <p><a th:href="@{/posts/new}">Create New Post</a></p>
    </div>
</body>
</html>
```

#### **8. Khởi tạo dữ liệu mẫu**

Để có dữ liệu để test, hãy tạo một `CommandLineRunner`.

```java
@Component
public class DataInitializer implements CommandLineRunner {
    // ... Inject repositories and PasswordEncoder ...

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Tạo permissions
        Permission createUser = new Permission(); createUser.setName("USER_CREATE");
        Permission viewPost = new Permission(); viewPost.setName("POST_VIEW");
        Permission createPost = new Permission(); createPost.setName("POST_CREATE");
        permissionRepository.saveAll(List.of(createUser, viewPost, createPost));

        // Tạo roles
        Role roleAdmin = new Role(); roleAdmin.setName("ROLE_ADMIN");
        roleAdmin.setPermissions(Set.of(createUser, viewPost, createPost));
        Role roleUser = new Role(); roleUser.setName("ROLE_USER");
        roleUser.setPermissions(Set.of(viewPost));
        roleRepository.saveAll(List.of(roleAdmin, roleUser));

        // Tạo users
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(roleAdmin));
        userRepository.save(admin);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRoles(Set.of(roleUser));
        userRepository.save(user);
    }
}
```

#### **9. Tóm tắt luồng hoạt động**

1.  Người dùng truy cập trang `/login` và nhập username/password.
2.  Form được submit đến `/perform_login`. Spring Security chặn request này.
3.  Nó gọi `CustomUserDetailsService.loadUserByUsername()` để lấy thông tin `User` từ MySQL.
4.  Nó lấy mật khẩu đã được mã hóa (`BCrypt`) từ đối tượng `User` và so sánh với mật khẩu người dùng nhập (sau khi đã mã hóa).
5.  Nếu khớp, người dùng được xác thực. Session được tạo.
6.  Spring Security chuyển hướng người dùng đến `/home` (`defaultSuccessUrl`).
7.  Khi người dùng truy cập các trang khác (ví dụ `/admin/dashboard`), Spring Security sẽ kiểm tra các `GrantedAuthority` của người dùng hiện tại (`hasRole('ADMIN')`) để quyết định có cho phép truy cập hay không.