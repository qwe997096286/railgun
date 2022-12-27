package io.github.lmikoto.railgun.entity;

import lombok.Data;

/**
 * @author liuyang
 * 2021/3/7 1:02 下午
 */
@Data
public class SimpleAnnotation implements SimpleName {

    /**
     * 全路径名
     */
    private String name;

    private String expr;

    public SimpleAnnotation() {
    }

    public SimpleAnnotation(String name, String expr) {
        this.name = name;
        this.expr = expr;
    }
}