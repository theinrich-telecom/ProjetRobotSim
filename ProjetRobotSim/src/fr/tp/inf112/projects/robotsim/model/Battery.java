package fr.tp.inf112.projects.robotsim.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

public class Battery implements Serializable {
	
	private static final long serialVersionUID = 5744149485828674046L;

	@JsonInclude
	private final float capacity;

	@JsonInclude
	private float level;

	public Battery() {
		this.capacity = 0;
	}

	public Battery(final float capacity) {
		this.capacity = capacity;
		level = capacity;
	}
	
	public float consume(float energy) {
		level-= energy;
		
		return level;
	}
	
	public float charge(float energy) {
		level+= energy;
		
		return level;
	}

	@Override
	public String toString() {
		return "Battery [capacity=" + capacity + "]";
	}
}
