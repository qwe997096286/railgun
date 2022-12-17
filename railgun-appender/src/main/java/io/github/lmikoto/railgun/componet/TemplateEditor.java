package io.github.lmikoto.railgun.componet;

import com.intellij.ui.JBSplitter;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

/**
 * @author liuyang
 * 2021/3/7 6:31 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TemplateEditor extends JPanel implements ComponentListener {
    private JPanel contentPanel;
    private JTextArea textArea;
    private JButton render;
    private JComboBox<String> comboBox1;
    private JScrollPane scroll;
    private Map<String,RenderCode> renderActionMap;
    private CodeTemplate currentTemplate;
    private Integer leftComponentWidth;
    public TemplateEditor() {

//        RenderSql2Config action2 = ;
//        RenderEntity2Select action3 = ;
//        renderActionMap.put(action1.getRenderType(), action1);
//        renderActionMap.put(action2.getRenderType(), action1);
//        renderActionMap.put(action3.getRenderType(), action1);
//        this.renderCode = renderCode;
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
        render.addActionListener(actionEvent -> {
            String text = textArea.getText();
            RenderCode renderCode = renderActionMap.get(comboBox1.getSelectedItem());
            if (renderCode instanceof SetCurTemplate) {
                ((SetCurTemplate) renderCode).setTemplate(this.currentTemplate);
            }
            renderCode.execute(text);
            NotificationUtils.simpleNotify("模版已处理完成。");
        });
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
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Container parent = TemplateEditor.this.getParent().getParent();
        TemplateEditor.this.scroll.setPreferredSize(new Dimension(parent.getWidth() -((JBSplitter) parent).getFirstComponent().getWidth() - 30, parent.getHeight() - 50));
        TemplateEditor.this.scroll.updateUI();
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
