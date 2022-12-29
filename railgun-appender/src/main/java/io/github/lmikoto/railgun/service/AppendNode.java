package io.github.lmikoto.railgun.service;

import io.github.lmikoto.railgun.entity.CodeGroup;

import java.util.List;

/**
 * @author jinwq
 * @Date 2022/12/28 09:19
 */
public interface AppendNode {
    void saveData2Tree(List<CodeGroup> groupByFile);
}
