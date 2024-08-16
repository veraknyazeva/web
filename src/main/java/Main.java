import handler.RequestHandler;
import model.Request;

import java.io.BufferedOutputStream;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        // код инициализации сервера (из вашего предыдущего ДЗ)

        //добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", new RequestHandler() {
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    final String response = "Hello word!";
                    byte[] bytes = response.getBytes();
                    final int length = bytes.length;
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + "text/plain" + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(bytes);
                    out.flush();
                } catch (Exception ex) {

                }
            }
        });
        server.addHandler("GET", "/what/time", new RequestHandler() {
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    final LocalDateTime response = LocalDateTime.now();
                    byte[] bytes = response.toString().getBytes();
                    final int length = bytes.length;
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + "text/plain" + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(bytes);
                    out.flush();
                } catch (Exception ex) {
                }
            }
        });

        server.start(9999);
    }
}
