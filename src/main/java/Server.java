import handler.RequestHandler;
import model.Request;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int POOL_SIZE = 64;
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final Map<String, RequestHandler> requestHandlers = new HashMap<>();

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            final ExecutorService threadPool = Executors.newFixedThreadPool(POOL_SIZE);
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.execute(() -> {
                    processingConnection(socket);
                });
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void processingConnection(Socket socket) {
        BufferedReader in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedOutputStream(socket.getOutputStream());
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            final var protocol = parts[2];

            Request request = new Request();
            request.setHttpMethod(method);
            request.setPath(path);
            request.setProtocol(protocol);

            Map<String, String> queryParams = request.getQueryParams();

            final String key = request.getHttpMethod() + request.getPath();

            if (validPaths.contains(path)) {
                DefaultHandler defaultHandler = new DefaultHandler();

                defaultHandler.handle(request, out);
            } else if (requestHandlers.containsKey(key)) {
                requestHandlers.get(key).handle(request, out);
            } else {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

        } catch (Exception ex) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void addHandler(String method, String path, RequestHandler requestHandler) {
        final String key = method + path;
        requestHandlers.put(key, requestHandler);
    }

    static class DefaultHandler implements RequestHandler {

        @Override
        public void handle(Request request, BufferedOutputStream out) {
            try {
                final var path = request.getPath();
                final Path filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }

                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            } catch (Exception exception) {

            }
        }
    }
}
