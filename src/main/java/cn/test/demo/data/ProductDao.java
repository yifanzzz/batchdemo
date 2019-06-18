package cn.test.demo.data;

import cn.test.demo.data.entity.Product;

import java.util.List;

public interface ProductDao {

    int insert(Product record);

    List<Product> selectAll();


}
