package io.github.lmikoto.railgun.service.impl;

import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.componet.FieldsConfigItem;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dict.SimpleDict;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.FieldsConfigModel;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.utils.JavaConvertUtils;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author jinwq
 * @Date 2022/12/2 10:36
 */
public class RenderSql2Config implements RenderCode {
    @Setter
    private List<Table> tables;
    private Appender appender;
    private DefaultParser parser;
    @Override
    public List<CodeRenderTabDto> execute(String sql) {
        this.parser = new DefaultParser();
        java.util.List<Table> tables = parser.parseSQLs(sql);
        this.tables = tables;
        for (Table table : this.tables) {
            SimpleClass po = JavaConvertUtils.getPOClass(table);
            SimpleClass dto = JavaConvertUtils.getDTOClass(table);
            Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
            JDialog dialog = new JDialog();
            dialog.setLayout(new FlowLayout());
//            配置生成代码设置对话框
            Map<String, FieldsConfigItem> fieldConfigMap = Maps.newHashMapWithExpectedSize(dto.getFields().size());
            dto.getFields().forEach((key, field) -> {
                if ("serialVersionUID".equals(key)) {
                    return;
                }
                FieldsConfigModel model = new FieldsConfigModel();
                FieldsConfigItem configItem = new FieldsConfigItem(field.getName() + "[" + field.getComment() + "]",
                        model);
                fieldConfigMap.put(key, configItem);
                dialog.add(configItem.getContentPanel());
            });
            JButton confirm = new JButton("确认");
            confirm.addActionListener(event -> {
                dto.getFields().forEach((key, value) -> {
                    FieldsConfigItem fieldsConfigItem = fieldConfigMap.get(key);
                    if (fieldsConfigItem == null) {
                        return;
                    }
                    FieldsConfigModel model = fieldsConfigItem.getConfigModel();
                    if (model.isExport()) {
                        value.addLabel(SimpleDict.EXPORT);
                    }
                    if (model.isImportable()) {
                        value.addLabel(SimpleDict.IMPORT);
                    }
                    if (model.isPaging()) {
                        value.addLabel(SimpleDict.PAGING);
                    }
                    if (model.isEdit()) {
                        value.addLabel(SimpleDict.EDIT);
                    }
                    if (model.isCreate()) {
                        value.addLabel(SimpleDict.CREATE);
                    }
                    if (model.isShow()) {
                        value.addLabel(SimpleDict.SHOW);
                    }
                    dialog.dispose();
                });
                velocityContext.put("dto", dto);
            });
            //展示对话框
            JPanel jPanel = new JPanel(new FlowLayout());
            jPanel.add(confirm);
            dialog.getRootPane().setDefaultButton(confirm);
            JButton cancel = new JButton("取消");
            cancel.addActionListener(e -> dialog.dispose());
            jPanel.add(cancel);
            jPanel.setSize(200, 50);
            dialog.add(jPanel);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setSize(600, 90 + 26 * dto.getFields().size());
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            velocityContext.put("po", po);
            velocityContext.put("dto", dto);
            if (Optional.ofNullable(po.getPk()).map(SimpleClass::getAnnotations).map(list -> list.stream().anyMatch(annotation ->
                    "@Embeddable".equals(annotation.getExpr()))).isPresent()) {
                velocityContext.put("pk", po.getPk());
            }
        }
        return null;
    }

    @Override
    public String getRenderType() {
        return TemplateDict.SQL2CONFIG;
    }
}
