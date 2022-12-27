package io.github.lmikoto.railgun.entity;

import lombok.Data;

/**
 * @author jinwq
 * @Date 2022/12/2 10:45
 */
@Data
public class ConfigModel {
    private String packageName = "io.github.noonrain";
    private boolean hasExport;
    private boolean hasImport;
    private boolean hasPaging;
    private boolean hasApproval;
    private boolean hasEdit;
    private boolean hasCreate;
    private boolean hasDelBatch;
    private boolean hasDel;
    private String groupDir;
    private String controllerPackage;
    private String servicePackage;
    private String daoPackage;

    public String getPackageName() {
        // 获取包名
        String groupDir = this.groupDir;
        int index = groupDir.lastIndexOf("/main/java/");
        if (index == -1) {
            return this.packageName;
        }
        if (groupDir.lastIndexOf('/') == groupDir.length() - 1) {
            groupDir = groupDir.substring(0, groupDir.length() - 1);
        }
        String packageName = groupDir.substring(index + 11)
                .replaceAll("/", ".");
        return packageName;
    }
}
