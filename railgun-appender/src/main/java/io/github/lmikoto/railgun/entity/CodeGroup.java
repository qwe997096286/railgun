package io.github.lmikoto.railgun.entity;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author liuyang
 * 2021/3/7 7:22 下午
 */
@Data
@NoArgsConstructor
public class CodeGroup implements Serializable {

    private static final long serialVersionUID = 213432292323213214L;

    private String name;

    private List<CodeDir> dirs;
    private Map<String, Object> velocityContext = Maps.newHashMap();
    private ConfigModel configModel;
    public static CodeGroup fromName(String name) {
        CodeGroup codeGroup = new CodeGroup();
        codeGroup.setName(name);
        codeGroup.configModel = new ConfigModel();
        return codeGroup;
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
