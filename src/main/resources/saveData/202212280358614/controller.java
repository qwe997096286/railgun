package ${config.packageName}.syste.rest;

import me.zhengjie.annotation.Log;
import ${config.packageName}.domain.${po.getSimpleName()};
import ${config.packageName}.service.${po.getUpCamelPOName()}Service;
import ${config.packageName}.service.dto.${po.getUpCamelPOName()}QueryCriteria;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
* @website https://eladmin.vip
* @author ${config.author}
* @date ${config.date}
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "${apiAlias}管理")
@RequestMapping("/api/${po.getLowCamelPOName()}")
public class ${po.getUpCamelPOName()}Controller {

    private final ${po.getUpCamelPOName()}Service ${po.getLowCamelPOName()}Service;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('${po.getLowCamelPOName()}:list')")
    public void export${po.getUpCamelPOName()}(HttpServletResponse response, ${po.getUpCamelPOName()}QueryCriteria criteria) throws IOException {
        ${po.getLowCamelPOName()}Service.download(${po.getLowCamelPOName()}Service.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询${apiAlias}")
    @ApiOperation("查询${apiAlias}")
    @PreAuthorize("@el.check('${po.getLowCamelPOName()}:list')")
    public ResponseEntity<Object> query${po.getUpCamelPOName()}(${po.getUpCamelPOName()}QueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(${po.getLowCamelPOName()}Service.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增${apiAlias}")
    @ApiOperation("新增${apiAlias}")
    @PreAuthorize("@el.check('${po.getLowCamelPOName()}:add')")
    public ResponseEntity<Object> create${po.getUpCamelPOName()}(@Validated @RequestBody ${dto.getSimpleName()} resources){
        return new ResponseEntity<>(${po.getLowCamelPOName()}Service.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改${apiAlias}")
    @ApiOperation("修改${apiAlias}")
    @PreAuthorize("@el.check('${po.getLowCamelPOName()}:edit')")
    public ResponseEntity<Object> update${po.getUpCamelPOName()}(@Validated @RequestBody ${dto.getSimpleName()} resources){
        ${po.getLowCamelPOName()}Service.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除${apiAlias}")
    @ApiOperation("删除${apiAlias}")
    @PreAuthorize("@el.check('${po.getLowCamelPOName()}:del')")
    public ResponseEntity<Object> delete${po.getUpCamelPOName()}(@RequestBody ${pkColumnType}[] ids) {
        ${po.getLowCamelPOName()}Service.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
