package io.github.lmikoto.railgun.utils;

import io.github.lmikoto.railgun.dao.DataCenter;
import io.github.lmikoto.railgun.dict.SimpleDict;
import io.github.lmikoto.railgun.entity.ConfigModel;
import io.github.lmikoto.railgun.entity.SimpleAnnotation;
import io.github.lmikoto.railgun.entity.SimpleClass;
import lombok.Data;

import java.util.Map;

/**
 * @author jinwq
 * @Date 2022/12/22 16:39
 */
@Data
public class JavaBuilder {
    private SimpleClass clazz;
    public void initSimpleClass() {
        Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
        SimpleClass controller = new SimpleClass();
        SimpleClass service = new SimpleClass();
        SimpleClass dao = new SimpleClass();
        velocityContext.put(SimpleDict.CONTROLLER, controller);
        velocityContext.put(SimpleDict.SERVICE, service);
        velocityContext.put(SimpleDict.DAO, dao);
    }
    public String buildController() {
        ConfigModel configModel = DataCenter.getConfigModel();
        Map<String, Object> velocityContext = DataCenter.getCurrentGroup().getVelocityContext();
        SimpleClass simpleClass = (SimpleClass) velocityContext.get(SimpleDict.CONTROLLER);
        Object dto = velocityContext.get("dto");
        if (!(dto instanceof SimpleClass)) {
            return "";
        }
        //构造controller类
        SimpleClass domain = (SimpleClass) dto;
        simpleClass.setName(configModel.getControllerPackage() + "." + domain.getSimpleName() + "Controller");
        SimpleAnnotation requestMapping = new SimpleAnnotation();

        /*requestMapping.setName("org.springframework.web.bind.annotation.RequestMapping");
        requestMapping.setExpr(String.format("@RequestMapping(\"/%s\"/%s)", StringUtils.camelToCamel(StringUtils.camelToSub(domain
                .getSimpleName(), 1,1), false), StringUtils.camelToCamel(StringUtils.camelToSub(domain
                .getSimpleName(), 2,3), false)));
        List<SimpleAnnotation> annotations = Lists.newArrayList(new SimpleAnnotation("org.springframework.stereotype" +
                ".Controller", "@Controller"), requestMapping);
        simpleClass.setAnnotations();
        //构造controller属性
        LinkedHashMap<String, SimpleField> fields = Maps.newLinkedHashMapWithExpectedSize(1);
        SimpleField simpleField = new SimpleField();
        SimpleClass service = (SimpleClass) velocityContext.get(SimpleDict.SERVICE);
        String fieldName = StringUtils.camelToCamel(service.getSimpleName(), false);
        simpleField.setName(fieldName);
        simpleField.setModifiers(Collections.singletonList(Modifier.Keyword.PRIVATE));
        simpleField.setClazz(service);
        SimpleAnnotation autowiredAnn = new SimpleAnnotation();
        autowiredAnn.setExpr("@Autowired");
        autowiredAnn.setName("org.springframework.beans.factory.annotation.Autowired");
        simpleField.setAnnotations(Collections.singletonList(autowiredAnn));
        fields.put(fieldName, simpleField);
        simpleClass.setFields(fields);
        //构造导出方法
        if (configModel.isHasExport()) {
            SimpleMethod simpleMethod = new SimpleMethod();
            List<SimpleField> params = Lists.newArrayListWithExpectedSize(2);
            List<String> lines = Lists.newArrayList(String.format("        %sService.download(%sService" +
                    ".queryAll(criteria), response);", fieldName, fieldName));
            simpleMethod.setLine(lines);
            SimpleField param1 = new SimpleField();
            param1.setName("response");
            param1.setClazz(new SimpleClass("javax.servlet.http.HttpServletResponse");
            params.put("response", param1);
            new SimpleField()
            simpleMethod.setParams(params);
        }*/
        return null;
    }
}
