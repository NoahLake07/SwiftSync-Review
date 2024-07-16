package com.swiftsync.review.server;

import com.csf.BasicServer;
import com.csf.ClientHandler;

import java.net.Socket;

import static com.swiftsync.review.client.SessionClient.removeTag;
import static com.swiftsync.review.util.AdvancedOutput.println;

public class SessionClientHandler extends ClientHandler {
    private Session session;

    public SessionClientHandler(Socket socket, BasicServer server) {
        super(socket, server);
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void receivedMessage(String message) {
        /*
        SESSION MESSAGE TAG DEFINITION
         If a message starts with the tag "<>", then the message should be
         sent to the rest of the session, but not received to the server.

         Otherwise, it will be assumed that the incoming message is intended
         for the server, and the message will be sent to the server.
         */

        if(message.startsWith("<>")){
            for(ClientHandler ch : session.getHandlers()){
                SessionClientHandler sch = (SessionClientHandler) ch;
                sch.send(removeTag(message));
            }
        } else {
            super.receivedMessage(message);
        }
    }
}
