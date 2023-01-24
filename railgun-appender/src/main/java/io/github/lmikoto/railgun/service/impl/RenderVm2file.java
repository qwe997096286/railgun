package io.github.lmikoto.railgun.service.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.SimpleClass;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import io.github.lmikoto.railgun.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author jinwq
 * @Date 2022/12/9 11:08
 */
@Slf4j
public class RenderVm2file implements RenderCode, SetCurTemplate
{
    private CodeTemplate curTemplate;
    @Override
    public List<CodeRenderTabDto> execute(String text) {
        File file = new File("temp_template/" + curTemplate.getName());
        FileOutputStream fileStream = null;
        try {
            if (!file.exists()) {
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdir();
                }
                file.createNewFile();
            }
            fileStream = new FileOutputStream(file);
            fileStream.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Throwables.getStackTraceAsString(e);
            NotificationUtils.simpleNotify("渲染模版时，导出人文件失败");
            return null;
        } finally {
            try {
                fileStream.flush();
                fileStream.close();
            } catch (IOException e) {
                Throwables.getStackTraceAsString(e);
                NotificationUtils.simpleNotify("渲染模版时，关闭文件流失败");
            }
        }
        VelocityEngine velocityEngine = DataCenter.getVelocityEngine();
        //设置velocity资源加载方式为file
        velocityEngine.setProperty("resource.loader", "file");
        //设置velocity资源加载方式为file时的处理类
        velocityEngine.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        String absolutePath = file.getAbsolutePath();
        velocityEngine.setProperty("file.resource.loader.path", absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)));
        velocityEngine.setProperty("output.encoding", "UTF-8");
        velocityEngine.setProperty("input.encoding", "UTF-8");
        velocityEngine.init();
        StringWriter sw = new StringWriter();
        Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
        Template template = velocityEngine.getTemplate(curTemplate.getName(), "UTF-8");
        velocityContext.put("config", DataCenter.getConfigModel());
        template.merge(new VelocityContext(velocityContext), sw);
        StringBuffer buffer = sw.getBuffer();
        String name = curTemplate.getName();
        SimpleClass po = (SimpleClass) velocityContext.get("po");
        if (name.contains("${po.lowCamelPOName}")) {
            name = name.replace("${po.lowCamelPOName}", po.getLowCamelPOName());
        } else if (po != null && name.endsWith(".java")) {
            name = po.getUpCamelPOName() + StringUtils.camelToCamel(name, true);
        }
        CodeRenderTabDto tabDto = new CodeRenderTabDto();
        tabDto.setTabContent(buffer.toString());
        tabDto.setTabName(name);
        return Lists.newArrayList(tabDto);
    }

    @Override
    public String getRenderType() {
        return TemplateDict.VM2FILE;
    }

    @Override
    public void setTemplate(CodeTemplate codeTemplate) {
        this.curTemplate = codeTemplate;
    }
}
