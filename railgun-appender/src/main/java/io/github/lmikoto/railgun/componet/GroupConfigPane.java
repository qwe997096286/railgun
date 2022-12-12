package io.github.lmikoto.railgun.componet;

import javax.swing.*;

/**
 * @author jinwq
 * @Date 2022/12/12 14:32
 */
public class GroupConfigPane extends JPanel{
    private JPanel contentPanel;
    private JComboBox<Integer> extraBtnCnt;
    private JCheckBox exportCheckBox;
    private JCheckBox importCheckBox;
    private JCheckBox pagingCheckBox;
    private JCheckBox accessCheckBox;
    private JCheckBox editCheckBox;
    private JCheckBox createCheckBox;
    private JCheckBox delCheckBox;
    private JCheckBox delBatchCheckBox;

    public GroupConfigPane() {
        super();
        extraBtnCnt.addItem(1);
        extraBtnCnt.addItem(2);
        extraBtnCnt.addItem(3);
        extraBtnCnt.addItem(4);
        extraBtnCnt.addItem(5);
    }
}
