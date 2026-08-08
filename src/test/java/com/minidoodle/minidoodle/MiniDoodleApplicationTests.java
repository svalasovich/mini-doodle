package com.minidoodle.minidoodle;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MiniDoodleApplicationTests {

  @Test
  void contextLoads() {}
}
