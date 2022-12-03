package io.github.lmikoto.railgun.action;

import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 * @author jinwq
 * @Time 2022/11/27
 */
public class ItemDeleteAction extends BaseTemplateAction implements AnActionButtonRunnable {

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
