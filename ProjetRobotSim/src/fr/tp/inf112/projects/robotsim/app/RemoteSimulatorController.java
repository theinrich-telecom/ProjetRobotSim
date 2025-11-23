package fr.tp.inf112.projects.robotsim.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.notifier.FactorySimulationEventConsumer;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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
            // TODO ouvrir un menu pour demander de d'abord sauvegarder cette factory
            return;
        }
        System.out.println("starting animation");
        try (HttpClient client = HttpClient.newHttpClient()){
            String canvasId = getCanvas() == null ? "test" : getCanvas().getId();
            final URI uri = new URI("http", null, "localhost", 8080, "/start/" + canvasId.replace(".factory", ""), null, null);
            System.out.println("Sending to " + uri.toString());
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
        System.out.println("stopping animation");
        try (HttpClient client = HttpClient.newHttpClient()){
            final URI uri = new URI("http", null, "localhost", 8080, "/stop/" + getCanvas().getId().replace(".factory", ""), null, null);
            HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            simulationRunning = false;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
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
        System.out.println("Setting new canvas");

        final List<Observer> observers = getCanvas().getObservers();

        System.out.println(observers.size());

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
