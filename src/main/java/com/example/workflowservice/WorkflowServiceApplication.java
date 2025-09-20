package com.example.workflowservice;

import com.example.workflowservice.service.WorkflowEngineService;
import com.example.workflowservice.workflow.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class WorkflowServiceApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceApplication.class);
    
    @Autowired
    private WorkflowEngineService workflowEngineService;
    
    @Autowired
    private List<Workflow> workflows;
    
    public static void main(String[] args) {
        SpringApplication.run(WorkflowServiceApplication.class, args);
    }
    
    @Override
    public void run(String... args) {
        logger.info("Starting Workflow Service Application");
        
        // Register all workflow implementations
        for (Workflow workflow : workflows) {
            workflowEngineService.registerWorkflow(workflow);
        }
        
        logger.info("Workflow Service Application started successfully");
        logger.info("Registered {} workflow types", workflows.size());
    }
}