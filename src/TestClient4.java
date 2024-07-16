import com.swiftsync.review.client.SessionClient;
import com.swiftsync.review.exception.InvalidSessionID;
import com.swiftsync.review.util.ConsoleColors;

import static com.swiftsync.review.util.AdvancedOutput.println;

public class TestClient4 {

    public static void main(String[] args) {
        SessionClient client4 = new SessionClient();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            client4.enterSession("NZFJER");
        } catch (InvalidSessionID e) {
            println(e.getMessage(), ConsoleColors.RED);
        }

        client4.send("<> msg test message from client 4 for session 2");

    }

}
