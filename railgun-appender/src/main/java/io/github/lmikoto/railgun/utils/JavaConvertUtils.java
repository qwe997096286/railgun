package io.github.lmikoto.railgun.utils;

import com.github.javaparser.ast.Modifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dict.SimpleDict;
import io.github.lmikoto.railgun.entity.SimpleAnnotation;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.SimpleField;
import io.github.lmikoto.railgun.entity.SimpleMethod;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/2 10:41
 */
public class JavaConvertUtils {
    public static SimpleClass getDTOClass(Table table) {
        SimpleClass simpleClass = new SimpleClass();
        simpleClass.setName(DataCenter.getConfigModel().getPackageName() + "." + StringUtils
                .underlineToCamel(table.getName(),true) + "Dto");
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
            if (field.getNotNull()) {
                simpleField.addLabel(SimpleDict.NOT_NULL);
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
        simpleClass.setName(DataCenter.getConfigModel().getPackageName() + "." + StringUtils.underlineToCamel(table.getName(),true));
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
                    simpleClass.setPk(fieldClass);
                }
                simpleField.addLabel(SimpleDict.PRIMARY);
            }
            if (field.getNotNull()) {
                simpleField.addLabel(SimpleDict.NOT_NULL);
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
                simpleField.setComment(JavaUtils.getSimpleName(field.getComment()));
            }

            simpleField.setAnnotations(fieldAnnotations);
        }
        simpleClass.setFields(fields);
//            构造get、set方法
        List<SimpleMethod> methods = Lists.newArrayListWithExpectedSize(fields.size() * 2);
        simpleClass.setMethods(methods);
        return simpleClass;
    }

    private static boolean setUnionPrimaryKey(SimpleClass simpleClass, LinkedHashMap<String, SimpleField> fields, List<Field> columns) {
        List<Field> columnsPrimary = columns.stream().filter(Field::getPrimaryKey).collect(Collectors.toList());
        if (columnsPrimary.size() < 2) {
            return false;
        }
        SimpleAnnotation eIdAnno = new SimpleAnnotation();
        eIdAnno.setName("javax.persistence.Embeddable");
        eIdAnno.setExpr("@Embeddable");
        SimpleClass pk = new SimpleClass();
        pk.setName(simpleClass.getName() + "PK");
        pk.setAnnotations(Collections.singletonList(eIdAnno));
        SimpleField pkField = new SimpleField();
        SimpleAnnotation fieldPKAnno = new SimpleAnnotation();
        fieldPKAnno.setName("javax.persistence.EmbeddedId");
        fieldPKAnno.setExpr("@EmbeddedId");
        pkField.setClazz(pk);
        pkField.setComment("主键");
        pkField.setAnnotations(Collections.singletonList(fieldPKAnno));
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
        simpleClass.setPk(pk);
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

}
