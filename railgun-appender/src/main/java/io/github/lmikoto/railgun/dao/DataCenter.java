package io.github.lmikoto.railgun.dao;

import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.ConfigModel;
import org.apache.velocity.app.VelocityEngine;

import java.util.List;

/**
 * @author jinwq
 * @Date 2022/12/1 19:29
 */
public class DataCenter {
    private static CodeGroup codeGroup;
    private static VelocityEngine velocityEngine;

    private static List<CodeGroup> groupList;
    public static CodeGroup getCurrentGroup() {
        return DataCenter.codeGroup;
    }

    public static ConfigModel getConfigModel() {
        if (codeGroup != null ) {
            return codeGroup.getConfigModel();
        }
        return null;
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

    public static List<CodeGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<CodeGroup> groupList) {
        DataCenter.groupList = groupList;
    }
}
