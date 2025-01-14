package io.github.lmikoto.railgun.sql;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
            Map<String, String> fullName2Comment = getCommentMap(statements);
            Map<String, String> uniqueFieldMap = getUniqueMap(statements);
            for(CreateTable createTable: createTables) {
                List<Field> fields = new ArrayList<>();
                Table table = new Table(fields);
                net.sf.jsqlparser.schema.Table tableScheme = createTable.getTable();
                table.setName(removeQuotes(tableScheme.getName()));
                String tableFullName;
                if (fullName2Comment == null || fullName2Comment.isEmpty() ) {
                    List<String> strList = createTable.getTableOptionsStrings();
                    for (int i = 0 ; i < strList.size() - 2 ; i++) {
                        if ("COMMENT".equals(strList.get(i)) && "=".equals(strList.get(i + 1))) {
                            table.setTable(removeQuotes(strList.get(i + 2)));
                            break;
                        }
                    }
                    tableFullName = removeQuotes(createTable.getTable().getFullyQualifiedName().toUpperCase());
                } else {
                    tableFullName = removeQuotes(createTable.getTable().getFullyQualifiedName().toUpperCase());
                    table.setTable(removeQuotes(fullName2Comment.get(tableFullName)));
                }
                List<Index> indexes = createTable.getIndexes();
                Map<String, Object> primaryKeyMap = Maps.newHashMap();
                if (CollectionUtils.isNotEmpty(indexes)) {
                    for (Index index : indexes) {
                        if ("PRIMARY KEY".equals(index.getType())) {
                            index.getColumns().forEach(columnParams -> primaryKeyMap.put(removeQuotes(columnParams.columnName),
                                    columnParams));
                        } else if ("UNIQUE KEY".equals(index.getType())) {
                            List<Index.ColumnParams> columns = index.getColumns();
                            if (columns.size() == 1) {
                                columns.forEach(columnParams -> uniqueFieldMap.put(tableFullName+ "." + removeQuotes(columnParams.columnName).toUpperCase(),
                                "UNIQUE"));
                            }
                        }
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
                    if (StringUtils.isNotEmpty(tableFullName) && fullName2Comment.containsKey(tableFullName + "." + removeQuotes(it.getColumnName().toUpperCase()))) {
                        field.setComment(fullName2Comment.get(tableFullName + "." + removeQuotes(it.getColumnName().toUpperCase())));
                    } else {
                        field.setComment(getColumnComment(it.getColumnSpecs()));
                    }
                    if (StringUtils.isNotEmpty(tableFullName) && uniqueFieldMap.containsKey(tableFullName + "." + removeQuotes(it.getColumnName().toUpperCase()))) {
                        field.setUnique(true);
                    } else {
                        field.setUnique(false);
                    }
                    // 主键
                    if (primaryKeyMap.containsKey(columnName)) {
                        field.setPrimaryKey(true);
                    } else {
                        field.setPrimaryKey(false);
                    }
                    List<String> columnSpecs = it.getColumnSpecs();
                    field.setNotNull(false);
                    if (CollectionUtils.isNotEmpty(columnSpecs)) {
                        for (int i = 0 ; i < columnSpecs.size() - 1 ; i++) {
                            if ("NOT".equalsIgnoreCase(columnSpecs.get(i)) && "NULL".equalsIgnoreCase(columnSpecs.get(i + 1))) {
                                field.setNotNull(true);
                                break;
                            }
                        }
                    }
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

    private Map<String, String> getUniqueMap(List<Statement> statements) {
        Map<String, String> fullName2Index = statements.stream().filter(statement -> statement
                instanceof CreateIndex).map(statement -> (CreateIndex) statement).collect(Collectors
                .toMap((CreateIndex index) -> {

                    if (Objects.nonNull(index.getTable())) {
                        List<String> columnsNames = index.getIndex().getColumnsNames();
                        if (CollectionUtils.isEmpty(columnsNames)) {
                            return null;
                        } else if (columnsNames.size() > 1) {
                            return null;
                        }
                        return removeQuotes(index.getTable().getFullyQualifiedName().toUpperCase()) +
                                "." + columnsNames.get(0).toUpperCase();
                    }
                    return null;
                }, index -> removeQuotes("UNIQUE"), (left, right) -> left));
        return fullName2Index;
    }

    private Map<String, String> getCommentMap(List<Statement> statements) {
        Map<String, String> fullName2Comment = statements.stream().filter(statement -> statement
                instanceof Comment).map(statement -> (Comment) statement).collect(Collectors
                .toMap((Comment comment) -> {
            if (Objects.nonNull(comment.getTable())) {
                return removeQuotes(comment.getTable().getFullyQualifiedName().toUpperCase());
            } else if (Objects.nonNull(comment.getColumn())) {
                return removeQuotes(comment.getColumn().getFullyQualifiedName().toUpperCase());
            } else if (Objects.nonNull(comment.getView())) {
                return removeQuotes(comment.getView().getFullyQualifiedName().toUpperCase());
            }
            return null;
        }, comment -> removeQuotes(comment.getComment().toString())));
        return fullName2Comment;
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
