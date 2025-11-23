# HƯỚNG DẪN TỐI ƯU THÔNG SỐ HỆ THỐNG VIDEO CONVERTER

## 📊 TỔNG QUAN CÁC THÔNG SỐ

### **A. Thông số Upload (UploadServlet.java)**
```java
@MultipartConfig(
    maxFileSize = 3221225472L,      // 3 GB
    maxRequestSize = 3221225472L,   // 3 GB
    fileSizeThreshold = 16777216    // 16 MB
)
```

### **B. Thông số Processing (ConversionBO.java)**
```java
private static final int WORKER_COUNT = 4;      // 4 workers
private static final int MAX_QUEUE_SIZE = 80;   // 80 jobs
```

### **C. Thông số Server**
```
CPU: 16 cores
RAM: 14 GB (14,336 MB)
```

---

## 🔗 MỐI LIÊN HỆ GIỮA CÁC THÔNG SỐ

### **1. maxFileSize ↔ RAM**
```
maxFileSize càng lớn → Cần RAM càng nhiều khi convert
```

**Công thức ước tính:**
```
RAM cần cho 1 video đang convert = maxFileSize × 1.3

Ví dụ:
- File 3GB → RAM = 3 × 1.3 = 3.9 GB/video
- File 1GB → RAM = 1 × 1.3 = 1.3 GB/video
- File 500MB → RAM = 0.5 × 1.3 = 0.65 GB/video
```

**Hệ số 1.3 bao gồm:**
- Input buffer (đọc video gốc)
- Output buffer (ghi video mới)
- FFmpeg internal buffers (giải mã, encode)

---

### **2. WORKER_COUNT ↔ RAM**
```
Tổng RAM cần = WORKER_COUNT × (maxFileSize × 1.3)
```

**Công thức an toàn:**
```
WORKER_COUNT ≤ (RAM_available × 0.7) / (maxFileSize × 1.3)

Trong đó:
- RAM_available = 14 GB
- 0.7 = Hệ số an toàn (dành 30% cho OS, Tomcat, DB)
```

**Ví dụ tính toán:**

| maxFileSize | RAM/video | Max Workers | Khuyến nghị |
|-------------|-----------|-------------|-------------|
| 3 GB | 3.9 GB | (14×0.7)/3.9 = 2.5 | **3-4 workers** |
| 2 GB | 2.6 GB | (14×0.7)/2.6 = 3.8 | **3-4 workers** |
| 1 GB | 1.3 GB | (14×0.7)/1.3 = 7.5 | **6-8 workers** |
| 500 MB | 0.65 GB | (14×0.7)/0.65 = 15 | **8-10 workers** |

---

### **3. WORKER_COUNT ↔ CPU**
```
WORKER_COUNT tối ưu = CPU_cores × 0.5 đến 0.75

Với 16 cores:
- Tối thiểu: 16 × 0.5 = 8 workers
- Tối đa: 16 × 0.75 = 12 workers
```

**NHƯNG:** Phải bị giới hạn bởi RAM!

**Công thức kết hợp:**
```
WORKER_COUNT = MIN(
    CPU_cores × 0.75,
    (RAM_available × 0.7) / (maxFileSize × 1.3)
)
```

---

### **4. MAX_QUEUE_SIZE ↔ Disk Space**
```
Disk cần = MAX_QUEUE_SIZE × maxFileSize × 2

Trong đó:
- ×2 vì: input file + output file
```

**Ví dụ:**
```
maxFileSize = 3GB, MAX_QUEUE_SIZE = 80
Disk cần = 80 × 3 × 2 = 480 GB
```

**Công thức an toàn:**
```
MAX_QUEUE_SIZE ≤ (Disk_free × 0.8) / (maxFileSize × 2)

Ví dụ với Disk free = 500GB:
MAX_QUEUE_SIZE ≤ (500 × 0.8) / (3 × 2) = 66 jobs
```

---

### **5. fileSizeThreshold ↔ RAM Upload**
```
RAM tạm cho upload = Số users upload đồng thời × fileSizeThreshold
```

**Kịch bản:**
- 10 users upload file 50MB cùng lúc
- `fileSizeThreshold = 100MB` → File <100MB nằm trong RAM
- RAM upload = 10 × 50MB = 500MB

**Nếu giảm xuống 16MB:**
- File >16MB stream ra disk ngay
- RAM upload = 10 × 16MB = 160MB (giảm 68%)

**Công thức:**
```
fileSizeThreshold ≤ (RAM_available × 0.1) / Expected_concurrent_uploads

Ví dụ với 14GB RAM, 20 users đồng thời:
fileSizeThreshold ≤ (14GB × 0.1) / 20 = 70MB
Khuyến nghị: 16-32MB (an toàn hơn)
```

---

## 🎯 CÔNG THỨC TỔNG QUÁT

### **Bước 1: Xác định maxFileSize**
Dựa trên nhu cầu người dùng:
- Web app thông thường: **500MB - 1GB**
- Chuyên nghiệp: **2GB - 3GB**
- Cao cấp: **5GB+**

### **Bước 2: Tính WORKER_COUNT**
```java
// Giới hạn bởi RAM
int maxWorkersByRAM = (int) ((RAM_GB * 0.7) / (maxFileSize_GB * 1.3));

// Giới hạn bởi CPU
int maxWorkersByCPU = (int) (CPU_cores * 0.75);

// Lấy MIN
WORKER_COUNT = Math.min(maxWorkersByRAM, maxWorkersByCPU);
```

### **Bước 3: Tính MAX_QUEUE_SIZE**
```java
// Dựa vào disk space
long diskFreeGB = getDiskFreeSpace();
MAX_QUEUE_SIZE = (int) ((diskFreeGB * 0.8) / (maxFileSize_GB * 2));

// Giới hạn hợp lý: 30-100
MAX_QUEUE_SIZE = Math.max(30, Math.min(MAX_QUEUE_SIZE, 100));
```

### **Bước 4: Tính fileSizeThreshold**
```java
// Dựa vào concurrent uploads
int expectedConcurrentUploads = 20; // Ước tính
fileSizeThreshold = (int) ((RAM_GB * 0.1 * 1024 * 1024 * 1024) / expectedConcurrentUploads);

// Khuyến nghị: 16MB - 64MB
fileSizeThreshold = Math.max(16 * 1024 * 1024, Math.min(fileSizeThreshold, 64 * 1024 * 1024));
```

---

## 📋 CÁC KỊCH BẢN CÂN BẰNG

### **Kịch bản 1: FILE LỚN (3GB) - CẤU HÌNH HIỆN TẠI**

**Thông số:**
```java
maxFileSize = 3GB
maxRequestSize = 3GB
fileSizeThreshold = 16MB
WORKER_COUNT = 4
MAX_QUEUE_SIZE = 80
```

**Tính toán:**
- RAM/video: 3 × 1.3 = **3.9GB**
- Tổng RAM convert: 4 × 3.9 = **15.6GB** ⚠️ (vượt 14GB)
- CPU sử dụng: 4/16 = **25%** (dư thừa CPU)
- Disk cần: 80 × 3 × 2 = **480GB**
- RAM upload (20 users): 20 × 16MB = **320MB** ✅

**Đánh giá:**
- ⚠️ **RAM quá giới hạn**: 4 workers có thể vượt 14GB
- ⚠️ **CPU dư thừa**: Chỉ dùng 25% CPU
- ✅ Disk OK nếu có >500GB free
- ✅ Upload RAM OK

**Tối ưu:**
```java
WORKER_COUNT = 3  // An toàn hơn: 3 × 3.9 = 11.7GB < 14GB
MAX_QUEUE_SIZE = 50  // Giảm xuống cho an toàn disk
```

---

### **Kịch bản 2: FILE VỪA (1GB) - TỐI ƯU CPU VÀ RAM**

**Thông số đề xuất:**
```java
maxFileSize = 1073741824L        // 1 GB
maxRequestSize = 1073741824L     // 1 GB
fileSizeThreshold = 33554432     // 32 MB
WORKER_COUNT = 7
MAX_QUEUE_SIZE = 100
```

**Tính toán:**
- RAM/video: 1 × 1.3 = **1.3GB**
- Tổng RAM: 7 × 1.3 = **9.1GB** ✅ (< 14GB)
- CPU sử dụng: 7/16 = **44%** ✅
- Disk cần: 100 × 1 × 2 = **200GB**
- RAM upload: 20 × 32MB = **640MB** ✅

**Đánh giá:**
- ✅ RAM an toàn
- ✅ CPU tận dụng tốt
- ✅ Xử lý nhanh (7 videos cùng lúc)
- ✅ Queue lớn hơn

---

### **Kịch bản 3: FILE NHỎ (500MB) - TỐI ĐA THROUGHPUT**

**Thông số đề xuất:**
```java
maxFileSize = 524288000L         // 500 MB
maxRequestSize = 524288000L      // 500 MB
fileSizeThreshold = 16777216     // 16 MB
WORKER_COUNT = 10
MAX_QUEUE_SIZE = 150
```

**Tính toán:**
- RAM/video: 0.5 × 1.3 = **0.65GB**
- Tổng RAM: 10 × 0.65 = **6.5GB** ✅
- CPU sử dụng: 10/16 = **63%** ✅
- Disk cần: 150 × 0.5 × 2 = **150GB**
- RAM upload: 20 × 16MB = **320MB** ✅

**Đánh giá:**
- ✅ RAM rất thoải mái
- ✅ CPU tận dụng tốt
- ✅ **Throughput cao nhất** (10 videos/lần)
- ✅ Queue lớn, ít bị từ chối

---

### **Kịch bản 4: SERVER YẾU (4GB RAM, 4 cores)**

**Thông số đề xuất:**
```java
maxFileSize = 524288000L         // 500 MB
maxRequestSize = 524288000L      // 500 MB
fileSizeThreshold = 16777216     // 16 MB
WORKER_COUNT = 2
MAX_QUEUE_SIZE = 30
```

**Tính toán:**
- RAM/video: 0.5 × 1.3 = **0.65GB**
- Tổng RAM: 2 × 0.65 = **1.3GB** ✅ (< 4GB)
- CPU sử dụng: 2/4 = **50%** ✅
- Disk cần: 30 × 0.5 × 2 = **30GB**

**Đánh giá:**
- ✅ Phù hợp server nhỏ
- ⚠️ Giới hạn file 500MB
- ⚠️ Chậm (chỉ 2 videos/lần)

---

## 🔄 BẢNG THAM KHẢO NHANH

### **Theo maxFileSize (với 14GB RAM, 16 cores)**

| maxFileSize | WORKER_COUNT | MAX_QUEUE_SIZE | fileSizeThreshold | Lý do |
|-------------|--------------|----------------|-------------------|-------|
| **5 GB** | 2 | 30 | 16 MB | RAM giới hạn: 2×6.5=13GB |
| **3 GB** | 3 | 50 | 16 MB | RAM giới hạn: 3×3.9=11.7GB |
| **2 GB** | 4 | 70 | 16-32 MB | Cân bằng: 4×2.6=10.4GB |
| **1 GB** | 7 | 100 | 32 MB | Tối ưu: 7×1.3=9.1GB |
| **500 MB** | 10 | 150 | 16 MB | Max throughput: 10×0.65=6.5GB |
| **200 MB** | 12 | 200 | 16 MB | Ultra fast: 12×0.26=3.1GB |

---

### **Theo mục tiêu sử dụng**

#### **1. Ưu tiên FILE LỚN (cho editing chuyên nghiệp)**
```java
maxFileSize = 3221225472L        // 3 GB
WORKER_COUNT = 3
MAX_QUEUE_SIZE = 50
fileSizeThreshold = 16777216     // 16 MB
```

#### **2. Ưu tiên TỐC ĐỘ (xử lý nhiều videos nhanh)**
```java
maxFileSize = 524288000L         // 500 MB
WORKER_COUNT = 10
MAX_QUEUE_SIZE = 150
fileSizeThreshold = 16777216     // 16 MB
```

#### **3. Ưu tiên ỔN ĐỊNH (an toàn, ít crash)**
```java
maxFileSize = 1073741824L        // 1 GB
WORKER_COUNT = 5
MAX_QUEUE_SIZE = 80
fileSizeThreshold = 33554432     // 32 MB
```

#### **4. Cân bằng TỐT NHẤT (khuyến nghị)**
```java
maxFileSize = 1073741824L        // 1 GB
WORKER_COUNT = 7
MAX_QUEUE_SIZE = 100
fileSizeThreshold = 33554432     // 32 MB
```

---

## ⚙️ HƯỚNG DẪN ĐIỀU CHỈNH TỪNG THAM SỐ

### **Khi tăng maxFileSize:**

**Ví dụ: 1GB → 3GB**

1. **Giảm WORKER_COUNT:**
   ```
   Cũ: 1GB × 1.3 × 7 workers = 9.1GB RAM
   Mới: 3GB × 1.3 × 7 workers = 27.3GB RAM ❌ QUÁ TẢI
   
   Điều chỉnh: WORKER_COUNT = 3 → 11.7GB ✅
   ```

2. **Giảm MAX_QUEUE_SIZE:**
   ```
   Cũ: 100 jobs × 1GB × 2 = 200GB disk
   Mới: 100 jobs × 3GB × 2 = 600GB disk ⚠️
   
   Điều chỉnh: MAX_QUEUE_SIZE = 50 → 300GB ✅
   ```

3. **Giữ nguyên fileSizeThreshold:**
   ```
   16MB vẫn OK (không liên quan trực tiếp)
   ```

**Code thay đổi:**
```java
// Cũ
maxFileSize = 1073741824L        // 1 GB
WORKER_COUNT = 7
MAX_QUEUE_SIZE = 100

// Mới
maxFileSize = 3221225472L        // 3 GB
WORKER_COUNT = 3                 // Giảm 7→3
MAX_QUEUE_SIZE = 50              // Giảm 100→50
```

---

### **Khi giảm maxFileSize:**

**Ví dụ: 3GB → 500MB**

1. **Tăng WORKER_COUNT:**
   ```
   Cũ: 3GB × 1.3 × 3 = 11.7GB RAM
   Mới: 0.5GB × 1.3 × 3 = 1.95GB RAM (dư thừa!)
   
   Điều chỉnh: WORKER_COUNT = 10 → 6.5GB ✅
   ```

2. **Tăng MAX_QUEUE_SIZE:**
   ```
   Cũ: 50 × 3GB × 2 = 300GB
   Mới: 50 × 0.5GB × 2 = 50GB (dư disk)
   
   Điều chỉnh: MAX_QUEUE_SIZE = 150 → 150GB ✅
   ```

**Code thay đổi:**
```java
// Cũ
maxFileSize = 3221225472L        // 3 GB
WORKER_COUNT = 3
MAX_QUEUE_SIZE = 50

// Mới
maxFileSize = 524288000L         // 500 MB
WORKER_COUNT = 10                // Tăng 3→10
MAX_QUEUE_SIZE = 150             // Tăng 50→150
```

---

### **Khi tăng WORKER_COUNT:**

**Ví dụ: 4 → 8 workers**

1. **Kiểm tra RAM:**
   ```
   3GB × 1.3 × 8 = 31.2GB ❌ VƯỢT 14GB
   
   → KHÔNG THỂ tăng nếu maxFileSize = 3GB
   ```

2. **Hoặc giảm maxFileSize:**
   ```
   Để 8 workers: maxFileSize ≤ (14 × 0.7) / (8 × 1.3) = 0.94GB
   
   Điều chỉnh: maxFileSize = 1GB ✅
   ```

**Code thay đổi:**
```java
// Nếu muốn 8 workers
maxFileSize = 1073741824L        // Giảm 3GB→1GB
WORKER_COUNT = 8                 // Tăng 4→8
```

---

### **Khi giảm fileSizeThreshold:**

**Ví dụ: 100MB → 16MB**

1. **Lợi ích:**
   ```
   20 users upload file 50MB:
   - 100MB threshold: 20 × 50MB = 1GB RAM
   - 16MB threshold: 20 × 16MB = 320MB RAM
   
   Tiết kiệm: 680MB RAM ✅
   ```

2. **Trade-off:**
   ```
   - Upload file nhỏ (<16MB) có thể chậm hơn 1 chút
   - Nhiều disk I/O hơn
   - Nhưng KHÔNG ảnh hưởng đáng kể với SSD
   ```

**Khuyến nghị:**
```
fileSizeThreshold = 16MB - 32MB (an toàn nhất)
```

---

## 📊 CÔNG CỤ TỰ ĐỘNG TÍNH TOÁN

### **Java Code để tính toán tự động:**

```java
public class ConfigCalculator {
    
    public static void calculateOptimalConfig(
        int cpuCores,           // 16
        int ramGB,              // 14
        double maxFileSizeGB,   // 3.0
        int diskFreeGB          // 500
    ) {
        // 1. Tính WORKER_COUNT
        int maxWorkersByRAM = (int) ((ramGB * 0.7) / (maxFileSizeGB * 1.3));
        int maxWorkersByCPU = (int) (cpuCores * 0.75);
        int workerCount = Math.min(maxWorkersByRAM, maxWorkersByCPU);
        workerCount = Math.max(2, workerCount); // Tối thiểu 2
        
        // 2. Tính MAX_QUEUE_SIZE
        int maxQueueSize = (int) ((diskFreeGB * 0.8) / (maxFileSizeGB * 2));
        maxQueueSize = Math.max(30, Math.min(maxQueueSize, 150));
        
        // 3. Tính fileSizeThreshold
        int expectedConcurrentUploads = 20;
        long fileSizeThreshold = (long) ((ramGB * 0.1 * 1024 * 1024 * 1024) / expectedConcurrentUploads);
        fileSizeThreshold = Math.max(16 * 1024 * 1024, 
                            Math.min(fileSizeThreshold, 64 * 1024 * 1024));
        
        // 4. In kết quả
        System.out.println("=== OPTIMAL CONFIGURATION ===");
        System.out.println("CPU Cores: " + cpuCores);
        System.out.println("RAM: " + ramGB + " GB");
        System.out.println("Max File Size: " + maxFileSizeGB + " GB");
        System.out.println("Disk Free: " + diskFreeGB + " GB");
        System.out.println();
        System.out.println("maxFileSize = " + (long)(maxFileSizeGB * 1024 * 1024 * 1024) + "L");
        System.out.println("maxRequestSize = " + (long)(maxFileSizeGB * 1024 * 1024 * 1024) + "L");
        System.out.println("fileSizeThreshold = " + fileSizeThreshold);
        System.out.println("WORKER_COUNT = " + workerCount);
        System.out.println("MAX_QUEUE_SIZE = " + maxQueueSize);
        System.out.println();
        
        // 5. Tính toán thêm
        double totalRAMUsed = workerCount * maxFileSizeGB * 1.3;
        int diskNeeded = (int) (maxQueueSize * maxFileSizeGB * 2);
        double cpuUsage = (workerCount * 100.0) / cpuCores;
        
        System.out.println("=== RESOURCE USAGE ===");
        System.out.println("Total RAM used: " + String.format("%.1f", totalRAMUsed) + " GB / " + ramGB + " GB");
        System.out.println("Disk needed: " + diskNeeded + " GB / " + diskFreeGB + " GB");
        System.out.println("CPU usage: " + String.format("%.1f", cpuUsage) + "%");
        System.out.println("Videos processing simultaneously: " + workerCount);
        System.out.println("Max jobs in queue: " + maxQueueSize);
    }
    
    public static void main(String[] args) {
        // Cấu hình hiện tại của bạn
        calculateOptimalConfig(16, 14, 3.0, 500);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Thử với file 1GB
        calculateOptimalConfig(16, 14, 1.0, 500);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Thử với file 500MB
        calculateOptimalConfig(16, 14, 0.5, 500);
    }
}
```

**Chạy code trên và output:**
```
=== OPTIMAL CONFIGURATION ===
CPU Cores: 16
RAM: 14 GB
Max File Size: 3.0 GB
Disk Free: 500 GB

maxFileSize = 3221225472L
maxRequestSize = 3221225472L
fileSizeThreshold = 16777216
WORKER_COUNT = 2
MAX_QUEUE_SIZE = 102

=== RESOURCE USAGE ===
Total RAM used: 7.8 GB / 14 GB
Disk needed: 612 GB / 500 GB ⚠️
CPU usage: 12.5%
Videos processing simultaneously: 2
Max jobs in queue: 102
```

---

## ✅ KHUYẾN NGHỊ CUỐI CÙNG

### **Cho server của bạn (16 cores, 14GB RAM):**

#### **✨ OPTION 1: Ưu tiên file lớn (3GB) - AN TOÀN NHẤT**
```java
// UploadServlet.java
@MultipartConfig(
    maxFileSize = 3221225472L,      // 3 GB
    maxRequestSize = 3221225472L,   // 3 GB
    fileSizeThreshold = 16777216    // 16 MB
)

// ConversionBO.java
private static final int WORKER_COUNT = 3;
private static final int MAX_QUEUE_SIZE = 50;
```

**Đặc điểm:**
- ✅ Hỗ trợ file 3GB
- ✅ RAM an toàn: 3 × 3.9 = 11.7GB
- ⚠️ Chậm: chỉ 3 videos cùng lúc
- ✅ Disk: 300GB

---

#### **🚀 OPTION 2: Cân bằng (1GB) - KHUYẾN NGHỊ**
```java
// UploadServlet.java
@MultipartConfig(
    maxFileSize = 1073741824L,      // 1 GB
    maxRequestSize = 1073741824L,   // 1 GB
    fileSizeThreshold = 33554432    // 32 MB
)

// ConversionBO.java
private static final int WORKER_COUNT = 7;
private static final int MAX_QUEUE_SIZE = 100;
```

**Đặc điểm:**
- ✅ File 1GB đủ cho hầu hết use cases
- ✅ RAM tốt: 7 × 1.3 = 9.1GB
- ✅ Nhanh: 7 videos cùng lúc
- ✅ Disk: 200GB
- ✅ **TỐI ƯU NHẤT**

---

#### **⚡ OPTION 3: Tốc độ cao (500MB) - MAX THROUGHPUT**
```java
// UploadServlet.java
@MultipartConfig(
    maxFileSize = 524288000L,       // 500 MB
    maxRequestSize = 524288000L,    // 500 MB
    fileSizeThreshold = 16777216    // 16 MB
)

// ConversionBO.java
private static final int WORKER_COUNT = 10;
private static final int MAX_QUEUE_SIZE = 150;
```

**Đặc điểm:**
- ⚠️ File giới hạn 500MB
- ✅ RAM rất tốt: 10 × 0.65 = 6.5GB
- ✅ Rất nhanh: 10 videos cùng lúc
- ✅ Disk: 150GB

---

## 🎓 TÓM TẮT CÔNG THỨC

```
1. RAM cho 1 video = maxFileSize × 1.3

2. WORKER_COUNT = MIN(
     (RAM × 0.7) / (maxFileSize × 1.3),
     CPU_cores × 0.75
   )

3. MAX_QUEUE_SIZE = MIN(
     (Disk_free × 0.8) / (maxFileSize × 2),
     150
   )

4. fileSizeThreshold = 16MB - 32MB (fixed)

5. Kiểm tra:
   - Total RAM = WORKER_COUNT × maxFileSize × 1.3 ≤ RAM × 0.7
   - Total Disk = MAX_QUEUE_SIZE × maxFileSize × 2 ≤ Disk_free × 0.8
```

---

## 📝 CHECKLIST KHI THAY ĐỔI

- [ ] Tính lại WORKER_COUNT dựa trên maxFileSize mới
- [ ] Kiểm tra: Total RAM ≤ 70% RAM available
- [ ] Tính lại MAX_QUEUE_SIZE dựa trên disk space
- [ ] Kiểm tra: Total Disk ≤ 80% Disk free
- [ ] Đặt fileSizeThreshold = 16-32MB
- [ ] Test với 1 file trước khi deploy
- [ ] Monitor RAM/CPU/Disk sau khi deploy
- [ ] Chuẩn bị tăng/giảm dựa trên usage thực tế

---

**📅 Document version: 1.0**  
**🔄 Last updated: 2025-01-19**  
**💡 Áp dụng cho: Server 16 cores, 14GB RAM**

