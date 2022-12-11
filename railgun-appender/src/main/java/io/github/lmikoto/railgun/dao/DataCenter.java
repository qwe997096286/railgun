package io.github.lmikoto.railgun.dao;

import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.ConfigModel;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author jinwq
 * @Date 2022/12/1 19:29
 */
public class DataCenter {
    private static CodeGroup codeGroup;
    private static VelocityEngine velocityEngine;

    public static CodeGroup getCurrentGroup() {
        return DataCenter.codeGroup;
    }

    public static ConfigModel getConfigModel() {
        if (codeGroup != null ) {
            return codeGroup.getConfigModel();
        }
        return new ConfigModel();
    }

    public void setCodeGroup(CodeGroup codeGroup) {
        DataCenter.codeGroup = codeGroup;
    }

    public static VelocityEngine getVelocityEngine() {
        if (velocityEngine == null) {
            velocityEngine = new VelocityEngine();
        }
        return velocityEngine;
    }
}
