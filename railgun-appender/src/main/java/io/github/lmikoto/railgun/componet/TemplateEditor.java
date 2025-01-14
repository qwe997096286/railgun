package io.github.lmikoto.railgun.componet;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.LocalChangeListImpl;
import com.intellij.ui.JBSplitter;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.AppenderUtils;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author liuyang
 * 2021/3/7 6:31 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class TemplateEditor extends JPanel implements ComponentListener {
    private JPanel contentPanel;
    private JTextArea textArea;
    private JComboBox<String> comboBox1;
    private JScrollPane scroll;
    private JButton generate;
    private JButton preview;
    private Map<String, RenderCode> renderActionMap;
    private CodeTemplate currentTemplate;
    private AppenderUtils appenderUtils;

    public TemplateEditor() {

        setLayout(new BorderLayout());
        super.add(contentPanel, BorderLayout.CENTER);
        this.comboBox1.addItem(TemplateDict.SQL2CLASS);
        this.comboBox1.addItem(TemplateDict.SQL2CONFIG);
        this.comboBox1.addItem(TemplateDict.ENTITY2CONFIG);
        this.comboBox1.addItem(TemplateDict.ENTITY2SELECT);
        this.comboBox1.addItem(TemplateDict.VM2FILE);
        this.comboBox1.addActionListener(l -> {
            if (currentTemplate != null) {
                currentTemplate.setType(comboBox1.getSelectedItem().toString());
            }
        });
        preview.addActionListener(actionEvent -> {
            String text = textArea.getText();
            RenderCode renderCode = renderActionMap.get(comboBox1.getSelectedItem());
            if (renderCode instanceof SetCurTemplate) {
                ((SetCurTemplate) renderCode).setTemplate(this.currentTemplate);
            }
            List<CodeRenderTabDto> tabDtos = renderCode.execute(text);
            if (CollectionUtils.isNotEmpty(tabDtos)) {
                RenderCodeView renderCodeView = new RenderCodeView(tabDtos);
                renderCodeView.setSize(800, 600);
                renderCodeView.setVisible(true);
                renderCodeView.setTitle("code generated");
            }
            NotificationUtils.simpleNotify("模版已处理完成。");
        });
        this.generate.addActionListener(actionEvent -> {
            String text = textArea.getText();
            currentTemplate.setContent(text);

            Optional<List<CodeDir>> codeDirs = Optional.ofNullable(DataCenter.getCurrentGroup()).map(CodeGroup::getDirs);
            if (codeDirs.isPresent() && !codeDirs.get().isEmpty()) {
                for (CodeDir codeDir : codeDirs.get()) {
                    if (CollectionUtils.isNotEmpty(codeDir.getTemplates()) && codeDir.getTemplates()
                            .stream().anyMatch(temp -> temp == currentTemplate)) {
                        List<Change> changeList = appenderUtils.renderShowCode(Collections.singletonList(codeDir), temp ->
                                temp == currentTemplate);
                        if (CollectionUtils.isEmpty(changeList)) {
                            break;
                        }
                        @NotNull Project @NotNull [] project = ProjectManager.getInstance().getOpenProjects();
                        LocalChangeListImpl.Builder guide = new LocalChangeListImpl.Builder(project[0], "guide");
                        LocalChangeListImpl changeListImpl = guide.setChanges(changeList).setId("guide").build();
                        List<LocalChangeListImpl> changeLists = Collections.singletonList(changeListImpl);
                        IChangesBrowser localChangesBrowser = new IChangesBrowser(project[0], changeLists);
                        localChangesBrowser.setVisible(true);
                        localChangesBrowser.showDiff();
                        break;
                    }
                }
            }
        });
        this.textArea.setEditable(false);
        //模型数据绑定
        this.textArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                TemplateEditor.this.currentTemplate.setContent(TemplateEditor.this.textArea.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        this.textArea.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                TemplateEditor.this.currentTemplate.setContent(TemplateEditor.this.textArea.getText());
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {

            }
        });
    }
    public TemplateEditor(String type) {
        this();
        this.comboBox1.setVisible(false);
        CodeTemplate codeTemplate = new CodeTemplate();
        codeTemplate.setType(type);
        this.setCurrentTemplate(codeTemplate);
    }
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public void setCurrentTemplate(CodeTemplate currentTemplate) {
        this.currentTemplate = currentTemplate;
        this.textArea.setEditable(true);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Container parent = this.getParent().getParent();
        Container grandParent = this.getRootPane();

        int width = grandParent.getWidth() - ((JBSplitter) parent)
                .getFirstComponent().getWidth() - this.getX() - 210;
        log.info("编辑器滚动面板宽度{}", width);
        this.scroll.setPreferredSize(new Dimension(width, grandParent.getHeight() - 230));
        this.scroll.updateUI();
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
