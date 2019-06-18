package cn.test.demo.step.readandwriter;

import cn.test.demo.data.entity.Detail;
import cn.test.demo.data.entity.Product;
import cn.test.demo.step.support.ClearCompositeItemWriter;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Writer {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Bean(name ="mywriter")
    @StepScope
    public ClearCompositeItemWriter<?> interactionsItemWriter(@Qualifier("productWriter") MyBatisBatchItemWriter productWriter,
                                                              @Qualifier("detailWriter") MyBatisBatchItemWriter detailWriter) {
        ClearCompositeItemWriter clearCompositeItemWriter = new ClearCompositeItemWriter();
        List<ItemWriter<?>> writers = new ArrayList<>(2);
        writers.add(productWriter);
        writers.add(detailWriter);
        clearCompositeItemWriter.setDelegates(writers);
        return clearCompositeItemWriter;
    }

    @Bean(name = "productWriter")
    public MyBatisBatchItemWriter<Product> productWriter(){
        MyBatisBatchItemWriter<Product> writer = new MyBatisBatchItemWriter();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("cn.test.demo.data.ProductDao.insert");
        return writer;
    }

    @Bean(name = "detailWriter")
    public MyBatisBatchItemWriter<Detail> detailWriter(){
        MyBatisBatchItemWriter<Detail> writer = new MyBatisBatchItemWriter();
        writer.setSqlSessionFactory(sqlSessionFactory);
        writer.setStatementId("cn.test.demo.data.DetailDao.insertDetail");
        return writer;
    }


}
