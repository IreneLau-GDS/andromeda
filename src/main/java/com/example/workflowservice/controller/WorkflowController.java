package com.example.workflowservice.controller;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowExecution;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.service.WorkflowEngineService;
import com.example.workflowservice.service.WorkflowExecutionService;
import com.example.workflowservice.workflow.WorkflowExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for workflow operations.
 */
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    
    private final WorkflowEngineService workflowEngineService;
    private final WorkflowExecutionService workflowExecutionService;
    
    @Autowired
    public WorkflowController(WorkflowEngineService workflowEngineService, 
                             WorkflowExecutionService workflowExecutionService) {
        this.workflowEngineService = workflowEngineService;
        this.workflowExecutionService = workflowExecutionService;
    }
    
    /**
     * Execute a workflow synchronously.
     */
    @PostMapping("/{workflowType}/execute")
    public ResponseEntity<?> executeWorkflow(
            @PathVariable String workflowType,
            @RequestBody WorkflowContext context) {
        
        try {
            logger.info("Executing workflow: {} with execution ID: {}", workflowType, context.getExecutionId());
            WorkflowResult result = workflowEngineService.executeWorkflow(workflowType, context);
            return ResponseEntity.ok(result);
            
        } catch (WorkflowExecutionException e) {
            logger.error("Workflow execution failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
                
        } catch (Exception e) {
            logger.error("Unexpected error during workflow execution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
    }
    
    /**
     * Execute a workflow asynchronously.
     */
    @PostMapping("/{workflowType}/execute-async")
    public ResponseEntity<?> executeWorkflowAsync(
            @PathVariable String workflowType,
            @RequestBody WorkflowContext context) {
        
        try {
            logger.info("Executing workflow asynchronously: {} with execution ID: {}", 
                workflowType, context.getExecutionId());
            
            CompletableFuture<WorkflowResult> future = workflowEngineService.executeWorkflowAsync(workflowType, context);
            
            return ResponseEntity.accepted()
                .body(Map.of(
                    "executionId", context.getExecutionId(),
                    "status", "ACCEPTED",
                    "message", "Workflow execution started asynchronously"
                ));
                
        } catch (Exception e) {
            logger.error("Failed to start async workflow execution", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to start async execution", "message", e.getMessage()));
        }
    }
    
    /**
     * Validate a workflow context without executing it.
     */
    @PostMapping("/{workflowType}/validate")
    public ResponseEntity<?> validateWorkflow(
            @PathVariable String workflowType,
            @RequestBody WorkflowContext context) {
        
        try {
            boolean isValid = workflowEngineService.validateWorkflowContext(workflowType, context);
            return ResponseEntity.ok(Map.of("valid", isValid));
            
        } catch (WorkflowExecutionException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage(), "errorCode", e.getErrorCode()));
                
        } catch (Exception e) {
            logger.error("Error during workflow validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Validation error", "message", e.getMessage()));
        }
    }
    
    /**
     * Get information about a specific workflow type.
     */
    @GetMapping("/{workflowType}/info")
    public ResponseEntity<?> getWorkflowInfo(@PathVariable String workflowType) {
        try {
            Map<String, Object> info = workflowEngineService.getWorkflowInfo(workflowType);
            return ResponseEntity.ok(info);
            
        } catch (WorkflowExecutionException e) {
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error getting workflow info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get workflow info", "message", e.getMessage()));
        }
    }
    
    /**
     * Get all registered workflow types.
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, String>> getRegisteredWorkflows() {
        Map<String, String> workflows = workflowEngineService.getRegisteredWorkflows();
        return ResponseEntity.ok(workflows);
    }
    
    /**
     * Get workflow execution by execution ID.
     */
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<?> getWorkflowExecution(@PathVariable String executionId) {
        Optional<WorkflowExecution> execution = workflowExecutionService.findByExecutionId(executionId);
        
        if (execution.isPresent()) {
            return ResponseEntity.ok(execution.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all workflow executions for a specific workflow type.
     */
    @GetMapping("/executions")
    public ResponseEntity<List<WorkflowExecution>> getWorkflowExecutions(
            @RequestParam(required = false) String workflowType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status) {
        
        List<WorkflowExecution> executions;
        
        if (workflowType != null) {
            executions = workflowExecutionService.findByWorkflowType(workflowType);
        } else if (userId != null) {
            executions = workflowExecutionService.findByUserId(userId);
        } else if (status != null) {
            try {
                WorkflowExecution.Status statusEnum = WorkflowExecution.Status.valueOf(status.toUpperCase());
                executions = workflowExecutionService.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            executions = workflowExecutionService.findAll();
        }
        
        return ResponseEntity.ok(executions);
    }
    
    /**
     * Delete a workflow execution.
     */
    @DeleteMapping("/executions/{executionId}")
    public ResponseEntity<Void> deleteWorkflowExecution(@PathVariable String executionId) {
        Optional<WorkflowExecution> execution = workflowExecutionService.findByExecutionId(executionId);
        
        if (execution.isPresent()) {
            workflowExecutionService.deleteByExecutionId(executionId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}