package io.github.lmikoto.railgun.componet;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author jinwq
 * @Date 2022/12/17 18:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ITabbedPane extends JBTabbedPane implements ComponentListener {

    @Override
    public void componentResized(ComponentEvent e) {
        Container parent = this.getParent();
        Container grandParent = this.getRootPane();
        this.setMaximumSize(new Dimension(grandParent.getWidth() -((JBSplitter) parent)
                .getFirstComponent().getWidth() - 360, parent.getHeight() - 50));
        this.updateUI();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
