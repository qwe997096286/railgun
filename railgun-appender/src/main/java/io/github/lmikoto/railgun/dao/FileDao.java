package io.github.lmikoto.railgun.dao;

import com.google.common.base.Throwables;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.vcsUtil.VcsUtil;
import io.github.lmikoto.railgun.componet.IChangesBrowser;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * @author jinwq
 * @Date 2022/12/24 18:51
 */
@Slf4j
public class FileDao {
    @SneakyThrows
    public static void saveFile(File file, String content) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            if (file.exists()) {
                NotificationUtils.simpleNotify("文件已存在请自行将代码拷入");
                showCompareBrowser(file, content);
                return;
            }
            FileOutputStream fileOutputStream = null;
            try {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));

            } catch (FileNotFoundException e) {
                log.error(Throwables.getStackTraceAsString(e));
            } catch (IOException e) {
                log.error(Throwables.getStackTraceAsString(e));
            } finally {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.error(Throwables.getStackTraceAsString(e));
                }
            }
        });
    }

    private static void showCompareBrowser(File file, String content) {
        FilePath filePath = VcsUtil.getFilePath(file);
        SimpleContentRevision simpleContentRevision = new SimpleContentRevision(content, filePath,
                VcsRevisionNumber.NULL.asString());
        ContentRevision currentRevision = CurrentContentRevision.create(filePath);
        Change change = new Change(simpleContentRevision, currentRevision);
        @NotNull Project @NotNull [] project = ProjectManager.getInstance().getOpenProjects();
        LocalChangeListImpl.Builder guide = new LocalChangeListImpl.Builder(project[0], "guide");
        List<Change> changes = Collections.singletonList(change);
        LocalChangeListImpl changeList = guide.setChanges(changes).setId("guide").build();
        List<LocalChangeListImpl> changeLists = Collections.singletonList(changeList);
        IChangesBrowser localChangesBrowser = new IChangesBrowser(project[0], changeLists);

        localChangesBrowser.setIncludedChanges(changes);
        localChangesBrowser.selectEntries(changeLists);
        localChangesBrowser.showDiff();

    }
}
