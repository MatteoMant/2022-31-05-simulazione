package it.polito.tdp.nyc.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.nyc.db.NYCDao;

public class Model {

	private NYCDao dao;
	private Graph<String, DefaultWeightedEdge> grafo;
	private int durataTotSimulazione;

	public Model() {
		dao = new NYCDao();
	}

	public void creaGrafo(String provider) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// Aggiunta dei vertici
		Graphs.addAllVertices(this.grafo, dao.getAllVertici(provider));
			
		// Aggiunta degli archi
		for (Adiacenza a : dao.getAllAdiacenze(provider)) {
			Graphs.addEdge(this.grafo, a.getS1(), a.getS2(), a.getPeso());
		}
		
	}
	
	public List<Adiacenza> getQuartieriAdiacenti(String quartiere){
		List<Adiacenza> result = new LinkedList<>();
		
		for (String vicino : Graphs.neighborListOf(this.grafo, quartiere)) {
			result.add(new Adiacenza(quartiere, vicino, this.grafo.getEdgeWeight(this.grafo.getEdge(quartiere, vicino))));
		}
		
		Collections.sort(result);
		return result;
	}
	
	public Map<Tecnico, Integer> simula(int n, String provider, String quartiere){
		Simulatore sim = new Simulatore(this.grafo, this);
		sim.init(n, provider, quartiere);
		sim.run();
		this.durataTotSimulazione = sim.getDurataTotale();
		return sim.getTecnicoHotspot();
	}
	
	public int getDurataTotSimulazione() {
		return durataTotSimulazione;
	}

	public List<Hotspot> getAllHotspotWithProviderAndQuartiere(String provider, String quartiere){
		return dao.getAllHotspotWithProviderAndQuartiere(provider, quartiere);
	}
	
	public Set<String> getAllVertici(){
		return this.grafo.vertexSet();
	}

	public List<String> getAllProvider() {
		return dao.getAllProvider();
	}
	
	public int getNumVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int getNumArchi() {
		return this.grafo.edgeSet().size();
	}

}
