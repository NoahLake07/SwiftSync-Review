import com.swiftsync.review.client.SessionClient;
import com.swiftsync.review.util.AdvancedOutput;
import com.swiftsync.review.util.ConsoleColors;

public class TestClient {

    public static void main(String[] args) {
        SessionClient client1 = new SessionClient();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        client1.requestNewSession();
    }

}
