package io.github.lmikoto.railgun.componet;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserBase;
import com.intellij.openapi.vcs.changes.ui.DefaultInclusionModel;
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder;
import com.intellij.openapi.vcs.changes.ui.VcsTreeModelData;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.*;

/**
 * @author jinwq
 * @Date 2022/12/27 08:51
 */
public class IChangesBrowser extends ChangesBrowserBase implements Disposable{
        @Nullable
        private Set<String> myChangeListNames;

        private Set<LocalChangeList> changeLists = new HashSet<>();
        public IChangesBrowser(@NotNull Project project, List<? extends LocalChangeList> changeListList) {
            super(project, true, true);
            if (CollectionUtils.isNotEmpty(changeListList)) {
                changeLists.addAll(changeListList);
            }
            myViewer.setInclusionModel(new DefaultInclusionModel(ChangeListChange.HASHING_STRATEGY));
            myViewer.rebuildTree();
        }

        @Override
        public void dispose() {
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

        private class MyChangeListListener extends ChangeListAdapter {
            @NotNull private final MergingUpdateQueue myUpdateQueue =
                    new MergingUpdateQueue("LocalChangesViewer", 300, true,
                            IChangesBrowser.this, IChangesBrowser.this);

            private void doUpdate() {
                myUpdateQueue.queue(new Update("update") {
                    @Override
                    public void run() {
                        myViewer.rebuildTree();
                    }
                });
            }

            @Override
            public void changeListsChanged() {
                doUpdate();
            }
        }
    }