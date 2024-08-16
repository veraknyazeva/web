package handler;

import model.Request;

import java.io.BufferedOutputStream;

@FunctionalInterface
public interface RequestHandler {

    void handle(Request request, BufferedOutputStream responseStream);
}
