package io.github.lmikoto.railgun.dao;

import com.google.common.base.Throwables;
import io.github.lmikoto.railgun.componet.NameEditDialog;
import io.github.lmikoto.railgun.entity.CodeDir;
import io.github.lmikoto.railgun.entity.CodeGroup;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.ConfigModel;
import io.github.lmikoto.railgun.utils.CollectionUtils;
import io.github.lmikoto.railgun.utils.JsonUtils;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author jinwq
 * @Date 2022/12/1 11:29
 */
@Slf4j
public class CodeGroupDao {

    private static int READING_SIZE = 1024;

    public static CodeGroup getGroup() {
        return getGroup("." + File.separator + "saveData" + File.separator + "auto_data.text");
    }
    public static CodeGroup getGroup(String dirStr) {
        File dataFile = new File(dirStr);
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
                log.error("读取json文件失败" + Throwables.getStackTraceAsString(e));
            }

            if (StringUtils.isEmpty(json)) {
                return null;
            }
            CodeGroup jsonObj = JsonUtils.fromJson(json, new JsonUtils.TypeReference<CodeGroup>() {
            });
            List<CodeDir> dirs = jsonObj.getDirs();
            if (CollectionUtils.isNotEmpty(dirs)) {
                List<CodeTemplate> templates = dirs.stream().map(dir -> {
                    List<CodeTemplate> setDirTemplate = dir.getTemplates();
                    if (CollectionUtils.isNotEmpty(setDirTemplate)) {
                        setDirTemplate.forEach( codeTemplate -> codeTemplate.setDir(dir.getRegTime()));
                    }
                    return setDirTemplate;
                }).flatMap(List::stream).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(templates)) {
                    templates.forEach(template -> {
                        String path = null;
                        String saveDir = Optional.ofNullable(jsonObj.getConfigModel()).map(ConfigModel::
                                getSaveFileDir).orElse("./");
                        if (StringUtils.isNotEmpty(template.getDir())) {
                            path = saveDir + template.getDir() + File.separator;
                        } else {
                            path = saveDir + "saveData" + File.separator;
                        }
                        File file = new File(path + template.getName());
                        StringBuffer fileContent = null;
                        try {
                            if (!file.exists()) {
                                return;
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
            if (DataCenter.getGroupList() == null) {
                return jsonObj;
            }
            while (DataCenter.getGroupList().contains(jsonObj) && !"Canceled".equals(jsonObj.getName())) {
                NameEditDialog dialog = new NameEditDialog();
                dialog.setTitle("Create Group");
                dialog.getNameField().setText(jsonObj.getName());
                dialog.getButtonOK().addActionListener(e -> {
                    String name = dialog.getNameField().getText();
                    if (StringUtils.isBlank(name)) {
                        NotificationUtils.simpleNotify("Group can't be empty");
                        return;
                    }
                    jsonObj.setName(name);
                    dialog.setVisible(false);
                });
                dialog.getButtonCancel().addActionListener(e -> {
                    NotificationUtils.simpleNotify("already exist group，can not create.");
                    jsonObj.setName("Canceled");
                });
                dialog.setSize(300, 150);
                dialog.setVisible(true);
                while (dialog.isVisible() && !"Canceled".equals(jsonObj.getName())) {
                    Thread.sleep(1000);
                }
                if ("Canceled".equals(jsonObj.getName())) {
                    NotificationUtils.simpleNotify("import canceled");
                    throw new RuntimeException("import canceled ");
                }
            }
            return jsonObj;
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
        }

        return null;
    }
}
