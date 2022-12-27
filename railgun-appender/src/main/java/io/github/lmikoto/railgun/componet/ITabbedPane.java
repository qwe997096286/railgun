package io.github.lmikoto.railgun.componet;

import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * @author jinwq
 * @Date 2022/12/17 18:17
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ITabbedPane extends JBTabbedPane implements ComponentListener {
    public ITabbedPane() {
        super();
        setMinimumSize(new Dimension(400, 300));
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Container parent = this.getParent();
        Container grandParent = this.getRootPane();
        int width = grandParent.getWidth() - ((JBSplitter) parent)
                .getFirstComponent().getWidth() - this.getX() - 130;
        log.info("设置最大宽度：{}", width);
        this.setMaximumSize(new Dimension(width, grandParent.getHeight() - 150));
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
