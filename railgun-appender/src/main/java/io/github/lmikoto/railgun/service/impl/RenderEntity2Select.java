package io.github.lmikoto.railgun.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.github.lmikoto.railgun.componet.ChooseDialog;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
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
 * @Date 2022/12/3 20:45
 */
public class RenderEntity2Select implements RenderCode {
    @Override
    public void execute(String text) {
        CompilationUnit unit = Optional.ofNullable(text).map(StaticJavaParser::parse).orElse(new CompilationUnit());
        Optional<TypeDeclaration<?>> typeDeclaration = Optional.ofNullable(unit.getTypes()).flatMap(NodeList::getFirst);
        if (!typeDeclaration.isPresent()) {
            NotificationUtils.simpleNotify("未编译出class");
            return;
        }
        TypeDeclaration<?> type = typeDeclaration.get();
        List<ConstructorDeclaration> constructors = type.getConstructors().stream()
                .filter( declar -> declar.getParameters().size() > 0)
                .collect(Collectors.toList());
        List<String[]> declaration = constructors.stream().map(construct -> construct
                        .getDeclarationAsString(false, false))
                .map(s -> new String[]{s}).collect(Collectors.toList());
        if (declaration.size() < 1) {
            NotificationUtils.simpleNotify("未找到能构造select的构造方法");
            return;
        } else if (declaration.size() == 1) {
            ChooseDialog.showRenderSelect(constructors.get(0));
            return;
        }
        DefaultTableModel defaultTableModel = new DefaultTableModel(declaration.toArray(new String[][]{}), new String[]{"构造器"});
        ChooseDialog chooseDialog = new ChooseDialog(defaultTableModel, constructors);
        chooseDialog.setSize(800, 600);
        chooseDialog.setLocationRelativeTo(null);
        chooseDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        chooseDialog.setResizable(false);
        chooseDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        chooseDialog.setVisible(true);
    }

    @Override
    public String getRenderType() {
        return TemplateDict.ENTITY2SELECT;
    }
}
