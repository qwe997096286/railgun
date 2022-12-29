package io.github.lmikoto.railgun.componet;

import io.github.lmikoto.railgun.entity.CodeDir;

import javax.swing.*;

/**
 * @author jinwq
 * @Date 2022/12/27 17:03
 */
public class DirConfigPane extends JPanel{
    private JPanel contentPane;
    private JCheckBox enable;
    private CodeDir currDir;
    public DirConfigPane() {
        add(contentPane);
        enable.addChangeListener(l -> {
            currDir.setEnable(enable.isSelected());
        });
    }
    public void setCurrDir(CodeDir codeDir) {
        this.currDir = codeDir;
        enable.setSelected(codeDir.getEnable());
    }
}
