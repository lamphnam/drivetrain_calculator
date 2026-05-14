# Tài liệu giải thích cơ sở dữ liệu và entity cho khách hàng

## Mục đích tài liệu

Tài liệu này mô tả ngắn gọn cách hệ thống backend đang lưu dữ liệu cho Module 1 của ứng dụng tính toán cơ khí.

Mục tiêu là giúp khách hàng hiểu:

- hệ thống đang lưu những loại dữ liệu nào
- mỗi bảng đóng vai trò gì
- dữ liệu nào phục vụ màn hình nhập liệu, kết quả và lịch sử
- vì sao hiện tại chưa cần thêm các bảng dư thừa

Đây là tài liệu theo góc nhìn nghiệp vụ, không đi quá sâu vào chi tiết kỹ thuật của JPA hoặc Spring Boot.

## Tổng quan mô hình dữ liệu

Hiện tại Module 1 sử dụng 5 nhóm dữ liệu chính:

1. `DesignConstantSet`
2. `DesignCase`
3. `Motor`
4. `Module1Result`
5. `ShaftState`

Quan hệ tổng quát:

```text
DesignConstantSet 1 --- N DesignCase
DesignCase        1 --- 1 Module1Result
Motor             1 --- N Module1Result
Module1Result     1 --- N ShaftState
```

Ý nghĩa thực tế:

- một bộ hằng số có thể được dùng cho nhiều bài tính
- một bài tính lưu ở mức tổng thể là một `DesignCase`
- mỗi `DesignCase` của Module 1 có một kết quả chính
- một động cơ có thể được chọn bởi nhiều bài tính khác nhau
- một kết quả Module 1 có nhiều trạng thái trục để hiển thị chi tiết

## 1. Bảng `design_constant_set`

### Vai trò

Đây là bảng lưu bộ hằng số thiết kế và các tỷ số mặc định mà backend dùng để tính Module 1.

### Dữ liệu chính

- mã bộ hằng số
- tên bộ hằng số
- các hiệu suất như `etaKn`, `etaD`, `etaBrc`, `etaBrt`, `etaOl`
- tỷ số truyền đai mặc định `U1`
- tỷ số hộp giảm tốc mặc định `Uh`
- cờ active để xác định bộ nào đang được dùng mặc định

### Ý nghĩa nghiệp vụ

- frontend không cần nhập các hằng số này bằng tay
- backend tự lấy bộ hằng số phù hợp để đảm bảo tính toán đồng nhất
- nếu người dùng không chỉ định bộ hằng số, hệ thống sẽ lấy bộ active mặc định

## 2. Bảng `design_case`

### Vai trò

Đây là bảng gốc cho một lần tính toán được lưu lại.

Có thể hiểu `DesignCase` là "hồ sơ bài tính".

### Dữ liệu chính

- `id`
- `caseCode`
- `caseName`
- công suất yêu cầu
- tốc độ đầu ra yêu cầu
- trạng thái xử lý
- liên kết tới bộ hằng số
- thời gian tạo và cập nhật

### Ý nghĩa nghiệp vụ

- khi người dùng bấm Calculate ở màn hình New Calculation, hệ thống sẽ tạo hoặc cập nhật một `DesignCase`
- `DesignCase` là nền tảng để tạo lịch sử bài tính
- màn hình Home và Saved Calculations có thể dùng dữ liệu này để hiển thị các bài tính gần đây

### Vì sao cần bảng này

Nếu chỉ lưu kết quả cuối cùng mà không có `DesignCase`, hệ thống sẽ khó quản lý lịch sử, truy vết đầu vào và tái mở một bài tính đã lưu.

## 3. Bảng `motor`

### Vai trò

Đây là danh mục động cơ được hệ thống dùng để chọn động cơ phù hợp.

### Dữ liệu chính

- mã động cơ
- công suất định mức
- số vòng quay định mức
- hãng sản xuất
- mô tả
- trạng thái active

### Ý nghĩa nghiệp vụ

- đây là dữ liệu tham chiếu dùng chung, không phải dữ liệu do người dùng tạo mỗi lần tính
- Module 1 sẽ chỉ xét các động cơ còn active
- từ dữ liệu này, backend chọn ra động cơ phù hợp nhất với yêu cầu công suất và tốc độ sơ bộ

## 4. Bảng `module1_result`

### Vai trò

Đây là bảng lưu kết quả tổng hợp của Module 1 cho một `DesignCase`.

Có thể hiểu đây là "kết quả chính" của bài tính.

### Dữ liệu chính

- hiệu suất toàn hệ thống
- công suất yêu cầu phía động cơ
- số vòng quay sơ bộ của động cơ
- tỷ số truyền tổng
- tỷ số đai `U1`
- tỷ số hộp giảm tốc `Uh`
- tỷ số bánh răng côn `U2`
- tỷ số bánh răng trụ `U3`
- động cơ được chọn
- ghi chú tính toán
- thời gian tạo và cập nhật

### Ý nghĩa nghiệp vụ

Bảng này phục vụ trực tiếp cho màn hình Calculation Result:

- phần Input Summary liên kết từ `DesignCase`
- phần Selected Motor lấy từ `Motor`
- phần System Efficiency và Required Motor Power lấy từ `Module1Result`
- phần Transmission Ratios cũng lấy trực tiếp từ đây
- phần Calculation Notes dùng để hiển thị cảnh báo hoặc giả định tạm thời

### Ghi chú hiện tại

Trong giai đoạn này, tỷ số `U2` đang là giá trị tạm để phục vụ luồng Module 1.
Sau này khi Module 3 được triển khai đầy đủ, phần này có thể được thay bằng logic thực tế hơn.

## 5. Bảng `shaft_state`

### Vai trò

Đây là bảng lưu trạng thái làm việc của từng trục trong kết quả Module 1.

### Dữ liệu chính

- mã trục
- thứ tự hiển thị
- công suất trên trục
- số vòng quay
- mô-men xoắn
- liên kết tới `Module1Result`

### Các trục hiện tại

- `MOTOR`
- `SHAFT_1`
- `SHAFT_2`
- `SHAFT_3`
- `DRUM_SHAFT`

### Ý nghĩa nghiệp vụ

Bảng này phục vụ cho phần Shaft Characteristics trên màn hình kết quả.

Nhờ có bảng riêng:

- frontend có thể hiển thị từng "card" trục rất rõ ràng
- dữ liệu trục có thứ tự ổn định
- các module sau có thể tái sử dụng thông tin này thay vì tính lại từ đầu

## Luồng lưu dữ liệu của Module 1

Khi người dùng nhập:

- công suất yêu cầu
- tốc độ đầu ra yêu cầu

hệ thống xử lý theo thứ tự:

1. chọn bộ hằng số phù hợp
2. tạo hoặc cập nhật `DesignCase`
3. tính toán kết quả Module 1
4. chọn động cơ từ bảng `motor`
5. lưu `Module1Result`
6. lưu các dòng `ShaftState`
7. cập nhật trạng thái bài tính thành hoàn tất

Kết quả là bài tính vừa được trả về cho frontend, vừa được lưu để mở lại sau này.

## Màn hình nào dùng dữ liệu nào

### Home / Overview

- có thể lấy bài tính gần nhất từ `DesignCase` + `Module1Result`
- không cần bảng riêng cho trang chủ

### New Calculation

- người dùng chỉ nhập công suất và tốc độ đầu ra
- bộ hằng số và danh mục động cơ được backend quản lý

### Calculation Result

- `DesignCase`: thông tin đầu vào và mã bài tính
- `Motor`: động cơ được chọn
- `Module1Result`: hiệu suất, công suất động cơ, tỷ số truyền
- `ShaftState`: trạng thái từng trục

### Saved Calculations / History

- danh sách lịch sử lấy từ `DesignCase` kết hợp với `Module1Result` và `Motor`
- không cần tạo thêm bảng History riêng

### Settings

- hiện tại chỉ là thông tin tĩnh
- chưa cần bảng lưu cài đặt riêng

## Vì sao không tạo thêm nhiều bảng hơn

Ở giai đoạn hiện tại, mô hình dữ liệu đang được giữ gọn để:

- dễ vận hành
- dễ kiểm soát dữ liệu
- tránh trùng lặp thông tin
- đủ phục vụ UI hiện tại của Module 1

Ví dụ:

- không cần bảng `Home`
- không cần bảng `History`
- không cần bảng `Settings`

Vì các màn hình đó đã có thể được dựng từ dữ liệu sẵn có.

## Lợi ích của mô hình hiện tại

- phù hợp với luồng người dùng: nhập là tính ngay
- có lưu lịch sử để xem lại
- không buộc frontend phải tạo case trước
- dữ liệu kết quả được tách rõ giữa kết quả tổng và chi tiết từng trục
- dễ mở rộng cho các module tiếp theo

## Phạm vi hiện tại và hướng mở rộng

Hiện tại hệ thống mới tập trung vào Module 1.

Trong tương lai, khi Module 2, 3, 4 được bổ sung, mô hình hiện tại vẫn có thể mở rộng theo hướng:

- dùng `DesignCase` làm gốc cho toàn bộ bài thiết kế
- dùng `Module1Result` làm đầu ra trung gian cho các bước tiếp theo
- bổ sung thêm bảng kết quả chuyên biệt cho các module sau nếu thật sự cần

Điều này giúp hệ thống phát triển theo từng giai đoạn mà không phải thiết kế lại toàn bộ cơ sở dữ liệu ngay từ đầu.

