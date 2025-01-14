package ${config.packageName}.rest;

import me.zhengjie.annotation.Log;
#if(!!$pk)
import ${config.packageName}.domain.${pk.clazz.simpleName};
#end
import ${config.packageName}.domain.${po.simpleName};
import ${config.packageName}.service.${po.upCamelPOName}Service;
import ${config.packageName}.service.dto.${dto.simpleName};
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
#if(!$pk&& $po.pk.has2Import)
import ${po.pk.name}
#end
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @description $!po.comment控制层
* @author ${config.author}
* @date ${config.date}
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "${po.comment}管理")
@RequestMapping("/api/${po.lowCamelPOName}")
public class ${po.upCamelPOName}Controller {

    private final ${po.upCamelPOName}Service ${po.lowCamelPOName}Service;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('${po.lowCamelPOName}:list')")
    public void export${po.upCamelPOName}(HttpServletResponse response, ${dto.simpleName} criteria) throws IOException {
        ${po.lowCamelPOName}Service.download(${po.lowCamelPOName}Service.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询${po.comment}")
    @ApiOperation("查询${po.comment}")
    @PreAuthorize("@el.check('${po.lowCamelPOName}:list')")
    public ResponseEntity<Object> query${po.upCamelPOName}(${dto.simpleName} criteria, Pageable pageable){
        PageRequest of = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        return new ResponseEntity<>(${po.lowCamelPOName}Service.queryAll(criteria, of), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增${po.comment}")
    @ApiOperation("新增${po.comment}")
    @PreAuthorize("@el.check('${po.lowCamelPOName}:add')")
    public ResponseEntity<Object> create${po.upCamelPOName}(@Validated @RequestBody ${po.simpleName} resources){
        return new ResponseEntity<>(${po.lowCamelPOName}Service.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改${po.comment}")
    @ApiOperation("修改${po.comment}")
    @PreAuthorize("@el.check('${po.lowCamelPOName}:edit')")
    public ResponseEntity<Object> update${po.upCamelPOName}(@Validated @RequestBody ${po.simpleName} resources){
        ${po.lowCamelPOName}Service.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除${po.comment}")
    @ApiOperation("删除${po.comment}")
    @PreAuthorize("@el.check('${po.lowCamelPOName}:del')")
    public ResponseEntity<Object> delete${po.upCamelPOName}(@RequestBody ${po.pk.clazz.simpleName}[] ids) {
        ${po.lowCamelPOName}Service.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
