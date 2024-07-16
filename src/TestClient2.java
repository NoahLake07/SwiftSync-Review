import com.swiftsync.review.client.SessionClient;
import com.swiftsync.review.exception.InvalidSessionID;
import com.swiftsync.review.util.AdvancedOutput;
import com.swiftsync.review.util.ConsoleColors;

import static com.swiftsync.review.util.AdvancedOutput.println;

public class TestClient2 {

    static String sessionId = "EGXMSZ";


    public static void main(String[] args) {
        SessionClient client2 = new SessionClient();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            client2.enterSession(sessionId);
        } catch (InvalidSessionID e) {
            println(e.getMessage(), ConsoleColors.RED);
        }

        client2.send("<> msg test message!!!");

    }

}
