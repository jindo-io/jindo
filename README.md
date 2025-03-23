# Jindo(珍島)

**J**VM **I**ntegrated, **N**o-**D**eps **O**peration

Jindo is a lightweight, JVM-based Git hooks manager that simplifies your development workflow.
It allows you to run JVM applications as Git hooks without external dependencies.

## Why Jindo?

- We need to run git-hooks in our project with just JVM.
- Don't rely on pre-commit or husky anymore.
- Here's a simple, fast, and lightweight 🐕 Jindo.

## Getting Started

### MacOS

```bash
brew tap jindo-io/jindo
brew install jindo
```

### Others

Download the latest release.

## Features

### 🚀 Zero External Dependencies
* Only requires JVM 8+
* No need for Node.js, Python, or other runtimes
* Minimal setup overhead

### 🔧 Simple Yet Powerful Configuration
* Easy-to-understand YAML configuration
* Support for multiple repositories
* Flexible hook management

### 📦 Seamless JVM Integration
* Direct access to Maven Central
* Automatic dependency resolution via Coursier
* Run any JVM-based tools (Java, Scala, Kotlin, etc.)

### 🔒 Reliable and Consistent
* Same JVM environment across all hooks
* Predictable behavior in CI/CD
* Better performance for JVM-based checks
* Direct execution without wrappers
