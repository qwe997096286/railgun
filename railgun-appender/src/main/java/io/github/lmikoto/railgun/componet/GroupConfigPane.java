package io.github.lmikoto.railgun.componet;

import com.google.common.collect.Lists;
import com.intellij.ui.JBSplitter;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.ConfigModel;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/12 14:32
 */
public class GroupConfigPane extends JScrollPane implements ActionListener, ComponentListener {
    private JPanel contentPanel;
    private JComboBox<Integer> extraBtnCnt;
    private JCheckBox exportCheckBox;
    private JCheckBox importCheckBox;
    private JCheckBox pagingCheckBox;
    private JCheckBox approvalCheckBox;
    private JCheckBox editCheckBox;
    private JCheckBox createCheckBox;
    private JCheckBox delCheckBox;
    private JCheckBox delBatchCheckBox;
    private JButton renderBtn;
    private JButton confBtn;
    private JTextField groupDir;
    private JButton chooseBtn;
    private JButton confFollow;
    private JTextField controllerPackage;
    private JTextField servicePackage;
    private JTextField daoPackage;

    public GroupConfigPane() {
        super();
        getViewport().add(contentPanel);
        extraBtnCnt.addItem(1);
        extraBtnCnt.addItem(2);
        extraBtnCnt.addItem(3);
        extraBtnCnt.addItem(4);
        extraBtnCnt.addItem(5);
        renderBtn.addActionListener(this);
        confBtn.addActionListener(this);
        chooseBtn.addActionListener(this);
        ConfigModel configModel = DataCenter.getConfigModel();
        if (configModel == null) {
            return;
        }
        exportCheckBox.setSelected(configModel.isHasExport());
        importCheckBox.setSelected(configModel.isHasImport());
        pagingCheckBox.setSelected(configModel.isHasPaging());
        approvalCheckBox.setSelected(configModel.isHasApproval());
        editCheckBox.setSelected(configModel.isHasEdit());
        createCheckBox.setSelected(configModel.isHasCreate());
        delCheckBox.setSelected(configModel.isHasDel());
        delBatchCheckBox.setSelected(configModel.isHasDelBatch());
        groupDir.setText(configModel.getGroupDir());
        controllerPackage.setText(configModel.getControllerPackage());
        servicePackage.setText(configModel.getServicePackage());
        daoPackage.setText(configModel.getDaoPackage());
        DataCenter.getCurrentGroup().getVelocityContext().put("config", configModel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(confBtn)) {
            ConfigModel configModel = DataCenter.getConfigModel();
            if (configModel != null) {
                configModel.setHasExport(exportCheckBox.isSelected());
                configModel.setHasImport(importCheckBox.isSelected());
                configModel.setHasPaging(pagingCheckBox.isSelected());
                configModel.setHasApproval(approvalCheckBox.isSelected());
                configModel.setHasEdit(editCheckBox.isSelected());
                configModel.setHasCreate(createCheckBox.isSelected());
                configModel.setHasDel(delCheckBox.isSelected());
                configModel.setHasDelBatch(delBatchCheckBox.isSelected());
                configModel.setGroupDir(groupDir.getText());
                configModel.setControllerPackage(controllerPackage.getText());
                configModel.setServicePackage(servicePackage.getText());
                configModel.setDaoPackage(daoPackage.getText());
                DataCenter.getCurrentGroup().getVelocityContext().put("config", configModel);
            }
        } else if (source.equals(this.renderBtn)) {
            Optional<List<CodeDir>> codeDirs = Optional.ofNullable(DataCenter.getCurrentGroup())
                    .flatMap(group -> Optional.ofNullable(group.getDirs()));
            if (!codeDirs.isPresent()) {
                return;
            }
            List<CodeTemplate> templates = codeDirs.get().stream().filter(dir -> CollectionUtils.isNotEmpty(dir.getTemplates()))
                    .flatMap(dir -> dir.getTemplates().stream()).collect(Collectors.toList());
            List<String> configTemplates = Lists.newArrayList(TemplateDict.ENTITY2CONFIG, TemplateDict.SQL2CONFIG);
            List<CodeTemplate> configs = templates.stream().filter(template -> configTemplates.contains(template.getType()))
                    .collect(Collectors.toList());

        } else if (source.equals(chooseBtn)) {
            JDialog dialog = new JDialog();
            FileDialog openDialog = new FileDialog(dialog, "选择项目目录", FileDialog.LOAD);
            openDialog.setVisible(true);
            groupDir.setText(openDialog.getDirectory());
            openDialog.setVisible(false);
            dialog.setVisible(false);
        } else if (source.equals(confFollow)) {

        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Container parent = this.getParent().getParent();
        this.setPreferredSize(new Dimension(parent.getWidth() -((JBSplitter) parent).getFirstComponent().getWidth() - 30, parent.getHeight() - 20));
        this.updateUI();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
