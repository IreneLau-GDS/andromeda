package com.example.workflowservice.workflow.impl;

import com.example.workflowservice.model.WorkflowContext;
import com.example.workflowservice.model.WorkflowResult;
import com.example.workflowservice.workflow.Workflow;
import com.example.workflowservice.workflow.WorkflowExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample workflow implementation for sending notifications.
 */
@Component
public class NotificationWorkflow implements Workflow {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationWorkflow.class);
    
    @Override
    public WorkflowResult execute(WorkflowContext context) throws WorkflowExecutionException {
        logger.info("Starting notification workflow for execution: {}", context.getExecutionId());
        
        try {
            String recipient = (String) context.getInputValue("recipient");
            String message = (String) context.getInputValue("message");
            String notificationType = (String) context.getInputValue("type");
            
            // Simulate notification sending
            boolean success = sendNotification(recipient, message, notificationType);
            
            if (success) {
                WorkflowResult result = WorkflowResult.success(context.getExecutionId());
                result.addOutputData("recipient", recipient);
                result.addOutputData("notificationType", notificationType);
                result.addOutputData("sentAt", System.currentTimeMillis());
                result.setMessage("Notification sent successfully");
                
                logger.info("Notification workflow completed for execution: {}", context.getExecutionId());
                return result;
            } else {
                throw new WorkflowExecutionException("Failed to send notification");
            }
            
        } catch (Exception e) {
            logger.error("Notification workflow failed for execution: {}", context.getExecutionId(), e);
            throw new WorkflowExecutionException("Notification sending failed: " + e.getMessage(), e,
                "NOTIFICATION_ERROR", getWorkflowType(), context.getExecutionId());
        }
    }
    
    @Override
    public boolean validate(WorkflowContext context) {
        String recipient = (String) context.getInputValue("recipient");
        String message = (String) context.getInputValue("message");
        String type = (String) context.getInputValue("type");
        
        if (recipient == null || recipient.trim().isEmpty()) {
            logger.warn("Validation failed: 'recipient' field is required");
            return false;
        }
        
        if (message == null || message.trim().isEmpty()) {
            logger.warn("Validation failed: 'message' field is required");
            return false;
        }
        
        if (type == null || (!type.equals("EMAIL") && !type.equals("SMS") && !type.equals("PUSH"))) {
            logger.warn("Validation failed: 'type' must be EMAIL, SMS, or PUSH");
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getWorkflowType() {
        return "NOTIFICATION";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getDescription() {
        return "Sends notifications via email, SMS, or push notification";
    }
    
    @Override
    public boolean supportsAsyncExecution() {
        return true;
    }
    
    @Override
    public long getEstimatedExecutionTime() {
        return 2000; // 2 seconds
    }
    
    @Override
    public void beforeExecution(WorkflowContext context) {
        logger.debug("Preparing notification workflow for execution: {}", context.getExecutionId());
        String type = (String) context.getInputValue("type");
        context.setVariable("notificationChannel", type);
    }
    
    @Override
    public void afterExecution(WorkflowContext context, WorkflowResult result) {
        String channel = (String) context.getVariable("notificationChannel");
        logger.debug("Notification sent via {} channel", channel);
    }
    
    private boolean sendNotification(String recipient, String message, String type) {
        // Simulate notification sending with different success rates
        logger.info("Sending {} notification to {}: {}", type, recipient, message);
        
        // Simulate some processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        // Simulate 95% success rate
        return Math.random() > 0.05;
    }
}