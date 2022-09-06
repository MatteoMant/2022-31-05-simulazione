package it.polito.tdp.nyc.model;

public class Tecnico {

	private int idTecnico;
	private boolean libero;
	private int numHotspotRevisionati;
	
	public Tecnico(int idTecnico) {
		super();
		this.idTecnico = idTecnico;
		this.libero = true;
		this.numHotspotRevisionati = 0;
	}

	public int getIdTecnico() {
		return idTecnico;
	}

	public int getNumHotspotRevisionati() {
		return numHotspotRevisionati;
	}
	
	public void incrementaNumHotspotRevisionati() {
		this.numHotspotRevisionati++;
	}

	public boolean isLibero() {
		return libero;
	}

	public void setLibero(boolean libero) {
		this.libero = libero;
	}
	
}
