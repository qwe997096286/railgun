package io.github.lmikoto.railgun.componet;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ListSelection;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListChange;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.ui.*;
import com.intellij.util.containers.ContainerUtil;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * @author jinwq
 * @Date 2022/12/27 08:51
 */
public class IChangesBrowser extends ChangesBrowserBase implements Disposable {
    @Nullable
    private Set<String> myChangeListNames;

    private Set<LocalChangeList> changeLists = new HashSet<>();

    public IChangesBrowser(@NotNull Project project, List<? extends LocalChangeList> changeListList) {
        super(project, true, true);
        if (CollectionUtils.isNotEmpty(changeListList)) {
            changeLists.addAll(changeListList);
        }
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                FileDocumentManager.getInstance().saveAllDocuments();
            }
        });
        myViewer.setInclusionModel(new DefaultInclusionModel(ChangeListChange.HASHING_STRATEGY));
        myViewer.rebuildTree();
    }

    @Override
    public void dispose() {
        System.out.println("11111");
    }

    @NotNull
    @Override
    protected List<AnAction> createDiffActions() {
        return ContainerUtil.append(
                super.createDiffActions()
        );
    }


    @NotNull
    @Override
    protected DefaultTreeModel buildTreeModel() {
        return TreeModelBuilder.buildFromChangeLists(myProject, getGrouping(), this.changeLists, Registry.is("vcs.skip.single.default.changelist"));
    }


    public void setIncludedChanges(@NotNull Collection<? extends Change> changes) {
        List<Change> changesToInclude = new ArrayList<>(changes);

        Set<Change> otherChanges = new HashSet<>();
        for (Change change : changes) {
            if (!(change instanceof ChangeListChange)) {
                otherChanges.add(change);
            }
        }

        // include all related ChangeListChange by a simple Change
        if (!otherChanges.isEmpty()) {
            for (Change change : getAllChanges()) {
                if (change instanceof ChangeListChange &&
                        otherChanges.contains(change)) {
                    changesToInclude.add(change);
                }
            }
        }

        myViewer.setIncludedChanges(changesToInclude);
    }

    @Override
    public void showDiff() {
        ListSelection<Object> selection = VcsTreeModelData.getListSelectionOrAll(myViewer);
        ListSelection<ChangeDiffRequestChain.Producer> producers = selection.map(this::getDiffRequestProducer);
        DiffRequestChain chain = new ChangeDiffRequestChain(producers.getList(), producers.getSelectedIndex());
        updateDiffContext(chain);
        DiffDialogHints hints = new DiffDialogHints(null, this, windowWrapper -> windowWrapper.getWindow()
                .addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        super.windowClosing(e);
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        super.windowClosed(e);
                    }

                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        super.windowLostFocus(e);
                    }
                }));


        DiffManager.getInstance().showDiff(myProject, chain, hints);
    }

    public List<Change> getAllChanges() {
        return VcsTreeModelData.all(myViewer).userObjects(Change.class);
    }

    public List<Change> getSelectedChanges() {
        return VcsTreeModelData.selected(myViewer).userObjects(Change.class);
    }

    public List<Change> getIncludedChanges() {
        return VcsTreeModelData.included(myViewer).userObjects(Change.class);
    }

    public void setChangeLists(@Nullable List<? extends LocalChangeList> changeLists) {
        myChangeListNames = changeLists != null ? ContainerUtil.map2Set(changeLists, LocalChangeList::getName) : null;
        myViewer.rebuildTree();
    }
}