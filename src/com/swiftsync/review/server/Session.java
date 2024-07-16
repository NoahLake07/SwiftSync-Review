package com.swiftsync.review.server;

import com.csf.ClientHandler;
import com.swiftsync.review.util.ConsoleColors;

import java.util.ArrayList;

import static com.swiftsync.review.util.AdvancedOutput.print;
import static com.swiftsync.review.util.AdvancedOutput.println;

public class Session {

    private String id;

    private ArrayList<ClientHandler> clients = new ArrayList<>();

    public Session(){
        this.id = SessionIDGenerator.generateSessionId();
    }

    public void broadcast(String msg){
        for(ClientHandler ch : clients){
            ch.send(msg);
        }
        println(clients.toString(), ConsoleColors.PURPLE);
    }

    public boolean contains(ClientHandler ch){
        return clients.contains(ch);
    }

    public String getID(){
        return this.id;
    }

    public void add(ClientHandler ch){
        this.clients.add(ch);
        ((SessionClientHandler) ch).setSession(this);  // let the ClientHandler know which Session it's in
    }

    protected ArrayList<ClientHandler> getHandlers(){
        return this.clients;
    }

}
