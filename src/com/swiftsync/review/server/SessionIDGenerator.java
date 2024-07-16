package com.swiftsync.review.server;

import java.util.Random;

public class SessionIDGenerator {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int ID_LENGTH = 6;
    private static final Random RANDOM = new Random();

    public static String generateSessionId() {
        StringBuilder sessionId = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            int index = RANDOM.nextInt(LETTERS.length());
            sessionId.append(LETTERS.charAt(index));
        }
        return sessionId.toString();
    }

}
