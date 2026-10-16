package com.restaurant.model;

import java.math.BigDecimal;

public class MenuItem {

    private int itemId;
    private String itemName;
    private BigDecimal price;
    private String category;
    private String imagePath;

    public MenuItem() {
    }

    public MenuItem(
            int itemId,
            String itemName,
            BigDecimal price,
            String category,
            String imagePath
    ) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return itemName + " - ₹" + price;
    }
}
