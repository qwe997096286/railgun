package io.github.lmikoto.railgun.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuyang
 * 2021/3/7 7:23 下午
 */
@Data
public class CodeDir implements Serializable {

    private static final long serialVersionUID = 213432213123213214L;
    private String name;
    private List<CodeTemplate> templates;

    public static CodeDir fromName(String name) {
        CodeDir codeDir = new CodeDir();
        codeDir.setName(name);
        return codeDir;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof CodeGroup)) {
            return false;
        } else {
            CodeGroup other = (CodeGroup) o;
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
