package fr.tp.inf112.projects.robotsim.persistence;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;

public class RequestProcessor implements Runnable {

    private final Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                final InputStream input = this.socket.getInputStream();
                final ObjectInputStream objectInputStream = new ObjectInputStream(input);
                final OutputStream output = this.socket.getOutputStream();
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(output);){
            final FactoryPersistenceManager manager = new FactoryPersistenceManager(null);
            final String socketId = this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();

            WebServer.LOGGER.info("New connection from " + socketId);

            Object obj = objectInputStream.readObject();
            System.out.println(obj);

            if(obj instanceof String str){
                if(str.equals("get_existing_canvas")){
                    WebServer.LOGGER.info(socketId + " tried to retrieve existing factories");
                    File currentDir = new File(System.getProperty("user.dir"));
                    StringBuilder files = new StringBuilder();
                    for(File file : Objects.requireNonNull(currentDir.listFiles())){
                        if(file.isFile() && file.getName().endsWith(".factory")){
                            files.append(file.getName().replace(".factory", "")).append(File.pathSeparator);
                        }
                    }
                    objectOutputStream.writeObject(files.toString());
                } else {
                    WebServer.LOGGER.info(socketId + " tried to retrieve " + str + " factory");
                    File currentDir = new File(System.getProperty("user.dir"));
                    Canvas result = manager.read(currentDir.getPath() + File.separator + str + ".factory");
                    objectOutputStream.writeObject(result);
                }
            } else if(obj instanceof Factory factory){
                WebServer.LOGGER.info(socketId + " tried to save " + factory.getId());
                manager.persist(factory);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                WebServer.LOGGER.log(Level.SEVERE, "An error occurred", e);
            }
        }
    }
}
