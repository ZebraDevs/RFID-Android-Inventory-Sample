package com.zebra.rfid.demo.reflexions;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class InventoryVM extends ViewModel {
    private ArrayList<ItemData> items;

    public ArrayList<ItemData> getItems() { return items; }
    public void setItems(ArrayList<ItemData> items) { this.items = items; }

}
