package fr.tp.inf112.projects.robotsim.persistence;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {

    private static final int SOCKET_TIMEOUT = 1000;

    private final InetAddress address;
    private final int port;

    public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser, final String address, int port) {
        super(canvasChooser);
        if(canvasChooser instanceof RemoteFileCanvasChooser remoteFileCanvasChooser){
            remoteFileCanvasChooser.setRemoteManager(this);
        }
        try {
            this.address = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        this.port = port;
    }

    @Override
    public Canvas read(String canvasId){
        try(final Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            try(
                    final OutputStream outputStream = socket.getOutputStream();
                    final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    final InputStream inputStream = socket.getInputStream();
                    final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
            ) {
                // Sending the canvas id
                objectOutputStream.writeObject(canvasId);

                // Retrieving the canvas
                if (objectInputStream.readObject() instanceof Canvas canvas) {
                    System.out.println(((Factory) canvas).getFigures().size());
                    return canvas;
                }
            } catch (IOException | ClassNotFoundException e) {
                SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to retrieve " + canvasId + " factory", e);
            }
        } catch (IOException e){
            SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to retrieve " + canvasId + " factory", e);
        }
        return null;
    }

    @Override
    public void persist(Canvas canvas){
        try(final Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            try(
                    final OutputStream outputStream = socket.getOutputStream();
                    final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
            ) {
                SimulatorApplication.LOGGER.info("Saving " + canvas.getId());
                objectOutputStream.writeObject(canvas);
                Thread.sleep(1);
            } catch (IOException e){
                SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to save " + canvas.getId() + " factory", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e){
            SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to save " + canvas.getId() + " factory", e);
        }
    }

    @Override
    public boolean delete(Canvas canvasModel) throws IOException {
        return false;
    }

    public List<String> getExistingSavedCanvas(){
        try(final Socket socket = new Socket()){
            socket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            try(
                    final OutputStream outputStream = socket.getOutputStream();
                    final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    final InputStream inputStream = socket.getInputStream();
                    final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
            ){
                // Sending the canvas id
                objectOutputStream.writeObject("get_existing_canvas");

                // Retrieving the canvas
                if (objectInputStream.readObject() instanceof String canvasNames) {
                    SimulatorApplication.LOGGER.info(canvasNames);
                    return List.of(canvasNames.split(File.pathSeparator));
                }
            } catch (IOException | ClassNotFoundException e){
                SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to retrieve existing canvas", e);
            }
        } catch (IOException e){
            SimulatorApplication.LOGGER.log(Level.SEVERE, "An error occurred while trying to retrieve existing canvas", e);
        }
        return null;
    }
}