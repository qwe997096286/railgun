package io.github.lmikoto.railgun.entity;

import lombok.Data;

/**
 * @author jinwq
 * @Date 2022/12/16 14:18
 */
@Data
public class FieldsConfigModel {

    private boolean export = true;
    private boolean importable = true;
    private boolean paging = true;
    private boolean edit = true;
    private boolean create = true;
    private boolean show = true;
}
