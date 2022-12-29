package io.github.lmikoto.railgun.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * @author liuyang
 * 2021/3/7 7:23 下午
 */
@Data
public class CodeTemplate implements Serializable {

    private static final long serialVersionUID = 213432876213214L;
    private String name;
    private String type;
    @JsonIgnore
    private String content;
    @JsonIgnore
    private String dir;
    public static CodeTemplate fromName(String name){
        return CodeTemplate.fromName(name, "template");
    }
    public static CodeTemplate fromName(String name, String type){
        CodeTemplate codeTemplate = new CodeTemplate();
        codeTemplate.setName(name);
        codeTemplate.setType(type);
        return codeTemplate;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CodeTemplate)) {
            return false;
        } else {
            CodeTemplate other = (CodeTemplate) o;
            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
                if (other$name != null) {
                    return false;
                }
            } else if (!this$name.equals(other$name)) {
                return false;
            }
            return true;

        }
    }

    @Override
    public int hashCode() {
        int result = 1;
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        return result;
    }
}
