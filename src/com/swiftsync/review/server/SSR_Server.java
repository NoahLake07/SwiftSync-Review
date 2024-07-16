package com.swiftsync.review.server;

import com.csf.BasicServer;
import com.csf.ClientHandler;
import com.swiftsync.review.Constants;
import com.swiftsync.review.util.AdvancedOutput;
import com.swiftsync.review.util.ConsoleColors;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.swiftsync.review.util.AdvancedOutput.println;

public class SSR_Server extends BasicServer {

    ArrayList<Session> sessions;
    ArrayList<ClientHandler> myClients = new ArrayList<>();
    private ExecutorService cachedThreadPool = null;
    private ExecutorService clientThreadPool = null;

    public SSR_Server(){
        super();
        sessions = new ArrayList<>();
        clientThreadPool = Executors.newCachedThreadPool();

        AdvancedOutput.setColor(ConsoleColors.RESET);
        println("===========================================\n" +
                "SWIFTSYNC MEDIA REVIEWER - SERVER " + Constants.VERSION, ConsoleColors.CYAN);

        println("\t> Initializing server...");
            cachedThreadPool = Executors.newCachedThreadPool();
            cachedThreadPool.submit(()->start(Constants.PORT));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            InetAddress inetAddress = null;
            String hostname = null;
            String ipAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
                hostname = inetAddress.getHostName();
                ipAddress = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        println("\t> Server running:");
            println("\t\tIP ADDRESS: " + ipAddress,ConsoleColors.YELLOW);
            println("\t\tPORT: " + Constants.PORT,ConsoleColors.YELLOW);
            println("\t\tHOSTNAME: " + hostname,ConsoleColors.YELLOW);
    }

    @Override
    public void handleNewClient(Socket socket){
        SessionClientHandler clientHandler = new SessionClientHandler(socket, this);
        clientThreadPool.submit(()->clientHandler.start());
        myClients.add(clientHandler);
        println("\t> New client connected: " + clientHandler.getSocket().getInetAddress().getHostAddress(), ConsoleColors.GREEN);
    }

    @Override
    public void receivedMessage(String s, ClientHandler ch){
        if(s.startsWith("createSession")){
            println("\t> Session request received", ConsoleColors.BLUE);

            // create new Session
            Session newSession = new Session();;

            // send the client the new session ID
            ch.send("createSession success " + newSession.getID());
            println("New session created: " + newSession.getID(), ConsoleColors.BLUE);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // add the new session to the list of sessions
            sessions.add(newSession);
            newSession.add(ch);
            ch.send("enterSession success " + newSession.getID());
            println("Client entered session: " + newSession.getID(), ConsoleColors.BLUE);
        }

        if(s.startsWith("enterSession")){
            println("\t> Session entry request received", ConsoleColors.BLUE);

            // get the session ID
            String sessionID = s.split(" ")[1];

            // find the session
            Session session = null;
            for(Session sesh : sessions){
                if(sesh.getID().equals(sessionID)){
                    session = sesh;
                    break;
                }
            }

            // if the session exists, add the client to it
            if(session != null){
                session.add(ch);
                ch.send("enterSession success " + sessionID);
                println("Client entered session: " + sessionID, ConsoleColors.BLUE);
            } else {
                ch.send("enterSession fail " + sessionID);
                println("\t! Client failed to enter session: " + sessionID, ConsoleColors.RED);
            }
        }
    }

    public static void main(String[] args) {
        SSR_Server server = new SSR_Server();
    }

}
