import com.swiftsync.review.client.SessionClient;
import com.swiftsync.review.exception.InvalidSessionID;
import com.swiftsync.review.util.AdvancedOutput;
import com.swiftsync.review.util.ConsoleColors;

import static com.swiftsync.review.util.AdvancedOutput.println;

public class TestClient3 {

    public static void main(String[] args) {
        SessionClient client3 = new SessionClient();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        client3.requestNewSession();

        client3.send("<> msg test message from client 3 for session two");

    }

}
