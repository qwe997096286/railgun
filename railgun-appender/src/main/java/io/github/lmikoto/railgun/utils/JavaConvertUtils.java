package io.github.lmikoto.railgun.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dict.SimpleDict;
import io.github.lmikoto.railgun.entity.*;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/2 10:41
 */
public class JavaConvertUtils {
    public static SimpleClass getDTOClass(Table table) {
        SimpleClass simpleClass = new SimpleClass();
        String dtoDir = Optional.ofNullable(DataCenter.getConfigModel()).map(ConfigModel::getDtoDir).orElse("");
        int beginIndex = dtoDir.indexOf("java" + File.separator) + 5;
        if (beginIndex >= 0 && beginIndex < dtoDir.length()) {
            dtoDir = dtoDir.substring(beginIndex).replaceAll(File.separator, ".");
        }
        simpleClass.setName(dtoDir + StringUtils.underlineToCamel(table.getName(),true) + "Dto");
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
            simpleField.setName(field.getName());
            if (field.getPrimaryKey()) {
                simpleField.addLabel(SimpleDict.PRIMARY);
            }
            if (field.getUnique()) {
                simpleField.addLabel(SimpleDict.UNIQUE);
            }
            if (field.getNotNull()) {
                simpleField.addLabel(SimpleDict.NOT_NULL);
            }
            if (StringUtils.isNotEmpty(field.getColumnSize()) && Pattern.matches("^[0-9]*$||^\\\\d+$", field.getColumnSize())) {
                simpleField.setLength(Integer.valueOf(field.getColumnSize()));
            }
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
    public static SimpleClass getPOClass(Table table) {
        SimpleClass simpleClass = new SimpleClass();
        String entityDir = Optional.ofNullable(DataCenter.getConfigModel()).map(ConfigModel::getEntityDir).orElse("");
        int beginIndex = entityDir.indexOf("java" + File.separator) + 5;
        if (beginIndex >= 0 && beginIndex < entityDir.length()) {
            entityDir = entityDir.substring(beginIndex).replaceAll(File.separator, ".");
        }
         entityDir = entityDir.replaceAll(File.separator, ".");
        simpleClass.setName(entityDir + StringUtils.underlineToCamel(table.getName(),true));
        SimpleAnnotation tableAnno = new SimpleAnnotation();
        tableAnno.setExpr("@Table(name = \"" + table.getName().toUpperCase() + "\")");
        tableAnno.setName("javax.persistence.Table");

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
        boolean unionKey = setUnionPrimaryKey(simpleClass, fields, table.getFields());
        for (Field field : table.getFields()) {
            SimpleClass fieldClass = new SimpleClass();
            fieldClass.setName(field.getFieldType());
            SimpleField simpleField = new SimpleField();
            simpleField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
            simpleField.setClazz(fieldClass);
            simpleField.setName(field.getName());
            if (field.getPrimaryKey()) {
                if (!unionKey) {
                    SimpleField pkField = new SimpleField();
                    pkField.setName(field.getName());
                    pkField.setComment(field.getComment());
                    pkField.setClazz(fieldClass);
                    simpleClass.setPk(pkField);
                }
                simpleField.addLabel(SimpleDict.PRIMARY);
            }
            if (field.getNotNull()) {
                simpleField.addLabel(SimpleDict.NOT_NULL);
            }
            if (field.getUnique()) {
                simpleField.addLabel(SimpleDict.UNIQUE);
            }
            fields.put(field.getName(), simpleField);
            List<SimpleAnnotation> fieldAnnotations = Lists.newArrayListWithExpectedSize(1);
            if (!unionKey && field.getPrimaryKey()) {
                SimpleAnnotation idAnnotation = new SimpleAnnotation();
                idAnnotation.setName("javax.persistence.Id");
                idAnnotation.setExpr("@Id");
                fieldAnnotations.add(idAnnotation);
            }
            SimpleAnnotation columnAnno = new SimpleAnnotation();
            columnAnno.setName("javax.persistence.Column");
            String expr = "@Column(name = \"" + field.getColumn() + "\"";
            if (unionKey && field.getPrimaryKey()) {
                expr += ", insertable = false, updatable = false";
            } else if (field.getNotNull()) {
                expr += ", nullable = false";
            }
            expr += ")";
            columnAnno.setExpr(expr);
            fieldAnnotations.add(columnAnno);
            if (StringUtils.isNotBlank(field.getComment())) {
                simpleField.setComment(field.getComment());
            }

            simpleField.setAnnotations(fieldAnnotations);
        }
        simpleClass.setFields(fields);
//            构造get、set方法
        List<SimpleMethod> methods = Lists.newArrayListWithExpectedSize(fields.size() * 2);
        if (simpleClass.getPk() == null) {
            SimpleClass string = new SimpleClass("String");
            SimpleField pk = new SimpleField();
            pk.setName("");
            pk.setClazz(string);
            simpleClass.setPk(pk);
        }
        simpleClass.setMethods(methods);
        return simpleClass;
    }

    private static boolean setUnionPrimaryKey(SimpleClass simpleClass, LinkedHashMap<String, SimpleField> fields, List<Field> columns) {
        List<Field> columnsPrimary = columns.stream().filter(Field::getPrimaryKey).collect(Collectors.toList());
        if (columnsPrimary.size() < 2) {
            return false;
        }
        SimpleAnnotation eIdAnno = new SimpleAnnotation();
        eIdAnno.setExpr("@Embeddable");
        eIdAnno.setName("javax.persistence.Embeddable");
        SimpleClass pk = new SimpleClass();
        pk.setName(simpleClass.getName() + "PK");
        pk.setAnnotations(Collections.singletonList(eIdAnno));
        pk.setComment(simpleClass.getComment() + "主键");
        SimpleField pkField = new SimpleField();
        SimpleAnnotation fieldPKAnno = new SimpleAnnotation();
        fieldPKAnno.setName("javax.persistence.EmbeddedId");
        fieldPKAnno.setExpr("@EmbeddedId");
        pkField.setClazz(pk);
        pkField.setComment(columnsPrimary.stream().map(Field::getComment).collect(Collectors.joining(",")));
        pkField.setAnnotations(Collections.singletonList(fieldPKAnno));
        pkField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
        pkField.setName("pk");
        fields.put("pk", pkField);
        LinkedHashMap<String, SimpleField> pkFields = Maps.newLinkedHashMapWithExpectedSize(columnsPrimary.size());
        setSerialImpl(pk, pkFields, new Random());
        for (Field field : columnsPrimary) {
            SimpleClass fieldClass = new SimpleClass();
            fieldClass.setName(field.getFieldType());
            SimpleField simpleField = new SimpleField();
            simpleField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
            simpleField.setClazz(fieldClass);
            simpleField.setName(field.getName());
            if (field.getPrimaryKey()) {
                simpleField.addLabel(SimpleDict.PRIMARY);
            }
            if (field.getNotNull()) {
                simpleField.addLabel(SimpleDict.NOT_NULL);
            }
            if (field.getUnique()) {
                simpleField.addLabel(SimpleDict.UNIQUE);
            }
            SimpleAnnotation columnAnno = new SimpleAnnotation();
            columnAnno.setName("javax.persistence.Column");
            String expr = "@Column(name = \"" + field.getColumn() + "\"";
            if (field.getNotNull()) {
                expr += ",nullable = false)";
            } else {
                expr += ")";
            }
            columnAnno.setExpr(expr);
            if (StringUtils.isNotBlank(field.getComment())) {
                simpleField.setComment(JavaUtils.getSimpleName(field.getComment()));
            }
            List<SimpleAnnotation> fieldAnnotations = Lists.newArrayList(columnAnno);
            simpleField.setAnnotations(fieldAnnotations);
            pkFields.put(field.getName(), simpleField);
        }
        fields.put("pk", pkField);
        pk.setFields(pkFields);
        simpleClass.setPk(pkField);
        return true;
    }

    public static void setSerialImpl(SimpleClass simpleClass, LinkedHashMap<String, SimpleField> fields, Random random) {
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

    public static void populateImport(SimpleClass dto) {
        Optional<Boolean> hasImport = Optional.ofNullable(DataCenter.getConfigModel()).map(ConfigModel::isHasImport);
        if (!hasImport.isPresent() || !hasImport.get()) {
            return;
        }
        AtomicInteger index = new AtomicInteger(0);
        dto.getFields().values().forEach(field -> {
            if (!field.hasLabel(SimpleDict.IMPORT)) {
                return;
            }
            List<SimpleAnnotation> annotations = field.getAnnotations();
            if (Objects.isNull(annotations)) {
                annotations = Lists.newArrayList();
                field.setAnnotations(annotations);
            }
            SimpleAnnotation sAnnotation = new SimpleAnnotation();
            sAnnotation.setName("com.alibaba.excel.annotation.ExcelProperty");
            sAnnotation.setExpr("@ExcelProperty(value = \"" + field.getComment() + "\", index = " + index.getAndIncrement() + ")");
            annotations.add(sAnnotation);
        });
    }

    public static void populatePageConst(SimpleClass dto) {
        Optional<Boolean> hasPaging = Optional.ofNullable(DataCenter.getConfigModel()).map(ConfigModel::isHasPaging);
        if (!hasPaging.isPresent() || !hasPaging.get()) {
            return;
        }
        ConstructorDeclaration constructorDeclaration = new ConstructorDeclaration();
        NodeList<Parameter> nodes = new NodeList<>();
        List<String> body = Lists.newArrayList();
        dto.getFields().values().forEach(field -> {
            if (!field.hasLabel(SimpleDict.PAGING)) {
                return;
            }
            Parameter parameter = new Parameter();
            parameter.setName(field.getSimpleName());
            parameter.setType(field.getClazz().getSimpleName());
            body.add("this." + field.getName() + " = " + field.getName() + ";");
            nodes.add(parameter);
        });
        constructorDeclaration.setLineComment(nodes.size() + "");
        constructorDeclaration.setParameters(nodes);
        List<Statement> statements = body.stream().map(StaticJavaParser::parseStatement).collect(Collectors.toList());
        NodeList<Statement> blokState = NodeList.nodeList(statements);
        constructorDeclaration.setBody(new BlockStmt(blokState));
        constructorDeclaration.setModifiers(Modifier.Keyword.PUBLIC);
        constructorDeclaration.setName(dto.getSimpleName());
        ConstructorDeclaration noneConstructor = new ConstructorDeclaration();
        noneConstructor.setParameters(NodeList.nodeList());
        noneConstructor.setName(dto.getSimpleName());
        noneConstructor.setModifiers(Modifier.Keyword.PUBLIC);
        List<ConstructorDeclaration> constructorList = dto.getConstructorList();
        if (CollectionUtils.isEmpty(constructorList)) {
            constructorList = Lists.newArrayList();
            dto.setConstructorList(constructorList);
        }
        constructorList.add(noneConstructor);
        constructorList.add(constructorDeclaration);
        ConstructorDeclaration clone = new ConstructorDeclaration();
        NodeList<Parameter> parameters = new NodeList<>(nodes);
        Parameter parameter = new Parameter();
        parameter.setType("BigDecimal");
        parameter.setName("rows");
        parameters.add(parameter);
        clone.setLineComment(nodes.size() + "");
        clone.setName(dto.getSimpleName());
        clone.setLineComment(parameters.size() + "");
        clone.setModifiers(Modifier.Keyword.PUBLIC);
        clone.setParameters(parameters);
        clone.setBody(new BlockStmt(blokState));
        constructorList.add(clone);
    }
}
