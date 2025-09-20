package com.example.workflowservice.service;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowExecution;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.repository.WorkflowExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing workflow execution persistence.
 */
@Service
public class WorkflowExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionService.class);
    
    private final WorkflowExecutionRepository repository;
    
    @Autowired
    public WorkflowExecutionService(WorkflowExecutionRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Saves the start of a workflow execution.
     */
    public WorkflowExecution saveExecutionStart(WorkflowContext context) {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId(context.getExecutionId());
        execution.setWorkflowType(context.getWorkflowType());
        execution.setUserId(context.getUserId());
        execution.setStatus(WorkflowExecution.Status.RUNNING);
        execution.setInputData(context.getInputData());
        execution.setMetadata(context.getMetadata());
        execution.setCreatedAt(context.getCreatedAt());
        
        WorkflowExecution saved = repository.save(execution);
        logger.debug("Saved workflow execution start: {}", saved.getExecutionId());
        return saved;
    }
    
    /**
     * Saves the result of a workflow execution.
     */
    public WorkflowExecution saveExecutionResult(WorkflowResult result) {
        Optional<WorkflowExecution> existingOpt = repository.findByExecutionId(result.getExecutionId());
        
        WorkflowExecution execution;
        if (existingOpt.isPresent()) {
            execution = existingOpt.get();
        } else {
            execution = new WorkflowExecution();
            execution.setExecutionId(result.getExecutionId());
            execution.setCreatedAt(LocalDateTime.now());
        }
        
        // Map WorkflowResult.Status to WorkflowExecution.Status
        execution.setStatus(mapResultStatusToExecutionStatus(result.getStatus()));
        execution.setOutputData(result.getOutputData());
        execution.setErrorCode(result.getErrorCode());
        execution.setErrorMessage(result.getMessage());
        execution.setErrorDetails(result.getErrorDetails());
        execution.setExecutionTimeMs(result.getExecutionTimeMs());
        execution.setCompletedAt(result.getCompletedAt());
        execution.setUpdatedAt(LocalDateTime.now());
        
        WorkflowExecution saved = repository.save(execution);
        logger.debug("Saved workflow execution result: {} with status: {}", 
            saved.getExecutionId(), saved.getStatus());
        return saved;
    }
    
    /**
     * Finds a workflow execution by execution ID.
     */
    public Optional<WorkflowExecution> findByExecutionId(String executionId) {
        return repository.findByExecutionId(executionId);
    }
    
    /**
     * Finds all workflow executions for a specific workflow type.
     */
    public List<WorkflowExecution> findByWorkflowType(String workflowType) {
        return repository.findByWorkflowType(workflowType);
    }
    
    /**
     * Finds all workflow executions for a specific user.
     */
    public List<WorkflowExecution> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }
    
    /**
     * Finds all workflow executions with a specific status.
     */
    public List<WorkflowExecution> findByStatus(WorkflowExecution.Status status) {
        return repository.findByStatus(status);
    }
    
    /**
     * Finds all workflow executions.
     */
    public List<WorkflowExecution> findAll() {
        return repository.findAll();
    }
    
    /**
     * Updates the status of a workflow execution.
     */
    public void updateStatus(String executionId, WorkflowExecution.Status status) {
        Optional<WorkflowExecution> executionOpt = repository.findByExecutionId(executionId);
        if (executionOpt.isPresent()) {
            WorkflowExecution execution = executionOpt.get();
            execution.setStatus(status);
            execution.setUpdatedAt(LocalDateTime.now());
            repository.save(execution);
            logger.debug("Updated workflow execution {} status to: {}", executionId, status);
        } else {
            logger.warn("Workflow execution not found for ID: {}", executionId);
        }
    }
    
    /**
     * Deletes a workflow execution by execution ID.
     */
    public void deleteByExecutionId(String executionId) {
        repository.deleteByExecutionId(executionId);
        logger.debug("Deleted workflow execution: {}", executionId);
    }
    
    private WorkflowExecution.Status mapResultStatusToExecutionStatus(WorkflowResult.Status resultStatus) {
        return switch (resultStatus) {
            case SUCCESS -> WorkflowExecution.Status.SUCCESS;
            case FAILED -> WorkflowExecution.Status.FAILED;
            case PARTIAL_SUCCESS -> WorkflowExecution.Status.PARTIAL_SUCCESS;
            case CANCELLED -> WorkflowExecution.Status.CANCELLED;
        };
    }
}