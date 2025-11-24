# BÁO CÁO THIẾT KẾ HỆ THỐNG VIDEO CONVERTER

**Đề tài:** Hệ thống chuyển đổi định dạng video trực tuyến  
**Mô hình:** MVC (Model-View-Controller)  
**Công nghệ:** Java Servlet, JSP, MySQL, FFmpeg  
**Ngày:** 24/11/2025

---

## MỤC LỤC

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Kiến trúc MVC](#2-kiến-trúc-mvc)
3. [Cấu trúc thư mục dự án](#3-cấu-trúc-thư-mục-dự-án)
4. [Sơ đồ luồng dữ liệu](#4-sơ-đồ-luồng-dữ-liệu)
5. [Chi tiết các thành phần](#5-chi-tiết-các-thành-phần)
6. [Worker Thread System](#6-worker-thread-system)
7. [Request-Response Flow](#7-request-response-flow)
8. [Database Schema](#8-database-schema)
9. [Cơ chế xử lý bất đồng bộ](#9-cơ-chế-xử-lý-bất-đồng-bộ)

---

## 1. TỔNG QUAN HỆ THỐNG

### 1.1. Mục đích
Xây dựng ứng dụng web cho phép người dùng:
- Upload video (tối đa 3GB)
- Chuyển đổi sang các định dạng: MP4, AVI, MKV, MOV, WEBM
- Theo dõi tiến trình chuyển đổi realtime
- Tải xuống video đã chuyển đổi
- Quản lý lịch sử chuyển đổi

### 1.2. Đặc điểm kỹ thuật
- **Xử lý bất đồng bộ:** Sử dụng BlockingQueue và Worker Threads
- **Đa luồng:** 4 worker threads xử lý song song
- **Hàng đợi:** Tối đa 80 jobs đồng thời
- **Timeout:** 30 phút/job
- **Bảo mật:** Session-based authentication, BCrypt password hashing

---

## 2. KIẾN TRÚC MVC

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT (Browser)                         │
│                    HTML/CSS/JavaScript/JSP                       │
└────────────────────────┬───────────────────────────────────────┘
                         │ HTTP Request/Response
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │LoginServlet  │  │UploadServlet │  │StatusServlet │          │
│  │RegisterServlet│  │DownloadServlet│  │AdminServlet │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│         │                   │                   │                │
│         └───────────────────┼───────────────────┘                │
│                             ▼                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       MODEL LAYER                                │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              BUSINESS OBJECT (BO)                        │   │
│  │  ┌──────────────────┐    ┌──────────────────┐          │   │
│  │  │  UserBO          │    │  ConversionBO    │          │   │
│  │  │  - login()       │    │  - submitJob()   │          │   │
│  │  │  - register()    │    │  - getUserJobs() │          │   │
│  │  └──────────────────┘    └────────┬─────────┘          │   │
│  │                                    │                     │   │
│  │                          ┌─────────▼──────────┐         │   │
│  │                          │  Worker Threads    │         │   │
│  │                          │  (4 threads)       │         │   │
│  │                          │  - processJob()    │         │   │
│  │                          │  - convertVideo()  │         │   │
│  │                          └─────────┬──────────┘         │   │
│  └────────────────────────────────────┼────────────────────┘   │
│                                        │                         │
│  ┌─────────────────────────────────────▼────────────────────┐  │
│  │              DATA ACCESS OBJECT (DAO)                     │  │
│  │  ┌────────────┐  ┌──────────────┐  ┌──────────────┐     │  │
│  │  │  UserDAO   │  │VideoDAO      │  │ConversionJobDAO│   │  │
│  │  │  - create()│  │- create()    │  │- createJob()   │   │  │
│  │  │  - findBy()│  │- getById()   │  │- updateStatus()│   │  │
│  │  └────────────┘  └──────────────┘  └──────────────┘     │  │
│  └──────────────────────────┬───────────────────────────────┘  │
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────┐  │
│  │                    BEAN (Entity)                          │  │
│  │  ┌──────┐  ┌───────┐  ┌──────────────┐                  │  │
│  │  │ User │  │ Video │  │ConversionJob │                  │  │
│  │  └──────┘  └───────┘  └──────────────┘                  │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE (MySQL)                            │
│    ┌────────┐    ┌────────┐    ┌──────────────────┐            │
│    │ users  │    │ videos │    │ conversion_jobs  │            │
│    └────────┘    └────────┘    └──────────────────┘            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SYSTEM                               │
│                   FFmpeg (Video Converter)                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. CẤU TRÚC THƯ MỤC DỰ ÁN

```
LTM/
├── src/main/
│   ├── java/com/videoconverter/
│   │   ├── controller/              # [CONTROLLER LAYER]
│   │   │   ├── LoginServlet.java    # Xử lý login
│   │   │   ├── RegisterServlet.java # Xử lý đăng ký
│   │   │   ├── UploadServlet.java   # Upload & submit job
│   │   │   ├── StatusServlet.java   # Kiểm tra tiến trình
│   │   │   ├── DownloadServlet.java # Tải video về
│   │   │   ├── LogoutServlet.java   # Logout
│   │   │   └── AdminServlet.java    # Quản lý admin
│   │   │
│   │   ├── model/                   # [MODEL LAYER]
│   │   │   ├── bean/                # Entity classes
│   │   │   │   ├── User.java        # User entity
│   │   │   │   ├── Video.java       # Video entity
│   │   │   │   └── ConversionJob.java # Job entity
│   │   │   │
│   │   │   ├── bo/                  # Business Logic
│   │   │   │   ├── UserBO.java      # User business logic
│   │   │   │   └── ConversionBO.java # Conversion logic + Workers
│   │   │   │
│   │   │   └── dao/                 # Database Access
│   │   │       ├── UserDAO.java     # Users table CRUD
│   │   │       ├── VideoDAO.java    # Videos table CRUD
│   │   │       └── ConversionJobDAO.java # Jobs table CRUD
│   │   │
│   │   ├── util/                    # Utilities
│   │   │   ├── DBConnection.java    # MySQL connection pool
│   │   │   ├── FFmpegWrapper.java   # FFmpeg integration
│   │   │   ├── PasswordUtil.java    # BCrypt hashing
│   │   │   └── GeneratePassword.java # Password generator
│   │   │
│   │   └── listener/
│   │       └── AppContextListener.java # Lifecycle management
│   │
│   └── webapp/                      # [VIEW LAYER]
│       ├── index.jsp                # Trang chủ
│       ├── login.jsp                # Trang đăng nhập
│       ├── register.jsp             # Trang đăng ký
│       ├── upload.jsp               # Trang upload video
│       ├── status.jsp               # Trang theo dõi tiến trình
│       ├── admin/
│       │   └── dashboard.jsp        # Dashboard admin
│       ├── css/
│       │   └── style.css            # Stylesheet
│       ├── images/
│       │   └── default-avatar.png
│       ├── uploads/                 # Thư mục lưu video
│       │   └── converted/           # Video đã convert
│       └── WEB-INF/
│           └── web.xml              # Cấu hình web app
│
├── pom.xml                          # Maven dependencies
└── target/                          # Compiled files
```

### 3.1. Phân tích từng layer

#### **CONTROLLER (Servlets)**
- Nhận HTTP Request từ client
- Validate dữ liệu đầu vào
- Gọi Business Logic (BO)
- Trả về Response (forward JSP hoặc redirect)

#### **MODEL**
- **Bean:** Đại diện cho các entity (User, Video, ConversionJob)
- **BO (Business Object):** Xử lý logic nghiệp vụ
  - UserBO: Authentication, authorization
  - ConversionBO: Quản lý queue, worker threads, conversion
- **DAO (Data Access Object):** Tương tác với database

#### **VIEW (JSP)**
- Hiển thị giao diện người dùng
- Sử dụng JSTL để render dữ liệu động
- CSS cho styling

---

## 4. SƠ ĐỒ LUỒNG DỮ LIỆU

### 4.1. Luồng Upload và Conversion

```
┌─────────┐
│  USER   │
└────┬────┘
     │ 1. Upload form (POST /upload)
     ▼
┌─────────────────┐
│ UploadServlet   │
└────┬────────────┘
     │ 2. Save file to disk
     │ 3. Call ConversionBO.submitJob()
     ▼
┌──────────────────┐
│  ConversionBO    │
└────┬─────────────┘
     │ 4. Insert video → VideoDAO
     │ 5. Insert job → ConversionJobDAO
     │ 6. Add job to BlockingQueue
     ▼
┌──────────────────────────┐
│  BlockingQueue<Job>      │ ← Max 80 jobs
│  [Job1, Job2, Job3, ...] │
└────┬─────────────────────┘
     │ 7. Worker threads take() job
     ▼
┌──────────────────────────┐
│  Worker Thread Pool      │
│  ┌────────────────────┐  │
│  │ Worker 1 (Thread)  │  │
│  │ Worker 2 (Thread)  │  │ ← 4 concurrent workers
│  │ Worker 3 (Thread)  │  │
│  │ Worker 4 (Thread)  │  │
│  └────────────────────┘  │
└────┬─────────────────────┘
     │ 8. processJob(job)
     │ 9. Update status → PROCESSING
     │ 10. Call FFmpegWrapper.convertVideo()
     ▼
┌─────────────────────┐
│  FFmpeg Process     │
│  (External)         │
└────┬────────────────┘
     │ 11. Convert video
     │ 12. Report progress (0-100%)
     ▼
┌──────────────────────┐
│ ConversionJobDAO     │
│ updateJobStatus()    │ ← Update progress in DB
└──────────────────────┘
     │ 13. Conversion complete
     │ 14. Save output file
     │ 15. Update status → COMPLETED
     ▼
┌──────────────────────┐
│    DATABASE          │
│  conversion_jobs     │
│  status: COMPLETED   │
│  progress: 100%      │
│  output_path: ...    │
└──────────────────────┘
     │
     ▼
┌──────────────────────┐
│   USER (via AJAX)    │
│   StatusServlet      │ ← Poll every 2 seconds
│   GET /status?id=X   │
└──────────────────────┘
```

---

## 5. CHI TIẾT CÁC THÀNH PHẦN

### 5.1. CONTROLLER Layer

#### **UploadServlet.java**
```
Chức năng: Upload video và submit conversion job
URL: /upload
Methods:
  - doGet(): Hiển thị form upload
  - doPost(): Xử lý upload
  
Flow doPost():
  1. Kiểm tra session (authenticated?)
  2. Nhận file từ form (Part videoFile)
  3. Validate format và size
  4. Lưu file vào /uploads với tên unique
  5. Gọi ConversionBO.submitJob()
  6. Redirect đến /status
```

#### **StatusServlet.java**
```
Chức năng: API kiểm tra tiến trình conversion
URL: /status
Methods:
  - doGet(): Trả về JSON với thông tin job
  
Response JSON:
{
  "jobId": 123,
  "status": "PROCESSING",
  "progress": 45,
  "videoFilename": "video.mp4",
  "outputFormat": "avi",
  "createdAt": "2025-11-24 10:30:00"
}
```

#### **DownloadServlet.java**
```
Chức năng: Download video đã convert
URL: /download?jobId=X
Methods:
  - doGet(): Stream file về client
  
Flow:
  1. Get jobId từ parameter
  2. Kiểm tra job thuộc về user hiện tại
  3. Kiểm tra status = COMPLETED
  4. Set headers (Content-Type, Content-Disposition)
  5. Stream file qua OutputStream
```

### 5.2. MODEL Layer

#### **ConversionBO.java** (Core component)
```java
Singleton Pattern - Thread-safe

Components:
  - BlockingQueue<ConversionJob> jobQueue    // Max 80 jobs
  - ExecutorService executorService          // Thread pool (4 threads)
  - VideoDAO, ConversionJobDAO, FFmpegWrapper

Methods:
  1. startWorkers()
     - Load pending jobs từ DB
     - Start 4 worker threads
     
  2. submitJob(userId, filename, filePath, size, format)
     - Insert video → DB
     - Insert job → DB (status: PENDING)
     - Add job to queue
     - Return ConversionJob
     
  3. getUserJobs(userId)
     - Get all jobs của user
     
  4. deleteJob(jobId, userId)
     - Delete job + output file
     
  5. stopWorkers()
     - Clear queue
     - Shutdown ExecutorService gracefully
     
Inner class: ConversionWorker implements Runnable
  - Chạy trong vòng lặp while(isRunning)
  - jobQueue.take() → block cho đến khi có job
  - processJob(job) → convert video
```

#### **ConversionJobDAO.java**
```sql
Database Operations:

1. createJob(job)
   INSERT INTO conversion_jobs (video_id, user_id, output_format, status, progress)
   
2. updateJobStatus(jobId, status, progress)
   UPDATE conversion_jobs SET status=?, progress=? WHERE job_id=?
   
3. completeJob(jobId, outputPath)
   UPDATE conversion_jobs SET status='COMPLETED', progress=100, 
          output_path=?, completed_at=NOW()
   
4. failJob(jobId, errorMessage)
   UPDATE conversion_jobs SET status='FAILED', error_message=?
   
5. getJobById(jobId)
   SELECT * FROM conversion_jobs WHERE job_id=?
   
6. getJobsByUserId(userId)
   SELECT * FROM conversion_jobs WHERE user_id=? ORDER BY created_at DESC
   
7. getPendingJobs()
   SELECT * WHERE status='PENDING' ORDER BY created_at ASC
```

#### **FFmpegWrapper.java**
```java
Wrapper cho FFmpeg CLI

Method: convertVideo(inputPath, outputPath, format, progressCallback)

Flow:
  1. Validate input file exists
  2. Build FFmpeg command:
     ffmpeg -i input.mp4 -f avi -y output.avi
  3. Start process
  4. Parse output để lấy:
     - Duration: tổng thời lượng video
     - Current time: thời gian đã xử lý
  5. Tính progress = (currentTime / duration) * 100
  6. Gọi progressCallback.accept(progress)
  7. Wait với timeout 30 phút
  8. Return true nếu exitValue == 0
```

---

## 6. WORKER THREAD SYSTEM

### 6.1. Kiến trúc Worker

```
┌───────────────────────────────────────────────────────┐
│            ConversionBO (Singleton)                   │
│                                                        │
│  ┌──────────────────────────────────────────────┐   │
│  │   BlockingQueue<ConversionJob>               │   │
│  │   Capacity: 80                               │   │
│  │   ┌────┐ ┌────┐ ┌────┐ ┌────┐              │   │
│  │   │Job1│→│Job2│→│Job3│→│Job4│ → ...        │   │
│  │   └────┘ └────┘ └────┘ └────┘              │   │
│  └──────────────┬───────────────────────────────┘   │
│                 │ take() - blocking                  │
│                 ▼                                     │
│  ┌──────────────────────────────────────────────┐   │
│  │    ExecutorService (Fixed Thread Pool)       │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────┐    │   │
│  │  │ Worker 1 (ConversionWorker)         │    │   │
│  │  │  ├─ while(isRunning)                │    │   │
│  │  │  ├─ job = queue.take()              │    │   │
│  │  │  └─ processJob(job)                 │    │   │
│  │  └─────────────────────────────────────┘    │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────┐    │   │
│  │  │ Worker 2 (ConversionWorker)         │    │   │
│  │  └─────────────────────────────────────┘    │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────┐    │   │
│  │  │ Worker 3 (ConversionWorker)         │    │   │
│  │  └─────────────────────────────────────┘    │   │
│  │                                               │   │
│  │  ┌─────────────────────────────────────┐    │   │
│  │  │ Worker 4 (ConversionWorker)         │    │   │
│  │  └─────────────────────────────────────┘    │   │
│  └──────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────┘
```

### 6.2. Lifecycle của Worker

```
┌─────────────────────────────────────────────────────────┐
│        Application Server Startup                       │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│    AppContextListener.contextInitialized()              │
│         ConversionBO.getInstance().startWorkers()       │
└───────────────────┬─────────────────────────────────────┘
                    │
         ┌──────────┴──────────┐
         │                     │
         ▼                     ▼
┌─────────────────┐   ┌──────────────────┐
│ Load Pending    │   │ Start 4 Workers  │
│ Jobs from DB    │   │ (Threads)        │
└────────┬────────┘   └────────┬─────────┘
         │                     │
         └──────────┬──────────┘
                    ▼
┌─────────────────────────────────────────────────────────┐
│           Workers Running (Infinite Loop)               │
│                                                          │
│  while (isRunning) {                                    │
│    ConversionJob job = queue.take();  ← BLOCKING       │
│    processJob(job);                                     │
│  }                                                       │
└───────────────────┬─────────────────────────────────────┘
                    │
                    │ (Continues until...)
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│        Application Server Shutdown                      │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│    AppContextListener.contextDestroyed()                │
│         ConversionBO.getInstance().stopWorkers()        │
│                                                          │
│  1. Set isRunning = false                               │
│  2. Clear queue                                         │
│  3. executorService.shutdown()                          │
│  4. Wait 60 seconds for graceful shutdown               │
│  5. Force shutdown if not finished                      │
└─────────────────────────────────────────────────────────┘
```

### 6.3. ProcessJob Flow

```
processJob(ConversionJob job) {
  
  1. Update status: PROCESSING
     ├─ jobDAO.updateJobStatus(jobId, "PROCESSING", 0)
     
  2. Get video từ DB
     ├─ Video video = videoDAO.getVideoById(job.getVideoId())
     └─ if (null) → failJob("Video not found")
     
  3. Check file tồn tại
     ├─ File inputFile = new File(video.getFilePath())
     └─ if (!exists) → failJob("File not found")
     
  4. Prepare output directory
     ├─ outputDir = inputFile.getParent() + "/converted"
     ├─ Create directory if not exists
     └─ outputFile = new File(outputDir, "video_converted.avi")
     
  5. Convert video
     ├─ ffmpegWrapper.convertVideo(
     │    inputPath,
     │    outputPath,
     │    format,
     │    progress -> jobDAO.updateJobStatus(jobId, "PROCESSING", progress)
     │  )
     │
     └─ FFmpeg runs in separate process
        ├─ Parse duration: Duration: 00:05:30
        ├─ Parse time: time=00:02:45
        ├─ Calculate progress: (165/330) * 100 = 50%
        └─ Update DB every second
        
  6. Handle result
     ├─ if (success && outputFile.exists())
     │    └─ jobDAO.completeJob(jobId, outputPath)
     │         ├─ status = COMPLETED
     │         ├─ progress = 100
     │         ├─ output_path = ...
     │         └─ completed_at = NOW()
     │
     └─ else
          └─ jobDAO.failJob(jobId, "Conversion failed")
               ├─ status = FAILED
               ├─ error_message = ...
               └─ completed_at = NOW()
}
```

---

## 7. REQUEST-RESPONSE FLOW

### 7.1. Upload Video Flow (Chi tiết)

```
┌──────────┐
│  CLIENT  │
└────┬─────┘
     │
     │ 1. GET /upload
     ▼
┌──────────────────────┐
│  UploadServlet       │
│    doGet()           │
└────┬─────────────────┘
     │
     │ 2. Forward to upload.jsp
     ▼
┌──────────────────────┐
│    upload.jsp        │
│  ┌────────────────┐ │
│  │ <form>         │ │
│  │  File: [...]   │ │
│  │  Format: [v]   │ │
│  │  [Upload]      │ │
│  └────────────────┘ │
└────┬─────────────────┘
     │
     │ 3. User selects file and submits
     │    POST /upload (multipart/form-data)
     ▼
┌──────────────────────────────────────────────────┐
│  UploadServlet.doPost()                          │
│                                                   │
│  4. Check session                                │
│     └─ if (null) → redirect /login               │
│                                                   │
│  5. Get form data                                │
│     ├─ Part filePart = request.getPart("file")  │
│     └─ String format = request.getParameter()    │
│                                                   │
│  6. Validate                                     │
│     ├─ if (file empty) → error                   │
│     └─ if (invalid format) → error               │
│                                                   │
│  7. Save file                                    │
│     ├─ Generate unique name: timestamp_filename  │
│     ├─ Path: /uploads/1732435678_video.mp4      │
│     └─ filePart.write(filePath)                  │
│                                                   │
│  8. Submit conversion job                        │
│     ├─ ConversionBO.submitJob(                   │
│     │    userId,                                 │
│     │    filename,                               │
│     │    filePath,                               │
│     │    fileSize,                               │
│     │    outputFormat                            │
│     │  )                                         │
│     │                                             │
│     └─ ConversionBO flow:                        │
│          ├─ VideoDAO.createVideo() → DB         │
│          ├─ ConversionJobDAO.createJob() → DB   │
│          └─ queue.offer(job) → BlockingQueue    │
│                                                   │
│  9. Redirect to status page                     │
│     └─ response.sendRedirect("/status")          │
└──────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────┐
│    status.jsp                                     │
│                                                   │
│  JavaScript:                                      │
│    setInterval(() => {                            │
│      fetch('/status?jobId=' + id)                │
│        .then(res => res.json())                   │
│        .then(data => {                            │
│          updateProgressBar(data.progress)         │
│          if (data.status === 'COMPLETED') {       │
│            showDownloadButton()                   │
│          }                                         │
│        })                                          │
│    }, 2000);  // Poll every 2 seconds             │
└──────────────────────────────────────────────────┘
```

### 7.2. Status Polling Flow

```
     ┌─────────────────────────────────────┐
     │  Client (JavaScript in status.jsp)  │
     └──────────┬──────────────────────────┘
                │
                │ Every 2 seconds
                │ GET /status?jobId=123
                ▼
     ┌─────────────────────────────────┐
     │  StatusServlet.doGet()          │
     │                                  │
     │  1. Get jobId from parameter    │
     │  2. Check session/auth          │
     │  3. Get job from BO             │
     │     └─ ConversionJobDAO         │
     │           .getJobById(jobId)    │
     │  4. Build JSON response:        │
     │     {                            │
     │       "jobId": 123,              │
     │       "status": "PROCESSING",    │
     │       "progress": 67,            │
     │       "videoFilename": "x.mp4", │
     │       "outputFormat": "avi"      │
     │     }                            │
     │  5. response.getWriter()        │
     │       .write(json)               │
     └──────────┬──────────────────────┘
                │
                │ JSON Response
                ▼
     ┌─────────────────────────────────┐
     │  Client JavaScript              │
     │                                  │
     │  Update UI:                     │
     │  - Progress bar: 67%            │
     │  - Status text: "Processing..." │
     │  - Time elapsed: 2m 15s         │
     │                                  │
     │  If status === "COMPLETED":     │
     │  - Show download button         │
     │  - Stop polling                 │
     └─────────────────────────────────┘
```

### 7.3. Download Flow

```
     ┌─────────────────────────────┐
     │  Client clicks Download     │
     │  GET /download?jobId=123    │
     └──────────┬──────────────────┘
                │
                ▼
     ┌─────────────────────────────────────────┐
     │  DownloadServlet.doGet()                │
     │                                          │
     │  1. Get jobId from parameter            │
     │  2. Check session/auth                  │
     │  3. Get job from DB                     │
     │     └─ ConversionJobDAO.getJobById()    │
     │  4. Validate:                            │
     │     ├─ job.userId == currentUser?       │
     │     └─ job.status == "COMPLETED"?       │
     │  5. Get output file path                │
     │     └─ File file = new File(outputPath) │
     │  6. Set response headers:               │
     │     ├─ Content-Type: video/avi          │
     │     ├─ Content-Length: 125678900        │
     │     └─ Content-Disposition:             │
     │          attachment; filename="x.avi"   │
     │  7. Stream file to client:              │
     │     ├─ FileInputStream fis              │
     │     ├─ OutputStream out                 │
     │     └─ Copy bytes: fis → out            │
     └──────────┬──────────────────────────────┘
                │
                │ File stream
                ▼
     ┌─────────────────────────────┐
     │  Browser downloads file     │
     │  video_converted.avi        │
     └─────────────────────────────┘
```

---

## 8. DATABASE SCHEMA

```sql
-- Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt hash
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',      -- USER, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Videos table
CREATE TABLE videos (
    video_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,             -- bytes
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
);

-- Conversion jobs table
CREATE TABLE conversion_jobs (
    job_id INT PRIMARY KEY AUTO_INCREMENT,
    video_id INT NOT NULL,
    user_id INT NOT NULL,
    output_format VARCHAR(10) NOT NULL,    -- mp4, avi, mkv, mov, webm
    status VARCHAR(20) NOT NULL,           -- PENDING, PROCESSING, COMPLETED, FAILED
    progress INT DEFAULT 0,                -- 0-100%
    output_path VARCHAR(500),
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (video_id) REFERENCES videos(video_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);
```

### 8.1. Quan hệ giữa các bảng

```
┌─────────────────┐
│     users       │
│  ┌───────────┐ │
│  │ user_id PK│◄├────────┐
│  │ username  │ │         │
│  │ password  │ │         │
│  │ email     │ │         │
│  │ role      │ │         │
│  └───────────┘ │         │
└─────────────────┘         │
         ▲                  │
         │                  │
         │ 1:N              │ 1:N
         │                  │
┌────────┴──────────┐  ┌────┴───────────────────┐
│     videos        │  │  conversion_jobs       │
│  ┌─────────────┐ │  │  ┌──────────────────┐ │
│  │ video_id PK │◄├──┼──┤ job_id PK        │ │
│  │ user_id FK  │ │  │  │ video_id FK      │ │
│  │ filename    │ │  │  │ user_id FK       │ │
│  │ file_path   │ │  │  │ output_format    │ │
│  │ file_size   │ │  │  │ status           │ │
│  └─────────────┘ │  │  │ progress         │ │
└───────────────────┘  │  │ output_path      │ │
                       │  │ error_message    │ │
                       │  └──────────────────┘ │
                       └────────────────────────┘
```

---

## 9. CƠ CHẾ XỬ LÝ BẤT ĐỒNG BỘ

### 9.1. Tại sao cần xử lý bất đồng bộ?

**Vấn đề:**
- Chuyển đổi video mất nhiều thời gian (5-30 phút/video)
- Nếu xử lý đồng bộ → user phải chờ → timeout → bad UX
- Server chỉ có thể xử lý 1 request tại 1 thời điểm

**Giải pháp:**
- Upload và submit job → return ngay lập tức
- Worker threads xử lý background
- User poll status qua AJAX

### 9.2. Producer-Consumer Pattern

```
┌─────────────────────────────────────────────────────┐
│                  PRODUCER                           │
│  (UploadServlet submits jobs)                       │
│                                                      │
│  submitJob() {                                      │
│    ConversionJob job = new ConversionJob(...);     │
│    queue.offer(job);  ← Non-blocking               │
│    return job;                                      │
│  }                                                   │
└───────────────────┬─────────────────────────────────┘
                    │ Add to queue
                    ▼
┌─────────────────────────────────────────────────────┐
│         BlockingQueue<ConversionJob>                │
│         Thread-safe, Blocking Operations            │
│  ┌────────────────────────────────────────────┐   │
│  │  [Job1] → [Job2] → [Job3] → [Job4] → ...  │   │
│  └────────────────────────────────────────────┘   │
│                                                      │
│  Methods:                                           │
│  - offer(job): Add job (non-blocking, false if full)│
│  - take(): Get job (blocking, waits if empty)      │
│  - size(): Current queue size                       │
└───────────────────┬─────────────────────────────────┘
                    │ Take from queue
                    ▼
┌─────────────────────────────────────────────────────┐
│              CONSUMERS                              │
│  (4 Worker Threads)                                 │
│                                                      │
│  Worker 1: while(true) { processJob(queue.take()); }│
│  Worker 2: while(true) { processJob(queue.take()); }│
│  Worker 3: while(true) { processJob(queue.take()); }│
│  Worker 4: while(true) { processJob(queue.take()); }│
│                                                      │
│  ← Workers are BLOCKED when queue is empty         │
│  ← Workers wake up automatically when job arrives   │
└─────────────────────────────────────────────────────┘
```

### 9.3. Thread Safety

**BlockingQueue đảm bảo:**
- Thread-safe operations (không cần synchronized)
- Automatic blocking/unblocking
- FIFO order (First In First Out)

**ConversionBO Singleton:**
```java
private static volatile ConversionBO instance;

public static ConversionBO getInstance() {
    if (instance == null) {
        synchronized (ConversionBO.class) {
            if (instance == null) {
                instance = new ConversionBO();
            }
        }
    }
    return instance;
}
```
- Double-checked locking
- volatile keyword → visibility across threads
- Đảm bảo chỉ 1 instance duy nhất

### 9.4. Concurrency Control

```
Scenario: 2 workers xử lý cùng lúc

┌─────────────────────────────────────────────────────┐
│                    Queue                            │
│  [Job1: video1.mp4 → avi]                          │
│  [Job2: video2.mp4 → mkv]                          │
│  [Job3: video3.mp4 → mov]                          │
└──────────┬──────────────────┬───────────────────────┘
           │                  │
           │ take()           │ take()
           ▼                  ▼
┌──────────────────┐  ┌──────────────────┐
│    Worker 1      │  │    Worker 2      │
│                  │  │                  │
│ Processing Job1  │  │ Processing Job2  │
│   Status: 35%    │  │   Status: 67%    │
│                  │  │                  │
│ FFmpeg Process 1 │  │ FFmpeg Process 2 │
│   PID: 12345     │  │   PID: 12346     │
└──────────────────┘  └──────────────────┘

- Mỗi worker có FFmpeg process riêng
- Không conflict về resources
- CPU/Memory được chia sẻ tự động bởi OS
```

---

## 10. TỔNG KẾT LUỒNG HOẠT ĐỘNG CHÍNH THỨC

### 10.1. Complete System Flow

```
1. SERVER STARTUP
   └─ AppContextListener.contextInitialized()
      ├─ ConversionBO.getInstance()
      ├─ Load pending jobs từ DB → Queue
      └─ Start 4 worker threads

2. USER REGISTRATION/LOGIN
   ├─ POST /register → RegisterServlet
   │  ├─ UserBO.register(username, password, email)
   │  ├─ BCrypt hash password
   │  └─ UserDAO.createUser() → DB
   │
   └─ POST /login → LoginServlet
      ├─ UserBO.login(username, password)
      ├─ BCrypt verify password
      ├─ Create session
      └─ Redirect to /upload

3. VIDEO UPLOAD & CONVERSION
   ├─ GET /upload → Show upload form
   │
   └─ POST /upload → UploadServlet
      ├─ Save file: /uploads/timestamp_filename.mp4
      ├─ ConversionBO.submitJob()
      │  ├─ VideoDAO.createVideo() → DB
      │  ├─ ConversionJobDAO.createJob() → DB
      │  └─ queue.offer(job)
      └─ Redirect to /status

4. BACKGROUND PROCESSING
   ├─ Worker threads continuously running
   ├─ job = queue.take() (BLOCKING)
   ├─ Update status: PROCESSING
   ├─ FFmpeg converts video
   │  └─ Progress updates: 0% → 100%
   └─ Update status: COMPLETED or FAILED

5. STATUS MONITORING
   ├─ Client: GET /status?jobId=X (every 2 seconds)
   ├─ StatusServlet returns JSON
   └─ Update UI: progress bar, status text

6. DOWNLOAD
   ├─ GET /download?jobId=X
   ├─ DownloadServlet streams file
   └─ Browser downloads: video_converted.avi

7. SERVER SHUTDOWN
   └─ AppContextListener.contextDestroyed()
      ├─ ConversionBO.stopWorkers()
      ├─ Clear queue
      └─ Shutdown ExecutorService (wait 60s)
```

### 10.2. Error Handling Strategy

```
1. Upload Errors:
   ├─ File too large → Show error message
   ├─ Invalid format → Show error message
   └─ Upload failed → Show error message

2. Conversion Errors:
   ├─ Video not found → failJob("Video not found")
   ├─ FFmpeg failed → failJob("Conversion failed")
   ├─ Timeout (30 min) → Kill process, failJob()
   └─ Out of disk space → failJob("Disk full")

3. Download Errors:
   ├─ Job not completed → HTTP 400
   ├─ Unauthorized → HTTP 403
   └─ File not found → HTTP 404

4. Database Errors:
   ├─ Connection failed → Retry 3 times
   ├─ Query failed → Log error, return null
   └─ Transaction failed → Rollback

5. Queue Full:
   └─ queue.offer() returns false
      └─ failJob("Queue is full, try again later")
```

---

## 11. THÔNG SỐ KỸ THUẬT

### 11.1. Configuration Parameters

```java
// ConversionBO
WORKER_COUNT = 4              // Number of worker threads
MAX_QUEUE_SIZE = 80           // Max jobs in queue
TIMEOUT_MINUTES = 30          // Max time per job

// UploadServlet
maxFileSize = 3GB             // Max upload size
maxRequestSize = 3GB
fileSizeThreshold = 16MB      // Buffer in RAM

// Database Connection
JDBC_URL = "jdbc:mysql://localhost:3306/video_converter"
MAX_POOL_SIZE = 20            // Connection pool size
CONNECTION_TIMEOUT = 30s

// Session
SESSION_TIMEOUT = 30 minutes  // Auto logout
```

### 11.2. Supported Formats

```
Input formats:  MP4, AVI, MKV, MOV, WEBM, FLV, WMV
Output formats: MP4, AVI, MKV, MOV, WEBM
```

### 11.3. System Requirements

```
Server:
- Java 17+
- Tomcat 10+
- MySQL 8+
- FFmpeg 4.4+
- RAM: 8GB minimum
- CPU: 4 cores minimum
- Disk: 500GB minimum

Client:
- Modern browser (Chrome, Firefox, Edge)
- JavaScript enabled
```

---

## 12. KẾT LUẬN

### 12.1. Ưu điểm của thiết kế

✅ **Kiến trúc MVC rõ ràng**
- Tách biệt View, Controller, Model
- Dễ bảo trì và mở rộng

✅ **Xử lý bất đồng bộ hiệu quả**
- User không phải chờ
- Xử lý song song nhiều video

✅ **Thread-safe**
- BlockingQueue đảm bảo concurrency
- Singleton pattern cho ConversionBO

✅ **Scalable**
- Dễ tăng số worker threads
- Dễ tăng kích thước queue

✅ **Error handling tốt**
- Mọi lỗi đều được log và xử lý
- User luôn nhận được feedback

### 12.2. Hướng phát triển

🔸 **Tối ưu performance:**
- Thêm Redis cache cho session
- Sử dụng CDN cho static files
- Compress video output

🔸 **Tăng tính năng:**
- Chọn resolution, bitrate, codec
- Preview video trước khi download
- Batch conversion (nhiều video cùng lúc)

🔸 **Cải thiện UX:**
- Drag & drop upload
- Real-time progress với WebSocket
- Email notification khi xong

🔸 **Bảo mật:**
- HTTPS cho tất cả requests
- Rate limiting để chống spam
- Virus scanning cho uploaded files

---

**Người thực hiện:** [Tên của bạn]  
**Ngày hoàn thành:** 24/11/2025  
**Phiên bản:** 1.0

---


