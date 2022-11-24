package io.github.lmikoto.railgun.entity;

import io.github.lmikoto.railgun.utils.CollectionUtils;
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

    private List<SimpleMethod> methods;

    private LinkedHashMap<String,SimpleField> fields;

    private LinkedHashMap<String,List<SimpleAnnotation>> fieldsAnno;

    private List<String> modifiers;

    private String comment;

    public Set<String> getImports(){
        if(CollectionUtils.isEmpty(imports)){
            imports = new HashSet<>();
        }
        return imports;
    }

}
