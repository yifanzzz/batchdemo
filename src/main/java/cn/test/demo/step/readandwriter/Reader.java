package cn.test.demo.step.readandwriter;

import cn.test.demo.data.entity.Product;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Reader{

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Bean(name = "myreader")
    @StepScope
    public MyBatisPagingItemReader<Product> reader() {
        MyBatisPagingItemReader myBatisPagingItemReader = new MyBatisPagingItemReader();
        myBatisPagingItemReader.setQueryId("cn.test.demo.data.ProductDao.selectAll");
        myBatisPagingItemReader.setSqlSessionFactory(sqlSessionFactory);
        return myBatisPagingItemReader;
    }

}
