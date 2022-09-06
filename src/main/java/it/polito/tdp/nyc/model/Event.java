package it.polito.tdp.nyc.model;

public class Event implements Comparable<Event>{

	public enum EventType {
		INIZIO_REVISIONE,
		FINE_REVISIONE
	}
	
	private int tempo; // il tempo rappresenta il numero di minuti che passano dall'inizio della simulazione
	private EventType tipo;
	private Tecnico tecnico;
	private Hotspot hotspot;
	
	public Event(int tempo, EventType tipo, Tecnico tecnico, Hotspot hotspot) {
		super();
		this.tempo = tempo;
		this.tipo = tipo;
		this.tecnico = tecnico;
		this.hotspot = hotspot;
	}

	public int getTempo() {
		return tempo;
	}

	public EventType getTipo() {
		return tipo;
	}

	public Tecnico getTecnico() {
		return tecnico;
	}

	public Hotspot getHotspot() {
		return hotspot;
	}

	@Override
	public int compareTo(Event other) {
		return this.getTempo() - other.getTempo();
	}
	
}
