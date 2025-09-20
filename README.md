# Workflow Service

A Spring Boot microservice for executing workflows with PostgreSQL persistence, built with Spring JDBC and Liquibase for database migrations.

## Features

- **Workflow Engine**: Pluggable workflow system with a common interface
- **PostgreSQL Integration**: Full persistence layer using Spring JDBC
- **Database Migrations**: Liquibase for schema management
- **REST API**: Complete REST endpoints for workflow management
- **Async Execution**: Support for both synchronous and asynchronous workflow execution
- **Testcontainers**: Integration tests with real PostgreSQL database
- **Sample Workflows**: Ready-to-use workflow implementations

## Architecture

### Core Components

- **Workflow Interface**: Base interface for all workflow implementations
- **WorkflowEngineService**: Main service for workflow registration and execution
- **WorkflowExecutionService**: Service for persistence operations
- **WorkflowExecutionRepository**: JDBC-based repository for database operations

### Database Schema

The service uses two main tables:
- `workflow_execution`: Stores workflow execution records
- `workflow_step`: Stores individual workflow step details (for future use)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (or use Docker)

### Running the Application

1. **Start PostgreSQL** (or use Docker):
   ```bash
   docker run --name postgres-workflow \
     -e POSTGRES_DB=workflow_db \
     -e POSTGRES_USER=workflow_user \
     -e POSTGRES_PASSWORD=workflow_pass \
     -p 5432:5432 -d postgres:15-alpine
   ```

2. **Build and run the application**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **The application will start on port 8080**

### Configuration

Update `src/main/resources/application.yml` with your database settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/workflow_db
    username: workflow_user
    password: workflow_pass
```

## API Endpoints

### Workflow Execution

- `POST /api/workflows/{workflowType}/execute` - Execute workflow synchronously
- `POST /api/workflows/{workflowType}/execute-async` - Execute workflow asynchronously
- `POST /api/workflows/{workflowType}/validate` - Validate workflow context
- `GET /api/workflows/{workflowType}/info` - Get workflow information
- `GET /api/workflows/types` - List all registered workflow types

### Workflow Execution Management

- `GET /api/workflows/executions/{executionId}` - Get execution by ID
- `GET /api/workflows/executions` - List executions (with optional filters)
- `DELETE /api/workflows/executions/{executionId}` - Delete execution record

### Query Parameters for Listing Executions

- `workflowType` - Filter by workflow type
- `userId` - Filter by user ID
- `status` - Filter by execution status

## Sample Workflows

### Data Processing Workflow

Processes a list of data records and calculates aggregations.

**Request Example**:
```bash
curl -X POST http://localhost:8080/api/workflows/DATA_PROCESSING/execute \
  -H "Content-Type: application/json" \
  -d '{
    "executionId": "exec-123",
    "workflowType": "DATA_PROCESSING",
    "userId": "user1",
    "inputData": {
      "data": [
        {"value": 10},
        {"value": 20},
        {"value": 30}
      ]
    }
  }'
```

### Notification Workflow

Sends notifications via different channels (EMAIL, SMS, PUSH).

**Request Example**:
```bash
curl -X POST http://localhost:8080/api/workflows/NOTIFICATION/execute \
  -H "Content-Type: application/json" \
  -d '{
    "executionId": "exec-456",
    "workflowType": "NOTIFICATION",
    "userId": "user1",
    "inputData": {
      "recipient": "user@example.com",
      "message": "Hello World!",
      "type": "EMAIL"
    }
  }'
```

## Creating Custom Workflows

1. **Implement the Workflow interface**:

```java
@Component
public class MyCustomWorkflow implements Workflow {
    
    @Override
    public WorkflowResult execute(WorkflowContext context) throws WorkflowExecutionException {
        // Your workflow logic here
        return WorkflowResult.success(context.getExecutionId());
    }
    
    @Override
    public boolean validate(WorkflowContext context) {
        // Validation logic
        return true;
    }
    
    @Override
    public String getWorkflowType() {
        return "MY_CUSTOM_WORKFLOW";
    }
    
    // Implement other required methods...
}
```

2. **The workflow will be automatically registered** when the application starts.

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test"

# Run only integration tests
mvn test -Dtest="*IntegrationTest"
```

### Integration Tests

The project uses Testcontainers for integration testing with a real PostgreSQL database. Tests automatically start a PostgreSQL container and run migrations.

## Database Migrations

Database schema is managed using Liquibase. Migration files are located in:
- `src/main/resources/db/changelog/`

To add new migrations:
1. Create a new changeset file in the changelog directory
2. Include it in `db.changelog-master.xml`

## Monitoring and Health Checks

The application includes Spring Boot Actuator endpoints:
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Spring JDBC** - Database access
- **PostgreSQL** - Database
- **Liquibase** - Database migrations
- **Jackson** - JSON processing
- **Testcontainers** - Integration testing
- **JUnit 5** - Testing framework
- **AssertJ** - Test assertions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License.