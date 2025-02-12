package com.example.finance;

public class ItemSizes {
    private int id;
    private String itemSize;

    // Constructor
    public ItemSizes() {}

    public ItemSizes(int id, String itemSize) {
        this.id = id;
        this.itemSize = itemSize;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getItemSize() {
        return itemSize;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }
}
