# Video Converter - Complete Application

## 📦 Project Overview

**Video Converter** là ứng dụng web Java cho phép người dùng upload và convert video sang các định dạng khác nhau sử dụng FFmpeg.

### 🎯 Core Features
- ✅ User Authentication & Authorization (BCrypt password hashing)
- ✅ Video Upload (max 500MB)
- ✅ Video Format Conversion (MP4, AVI, MKV, WebM, MOV, FLV)
- ✅ Resolution & Quality Settings
- ✅ Real-time Progress Tracking
- ✅ Background Processing with Worker Threads
- ✅ Profile Management
- ✅ Job History & Management
- ✅ Auto-refresh Status Page
- ✅ Responsive Bootstrap 5 UI

## 🏗️ Architecture

### Technology Stack
- **Backend:** Java 11, Jakarta EE 6.0, Servlets
- **Frontend:** JSP, Bootstrap 5, JavaScript
- **Database:** MySQL 8.0+
- **Video Processing:** FFmpeg
- **Build Tool:** Maven
- **Server:** Apache Tomcat 10+

### Design Patterns
- MVC (Model-View-Controller)
- DAO (Data Access Object)
- Singleton (ConversionService)
- Producer-Consumer (BlockingQueue)
- Filter (Authentication)

## 📁 Project Structure

```
VideoConverter/
├── src/
│   ├── main/
│   │   ├── java/com/videoconverter/
│   │   │   ├── controller/          # Servlets
│   │   │   │   ├── LoginServlet.java
│   │   │   │   ├── RegisterServlet.java
│   │   │   │   ├── UploadServlet.java
│   │   │   │   ├── StatusServlet.java
│   │   │   │   ├── ProfileServlet.java
│   │   │   │   ├── DownloadServlet.java
│   │   │   │   └── DeleteJobServlet.java
│   │   │   ├── model/               # POJOs
│   │   │   │   ├── User.java
│   │   │   │   ├── Video.java
│   │   │   │   ├── ConversionJob.java
│   │   │   │   └── ConversionSettings.java
│   │   │   ├── dao/                 # Data Access
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── VideoDAO.java
│   │   │   │   └── ConversionJobDAO.java
│   │   │   ├── service/             # Business Logic
│   │   │   │   └── ConversionService.java
│   │   │   ├── util/                # Utilities
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── PasswordUtil.java
│   │   │   │   └── FFmpegWrapper.java
│   │   │   ├── filter/              # Security
│   │   │   │   └── AuthenticationFilter.java
│   │   │   └── listener/            # Lifecycle
│   │   │       └── AppContextListener.java
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       ├── css/
│   │       │   └── style.css
│   │       ├── js/
│   │       │   └── script.js
│   │       ├── includes/
│   │       │   ├── header.jsp
│   │       │   └── footer.jsp
│   │       ├── uploads/             # Original videos
│   │       ├── converted/           # Converted videos
│   │       ├── login.jsp
│   │       ├── register.jsp
│   │       ├── upload.jsp
│   │       ├── status.jsp
│   │       └── profile.jsp
├── pom.xml
├── database.sql
├── QUICK_START.bat
├── FFMPEG_SETUP.md
├── TESTING_GUIDE.md
└── PROJECT_STATUS.md
```

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+
- FFmpeg 4.0+
- Apache Tomcat 10+

### 1. Verify Prerequisites
```cmd
.\QUICK_START.bat
```

### 2. Install FFmpeg
Follow instructions in `FFMPEG_SETUP.md`

### 3. Setup Database
```sql
CREATE DATABASE video_converter;
USE video_converter;
SOURCE database.sql;
```

### 4. Build & Deploy
```cmd
mvn clean package
copy target\VideoConverter.war %CATALINA_HOME%\webapps\
%CATALINA_HOME%\bin\startup.bat
```

### 5. Access Application
```
http://localhost:8080/VideoConverter
```

Default credentials:
- Username: `admin`
- Password: `admin123`

## 🧪 Testing

Follow the comprehensive testing guide:
```cmd
# See TESTING_GUIDE.md for 14 detailed test cases
```

Key areas to test:
1. User Registration & Login
2. Video Upload
3. Format Conversion
4. Progress Tracking
5. Download Converted Videos
6. Profile Management
7. Job Deletion
8. Authentication & Authorization

## 📊 Database Schema

### Tables
- **users** - User accounts and profiles
- **videos** - Uploaded video metadata
- **conversion_jobs** - Conversion tasks and status

### Key Relationships
- User → Videos (1:N)
- User → ConversionJobs (1:N)
- Video → ConversionJobs (1:N)

## 🔐 Security Features

- BCrypt password hashing (work factor: 10)
- Session-based authentication
- Global authentication filter
- Authorization checks on all actions
- SQL injection prevention (PreparedStatements)
- XSS protection (input validation)
- CSRF protection (session validation)

## 🎨 UI/UX Features

- Responsive Bootstrap 5 design
- Blue gradient theme
- Form validation (client & server-side)
- Password strength indicator
- Auto-refresh status page (5 seconds)
- Progress bars with animations
- Toast notifications
- Loading states
- Error messages

## ⚙️ Configuration

### Database Connection
Edit `src/main/java/com/videoconverter/util/DBConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/video_converter";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### FFmpeg Paths
Edit `src/main/java/com/videoconverter/util/FFmpegWrapper.java`:
```java
// Use "ffmpeg" if in PATH, or full path
private static final String FFMPEG_CMD = "ffmpeg";
private static final String FFPROBE_CMD = "ffprobe";
```

### Worker Threads
Edit `src/main/java/com/videoconverter/service/ConversionService.java`:
```java
private static final int WORKER_THREADS = 3;
```

### File Size Limit
Edit `src/main/java/com/videoconverter/controller/UploadServlet.java`:
```java
@MultipartConfig(
    maxFileSize = 524288000,      // 500 MB
    maxRequestSize = 524288000
)
```

## 📈 Performance

### Optimization Tips
1. Increase worker threads for more concurrent conversions
2. Use SSD for upload/converted directories
3. Enable connection pooling for database
4. Implement caching for video metadata
5. Use CDN for static resources in production

### Monitoring
- Check Tomcat logs: `logs/catalina.out`
- Monitor database connections
- Track conversion queue length
- Watch disk space usage

## 🐛 Troubleshooting

See `TESTING_GUIDE.md` section "Common Issues & Solutions"

Quick fixes:
- **FFmpeg not found:** Add to PATH and restart terminal
- **Database error:** Check MySQL is running and credentials are correct
- **Upload fails:** Check file size limit and disk space
- **Conversion stuck:** Check FFmpeg installation and logs

## 📝 API Documentation

### Servlets Endpoints

#### Authentication
- `GET/POST /login` - User login
- `GET/POST /register` - User registration
- `GET/POST /logout` - User logout

#### Core Features
- `GET/POST /upload` - Upload and convert video
- `GET /status?status={filter}` - View conversion jobs
- `GET /download?jobId={id}` - Download converted video
- `POST /deleteJob?jobId={id}` - Delete conversion job

#### Profile
- `GET/POST /profile` - View/update profile
  - Actions: updateProfile, changePassword, uploadAvatar

## 🔄 Conversion Workflow

1. **Upload:** User uploads video via /upload
2. **Validation:** File type and size checked
3. **Storage:** Original video saved to /uploads/
4. **Metadata:** Video info extracted with ffprobe
5. **Job Creation:** ConversionJob record created in DB
6. **Queue:** Job added to BlockingQueue
7. **Processing:** Worker thread picks up job
8. **Conversion:** FFmpeg converts video with settings
9. **Progress:** Real-time progress updates in DB
10. **Completion:** Converted video saved to /converted/
11. **Notification:** Status updated to COMPLETED
12. **Download:** User can download converted video

## 🎓 Learning Resources

This project demonstrates:
- Java Servlets & JSP development
- Database interaction with JDBC
- Multi-threading with ExecutorService
- Process management with ProcessBuilder
- File upload/download handling
- Session management
- Security best practices
- Bootstrap UI development
- MVC architecture
- DAO pattern implementation

## 📞 Support

For issues or questions:
1. Check `TESTING_GUIDE.md`
2. Review `FFMPEG_SETUP.md`
3. Check Tomcat logs
4. Verify database connection
5. Test FFmpeg installation

## 📄 License

This is an educational project for learning Java web development.

## 👨‍💻 Development

### Adding New Features

1. **Model:** Add POJO in `model/`
2. **DAO:** Create DAO in `dao/`
3. **Service:** Add business logic in `service/`
4. **Controller:** Create servlet in `controller/`
5. **View:** Add JSP in `webapp/`
6. **Test:** Follow testing guide

### Code Style
- Use meaningful variable names
- Add JavaDoc comments
- Follow Java naming conventions
- Handle exceptions properly
- Log important events
- Validate all inputs

## 🎯 Future Enhancements

See `PROJECT_STATUS.md` for complete roadmap:
- Batch conversion
- Video preview
- Email notifications
- Admin panel
- REST API
- Docker support
- Cloud storage integration

## ✅ Status

**Version:** 1.0.0  
**Status:** Development Complete ✅  
**Next:** Testing Phase  
**Last Updated:** November 7, 2025

---

**Made with ❤️ for learning Java Web Development**
