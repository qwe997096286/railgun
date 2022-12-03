package io.github.lmikoto.railgun.action;

import com.google.common.collect.Lists;
import com.intellij.ui.treeStructure.Tree;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author liuyang
 * 2021/3/7 7:46 下午
 */
public abstract class BaseTemplateAction {


    private final Tree templateTree;

    @Setter
    @Getter
    private java.util.List<CodeGroup> groupList;
    public BaseTemplateAction(TemplateConfigurable configurable){
        templateTree = configurable.getTemplateTree();
        groupList = configurable.getCodeGroups();
    }

    protected DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) this.templateTree.getLastSelectedPathComponent();
    }

    protected void showDialog(JDialog dialog, int width, int height) {
        dialog.setSize(width, height);
        dialog.setAlwaysOnTop(true);
        dialog.setLocationRelativeTo(null);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private void addNode(DefaultMutableTreeNode pNode, MutableTreeNode newNode){
        pNode.add(newNode);
        DefaultTreeModel model = (DefaultTreeModel) this.templateTree.getModel();
        TreeNode[] nodes = model.getPathToRoot(newNode);
        TreePath path = new TreePath(nodes);
        this.templateTree.scrollPathToVisible(path);
        this.templateTree.updateUI();
    }
    protected void updateUI(){
        this.templateTree.updateUI();
    }

    protected void addGroup(CodeGroup group) {
        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) this.templateTree.getModel().getRoot();
        this.groupList.add(group);
        addNode(treeRoot, new DefaultMutableTreeNode(group));
        saveTree();
    }

    protected void addDir(CodeDir dir, DefaultMutableTreeNode selectPath) {
        Object[] userObjectPath = selectPath.getUserObjectPath();
        CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
        List<CodeDir> dirs = codeGroup.getDirs();
        if (CollectionUtils.isEmpty(dirs)) {
            dirs = Lists.newArrayList();
            codeGroup.setDirs(dirs);
        }
        dirs.add(dir);
        addNode(selectPath, new DefaultMutableTreeNode(dir));
        saveTree();
    }

    protected void addTemplate(CodeTemplate codeTemplate, DefaultMutableTreeNode selectPath) {
        Object[] userObjectPath = selectPath.getUserObjectPath();
        CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
        List<CodeDir> dirs = codeGroup.getDirs();
        CodeDir codeDir = dirs.get(dirs.indexOf(userObjectPath[2]));
        List<CodeTemplate> templates = codeDir.getTemplates();
        if (Objects.isNull(templates)) {
            templates = Lists.newArrayList();
            codeDir.setTemplates(templates);
        }
        templates.add(codeTemplate);
        addNode(selectPath, new DefaultMutableTreeNode(codeTemplate));
        saveTree();
    }

    protected void deleteItem(DefaultMutableTreeNode node) {
        if (!(node.getParent() instanceof DefaultMutableTreeNode)) {
            return;
        }
        DefaultMutableTreeNode parent =(DefaultMutableTreeNode) node.getParent();
        Object userObject = parent.getUserObject();
        if (userObject instanceof CodeGroup) {
            CodeGroup codeGroup = (CodeGroup) userObject;
            groupList.remove(codeGroup);
        } else if (userObject instanceof CodeDir) {
            CodeDir codeDir = (CodeDir) userObject;
            Object[] userObjectPath = node.getUserObjectPath();
            CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
            List<CodeDir> dirs = codeGroup.getDirs();
            dirs.remove(codeDir);
        } else if (userObject instanceof CodeTemplate){
            CodeTemplate template = (CodeTemplate) userObject;
            Object[] userObjectPath = node.getUserObjectPath();
            CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
            List<CodeDir> dirs = codeGroup.getDirs();
            CodeDir codeDir = dirs.get(dirs.indexOf(userObjectPath[2]));
            List<CodeTemplate> templates = codeDir.getTemplates();
            templates.remove(template);
        }
        parent.remove(node);
        saveTree();
    }


    protected boolean saveTree() {
        File dataFile = new File( "./saveData/auto_data.text");
        FileWriter fileWriter = null;
        try {
            if (!dataFile.exists()) {
                File dir = new File("./saveData/");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                dataFile.createNewFile();
            }
            fileWriter = new FileWriter(dataFile);
            fileWriter.write(JsonUtils.toPrettyJson(groupList.get(0)));
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
