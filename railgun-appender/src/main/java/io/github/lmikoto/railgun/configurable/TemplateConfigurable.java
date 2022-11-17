package io.github.lmikoto.railgun.configurable;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import io.github.lmikoto.railgun.configurable.action.ItemDeleteAction;
import io.github.lmikoto.railgun.configurable.action.TemplateAddAction;
import io.github.lmikoto.railgun.configurable.componet.TemplateEditor;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liuyang
 * 2021/3/7 5:57 下午
 */
public class TemplateConfigurable extends JBPanel implements Configurable{

    private final static Logger logger = LoggerFactory.getLogger(TemplateConfigurable.class);
    @Getter
    private Tree templateTree;

    private ToolbarDecorator toolbarDecorator;

    private TemplateEditor templateEditor;

    private JSplitPane jSplitPane;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "模版配置";
    }

    @Override
    public @Nullable JComponent createComponent() {
        init();
        return this;
    }

    private void init() {
        File dataFile = new File( "./saveData/auto_data.text");

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // init template tree
        templateTree = new Tree();
//        templateTree.putClientProperty("JTree.lineStyle", "Horizontal");
        templateTree.setRootVisible(false);
        templateTree.setShowsRootHandles(true);
        templateTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        templateTree.setCellRenderer(new TemplateTreeCellRenderer());
        List<CodeGroup> groupByFile = null;
        if (dataFile.exists()) {
            groupByFile = getTreeByFile(dataFile);
            saveData2Tree(groupByFile, templateTree);
        } else {
            groupByFile = Lists.newArrayList();
        }
        TemplateAddAction action = new TemplateAddAction(this);
        action.setGroupList(groupByFile);
        ItemDeleteAction itemDeleteAction = new ItemDeleteAction(this);
        itemDeleteAction.setGroupList(groupByFile);
        toolbarDecorator = ToolbarDecorator.createDecorator(templateTree)
                .setAddAction(action)
                .setRemoveAction(itemDeleteAction);
//                .setRemoveAction(new TemplateRemoveAction(this))
//                .setEditAction(new TemplateEditAction(this));

        templateTree.addTreeSelectionListener( it -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) templateTree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            Object object = node.getUserObject();
            if(object instanceof CodeTemplate) {
                templateEditor.getContentPanel().setVisible(true);
//                templateEditor.refresh(template);
            } else {
                templateEditor.getContentPanel().setVisible(false);
            }
        });



        JPanel templatesPanel = toolbarDecorator.createPanel();
        templatesPanel.setPreferredSize(JBUI.size(240,100));
        templateEditor = constructEditor();
        jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(240);
        jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setContinuousLayout(true);
        jSplitPane.setBorder(BorderFactory.createEmptyBorder());
        jSplitPane.setLeftComponent(templatesPanel);
        jSplitPane.setRightComponent(templateEditor);
        add(jSplitPane, BorderLayout.CENTER);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(this);

    }

    private void saveData2Tree(List<CodeGroup> groupByFile, Tree templateTree) {
        if (CollectionUtils.isEmpty(groupByFile)) {
            return;
        }
        DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) this.templateTree.getModel().getRoot();
        groupByFile.forEach(group -> {
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
            addNode(treeRoot, groupNode);
            if (CollectionUtils.isNotEmpty(group.getDirs())) {
                group.getDirs().forEach(dir -> {
                    DefaultMutableTreeNode dirNode = new DefaultMutableTreeNode(dir);
                    addNode(groupNode, dirNode);
                    List<CodeTemplate> templates = dir.getTemplates();
                    if (CollectionUtils.isNotEmpty(templates)) {
                        templates.forEach( template -> addNode(dirNode, new DefaultMutableTreeNode(template)));
                    }
                });
            }
        });
    }

    private void addNode(DefaultMutableTreeNode pNode, MutableTreeNode newNode){
        pNode.add(newNode);
        DefaultTreeModel model = (DefaultTreeModel) this.templateTree.getModel();
        TreeNode[] nodes = model.getPathToRoot(newNode);
        TreePath path = new TreePath(nodes);
        this.templateTree.scrollPathToVisible(path);
        this.templateTree.updateUI();
    }
    private java.util.List<CodeGroup> getTreeByFile(File dataFile) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFile));
            Object object = objectInputStream.readObject();
            if (object instanceof java.util.List) {
                java.util.List list = (java.util.List) object;
                ArrayList<CodeGroup> codeGroupList = Lists.newArrayListWithExpectedSize(list.size());
                list.forEach(obj -> {
                    CodeGroup codeGroup = obj instanceof CodeGroup ? (CodeGroup) obj : null;
                    if (Objects.nonNull(codeGroup)) {
                        codeGroupList.add(codeGroup);
                    }
                });
                return codeGroupList;
            }
        } catch (Exception e) {
            logger.error(Throwables.getStackTraceAsString(e));
        }

        return Lists.newArrayList();
    }

    private TemplateEditor constructEditor() {
        JTextArea templateInput = new JTextArea("模版");
        TemplateEditor editor = new TemplateEditor();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());
        templateInput.setBackground(Color.LIGHT_GRAY);
        editor.setLayout(new BorderLayout());
        editor.setTextArea(templateInput);
        editor.setContentPanel(jPanel);
        editor.add(jPanel, BorderLayout.CENTER);
        editor.getContentPanel().add(templateInput, BorderLayout.CENTER);
        return editor;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    public class TemplateTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean isLeaf, int row,boolean hasFocus) {
            if (selected) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object obj = treeNode.getUserObject();
            if (obj instanceof CodeGroup) {
                CodeGroup node = (CodeGroup) obj;
                DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
                tempCellRenderer.setOpenIcon(AllIcons.Nodes.JavaModule);
                tempCellRenderer.setClosedIcon(AllIcons.Nodes.JavaModule);
                tempCellRenderer.setLeafIcon(AllIcons.Nodes.JavaModule);
                return tempCellRenderer.getTreeCellRendererComponent(tree, node.getName(), selected, expanded, false, row, hasFocus);
            }
            else if (obj instanceof CodeDir) {
                CodeDir group = (CodeDir) obj;
                DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
                tempCellRenderer.setOpenIcon(AllIcons.Nodes.Folder);
                tempCellRenderer.setClosedIcon(AllIcons.Nodes.Folder);
                tempCellRenderer.setLeafIcon(AllIcons.Nodes.Folder);
                return tempCellRenderer.getTreeCellRendererComponent(tree, group.getName(), selected, expanded, false, row, hasFocus);
            }
            else if (obj instanceof CodeTemplate) {
                CodeTemplate node = (CodeTemplate) obj;
                DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
                return tempCellRenderer.getTreeCellRendererComponent(tree, node.getName(), selected, expanded, true, row, hasFocus);
            } else {
                String text = (String) obj;
                DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
                return tempCellRenderer.getTreeCellRendererComponent(tree, text, selected, expanded, false, row, hasFocus);
            }
        }
    }
}
