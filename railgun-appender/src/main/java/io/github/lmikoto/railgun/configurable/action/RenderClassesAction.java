package io.github.lmikoto.railgun.configurable.action;

import com.github.javaparser.ast.Modifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import io.github.lmikoto.railgun.JavaUtils;
import io.github.lmikoto.railgun.configurable.componet.RenderCodeView;
import io.github.lmikoto.railgun.entity.SimpleAnnotation;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.SimpleField;
import io.github.lmikoto.railgun.entity.SimpleMethod;
import io.github.lmikoto.railgun.model.Field;
import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * @author jinwq
 * @Time 2022-11-24 14:01
 * */
@Slf4j
public class RenderClassesAction implements AnActionButtonRunnable {
    @Setter
    private List<Table> tables;
    @Getter
    private List<SimpleClass> clazz;
    private static String entityPackage = "io.github.noonrain";
    @Override
    public void run(AnActionButton anActionButton) {
        clazz = Lists.newArrayListWithExpectedSize(tables.size());
        for (Table table : tables) {
            SimpleClass po = getPOClass(table);
            SimpleClass dto = getDTOClass(table);
            clazz.add(po);
            clazz.add(dto);
        }
        RenderCodeView renderCodeView = new RenderCodeView(clazz);
        renderCodeView.setSize(800, 600);
        renderCodeView.setVisible(true);
        renderCodeView.setTitle("code generated");
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
        for (Field field : table.getFields()) {
            SimpleMethod setMethod = new SimpleMethod();
            setMethod.setName("set" + StringUtils.underlineToCamel(field.getName(), true));
            String fieldType = field.getFieldType();
            SimpleClass returnType = new SimpleClass();
            returnType.setName(fieldType);
            setMethod.setType(returnType);
            setMethod.setLine(Lists.newArrayList("return this." +
                    StringUtils.underlineToCamel(field.getName()) + ";"));
            SimpleMethod getMethod = new SimpleMethod();
            getMethod.setName("get" + StringUtils.underlineToCamel(field.getName(), true));
            LinkedHashMap<String, SimpleClass> params = Maps.newLinkedHashMap();
            params.put(StringUtils.underlineToCamel(field.getName()), returnType);
            getMethod.setParams(params);
            getMethod.setLine(Lists.newArrayList("this." + StringUtils.underlineToCamel(field.getName()) +
                    "=" + StringUtils.underlineToCamel(field.getName()) + ";"));
            methods.add(setMethod);
            methods.add(getMethod);
        }
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
//        for (Field field : table.getFields()) {
//            SimpleMethod setMethod = new SimpleMethod();
//            setMethod.setName("set" + StringUtils.underlineToCamel(field.getName(), true));
//            String fieldType = field.getFieldType();
//            SimpleClass returnType = new SimpleClass();
//            returnType.setName(fieldType);
//            setMethod.setType(returnType);
//            setMethod.setLine(Lists.newArrayList("return this." +
//                    StringUtils.underlineToCamel(field.getName()) + ";"));
//            SimpleMethod getMethod = new SimpleMethod();
//            getMethod.setName("get" + StringUtils.underlineToCamel(field.getName(), true));
//            LinkedHashMap<String, SimpleClass> params = Maps.newLinkedHashMap();
//            params.put(StringUtils.underlineToCamel(field.getName()), returnType);
//            getMethod.setParams(params);
//            getMethod.setLine(Lists.newArrayList("this." + StringUtils.underlineToCamel(field.getName()) +
//                    "=" + StringUtils.underlineToCamel(field.getName()) + ";"));
//            methods.add(setMethod);
//            methods.add(getMethod);
//        }
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
