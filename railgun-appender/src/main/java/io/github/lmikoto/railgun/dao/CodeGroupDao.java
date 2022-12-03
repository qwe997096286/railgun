package io.github.lmikoto.railgun.dao;

import com.google.common.base.Throwables;
import io.github.lmikoto.railgun.utils.JsonUtils;
import io.github.lmikoto.railgun.entity.CodeGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;

/**
 * @author jinwq
 * @Date 2022/12/1 11:29
 */
@Slf4j
public class CodeGroupDao {
    public static CodeGroup getGroup() {
        File dataFile = new File("./saveData/auto_data.text");
        try {
            FileReader fileReader = new FileReader(dataFile);

            char[] jsonChar = {};
            int amt = fileReader.read(jsonChar);

            CodeGroup jsonObj = JsonUtils.fromJson(new String(jsonChar), new JsonUtils.TypeReference<CodeGroup>() {
            });
            return jsonObj;
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
        }

        return null;
    }
}
