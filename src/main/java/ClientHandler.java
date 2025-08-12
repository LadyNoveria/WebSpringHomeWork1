import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
//                returnNotFound(out);
                try {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: invalid request\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            final var path = parts[1];
            if (!Server.validPaths.contains(path)) {
                //returnNotFound(out);
                try {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: not found path\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            Request request = Request.builder()
                    .httpMethod(parts[0])
                    .path(parts[1])
                    .out(out)
                    .body(socket.getInputStream())
                    .build();

            System.out.println("HTTP Method: " + request.getHttpMethod());
            System.out.println("All handlers: " + Server.handlers);

            Map<String, Handler> map = Server.handlers.get(request.getHttpMethod());
            if (map == null || map.get(request.getPath()) == null) {
                //returnNotFound(out);
                System.out.println("No handlers for method: " + request.getHttpMethod());
                try {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: no file\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            try {
                Handler handler = map.get(request.getPath());
                handler.handle(request, out);
            } catch (IOException e) {
                System.err.println("Client processing error: " + e.getMessage());
            }


        } catch (IOException e) {
            System.err.println("Client processing error: " + e.getMessage());
        }
    }

    private void returnNotFound(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: closeRR\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
