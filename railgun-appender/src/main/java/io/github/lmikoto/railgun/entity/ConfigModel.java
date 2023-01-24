package io.github.lmikoto.railgun.entity;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private boolean hasPower;
    private boolean hasDelBatch;
    private boolean hasDel;
    private String groupDir;
    private String saveFileDir;
    private String entityDir;
    private String dtoDir;
    private String author;

    public String getPackageName() {
        if (StringUtils.isEmpty(this.groupDir)) {
            return this.packageName;
        }
        // 获取包名
        String groupDir = this.groupDir;
        int index = groupDir.lastIndexOf(File.separator + "main" + File.separator + "java" + File.separator);
        if (index == -1) {
            return this.packageName;
        }
        if (groupDir.lastIndexOf(File.separator) == groupDir.length() - 1) {
            groupDir = groupDir.substring(0, groupDir.length() - 1);
        }
        String packageName = groupDir.substring(index + 11)
                .replaceAll(File.separator, ".");
        return packageName;
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date());
    }
}
