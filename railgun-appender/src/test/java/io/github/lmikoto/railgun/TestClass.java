package io.github.lmikoto.railgun;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestClass extends TestSupperClass {

    TestClass test1;

    public void test(TestSupperClass test1) {
    }
}
