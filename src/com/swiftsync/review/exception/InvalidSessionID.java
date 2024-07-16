package com.swiftsync.review.exception;

public class InvalidSessionID extends Exception {

    String triedSession = "--";

    public InvalidSessionID(String triedSession){
        this.triedSession = triedSession;
    }

    public String getMessage(){
        return "Session ID " + triedSession + " is invalid!";
    }

}
