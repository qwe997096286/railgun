package io.github.lmikoto.railgun.configurable.componet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.swing.*;

/**
 * @author liuyang
 * 2021/3/7 6:31 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TemplateEditor extends JPanel{
    private JPanel contentPanel;
    private JTextArea textArea;
}
