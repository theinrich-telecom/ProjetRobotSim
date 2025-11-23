package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import fr.tp.inf112.projects.canvas.controller.Observable;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.Figure;
import fr.tp.inf112.projects.canvas.model.Style;
import fr.tp.inf112.projects.robotsim.model.motion.Motion;
import fr.tp.inf112.projects.robotsim.model.notifier.FactoryModelChangedNotifier;
import fr.tp.inf112.projects.robotsim.model.notifier.LocalModelNotifier;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class Factory extends Component implements Canvas, Observable {

	private static final long serialVersionUID = 5156526483612458192L;
	
	private static final ComponentStyle DEFAULT = new ComponentStyle(5.0f);

	@JsonManagedReference("factory-components")
    private final List<Component> components;

	@JsonIgnore
	private transient boolean simulationStarted;

    @JsonIgnore
    private transient FactoryModelChangedNotifier notifier = new LocalModelNotifier();

	public Factory(){
        super(null, null, null);
        this.components = new ArrayList<>();
	}

	public Factory(final int width,
				   final int height,
				   final String name ) {
		super(null, new RectangularShape(0, 0, width, height), name);
		
		components = new ArrayList<>();
		simulationStarted = false;
	}

    @JsonIgnore
	public List<Observer> getObservers() {
		if (this.notifier == null) {
			this.notifier = new LocalModelNotifier();
		}
		
		return this.notifier.getObservers();
	}

	@Override
	public boolean addObserver(Observer observer) {
        if(notifier == null){
            notifier = new LocalModelNotifier();
        }
        return notifier.addObserver(observer);
	}

	@Override
	public boolean removeObserver(Observer observer) {
        if(notifier == null){
            notifier = new LocalModelNotifier();
        }
        return notifier.removeObserver(observer);
	}
	
	public void notifyObservers() {
        if(notifier != null){
            notifier.notifyObservers();
        }
	}

    public void setNotifier(FactoryModelChangedNotifier notifier) {
        this.notifier = notifier;
    }

    public boolean addComponent(final Component component) {
		if (components.add(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	public boolean removeComponent(final Component component) {
		if (components.remove(component)) {
			notifyObservers();
			
			return true;
		}
		
		return false;
	}

	protected List<Component> getComponents() {
		return components;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@JsonIgnore
	public Collection<Figure> getFigures() {
		return (Collection) components;
	}

	@Override
	public String toString() {
		return super.toString() + " components=" + components + "]";
	}
	
	public boolean isSimulationStarted() {
		return simulationStarted;
	}

	public void startSimulation() {
		if (!isSimulationStarted()) {
			this.simulationStarted = true;
			notifyObservers();

			this.behave();
		}
	}

	public void stopSimulation() {
		if (isSimulationStarted()) {
			this.simulationStarted = false;
			
			notifyObservers();
		}
	}

	@Override
	public boolean behave() {
		for (final Component component : getComponents()) {
			Thread thread = new Thread(component);
			thread.start();
		}
		
		return true;
	}
	
	@Override
	@JsonIgnore
	public Style getStyle() {
		return DEFAULT;
	}
	
	public boolean hasObstacleAt(final PositionedShape shape) {
		for (final Component component : getComponents()) {
			if (component.overlays(shape) && !component.canBeOverlayed(shape)) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasMobileComponentAt(final PositionedShape shape,
										final Component movingComponent) {
		for (final Component component : getComponents()) {
			if (component != movingComponent && component.isMobile() && component.overlays(shape)) {
				return true;
			}
		}
		
		return false;
	}

	@JsonIgnore
	public Component getMobileComponentAt(	final Position position,
											final Component ignoredComponent) {
		if (position == null) {
			return null;
		}
		
		return getMobileComponentAt(new RectangularShape(position.getxCoordinate(), position.getyCoordinate(), 2, 2), ignoredComponent);
	}

	@JsonIgnore
	public Component getMobileComponentAt(	final PositionedShape shape,
											final Component ignoredComponent) {
		if (shape == null) {
			return null;
		}
		
		for (final Component component : getComponents()) {
			if (component != ignoredComponent && component.isMobile() && component.overlays(shape)) {
				return component;
			}
		}
		
		return null;
	}

	public synchronized int moveComponent(final Motion motion, final Component componentToMove){
		final Position targetPosition = motion.getTargetPosition();
		final PositionedShape shape = new RectangularShape(targetPosition.getxCoordinate(), targetPosition.getyCoordinate(), 2, 2);
		if(!this.hasMobileComponentAt(shape, componentToMove)){
			return motion.moveToTarget();
		}
		return 0;
	}
}
