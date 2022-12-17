package io.github.lmikoto.railgun.configurable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import io.github.lmikoto.railgun.action.ItemDeleteAction;
import io.github.lmikoto.railgun.action.SaveGroupAction;
import io.github.lmikoto.railgun.action.TemplateAddAction;
import io.github.lmikoto.railgun.componet.GroupConfigPane;
import io.github.lmikoto.railgun.componet.ITabbedPane;
import io.github.lmikoto.railgun.componet.TemplateEditor;
import io.github.lmikoto.railgun.dao.CodeGroupDao;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.service.impl.RenderEntity2Select;
import io.github.lmikoto.railgun.service.impl.RenderSql2Class;
import io.github.lmikoto.railgun.service.impl.RenderSql2Config;
import io.github.lmikoto.railgun.service.impl.RenderVm2file;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private GroupConfigPane groupConfigPane;
    private JSplitPane jSplitPane;
    @Getter
    private List<CodeGroup> codeGroups;
    @Getter
    private DataCenter dataCenter;
    private ITabbedPane jPanel;

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
        groupByFile = Lists.newArrayList();
        this.codeGroups = groupByFile;
        CodeGroup group = CodeGroupDao.getGroup();
        if (group != null) {
            groupByFile.add(group);
            saveData2Tree(groupByFile, templateTree);
        }
        TemplateAddAction action = new TemplateAddAction(this);
        ItemDeleteAction itemDeleteAction = new ItemDeleteAction(this);
        Map<String, RenderCode> renderCodes = createRenderCode();
        templateEditor = new TemplateEditor();
        templateEditor.setRenderActionMap(renderCodes);
        toolbarDecorator = ToolbarDecorator.createDecorator(templateTree)
                .setAddAction(action)
                .setRemoveAction(itemDeleteAction)
                .setEditAction(new SaveGroupAction(this));
//                .setRemoveAction(new TemplateRemoveAction(this))
//                .setEditAction(new TemplateEditAction(this));
        dataCenter = new DataCenter();
        dataCenter.setCodeGroup(group);
        templateTree.addTreeSelectionListener(this::valueChanged);

        //设置group配置面版
        groupConfigPane = new GroupConfigPane();
        JPanel templatesPanel = toolbarDecorator.createPanel();
        templatesPanel.setPreferredSize(JBUI.size(240,100));
        jSplitPane = new JSplitPane();
        jSplitPane.setDividerLocation(240);
        jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane.setContinuousLayout(true);
        jSplitPane.setBorder(BorderFactory.createEmptyBorder());
        jSplitPane.setLeftComponent(templatesPanel);
        // 配置面板右侧
        jPanel = new ITabbedPane();
        jPanel.add("code group", groupConfigPane);
        jPanel.add("code template", templateEditor);
        jPanel.setVisible(true);
        jSplitPane.setRightComponent(jPanel);
        add(jSplitPane, BorderLayout.CENTER);
        this.addComponentListener(jPanel);
        templatesPanel.addComponentListener(jPanel);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(this);

    }

    private Map<String, RenderCode> createRenderCode() {
        Map<String, RenderCode> renderActionMap = Maps.newHashMapWithExpectedSize(4);
        java.util.List<RenderCode> renderCodes = Lists.newArrayList(new RenderSql2Class(), new RenderSql2Config(),
                new RenderEntity2Select(), new RenderVm2file());
        renderCodes.forEach(renderCode -> {
            renderActionMap.put(renderCode.getRenderType(), renderCode);
        });
        return renderActionMap;
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



    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    private void valueChanged(TreeSelectionEvent it) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) templateTree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        Object object = node.getUserObject();
        Object[] userObjectPath = node.getUserObjectPath();
        if (!DataCenter.getCurrentGroup().equals(userObjectPath[1])) {
            this.dataCenter.setCodeGroup(codeGroups.get(codeGroups.indexOf(userObjectPath[1])));
        }
        if (object instanceof CodeTemplate) {

            CodeTemplate codeTemplate = (CodeTemplate) object;
            CodeGroup codeGroup = this.codeGroups.get(this.codeGroups.indexOf(userObjectPath[1]));
            List<CodeDir> dirs = codeGroup.getDirs();
            CodeDir codeDir = dirs.get(dirs.indexOf(userObjectPath[2]));
            List<CodeTemplate> templates = codeDir.getTemplates();
            if (Objects.isNull(templates)) {
                templates = Lists.newArrayList();
                codeDir.setTemplates(templates);
            }
            templateEditor.setCurrentTemplate(templates.get(templates.indexOf(codeTemplate)));
            templateEditor.getComboBox1().setSelectedItem(codeTemplate.getType());
            templateEditor.getTextArea().setText(codeTemplate.getContent());
            jPanel.setVisible(true);
            jPanel.setSelectedComponent(templateEditor);
        } else if (object instanceof CodeGroup) {
            jPanel.setVisible(true);
            jPanel.setSelectedComponent(groupConfigPane);
        } else {
            jPanel.setVisible(false);
        }
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
                if (StringUtils.isNoneBlank(node.getType()) && node.getType().contains("config")) {
                    tempCellRenderer.setOpenIcon(AllIcons.General.Settings);
                    tempCellRenderer.setClosedIcon(AllIcons.General.Settings);
                    tempCellRenderer.setLeafIcon(AllIcons.General.Settings);
                }
                return tempCellRenderer.getTreeCellRendererComponent(tree, node.getName(), selected, expanded, true, row, hasFocus);
            } else {
                String text = (String) obj;
                DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
                return tempCellRenderer.getTreeCellRendererComponent(tree, text, selected, expanded, false, row, hasFocus);
            }
        }
    }
}
