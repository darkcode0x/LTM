# Update Guide - Admin Features & Activity Logs

## 📌 Version 1.3.0 - November 7, 2025

### 🎯 What's New?

✅ **Admin Panel** - Complete user management dashboard  
✅ **Role-Based Access** - USER and ADMIN roles  
✅ **Activity Logging** - Track all user actions  
✅ **User Management** - Enable/disable accounts, update quotas  
✅ **Activity Monitoring** - View logs by action type

---

## 🔄 Update Steps

### 1. **Backup Current Database**

```sql
-- Create backup
mysqldump -u root -p video_converter > backup_video_converter_$(date +%Y%m%d).sql
```

### 2. **Stop Tomcat Server**

```cmd
# Windows
D:\apache-tomcat-10.1.48\bin\shutdown.bat

# Or kill process
taskkill /F /IM java.exe
```

### 3. **Update Database Schema**

```cmd
# Open MySQL
mysql -u root -p

# Run migration script
USE video_converter;
SOURCE D:/XUANQUOC/Desktop/LTM/VideoConverter/migration_v1.3.0.sql;
```

**Or manually execute:**

```sql
-- Add role column
ALTER TABLE users ADD COLUMN role ENUM('USER', 'ADMIN') DEFAULT 'USER' AFTER avatar;

-- Set admin role for admin user
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';

-- Verify
SELECT user_id, username, role FROM users;
```

### 4. **Deploy New WAR File**

```cmd
# Copy new WAR file
copy target\video-converter-1.0-SNAPSHOT.war D:\apache-tomcat-10.1.48\webapps\

# Or rename for shorter URL
copy target\video-converter-1.0-SNAPSHOT.war D:\apache-tomcat-10.1.48\webapps\VideoConverter.war
```

### 5. **Start Tomcat**

```cmd
D:\apache-tomcat-10.1.48\bin\startup.bat
```

### 6. **Test Admin Features**

1. **Login as admin:**
   - Username: `admin`
   - Password: `admin123`
   
2. **Access admin panel:**
   - URL: http://localhost:8080/video-converter-1.0-SNAPSHOT/admin
   - Or: http://localhost:8080/VideoConverter/admin

3. **Test features:**
   - View dashboard statistics
   - Manage users (enable/disable, update quota)
   - View activity logs
   - Filter logs by action type

---

## 🆕 New Features Details

### Admin Dashboard (`/admin`)

- **Statistics Cards:**
  - Total Users
  - Active Users  
  - Administrator Count

- **Recent Activity:**
  - Last 24 hours of activity
  - User details and actions
  - IP addresses logged

### User Management (`/admin?action=users`)

- **View all users** with details:
  - Avatar, username, email
  - Role (USER/ADMIN)
  - Status (Active/Inactive)
  - Daily quota
  - Total conversions
  - Join date

- **Actions:**
  - ✅ Enable/Disable accounts
  - 🔢 Update daily quota (0-100)
  - 🚫 Cannot disable your own account

### Activity Logs (`/admin?action=logs`)

- **Track actions:**
  - LOGIN - User login
  - LOGOUT - User logout
  - UPLOAD - Video upload
  - DOWNLOAD - Video download
  - DELETE - Job deletion
  - ADMIN_ACTION - Admin operations

- **Filter by:**
  - All activities
  - Login/Logout events
  - Upload/Download actions
  - Delete operations
  - Admin actions

- **Display:**
  - Username and email
  - Action type with color badges
  - Description
  - IP address
  - Timestamp

---

## 🔐 Access Control

### User Roles:

| Role | Access |
|------|--------|
| **USER** | Upload, Status, Profile, Download |
| **ADMIN** | All user features + Admin Panel |

### Admin Panel Access:

- Only users with `role='ADMIN'` can access
- Non-admin users redirected to login
- Admin menu appears in navigation bar
- Yellow "Admin" badge in header

---

## 📊 Database Changes

### Users Table:

```sql
-- New column
role ENUM('USER', 'ADMIN') DEFAULT 'USER'
```

### Activity Logs Table:

Already exists from `database.sql`:

```sql
CREATE TABLE activity_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

---

## 🔧 Code Changes

### New Classes:

1. **ActivityLog.java** - Model for activity logs
2. **ActivityLogDAO.java** - Database operations for logs
3. **AdminServlet.java** - Admin panel controller

### Updated Classes:

1. **User.java** - Added `role` field and `isAdmin()` method
2. **UserDAO.java** - Added `role` to queries
3. **LoginServlet.java** - Logs login activity, redirects admin to dashboard
4. **LogoutServlet.java** - Logs logout activity
5. **header.jsp** - Shows admin menu for admin users

### New JSP Pages:

1. **admin/dashboard.jsp** - Admin dashboard
2. **admin/users.jsp** - User management
3. **admin/logs.jsp** - Activity logs viewer

---

## 🧪 Testing Checklist

- [ ] Database migration successful
- [ ] Admin user has ADMIN role
- [ ] Admin can login and access /admin
- [ ] Regular user cannot access /admin
- [ ] Dashboard shows correct statistics
- [ ] Can view all users in user management
- [ ] Can enable/disable user accounts
- [ ] Can update user quotas
- [ ] Activity logs are being recorded
- [ ] Can filter logs by action type
- [ ] Login/Logout activities logged
- [ ] Admin actions logged with descriptions
- [ ] Cannot disable own admin account

---

## 🐛 Troubleshooting

### Issue: Admin menu not showing

**Solution:**
```sql
-- Check user role
SELECT username, role FROM users WHERE username = 'admin';

-- If null or USER, update to ADMIN
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
```

### Issue: 404 on /admin

**Check:**
1. AdminServlet.java compiled
2. @WebServlet annotation present
3. Tomcat restarted after deployment

### Issue: Activity logs empty

**Check:**
```sql
-- Verify table exists
SHOW TABLES LIKE 'activity_logs';

-- Check permissions
SHOW GRANTS FOR 'root'@'localhost';

-- Test insert
INSERT INTO activity_logs (user_id, action, description, ip_address) 
VALUES (1, 'TEST', 'Test log', '127.0.0.1');
```

### Issue: Cannot access admin panel

**Verify:**
1. Logged in as admin user
2. User role is 'ADMIN' in database
3. Session contains user object
4. No authentication filter blocking

---

## 📝 Activity Log Actions

| Action | Description | When Triggered |
|--------|-------------|----------------|
| LOGIN | User logged in | LoginServlet.doPost() |
| LOGOUT | User logged out | LogoutServlet.handleLogout() |
| UPLOAD | Video uploaded | UploadServlet (to be added) |
| DOWNLOAD | Video downloaded | DownloadServlet (to be added) |
| DELETE | Job deleted | DeleteJobServlet (to be added) |
| ADMIN_ACTION | Admin operation | AdminServlet (toggle status, update quota) |

---

## 🔒 Security Notes

- Admin role required for `/admin` access
- Activity logs include IP addresses
- Cannot disable your own admin account
- All admin actions are logged
- User passwords remain hashed with BCrypt

---

## 📈 Future Enhancements

- [ ] Export activity logs to CSV
- [ ] Email notifications for admin actions
- [ ] User role promotion/demotion
- [ ] Bulk user operations
- [ ] Advanced log filtering (date range, user)
- [ ] Activity log retention policy
- [ ] Real-time admin dashboard with WebSocket

---

## 📞 Support

**Database Issues:**
```sql
-- Reset to default state
USE video_converter;
SOURCE database.sql;
SOURCE migration_v1.3.0.sql;
```

**Application Issues:**
- Check Tomcat logs: `CATALINA_HOME/logs/catalina.out`
- Verify compilation: `mvn clean package -DskipTests`
- Clear browser cache and cookies

---

**Updated:** November 7, 2025  
**Version:** 1.3.0  
**Compatible with:** VideoConverter v1.2.0+
