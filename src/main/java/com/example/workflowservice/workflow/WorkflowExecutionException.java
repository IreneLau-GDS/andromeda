package com.example.workflowservice.workflow;

/**
 * Exception thrown when workflow execution fails.
 */
public class WorkflowExecutionException extends Exception {
    
    private final String errorCode;
    private final String workflowType;
    private final String executionId;
    
    public WorkflowExecutionException(String message) {
        super(message);
        this.errorCode = null;
        this.workflowType = null;
        this.executionId = null;
    }
    
    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.workflowType = null;
        this.executionId = null;
    }
    
    public WorkflowExecutionException(String message, String errorCode, String workflowType, String executionId) {
        super(message);
        this.errorCode = errorCode;
        this.workflowType = workflowType;
        this.executionId = executionId;
    }
    
    public WorkflowExecutionException(String message, Throwable cause, String errorCode, String workflowType, String executionId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.workflowType = workflowType;
        this.executionId = executionId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getWorkflowType() {
        return workflowType;
    }
    
    public String getExecutionId() {
        return executionId;
    }
}