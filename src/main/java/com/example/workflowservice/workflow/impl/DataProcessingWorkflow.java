package com.example.workflowservice.workflow.impl;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.workflow.Workflow;
import com.example.workflowservice.workflow.WorkflowExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample workflow implementation for data processing operations.
 */
@Component
public class DataProcessingWorkflow implements Workflow {
    
    private static final Logger logger = LoggerFactory.getLogger(DataProcessingWorkflow.class);
    
    @Override
    public WorkflowResult execute(WorkflowContext context) throws WorkflowExecutionException {
        logger.info("Starting data processing workflow for execution: {}", context.getExecutionId());
        
        try {
            // Simulate data processing steps
            List<Map<String, Object>> inputData = getInputDataList(context);
            
            // Step 1: Validate data
            validateInputData(inputData);
            
            // Step 2: Transform data
            List<Map<String, Object>> transformedData = transformData(inputData);
            
            // Step 3: Process data
            Map<String, Object> processedResult = processData(transformedData);
            
            // Create result
            WorkflowResult result = WorkflowResult.success(context.getExecutionId());
            result.addOutputData("processedRecords", processedResult.get("recordCount"));
            result.addOutputData("totalValue", processedResult.get("totalValue"));
            result.addOutputData("averageValue", processedResult.get("averageValue"));
            result.setMessage("Data processing completed successfully");
            
            logger.info("Data processing workflow completed for execution: {}", context.getExecutionId());
            return result;
            
        } catch (Exception e) {
            logger.error("Data processing workflow failed for execution: {}", context.getExecutionId(), e);
            throw new WorkflowExecutionException("Data processing failed: " + e.getMessage(), e,
                "DATA_PROCESSING_ERROR", getWorkflowType(), context.getExecutionId());
        }
    }
    
    @Override
    public boolean validate(WorkflowContext context) {
        // Check if required input data is present
        Object data = context.getInputValue("data");
        if (data == null) {
            logger.warn("Validation failed: 'data' field is required");
            return false;
        }
        
        if (!(data instanceof List)) {
            logger.warn("Validation failed: 'data' field must be a list");
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getWorkflowType() {
        return "DATA_PROCESSING";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getDescription() {
        return "Processes input data by validating, transforming, and aggregating records";
    }
    
    @Override
    public boolean supportsAsyncExecution() {
        return true;
    }
    
    @Override
    public long getEstimatedExecutionTime() {
        return 5000; // 5 seconds
    }
    
    @Override
    public void beforeExecution(WorkflowContext context) {
        logger.debug("Preparing data processing workflow for execution: {}", context.getExecutionId());
        context.setVariable("startTime", System.currentTimeMillis());
    }
    
    @Override
    public void afterExecution(WorkflowContext context, WorkflowResult result) {
        Long startTime = (Long) context.getVariable("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Data processing workflow execution took: {}ms", duration);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getInputDataList(WorkflowContext context) {
        return (List<Map<String, Object>>) context.getInputValue("data");
    }
    
    private void validateInputData(List<Map<String, Object>> data) throws WorkflowExecutionException {
        if (data.isEmpty()) {
            throw new WorkflowExecutionException("Input data list cannot be empty");
        }
        
        for (Map<String, Object> record : data) {
            if (!record.containsKey("value")) {
                throw new WorkflowExecutionException("Each record must contain a 'value' field");
            }
        }
    }
    
    private List<Map<String, Object>> transformData(List<Map<String, Object>> data) {
        return data.stream()
            .map(record -> {
                Map<String, Object> transformed = new HashMap<>(record);
                Object value = record.get("value");
                if (value instanceof Number) {
                    transformed.put("normalizedValue", ((Number) value).doubleValue());
                }
                return transformed;
            })
            .toList();
    }
    
    private Map<String, Object> processData(List<Map<String, Object>> data) {
        Map<String, Object> result = new HashMap<>();
        
        int recordCount = data.size();
        double totalValue = data.stream()
            .mapToDouble(record -> {
                Object value = record.get("normalizedValue");
                return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
            })
            .sum();
        
        double averageValue = recordCount > 0 ? totalValue / recordCount : 0.0;
        
        result.put("recordCount", recordCount);
        result.put("totalValue", totalValue);
        result.put("averageValue", averageValue);
        
        return result;
    }
}