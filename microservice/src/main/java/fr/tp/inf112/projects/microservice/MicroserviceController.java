package fr.tp.inf112.projects.microservice;

import fr.tp.inf112.projects.microservice.kafka.KafkaFactoryModelChangeNotifier;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.notifier.FactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.persistence.RemoteFactoryPersistenceManager;
import fr.tp.inf112.projects.robotsim.persistence.RemoteFileCanvasChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
public class MicroserviceController {

    public static final Logger LOGGER = SimulatorApplication.LOGGER;

    public static final String IP = "localhost";
    public static final int PORT = 50050;

    private final Map<String, Factory> runningFactories = new HashMap<>();

    @Autowired
    private KafkaTemplate<String, Factory> simulationEventTemplate;

    @GetMapping("/start/{factoryId}")
    public boolean runFactory(@PathVariable String factoryId) {
        LOGGER.info(() -> "Requête reçue pour démarrer la factory avec ID: " + factoryId);

        final RemoteFileCanvasChooser canvasChooser = new RemoteFileCanvasChooser("factory", "Puck Factory");
        RemoteFactoryPersistenceManager persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, IP, PORT);

        try {
            if (persistenceManager.read(factoryId) instanceof Factory factory && !this.runningFactories.containsKey(factoryId)) {
                LOGGER.info(() -> "Factory trouvée pour ID: " + factoryId + ". Démarrage de la simulation...");
                final FactoryModelChangedNotifier notifier = new KafkaFactoryModelChangeNotifier(factory, simulationEventTemplate);
                factory.setNotifier(notifier);
                factory.startSimulation();
                this.runningFactories.put(factoryId, factory);
                LOGGER.info(() -> "Factory " + factoryId + " démarrée avec succès.");
                return true;
            } else {
                LOGGER.warning(() -> "Aucune factory trouvée pour ID: " + factoryId + " ou déjà en cours d'exécution.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage de la factory " + factoryId + " : " + e.getMessage(), e);
        }

        return false;
    }

    @GetMapping("/stop/{factoryId}")
    public void stopFactory(@PathVariable String factoryId) {
        LOGGER.info(() -> "Requête reçue pour arrêter la factory avec ID: " + factoryId);

        if (this.runningFactories.get(factoryId) instanceof Factory factory) {
            try {
                factory.stopSimulation();
                this.runningFactories.remove(factoryId);
                LOGGER.info(() -> "Factory " + factoryId + " arrêtée avec succès.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'arrêt de la factory " + factoryId + " : " + e.getMessage(), e);
            }
        } else {
            LOGGER.warning(() -> "Aucune factory en cours d'exécution trouvée pour ID: " + factoryId);
        }
    }

    @GetMapping("/get/{factoryId}")
    public Factory getFactory(@PathVariable String factoryId) {
        LOGGER.info(() -> "Requête reçue pour récupérer la factory avec ID: " + factoryId);

        Factory factory = this.runningFactories.get(factoryId);
        if (factory != null) {
            LOGGER.info(() -> "Factory " + factoryId + " trouvée et renvoyée.");
        } else {
            LOGGER.warning(() -> "Factory " + factoryId + " introuvable.");
        }
        return factory;
    }
}
