package io.github.lmikoto.railgun.configurable.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.ui.treeStructure.Tree;
import io.github.lmikoto.railgun.configurable.CodeDir;
import io.github.lmikoto.railgun.configurable.CodeGroup;
import io.github.lmikoto.railgun.configurable.CodeTemplate;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
        groupList = Lists.newArrayList();
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
        Object userObject = selectPath.getUserObject();
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
            List<CodeDir> dirs = codeGroup.getDirs();
            if (CollectionUtils.isNotEmpty(dirs)) {
                dirs.remove(node.getUserObject());
            }
        } else if (userObject instanceof CodeDir) {
            CodeDir codeDir = (CodeDir) userObject;
            List<CodeTemplate> templates = codeDir.getTemplates();
            if (CollectionUtils.isNotEmpty(templates)) {
                templates.remove(node.getUserObject());
            }
        } else {
            groupList.remove(userObject);
        }
        parent.remove(node);
        saveTree();
    }


    protected boolean saveTree() {
        File dataFile = new File( "./saveData/auto_data.text");
        ObjectOutputStream objectOutputStream = null;
        try {
            if (!dataFile.exists()) {
                File dir = new File("./saveData/");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                dataFile.createNewFile();
            }
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(dataFile));
            objectOutputStream.writeObject(groupList);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
