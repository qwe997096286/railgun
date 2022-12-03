package io.github.lmikoto.railgun.configurable.action;

import io.github.lmikoto.railgun.action.RenderSelectAction;
import org.junit.Test;

public class RenderSelectActionTest {

    @Test
    public void actionPerformed() {
        RenderSelectAction renderSelectAction = new RenderSelectAction();
        renderSelectAction.actionPerformed(null);
        System.out.println(111);
    }
}