package com.example.sportcenterapp.models;

public class CartItem {
    private int id;
    private int userId;
    private int productId;
    private String productName;
    private int quantity;
    private Product product;


    public CartItem(int id, int userId, int productId, String productName, int quantity) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}

