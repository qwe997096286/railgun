package io.github.lmikoto.railgun.action;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBScrollPane;
import io.github.lmikoto.railgun.componet.ChooseDialog;
import io.github.lmikoto.railgun.componet.TemplateEditor;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.NotificationUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Time 2022/11/27
 */
public class RenderSelectAction extends AnAction implements RenderCode {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        TemplateEditor templateEditor = new TemplateEditor(this);
        JDialog dialog = new JDialog();
        JBScrollPane jScrollPane = new JBScrollPane();
        jScrollPane.getViewport().add(templateEditor);
        jScrollPane.setVisible(true);
        dialog.setContentPane(jScrollPane);
        templateEditor.getContentPanel().setVisible(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    @Override
    public void execute(String oldCode) {
        CompilationUnit unit = Optional.ofNullable(oldCode).map(StaticJavaParser::parse).orElse(new CompilationUnit());
        Optional<TypeDeclaration<?>> typeDeclaration = Optional.ofNullable(unit.getTypes()).flatMap(NodeList::getFirst);
        if (!typeDeclaration.isPresent()) {
            NotificationUtils.simpleNotify("未编译出class");
            return;
        }
        TypeDeclaration<?> type = typeDeclaration.get();
        List<ConstructorDeclaration> constructors = type.getConstructors();
        List<String[]> declaration = constructors.stream().map(construct -> construct
                        .getDeclarationAsString(false, false))
                .map(s -> new String[]{s}).collect(Collectors.toList());

        DefaultTableModel defaultTableModel = new DefaultTableModel(declaration.toArray(new String[][]{}), new String[]{"构造器"});
        ChooseDialog chooseDialog = new ChooseDialog(defaultTableModel, constructors);
        chooseDialog.setSize(800, 600);
        chooseDialog.setLocationRelativeTo(null);
        chooseDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        chooseDialog.setResizable(false);
        chooseDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        chooseDialog.setVisible(true);
    }
}
