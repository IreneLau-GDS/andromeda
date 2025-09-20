package com.example.workflowservice.workflow.impl;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.workflow.WorkflowExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataProcessingWorkflowTest {
    
    private DataProcessingWorkflow workflow;
    
    @BeforeEach
    void setUp() {
        workflow = new DataProcessingWorkflow();
    }
    
    @Test
    void shouldExecuteSuccessfully() throws WorkflowExecutionException {
        // Given
        Map<String, Object> inputData = Map.of("data", List.of(
            Map.of("value", 10),
            Map.of("value", 20),
            Map.of("value", 30)
        ));
        WorkflowContext context = new WorkflowContext("DATA_PROCESSING", inputData);
        context.setExecutionId("test-exec-1");
        context.setUserId("test-user");
        
        // When
        WorkflowResult result = workflow.execute(context);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(WorkflowResult.Status.SUCCESS);
        assertThat(result.getOutputData()).containsKey("processedRecords");
        assertThat(result.getOutputData()).containsKey("totalValue");
        assertThat(result.getOutputData()).containsKey("averageValue");
        
        assertThat(result.getOutputValue("processedRecords")).isEqualTo(3);
        assertThat(result.getOutputValue("totalValue")).isEqualTo(60.0);
        assertThat(result.getOutputValue("averageValue")).isEqualTo(20.0);
    }
    
    @Test
    void shouldValidateInputData() {
        // Given - valid context
        Map<String, Object> validInputData = Map.of("data", List.of(Map.of("value", 10)));
        WorkflowContext validContext = new WorkflowContext("DATA_PROCESSING", validInputData);
        validContext.setExecutionId("test-exec-1");
        validContext.setUserId("test-user");
        
        // When & Then
        assertThat(workflow.validate(validContext)).isTrue();
        
        // Given - invalid context (no data)
        WorkflowContext invalidContext1 = new WorkflowContext("DATA_PROCESSING", Map.of());
        invalidContext1.setExecutionId("test-exec-2");
        invalidContext1.setUserId("test-user");
        
        // When & Then
        assertThat(workflow.validate(invalidContext1)).isFalse();
        
        // Given - invalid context (data is not a list)
        Map<String, Object> invalidInputData = Map.of("data", "not a list");
        WorkflowContext invalidContext2 = new WorkflowContext("DATA_PROCESSING", invalidInputData);
        invalidContext2.setExecutionId("test-exec-3");
        invalidContext2.setUserId("test-user");
        
        // When & Then
        assertThat(workflow.validate(invalidContext2)).isFalse();
    }
    
    @Test
    void shouldThrowExceptionForEmptyData() {
        // Given
        Map<String, Object> inputData = Map.of("data", List.of());
        WorkflowContext context = new WorkflowContext("DATA_PROCESSING", inputData);
        context.setExecutionId("test-exec-1");
        context.setUserId("test-user");
        
        // When & Then
        assertThatThrownBy(() -> workflow.execute(context))
            .isInstanceOf(WorkflowExecutionException.class)
            .hasMessageContaining("Input data list cannot be empty");
    }
    
    @Test
    void shouldThrowExceptionForMissingValueField() {
        // Given
        Map<String, Object> inputData = Map.of("data", List.of(
            Map.of("value", 10),
            Map.of("name", "test") // Missing value field
        ));
        WorkflowContext context = new WorkflowContext("DATA_PROCESSING", inputData);
        context.setExecutionId("test-exec-1");
        context.setUserId("test-user");
        
        // When & Then
        assertThatThrownBy(() -> workflow.execute(context))
            .isInstanceOf(WorkflowExecutionException.class)
            .hasMessageContaining("Each record must contain a 'value' field");
    }
    
    @Test
    void shouldReturnCorrectWorkflowInfo() {
        assertThat(workflow.getWorkflowType()).isEqualTo("DATA_PROCESSING");
        assertThat(workflow.getVersion()).isEqualTo("1.0.0");
        assertThat(workflow.getDescription()).contains("Processes input data");
        assertThat(workflow.supportsAsyncExecution()).isTrue();
        assertThat(workflow.getEstimatedExecutionTime()).isEqualTo(5000);
    }
    
    @Test
    void shouldExecuteLifecycleMethods() throws WorkflowExecutionException {
        // Given
        Map<String, Object> inputData = Map.of("data", List.of(Map.of("value", 10)));
        WorkflowContext context = new WorkflowContext("DATA_PROCESSING", inputData);
        context.setExecutionId("test-exec-1");
        context.setUserId("test-user");
        
        // When
        workflow.beforeExecution(context);
        WorkflowResult result = workflow.execute(context);
        workflow.afterExecution(context, result);
        
        // Then
        assertThat(context.getVariable("startTime")).isNotNull();
        assertThat(result.getStatus()).isEqualTo(WorkflowResult.Status.SUCCESS);
    }
}