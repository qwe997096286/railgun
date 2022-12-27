package io.github.lmikoto.railgun.dao;

import com.google.common.base.Throwables;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/1 11:29
 */
@Slf4j
public class CodeGroupDao {

    private static int READING_SIZE = 1024;

    public static CodeGroup getGroup() {
        File dataFile = new File("./saveData/auto_data.text");
        String json = null;
        try {
            FileReader fileReader = new FileReader(dataFile);
            StringBuffer jsonContent = null;
            try {
                if (!dataFile.exists()) {
                    dataFile.createNewFile();
                }
                BufferedReader fis = new BufferedReader(fileReader);
                jsonContent = new StringBuffer();
                String bufferString = null;
                while (null != (bufferString = fis.readLine())) {
                    jsonContent.append(bufferString).append('\n');
                }
                json = jsonContent.toString();
            } catch (IOException e) {
                log.error(Throwables.getStackTraceAsString(e));
            }


            CodeGroup jsonObj = JsonUtils.fromJson(json, new JsonUtils.TypeReference<CodeGroup>() {
            });
            List<CodeDir> dirs = jsonObj.getDirs();
            if (CollectionUtils.isNotEmpty(dirs)) {
                List<CodeTemplate> templates = dirs.stream().map(CodeDir::getTemplates).filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(templates)) {
                    templates.forEach(template -> {
                        String path = null;
                        if (StringUtils.isNotEmpty(template.getDir())) {
                            path = template.getDir() + "/";
                        } else {
                            path = "saveData/";
                        }
                        File file = new File(path + template.getName());
                        StringBuffer fileContent = null;
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            BufferedReader fis = new BufferedReader(new FileReader(file));
                            fileContent = new StringBuffer();
                            String bufferString = null;
                            while (null != (bufferString = fis.readLine())) {
                                fileContent.append(bufferString).append('\n');
                            }
                        } catch (IOException e) {
                            log.error(Throwables.getStackTraceAsString(e));
                        }

                        template.setContent(fileContent.toString());
                    });
                }
            }
            return jsonObj;
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
        }

        return null;
    }
}
