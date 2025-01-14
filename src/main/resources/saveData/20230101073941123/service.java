package ${config.packageName}.service;

#if(!!$pk)
import ${config.packageName}.domain.${pk.clazz.simpleName};
#end
import ${config.packageName}.domain.${po.simpleName};
import ${config.packageName}.service.dto.${dto.simpleName};
import org.springframework.data.domain.Pageable;
#if(!$pk&& $po.pk.has2Import))
import ${po.pk.name}
#end
import java.util.Map;
import java.util.List;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @description $!po.comment业务层
* @author ${config.author}
* @date ${config.date}
**/
public interface ${po.upCamelPOName}Service {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    Map<String,Object> queryAll(${dto.simpleName} criteria, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List<${className}Dto>
    */
    List<${dto.simpleName}> queryAll(${dto.simpleName} criteria);

    /**
     * 根据ID查询
     * @param ${po.pk.clazz.lowSimpleName} ID
     * @return ${dto.simpleName}
     */
    ${dto.simpleName} findById(${po.pk.clazz.simpleName} ${po.pk.lowSimpleName});

    /**
    * 创建
    * @param resources /
    * @return ${className}Dto
    */
    ${dto.simpleName} create(${po.simpleName} resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(${po.simpleName} resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(${po.pk.clazz.simpleName}[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<${dto.simpleName}> all, HttpServletResponse response) throws IOException;
}
