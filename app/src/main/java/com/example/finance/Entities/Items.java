package com.example.finance;

public class Items {
    private int id;
    private String itemName;

    // Constructor
    public Items() {}

    public Items(int id, String itemName) {
        this.id = id;
        this.itemName = itemName;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
