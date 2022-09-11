package it.polito.tdp.nyc.model;

public class Tecnico {

	private int idTecnico;
	private boolean libero;
	
	public Tecnico(int idTecnico) {
		super();
		this.idTecnico = idTecnico;
		this.libero = true;
	}

	public int getIdTecnico() {
		return idTecnico;
	}

	public boolean isLibero() {
		return libero;
	}

	public void setLibero(boolean libero) {
		this.libero = libero;
	}
	
}
