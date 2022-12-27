package io.github.lmikoto.railgun.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.lmikoto.railgun.componet.TemplateEditor;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;

import javax.swing.*;
import java.awt.*;

/**
 * @author jinwq
 * @Time 2022/11/27
 */
public class RenderSelectAction extends AnAction{

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        TemplateEditor templateEditor = new TemplateEditor(TemplateDict.ENTITY2SELECT);
        JDialog dialog = new JDialog();
        JScrollPane jScrollPane = new JScrollPane();
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

}
