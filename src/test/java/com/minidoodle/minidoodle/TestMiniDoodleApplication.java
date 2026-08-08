package com.minidoodle.minidoodle;

import org.springframework.boot.SpringApplication;

public class TestMiniDoodleApplication {

  public static void main(String[] args) {
    SpringApplication.from(MiniDoodleApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
