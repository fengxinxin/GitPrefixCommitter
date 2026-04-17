package com.iolo.gitprefix.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler;
import com.iolo.gitprefix.TerminalRunner;
import com.iolo.gitprefix.settings.AppSettingsState;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GitPrefixCommitAction extends AnAction {

    private final boolean isAutoMode;

    // 无参构造函数给 XML 默认使用（手动模式）
    public GitPrefixCommitAction() {
        this(false);
    }

    // 带参数构造函数，方便扩展
    public GitPrefixCommitAction(boolean isAutoMode) {
        this.isAutoMode = isAutoMode;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 1. 获取 Commit Handler (2025.x 推荐方式)
        AbstractCommitWorkflowHandler<?, ?> handler = (AbstractCommitWorkflowHandler<?, ?>) e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER);
        if (handler == null) {
            Messages.showWarningDialog(project, "Please use this action within the Commit tool window.", "Git Prefix Committer");
            return;
        }

        // 2. 获取提交注释模板 (原代码中的 messageTemplate)
        // 2025.x 最稳定的获取方式：直接从 UI 组件获取当前输入的文字
        String commitMessage = handler.getUi().getCommitMessageUi().getText();

        // 3. 获取并分类变更文件
        Collection<Change> includedChanges = handler.getUi().getIncludedChanges();

        // 提取删除的文件路径
        List<String> deletedPaths = includedChanges.stream().filter(c -> c.getType() == Change.Type.DELETED).map(c -> getRelativePath(project, c.getBeforeRevision())).filter(Objects::nonNull).collect(Collectors.toList());

        // 提取新增或修改的文件路径
        List<String> updatePaths = includedChanges.stream().filter(c -> c.getType() != Change.Type.DELETED).map(c -> getRelativePath(project, c.getAfterRevision())).filter(Objects::nonNull).collect(Collectors.toList());

        // 4. 构建命令字符串 (对应原代码中的 initCommand 逻辑)
        String finalCommand = buildGitCommand(project, commitMessage, deletedPaths, updatePaths);

        // 5. 显示结果对话框
        // --- 核心优化部分：根据模式选择执行路径 ---
        if (isAutoMode) {
            // 自动模式：直接跳过 Dialog 运行
            TerminalRunner.runCommand(project, finalCommand);
            // 可以在此处添加一个简单的气泡通知
        } else {
            // 手动模式：弹出你写的那个 Dialog
            new CommandResultDialog(project, finalCommand).show();
        }
    }

    private @Nullable String getRelativePath(@NotNull Project project, @Nullable ContentRevision revision) {
        if (revision == null || project.getBaseDir() == null) return null;
        return revision.getFile().getPath().substring(project.getBasePath().length() + 1);
    }

    private String buildGitCommand(Project project, String message, List<String> deleted, List<String> updates) {

        // 从持久化服务中获取配置的前缀
        String prefix = AppSettingsState.getInstance().getPrefix();

        StringBuilder finalResult = new StringBuilder();

        // 1. 处理删除的文件命令 (git rm)
        StringBuilder deleteCommand = new StringBuilder();
        if (CollectionUtils.isNotEmpty(deleted)) {
            deleteCommand.append("git rm --ignore-unmatch --cached -r -- ");
            String resultDelete = String.join(" ", deleted);
            deleteCommand.append(resultDelete).append("\n");
        }

        // 2. 处理新增和修改的文件命令 (git add)
        StringBuilder updateAndNewCommand = new StringBuilder();
        if (CollectionUtils.isNotEmpty(updates)) {
            updateAndNewCommand.append(prefix + " git add -- ");
            String result = String.join(" ", updates);
            updateAndNewCommand.append(result).append("\n");
        }

        // 3. 处理提交命令 (git commit)
        StringBuilder commitCommand = new StringBuilder(prefix + " git commit -m ");
        String finalCommitMessage = message;

        // 拼接消息和对应的文件
        commitCommand.append("\"").append(finalCommitMessage).append("\" ");
        commitCommand.append("-- " + String.join(" ", updates));

        // 4. 合并所有结果
        finalResult.append(deleteCommand).append(updateAndNewCommand).append(commitCommand);

        return finalResult.toString();
    }

    // 内部类：结果对话框，带“复制”和“取消”按钮
    private static class CommandResultDialog extends DialogWrapper {
        private final Project project; // 1. 定义成员变量
        private final String command;
        private final JBTextArea textArea;

        protected CommandResultDialog(Project project, String command) {
            super(project);
            this.project = project;
            this.command = command;
            this.textArea = new JBTextArea(15, 60);
            setTitle("Generated Git Command");
            init();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            textArea.setText(command);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setMargin(JBUI.insets(10));
            return new JBScrollPane(textArea);
        }

        @Override
        protected Action @NotNull [] createActions() {
            // 运行按钮
            Action runAction = new AbstractAction("Run in Terminal") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 调用上面写的工具类
                    TerminalRunner.runCommand(project, textArea.getText());
                    close(OK_EXIT_CODE);
                }
            };
            // 自定义“Copy”动作
            Action copyAction = new AbstractAction("Copy to Clipboard") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CopyPasteManager.getInstance().setContents(new StringSelection(textArea.getText()));
                    close(OK_EXIT_CODE);
                    Messages.showInfoMessage("Command copied successfully!", "Success");
                }
            };

            // 设置为默认按钮（按回车键即可触发），配合蓝色样式效果最佳
            runAction.putValue(DialogWrapper.DEFAULT_ACTION, true);

            return new Action[]{runAction, copyAction, getCancelAction()};
        }

        @Override
        protected JButton createJButtonForAction(Action action) {
            JButton button = super.createJButtonForAction(action);

            // 关键点：将按钮标记为“默认按钮”或“主要按钮”
            // 在 IntelliJ 皮肤中，这会触发蓝色高亮
            if ("Run in Terminal".equals(action.getValue(Action.NAME))) {
                button.putClientProperty("ActionToolbar.isPrimary", true); // 某些版本有效
                button.putClientProperty("JButton.buttonType", "primary"); // 现代 IDEA UI 规范
            }
            return button;
        }
    }
}