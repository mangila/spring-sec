# spring-sec

Design for Security is a broad topic, but let's KISS (Keep it simple Stupid) :)

**NOTE** DO NOT forget about Spring Actuator when securing a Spring Boot application.

#### Basic Web App L7 Security Design scope

* Protect the data
* Authorize the data
* Secure the data
* Validate the data
* Sensitive value management

### Protect the data

Authenticate the app; in this example, we will use basic authentication.

Authentication is the process of verifying the identity of a user.

### Authorize the data

Authorize the user to access the data

Role-based authorization, or more fine-grained access control.

In this example, we will use Spring Security's built-in support for role-based access control.

Two users are partitioned into this application:

* One reader user with only read access to the data
* One writer user with read and write access to the data

Authorization is the process of determining whether a user is allowed to access a resource.

### Secure the data

Encrypt data at transit HTTPS (SSL/TLS) - Layer 6 stuffs, but anyway

Often, another proxy service can be used for managing the SSL/TLS termination.

### Validate the data

Make sure the data don't get corrupted

Validate and sanitize input data

Fail fast and fail loudly or fail silently with a fallback value

### Sensitive value management

The current best practice is to use secure storage for sensitive values.

And only fetch credentials that are needed. Least privilege principle.

Like a third-party service, or an encrypted value in a database.

In this example, we will use a simple in-memory store. To simulate a real-world construct.