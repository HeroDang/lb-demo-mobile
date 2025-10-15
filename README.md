# 📱 Android Load Balancer Client (Retrofit + Kotlin)

Ứng dụng Android mini dùng để test **Load Balancer Server (NGINX + Flask)**.

## 🎯 Mục tiêu
- Gửi request đến server load balancer qua `Retrofit`
- Hiển thị tên server backend xử lý (api1 / api2)
- Minh họa thuật toán phân tải: Round Robin, Least Connections, IP Hash

---

## ⚙️ Cấu trúc
```
mobile-lbdemo/
├── app/src/main/java/com/example/lbdemo/
│   ├── MainActivity.kt
│   ├── RetrofitClient.kt
│   ├── ApiService.kt
│   ├── ApiResponse.kt
├── app/src/main/res/layout/
│   └── activity_main.xml
```
---

## 🧱 Công nghệ sử dụng
| Thành phần | Công nghệ |
|-------------|------------|
| Ngôn ngữ | Kotlin |
| UI | XML Layout + ConstraintLayout |
| Networking | Retrofit2 + OkHttp + Gson |
| Async | Kotlin Coroutines |
| Test trên emulator | 10.0.2.2:8080 (map tới localhost của PC) |

---

## 🚀 Cách chạy demo

1. Mở project trong **Android Studio**
2. Đảm bảo server Docker đang chạy (port 8080)
   docker-compose up -d
3. Chạy app trên **Android Emulator**
4. Nhấn các nút để gửi request:

| Nút | API gọi | Mục đích |
|------|----------|----------|
| 🟢 GET /api/hello | Trả về server xử lý | Test Round Robin |
| 🟠 GET /api/slow | Delay 3s | Test Least Connections |
| 🔁 Send 10 requests | Gửi 10 request song song | Xem phân phối tải |

---

## 🧩 Mã nguồn chính

RetrofitClient.kt
private const val BASE_URL = "http://10.0.2.2:8080/"

⚠️ Emulator dùng `10.0.2.2` để trỏ tới host machine (chạy Docker).  
Nếu test trên **thiết bị thật**, thay bằng **IP LAN của máy**.

---

## 📊 Demo kết quả

Ví dụ app hiển thị:
OK
server: api1
message: Hello from Flask!

Sau vài lần bấm:
api1 → api2 → api1 → api2

Với endpoint `/api/slow`, thời gian phản hồi chậm hơn 3s (minh họa tải nặng).  
Với “Send 10 concurrent requests”, kết quả nhóm server:
Results: [api1, api2, api2, api1, api2, api1, api1, api2, api2, api1]
Summary: {api1=5, api2=5}

---

## 🧠 Liên hệ với Mobile & Pervasive Computing
- App mobile đóng vai trò **client pervasive**, gửi request mọi lúc, mọi nơi.  
- Load Balancer phía server đảm bảo **phân phối tải đều, phản hồi nhanh**, nâng cao QoS.  
- Minh họa rõ mối liên hệ giữa **client di động** và **middleware phân tải thông minh** trong môi trường pervasive.

---

## 🧰 Troubleshooting
| Lỗi | Nguyên nhân | Cách khắc phục |
|------|--------------|----------------|
| timeout hoặc connection refused | Docker server chưa chạy | docker-compose up -d |
| Không đổi server | Cấu hình LB cố định (ip_hash) | Đổi thuật toán trong nginx.conf |
| App thật không kết nối được | Sai IP 10.0.2.2 | Dùng IP LAN của PC |

---

**Author:** Nhóm SE405  
**Course:** Mobile & Pervasive Computing  
**Date:** October 2025
