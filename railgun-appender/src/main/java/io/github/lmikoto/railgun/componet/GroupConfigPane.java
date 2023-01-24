package io.github.lmikoto.railgun.componet;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.vcsUtil.VcsUtil;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dao.FileDao;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.ConfigModel;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.SimpleField;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.*;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/12 14:32
 */
@Slf4j
public class GroupConfigPane extends JScrollPane implements ActionListener, ComponentListener {
    private final Appender appender;
    private PlaceholderTextField comp;
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
    private JPanel authorPane;
    private JCheckBox powerCheckBox;
    @Setter
    private Map<String, RenderCode> renderCodeMap;
    private List<? extends LocalChangeList> changeLists;
    private AppenderUtils appenderUtils;

    public GroupConfigPane() {
        super();
        getViewport().add(contentPanel);
        this.appender = new Appender();
        extraBtnCnt.addItem(1);
        extraBtnCnt.addItem(2);
        extraBtnCnt.addItem(3);
        extraBtnCnt.addItem(4);
        extraBtnCnt.addItem(5);
        this.comp = new PlaceholderTextField();
        this.comp.setPlaceholder("请输入作者");
        authorPane.add(comp);
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
        setConfig(configModel);
    }

    public void setConfig(ConfigModel configModel) {
        exportCheckBox.setSelected(configModel.isHasExport());
        importCheckBox.setSelected(configModel.isHasImport());
        pagingCheckBox.setSelected(configModel.isHasPaging());
        approvalCheckBox.setSelected(configModel.isHasApproval());
        editCheckBox.setSelected(configModel.isHasEdit());
        createCheckBox.setSelected(configModel.isHasCreate());
        delCheckBox.setSelected(configModel.isHasDel());
        delBatchCheckBox.setSelected(configModel.isHasDelBatch());
        powerCheckBox.setSelected(configModel.isHasDelBatch());
        groupDir.setText(configModel.getGroupDir());
        saveFileDir.setText(configModel.getSaveFileDir());
        entityDir.setText(configModel.getEntityDir());
        dtoDir.setText(configModel.getDtoDir());
        this.comp.setText(configModel.getAuthor());
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
                configModel.setHasPower(powerCheckBox.isSelected());
                configModel.setGroupDir(groupDir.getText());
                configModel.setSaveFileDir(saveFileDir.getText());
                configModel.setEntityDir(entityDir.getText());
                configModel.setDtoDir(dtoDir.getText());
                configModel.setAuthor(comp.getText());
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
            textField.setText(openDialog.getDirectory() + directory + File.separator);
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
        List<CodeDir> dirs = codeDirs.get().stream().filter(dir -> CollectionUtils.isNotEmpty(dir.getTemplates()))
                .filter(CodeDir::getEnable).collect(Collectors.toList());

        List<Change> changes = appenderUtils.renderShowCode(dirs, temp -> TemplateDict.VM2FILE.equals(temp.getType()));
        @NotNull Project @NotNull [] project = ProjectManager.getInstance().getOpenProjects();
        LocalChangeListImpl.Builder guide = new LocalChangeListImpl.Builder(project[0], "guide");
        List<Change> objChanges = populateEntity();
        objChanges.addAll(changes);
        LocalChangeListImpl changeList = guide.setChanges(objChanges).setId("guide").build();
        changeLists = Collections.singletonList(changeList);
        IChangesBrowser localChangesBrowser = new IChangesBrowser(project[0], changeLists);
        localChangesBrowser.setIncludedChanges(changes);
        localChangesBrowser.selectEntries(changeLists);
        localChangesBrowser.setVisible(true);
        localChangesBrowser.showDiff();
    }

    @SneakyThrows
    private List<Change> populateEntity() {
        List<Change> changeList = Lists.newArrayListWithExpectedSize(3);
        Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
        SimpleClass po = (SimpleClass) velocityContext.get("po");
        SimpleClass dto = (SimpleClass) velocityContext.get("dto");
        JavaConvertUtils.populateImport(dto);
        JavaConvertUtils.populatePageConst(dto);
        SimpleField pk = (SimpleField) velocityContext.get("pk") ;
        ConfigModel configModel = DataCenter.getConfigModel();
        if (configModel == null || StringUtils.isEmpty(configModel.getEntityDir())
                || StringUtils.isEmpty(configModel.getDtoDir())) {
            NotificationUtils.simpleNotify("请选配置entity、dto的包路径");
            throw new RuntimeException("请选配置entity、dto的包路径");
        }
        if (po == null) {
            NotificationUtils.simpleNotify("请选生成类配置");
            return Collections.emptyList();
        }
        FilePath filePath = VcsUtil.getFilePath(configModel.getEntityDir() + File.separator + po.getSimpleName() + ".java");
        SimpleContentRevision simpleContentRevision = new SimpleContentRevision(appender.process(po, null), filePath,
                VcsRevisionNumber.NULL.asString());
        ContentRevision currentRevision;
        currentRevision = CurrentContentRevision.create(filePath);
        Change change;
        if (filePath.getIOFile().exists()) {
            change = new Change(simpleContentRevision, currentRevision);
        } else {
            FileDao.saveFile(filePath.getIOFile(), simpleContentRevision.getContent());
            @Nullable VirtualFile fileDirPath = VcsUtil.getFilePath(configModel.getEntityDir()).getVirtualFile();
            try {
                if (!fileDirPath.exists()) {
                    NotificationUtils.simpleNotify("未找到指定目录");
                }
                FileDocumentManager.getInstance().reloadFiles(StandardFileSystems.local().findFileByPath(
                        fileDirPath.findOrCreateChildData(LocalFileSystem.getInstance(), po.getSimpleName() + ".java")
                                .getPath()));
            } catch (IOException e) {
                log.error(Throwables.getStackTraceAsString(e));
            }
            change = new Change(null, currentRevision);
        }
        changeList.add(change);
        if (pk != null) {
            filePath = VcsUtil.getFilePath(configModel.getEntityDir() + File.separator + pk.getClazz().getSimpleName() + ".java");
            simpleContentRevision = new SimpleContentRevision(appender.process(pk.getClazz(), null), filePath,
                    VcsRevisionNumber.NULL.asString());
            currentRevision = CurrentContentRevision.create(filePath);
            if (filePath.getIOFile().exists()) {
                change = new Change(simpleContentRevision, currentRevision);
            } else {
                FileDao.saveFile(filePath.getIOFile(), simpleContentRevision.getContent());
                @Nullable VirtualFile fileDirPath = VcsUtil.getFilePath(configModel.getEntityDir()).getVirtualFile();
                try {
                    if (!fileDirPath.exists()) {
                        NotificationUtils.simpleNotify("未找到指定目录");
                    }
                    FileDocumentManager.getInstance().reloadFiles(StandardFileSystems.local().findFileByPath(
                            fileDirPath.findOrCreateChildData(LocalFileSystem.getInstance(), pk.getSimpleName()
                                            + ".java").getPath()));
                } catch (IOException e) {
                    log.error(Throwables.getStackTraceAsString(e));
                }
                change = new Change(null, currentRevision);
            }
            changeList.add(change);
        }
        filePath = VcsUtil.getFilePath(configModel.getDtoDir() + File.separator + dto.getSimpleName() + ".java");
        simpleContentRevision = new SimpleContentRevision(appender.process(dto, null), filePath,
                VcsRevisionNumber.NULL.asString());
        currentRevision = CurrentContentRevision.create(filePath);
        if (filePath.getIOFile().exists()) {
            change = new Change(simpleContentRevision, currentRevision);
        } else {
            FileDao.saveFile(filePath.getIOFile(), simpleContentRevision.getContent());
            @Nullable VirtualFile fileDirPath = VcsUtil.getFilePath(configModel.getDtoDir()).getVirtualFile();
            try {
                if (!fileDirPath.exists()) {
                    NotificationUtils.simpleNotify("未找到指定目录");
                }
                FileDocumentManager.getInstance().reloadFiles(StandardFileSystems.local().findFileByPath(
                        fileDirPath.findOrCreateChildData(LocalFileSystem.getInstance(), dto.getSimpleName()
                                        + ".java").getPath()));
            } catch (IOException e) {
                log.error(Throwables.getStackTraceAsString(e));
            }
            change = new Change(null, currentRevision);
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

    public void setAppenderUtils(AppenderUtils appenderUtils) {
        this.appenderUtils = appenderUtils;
    }

    public AppenderUtils getAppenderUtils() {
        return appenderUtils;
    }
}
