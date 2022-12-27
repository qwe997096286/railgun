package io.github.lmikoto.railgun.service;

import io.github.lmikoto.railgun.dto.CodeRenderTabDto;

import java.util.List;

/**
 * @author jinwq
 * @Time 2022/11/27
 */
public interface RenderCode {
    List<CodeRenderTabDto> execute(String text);

    String getRenderType();
}
