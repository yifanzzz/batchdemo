package cn.test.demo.step.readandwriter;

import cn.test.demo.data.BaseEntity;
import cn.test.demo.data.entity.Detail;
import cn.test.demo.data.entity.Product;
import cn.test.demo.step.support.ClearCompositeItemWriter;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class Processor implements ItemProcessor<Product, BaseEntity> {

    @Autowired
    MyBatisBatchItemWriter productWriter;

    @Autowired
    MyBatisBatchItemWriter detailWriter;

    @Autowired
    ClearCompositeItemWriter clearCompositeItemWriter;

    @Override
    public BaseEntity process(Product product) throws Exception {
        if(product.getName().equals("aaa")){
            Product prod = new Product();
            BeanUtils.copyProperties(product,prod);
            prod.setDescription(prod.getDescription()+"-aaa");
            prod.setId(prod.getId()+10);
            List list = new ArrayList();
            list.add(productWriter);
            clearCompositeItemWriter.setActualList(list);
            return prod;
        }else{
            Detail detail = new Detail();
            detail.setId(product.getId()+10);
            detail.setProductName(product.getName()+"-detail");
            List list = new ArrayList();
            list.add(detailWriter);
            clearCompositeItemWriter.setActualList(list);
            return detail;
        }
    }
}
