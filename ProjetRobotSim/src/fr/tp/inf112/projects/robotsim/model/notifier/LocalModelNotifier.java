package fr.tp.inf112.projects.robotsim.model.notifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.tp.inf112.projects.canvas.controller.Observer;

import java.util.ArrayList;
import java.util.List;

public class LocalModelNotifier implements FactoryModelChangedNotifier{

    @JsonIgnore
    private transient List<Observer> observers;

    public LocalModelNotifier() {
        this(new ArrayList<>());
    }

    public LocalModelNotifier(List<Observer> observers) {
        this.observers = observers;
    }

    @Override
    public void notifyObservers() {
        for(final Observer observer : this.observers){
            observer.modelChanged();
        }
    }

    @Override
    public boolean addObserver(Observer observer) {
        return observers.add(observer);
    }

    @Override
    public boolean removeObserver(Observer observer) {
        return observers.remove(observer);
    }

    @Override
    public List<Observer> getObservers() {
        if (observers == null) {
            observers = new ArrayList<>();
        }

        return observers;
    }
}
