### Synchronous vs Asynchronous

**Đồng bộ (Synchronous / Blocking):**
-   **Giống như xếp hàng:** Bạn đến một quán cà phê, gọi món và **đứng chờ** cho đến khi nhận được cà phê mới có thể rời đi. Công việc tiếp theo (rời đi) bị **chặn (block)** bởi công việc hiện tại (chờ cà phê).
-   **Trong lập trình:** Một câu lệnh phải thực thi xong hoàn toàn thì câu lệnh tiếp theo mới được bắt đầu. Luồng thực thi (thread) bị chặn lại.

**Bất đồng bộ (Asynchronous / Non-blocking):**
-   **Giống như nhận số thứ tự:** Bạn đến quán cà phê, gọi món, nhận một thiết bị rung báo hiệu. Sau đó bạn có thể đi tìm chỗ ngồi, lướt điện thoại... làm việc khác. Khi cà phê xong, thiết bị sẽ rung và bạn đến lấy. Công việc (chờ cà phê) diễn ra trong nền và bạn không bị chặn.
-   **Trong lập trình:** Một tác vụ được khởi tạo (ví dụ: gọi API, đọc file), và chương trình có thể tiếp tục thực thi các câu lệnh khác ngay lập tức mà không cần chờ tác vụ đó hoàn thành. Khi tác vụ xong, chương trình sẽ được thông báo (thông qua callback, promise, future...) để xử lý kết quả.

---

## 1. Đồng bộ và Bất đồng bộ trong JAVA

Java được xây dựng trên mô hình **đa luồng (multi-threading)**. Đây là cách tiếp cận cốt lõi của nó để xử lý đồng thời (concurrency).

### a. Đồng bộ trong Java (Mặc định)

Mọi đoạn code Java bạn viết theo cách thông thường đều là đồng bộ.

```java
public class SynchronousExample {

    public static void main(String[] args) {
        System.out.println("Bắt đầu công việc chính...");
        
        String data = fetchDataFromServer(); // Luồng chính bị BLOCK ở đây, chờ 5 giây
        
        System.out.println("Dữ liệu nhận được: " + data);
        System.out.println("Kết thúc công việc chính.");
    }

    public static String fetchDataFromServer() {
        System.out.println("Bắt đầu tải dữ liệu từ server...");
        try {
            // Giả lập một tác vụ tốn thời gian như gọi API
            Thread.sleep(5000); // Tạm dừng luồng hiện tại trong 5 giây
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("... Tải dữ liệu xong.");
        return "Dữ liệu server";
    }
}
```

**Kết quả:**
```
Bắt đầu công việc chính...
Bắt đầu tải dữ liệu từ server...
(chờ 5 giây)
... Tải dữ liệu xong.
Dữ liệu nhận được: Dữ liệu server
Kết thúc công việc chính.
```
Luồng `main` đã phải dừng lại hoàn toàn trong 5 giây.

### b. Bất đồng bộ trong Java

Để đạt được tính bất đồng bộ, Java tạo ra **một luồng mới** để thực hiện tác vụ tốn thời gian, cho phép luồng gốc tiếp tục công việc của nó.

#### Cách cổ điển: Dùng `Thread`
```java
// Tạo một luồng mới để chạy tác vụ
new Thread(() -> {
    String data = fetchDataFromServer(); // Tác vụ này chạy trên luồng mới
    System.out.println("Callback: Dữ liệu nhận được trên luồng phụ: " + data);
}).start();

System.out.println("Luồng chính không bị chặn và tiếp tục chạy...");
```

#### Cách hiện đại (Java 8+): Dùng `CompletableFuture`

`CompletableFuture` là một công cụ mạnh mẽ, tương tự như `Promise` trong JavaScript, cho phép bạn xử lý kết quả của một tác vụ bất đồng bộ một cách linh hoạt.

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AsynchronousExample {

    public static void main(String[] args) throws Exception {
        System.out.println("Luồng chính: Bắt đầu công việc.");

        // Yêu cầu thực hiện tác vụ fetchDataFromServer một cách bất đồng bộ
        CompletableFuture.supplyAsync(AsynchronousExample::fetchDataFromServer)
            // .thenAccept() là một callback, sẽ được gọi khi future hoàn thành
            .thenAccept(data -> {
                System.out.println("Callback trên luồng khác: Dữ liệu nhận được là '" + data + "'");
            });

        System.out.println("Luồng chính: Đã yêu cầu tải dữ liệu và tiếp tục làm việc khác mà không bị chặn.");
        
        // Giữ luồng chính sống để xem kết quả từ luồng phụ
        Thread.sleep(6000); 
    }

    public static String fetchDataFromServer() {
        System.out.println("Luồng phụ: Bắt đầu tải dữ liệu...");
        try {
            TimeUnit.SECONDS.sleep(5); // Giả lập tác vụ I/O
        } catch (InterruptedException e) {}
        System.out.println("Luồng phụ: ...Tải xong.");
        return "Dữ liệu server bất đồng bộ";
    }
}
```
**Kết quả:**
```
Luồng chính: Bắt đầu công việc.
Luồng chính: Đã yêu cầu tải dữ liệu và tiếp tục làm việc khác mà không bị chặn.
Luồng phụ: Bắt đầu tải dữ liệu...
(chờ 5 giây, trong lúc đó luồng chính có thể làm việc khác)
Luồng phụ: ...Tải xong.
Callback trên luồng khác: Dữ liệu nhận được là 'Dữ liệu server bất đồng bộ'
```
Bạn có thể thấy luồng chính in ra thông báo ngay lập tức, không cần chờ 5 giây.

---

## 2. Đồng bộ và Bất đồng bộ trong JAVASCRIPT

JavaScript (trong cả trình duyệt và Node.js) có một mô hình hoàn toàn khác: **đơn luồng (single-threaded)** kết hợp với **vòng lặp sự kiện (Event Loop)**.

### a. Đồng bộ trong JavaScript (Mặc định cho các tác vụ CPU)

Giống như Java, các phép tính toán thông thường trong JS là đồng bộ.

```javascript
console.log("Bắt đầu");

function blockFor5Seconds() {
  const start = Date.now();
  while (Date.now() - start < 5000) {
    // Vòng lặp bận, chặn hoàn toàn luồng chính
  }
}

blockFor5Seconds(); // Toàn bộ chương trình và UI sẽ bị "đơ" ở đây

console.log("Kết thúc");
```

### b. Bất đồng bộ trong JavaScript (Cốt lõi)

Đây là thế mạnh của JavaScript. Hầu hết các tác vụ I/O (gọi API, đọc file, `setTimeout`) đều là bất đồng bộ và không chặn luồng chính.

#### Cách cổ điển: Callbacks
```javascript
console.log("Bắt đầu");

setTimeout(() => {
  // Hàm này (callback) được đẩy vào hàng đợi
  // và chỉ được thực thi sau khi luồng chính rảnh và sau 2 giây
  console.log("Callback được gọi sau 2 giây"); 
}, 2000);

console.log("Kết thúc");
```
**Kết quả:**
```
Bắt đầu
Kết thúc
Callback được gọi sau 2 giây
```
Thông báo "Kết thúc" được in ra ngay lập tức.

#### Cách hiện đại: `Promise` và `async/await`

`Promise` là một đối tượng đại diện cho sự hoàn thành (hoặc thất bại) của một tác vụ bất đồng bộ. `async/await` là cú pháp "ngọt ngào" hơn để làm việc với Promise, giúp code trông giống như code đồng bộ.

```javascript
console.log("Bắt đầu");

function fetchDataFromServer() {
  // new Promise trả về một lời hứa
  return new Promise(resolve => {
    setTimeout(() => {
      console.log("... Dữ liệu đã tải xong từ server");
      resolve("Dữ liệu từ Promise");
    }, 2000);
  });
}

// async/await chỉ có thể dùng bên trong một hàm async
async function main() {
  console.log("Bên trong hàm main: đang gọi fetchData...");
  
  // await: Tạm dừng việc thực thi CỦA HÀM main NÀY
  // cho đến khi Promise được giải quyết, nhưng không chặn luồng chính của JS
  const data = await fetchDataFromServer(); 
  
  console.log("Dữ liệu nhận được:", data);
}

main();

console.log("Mã ngoài hàm main đã chạy xong.");
```
**Kết quả:**
```
Bắt đầu
Bên trong hàm main: đang gọi fetchData...
Mã ngoài hàm main đã chạy xong.
(chờ 2 giây)
... Dữ liệu đã tải xong từ server
Dữ liệu nhận được: Dữ liệu từ Promise
```

---

## 3. So sánh trực tiếp Java vs JavaScript

| Tiêu chí | JAVA | JAVASCRIPT |
| :--- | :--- | :--- |
| **Mô hình đồng thời** | **Đa luồng (Multi-threaded)** | **Đơn luồng với Vòng lặp Sự kiện (Single-threaded Event Loop)** |
| **Cách tiếp cận bất đồng bộ** | Tạo một luồng mới để chạy tác vụ trong nền. Luồng gốc không bị chặn. | Giao tác vụ cho môi trường (trình duyệt, hệ điều hành) xử lý. Đặt một callback vào hàng đợi. Event Loop sẽ thực thi callback khi luồng chính rảnh. |
| **Công cụ chính (Hiện đại)** | `CompletableFuture` (tương tự Promise) | `Promise` và `async/await` |
| **Quản lý tài nguyên dùng chung** | **Phức tạp.** Cần các cơ chế khóa (`synchronized`, `Lock`) để tránh **Race Condition**, **Deadlock**. | **Đơn giản hơn.** Vì là đơn luồng, các đoạn code JavaScript không thể bị ngắt giữa chừng bởi một đoạn code khác. Không có race condition ở cấp độ thấp. |
| **Phù hợp cho** | **Tác vụ nặng về CPU (CPU-intensive):** Có thể tận dụng nhiều nhân CPU bằng cách chia công việc ra nhiều luồng. | **Tác vụ nặng về I/O (I/O-intensive):** (Ví dụ: server web, gọi API). Có thể xử lý hàng nghìn kết nối đồng thời mà không cần tạo hàng nghìn luồng, tiết kiệm bộ nhớ. |
| **Ví dụ Code** | `CompletableFuture.runAsync(...)` | `await someFunction()` |
| **Triết lý** | "Nếu một việc tốn thời gian, hãy giao cho một **người khác (luồng khác)** làm." | "Nếu một việc tốn thời gian, hãy **bắt đầu nó**, làm việc khác, và **quay lại xử lý kết quả sau**." |

### Tóm tắt

-   **Java** đạt được tính bất đồng bộ bằng cách sử dụng **thêm người (luồng)**. Điều này mạnh mẽ cho các tác vụ tính toán nặng nhưng tốn kém hơn về bộ nhớ và phức tạp hơn trong việc quản lý.
-   **JavaScript** đạt được tính bất đồng bộ bằng cách **quản lý thời gian thông minh trên một người (luồng)**. Điều này cực kỳ hiệu quả cho các tác vụ chờ đợi (như mạng, file), giúp nó xử lý được lượng lớn kết nối đồng thời một cách nhẹ nhàng.