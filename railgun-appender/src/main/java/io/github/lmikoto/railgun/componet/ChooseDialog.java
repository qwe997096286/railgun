package io.github.lmikoto.railgun.componet;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import io.github.lmikoto.railgun.utils.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChooseDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table1;
    private List<ConstructorDeclaration> conList;
    public ChooseDialog(DefaultTableModel model, List<ConstructorDeclaration> constructors) {
        this();
        table1.setModel(model);
        table1.setEnabled(true);
        this.conList = constructors;
    }
    public ChooseDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        int selectedRow = table1.getSelectedRow();
        if (selectedRow == -1) {
            NotificationUtils.simpleNotify("未选择要使用的构造器");
            return;
        }
        ConstructorDeclaration constructors = conList.get(selectedRow);
        String selectBody = constructors.getParameters().stream().map(param -> "tempTable." +
                StringUtils.camelToUnderline(param.getNameAsString())).collect(Collectors.joining(","));
        StringBuilder select = new StringBuilder("sql.append(\"select ");
        String s = select.append(selectBody).append("\")").toString();
        CodeRenderTabDto selectTabDto = new CodeRenderTabDto("select sql", s);
        RenderCodeView renderCodeView = new RenderCodeView(Lists.newArrayList(selectTabDto));
        renderCodeView.setSize(800, 600);
        renderCodeView.setVisible(true);
        renderCodeView.setTitle("select sql");
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ChooseDialog dialog = new ChooseDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
