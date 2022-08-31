package com.example.front;

public class ReportRequest {

    public String content;

    public String password;

    public String getEmail() {
        return content;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String content) {
        this.content = content;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ReportRequest(String content, String password) {
        this.content = content;
        this.password = password;
    }
}
