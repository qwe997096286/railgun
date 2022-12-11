package io.github.lmikoto.railgun.service.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.github.lmikoto.railgun.componet.RenderCodeView;
import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dto.CodeRenderTabDto;
import io.github.lmikoto.railgun.entity.CodeTemplate;
import io.github.lmikoto.railgun.entity.SetCurTemplate;
import io.github.lmikoto.railgun.entity.dict.TemplateDict;
import io.github.lmikoto.railgun.service.RenderCode;
import io.github.lmikoto.railgun.utils.NotificationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author jinwq
 * @Date 2022/12/9 11:08
 */
@Slf4j
public class RenderVm2file implements RenderCode, SetCurTemplate
{
    private CodeTemplate curTemplate;
    @Override
    public void execute(String text) {
        File file = new File(curTemplate.getName());
        FileOutputStream fileStream = null;
        try {
            fileStream = new FileOutputStream(file);
            fileStream.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Throwables.getStackTraceAsString(e);
            NotificationUtils.simpleNotify("渲染模版时，导出人文件失败");
            return;
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
        velocityEngine.setProperty("input.encoding", "UTF-8");
        velocityEngine.setProperty("output.encoding", "UTF-8");
        velocityEngine.init();
        Template template = velocityEngine.getTemplate(file.getAbsoluteFile().getAbsolutePath(), "UTF-8");
        StringWriter sw = new StringWriter();
        template.merge(new VelocityContext(DataCenter.getCurrentGroup().getVelocityContext()), sw);
        StringBuffer buffer = sw.getBuffer();
        CodeRenderTabDto tabDto = new CodeRenderTabDto();
        tabDto.setTabContent(buffer.toString());
        tabDto.setTabName("template");
        RenderCodeView renderCodeView = new RenderCodeView(Lists.newArrayList(tabDto));
        renderCodeView.setSize(800, 600);
        renderCodeView.setVisible(true);
        renderCodeView.setTitle("code generated");
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
