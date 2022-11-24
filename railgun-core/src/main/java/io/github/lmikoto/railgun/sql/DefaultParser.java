package io.github.lmikoto.railgun.sql;

import com.google.common.base.Throwables;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.utils.StringUtils;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DefaultParser extends AbstractParser {
    @Override
    public List<Table> parseSQLs(String sqls) {
        if (StringUtils.isBlank(sqls)) {
            return null;
        }

        List<Table> result = new ArrayList<>();
        // 解析sql语句
        try {
            List<Statement> statements = CCJSqlParserUtil.parseStatements(sqls).getStatements();
            if (statements == null || statements.isEmpty()) {
                throw new RuntimeException("Nothing in parse !!!");
            }
            List<CreateTable> createTables = new ArrayList<>();
            for (Statement statement: statements) {
                if (statement instanceof CreateTable) {
                    createTables.add((CreateTable) statement);
                }
            }
            if (createTables.isEmpty()) {
                throw new RuntimeException("Only support create table statement !!!");
            }

            for(CreateTable createTable: createTables) {
                List<Field> fields = new ArrayList<>();
                Table table = new Table(fields);
                net.sf.jsqlparser.schema.Table tableScheme = createTable.getTable();
                table.setName(removeQuotes(tableScheme.getName()));
                List<String> strList = createTable.getTableOptionsStrings();
                for (int i = 0 ; i < strList.size() - 2 ; i++) {
                    if ("COMMENT".equals(strList.get(i)) && "=".equals(strList.get(i + 1))) {
                        table.setTable(removeQuotes(strList.get(i + 2)));
                        break;
                    }
                }
                createTable.getColumnDefinitions().forEach(it -> {
                    Field field = new Field();
                    // 字段名称
                    String columnName = removeQuotes(it.getColumnName());
                    // 同时设置了 FieldName
                    field.setColumn(columnName);

                    // 字段类型
                    ColDataType colDataType = it.getColDataType();
                    // 同时设置了字段类型
                    field.setColumnType(colDataType.getDataType());
                    field.setColumnSize(firstOrNull(colDataType.getArgumentsStringList()));

                    // comment注释
                    field.setComment(getColumnComment(it.getColumnSpecs()));

                    fields.add(field);
                });

                if (table.getFields() != null && !table.getFields().isEmpty()) {
                    result.add(table);
                }
            }
            return result;
        } catch (Exception ignore) {
            log.error(Throwables.getStackTraceAsString(ignore));
        }
        return null;
    }

    /**
     * 获取字段的注释
     */
    private String getColumnComment(List<String> specs) {
        if (specs == null || specs.isEmpty()) {
            return null;
        }
        for (int size = specs.size(), i = 0; i < size; i++) {
            String spec = specs.get(i);
            if ("COMMENT".equals(spec.toUpperCase())) {
                // 下一个为comment内容, 查看是否越界
                if (i + 1 >= size) {
                    return null;
                }
                return removeQuotes(specs.get(i + 1));
            }
        }
        return null;
    }

}
