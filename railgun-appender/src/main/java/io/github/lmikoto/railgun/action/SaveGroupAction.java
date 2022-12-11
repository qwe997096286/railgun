package io.github.lmikoto.railgun.action;

import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;

/**
 * @author jinwq
 * @Date 2022/12/10 17:31
 */
public class SaveGroupAction extends BaseTemplateAction implements AnActionButtonRunnable {
    public SaveGroupAction(TemplateConfigurable configurable) {
        super(configurable);
    }

    @Override
    public void run(AnActionButton anActionButton) {
        super.saveTree();
    }
}
