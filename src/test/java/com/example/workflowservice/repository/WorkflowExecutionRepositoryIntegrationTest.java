package com.example.workflowservice.repository;

import com.example.workflowservice.model.WorkflowExecution;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class WorkflowExecutionRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("workflow_test")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private WorkflowExecutionRepository repository;
    
    @BeforeEach
    void setUp() {
        repository = new WorkflowExecutionRepository(jdbcTemplate, objectMapper);
        
        // Clean up data before each test
        jdbcTemplate.execute("DELETE FROM workflow_execution");
    }
    
    @Test
    void shouldSaveAndRetrieveWorkflowExecution() {
        // Given
        WorkflowExecution execution = createSampleExecution();
        
        // When
        WorkflowExecution saved = repository.save(execution);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getExecutionId()).isEqualTo(execution.getExecutionId());
        assertThat(saved.getWorkflowType()).isEqualTo(execution.getWorkflowType());
        assertThat(saved.getStatus()).isEqualTo(execution.getStatus());
    }
    
    @Test
    void shouldFindByExecutionId() {
        // Given
        WorkflowExecution execution = createSampleExecution();
        repository.save(execution);
        
        // When
        Optional<WorkflowExecution> found = repository.findByExecutionId(execution.getExecutionId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getExecutionId()).isEqualTo(execution.getExecutionId());
        assertThat(found.get().getWorkflowType()).isEqualTo(execution.getWorkflowType());
    }
    
    @Test
    void shouldReturnEmptyWhenExecutionIdNotFound() {
        // When
        Optional<WorkflowExecution> found = repository.findByExecutionId("non-existent-id");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    void shouldFindByWorkflowType() {
        // Given
        WorkflowExecution execution1 = createSampleExecution();
        execution1.setWorkflowType("TYPE_A");
        
        WorkflowExecution execution2 = createSampleExecution();
        execution2.setExecutionId("exec-2");
        execution2.setWorkflowType("TYPE_B");
        
        WorkflowExecution execution3 = createSampleExecution();
        execution3.setExecutionId("exec-3");
        execution3.setWorkflowType("TYPE_A");
        
        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);
        
        // When
        List<WorkflowExecution> typeAExecutions = repository.findByWorkflowType("TYPE_A");
        
        // Then
        assertThat(typeAExecutions).hasSize(2);
        assertThat(typeAExecutions)
            .extracting(WorkflowExecution::getWorkflowType)
            .containsOnly("TYPE_A");
    }
    
    @Test
    void shouldFindByUserId() {
        // Given
        WorkflowExecution execution1 = createSampleExecution();
        execution1.setUserId("user1");
        
        WorkflowExecution execution2 = createSampleExecution();
        execution2.setExecutionId("exec-2");
        execution2.setUserId("user2");
        
        WorkflowExecution execution3 = createSampleExecution();
        execution3.setExecutionId("exec-3");
        execution3.setUserId("user1");
        
        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);
        
        // When
        List<WorkflowExecution> user1Executions = repository.findByUserId("user1");
        
        // Then
        assertThat(user1Executions).hasSize(2);
        assertThat(user1Executions)
            .extracting(WorkflowExecution::getUserId)
            .containsOnly("user1");
    }
    
    @Test
    void shouldFindByStatus() {
        // Given
        WorkflowExecution execution1 = createSampleExecution();
        execution1.setStatus(WorkflowExecution.Status.SUCCESS);
        
        WorkflowExecution execution2 = createSampleExecution();
        execution2.setExecutionId("exec-2");
        execution2.setStatus(WorkflowExecution.Status.FAILED);
        
        WorkflowExecution execution3 = createSampleExecution();
        execution3.setExecutionId("exec-3");
        execution3.setStatus(WorkflowExecution.Status.SUCCESS);
        
        repository.save(execution1);
        repository.save(execution2);
        repository.save(execution3);
        
        // When
        List<WorkflowExecution> successExecutions = repository.findByStatus(WorkflowExecution.Status.SUCCESS);
        
        // Then
        assertThat(successExecutions).hasSize(2);
        assertThat(successExecutions)
            .extracting(WorkflowExecution::getStatus)
            .containsOnly(WorkflowExecution.Status.SUCCESS);
    }
    
    @Test
    void shouldUpdateExistingExecution() {
        // Given
        WorkflowExecution execution = createSampleExecution();
        WorkflowExecution saved = repository.save(execution);
        
        // When
        saved.setStatus(WorkflowExecution.Status.SUCCESS);
        saved.setErrorMessage("Updated message");
        saved.setExecutionTimeMs(5000L);
        saved.setCompletedAt(LocalDateTime.now());
        
        WorkflowExecution updated = repository.save(saved);
        
        // Then
        Optional<WorkflowExecution> found = repository.findByExecutionId(execution.getExecutionId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(WorkflowExecution.Status.SUCCESS);
        assertThat(found.get().getErrorMessage()).isEqualTo("Updated message");
        assertThat(found.get().getExecutionTimeMs()).isEqualTo(5000L);
        assertThat(found.get().getCompletedAt()).isNotNull();
    }
    
    @Test
    void shouldHandleJsonDataCorrectly() {
        // Given
        WorkflowExecution execution = createSampleExecution();
        
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("key1", "value1");
        inputData.put("key2", 123);
        inputData.put("key3", true);
        
        Map<String, Object> outputData = new HashMap<>();
        outputData.put("result", "success");
        outputData.put("count", 42);
        
        execution.setInputData(inputData);
        execution.setOutputData(outputData);
        
        // When
        WorkflowExecution saved = repository.save(execution);
        Optional<WorkflowExecution> found = repository.findByExecutionId(execution.getExecutionId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getInputData()).isEqualTo(inputData);
        assertThat(found.get().getOutputData()).isEqualTo(outputData);
    }
    
    @Test
    void shouldDeleteByExecutionId() {
        // Given
        WorkflowExecution execution = createSampleExecution();
        repository.save(execution);
        
        // Verify it exists
        Optional<WorkflowExecution> found = repository.findByExecutionId(execution.getExecutionId());
        assertThat(found).isPresent();
        
        // When
        repository.deleteByExecutionId(execution.getExecutionId());
        
        // Then
        Optional<WorkflowExecution> afterDelete = repository.findByExecutionId(execution.getExecutionId());
        assertThat(afterDelete).isEmpty();
    }
    
    @Test
    void shouldFindAllExecutions() {
        // Given
        WorkflowExecution execution1 = createSampleExecution();
        WorkflowExecution execution2 = createSampleExecution();
        execution2.setExecutionId("exec-2");
        
        repository.save(execution1);
        repository.save(execution2);
        
        // When
        List<WorkflowExecution> allExecutions = repository.findAll();
        
        // Then
        assertThat(allExecutions).hasSize(2);
    }
    
    private WorkflowExecution createSampleExecution() {
        WorkflowExecution execution = new WorkflowExecution();
        execution.setExecutionId("test-exec-1");
        execution.setWorkflowType("TEST_WORKFLOW");
        execution.setUserId("test-user");
        execution.setStatus(WorkflowExecution.Status.RUNNING);
        execution.setCreatedAt(LocalDateTime.now());
        execution.setUpdatedAt(LocalDateTime.now());
        
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("testKey", "testValue");
        execution.setInputData(inputData);
        
        return execution;
    }
}