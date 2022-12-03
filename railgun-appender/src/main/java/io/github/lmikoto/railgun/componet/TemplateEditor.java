package io.github.lmikoto.railgun.componet;

import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.service.impl.RenderEntity2Select;
import io.github.lmikoto.railgun.service.impl.RenderSql2Class;
import io.github.lmikoto.railgun.service.impl.RenderSql2Config;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
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
        List<RenderCode> renderCodes = Lists.newArrayList(new RenderSql2Class(), new RenderSql2Config(),
                new RenderEntity2Select());
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
        render.addActionListener(actionEvent -> {
            String text = textArea.getText();
            renderCode.execute(text);
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
