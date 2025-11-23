package fr.tp.inf112.projects.robotsim.model.notifier;

import fr.tp.inf112.projects.canvas.controller.Observer;

import java.util.List;

public interface FactoryModelChangedNotifier {

    void notifyObservers();
    boolean addObserver(Observer observer);
    boolean removeObserver(Observer observer);
    List<Observer> getObservers();

}
