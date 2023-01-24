package io.github.lmikoto.railgun.action;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.dao.CodeGroupDao;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.service.AppendNode;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author jinwq
 * @Date 2022/12/10 17:31
 */
public class SaveGroupAction extends BaseTemplateAction implements AnActionButtonRunnable {
    private AppendNode appendNode;
    public SaveGroupAction(TemplateConfigurable configurable) {
        super(configurable);
        appendNode = configurable;
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
        Object userObject = null;
        if (selectedNode != null) {
            userObject = selectedNode.getUserObject();
        }
        List<AnAction> actionList = Lists.newArrayList();
        if (userObject instanceof CodeGroup) {
            int index = this.getGroupList().indexOf(userObject);
            if (index != -1) {
                actionList.add(new SaveMenuItem(this.getGroupList().get(index)));
            }
        }
        actionList.add(new SaveFirstGroup((obj) -> this.saveTree()));
        actionList.add(new ImportCodeGroup(this.getGroupList()));
        return actionList;
    }


    class ImportCodeGroup extends AnAction implements DumbAware {
        private final List<CodeGroup> groupList;

        public ImportCodeGroup(List<CodeGroup> groupList) {
            super("import");
            this.groupList = groupList;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            JFileChooser fileChoose = new JFileChooser("./");
            fileChoose.setMultiSelectionEnabled(false);
            fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChoose.setDialogTitle("选择导入文件路径");
            JDialog jDialog = new JDialog();
            int result = fileChoose.showOpenDialog(jDialog);
            if (JFileChooser.APPROVE_OPTION == result) {
                String directory = fileChoose.getSelectedFile().getPath();
                CodeGroup group = CodeGroupDao.getGroup(directory);
                if (null == group) {
                    NotificationUtils.simpleNotify("group导入失败");
                    return;
                }
                appendNode.saveData2Tree(Collections.singletonList(group));
                groupList.add(group);
                NotificationUtils.simpleNotify("当前选择的group已导入成功");
            }
            jDialog.dispose();
        }
    }
    class SaveMenuItem extends AnAction implements DumbAware {
        private final CodeGroup codeGroup;

        public SaveMenuItem(CodeGroup codeGroup) {
            super("save code group");
            this.codeGroup = codeGroup;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

            JFileChooser fileChoose = new JFileChooser("./");
            fileChoose.setMultiSelectionEnabled(false);
            fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChoose.setDialogTitle("选择保存路径");
            JDialog jDialog = new JDialog();
            int result = fileChoose.showSaveDialog(jDialog);
            if (JFileChooser.APPROVE_OPTION == result) {
                String directory = fileChoose.getSelectedFile().getPath();
                File file = new File(directory);
                boolean success = saveTree(file.getPath(), codeGroup);
                if (success) {
                    NotificationUtils.simpleNotify("当前选择的group已保存成功");
                }
            }
            jDialog.dispose();

        }
    }
    class SaveFirstGroup extends AnAction implements DumbAware {

        private final Consumer consumer;

        public SaveFirstGroup(Consumer consumer) {
            super("save first group");
            this.consumer = consumer;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            consumer.accept(e);
            NotificationUtils.simpleNotify("save first group success");
        }
    }
}
