package com.videoconverter.dao;

import com.videoconverter.model.Video;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VideoDAO - Data Access Object for Video operations
 * Handles all database operations related to videos table
 */
public class VideoDAO {

    /**
     * Insert a new video record into database
     * 
     * @param video Video object to insert
     * @return Generated video ID, or -1 if failed
     */
    public int insert(Video video) {
        String sql = "INSERT INTO videos (user_id, original_filename, file_path, file_size, " +
                     "duration, resolution, format) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, video.getUserId());
            stmt.setString(2, video.getOriginalFilename());
            stmt.setString(3, video.getFilePath());
            stmt.setLong(4, video.getFileSize());
            
            // Handle nullable fields
            if (video.getDuration() > 0) {
                stmt.setInt(5, video.getDuration());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            if (video.getResolution() != null && !video.getResolution().isEmpty()) {
                stmt.setString(6, video.getResolution());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }
            
            if (video.getFormat() != null && !video.getFormat().isEmpty()) {
                stmt.setString(7, video.getFormat());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int videoId = generatedKeys.getInt(1);
                        video.setVideoId(videoId);
                        System.out.println("Video inserted successfully with ID: " + videoId);
                        return videoId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting video: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Find video by ID
     * 
     * @param videoId Video ID
     * @return Video object if found, null otherwise
     */
    public Video findById(int videoId) {
        String sql = "SELECT * FROM videos WHERE video_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, videoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractVideoFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding video by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find all videos uploaded by a specific user
     * 
     * @param userId User ID
     * @return List of videos
     */
    public List<Video> findByUserId(int userId) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos WHERE user_id = ? ORDER BY uploaded_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    videos.add(extractVideoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding videos by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    /**
     * Update video metadata (duration, resolution, format)
     * 
     * @param video Video object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateMetadata(Video video) {
        String sql = "UPDATE videos SET duration = ?, resolution = ?, format = ? WHERE video_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (video.getDuration() > 0) {
                stmt.setInt(1, video.getDuration());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setString(2, video.getResolution());
            stmt.setString(3, video.getFormat());
            stmt.setInt(4, video.getVideoId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Video metadata updated successfully: " + video.getVideoId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating video metadata: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Delete a video record
     * 
     * @param videoId Video ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int videoId) {
        String sql = "DELETE FROM videos WHERE video_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, videoId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Video deleted successfully: " + videoId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting video: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Get total count of videos for a user
     * 
     * @param userId User ID
     * @return Total number of videos
     */
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM videos WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting videos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Get total storage size used by a user
     * 
     * @param userId User ID
     * @return Total file size in bytes
     */
    public long getTotalStorageByUserId(int userId) {
        String sql = "SELECT SUM(file_size) FROM videos WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total storage: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0L;
    }

    /**
     * Find all videos (for admin purposes)
     * 
     * @return List of all videos
     */
    public List<Video> findAll() {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos ORDER BY uploaded_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                videos.add(extractVideoFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all videos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    /**
     * Find videos with pagination
     * 
     * @param userId User ID
     * @param limit Number of records per page
     * @param offset Starting position
     * @return List of videos
     */
    public List<Video> findByUserIdWithPagination(int userId, int limit, int offset) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos WHERE user_id = ? ORDER BY uploaded_at DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    videos.add(extractVideoFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding videos with pagination: " + e.getMessage());
            e.printStackTrace();
        }
        
        return videos;
    }

    /**
     * Extract Video object from ResultSet
     * 
     * @param rs ResultSet containing video data
     * @return Video object
     * @throws SQLException if error reading ResultSet
     */
    private Video extractVideoFromResultSet(ResultSet rs) throws SQLException {
        Video video = new Video();
        video.setVideoId(rs.getInt("video_id"));
        video.setUserId(rs.getInt("user_id"));
        video.setOriginalFilename(rs.getString("original_filename"));
        video.setFilePath(rs.getString("file_path"));
        video.setFileSize(rs.getLong("file_size"));
        
        // Handle nullable integer
        int duration = rs.getInt("duration");
        if (!rs.wasNull()) {
            video.setDuration(duration);
        }
        
        video.setResolution(rs.getString("resolution"));
        video.setFormat(rs.getString("format"));
        video.setUploadedAt(rs.getTimestamp("uploaded_at"));
        
        return video;
    }
}
