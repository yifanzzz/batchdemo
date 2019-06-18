package cn.test.demo.data.entity;

import cn.test.demo.data.BaseEntity;

/**
 * 产品类
 */
public class Product extends BaseEntity {
	private int id;// id
	private String name;// 产品名
	private String description;// 产品描述
	private int quantity;// 产品数量

	public Product() {
	}

	public Product(int id, String name, String description, int quantity) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.quantity = quantity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	@Override
	public String toString() {
		return "Product [id=" + id + ", name=" + name + ", description=" + description + ", quantity=" + quantity + "]";
	}
}
