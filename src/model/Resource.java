package model;

public class Resource {

    private String materialID;
    private String materialName;
    private double price;
    private int quantity;

    public Resource(String id, String name, double price, int qty) {
        this.materialID = id;
        this.materialName = name;
        this.price = price;
        this.quantity = qty;
    }

    public String getMaterialID() { return materialID; }
    public String getMaterialName() { return materialName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    public void addStock(int qty) {
        quantity += qty;
    }

    public boolean useStock(int qty) {
        if (qty > quantity) return false;
        quantity -= qty;
        return true;
    }

    public double getTotalValue() {
        return price * quantity;
    }
}