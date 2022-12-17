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
}
