Dify Webapp Plugin for Fess [![Java CI with Maven](https://github.com/codelibs/fess-webapp-dify/actions/workflows/maven.yml/badge.svg)](https://github.com/codelibs/fess-webapp-dify/actions/workflows/maven.yml)
==========================

## Overview

Dify Webapp Plugin is a webapp plugin for Fess.
This enhances the functionality of Fess by providing an API for querying documents.

## Download

You can download the plugin from the [Maven Repository](https://repo1.maven.org/maven2/org/codelibs/fess/fess-webapp-dify/).

## Installation

For detailed installation instructions, refer to the [Plugin section](https://fess.codelibs.org/14.3/admin/plugin-guide.html) of the Fess Administration Guide.

## API Documentation

The Dify Plugin provides a retrieval API for querying documents. Below is an overview of the available endpoint.

### Query Endpoint

#### GET /query

Finds documents by search conditions using GET method.

- **Parameters:**
  - `query` (string): Search words (e.g., "Fess")
  - `top_k` (integer): Top K results to return (default: 3)
  - `operator` (string): Query operator (AND, OR)

- **Responses:**
  - `200`: Successful response with the results
  - `401`: Unauthorized request
  - `404`: Not found or validation error
  - `500`: Internal server error

### Components

#### Parameters

- **query**: Search words (optional)
- **top_k**: Number of top results to return (optional, default: 3)
- **operator**: Query operator (optional, values: AND, OR)

#### Responses

- **SuccessResponse**: Contains the query results.
- **UnauthorizedError**: Indicates an unauthorized request.
- **NotFoundError**: Indicates that the requested resource was not found.
- **InternalServerError**: Indicates an internal server error.

#### Schemas

- **Document**: Represents a document with fields such as `id`, `url`, and `content`.
- **HTTPValidationError**: Represents a validation error.
- **QueryResponse**: Represents the response from a query, containing an array of documents.
- **ValidationError**: Represents a specific validation error.

#### Security Schemes

- **HTTPBearer**: Uses HTTP Bearer tokens for authentication.

## Contributing

We welcome contributions to improve this plugin.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](https://www.apache.org/licenses/LICENSE-2.0.html) file for details.
