package com.sporoc.snapbuddy;

/**
 * Created by rriskhh on 28/10/17.
 */
public class ContactListModel {

    private String name;
    private String phone;
    private boolean selected;

    public  ContactListModel(){
        this.name = "";
        this.phone = "";
    }

    public String getName() {
        return name;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
