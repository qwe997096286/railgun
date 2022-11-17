package io.github.lmikoto.railgun.configurable.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.NlsActions;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.util.ui.ConfirmationDialog;
import io.github.lmikoto.railgun.configurable.CodeGroup;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.configurable.componet.NameEditDialog;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

public class ItemDeleteAction extends BaseTemplateAction implements AnActionButtonRunnable {

    @Setter
    private List<CodeGroup> groupList;

    public ItemDeleteAction(TemplateConfigurable configurable) {
        super(configurable);
    }

    @Override
    public void run(AnActionButton anActionButton) {
        DefaultMutableTreeNode selectedNode = this.getSelectedNode();
        int n = JOptionPane.showConfirmDialog(anActionButton.getContextComponent(), "确认是否删除", "确认对话框",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            deleteItem(selectedNode);
            saveTree();
        }
        updateUI();
    }

}
