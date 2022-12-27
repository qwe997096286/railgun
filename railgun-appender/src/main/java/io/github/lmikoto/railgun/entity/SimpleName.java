package io.github.lmikoto.railgun.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.lmikoto.railgun.utils.JavaUtils;
import io.github.lmikoto.railgun.utils.StringUtils;

/**
 * @author liuyang
 * 2021/3/7 2:57 下午
 */
public interface SimpleName {

    /**
     * name
     * @return
     */
    String getName();

    /**
     * simple name
     * @return
     */
    @JsonIgnore
    default String getSimpleName(){
        return JavaUtils.getSimpleName(getName());
    }
}
