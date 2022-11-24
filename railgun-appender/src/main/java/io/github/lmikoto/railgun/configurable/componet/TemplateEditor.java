package io.github.lmikoto.railgun.configurable.componet;

import io.github.lmikoto.railgun.configurable.action.RenderClassesAction;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.sql.DefaultParser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;

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
    private DefaultParser parser;
    private RenderClassesAction renderClassesAction;
    public TemplateEditor() {
        renderClassesAction = new RenderClassesAction();
        setLayout(new BorderLayout());
        this.parser = new DefaultParser();
        super.add(contentPanel, BorderLayout.CENTER);
////        配置编辑器子面版
//        JPanel jPanel = new JPanel();
//        jPanel.setLayout(new BorderLayout());
//        this.setContentPanel(jPanel);
//        this.add(jPanel, BorderLayout.CENTER);
////        配置编辑器
//        JTextArea templateInput = new JTextArea("模版");
//        templateInput.setBackground(Color.LIGHT_GRAY);
//        this.setTextArea(templateInput);
//        this.getContentPanel().add(templateInput, BorderLayout.CENTER);
////        配置按钮
//        JButton render = new JButton("Render");
        render.addActionListener(actionEvent -> {
            String text = textArea.getText();
            java.util.List<Table> tables = parser.parseSQLs(text);
            renderClassesAction.setTables(tables);
            renderClassesAction.run(null);
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
