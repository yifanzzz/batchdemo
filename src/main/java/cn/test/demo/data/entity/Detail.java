package cn.test.demo.data.entity;

import cn.test.demo.data.BaseEntity;

public class Detail extends BaseEntity {

    private int id;

    private String productName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
