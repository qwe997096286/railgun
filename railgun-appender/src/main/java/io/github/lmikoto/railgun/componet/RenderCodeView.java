package io.github.lmikoto.railgun.componet;

import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

public class RenderCodeView extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane textPane1;
    private JScrollPane scrollPane;
    private JTabbedPane tabbedPane1;
    private List<CodeRenderTabDto> tabDatas;
    private List<JScrollPane> scrollPaneList;
    private List<JTextPane> textPaneList;
    public RenderCodeView(String codes) {
        this();
        textPane1.setText(codes);
    }
    public RenderCodeView(List<CodeRenderTabDto> tabContent) {
        this();
        textPane1.setText(tabContent.get(0).getTabContent());
        tabDatas.addAll(tabContent);
        this.scrollPaneList = Lists.newArrayListWithExpectedSize(tabContent.size());
        scrollPaneList.add(this.scrollPane);
        textPaneList.add(textPane1);
        tabbedPane1.setTitleAt(0, tabContent.get(0).getTabName());
        tabContent.remove(0);
        for (CodeRenderTabDto curTab : tabContent) {
            JTextPane jTextPane = new JTextPane();
            JScrollPane jScrollPane = new JScrollPane();
            jScrollPane.getViewport().add(jTextPane);
            tabbedPane1.add(curTab.getTabName(), jScrollPane);
            jTextPane.setText(curTab.getTabContent());
            textPaneList.add(jTextPane);
            scrollPaneList.add(jScrollPane);
        }
    }
    public RenderCodeView() {
        this.tabDatas = Lists.newArrayList();
        this.textPaneList = Lists.newArrayList();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        textPane1.setAutoscrolls(true);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        RenderCodeView dialog = new RenderCodeView();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
