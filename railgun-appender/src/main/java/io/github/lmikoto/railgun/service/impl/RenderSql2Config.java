package io.github.lmikoto.railgun.service.impl;

import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.utils.JavaConvertUtils;
import lombok.Setter;

import java.util.List;
import java.util.Map;

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
    public void execute(String sql) {
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

    @Override
    public String getRenderType() {
        return TemplateDict.SQL2CONFIG;
    }
}
