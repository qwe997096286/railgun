package io.github.lmikoto.railgun.configurable.componet;

import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.Appender;
import io.github.lmikoto.railgun.entity.SimpleClass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RenderCodeView extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane textPane1;
    private JScrollPane scrollPane;
    private JTabbedPane tabbedPane1;
    private Appender appender;
    private List<SimpleClass> clazzList;
    private List<JScrollPane> scrollPaneList;
    private List<JTextPane> textPaneList;
    public RenderCodeView(String codes) {
        this();
        textPane1.setText(codes);
    }
    public RenderCodeView(List<SimpleClass> classList) {
        this();
        textPane1.setText(appender.process(classList.get(0), null));
        clazzList.addAll(classList);
        this.scrollPaneList = Lists.newArrayListWithExpectedSize(classList.size());
        scrollPaneList.add(this.scrollPane);
        textPaneList.add(textPane1);
        tabbedPane1.setTitleAt(0, classList.get(0).getSimpleName());
        classList.remove(0);
        for (SimpleClass curClass : classList) {
            JTextPane jTextPane = new JTextPane();
            JScrollPane jScrollPane = new JScrollPane();
            jScrollPane.getViewport().add(jTextPane);
            tabbedPane1.add(curClass.getSimpleName(), jScrollPane);
            textPaneList.add(jTextPane);
            scrollPaneList.add(jScrollPane);
        }
        tabbedPane1.addChangeListener(event -> {
            JTabbedPane target = (JTabbedPane) event.getSource();
            int tabPlacement = target.getTabPlacement();
            if (textPaneList.get(tabPlacement).getText().length() > 10) {
                return;
            }
            textPaneList.get(tabPlacement).setText(appender.process(clazzList.get(tabPlacement), null));
            System.out.println(event);
        });
    }
    public RenderCodeView() {
        this.appender = new Appender();
        this.clazzList = Lists.newArrayList();
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
