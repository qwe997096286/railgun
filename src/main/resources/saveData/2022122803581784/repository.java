package ${config.packageName}.repository;

#if(!!$pk)
import ${config.packageName}.domain.${pk.clazz.simpleName};
#end
import ${config.packageName}.domain.${po.simpleName};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
#if(!$pk&& $po.pk.has2Import)
import ${po.pk.name}
#end

/**
* @description $!po.comment持久层
* @author ${config.author}
* @date ${config.date}
**/
public interface ${po.upCamelPOName}Repository extends JpaRepository<${po.simpleName}, ${po.pk.clazz.simpleName}>,
 JpaSpecificationExecutor<${po.simpleName}> {
#foreach($element in $po.fields)
#if($element.hasLabel("UNIQUE"))
    /**
    * 根据 ${element.comment} 查询
    * @param ${element.name} /
    * @return /
    */
    ${po.simpleName} findBy${element.upSimpleName}(${element.clazz.simpleName} ${element.name});
#end
#end
}
