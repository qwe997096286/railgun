package io.github.lmikoto.railgun.service;

/**
 * @author jinwq
 * @Time 2022/11/27
 */
public interface RenderCode {
    void execute(String text);

    String getRenderType();
}
