package com.example.workflowservice.workflow;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowResult;

/**
 * Core interface for all workflow implementations.
 * This interface defines the contract that all workflow subclasses must implement
 * to be used within the workflow engine service.
 */
public interface Workflow {
    
    /**
     * Executes the workflow with the provided context.
     * 
     * @param context The workflow execution context containing input data and metadata
     * @return WorkflowResult containing the execution result and status
     * @throws WorkflowExecutionException if the workflow execution fails
     */
    WorkflowResult execute(WorkflowContext context) throws WorkflowExecutionException;
    
    /**
     * Validates the workflow context before execution.
     * 
     * @param context The workflow context to validate
     * @return true if the context is valid, false otherwise
     */
    boolean validate(WorkflowContext context);
    
    /**
     * Returns the unique identifier for this workflow type.
     * 
     * @return The workflow type identifier
     */
    String getWorkflowType();
    
    /**
     * Returns the version of this workflow implementation.
     * 
     * @return The workflow version
     */
    String getVersion();
    
    /**
     * Returns a human-readable description of what this workflow does.
     * 
     * @return The workflow description
     */
    String getDescription();
    
    /**
     * Indicates whether this workflow can be executed asynchronously.
     * 
     * @return true if the workflow supports async execution, false otherwise
     */
    default boolean supportsAsyncExecution() {
        return false;
    }
    
    /**
     * Returns the estimated execution time in milliseconds.
     * This is used for scheduling and timeout purposes.
     * 
     * @return Estimated execution time in milliseconds, -1 if unknown
     */
    default long getEstimatedExecutionTime() {
        return -1;
    }
    
    /**
     * Called before workflow execution for any setup operations.
     * 
     * @param context The workflow context
     */
    default void beforeExecution(WorkflowContext context) {
        // Default implementation does nothing
    }
    
    /**
     * Called after workflow execution for cleanup operations.
     * 
     * @param context The workflow context
     * @param result The execution result
     */
    default void afterExecution(WorkflowContext context, WorkflowResult result) {
        // Default implementation does nothing
    }
}