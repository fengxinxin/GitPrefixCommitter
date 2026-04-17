# Git Prefix Committer

[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/com.iolo.gitprefix.GitPrefixCommitter?label=plugin)](https://plugins.jetbrains.com/plugin/com.iolo.gitprefix.GitPrefixCommitter)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**Git Prefix Committer** is a productivity-focused IntelliJ IDEA plugin designed to streamline Git workflows that require specific command prefixes. It allows developers to generate and copy complete Git instructions directly from the Commit tool window, making it perfect for environments using custom CLI tools (like `testing`), build scripts, or security wrappers.

---

## 🌟 Key Features

* **One-Click Command Generation**: Adds a dedicated action icon next to the primary "Commit" button to generate Git commands for selected changes.
* **Smart Path Resolution**: Automatically converts absolute file paths to relative paths (based on the project root), ensuring commands are ready-to-run in the terminal.
* **Automatic Change Categorization**:
    * **Deleted Files**: Generates `git rm --ignore-unmatch --cached -r --` commands.
    * **Modified/New Files**: Generates `git add --` and prefixed `git commit` commands.
* **Highly Customizable**: Define your global command prefix (e.g., `testing`) via the IDE settings panel.
* **IntelliJ 2025.3 Native Integration**: Deeply integrated with the latest IntelliJ Platform Commit Workflow UI.

---

## 🚀 Installation

1.  **From Marketplace** (Recommended):
    * Go to `Settings` -> `Plugins`.
    * Search for "Git Prefix Committer".
    * Click `Install`.

2.  **Manual Installation**:
    * Download the latest `ZIP` from [Releases](https://github.com/fengxinxin/GitPrefixCommitter/releases).
    * In the `Plugins` settings, click the gear icon and select `Install Plugin from Disk...`.

---

## 🛠️ Usage

### 1. Configure Prefix
Navigate to `Settings` -> `Appearance & Behavior` -> `Git Prefix Settings`. Enter your required prefix (e.g., `testing`).

### 2. Generate Commands
In the `Commit` tool window, check the files you want to include, then click the **Green Play Icon** located next to the blue Commit button.

### 3. Execute
Verify the generated commands in the popup dialog, click `Copy` to clipboard, and paste them into your terminal to execute.

---

## 📄 Example Output

If your prefix is set to `testing`, and you have modified `Main.java` and deleted `OldFile.java`, the plugin will generate:

```bash
git rm --ignore-unmatch --cached -r -- OldFile.java
testing git add -- src/Main.java
testing git commit -m "your commit message" -- src/Main.java