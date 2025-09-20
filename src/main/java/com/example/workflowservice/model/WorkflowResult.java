package com.example.workflowservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Result object returned after workflow execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowResult {
    
    public enum Status {
        SUCCESS,
        FAILED,
        PARTIAL_SUCCESS,
        CANCELLED
    }
    
    private String executionId;
    private Status status;
    private String message;
    private Map<String, Object> outputData;
    private LocalDateTime completedAt;
    private long executionTimeMs;
    private String errorCode;
    private String errorDetails;
    
    public WorkflowResult() {
        this.outputData = new HashMap<>();
        this.completedAt = LocalDateTime.now();
    }
    
    public WorkflowResult(String executionId, Status status) {
        this();
        this.executionId = executionId;
        this.status = status;
    }
    
    public WorkflowResult(String executionId, Status status, String message) {
        this(executionId, status);
        this.message = message;
    }
    
    // Static factory methods
    public static WorkflowResult success(String executionId) {
        return new WorkflowResult(executionId, Status.SUCCESS, "Workflow executed successfully");
    }
    
    public static WorkflowResult success(String executionId, Map<String, Object> outputData) {
        WorkflowResult result = success(executionId);
        result.setOutputData(outputData);
        return result;
    }
    
    public static WorkflowResult failed(String executionId, String message) {
        return new WorkflowResult(executionId, Status.FAILED, message);
    }
    
    public static WorkflowResult failed(String executionId, String message, String errorCode) {
        WorkflowResult result = failed(executionId, message);
        result.setErrorCode(errorCode);
        return result;
    }
    
    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getOutputData() {
        return outputData;
    }
    
    public void setOutputData(Map<String, Object> outputData) {
        this.outputData = outputData != null ? outputData : new HashMap<>();
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    // Utility methods
    public boolean isSuccessful() {
        return status == Status.SUCCESS || status == Status.PARTIAL_SUCCESS;
    }
    
    public void addOutputData(String key, Object value) {
        outputData.put(key, value);
    }
    
    public Object getOutputValue(String key) {
        return outputData.get(key);
    }
}