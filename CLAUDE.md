# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run tests
./gradlew test

# Run IDE with plugin loaded
./gradlew runIde

# Build searchable options
./gradlew jarSearchableOptions

# Verify plugin
./gradlew verifyPlugin

# Publish plugin
./gradlew publishPlugin
```

## Project Overview

This is an IntelliJ Platform plugin called **Easy Dev** - a deployment tool inspired by Alibaba Cloud Toolkit. It provides server management and service deployment capabilities for development workflows.

**Target Platform:** IntelliJ IDEA 2022.3+ (build 223.8836+)

**Tech Stack:**
- Java (primary) and Kotlin for UI components
- Gradle with `org.jetbrains.intellij.platform` plugin (v2.10.4)
- sshj library for SSH/SFTP operations
- Swing for UI components

## Architecture

### Core Modules

**Services** (`src/main/java/tech/lin2j/idea/plugin/service/`):
- `ISshService` / `SshjSshService` - SSH connection and command execution
- `IHotReloadService` / `HotReloadServiceImpl` - Remote class hot reload using Arthas

**SSH Layer** (`src/main/java/tech/lin2j/idea/plugin/ssh/`):
- `SshConnectionManager` - Manages SSH connections with proxy/jump host support
- `SshjConnection` - sshj-based connection implementation
- `CommandLog` / `ConsoleCommandLog` - Command execution logging

**Models** (`src/main/java/tech/lin2j/idea/plugin/model/`):
- `DeployProfile` - Upload/deployment configuration
- `ConfigPersistence` - Plugin configuration storage
- Event classes for application events

**UI Components** (`src/main/java/tech/lin2j/idea/plugin/ui/`):
- `DashboardView` - Main tool window dashboard
- `SFTPEditor` / `SFTPFileSystem` - Virtual file system for remote files
- `ProgressTable` - File transfer progress tracking
- Dialog classes for configuration

**Actions** (`src/main/java/tech/lin2j/idea/plugin/action/`):
- Context menu actions for SFTP operations
- Hot reload action (`ClassHotReloadAction`)
- Configuration export/import actions

### Key Features

1. **Server Management** - Add/edit/remove servers with SSH password or private key auth
2. **File Transfer** - SFTP-based upload/download with filtering
3. **Command Execution** - Pre/post-upload commands with async support
4. **Run Configuration** - Deploy profiles integrated with IDE build process
5. **SFTP Panel** - Virtual file editor for remote file browsing
6. **Hot Reload** - Remote class redefinition via Arthas

### Configuration

The plugin uses `ConfigPersistence` for storing settings. Export/import functionality is available via `ConfigImportExport`.

### i18n

Internationalization via properties files:
- `src/main/resources/messages_en.properties`
- `src/main/resources/messages_zh.properties`

## Development Notes

- Local IntelliJ IDEA installation path: `D:\Program Files\ideaIU-2022.3.3.win`
- JVM arguments: `-Xmx2024m`
- The plugin depends on bundled plugins: `com.intellij.java`, `org.jetbrains.plugins.terminal`