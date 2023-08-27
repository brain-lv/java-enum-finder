# Java Enum Finder Gradle Plugin

##   Overview

The Java Enum Finder is a Gradle plugin designed to search for usages of specific Java Enum classes in your project's main source. The plugin scans through the source code files to find occurrences of the specified enums and reports the files and line numbers where these enums are used.

## Requirements

Java 8 or higher
Gradle 5.6 or higher
## Installation

To add the plugin to your project, include the following in your build.gradle:

```groovy
plugins {
    id 'lv.brain.gradle.javaEnumFinder' version '1.0.0'
}
```
### Configuration

To configure the plugin, you can specify the enums you wish to search for using the javaEnumFinder block in your build.gradle file:

```groovy
javaEnumFinder {
    target 'java.nio.file.AccessMode'
}
```
## Usage

To execute the plugin, run the following Gradle task:

```bash
./gradlew javaEnumFind
```
This will scan your project's main source code for the specified enum and output the results, such as:

```bash
Found 'READ' from 'java.nio.file.AccessMode' in 'src/main/java/com/yourcompany/YourClass.java' at line 42
Found 'WRITE' from 'java.nio.file.AccessMode' in 'src/main/java/com/yourcompany/AnotherClass.java' at line 13
```
## Contributing

If you find any issues or have suggestions for improvements, please file an issue or create a pull request.

## License

This project is licensed under the "Mozilla Public License Version 2.0" - see the LICENSE.md file for details.