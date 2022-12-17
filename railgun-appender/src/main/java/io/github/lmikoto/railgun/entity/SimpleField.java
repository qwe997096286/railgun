package io.github.lmikoto.railgun.entity;

import com.github.javaparser.ast.Modifier;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

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

    private String label;

    public boolean hasLabel(String expect) {
        if (StringUtils.isEmpty(label)) {
            return false;
        }
        return label.contains(expect + "&");
    }

    public boolean addLabel(String label) {
        if (this.label == null) {
            this.label = label + "&";
        } else {
            this.label += label + "&";
        }
        return true;
    }
}
