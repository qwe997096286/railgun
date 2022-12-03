package io.github.lmikoto.railgun;

import io.github.lmikoto.railgun.model.Table;
import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.utils.Appender;
import io.github.lmikoto.railgun.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author liuyang
 * 2021/3/6 4:23 下午
 */
@Slf4j
public class RailgunTest {

    private final Appender railgun = new Appender();

    private final String userDir = System.getProperty("user.dir");
    private final String testPath = "src/test/java/io/github/lmikoto/railgun";
    private final String configPath = userDir + File.separator + testPath + File.separator + "config.json";

    @Test
    @SneakyThrows
    public void gen(){
        String config = new String(Files.readAllBytes(Paths.get(configPath)));
        railgun.fullProcess(config,testPath + File.separator + "TestClass.java");
    }

    @Test
    public void append(){

    }

    @Test
    public void parseSql(){
        DefaultParser defaultParser = new DefaultParser();
        List<Table> tables = defaultParser.parseSQLs("CREATE TABLE `code_gen_config` (\n" +
                "  `config_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
                "  `table_name` varchar(255) DEFAULT NULL COMMENT '表名',\n" +
                "  `author` varchar(255) DEFAULT NULL COMMENT '作者',\n" +
                "  `cover` bit(1) DEFAULT NULL COMMENT '是否覆盖',\n" +
                "  `module_name` varchar(255) DEFAULT NULL COMMENT '模块名称',\n" +
                "  `pack` varchar(255) DEFAULT NULL COMMENT '至于哪个包下',\n" +
                "  `path` varchar(255) DEFAULT NULL COMMENT '前端代码生成的路径',\n" +
                "  `api_path` varchar(255) DEFAULT NULL COMMENT '前端Api文件路径',\n" +
                "  `prefix` varchar(255) DEFAULT NULL COMMENT '表前缀',\n" +
                "  `api_alias` varchar(255) DEFAULT NULL COMMENT '接口名称',\n" +
                "  PRIMARY KEY (`config_id`) USING BTREE,\n" +
                "  KEY `idx_table_name` (`table_name`(100))\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='代码生成器配置';");
        log.info(JsonUtils.toPrettyJson(tables));

    }

}
