package io.github.lmikoto.railgun.componet;

import io.github.lmikoto.railgun.entity.CodeDir;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author jinwq
 * @Date 2022/12/27 17:03
 */
public class DirConfigPane extends JPanel {
    private JPanel contentPane;
    private JCheckBox enable;
    private CodeDir currDir;

    public DirConfigPane() {
        add(contentPane);
        enable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currDir.setEnable(enable.isSelected());
            }
        });
    }

    public void setCurrDir(CodeDir codeDir) {
        this.currDir = codeDir;
        enable.setSelected(codeDir.getEnable());
    }
}
