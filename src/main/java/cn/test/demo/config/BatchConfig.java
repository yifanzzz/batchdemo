package cn.test.demo.config;

import cn.test.demo.data.BaseEntity;
import cn.test.demo.data.entity.Product;
import cn.test.demo.step.support.ClearCompositeItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    ItemProcessor processor;

    @Bean(name = "job1")
    public Job job1(@Qualifier("step1") Step step1) {
        return jobBuilderFactory.get("job1")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }

    @Bean(name = "step1")
    public Step step1(@Qualifier("myreader") MyBatisPagingItemReader<Product> myreader,
                      @Qualifier("mywriter") ClearCompositeItemWriter<BaseEntity> mywriter) {
        return stepBuilderFactory.get("step1")
                .<Product, BaseEntity> chunk(1)
                .reader(myreader)
                .processor(processor)
                .writer(mywriter)
                .build();
        /*return new MyStepBuilder("step1").chunk(1)
                .reader(myreader)
                .processor(processor)
                .writer(mywriter)
                .build();*/
    }
}
