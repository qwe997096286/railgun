package io.github.lmikoto.railgun.componet;

import io.github.lmikoto.railgun.entity.FieldsConfigModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

/**
 * @author jinwq
 * @Date 2022/12/16 14:03
 */
@NoArgsConstructor
@Data
public class FieldsConfigItem {
    private JCheckBox export;
    private JCheckBox importBox;
    private JCheckBox paging;
    private JCheckBox edit;
    private JCheckBox create;
    private JPanel contentPanel;
    private JCheckBox show;
    private JLabel label;
    private FieldsConfigModel configModel;

    public FieldsConfigItem(String label, FieldsConfigModel model) {
        this.label.setText(label);
        this.configModel = model;
        export.setSelected(model.isExport());
        importBox.setSelected(model.isImportable());
        paging.setSelected(model.isPaging());
        edit.setSelected(model.isEdit());
        create.setSelected(model.isCreate());
        show.setSelected(model.isShow());
    }

    public FieldsConfigModel getConfigModel() {
        configModel.setExport(export.isSelected());
        configModel.setImportable(importBox.isSelected());
        configModel.setPaging(paging.isSelected());
        configModel.setEdit(edit.isSelected());
        configModel.setCreate(create.isSelected());
        configModel.setShow(show.isSelected());
        return configModel;
    }
}
