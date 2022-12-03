package io.github.lmikoto.railgun.dto;

import lombok.Data;

/**
 * @author jinwq
 * @Date 2022/11/25 14:32
 */
@Data
public class CodeRenderTabDto {
    private String tabName;
    private String tabContent;

    public CodeRenderTabDto(String tabName, String tabContent) {
        this.tabName = tabName;
        this.tabContent = tabContent;
    }
}
