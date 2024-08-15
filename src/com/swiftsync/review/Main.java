package com.swiftsync.review;

import com.csf.BasicClient;

public class Main {
    public static void main(String[] args) {
        BasicClient client = new BasicClient("http://192.168.137.93", 8081);
        client.startConnection();
        System.out.println("Client started.");
    }
}
