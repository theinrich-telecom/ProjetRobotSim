package fr.tp.inf112.projects.robotsim.persistence;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class WebServer {

    public static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    public static void main(String args[]) {
        LOGGER.info("Starting server...");
        try (
                ServerSocket serverSocket = new ServerSocket(50050);
        ) {
            do {
                try {
                    Socket socket = serverSocket.accept();
                    Runnable reqProcessor = new RequestProcessor(socket);
                    new Thread(reqProcessor).start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } while (true);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}