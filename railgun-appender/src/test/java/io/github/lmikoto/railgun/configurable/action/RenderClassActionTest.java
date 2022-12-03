package io.github.lmikoto.railgun.configurable.action;

import io.github.lmikoto.railgun.sql.DefaultParser;
import io.github.lmikoto.railgun.sql.Parser;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.junit.Test;

import java.io.File;

public class RenderClassActionTest extends TestCase {


    private final String userDir = System.getProperty("user.dir");
    private final String testPath = "src/test/java/io/github/lmikoto/railgun/configurable/action";
    private final String configPath = userDir + File.separator + testPath + File.separator + "oracle_script.sql";
    private Parser parser = new DefaultParser();
    @Test
    @SneakyThrows
    public void testRun() {
//        RenderClassAction renderClassesAction = new RenderClassAction();
//        String table = new String(Files.readAllBytes(Paths.get(configPath)));
//        List<Table> tables = parser.parseSQLs(table);
//        renderClassesAction.setTables(tables);
//        renderClassesAction.run(null);
    }
}