package org.springframework.batch.core.configuration.annotation;

import cn.test.demo.step.builder.MyStepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

public class StepBuilderFactory {

    private JobRepository jobRepository;

    private PlatformTransactionManager transactionManager;

    public StepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    /**
     * Creates a step builder and initializes its job repository and transaction manager. Note that if the builder is
     * used to create a &#64;Bean definition then the name of the step and the bean name might be different.
     *
     * @param name the name of the step
     * @return a step builder
     */
    public MyStepBuilder get(String name) {
        MyStepBuilder builder = new MyStepBuilder(name).repository(jobRepository).transactionManager(
                transactionManager);
        return builder;
    }

}
