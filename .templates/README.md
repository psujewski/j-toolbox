> **ℹ️ This file is generated from `.templates/README.md` during release.**  
> For the development version, see `.templates/README.md`.

# Toolbox

A lightweight Java utility library providing **testable abstractions** and
**functional-style modeling** for domain logic.

## Installation

Add the following dependency to your pom.xml:

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
</dependency>
```

[![Maven Central](https://img.shields.io/maven-central/v/info.psuj/toolbox.svg?label=Maven%20Central)](https://search.maven.org/artifact/info.psuj/toolbox)

## Features

* Testable providers for static Java classes (e.g. `Clock`, `UUID`)
* Immutable `Result` object to model operation outcomes
* Zero dependencies

## Package Layout

* `info.psuj.toolbox.time` – Time abstractions
* `info.psuj.toolbox.uuid` – UUID abstractions
* `info.psuj.toolbox.shared` – Domain-level modeling

## Testable Providers

### time

```java
TimeProvider time = new SystemTimeProvider();
Instant now = time.now();
```

In tests, replace with:

```java
TimeProvider time = new FixedTimeProvider(Instant.parse("2024-01-01T00:00:00Z"));
```

### uuid

```java
UuidProvider uuid = new SystemUuidProvider();
UUID id = uuid.randomUuid();
```

In tests, replace with:

```java
UuidProvider uuid = new FixedUuidProvider(UUID.fromString("00000000-0000-0000-0000-000000000001"));
```

## Result

`Result` models the outcome of a business operation. It may contain:

* A successful entity (any object)
* One or more domain events (`DomainEvent`)
* Or a set of error messages (on failure)

### Example

```java
Result<User> result = Result.success(user, new UserCreatedEvent());
```

Your event type:

```java
record UserCreatedEvent(UUID userId) implements DomainEvent {}
```

### API Overview

| Method                              | Description                                               |
| ----------------------------------- | --------------------------------------------------------- |
| `Result.success()`                  | Returns a success with no entity or events                |
| `Result.success(entity)`            | Returns a success with the given entity, no events        |
| `Result.success(entity, events...)` | Returns a success with entity and domain events           |
| `Result.success(Set<DomainEvent>)`  | Returns a success with domain events, no entity           |
| `Result.success(events...)`         | Returns a success with one or more domain events          |
| `Result.failure(String...)`         | Returns a failure with one or more error messages         |
| `Result.failure(Set<String>)`       | Returns a failure with a given set of error messages      |
| `isSuccess()`                       | Returns `true` if the result is successful                |
| `isFailure()`                       | Returns `true` if the result is a failure                 |
| `entity()`                          | Returns the optional entity (`Optional<T>`)               |
| `events()`                          | Returns unmodifiable set of domain events                 |
| `errors()`                          | Returns unmodifiable set of error messages                |
| `map(Function<T, R>)`               | Transforms the entity (if present), keeps original events |

## License

MIT – use freely in open source and commercial projects.
