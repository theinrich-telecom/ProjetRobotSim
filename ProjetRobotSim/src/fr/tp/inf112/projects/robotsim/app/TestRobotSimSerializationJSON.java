package fr.tp.inf112.projects.robotsim.app;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.model.*;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.persistence.RemoteFactoryPersistenceManager;
import fr.tp.inf112.projects.robotsim.persistence.RemoteFileCanvasChooser;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

public class TestRobotSimSerializationJSON {

    public static final Logger LOGGER = SimulatorApplication.LOGGER;

    public static void main(String[] args) {
        try {
            TestRobotSimSerializationJSON test = new TestRobotSimSerializationJSON();
            test.testSerialization();
            test.runSerialized();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private final ObjectMapper objectMapper;

    public TestRobotSimSerializationJSON(){
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .build();
        objectMapper.activateDefaultTyping(typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL);
    }

    public static Factory createFactory(){
        LOGGER.info("Starting the robot simulator...");

        final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");
        final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
        new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
        final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
        final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

        final Room room2 = new Room(factory, new RectangularShape( 120, 22, 75, 75 ), "Production Room 2");
        new Door(room2, Room.WALL.LEFT, 10, 20, true, "Entrance");
        final Area area2 = new Area(room2, new RectangularShape( 135, 35, 50, 50 ), "Production Area 1");
        final Machine machine2 = new Machine(area2, new RectangularShape( 150, 50, 15, 15 ), "Machine 1");

        final int baselineSize = 3;
        final int xCoordinate = 10;
        final int yCoordinate = 165;
        final int width =  10;
        final int height = 30;
        final BasicPolygonShape conveyorShape = new BasicPolygonShape();
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate, yCoordinate));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate + width, yCoordinate));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate + width, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate - baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate - baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new fr.tp.inf112.projects.canvas.model.impl.BasicVertex(xCoordinate, yCoordinate + height - baselineSize));

        final Room chargingRoom = new Room(factory, new RectangularShape(125, 125, 50, 50), "Charging Room");
        new Door(chargingRoom, Room.WALL.RIGHT, 10, 20, false, "Entrance");
        final ChargingStation chargingStation = new ChargingStation(factory, new RectangularShape(150, 145, 15, 15), "Charging Station");

        final FactoryPathFinder jgraphPahtFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);
        final Robot robot1 = new Robot(factory, jgraphPahtFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
        robot1.addTargetComponent(machine1);
        robot1.addTargetComponent(machine2);
        robot1.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
        robot1.addTargetComponent(chargingStation);

        final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
        final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
        robot2.addTargetComponent(machine1);
        robot2.addTargetComponent(machine2);
        robot2.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
        robot2.addTargetComponent(chargingStation);

        return factory;
    }

    public Factory testSerialization() throws JsonProcessingException {
        final String factoryAsJsonString = objectMapper.writeValueAsString(createFactory());
        LOGGER.info(factoryAsJsonString);
        final Factory roundTrip = objectMapper.readValue(factoryAsJsonString,
                Factory.class);
        LOGGER.info(roundTrip.toString());

        for(Figure component : roundTrip.getFigures()){
            if(component instanceof Robot robot && robot.getPositionedShape() instanceof CircularShape shape){
                System.out.println(shape.getWidth());
                System.out.println(shape.getHeight());
            }
        }
        return roundTrip;
    }

    public void runSerialized() throws JsonProcessingException {
        Factory factory = this.testSerialization();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final RemoteFileCanvasChooser canvasChooser = new RemoteFileCanvasChooser("factory", "Puck Factory");
                final java.awt.Component factoryViewer = new CanvasViewer(new SimulatorController(factory, new RemoteFactoryPersistenceManager(canvasChooser, "localhost", 50050)));
                canvasChooser.setViewer(factoryViewer);



                //new CanvasViewer(factory);
            }
        });
    }

}
