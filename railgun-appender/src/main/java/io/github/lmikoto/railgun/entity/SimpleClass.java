package io.github.lmikoto.railgun.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JavaUtils;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.Data;

import java.util.*;

/**
 * @author lmikoto
 */
@Data
public class SimpleClass implements SimpleName {

    private Set<String> imports;

    /**
     * class name
     */
    private String name;

    private List<SimpleAnnotation> annotations;

    private List<SimpleClass> extend;

    private List<SimpleClass> impl;

    private SimpleClass pk;

    private List<SimpleMethod> methods;

    private LinkedHashMap<String,SimpleField> fields;

    private LinkedHashMap<String,List<SimpleAnnotation>> fieldsAnno;

    private List<String> modifiers;

    private String comment;

    public SimpleClass() {
    }

    public SimpleClass(String name) {
        this.name = name;
    }

    public Set<String> getImports(){
        if(CollectionUtils.isEmpty(imports)){
            imports = new HashSet<>();
        }
        return imports;
    }

    @JsonIgnore
    public String getLowCamelPOName(){
        return StringUtils.camelToCamel(StringUtils.camelToSub(JavaUtils.getSimpleName(getName()),
                1, 3), false);
    }
    @JsonIgnore
    public String getUpCamelPOName(){
        return StringUtils.camelToCamel(StringUtils.camelToSub(JavaUtils.getSimpleName(getName()),
                1, 3), true);
    }
}
