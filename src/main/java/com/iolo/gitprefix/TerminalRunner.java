package com.iolo.gitprefix;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;

import java.io.IOException;

public class TerminalRunner {
    public static void runCommand(Project project, String command) {
        TerminalToolWindowManager terminalManager = TerminalToolWindowManager.getInstance(project);
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Terminal");

        if (window != null) {
            window.show(() -> {
                try {
                    // 1. 获取或创建终端
                    ShellTerminalWidget widget = terminalManager.createLocalShellWidget(project.getBasePath(), "Git Prefix Run");

                    // 2. 将命令按行拆分
                    String[] lines = command.split("\n");

                    // 3. 循环发送每一行
                    for (String line : lines) {
                        String trimmedLine = line.trim();
                        if (!trimmedLine.isEmpty()) {
                            // 使用 executeCommand 发送单行，它会自动处理换行并触发执行
                            widget.executeCommand(trimmedLine);

                            // 关键：为了防止 PowerShell 吞掉命令或顺序错乱
                            // 在发送下一行前给 Shell 一点点喘息时间（50ms-100ms）
                            Thread.sleep(100);
                        }
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}