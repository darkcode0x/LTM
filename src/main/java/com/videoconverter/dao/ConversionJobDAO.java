package com.videoconverter.dao;

import com.videoconverter.model.ConversionJob;
import com.videoconverter.model.ConversionJob.JobStatus;
import com.videoconverter.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ConversionJobDAO - Data Access Object for ConversionJob operations
 * Handles all database operations related to conversion_jobs table
 */
public class ConversionJobDAO {

    /**
     * Insert a new conversion job into database
     * 
     * @param job ConversionJob object to insert
     * @return Generated job ID, or -1 if failed
     */
    public int insert(ConversionJob job) {
        String sql = "INSERT INTO conversion_jobs (video_id, user_id, status, progress, priority, " +
                     "output_format, output_resolution, quality, video_bitrate, audio_bitrate, " +
                     "codec, frame_rate, start_time, end_time, estimated_time, retry_count, max_retries) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, job.getVideoId());
            stmt.setInt(2, job.getUserId());
            stmt.setString(3, job.getStatusString());
            stmt.setInt(4, job.getProgress());
            stmt.setInt(5, job.getPriority());
            stmt.setString(6, job.getOutputFormat());
            stmt.setString(7, job.getOutputResolution());
            stmt.setString(8, job.getQuality());
            stmt.setString(9, job.getVideoBitrate());
            stmt.setString(10, job.getAudioBitrate());
            stmt.setString(11, job.getCodec());
            stmt.setInt(12, job.getFrameRate());
            
            // Handle nullable fields
            if (job.getStartTime() != null) {
                stmt.setInt(13, job.getStartTime());
            } else {
                stmt.setNull(13, Types.INTEGER);
            }
            
            if (job.getEndTime() != null) {
                stmt.setInt(14, job.getEndTime());
            } else {
                stmt.setNull(14, Types.INTEGER);
            }
            
            if (job.getEstimatedTime() != null) {
                stmt.setInt(15, job.getEstimatedTime());
            } else {
                stmt.setNull(15, Types.INTEGER);
            }
            
            stmt.setInt(16, job.getRetryCount());
            stmt.setInt(17, job.getMaxRetries());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int jobId = generatedKeys.getInt(1);
                        job.setJobId(jobId);
                        System.out.println("Conversion job inserted successfully with ID: " + jobId);
                        return jobId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting conversion job: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }

    /**
     * Update an existing conversion job
     * 
     * @param job ConversionJob object with updated information
     * @return true if successful, false otherwise
     */
    public boolean update(ConversionJob job) {
        String sql = "UPDATE conversion_jobs SET status = ?, progress = ?, " +
                     "output_filename = ?, output_path = ?, output_size = ?, " +
                     "started_at = ?, completed_at = ?, error_message = ?, retry_count = ? " +
                     "WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, job.getStatusString());
            stmt.setInt(2, job.getProgress());
            stmt.setString(3, job.getOutputFilename());
            stmt.setString(4, job.getOutputPath());
            
            if (job.getOutputSize() != null) {
                stmt.setLong(5, job.getOutputSize());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            
            stmt.setTimestamp(6, job.getStartedAt());
            stmt.setTimestamp(7, job.getCompletedAt());
            stmt.setString(8, job.getErrorMessage());
            stmt.setInt(9, job.getRetryCount());
            stmt.setInt(10, job.getJobId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Conversion job updated successfully: " + job.getJobId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating conversion job: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Find conversion job by ID
     * 
     * @param jobId Job ID
     * @return ConversionJob object if found, null otherwise
     */
    public ConversionJob findById(int jobId) {
        String sql = "SELECT * FROM conversion_jobs WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jobId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractJobFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding conversion job by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * Find all conversion jobs for a specific user
     * 
     * @param userId User ID
     * @return List of conversion jobs
     */
    public List<ConversionJob> findByUserId(int userId) {
        List<ConversionJob> jobs = new ArrayList<>();
        String sql = "SELECT * FROM conversion_jobs WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(extractJobFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding conversion jobs by user ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jobs;
    }

    /**
     * Update job progress
     * 
     * @param jobId Job ID
     * @param progress Progress percentage (0-100)
     * @return true if successful, false otherwise
     */
    public boolean updateProgress(int jobId, int progress) {
        String sql = "UPDATE conversion_jobs SET progress = ? WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Math.max(0, Math.min(100, progress))); // Clamp 0-100
            stmt.setInt(2, jobId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating job progress: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Update job status
     * 
     * @param jobId Job ID
     * @param status New status
     * @return true if successful, false otherwise
     */
    public boolean updateStatus(int jobId, JobStatus status) {
        String sql = "UPDATE conversion_jobs SET status = ? WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, jobId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Job status updated to " + status + " for job ID: " + jobId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating job status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Mark job as started
     * 
     * @param jobId Job ID
     * @return true if successful, false otherwise
     */
    public boolean markAsStarted(int jobId) {
        String sql = "UPDATE conversion_jobs SET status = 'PROCESSING', " +
                     "started_at = CURRENT_TIMESTAMP WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jobId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error marking job as started: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Mark job as completed
     * 
     * @param jobId Job ID
     * @param outputFilename Output filename
     * @param outputPath Output file path
     * @param outputSize Output file size
     * @return true if successful, false otherwise
     */
    public boolean markAsCompleted(int jobId, String outputFilename, String outputPath, long outputSize) {
        String sql = "UPDATE conversion_jobs SET status = 'COMPLETED', progress = 100, " +
                     "completed_at = CURRENT_TIMESTAMP, output_filename = ?, " +
                     "output_path = ?, output_size = ? WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, outputFilename);
            stmt.setString(2, outputPath);
            stmt.setLong(3, outputSize);
            stmt.setInt(4, jobId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Job marked as completed: " + jobId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error marking job as completed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Mark job as failed
     * 
     * @param jobId Job ID
     * @param errorMessage Error message
     * @return true if successful, false otherwise
     */
    public boolean markAsFailed(int jobId, String errorMessage) {
        String sql = "UPDATE conversion_jobs SET status = 'FAILED', " +
                     "error_message = ?, retry_count = retry_count + 1 WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, errorMessage);
            stmt.setInt(2, jobId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Job marked as failed: " + jobId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error marking job as failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Find pending jobs (for worker threads to process)
     * 
     * @param limit Maximum number of jobs to retrieve
     * @return List of pending jobs ordered by priority
     */
    public List<ConversionJob> findPendingJobs(int limit) {
        List<ConversionJob> jobs = new ArrayList<>();
        String sql = "SELECT * FROM conversion_jobs WHERE status = 'PENDING' " +
                     "ORDER BY priority DESC, created_at ASC LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(extractJobFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding pending jobs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jobs;
    }

    /**
     * Find jobs by status
     * 
     * @param status Job status
     * @return List of jobs with the specified status
     */
    public List<ConversionJob> findByStatus(JobStatus status) {
        List<ConversionJob> jobs = new ArrayList<>();
        String sql = "SELECT * FROM conversion_jobs WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(extractJobFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding jobs by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return jobs;
    }

    /**
     * Count jobs by user ID and status
     * 
     * @param userId User ID
     * @param status Job status
     * @return Number of jobs
     */
    public int countByUserIdAndStatus(int userId, JobStatus status) {
        String sql = "SELECT COUNT(*) FROM conversion_jobs WHERE user_id = ? AND status = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting jobs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Delete a conversion job
     * 
     * @param jobId Job ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int jobId) {
        String sql = "DELETE FROM conversion_jobs WHERE job_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, jobId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("Conversion job deleted successfully: " + jobId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting conversion job: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    /**
     * Extract ConversionJob object from ResultSet
     * 
     * @param rs ResultSet containing job data
     * @return ConversionJob object
     * @throws SQLException if error reading ResultSet
     */
    private ConversionJob extractJobFromResultSet(ResultSet rs) throws SQLException {
        ConversionJob job = new ConversionJob();
        job.setJobId(rs.getInt("job_id"));
        job.setVideoId(rs.getInt("video_id"));
        job.setUserId(rs.getInt("user_id"));
        job.setStatusFromString(rs.getString("status"));
        job.setProgress(rs.getInt("progress"));
        job.setPriority(rs.getInt("priority"));
        job.setOutputFormat(rs.getString("output_format"));
        job.setOutputResolution(rs.getString("output_resolution"));
        job.setQuality(rs.getString("quality"));
        job.setVideoBitrate(rs.getString("video_bitrate"));
        job.setAudioBitrate(rs.getString("audio_bitrate"));
        job.setCodec(rs.getString("codec"));
        job.setFrameRate(rs.getInt("frame_rate"));
        
        // Handle nullable integers
        int startTime = rs.getInt("start_time");
        if (!rs.wasNull()) {
            job.setStartTime(startTime);
        }
        
        int endTime = rs.getInt("end_time");
        if (!rs.wasNull()) {
            job.setEndTime(endTime);
        }
        
        int estimatedTime = rs.getInt("estimated_time");
        if (!rs.wasNull()) {
            job.setEstimatedTime(estimatedTime);
        }
        
        job.setOutputFilename(rs.getString("output_filename"));
        job.setOutputPath(rs.getString("output_path"));
        
        long outputSize = rs.getLong("output_size");
        if (!rs.wasNull()) {
            job.setOutputSize(outputSize);
        }
        
        job.setCreatedAt(rs.getTimestamp("created_at"));
        job.setStartedAt(rs.getTimestamp("started_at"));
        job.setCompletedAt(rs.getTimestamp("completed_at"));
        job.setErrorMessage(rs.getString("error_message"));
        job.setRetryCount(rs.getInt("retry_count"));
        job.setMaxRetries(rs.getInt("max_retries"));
        
        return job;
    }
}
