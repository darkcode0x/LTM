# Video Converter - Testing Guide

## 📋 Pre-Testing Checklist

### 1. Database Setup
- [ ] MySQL Server running
- [ ] Database `video_converter` created
- [ ] Tables created from `database.sql`
- [ ] Connection configured in `DBConnection.java`

### 2. FFmpeg Installation
- [ ] FFmpeg installed
- [ ] Added to System PATH
- [ ] `ffmpeg -version` works in CMD

### 3. Project Build
- [ ] Maven dependencies downloaded
- [ ] Project compiled without errors
- [ ] WAR file generated

### 4. Server Setup
- [ ] Tomcat 10+ installed
- [ ] Application deployed
- [ ] Server started successfully

## 🗄️ Database Setup Commands

### Create Database
```sql
-- Open MySQL Command Line or Workbench
CREATE DATABASE IF NOT EXISTS video_converter 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE video_converter;

-- Run the database.sql file
SOURCE D:/XUANQUOC/Desktop/LTM/VideoConverter/database.sql;

-- Verify tables created
SHOW TABLES;

-- Insert test user (optional)
INSERT INTO users (username, email, password, full_name, phone, daily_quota)
VALUES 
('admin', 'admin@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
 'Admin User', '1234567890', 100),
('demo', 'demo@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
 'Demo User', '0987654321', 50);

-- Password for both: admin123
```

## 🚀 Build and Deploy

### Using Maven
```cmd
cd D:\XUANQUOC\Desktop\LTM\VideoConverter

# Clean and build
mvn clean package

# Output: target\VideoConverter.war
```

### Deploy to Tomcat
1. Copy `target\VideoConverter.war` to `TOMCAT_HOME\webapps\`
2. Start Tomcat
3. Wait for deployment (check logs)
4. Access: http://localhost:8080/VideoConverter

## 🧪 Test Cases

### Test 1: Registration
**Steps:**
1. Go to: http://localhost:8080/VideoConverter/register
2. Fill form:
   - Username: testuser
   - Email: test@example.com
   - Full Name: Test User
   - Phone: 1234567890
   - Password: test123
   - Confirm Password: test123
3. Click "Create Account"

**Expected:**
- ✅ Redirected to login page
- ✅ Success message displayed
- ✅ User record in database

**Verify Database:**
```sql
SELECT * FROM users WHERE username = 'testuser';
```

### Test 2: Login
**Steps:**
1. Go to: http://localhost:8080/VideoConverter/login
2. Enter credentials:
   - Username: admin
   - Password: admin123
3. Click "Sign In"

**Expected:**
- ✅ Redirected to upload page
- ✅ User info in navbar
- ✅ Session created
- ✅ last_login updated in database

**Verify Session:**
- Check navbar shows username
- Try accessing /profile (should work)

### Test 3: Video Upload
**Steps:**
1. Login as admin
2. Go to: http://localhost:8080/VideoConverter/upload
3. Select a test video file (MP4, < 500MB)
4. Configure settings:
   - Output Format: MP4
   - Resolution: 1280x720
   - Quality: Medium
5. Click "Upload & Convert"

**Expected:**
- ✅ File uploaded successfully
- ✅ Redirected to status page
- ✅ Job appears with "Pending" status
- ✅ Video record in database
- ✅ ConversionJob record created

**Verify Database:**
```sql
-- Check video uploaded
SELECT * FROM videos ORDER BY uploaded_at DESC LIMIT 1;

-- Check job created
SELECT * FROM conversion_jobs ORDER BY created_at DESC LIMIT 1;
```

**Verify Files:**
```cmd
# Check upload directory
dir D:\XUANQUOC\Desktop\LTM\VideoConverter\src\main\webapp\uploads
```

### Test 4: Conversion Process
**Steps:**
1. After upload, stay on status page
2. Watch job status change:
   - Pending → Processing → Completed
3. Check progress percentage updates

**Expected:**
- ✅ Status changes automatically
- ✅ Progress bar animates (0% → 100%)
- ✅ Page auto-refreshes every 5 seconds
- ✅ Download button appears when completed

**Monitor Logs:**
```
Check Tomcat logs (catalina.out or console):
- ConversionService processing job
- FFmpeg command execution
- Progress updates
```

**Verify Database:**
```sql
-- Check job status progression
SELECT job_id, status, progress, error_message 
FROM conversion_jobs 
ORDER BY created_at DESC LIMIT 5;
```

**Verify Output File:**
```cmd
# Check converted directory
dir D:\XUANQUOC\Desktop\LTM\VideoConverter\src\main\webapp\converted
```

### Test 5: Download Converted Video
**Steps:**
1. On status page, find completed job
2. Click download button (green download icon)

**Expected:**
- ✅ File download starts
- ✅ Correct filename
- ✅ Video plays correctly
- ✅ File size reasonable

**Verify:**
- Open downloaded video in media player
- Check resolution matches settings
- Verify format is correct

### Test 6: Profile Management
**Steps:**
1. Go to: http://localhost:8080/VideoConverter/profile
2. View statistics
3. Update profile:
   - Change Full Name
   - Update Email
   - Click "Save Changes"

**Expected:**
- ✅ Statistics displayed correctly
- ✅ Profile updated
- ✅ Success message shown
- ✅ Database updated

**Verify Database:**
```sql
SELECT user_id, username, email, full_name, total_conversions 
FROM users WHERE username = 'admin';
```

### Test 7: Change Password
**Steps:**
1. On profile page, scroll to "Change Password"
2. Fill form:
   - Current Password: admin123
   - New Password: newpass123
   - Confirm: newpass123
3. Click "Change Password"

**Expected:**
- ✅ Password updated
- ✅ Success message
- ✅ Can login with new password

**Verify:**
- Logout
- Login with new password
- Should work

### Test 8: Delete Job
**Steps:**
1. On status page, find any job
2. Click delete button (red trash icon)
3. Confirm deletion

**Expected:**
- ✅ Confirmation dialog appears
- ✅ Job removed from list
- ✅ Files deleted from disk
- ✅ Database record deleted

**Verify Database:**
```sql
-- Job should be gone
SELECT * FROM conversion_jobs WHERE job_id = ?;
```

### Test 9: Logout
**Steps:**
1. Click username dropdown in navbar
2. Click "Logout"

**Expected:**
- ✅ Redirected to login page
- ✅ Session destroyed
- ✅ Cannot access protected pages

**Verify:**
- Try accessing /upload directly
- Should redirect to login

### Test 10: Authentication Filter
**Steps:**
1. Logout completely
2. Try accessing URLs directly:
   - http://localhost:8080/VideoConverter/upload
   - http://localhost:8080/VideoConverter/status
   - http://localhost:8080/VideoConverter/profile

**Expected:**
- ✅ All redirect to login page
- ✅ Original URL saved
- ✅ After login, redirect to intended page

## 🔍 Advanced Testing

### Test 11: Multiple Concurrent Uploads
**Steps:**
1. Open 3 browser windows
2. Login in each
3. Upload different videos simultaneously

**Expected:**
- ✅ All uploads succeed
- ✅ Jobs processed by worker threads
- ✅ No conflicts or errors

### Test 12: Large File Upload
**Steps:**
1. Upload file close to 500MB limit

**Expected:**
- ✅ Upload completes
- ✅ Progress shown
- ✅ Conversion successful

### Test 13: Failed Conversion
**Steps:**
1. Upload corrupted or invalid video
2. Watch status

**Expected:**
- ✅ Status changes to "Failed"
- ✅ Error message displayed
- ✅ Can retry or delete

### Test 14: Application Restart
**Steps:**
1. Upload video (don't wait for completion)
2. Restart Tomcat while processing
3. Start Tomcat again

**Expected:**
- ✅ Pending jobs loaded on startup
- ✅ Processing resumes automatically
- ✅ No data loss

## 📊 Performance Testing

### Monitor System Resources
```cmd
# Check CPU usage during conversion
tasklist /fi "imagename eq java.exe" /v

# Check memory
tasklist /fi "imagename eq java.exe" /fo table
```

### Test Multiple Jobs
1. Upload 5-10 videos
2. Monitor:
   - Database connections
   - Thread pool usage
   - Memory usage
   - Disk I/O

## 🐛 Common Issues & Solutions

### Issue 1: Database Connection Failed
**Solution:**
```java
// Check DBConnection.java
- Verify MySQL is running
- Check credentials (username/password)
- Verify database name exists
```

### Issue 2: FFmpeg Not Found
**Solution:**
```cmd
# Verify PATH
echo %PATH%

# Test directly
ffmpeg -version

# If fails, add to PATH or use full path in code
```

### Issue 3: Upload Failed - File Too Large
**Solution:**
```xml
<!-- Check web.xml or @MultipartConfig -->
<max-file-size>524288000</max-file-size> <!-- 500MB -->
```

### Issue 4: Conversion Stuck
**Solution:**
```
- Check Tomcat logs for errors
- Verify FFmpeg is working
- Check disk space
- Restart ConversionService
```

## ✅ Final Validation Checklist

- [ ] All 14 test cases passed
- [ ] No errors in Tomcat logs
- [ ] Database records correct
- [ ] Files uploaded/converted correctly
- [ ] Authentication working
- [ ] Authorization enforced
- [ ] Session management working
- [ ] Auto-refresh functioning
- [ ] File cleanup on delete
- [ ] Password hashing secure
- [ ] Form validation working
- [ ] Error messages displayed
- [ ] Success messages shown
- [ ] Responsive design works on mobile

## 📝 Test Report Template

```
Date: _______________________
Tester: _____________________
Version: ____________________

Test Results:
[ ] Registration: PASS/FAIL
[ ] Login: PASS/FAIL
[ ] Upload: PASS/FAIL
[ ] Conversion: PASS/FAIL
[ ] Download: PASS/FAIL
[ ] Profile: PASS/FAIL
[ ] Password Change: PASS/FAIL
[ ] Delete Job: PASS/FAIL
[ ] Logout: PASS/FAIL
[ ] Auth Filter: PASS/FAIL

Issues Found:
1. _______________________
2. _______________________
3. _______________________

Performance Notes:
- Upload Speed: _______
- Conversion Time: _______
- Memory Usage: _______

Overall Status: PASS/FAIL
```

## 🎓 Next Steps After Testing

1. **Fix any bugs found**
2. **Optimize performance**
3. **Add more features** (optional):
   - Batch conversion
   - Video preview
   - Sharing functionality
   - Admin panel
4. **Deploy to production server**
5. **Setup monitoring**
6. **Create user documentation**

Good luck with testing! 🚀
