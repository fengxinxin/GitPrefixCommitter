package com.iolo.gitprefix.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GitPrefixConfigurable implements Configurable {
    private final JBTextField myPrefixField = new JBTextField();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Git Prefix Committer Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Git command prefix:", myPrefixField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public boolean isModified() {
        return !myPrefixField.getText().equals(AppSettingsState.getInstance().getPrefix());
    }

    @Override
    public void apply() {
        AppSettingsState.getInstance().setPrefix(myPrefixField.getText());
    }

    @Override
    public void reset() {
        myPrefixField.setText(AppSettingsState.getInstance().getPrefix());
    }
}