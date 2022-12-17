package io.github.lmikoto.railgun.entity;

import lombok.Data;

/**
 * @author jinwq
 * @Date 2022/12/16 14:18
 */
@Data
public class FieldsConfigModel {

    private boolean export;
    private boolean importable;
    private boolean paging;
    private boolean edit;
    private boolean create;
    private boolean show;
}
