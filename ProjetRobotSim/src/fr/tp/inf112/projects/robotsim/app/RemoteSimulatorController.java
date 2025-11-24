package fr.tp.inf112.projects.robotsim.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.notifier.FactorySimulationEventConsumer;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;

import static fr.tp.inf112.projects.robotsim.app.SimulatorApplication.LOGGER;

public class RemoteSimulatorController extends SimulatorController {

    private String factoryId;
    private boolean simulationRunning;

    public RemoteSimulatorController(CanvasPersistenceManager persistenceManager, String factoryId) {
        super(new Factory(200, 200, "Simple Test Puck Factory"), persistenceManager);
        this.factoryId = factoryId;
        this.setCanvas(TestRobotSimSerializationJSON.createFactory());
    }

    public Factory getFactory(String id) throws URISyntaxException, InterruptedException, IOException{
        try (HttpClient client = HttpClient.newHttpClient()){
            final URI uri = new URI("http", null, "localhost", 8080, "/get/" + id, null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return readFactory(response.body());
        }
    }

    @Override
    public void startAnimation() {
        if(this.getCanvas().getId() == null){
            JOptionPane.showMessageDialog(null, "Vous devez d'abord sauvegarder ou charger une usine avant de lancer la simulation");
            return;
        }
        LOGGER.info("starting animation");
        try (HttpClient client = HttpClient.newHttpClient()){
            String canvasId = getCanvas() == null ? "test" : getCanvas().getId();
            final URI uri = new URI("http", null, "localhost", 8080, "/start/" + canvasId.replace(".factory", ""), null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            simulationRunning = true;

            this.updateViewer();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopAnimation() {
        LOGGER.info("stopping animation");
        try (HttpClient client = HttpClient.newHttpClient()){
            final URI uri = new URI("http", null, "localhost", 8080, "/stop/" + getCanvas().getId().replace(".factory", ""), null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            simulationRunning = false;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Une erreur est survenue", e);
        }
    }

    private void updateViewer()
            throws InterruptedException, URISyntaxException, IOException {
        FactorySimulationEventConsumer consumer = new FactorySimulationEventConsumer(this);
        consumer.consumeMessages();
/*        do {
            final Factory remoteFactoryModel = getFactory(getCanvas().getId().replace(".factory", ""));
            setCanvas(remoteFactoryModel);
            Thread.sleep(100);
        } while (simulationRunning);*/
    }

    public Factory readFactory(String json) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapper.readValue(json, Factory.class);
    }

    @Override
    public void setCanvas(final Canvas canvasModel) {
        LOGGER.info("Setting new canvas");

        final List<Observer> observers = getCanvas().getObservers();

        super.setCanvas(canvasModel);
        for (final Observer observer : observers) {
            getCanvas().addObserver(observer);
        }

        getCanvas().notifyObservers();
    }

    @Override
    public boolean isAnimationRunning() {
        return this.simulationRunning;
    }
}
