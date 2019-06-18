package cn.test.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableBatchProcessing
@MapperScan("cn.test.demo.data")
public class SprinpBatchApplication
{
        public static void main(String[] args) {
            SpringApplication.run(SprinpBatchApplication.class, args);
        }
}
