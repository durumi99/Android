package com.example.front;

public class ReportRequest {

    public String title;
    public String content;
    public String address;
    public String image;

    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public String getAddresss() {
        return address;
    }
    public String getImage() {
        return image;
    }


    public void setTitle(String title) {
        this.title = title;

    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setImage(String image) {
        this.image = image;
    }


    public ReportRequest(String title, String content,String address,String image) {
        this.title = title;
        this.content = content;
        this.address = address;
        this.image = image;
    }
}
