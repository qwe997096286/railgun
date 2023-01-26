package io.github.lmikoto.railgun.utils;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dao.FileDao;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/31 19:56
 */
@Data
@Slf4j
public class AppenderUtils {
    private Map<String, RenderCode> renderCodeMap;

    public List<Change> renderShowCode(List<CodeDir> codeDirs, Predicate<CodeTemplate> isTarget) {
        if (CollectionUtils.isEmpty(codeDirs)) {
            NotificationUtils.simpleNotify("请输入代码目录");
            return null;
        }
        if (!Optional.ofNullable(DataCenter.getCurrentGroup()).map(CodeGroup::getVelocityContext).map(map ->
                map.get("po")).isPresent() && codeDirs.stream().anyMatch(codeDir -> hasVMTemplate(codeDir, isTarget))) {
            NotificationUtils.simpleNotify("未添加类配置");
            return null;
        }
        List<CodeTemplate> templates = codeDirs.stream().filter(dir -> CollectionUtils.isNotEmpty(dir.getTemplates()))
                .peek(dir -> {
                    List<CodeTemplate> dirTemplates = dir.getTemplates();
                    if (CollectionUtils.isNotEmpty(dirTemplates)) {
                        dirTemplates.forEach(temple -> {
                            temple.setDir(dir.getName());
                        });
                    }
                })
                .flatMap(dir -> dir.getTemplates().stream()).filter(isTarget::test).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(templates)) {
            NotificationUtils.simpleNotify("未启用任何vm模版");
            return null;
        }
        List<Change> changes = Lists.newArrayListWithExpectedSize(templates.size());
        for (CodeTemplate codeTemplate : templates) {
            RenderCode renderCode = this.renderCodeMap.get(codeTemplate.getType());
            if (renderCode instanceof SetCurTemplate) {
                ((SetCurTemplate) renderCode).setTemplate(codeTemplate);
            }
            List<CodeRenderTabDto> tabDtoList = renderCode.execute(codeTemplate.getContent());
            if (CollectionUtils.isEmpty(tabDtoList)) {
                continue;
            }
            trans2Changes(changes, codeTemplate, tabDtoList);
        }
        return changes;

    }

    private static void trans2Changes(List<Change> changes, CodeTemplate codeTemplate, List<CodeRenderTabDto> tabDtoList) {
        for (CodeRenderTabDto codeRenderTabDto : tabDtoList) {

            FilePath filePath = VcsUtil.getFilePath(codeTemplate.getDir() + File.separator + codeRenderTabDto.getTabName());
            SimpleContentRevision simpleContentRevision = new SimpleContentRevision(codeRenderTabDto.getTabContent(), filePath,
                    VcsRevisionNumber.NULL.asString());
            ContentRevision currentRevision = CurrentContentRevision.create(filePath);
            Change change = null;
            if (filePath.getIOFile().exists()) {
                change = new Change(simpleContentRevision, currentRevision);
            } else {
                FileDao.saveFile(filePath.getIOFile(), simpleContentRevision.getContent());
                @Nullable VirtualFile fileDirPath = VcsUtil.getFilePath(codeTemplate.getDir()).getVirtualFile();
                if (!fileDirPath.exists()) {
                    NotificationUtils.simpleNotify("未找到指定目录");
                    continue;
                }
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        FileDocumentManager.getInstance().reloadFiles(StandardFileSystems.local().findFileByPath(
                                fileDirPath.findOrCreateChildData(LocalFileSystem.getInstance(), codeRenderTabDto.getTabName())
                                        .getPath()));
                    } catch (IOException e) {
                        log.error(Throwables.getStackTraceAsString(e));
                    }
                });
                change = new Change(null, currentRevision);
            }
            changes.add(new ChangeListChange(change, "guide", "guide"));
        }
    }

    private boolean hasVMTemplate(CodeDir codeDir, Predicate<CodeTemplate> isTarget) {
        if (CollectionUtils.isEmpty(codeDir.getTemplates())) {
            return false;
        }
        String vm2file = TemplateDict.VM2FILE;
        return codeDir.getTemplates().stream().filter(isTarget::test).map(CodeTemplate::getType)
                .anyMatch(vm2file::equals);
    }
}
