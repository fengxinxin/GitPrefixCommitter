package com.iolo.gitprefix;

import com.intellij.openapi.util.IconLoader;
import javax.swing.*;

public interface MyIcons {
    // 指向新图标路径
    public static final Icon GitPrefixCommitterManual = IconLoader.getIcon("/icons/GitPrefixCommitter.svg", MyIcons.class);
    public static final Icon GitPrefixCommitterAutomation = IconLoader.getIcon("/icons/GitPrefixCommitterAutomation.svg", MyIcons.class);
}
