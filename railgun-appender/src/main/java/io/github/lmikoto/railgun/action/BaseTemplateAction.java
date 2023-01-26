package io.github.lmikoto.railgun.action;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.treeStructure.Tree;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.ConfigModel;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuyang
 * 2021/3/7 7:46 下午
 */
@Slf4j
public abstract class BaseTemplateAction {


    private final Tree templateTree;

    @Setter
    @Getter
    private List<CodeGroup> groupList;

    public BaseTemplateAction(TemplateConfigurable configurable) {
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

    private void addNode(DefaultMutableTreeNode pNode, MutableTreeNode newNode) {
        pNode.add(newNode);
        DefaultTreeModel model = (DefaultTreeModel) this.templateTree.getModel();
        TreeNode[] nodes = model.getPathToRoot(newNode);
        TreePath path = new TreePath(nodes);
        this.templateTree.scrollPathToVisible(path);
        this.templateTree.updateUI();
    }

    protected void updateUI() {
        this.templateTree.updateUI();
    }

    protected void addGroup(CodeGroup group) {
        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) this.templateTree.getModel().getRoot();
        if (DataCenter.getCurrentGroup() == null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setCodeGroup(group);
        }
        this.groupList.add(group);
        addNode(treeRoot, new DefaultMutableTreeNode(group));
        if ((groupList.size() == 1)) {
            saveTree();
        }
    }

    protected void addDir(CodeDir dir, DefaultMutableTreeNode selectPath) {
        Object[] userObjectPath = selectPath.getUserObjectPath();
        CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
        List<CodeDir> dirs = codeGroup.getDirs();
        if (CollectionUtils.isEmpty(dirs)) {
            dirs = Lists.newArrayList();
            codeGroup.setDirs(dirs);
        }
        Random random = new Random();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        dir.setRegTime(sdf.format(new Date()) + random.nextInt(999));
        dirs.add(dir);
        addNode(selectPath, new DefaultMutableTreeNode(dir));
        if (userObjectPath[1].equals(groupList.get(0))) {
            saveTree();
        }
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
        if (userObjectPath[1].equals(groupList.get(0))) {
            saveTree();
        }
    }

    protected void deleteItem(DefaultMutableTreeNode node) {
        if (!(node.getParent() instanceof DefaultMutableTreeNode)) {
            return;
        }
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        Object userObject = node.getUserObject();
        Object[] userObjectPath = node.getUserObjectPath();
        if (userObject instanceof CodeGroup) {
            CodeGroup codeGroup = (CodeGroup) userObject;
            groupList.remove(codeGroup);
        } else if (userObject instanceof CodeDir) {
            CodeDir codeDir = (CodeDir) userObject;
            CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
            List<CodeDir> dirs = codeGroup.getDirs();
            dirs.remove(codeDir);
        } else if (userObject instanceof CodeTemplate) {
            CodeTemplate template = (CodeTemplate) userObject;
            CodeGroup codeGroup = groupList.get(groupList.indexOf(userObjectPath[1]));
            List<CodeDir> dirs = codeGroup.getDirs();
            CodeDir codeDir = dirs.get(dirs.indexOf(userObjectPath[2]));
            List<CodeTemplate> templates = codeDir.getTemplates();
            templates.remove(template);
        }
        parent.remove(node);
        updateUI();
        if (groupList.size() > 0 && userObjectPath[1].equals(groupList.get(0))) {
            saveTree();
        }
    }


    protected boolean saveTree() {
        return saveTree("." + File.separator + "saveData" + File.separator + "auto_data.text", groupList.get(0));
    }

    protected boolean saveTree(String dirStr, CodeGroup codeGroup) {
        Application application = ApplicationManager.getApplication();
        Computable<Boolean> computation = new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                File dataFile = new File(dirStr);
                FileWriter fileWriter = null;
                try {
                    if (!dataFile.exists()) {
                        File dir = dataFile.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        dataFile.createNewFile();
                    }
                    fileWriter = new FileWriter(dataFile);
                    List<CodeDir> dirs = codeGroup.getDirs();
                    if (CollectionUtils.isNotEmpty(dirs)) {
                        List<CodeTemplate> templates = dirs.stream().map(dir -> {
                                    List<CodeTemplate> setDirTemplate = dir.getTemplates();
                                    if (CollectionUtils.isNotEmpty(setDirTemplate)) {
                                        setDirTemplate.forEach(codeTemplate -> codeTemplate.setDir(dir.getRegTime()));
                                    }
                                    return setDirTemplate;
                                }).filter(Objects::nonNull)
                                .flatMap(List::stream).filter(template -> StringUtils.isNotBlank(template.getContent())).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(templates)) {
                            for (CodeTemplate template : templates) {
                                String path = null;
                                ConfigModel configModel = DataCenter.getConfigModel();
                                String saveDir = Optional.ofNullable(configModel).map(ConfigModel::getSaveFileDir).orElse("./");
                                if (StringUtils.isNotEmpty(template.getDir())) {
                                    path = saveDir + template.getDir() + File.separator;
                                } else {
                                    path = saveDir + "saveData" + File.separator;
                                }
                                File file = new File(path + template.getName());
                                FileOutputStream fos = null;
                                if (!file.exists()) {
                                    File dir = file.getParentFile();
                                    if (!dir.exists()) {
                                        dir.mkdir();
                                    }
                                    file.createNewFile();
                                }
                                fos = new FileOutputStream(file);
                                fos.write(template.getContent().getBytes(StandardCharsets.UTF_8));
                                fos.flush();
                                fos.close();

                            }
                        }
                    }
                    fileWriter.write(JsonUtils.toPrettyJson(codeGroup));
                    return true;
                } catch (FileNotFoundException e) {
                    log.error(Throwables.getStackTraceAsString(e));

                } catch (IOException e) {
                    log.error(Throwables.getStackTraceAsString(e));
                } finally {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }

        };
        application.runWriteAction(computation);
        return computation.get();
    }
}
