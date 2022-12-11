package io.github.lmikoto.railgun.action;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.uiDesigner.core.GridConstraints;
import io.github.lmikoto.railgun.componet.NameEditDialog;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import io.github.lmikoto.railgun.utils.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liuyang
 * 2021/3/7 7:46 下午
 */
public class TemplateAddAction extends BaseTemplateAction implements AnActionButtonRunnable {
    public TemplateAddAction(TemplateConfigurable configurable) {
        super(configurable);
    }

    @Override
    public void run(AnActionButton anActionButton) {
        final DefaultMutableTreeNode selectedNode = getSelectedNode();
        List<AnAction> actions = getMultipleActions(selectedNode);
        if (CollectionUtils.isEmpty(actions)) {
            return;
        }

        DefaultActionGroup group = new DefaultActionGroup(actions);
        JBPopupFactory.getInstance()
                .createActionGroupPopup(null, group, DataManager.getInstance().getDataContext(anActionButton.getContextComponent()),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true).show(anActionButton.getPreferredPopupPoint());
    }

    private List<AnAction> getMultipleActions(DefaultMutableTreeNode selectedNode) {
        List<AnAction> actions = new ArrayList<>();
        CodeGroupAddAction groupAction = new CodeGroupAddAction();

        if (selectedNode == null) {
            actions.add(groupAction);
            return actions;
        }

        Object object = selectedNode.getUserObject();
        // 2. 如果选中的是group, 则可以新增root, group以及template
        if (object instanceof CodeGroup) {
            CodeDirAddAction codeDirAddAction = new CodeDirAddAction(selectedNode);
            actions.add(codeDirAddAction);
        } else if (object instanceof CodeDir) {
            CodeTemplateAddAction templateAction = new CodeTemplateAddAction(selectedNode, TemplateDict.SQL2CLASS);
            actions.add(templateAction);
        }
        // 3. 如果选中的是template, 则可以新增root, group以及template
        return actions;
    }

    class CodeGroupAddAction extends AnAction implements DumbAware {

        public CodeGroupAddAction() {
            super("Code Group", null, AllIcons.Nodes.JavaModule);
        }

        @Override
        public void update(AnActionEvent e) {
            setEnabledInModalContext(false);
            e.getPresentation().setEnabled(true);
        }
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            NameEditDialog dialog = new NameEditDialog();
            dialog.setTitle("Create Group");
            dialog.getButtonOK().addActionListener(e -> {
                String name = dialog.getNameField().getText();
//                if (StringUtils.isBlank(name)) {
//                    showErrorBorder(dialog.getNameField(), true);
//                    return;
//                }
                addGroup(CodeGroup.fromName(name.trim()));
                dialog.setVisible(false);
            });
            showDialog(dialog, 300, 150);
        }
    }
    class CodeTemplateAddAction extends AnAction implements DumbAware {

        private final DefaultMutableTreeNode selectedNode;
        private String type;
        private ComboBox<String> types;
        public CodeTemplateAddAction(DefaultMutableTreeNode selectedNode, String type) {
            super("Code Template ", null, AllIcons.Nodes.JavaModule);
            char c = Character.toUpperCase(type.charAt(0));
            type = c + type.substring(1);
            this.selectedNode = selectedNode;
            this.type = type;
        }

        @Override
        public void update(AnActionEvent e) {
            setEnabledInModalContext(false);
            e.getPresentation().setEnabled(true);
        }
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            NameEditDialog dialog = new NameEditDialog();
            dialog.setTitle("Create Code Template");
            ComboBox<String> types = new ComboBox<>();

            types.addItem(TemplateDict.SQL2CLASS);
            types.addItem(TemplateDict.SQL2CONFIG);
            types.addItem(TemplateDict.ENTITY2CONFIG);
            types.addItem(TemplateDict.ENTITY2SELECT);
            types.addItem(TemplateDict.VM2FILE);
            GridConstraints gridConstraints = new GridConstraints();
            gridConstraints.setAnchor(GridConstraints.ANCHOR_SOUTHWEST);
            dialog.getSouthPanel().add(types, gridConstraints);
            dialog.getButtonOK().addActionListener(e -> {
                String name = dialog.getNameField().getText();
                String typeSelect = (String) types.getSelectedItem();
                if (StringUtils.isBlank(name) || StringUtils.isBlank(typeSelect)) {
                    NotificationUtils.simpleNotify("请将信息填写完整");
                    return;
                }
                addTemplate(CodeTemplate.fromName(name.trim(), typeSelect), this.selectedNode);
                dialog.setVisible(false);
            });
            showDialog(dialog, 400, 150);
        }
    }
    class CodeDirAddAction extends AnAction implements DumbAware {

        private final DefaultMutableTreeNode selectedNode;

        public CodeDirAddAction(DefaultMutableTreeNode selectedNode) {
            super("Code Dir", null, AllIcons.Nodes.JavaModule);
            this.selectedNode = selectedNode;
        }

        @Override
        public void update(AnActionEvent e) {
            setEnabledInModalContext(false);
            e.getPresentation().setEnabled(true);
        }
        @Override
        public void actionPerformed(AnActionEvent anActionEvent) {
            NameEditDialog dialog = new NameEditDialog();
            dialog.setTitle("Create Code Dir");
            dialog.getButtonOK().addActionListener(e -> {
                String name = dialog.getNameField().getText();
//                if (StringUtils.isBlank(name)) {
//                    showErrorBorder(dialog.getNameField(), true);
//                    return;
//                }
                addDir(CodeDir.fromName(name.trim()), selectedNode);
                dialog.setVisible(false);
            });
            showDialog(dialog, 300, 150);
        }
    }
}
