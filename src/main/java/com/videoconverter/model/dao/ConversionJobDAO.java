package com.videoconverter.model.dao;

import com.videoconverter.model.bean.ConversionJob;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConversionJobDAO - Truy cập database cho bảng conversion_jobs
 */
public class ConversionJobDAO {

    /**
     * Tạo job mới với status PENDING
     */
    public boolean createJob(ConversionJob job) {
        String sql = "INSERT INTO conversion_jobs (video_id, user_id, output_format, status, progress) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, job.getVideoId());
            stmt.setInt(2, job.getUserId());
            stmt.setString(3, job.getOutputFormat());
            stmt.setString(4, job.getStatus());
            stmt.setInt(5, job.getProgress());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    job.setJobId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[JobDAO] Create failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update status và progress của job
     */
    public boolean updateJobStatus(int jobId, String status, int progress) {
        String sql = "UPDATE conversion_jobs SET status = ?, progress = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, progress);
            stmt.setInt(3, jobId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDAO] Update status failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mark job là COMPLETED
     */
    public boolean completeJob(int jobId, String outputPath) {
        String sql = "UPDATE conversion_jobs SET status = 'COMPLETED', progress = 100, output_path = ?, completed_at = NOW() WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, outputPath);
            stmt.setInt(2, jobId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDAO] Complete failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mark job là FAILED
     */
    public boolean failJob(int jobId, String errorMessage) {
        String sql = "UPDATE conversion_jobs SET status = 'FAILED', error_message = ?, completed_at = NOW() WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, errorMessage);
            stmt.setInt(2, jobId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDAO] Fail job failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy job theo ID
     */
    public ConversionJob getJobById(int jobId) {
        String sql = "SELECT j.*, v.filename as video_filename FROM conversion_jobs j " +
                     "INNER JOIN videos v ON j.video_id = v.video_id WHERE j.job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractJob(rs);
            }
        } catch (SQLException e) {
            System.err.println("[JobDAO] Get by ID failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy tất cả jobs của user
     */
    public List<ConversionJob> getJobsByUserId(int userId) {
        List<ConversionJob> jobs = new ArrayList<>();
        String sql = "SELECT j.*, v.filename as video_filename FROM conversion_jobs j " +
                     "INNER JOIN videos v ON j.video_id = v.video_id " +
                     "WHERE j.user_id = ? ORDER BY j.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                jobs.add(extractJob(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JobDAO] Get by user failed: " + e.getMessage());
        }
        return jobs;
    }

    /**
     * Lấy tất cả pending jobs (dùng khi restart server)
     */
    public List<ConversionJob> getPendingJobs() {
        List<ConversionJob> jobs = new ArrayList<>();
        String sql = "SELECT j.*, v.filename as video_filename FROM conversion_jobs j " +
                     "INNER JOIN videos v ON j.video_id = v.video_id " +
                     "WHERE j.status = 'PENDING' ORDER BY j.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                jobs.add(extractJob(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JobDAO] Get pending failed: " + e.getMessage());
        }
        return jobs;
    }

    /**
     * Xóa job
     */
    public boolean deleteJob(int jobId) {
        String sql = "DELETE FROM conversion_jobs WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDAO] Delete failed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thống kê conversions (dùng cho admin dashboard)
     */
    public Map<Integer, Integer> getConversionCountByUser() {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT user_id, COUNT(*) as count FROM conversion_jobs WHERE status = 'COMPLETED' GROUP BY user_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                counts.put(rs.getInt("user_id"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            // Lỗi get conversion count
        }
        return counts;
    }

    /**
     * Helper: Extract job từ ResultSet
     */
    private ConversionJob extractJob(ResultSet rs) throws SQLException {
        ConversionJob job = new ConversionJob(
            rs.getInt("job_id"),
            rs.getInt("video_id"),
            rs.getInt("user_id"),
            rs.getString("output_format"),
            rs.getString("status"),
            rs.getInt("progress"),
            rs.getString("output_path"),
            rs.getString("error_message"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("completed_at")
        );
        job.setVideoFilename(rs.getString("video_filename"));
        return job;
    }
}

