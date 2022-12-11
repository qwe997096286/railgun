package io.github.lmikoto.railgun.componet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.service.impl.RenderEntity2Select;
import io.github.lmikoto.railgun.service.impl.RenderSql2Class;
import io.github.lmikoto.railgun.service.impl.RenderSql2Config;
import io.github.lmikoto.railgun.service.impl.RenderVm2file;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

/**
 * @author liuyang
 * 2021/3/7 6:31 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TemplateEditor extends JPanel {
    private JPanel contentPanel;
    private JTextArea textArea;
    private JButton render;
    private JComboBox<String> comboBox1;
    private Map<String,RenderCode> renderActionMap;
    private CodeTemplate currentTemplate;
    public TemplateEditor() {
        this.renderActionMap = Maps.newHashMapWithExpectedSize(3);
        java.util.List<RenderCode> renderCodes = Lists.newArrayList(new RenderSql2Class(), new RenderSql2Config(),
                new RenderEntity2Select(), new RenderVm2file());
        renderCodes.forEach(renderCode -> {
            renderActionMap.put(renderCode.getRenderType(), renderCode);
        });
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
            System.out.println(l.getActionCommand());
        });
        render.addActionListener(actionEvent -> {
            String text = textArea.getText();
            RenderCode renderCode = renderActionMap.get(currentTemplate.getType());
            if (renderCode instanceof SetCurTemplate) {
                ((SetCurTemplate) renderCode).setTemplate(this.currentTemplate);
            }
            renderCode.execute(text);
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
        this.textArea.setText(currentTemplate.getContent());
        this.currentTemplate = currentTemplate;
    }
}
