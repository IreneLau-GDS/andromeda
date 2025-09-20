package com.example.workflowservice.repository;

import com.example.workflowservice.model.WorkflowExecution;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing workflow execution data using Spring JDBC.
 */
@Repository
public class WorkflowExecutionRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WorkflowExecutionRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
    
    public WorkflowExecution save(WorkflowExecution execution) {
        if (execution.getId() == null) {
            return insert(execution);
        } else {
            return update(execution);
        }
    }
    
    private WorkflowExecution insert(WorkflowExecution execution) {
        String sql = """
            INSERT INTO workflow_execution (
                execution_id, workflow_type, user_id, status, input_data, 
                output_data, metadata, error_code, error_message, error_details,
                execution_time_ms, created_at, completed_at, updated_at
            ) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, execution.getExecutionId());
            ps.setString(2, execution.getWorkflowType());
            ps.setString(3, execution.getUserId());
            ps.setString(4, execution.getStatus().name());
            ps.setString(5, toJson(execution.getInputData()));
            ps.setString(6, toJson(execution.getOutputData()));
            ps.setString(7, toJson(execution.getMetadata()));
            ps.setString(8, execution.getErrorCode());
            ps.setString(9, execution.getErrorMessage());
            ps.setString(10, execution.getErrorDetails());
            ps.setObject(11, execution.getExecutionTimeMs());
            ps.setTimestamp(12, Timestamp.valueOf(execution.getCreatedAt()));
            ps.setTimestamp(13, execution.getCompletedAt() != null ? 
                Timestamp.valueOf(execution.getCompletedAt()) : null);
            ps.setTimestamp(14, Timestamp.valueOf(execution.getUpdatedAt()));
            return ps;
        }, keyHolder);
        
        execution.setId(keyHolder.getKey().longValue());
        return execution;
    }
    
    private WorkflowExecution update(WorkflowExecution execution) {
        execution.setUpdatedAt(LocalDateTime.now());
        
        String sql = """
            UPDATE workflow_execution SET 
                workflow_type = ?, user_id = ?, status = ?, input_data = ?::jsonb,
                output_data = ?::jsonb, metadata = ?::jsonb, error_code = ?,
                error_message = ?, error_details = ?, execution_time_ms = ?,
                completed_at = ?, updated_at = ?
            WHERE execution_id = ?
            """;
        
        jdbcTemplate.update(sql,
            execution.getWorkflowType(),
            execution.getUserId(),
            execution.getStatus().name(),
            toJson(execution.getInputData()),
            toJson(execution.getOutputData()),
            toJson(execution.getMetadata()),
            execution.getErrorCode(),
            execution.getErrorMessage(),
            execution.getErrorDetails(),
            execution.getExecutionTimeMs(),
            execution.getCompletedAt() != null ? 
                Timestamp.valueOf(execution.getCompletedAt()) : null,
            Timestamp.valueOf(execution.getUpdatedAt()),
            execution.getExecutionId()
        );
        
        return execution;
    }
    
    public Optional<WorkflowExecution> findByExecutionId(String executionId) {
        String sql = "SELECT * FROM workflow_execution WHERE execution_id = ?";
        
        try {
            WorkflowExecution execution = jdbcTemplate.queryForObject(sql, 
                new WorkflowExecutionRowMapper(), executionId);
            return Optional.ofNullable(execution);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public List<WorkflowExecution> findByWorkflowType(String workflowType) {
        String sql = "SELECT * FROM workflow_execution WHERE workflow_type = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new WorkflowExecutionRowMapper(), workflowType);
    }
    
    public List<WorkflowExecution> findByUserId(String userId) {
        String sql = "SELECT * FROM workflow_execution WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new WorkflowExecutionRowMapper(), userId);
    }
    
    public List<WorkflowExecution> findByStatus(WorkflowExecution.Status status) {
        String sql = "SELECT * FROM workflow_execution WHERE status = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new WorkflowExecutionRowMapper(), status.name());
    }
    
    public List<WorkflowExecution> findAll() {
        String sql = "SELECT * FROM workflow_execution ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new WorkflowExecutionRowMapper());
    }
    
    public void deleteByExecutionId(String executionId) {
        String sql = "DELETE FROM workflow_execution WHERE execution_id = ?";
        jdbcTemplate.update(sql, executionId);
    }
    
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    private Map<String, Object> fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to Map", e);
        }
    }
    
    private class WorkflowExecutionRowMapper implements RowMapper<WorkflowExecution> {
        @Override
        public WorkflowExecution mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkflowExecution execution = new WorkflowExecution();
            execution.setId(rs.getLong("id"));
            execution.setExecutionId(rs.getString("execution_id"));
            execution.setWorkflowType(rs.getString("workflow_type"));
            execution.setUserId(rs.getString("user_id"));
            execution.setStatus(WorkflowExecution.Status.valueOf(rs.getString("status")));
            execution.setInputData(fromJson(rs.getString("input_data")));
            execution.setOutputData(fromJson(rs.getString("output_data")));
            execution.setMetadata(fromJson(rs.getString("metadata")));
            execution.setErrorCode(rs.getString("error_code"));
            execution.setErrorMessage(rs.getString("error_message"));
            execution.setErrorDetails(rs.getString("error_details"));
            
            Long executionTimeMs = rs.getObject("execution_time_ms", Long.class);
            execution.setExecutionTimeMs(executionTimeMs);
            
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                execution.setCreatedAt(createdAt.toLocalDateTime());
            }
            
            Timestamp completedAt = rs.getTimestamp("completed_at");
            if (completedAt != null) {
                execution.setCompletedAt(completedAt.toLocalDateTime());
            }
            
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                execution.setUpdatedAt(updatedAt.toLocalDateTime());
            }
            
            return execution;
        }
    }
}