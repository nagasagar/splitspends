# Backend Documentation

This folder contains documentation and specifications for the SplitSpends backend API.

## Files

### `openapi.json`
- **Generated automatically** during `mvn verify` 
- Contains the complete OpenAPI 3.1 specification for the REST API
- Includes all endpoints, schemas, and operation details
- Source of truth for API documentation

### Generation Process
The OpenAPI specification is automatically generated using:
1. **SpringDoc OpenAPI Maven Plugin** - Generates the spec from running application
2. **Maven Antrun Plugin** - Copies the file to `../docs/openapi.json` for centralized access

### Usage
- **Backend Developers**: Use the local `openapi.json` for API development
- **Frontend Developers**: Access via root `docs/openapi.json` or this local copy
- **CI/CD**: Include in build artifacts and deployment processes
- **API Tools**: Import into Postman, Insomnia, or other API clients

### Manual Generation
To regenerate the OpenAPI specification:
```bash
mvn verify
```

The file will be updated in both locations:
- `backend/docs/openapi.json` (primary)
- `docs/openapi.json` (copy for centralized access)
