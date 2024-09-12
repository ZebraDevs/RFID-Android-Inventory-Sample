package com.zebra.rfid.demo.reflexions;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class ItemData {
    private String epc;
    private String size;
    private String itemname;
    private String image;

    public int getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(int expectedCount) {
        this.expectedCount = expectedCount;
    }

    private int expectedCount;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof ItemData) {
            return ((ItemData) obj).getEPC().equals(epc);
        }
        return false;
    }

    public int getSeencount() {
        return seencount;
    }

    public void setSeencount(int seencount) {
        this.seencount = seencount;
    }

    private int seencount;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    private String sku;
    public String getEPC() {
        return epc;
    }

    public void setEPC(String epc) {
        this.epc = epc;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public ItemData(String epc, String size, String itemname, String image,String sku) {
        this.epc = epc;
        this.size = size;
        this.itemname = itemname;
        this.image = image;
        this.sku = sku;
        this.seencount= 1;
    }

    public ItemData(String epc, String size, String itemname, String image,String sku, int seencount, int expectedCount) {
        this.epc = epc;
        this.size = size;
        this.itemname = itemname;
        this.image = image;
        this.sku=sku;
        this.seencount= seencount;
        this.expectedCount= expectedCount;
    }

    public void increaseCount(){
        seencount++;
    }
}
