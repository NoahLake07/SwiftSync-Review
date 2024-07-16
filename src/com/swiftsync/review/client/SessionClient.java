package com.swiftsync.review.client;

import com.csf.BasicClient;
import com.swiftsync.review.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.swiftsync.review.exception.InvalidSessionID;
import com.swiftsync.review.util.AdvancedOutput;
import com.swiftsync.review.util.ConsoleColors;

import static com.swiftsync.review.util.AdvancedOutput.print;
import static com.swiftsync.review.util.AdvancedOutput.println;

public class SessionClient extends BasicClient {

    String currentSession = "";
    boolean isWaiting = false;
    String awaitingTag = "*", requestedString = "";
    private ExecutorService cachedThreadPool = null;

    public SessionClient() {
        super(Constants.SERVER_ADDRESS, Constants.PORT);
        AdvancedOutput.setColor(ConsoleColors.RESET);

        cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.submit(this::startConnection);
    }

    public boolean enterSession(String sessionID) throws InvalidSessionID {
        println("Requesting to enter session " + sessionID + "...", ConsoleColors.YELLOW);
        send("enterSession " + sessionID);

        // wait for a response tagged with "enterSession"
        awaitingTag = "enterSession";
        isWaiting = true;
        while(isWaiting);
        String response = removeTag(requestedString);

         // if the response is "enterSession success", set currentSession and return true
            if(response.contains("success")){
                currentSession = sessionID;
                println("\t> Entered session " + sessionID, ConsoleColors.BLUE);
                return true;
            }

         // if the response is "enterSession fail", return false
            if(response.contains("fail")){
                println("\t! Failed to enter session " + sessionID, ConsoleColors.RED);
                return false;
            }

         // else throw an InvalidSessionID exception
            InvalidSessionID exception = new InvalidSessionID(sessionID);
            println(exception.getMessage(), ConsoleColors.RED);
            throw exception;
    }

    public String requestNewSession(){
        println("Requesting new session...", ConsoleColors.YELLOW);
        send("createSession");

        // wait for a response tagged with "newSession"
        awaitingTag = "createSession";
        isWaiting = true;
        while(isWaiting){
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }

        // parse the response
        String response = removeTag(requestedString);
        if(response.contains("success")){
            String id = response.split(" ")[1];
            println("Created new session "+id,ConsoleColors.BLUE);
            return id;
        } else {
            return "failed to create new session";
        }
    }

    @Override
    public void receivedMessage(String s){
        if(isWaiting && s.startsWith(awaitingTag)){
            requestedString = s;
            isWaiting = false;
            awaitingTag = "*";
        } else {
            parse(s);
        }
    }

    @Override
    public void send(String s){super.send(s);}

    private void parse(String s){
        if(s.startsWith("enterSession success")){
            println("Client entered session: " + s.split(" ")[2], ConsoleColors.BLUE);
        }

        if(s.startsWith("enterSession fail")){
            println("\t! Client failed to enter session: " + s.split(" ")[2], ConsoleColors.RED);
        }

        if(s.startsWith("msg")){
            println("[msg]  "+removeTag(s), ConsoleColors.PURPLE);
        }
    }

    public static String removeTag(String s){
        int marker = 0;
        for (int i = 0; i < s.length()-1; i++) {
            if(s.charAt(i) == ' '){
                marker = i;
                break;
            }
        }
        return s.substring(marker+1);
    }

    public SessionClient(String serverAddress, int serverPort) {
        super(serverAddress, serverPort);
    }

}
