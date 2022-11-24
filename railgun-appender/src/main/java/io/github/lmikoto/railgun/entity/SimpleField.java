package io.github.lmikoto.railgun.entity;

import com.github.javaparser.ast.Modifier;
import lombok.Data;

import java.util.List;

/**
 * @author jinwq
 * @Date 2022/11/23
 */
@Data
public class SimpleField implements SimpleName {

    private String name;

    private SimpleClass clazz;

    private List<Modifier.Keyword> modifiers;

    private List<SimpleAnnotation> annotations;

    private String expr;

    private String comment;
}
