package com.example.workflowservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Context object that carries data and metadata throughout workflow execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowContext {
    
    private String executionId;
    private String workflowType;
    private String userId;
    private LocalDateTime createdAt;
    private Map<String, Object> inputData;
    private Map<String, Object> metadata;
    private Map<String, Object> variables;
    
    public WorkflowContext() {
        this.executionId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.inputData = new HashMap<>();
        this.metadata = new HashMap<>();
        this.variables = new HashMap<>();
    }
    
    public WorkflowContext(String workflowType, Map<String, Object> inputData) {
        this();
        this.workflowType = workflowType;
        this.inputData = inputData != null ? new HashMap<>(inputData) : new HashMap<>();
    }
    
    // Getters and Setters
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getWorkflowType() {
        return workflowType;
    }
    
    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Map<String, Object> getInputData() {
        return inputData;
    }
    
    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData != null ? inputData : new HashMap<>();
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables != null ? variables : new HashMap<>();
    }
    
    // Utility methods
    public Object getInputValue(String key) {
        return inputData.get(key);
    }
    
    public void setInputValue(String key, Object value) {
        inputData.put(key, value);
    }
    
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    public void setMetadataValue(String key, Object value) {
        metadata.put(key, value);
    }
}