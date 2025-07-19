# Jindo Configuration Examples

This directory contains example `.jindo.yaml` configuration files for different project types and use cases.

## Available Examples

### `basic.jindo.yaml`
A minimal configuration showing:
- Scala formatting with scalafmt
- Simple compilation check
- Basic pre-commit hooks only

**Best for**: Small Scala projects, getting started with Jindo

### `comprehensive.jindo.yaml`
A full-featured configuration demonstrating:
- Multiple pre-commit hooks
- JVM tools with custom repositories
- System commands with working directories
- Complex argument passing

**Best for**: Large projects, teams wanting comprehensive quality gates

### `java-project.jindo.yaml`
Java-focused configuration featuring:
- Google Java Format for code formatting
- SpotBugs for static analysis
- Checkstyle for code style enforcement
- Maven integration with compile and test

**Best for**: Java projects using Maven, code quality enforcement

## Using These Examples

1. Copy the appropriate example to your project root as `.jindo.yaml`:
   ```bash
   cp examples/basic.jindo.yaml .jindo.yaml
   ```

2. Customize the configuration for your project needs

3. Install the git hooks:
   ```bash
   jindo install
   ```

4. Test the configuration:
   ```bash
   jindo validate
   jindo list
   ```

## Configuration Schema

All examples follow the [JSON Schema](../jindo-schema.json) for validation.

## Common Patterns

### JVM Tools
```yaml
- id: tool-name
  main: com.example.MainClass
  dependencies:
    - group:artifact:version
  args: ["--option", "value"]
```

### System Commands
```yaml
- id: command-name
  command: executable
  args: ["arg1", "arg2"]
  working-directory: "/path/to/dir"  # optional
```

### Custom Repositories
```yaml
- id: private-tool
  main: com.company.Tool
  dependencies:
    - com.company:private-tool:1.0.0
  repositories:
    - https://nexus.company.com/repository/maven-public/
    - https://repo1.maven.org/maven2/
```

## Troubleshooting

- **Dependency resolution fails**: Check that versions exist in Maven Central
- **Hook execution fails**: Verify main class names and command paths
- **Permission denied**: Ensure jindo binary is executable and in PATH
- **Git hooks not triggering**: Run `jindo install` to reinstall hooks

For more help, see the main [README](../README.md) or run `jindo --help`.