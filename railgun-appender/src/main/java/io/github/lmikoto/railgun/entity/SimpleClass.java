package io.github.lmikoto.railgun.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JavaUtils;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.Data;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

    private SimpleField pk;

    private List<SimpleMethod> methods;

    private LinkedHashMap<String,SimpleField> fields;

    @Deprecated
    private LinkedHashMap<String,List<SimpleAnnotation>> fieldsAnno;

    private List<ConstructorDeclaration> constructorList;

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
    public String getLowCamelPOName() {
        return StringUtils.camelToCamel(StringUtils.camelToSub(JavaUtils.getSimpleName(getName()),
                1, 3), false);
    }
    @JsonIgnore
    public String getUpCamelPOName() {
        return StringUtils.camelToCamel(StringUtils.camelToSub(JavaUtils.getSimpleName(getName()),
                1, 3), true);
    }

    @JsonIgnore
    public String getSubName(int begin, int end) {
        return StringUtils.camelToUnderline(StringUtils.camelToSub(JavaUtils.getSimpleName(getName()),
                begin, end));
    }

    public boolean has2Import() {
        if (StringUtils.isNotEmpty(this.name)) {
            return false;
        }
        return this.name.contains(".");
    }
}
