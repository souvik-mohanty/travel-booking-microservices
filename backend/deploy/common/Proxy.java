import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

// Tiny TCP forwarder started before the Spring Boot app in each Render image.
// Render's port scan gives up after a short window, and on the free plan's
// 0.1 vCPU a Spring Boot service needs longer than that to bind its port. This
// binds Render's $PORT within seconds, then holds each incoming connection
// until the app is listening on the internal port and pipes bytes through --
// so a request that wakes a sleeping service waits for the app instead of
// failing. Pure TCP: no HTTP parsing.
public class Proxy {

    private static final long WAIT_FOR_APP_NANOS = 5L * 60 * 1_000_000_000L;

    public static void main(String[] args) throws IOException {
        int listenPort = Integer.parseInt(args[0]);
        int appPort = Integer.parseInt(args[1]);

        try (ServerSocket server = new ServerSocket(listenPort, 128)) {
            while (true) {
                Socket client = server.accept();
                Thread.startVirtualThread(() -> handle(client, appPort));
            }
        }
    }

    private static void handle(Socket client, int appPort) {
        Socket app = null;
        try {
            app = connectWhenReady(appPort);
            if (app == null) {
                client.close();
                return;
            }
            Socket upstream = app;
            Thread.startVirtualThread(() -> pipe(client, upstream));
            pipe(upstream, client);
        } catch (Exception e) {
            closeQuietly(client);
            closeQuietly(app);
        }
    }

    private static Socket connectWhenReady(int port) throws InterruptedException {
        long deadline = System.nanoTime() + WAIT_FOR_APP_NANOS;
        while (System.nanoTime() < deadline) {
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress("127.0.0.1", port), 1000);
                return s;
            } catch (IOException e) {
                closeQuietly(s);
                Thread.sleep(250);
            }
        }
        return null;
    }

    // Copies until either side closes, then closes both.
    private static void pipe(Socket from, Socket to) {
        byte[] buffer = new byte[8192];
        try {
            InputStream in = from.getInputStream();
            OutputStream out = to.getOutputStream();
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                out.flush();
            }
        } catch (IOException ignored) {
            // peer went away
        } finally {
            closeQuietly(from);
            closeQuietly(to);
        }
    }

    private static void closeQuietly(Socket s) {
        if (s == null) return;
        try {
            s.close();
        } catch (IOException ignored) {
            // nothing to do
        }
    }
}
