package io.github.lmikoto.railgun.entity;

import com.github.javaparser.ast.Modifier;
import lombok.Data;

import java.util.List;

/**
 * @author jinwq
 * @Date 2022/11/23 14:27
 */
@Data
public class SimpleMethod {

    private String name;

    private SimpleClass type;

    private List<SimpleAnnotation> annotations;

    private List<Modifier.Keyword> modifiers;

    private List<SimpleField> params;

    private String comment;

    private List<String> line;
}