package ${config.packageName}.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
#if(!!$pk)
import ${config.packageName}.domain.${pk.clazz.simpleName};
#end
import ${config.packageName}.domain.${po.simpleName};
#foreach($element in $po.fields)
#if($element.hasLabel("UNIQUE"))
#set($uniFlag = true)
#end
#end
#if(!!$uniFlag)
import me.zhengjie.exception.EntityExistException;
#end
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import ${config.packageName}.repository.${po.upCamelPOName}Repository;
import ${config.packageName}.service.${po.upCamelPOName}Service;
import ${config.packageName}.service.dto.${dto.simpleName};
import ${config.packageName}.service.mapstruct.${po.upCamelPOName}Mapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
#if($po.pk.clazz.name == 'Long')
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
#end
#if($po.pk.clazz.name == 'String')
import cn.hutool.core.util.IdUtil;
#end
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
#if(!$pk&& $po.pk.has2Import))
import ${po.pk.name}
#end
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
* @description $!po.comment业务层实现
* @author ${config.author}
* @date ${config.date}
**/
@Service
@RequiredArgsConstructor
public class ${po.upCamelPOName}ServiceImpl implements ${po.upCamelPOName}Service {

    private final ${po.upCamelPOName}Repository ${po.lowCamelPOName}Repository;
    private final ${po.upCamelPOName}Mapper ${po.lowCamelPOName}Mapper;

    @Override
    public Map<String,Object> queryAll(${dto.simpleName} criteria, Pageable pageable){
        Page<${po.simpleName}> page = ${po.lowCamelPOName}Repository.findAll((root, criteriaQuery, criteriaBuilder) ->
         QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(${po.lowCamelPOName}Mapper::toDto));
    }

    @Override
    public List<${dto.simpleName}> queryAll(${dto.simpleName} criteria){
        return ${po.lowCamelPOName}Mapper.toDto(${po.lowCamelPOName}Repository.findAll((root, criteriaQuery, criteriaBuilder)
         -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public ${dto.simpleName} findById(${po.pk.clazz.simpleName} ${po.pk.lowSimpleName}) {
        ${po.simpleName} ${po.lowCamelPOName} = ${po.lowCamelPOName}Repository.findById(${po.pk.lowSimpleName}).orElseGet(${po.simpleName}::new);
        ValidationUtil.isNull(${po.lowCamelPOName}.get${po.pk.upSimpleName}(),"${po.simpleName}","${po.pk.lowSimpleName}",${po.pk.lowSimpleName});
        return ${po.lowCamelPOName}Mapper.toDto(${po.lowCamelPOName});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ${dto.simpleName} create(${po.simpleName} resources) {
#if($po.pk.clazz.name == "Long")
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        resources.set${po.pk.upSimpleName}(snowflake.nextId());
#end
#if($po.pk.clazz.name == "String")
        resources.set${po.pk.upSimpleName}(IdUtil.simpleUUID());
#end
#foreach($element in $po.fields)
#if($element.hasLabel("UNIQUE"))
        if(${po.lowCamelPOName}Repository.findBy${element.upSimpleName}(resources.get${element.upSimpleName}()) != null){
            throw new EntityExistException(${po.simpleName}.class,"${element.comment}",resources.get${element.upSimpleName}());
        }
#end
#end
        return ${po.lowCamelPOName}Mapper.toDto(${po.lowCamelPOName}Repository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(${po.simpleName} resources) {
        ${po.simpleName} ${po.lowCamelPOName} = ${po.lowCamelPOName}Repository.findById(resources.get${po.pk.upSimpleName}()).orElseGet(${po.simpleName}::new);
        ValidationUtil.isNull( ${po.lowCamelPOName}.get${po.pk.upSimpleName}(),"${po.simpleName}","id",resources.get${po.pk.upSimpleName}());
#set($first = true)
#foreach($element in $po.fields)
#if($element.hasLabel("UNIQUE"))
#if($first == true)
        ${po.simpleName} ${po.lowCamelPOName}1;
#set($first = false)
#end
        ${po.lowCamelPOName}1 = ${po.lowCamelPOName}Repository.findBy${element.upSimpleName}(resources.get${element.upSimpleName}());
        if(${po.lowCamelPOName}1 != null && !${po.lowCamelPOName}1.get${element.upSimpleName}().equals(${po.lowCamelPOName}.get${element.upSimpleName}())){
            throw new EntityExistException(${po.simpleName}.class,"${element.comment}",resources.get${element.upSimpleName}());
        }
#end
#end
        BeanUtil.copyProperties(resources, ${po.lowCamelPOName}, CopyOptions.create().setIgnoreNullValue(true));
        ${po.lowCamelPOName}Repository.save(${po.lowCamelPOName});
    }

    @Override
    public void deleteAll(${po.pk.clazz.simpleName}[] ids) {
        for (${po.pk.clazz.simpleName} ${po.pk.name} : ids) {
            ${po.lowCamelPOName}Repository.deleteById(${po.pk.name});
        }
    }

    @Override
    public void download(List<${dto.simpleName}> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (${dto.simpleName} ${po.lowCamelPOName} : all) {
            Map<String,Object> map = new LinkedHashMap<>();
#foreach($element in $dto.fields)
#if($element.hasLabel("EXPORT"))
            map.put("${element.comment}",  ${po.lowCamelPOName}.get${element.upSimpleName}());
#end
#end
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
