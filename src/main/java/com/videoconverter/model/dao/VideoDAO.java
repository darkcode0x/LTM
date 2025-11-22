package com.videoconverter.model.dao;

import com.videoconverter.model.bean.Video;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VideoDAO - Database access for videos table
 */
public class VideoDAO {

    public boolean createVideo(Video video) {
        String sql = "INSERT INTO videos (user_id, filename, file_path, file_size) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, video.getUserId());
            stmt.setString(2, video.getFilename());
            stmt.setString(3, video.getFilePath());
            stmt.setLong(4, video.getFileSize());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    video.setVideoId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Video getVideoById(int videoId) {
        String sql = "SELECT * FROM videos WHERE video_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, videoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractVideo(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Video> getVideosByUserId(int userId) {
        List<Video> videos = new ArrayList<>();
        String sql = "SELECT * FROM videos WHERE user_id = ? ORDER BY uploaded_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                videos.add(extractVideo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return videos;
    }

    public int getTotalVideosByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM videos WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Video extractVideo(ResultSet rs) throws SQLException {
        return new Video(
            rs.getInt("video_id"),
            rs.getInt("user_id"),
            rs.getString("filename"),
            rs.getString("file_path"),
            rs.getLong("file_size"),
            rs.getTimestamp("uploaded_at")
        );
    }
}

