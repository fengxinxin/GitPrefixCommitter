package com.iolo.gitprefix.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(
        name = "com.iolo.gitprefix.AppSettingsState",
        storages = @Storage("GitPrefixSettings.xml")
)
public final class AppSettingsState extends SimplePersistentStateComponent<AppSettingsState.State> {

    public static class State extends BaseState {
        // 使用 @Property 确保序列化兼容性
        public String prefix = "";
    }

    public AppSettingsState() {
        super(new State());
    }

    public static AppSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(AppSettingsState.class);
    }

    public String getPrefix() {
        return getState().prefix != null ? getState().prefix : "";
    }

    public void setPrefix(String value) {
        getState().prefix = value;
    }
}