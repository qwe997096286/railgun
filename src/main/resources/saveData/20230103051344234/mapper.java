package ${config.packageName}.service.mapstruct;

import me.zhengjie.base.BaseMapper;
import ${config.packageName}.domain.${po.simpleName};
import ${config.packageName}.service.dto.${dto.simpleName};
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @description ${po.comment}映射工具
* @author ${author}
* @date ${date}
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ${po.upCamelPOName}Mapper extends BaseMapper<${dto.simpleName}, ${po.simpleName}> {

}
