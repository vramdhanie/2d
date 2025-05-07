# Contributing to Java 2D Game Template

Thank you for considering contributing to the Java 2D Game Template! This document explains how to contribute to the project effectively.

## Code of Conduct

Please note that this project is released with a [Contributor Code of Conduct](CODE_OF_CONDUCT.md). By participating in this project you agree to abide by its terms.

## How Can I Contribute?

### Reporting Bugs

* Check if the bug has already been reported in the Issues section
* Use the bug report template when creating a new issue
* Include detailed steps to reproduce the bug
* Include screenshots if applicable
* Specify the version of Java and OS you're using

### Suggesting Enhancements

* Check if the enhancement has already been suggested in the Issues section
* Use the feature request template when creating a new issue
* Provide a clear and detailed explanation of the feature you want to see
* Explain why this enhancement would be useful to most users

### Pull Requests

1. Fork the repository
2. Create a new branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run the tests to ensure your changes don't break existing functionality
5. Commit your changes (`git commit -m 'Add some amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Styleguides

### Git Commit Messages

* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

### Java Styleguide

* Follow the standard Java coding conventions
* Use 4 spaces for indentation, not tabs
* Use explicit imports instead of wildcard imports
* Keep methods short and focused on a single task
* Document public methods and classes with Javadoc

### Testing

* Write unit tests for new features
* Make sure all tests pass before submitting a pull request
* Add tests for bug fixes to prevent regression

## Development Setup

To set up the project for development:

1. Clone your fork of the repository
2. Install dependencies:
   ```
   ./gradlew build
   ```
3. Run the application:
   ```
   ./gradlew run
   ```

## Project Structure

* `app/src/main/java/com/vincentramdhanie/twod/game/` - Main source code
* `app/src/main/resources/` - Game assets and resources
* `app/src/test/` - Test files

Thank you for contributing to the Java 2D Game Template! 