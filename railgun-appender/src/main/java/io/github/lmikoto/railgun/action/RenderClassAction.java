package io.github.lmikoto.railgun.action;

import com.google.common.collect.Lists;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import io.github.lmikoto.railgun.componet.RenderCodeView;
import io.github.lmikoto.railgun.configurable.TemplateConfigurable;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.utils.JavaConvertUtils;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author jinwq
 * @Time 2022-11-24 14:01
 * */
@Slf4j
public class RenderClassAction extends BaseTemplateAction implements AnActionButtonRunnable, RenderCode {
    @Setter
    private List<Table> tables;
    private Appender appender;
    private DefaultParser parser;
    private static String entityPackage = "io.github.noonrain";

    public RenderClassAction(TemplateConfigurable configurable) {
        super(configurable);
        this.appender = new Appender();
    }

    @Override
    public void run(AnActionButton anActionButton) {
        this.parser = new DefaultParser();
        List<CodeRenderTabDto> tabDtos = Lists.newArrayListWithExpectedSize(tables.size() * 3);
        for (Table table : tables) {
            SimpleClass po = JavaConvertUtils.getPOClass(table);
            SimpleClass dto = JavaConvertUtils.getDTOClass(table);
            String poClass = appender.process(po, null);
            String dtoClass = appender.process(dto, null);
            String doc = generateDoc(table);
            tabDtos.add(new CodeRenderTabDto(po.getSimpleName(), poClass));
            tabDtos.add(new CodeRenderTabDto(dto.getSimpleName(), dtoClass));
            tabDtos.add(new CodeRenderTabDto(table.getTable(), doc));
        }
        RenderCodeView renderCodeView = new RenderCodeView(tabDtos);
        renderCodeView.setSize(800, 600);
        renderCodeView.setVisible(true);
        renderCodeView.setTitle("code generated");
    }

    public void execute(String sql) {
        java.util.List<Table> tables = parser.parseSQLs(sql);
        this.tables = tables;
        this.run(null);
    }
    public void populateEntity(String sql) {
        java.util.List<Table> tables = parser.parseSQLs(sql);
        this.tables = tables;
        for (Table table : this.tables) {
            SimpleClass po = JavaConvertUtils.getPOClass(table);
            SimpleClass dto = JavaConvertUtils.getDTOClass(table);
            Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
            velocityContext.put("po", po);
            velocityContext.put("dto", dto);
        }
    }
    private String generateDoc(Table table) {
        StringBuilder doc = new StringBuilder();
        doc.append("|").append(table.getTable()).append("|").append(table.getName()).append("| | | |\n");
        doc.append("| :----: | :----: | :----: | :----: | :----: |\n")
                .append("|字段|名称|长度类型|是否为空|说明|\n");
        table.getFields().forEach(field -> {
            doc.append("|").append(field.getColumn()).append("|").append(field.getComment()).append("|")
                    .append(field.getColumnType());
            if (StringUtils.isNotEmpty(field.getColumnSize())) {
                doc.append("(").append(field.getColumnSize()).append(")");
            }
            doc.append("|").append(field.getNotNull() != null && field.getNotNull() ? "not null" : " ").append("|-|\n");
        });
        return doc.toString();
    }

}
