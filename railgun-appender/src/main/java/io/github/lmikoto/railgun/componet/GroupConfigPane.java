package io.github.lmikoto.railgun.componet;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.ui.JBSplitter;
import com.intellij.vcsUtil.VcsUtil;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.*;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.service.impl.RenderVm2file;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/12 14:32
 */
public class GroupConfigPane extends JScrollPane implements ActionListener, ComponentListener {
    private final Appender appender;
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
    private JTextField saveFileDir;
    private JButton saveFileDirBtn;
    private JTextField entityDir;
    private JButton entityDirBtn;
    private JTextField dtoDir;
    private JButton dtoDirBtn;
    @Setter
    private Map<String, RenderCode> renderCodeMap;
    private List<? extends LocalChangeList> changeLists;

    public GroupConfigPane() {
        super();
        getViewport().add(contentPanel);
        this.appender = new Appender();
        extraBtnCnt.addItem(1);
        extraBtnCnt.addItem(2);
        extraBtnCnt.addItem(3);
        extraBtnCnt.addItem(4);
        extraBtnCnt.addItem(5);
        renderBtn.addActionListener(this);
        confBtn.addActionListener(this);
        saveFileDirBtn.addActionListener(this);
        chooseBtn.addActionListener(this);
        dtoDirBtn.addActionListener(this);
        entityDirBtn.addActionListener(this);
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
        saveFileDir.setText(configModel.getSaveFileDir());
        entityDir.setText(configModel.getEntityDir());
        dtoDir.setText(configModel.getDtoDir());
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
                configModel.setSaveFileDir(saveFileDir.getText());
                configModel.setEntityDir(entityDir.getText());
                configModel.setDtoDir(dtoDir.getText());
                DataCenter.getCurrentGroup().getVelocityContext().put("config", configModel);
            }
        } else if (source.equals(this.renderBtn)) {
            this.renderCode();
        } else if (source.equals(chooseBtn)) {
            actionPerformed(groupDir, "选择项目目录");
        } else if (source.equals(saveFileDirBtn)) {
            actionPerformed(saveFileDir, "选择文件保存目录");
        } else if (source.equals(dtoDirBtn)) {
            actionPerformed(dtoDir, "选择dto保存目录");
        } else if (source.equals(entityDirBtn)) {
            actionPerformed(entityDir, "选择entity保存目录");
        } else if (source.equals(confFollow)) {

        }
    }

    private void actionPerformed(JTextField textField, String title) {
        JDialog dialog = new JDialog();
        FileDialog openDialog = new FileDialog(dialog, title, FileDialog.LOAD);
        openDialog.setVisible(true);
        String directory = openDialog.getFile();
        if (StringUtils.isNotEmpty(directory)) {
            textField.setText(openDialog.getDirectory() + directory + "/");
        }
        openDialog.dispose();
        dialog.dispose();
    }

    private void renderCode() {
        Optional<List<CodeDir>> codeDirs = Optional.ofNullable(DataCenter.getCurrentGroup())
                .flatMap(group -> Optional.ofNullable(group.getDirs()));
        if (!codeDirs.isPresent()) {
            return;
        }
        if (!Optional.ofNullable(DataCenter.getCurrentGroup()).map(CodeGroup::getVelocityContext).map(map ->
                map.get("po")).isPresent()) {
            NotificationUtils.simpleNotify("未添加类配置");
            return;
        }
        List<CodeTemplate> templates = codeDirs.get().stream().filter(dir -> CollectionUtils.isNotEmpty(dir.getTemplates()))
                .filter(CodeDir::getEnable).peek(dir -> {
                    List<CodeTemplate> dirTemplates = dir.getTemplates();
                    if (CollectionUtils.isNotEmpty(dirTemplates)) {
                        dirTemplates.forEach(temple -> {
                            temple.setDir(dir.getName());
                        });
                    }
                })
                .flatMap(dir -> dir.getTemplates().stream()).collect(Collectors.toList());
        List<CodeTemplate> vmTemplate = templates.stream().filter(template -> TemplateDict.VM2FILE.equals(template.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(vmTemplate)) {
            NotificationUtils.simpleNotify("未启用任何vm模版");
            return;
        }
        RenderCode renderCode = this.renderCodeMap.get(TemplateDict.VM2FILE);
        @NotNull Project @NotNull [] project = ProjectManager.getInstance().getOpenProjects();
        LocalChangeListImpl.Builder guide = new LocalChangeListImpl.Builder(project[0], "guide");
        List<Change> changes = Lists.newArrayListWithExpectedSize(vmTemplate.size());
        changes.addAll(populateEntity());
        for (CodeTemplate codeTemplate : vmTemplate) {
            if (renderCode instanceof RenderVm2file) {
                ((RenderVm2file) renderCode).setTemplate(codeTemplate);
            }
            List<CodeRenderTabDto> tabDtoList = renderCode.execute(codeTemplate.getContent());
            if (CollectionUtils.isEmpty(tabDtoList)) {
                continue;
            }
            CodeRenderTabDto codeRenderTabDto = tabDtoList.get(0);
            FilePath filePath = VcsUtil.getFilePath(codeTemplate.getDir() + "/" + codeRenderTabDto.getTabName());
            SimpleContentRevision simpleContentRevision = new SimpleContentRevision(codeRenderTabDto.getTabContent(), filePath,
                    VcsRevisionNumber.NULL.asString());
            ContentRevision currentRevision = CurrentContentRevision.create(filePath);
            Change change = null;
            if (filePath.getIOFile().exists()) {
                change = new Change(simpleContentRevision, currentRevision);
            } else {
                change = new Change(null, simpleContentRevision);
            }
            changes.add(new ChangeListChange(change, "guide", "guide"));
        }
        LocalChangeListImpl changeList = guide.setChanges(changes).setId("guide").build();
        changeLists = Collections.singletonList(changeList);
        IChangesBrowser localChangesBrowser = new IChangesBrowser(project[0], changeLists);
        localChangesBrowser.setIncludedChanges(changes);
        localChangesBrowser.selectEntries(changeLists);
        localChangesBrowser.setVisible(true);
        this.add(localChangesBrowser);
        localChangesBrowser.showDiff();
    }

    @SneakyThrows
    private Collection<? extends Change> populateEntity() {
        List<Change> changeList = Lists.newArrayListWithExpectedSize(3);
        Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
        SimpleClass po = (SimpleClass) velocityContext.get("po");
        SimpleClass dto = (SimpleClass) velocityContext.get("dto");
        SimpleClass pk = (SimpleClass) velocityContext.get("pk");
        ConfigModel configModel = DataCenter.getConfigModel();
        if (configModel == null || StringUtils.isEmpty(configModel.getEntityDir())
                || StringUtils.isEmpty(configModel.getDtoDir())) {
            NotificationUtils.simpleNotify("请选配置entity、dto的包路径");
            throw new RuntimeException("请选配置entity、dto的包路径");
        }
        FilePath filePath = VcsUtil.getFilePath(configModel.getEntityDir() + "/" + po.getSimpleName());
        SimpleContentRevision simpleContentRevision = new SimpleContentRevision(appender.process(po, null), filePath,
                VcsRevisionNumber.NULL.asString());
        ContentRevision currentRevision;
        if (!filePath.getIOFile().exists()) {
            filePath.getIOFile().createNewFile();
        }
        currentRevision = CurrentContentRevision.create(filePath);
        Change change;
        if (filePath.getIOFile().exists()) {
            change = new Change(simpleContentRevision, currentRevision);
        } else {
            change = new Change(null, simpleContentRevision);
        }
        changeList.add(change);
        if (pk != null) {
            filePath = VcsUtil.getFilePath(configModel.getEntityDir() + "/" + pk.getSimpleName());
            simpleContentRevision = new SimpleContentRevision(appender.process(pk, null), filePath,
                    VcsRevisionNumber.NULL.asString());
            currentRevision = CurrentContentRevision.create(filePath);
            if (filePath.getIOFile().exists()) {
                change = new Change(simpleContentRevision, currentRevision);
            } else {
                change = new Change(null, simpleContentRevision);
            }
            changeList.add(change);
        }
        filePath = VcsUtil.getFilePath(configModel.getDtoDir() + "/" + dto.getSimpleName());
        simpleContentRevision = new SimpleContentRevision(appender.process(dto, null), filePath,
                VcsRevisionNumber.NULL.asString());
        currentRevision = CurrentContentRevision.create(filePath);
        if (filePath.getIOFile().exists()) {
            change = new Change(simpleContentRevision, currentRevision);
        } else {
            change = new Change(null, simpleContentRevision);
        }
        changeList.add(change);
        return changeList;
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
