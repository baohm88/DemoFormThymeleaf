### Bối cảnh ví dụ

Để các ví dụ dễ hiểu, hãy giả sử trong Controller, chúng ta truyền các đối tượng sau vào `Model`:

**Java Controller Code:**
```java
// Giả sử có một class User
public class User {
    private Long id;
    private String username;
    private String email;
    private String role; // "ADMIN", "USER"
    private boolean active;
    private Date creationDate;
    private String bio; // Có thể chứa mã HTML
}

@Controller
public class DemoController {
    @GetMapping("/demo")
    public String showDemo(Model model) {
        User user = new User(1L, "john_doe", "john@example.com", "ADMIN", true, new Date(), "Đây là <b>tiểu sử</b> của tôi.");
        List<User> userList = List.of(
            new User(1L, "john_doe", "john@example.com", "ADMIN", true, new Date(), ""),
            new User(2L, "jane_smith", "jane@example.com", "USER", false, new Date(), "")
        );

        model.addAttribute("currentUser", user);
        model.addAttribute("allUsers", userList);
        return "demo";
    }
}
```

Tất cả các ví dụ Thymeleaf dưới đây sẽ dựa trên các đối tượng `currentUser` và `allUsers` này.

---

### Tổng hợp các thuộc tính Thymeleaf phổ biến nhất

### 1. Hiển thị văn bản và giá trị

| Thuộc tính | Chức năng | Ví dụ | Kết quả HTML |
| :--- | :--- | :--- | :--- |
| **`th:text`** | Thay thế nội dung bên trong thẻ bằng một giá trị. **An toàn**, sẽ mã hóa các ký tự HTML (ví dụ `<b>` thành `&lt;b&gt;`). Luôn ưu tiên dùng. | `<p th:text="${currentUser.username}">Tên người dùng</p>` | `<p>john_doe</p>` |
| **`th:utext`** | (Unescaped Text) Giống `th:text` nhưng **không an toàn**. Nó sẽ hiển thị trực tiếp HTML. Chỉ dùng khi bạn tin tưởng 100% nguồn dữ liệu. | `<div th:utext="${currentUser.bio}"></div>` | `<div>Đây là <b>tiểu sử</b> của tôi.</div>` |

### 2. Thao tác với thuộc tính HTML

| Thuộc tính | Chức năng | Ví dụ | Kết quả HTML |
| :--- | :--- | :--- | :--- |
| **`th:href`** | Đặt giá trị cho thuộc tính `href`. Sử dụng cú pháp `@{...}` để tạo URL động. | `<a th:href="@{/users/{id}(id=${currentUser.id})}">Hồ sơ</a>` | `<a href="/users/1">Hồ sơ</a>` |
| **`th:src`** | Đặt giá trị cho thuộc tính `src` (dùng cho ảnh, script...). | `<img th:src="@{/images/profile.png}" />` | `<img src="/images/profile.png" />` |
| **`th:value`** | Đặt giá trị cho thuộc tính `value` (thường dùng trong form). | `<input type="text" th:value="${currentUser.username}" />` | `<input type="text" value="john_doe" />` |
| **`th:class`** | Đặt giá trị cho thuộc tính `class`. | `<div th:class="${currentUser.active} ? 'active-user' : 'inactive-user'"></div>` | `<div class="active-user"></div>` |
| **`th:classappend`**| Thêm một class vào danh sách class đã có. Rất hữu ích. | `<div class="card" th:classappend="${currentUser.role == 'ADMIN'} ? 'admin-card'"></div>` | `<div class="card admin-card"></div>` |
| **`th:styleappend`**| Thêm CSS inline vào thuộc tính `style`. | `<p style="font-size: 16px;" th:styleappend="${!currentUser.active} ? 'color: red;'">Tình trạng</p>` | `<p style="font-size: 16px; color: red;">Tình trạng</p>` |
| **`th:attr`** | Cho phép đặt nhiều thuộc tính cùng lúc hoặc các thuộc tính không có shortcut. | `<button th:attr="data-userid=${currentUser.id}, disabled=${!currentUser.active}">Xóa</button>` | `<button data-userid="1" disabled="false">Xóa</button>` |

### 3. Vòng lặp (Iteration)

| Thuộc tính | Chức năng | Ví dụ |
| :--- | :--- | :--- |
| **`th:each`** | Lặp qua một danh sách (Collection) hoặc mảng. Thẻ chứa `th:each` sẽ được lặp lại cho mỗi phần tử. | `<tbody> <tr th:each="user, iterStat : ${allUsers}"> <td th:text="${iterStat.count}"></td> <td th:text="${user.username}"></td> <td th:text="${user.email}"></td> </tr></tbody>` |
| **Biến trạng thái `iterStat`** | Cung cấp thông tin về vòng lặp. Rất hữu ích.<br>- `iterStat.index`: Index bắt đầu từ 0.<br>- `iterStat.count`: Đếm bắt đầu từ 1.<br>- `iterStat.size`: Tổng số phần tử.<br>- `iterStat.current`: Phần tử hiện tại (giống `user`).<br>- `iterStat.even`/`iterStat.odd`: `true`/`false` cho hàng chẵn/lẻ.<br>- `iterStat.first`/`iterStat.last`: `true`/`false` cho phần tử đầu/cuối. | `<tr th:each="u : ${allUsers}" th:classappend="${iterStat.odd} ? 'table-secondary'">...</tr>` |

### 4. Điều kiện (Conditionals)

| Thuộc tính | Chức năng | Ví dụ |
| :--- | :--- | :--- |
| **`th:if`** | Chỉ render thẻ và nội dung của nó nếu điều kiện là `true`. | `<a th:if="${currentUser.role == 'ADMIN'}" th:href="@{/admin}">Trang quản trị</a>` |
| **`th:unless`** | Ngược lại với `th:if`. Render nếu điều kiện là `false`. | `<p th:unless="${currentUser.active}">Tài khoản này đã bị khóa.</p>` |
| **`th:switch`, `th:case`** | Tương tự cấu trúc `switch-case` trong Java. | `<div th:switch="${currentUser.role}"> <p th:case="'ADMIN'">Người dùng là Quản trị viên.</p> <p th:case="'USER'">Người dùng là thành viên.</p> <p th:case="*">Vai trò không xác định.</p></div>` |

### 5. Xử lý Form (Rất quan trọng cho CRUD)

| Thuộc tính | Chức năng | Ví dụ |
| :--- | :--- | :--- |
| **`th:action`** | Đặt URL submit cho form. Dùng `@{...}`. | `<form th:action="@{/users/save}" method="post">...</form>` |
| **`th:object`** | Liên kết toàn bộ form với một đối tượng trong model. Cho phép sử dụng cú pháp `*{...}` bên trong form. | `<form th:action="@{/users/save}" th:object="${currentUser}" method="post"> ... </form>` |
| **`th:field`** | **Thuộc tính ma thuật!** Tự động liên kết một trường input với một thuộc tính của đối tượng `th:object`. Nó sẽ tự sinh `id`, `name`, và `value`. | `<input type="text" th:field="*{username}" />` <br>*(Sẽ sinh ra: `id="username" name="username" value="john_doe"`)* |
| **`th:errors`** | Hiển thị lỗi validation cho một trường cụ thể. | `<div class="error" th:if="${#fields.hasErrors('username')}" th:errors="*{username}"></div>` |
| **`th:selected`** | Dùng trong thẻ `<option>` để chọn một giá trị mặc định. | `<option th:each="r : ${roles}" th:value="${r}" th:text="${r}" th:selected="${r == currentUser.role}"></option>` |

### 6. Template Fragments (Tái sử dụng code)

Dùng để xây dựng layout chung (header, footer, sidebar...).

| Thuộc tính | Chức năng | Ví dụ |
| :--- | :--- | :--- |
| **`th:fragment`** | Định nghĩa một đoạn HTML có thể tái sử dụng. | **File: `fragments/header.html`**<br>`<nav th:fragment="main-header" class="navbar">...</nav>` |
| **`th:insert`** | Chèn nội dung của một fragment **vào bên trong** thẻ hiện tại. | **File: `home.html`**<br>`<div th:insert="~{fragments/header :: main-header}"></div>`<br>Kết quả: `<div><nav class="navbar">...</nav></div>` |
| **`th:replace`** | **Thay thế** thẻ hiện tại bằng nội dung của fragment. | **File: `home.html`**<br>`<div th:replace="~{fragments/header :: main-header}"></div>`<br>Kết quả: `<nav class="navbar">...</nav>` |

Cú pháp: `~{đường_dẫn :: tên_fragment}`

### 7. Các thẻ đặc biệt và Utility Objects

| Thuộc tính/Đối tượng | Chức năng | Ví dụ |
| :--- | :--- | :--- |
| **`th:block`** | Một thẻ "vô hình". Nó không render ra HTML nhưng có thể chứa các logic như `th:each`, `th:if`. Rất hữu ích để lặp mà không cần tạo thẻ `div` hay `span` thừa. | `<th:block th:each="user : ${allUsers}"> <div th:text="${user.username}"></div> <hr/> </th:block>` |
| **`#dates`** | Utility object để định dạng ngày tháng. | `<td><span th:text="${#dates.format(currentUser.creationDate, 'dd/MM/yyyy HH:mm')}"></span></td>` |
| **`#strings`** | Utility object cho chuỗi (`isEmpty`, `contains`, `toUpperCase`...). | `<div th:if="${#strings.isEmpty(currentUser.email)}">Chưa có email</div>` |
| **`#numbers`** | Utility object cho số (định dạng tiền tệ, phần trăm...). | `<p th:text="${#numbers.formatDecimal(price, 1, 'POINT', 2, 'COMMA')}"></p>` |
| **`#lists`, `#arrays`** | Utility object cho list/array (`size`, `isEmpty`...). | `<p>Tổng số người dùng: <span th:text="${#lists.size(allUsers)}"></span></p>` |