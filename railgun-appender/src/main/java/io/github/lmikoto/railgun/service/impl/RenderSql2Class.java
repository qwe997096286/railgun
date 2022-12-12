package io.github.lmikoto.railgun.service.impl;

import com.github.javaparser.ast.Modifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.utils.JavaConvertUtils;
import io.github.lmikoto.railgun.utils.JavaUtils;
import io.github.lmikoto.railgun.componet.RenderCodeView;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.SimpleAnnotation;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.SimpleField;
import io.github.lmikoto.railgun.entity.SimpleMethod;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author jinwq
 * @Date 2022/12/2 10:24
 */
public class RenderSql2Class implements RenderCode {
    @Setter
    private List<Table> tables;
    private Appender appender;
    private DefaultParser parser;
    private static String entityPackage = "io.github.noonrain";

    public RenderSql2Class() {
        this.appender = new Appender();
        this.parser = new DefaultParser();
    }

    public void run() {
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
        this.run();
    }

    @Override
    public String getRenderType() {
        return TemplateDict.SQL2CLASS;
    }

    public void populateEntity(String sql) {
        java.util.List<Table> tables = parser.parseSQLs(sql);
        this.tables = tables;
        for (Table table : this.tables) {
            SimpleClass po = getPOClass(table);
            SimpleClass dto = getDTOClass(table);
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

    private SimpleClass getDTOClass(Table table) {
        SimpleClass simpleClass = new SimpleClass();
        simpleClass.setName(entityPackage + "." + StringUtils.underlineToCamel(table.getName(),true) + "Dto");
        Random random = new Random();
        if (StringUtils.isNotEmpty(table.getTable())) {
            simpleClass.setComment(table.getTable());
        }
        LinkedHashMap<String, SimpleField> fields = Maps.newLinkedHashMapWithExpectedSize(table.getFields().size());
//            构造属性
        setSerialImpl(simpleClass, fields, random);
        for (Field field : table.getFields()) {
            SimpleClass fieldClass = new SimpleClass();
            fieldClass.setName(field.getFieldType());
            SimpleField simpleField = new SimpleField();
            simpleField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
            simpleField.setClazz(fieldClass);
            fields.put(field.getName(), simpleField);
            if (StringUtils.isNotBlank(field.getComment())) {
                simpleField.setComment(JavaUtils.getSimpleName(field.getComment()));
            }
            if ("java.util.Date".equals(fieldClass.getName())) {
                SimpleAnnotation dateFormat = new SimpleAnnotation();
                dateFormat.setName("org.springframework.format.annotation.DateTimeFormat");
                dateFormat.setExpr("@DateTimeFormat(pattern = \"yyyy-MM-dd\"");
                SimpleAnnotation jsonFormat = new SimpleAnnotation();
                jsonFormat.setName("com.fasterxml.jackson.annotation.JsonFormat");
                jsonFormat.setExpr("@JsonFormat(pattern = \"yyyy-MM-dd\", timezone=\"GMT+8\"");
            }
        }
        simpleClass.setFields(fields);
//            构造get、set方法
        List<SimpleMethod> methods = Lists.newArrayListWithExpectedSize(fields.size() * 2);
        simpleClass.setMethods(methods);
        return simpleClass;
    }

    @NotNull
    private static SimpleClass getPOClass(Table table) {
        SimpleClass simpleClass = new SimpleClass();
        simpleClass.setName(entityPackage + "." + StringUtils.underlineToCamel(table.getName(),true));
        SimpleAnnotation tableAnno = new SimpleAnnotation();
        tableAnno.setName("javax.persistence.Table");
        tableAnno.setExpr("@Table(name = \"" + table.getName().toUpperCase() + "\")");

        SimpleAnnotation entityAnno = new SimpleAnnotation();
        entityAnno.setName("javax.persistence.Entity");
        entityAnno.setExpr("@Entity");


        if (StringUtils.isNotEmpty(table.getTable())) {
            simpleClass.setComment(table.getTable());
        }

        simpleClass.setAnnotations(Lists.newArrayList(entityAnno, tableAnno));
        LinkedHashMap<String, SimpleField> fields = Maps.newLinkedHashMapWithExpectedSize(table.getFields().size());
//            构造属性
        Random random = new Random();
        setSerialImpl(simpleClass, fields, random);
        for (Field field : table.getFields()) {
            SimpleClass fieldClass = new SimpleClass();
            fieldClass.setName(field.getFieldType());
            SimpleField simpleField = new SimpleField();
            simpleField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
            simpleField.setClazz(fieldClass);
            fields.put(field.getName(), simpleField);
            SimpleAnnotation columnAnno = new SimpleAnnotation();
            columnAnno.setName("javax.persistence.Column");
            columnAnno.setExpr("@Column(name = \"" + field.getColumn() + "\")");
            if (StringUtils.isNotBlank(field.getComment())) {
                simpleField.setComment(JavaUtils.getSimpleName(field.getComment()));
            }
            List<SimpleAnnotation> fieldAnnotations = Lists.newArrayList(columnAnno);
            simpleField.setAnnotations(fieldAnnotations);
        }
        simpleClass.setFields(fields);
//            构造get、set方法
        List<SimpleMethod> methods = Lists.newArrayListWithExpectedSize(fields.size() * 2);
        simpleClass.setMethods(methods);
        return simpleClass;
    }

    private static void setSerialImpl(SimpleClass simpleClass, LinkedHashMap<String, SimpleField> fields, Random random) {
        //        设置序列化、序列号
        SimpleClass serializable = new SimpleClass();
        serializable.setName("java.io.Serializable");
        simpleClass.setImpl(Collections.singletonList(serializable));
        SimpleField serialField = new SimpleField();
        SimpleClass serialLong = new SimpleClass();
        serialLong.setName("long");
        serialField.setClazz(serialLong);
        serialField.setName("serialVersionUID");
        long randomLong = random.nextLong();
        serialField.setExpr(randomLong + "L");
        serialField.setModifiers(Lists.newArrayList(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC
                , Modifier.Keyword.FINAL));
        fields.put("serialVersionUID", serialField);
    }

}
