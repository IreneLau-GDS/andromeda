package com.example.workflowservice.service;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.workflow.Workflow;
import com.example.workflowservice.workflow.WorkflowExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Core service for managing and executing workflows.
 */
@Service
public class WorkflowEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineService.class);
    
    private final Map<String, Workflow> registeredWorkflows = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final WorkflowExecutionService executionService;
    
    @Autowired
    public WorkflowEngineService(WorkflowExecutionService executionService) {
        this.executionService = executionService;
    }
    
    /**
     * Registers a workflow implementation with the engine.
     */
    public void registerWorkflow(Workflow workflow) {
        String workflowType = workflow.getWorkflowType();
        logger.info("Registering workflow: {} (version: {})", workflowType, workflow.getVersion());
        registeredWorkflows.put(workflowType, workflow);
    }
    
    /**
     * Unregisters a workflow from the engine.
     */
    public void unregisterWorkflow(String workflowType) {
        logger.info("Unregistering workflow: {}", workflowType);
        registeredWorkflows.remove(workflowType);
    }
    
    /**
     * Executes a workflow synchronously.
     */
    public WorkflowResult executeWorkflow(String workflowType, WorkflowContext context) throws WorkflowExecutionException {
        Workflow workflow = getWorkflow(workflowType);
        context.setWorkflowType(workflowType);
        
        logger.info("Executing workflow: {} with execution ID: {}", workflowType, context.getExecutionId());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate context
            if (!workflow.validate(context)) {
                throw new WorkflowExecutionException("Workflow context validation failed", 
                    "VALIDATION_ERROR", workflowType, context.getExecutionId());
            }
            
            // Save execution start
            executionService.saveExecutionStart(context);
            
            // Execute workflow
            workflow.beforeExecution(context);
            WorkflowResult result = workflow.execute(context);
            workflow.afterExecution(context, result);
            
            // Set execution time
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);
            result.setExecutionId(context.getExecutionId());
            
            // Save execution result
            executionService.saveExecutionResult(result);
            
            logger.info("Workflow {} completed successfully in {}ms", workflowType, executionTime);
            return result;
            
        } catch (WorkflowExecutionException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            WorkflowResult errorResult = WorkflowResult.failed(context.getExecutionId(), e.getMessage(), e.getErrorCode());
            errorResult.setExecutionTimeMs(executionTime);
            errorResult.setErrorDetails(e.getCause() != null ? e.getCause().getMessage() : null);
            
            executionService.saveExecutionResult(errorResult);
            logger.error("Workflow {} failed after {}ms: {}", workflowType, executionTime, e.getMessage());
            throw e;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            WorkflowResult errorResult = WorkflowResult.failed(context.getExecutionId(), "Unexpected error during workflow execution");
            errorResult.setExecutionTimeMs(executionTime);
            errorResult.setErrorDetails(e.getMessage());
            
            executionService.saveExecutionResult(errorResult);
            logger.error("Unexpected error in workflow {}: {}", workflowType, e.getMessage(), e);
            throw new WorkflowExecutionException("Unexpected error during workflow execution", e, 
                "UNEXPECTED_ERROR", workflowType, context.getExecutionId());
        }
    }
    
    /**
     * Executes a workflow asynchronously.
     */
    public CompletableFuture<WorkflowResult> executeWorkflowAsync(String workflowType, WorkflowContext context) throws WorkflowExecutionException {
        Workflow workflow = getWorkflow(workflowType);
        
        if (!workflow.supportsAsyncExecution()) {
            logger.warn("Workflow {} does not support async execution, executing synchronously", workflowType);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWorkflow(workflowType, context);
            } catch (WorkflowExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    /**
     * Gets a registered workflow by type.
     */
    public Workflow getWorkflow(String workflowType) throws WorkflowExecutionException {
        Workflow workflow = registeredWorkflows.get(workflowType);
        if (workflow == null) {
            throw new WorkflowExecutionException("Workflow type not found: " + workflowType, 
                "WORKFLOW_NOT_FOUND", workflowType, null);
        }
        return workflow;
    }
    
    /**
     * Returns all registered workflow types.
     */
    public Map<String, String> getRegisteredWorkflows() {
        Map<String, String> workflows = new HashMap<>();
        registeredWorkflows.forEach((type, workflow) -> 
            workflows.put(type, workflow.getDescription()));
        return workflows;
    }
    
    /**
     * Gets workflow information.
     */
    public Map<String, Object> getWorkflowInfo(String workflowType) throws WorkflowExecutionException {
        Workflow workflow = getWorkflow(workflowType);
        Map<String, Object> info = new HashMap<>();
        info.put("type", workflow.getWorkflowType());
        info.put("version", workflow.getVersion());
        info.put("description", workflow.getDescription());
        info.put("supportsAsync", workflow.supportsAsyncExecution());
        info.put("estimatedExecutionTime", workflow.getEstimatedExecutionTime());
        return info;
    }
    
    /**
     * Validates a workflow context without executing it.
     */
    public boolean validateWorkflowContext(String workflowType, WorkflowContext context) throws WorkflowExecutionException {
        Workflow workflow = getWorkflow(workflowType);
        return workflow.validate(context);
    }
}