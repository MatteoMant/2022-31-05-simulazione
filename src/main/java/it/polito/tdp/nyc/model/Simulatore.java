package it.polito.tdp.nyc.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.nyc.model.Event.EventType;

public class Simulatore {
	
	// Parametri della simulazione
	private int numTecnici;
	private List<Tecnico> tecnici;
	
	// Output della simulazione
	private int durataTotale; // int ? 
	private Map<Tecnico, Integer> tecnicoHotspot; // Tecnico --> numero di hotspot da lui revisionati 
	private List<Hotspot> hotspotRevisionati;
	private List<Hotspot> hotspotDaRevisionare;
	private List<String> quartieriRevisionati;
	
	private String quartiereCorrente;
	private String provider;
	
	// Stato del mondo
	private Graph<String, DefaultWeightedEdge> grafo;
	private Model model;
	
	// Coda degli eventi
	private PriorityQueue<Event> queue;
	
	public Simulatore(Graph<String, DefaultWeightedEdge> grafo, Model model) {
		this.grafo = grafo;
		this.model = model;
	}
	
	public void init(int n, String provider, String quartiere) {
		this.numTecnici = n;
		this.quartiereCorrente = quartiere;
		this.provider = provider;
		this.tecnicoHotspot = new HashMap<>();
		this.quartieriRevisionati = new LinkedList<>(); 
		this.hotspotRevisionati = new LinkedList<>(); 
		this.tecnici = new LinkedList<>();
		for (int nTecnico = 0; nTecnico < this.numTecnici; nTecnico++) {
			this.tecnici.add(new Tecnico(nTecnico));
		}
		
		// questi sono gli hotspot del provider selezionato presenti nel quartiere selezionato
		this.hotspotDaRevisionare = this.model.getAllHotspotWithProviderAndQuartiere(provider, quartiere);
		
		
		this.queue = new PriorityQueue<>();
		
		for (Hotspot h : this.hotspotDaRevisionare) { // hotspot che devono essere revisionati
			Tecnico t = cercaTecnicoLibero();
			this.hotspotRevisionati.add(h);
			if (t != null) {
				t.setLibero(false);
				this.queue.add(new Event(1, EventType.INIZIO_REVISIONE, t, h));
			}
		}
		
	}

	public void run() {
		while (!this.queue.isEmpty()) {
			Event e = this.queue.poll();
			this.durataTotale = e.getTempo();
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		
		switch(e.getTipo()) {
		case INIZIO_REVISIONE:
			// se l'evento è di tipo 'INIZIO_REVISIONE' allora dovrò generare un nuovo evento di tipo 'FINE_REVISIONE'
			// perchè tale revisione verrà portata a termine
			
			int durataRevisione = 0; 
			if (Math.random() < 0.10) {
				durataRevisione = 10+15;
			} else {
				durataRevisione = 10;
			}
			this.queue.add(new Event(e.getTempo()+durataRevisione, EventType.FINE_REVISIONE, e.getTecnico(), e.getHotspot()));
			
			break;
			
		case FINE_REVISIONE:
			if (!this.tecnicoHotspot.containsKey(e.getTecnico())) {
				this.tecnicoHotspot.put(e.getTecnico(), 1);
			} else {
				this.tecnicoHotspot.put(e.getTecnico(), this.tecnicoHotspot.get(e.getTecnico())+1);
			}
			Hotspot hotspot = cercaHotspotDaRevisionare(); // devo capire quale hotspot fare revisionare da questo tecnico 
			if (hotspot != null) {
				int spostamento = 10 + (int)(Math.random()*11);
				this.queue.add(new Event(e.getTempo()+spostamento, EventType.INIZIO_REVISIONE, e.getTecnico(), hotspot));
			}
			
			break;
		}
		
	}
	
	private Tecnico cercaTecnicoLibero() {
		List<Tecnico> candidati = new LinkedList<>();
		
		for (Tecnico tecnico : this.tecnici) {
			if (tecnico.isLibero()) {
				candidati.add(tecnico);
			}
		}
		
		if (candidati.size() == 0) {
			return null;
		}
		
		int scelto = (int)(Math.random()*candidati.size());
		
		return candidati.get(scelto); // se nessun tecnico è libero restituisco null
	}

	private Hotspot cercaHotspotDaRevisionare() {
		List<Hotspot> candidati = new LinkedList<>();
		
		for (Hotspot h : this.hotspotDaRevisionare) {
			if (!this.hotspotRevisionati.contains(h)) {
				candidati.add(h); // allora tale hotspot deve ancora essere revisionato
			}
		}
		
		if (candidati.size() == 0) { // gli hotspot di un quartiere sono stati tutti revisionati
			// in questo caso devo passare alla revisione degli hotspot del quartiere più vicino a quello appena revisionato
			this.quartieriRevisionati.add(this.quartiereCorrente);
			String quartierePrecedente = this.quartiereCorrente;
			this.quartiereCorrente = cercaQuartierePiuVicino(this.quartiereCorrente);
			if (this.quartiereCorrente != null) {
				this.hotspotDaRevisionare = this.model.getAllHotspotWithProviderAndQuartiere(this.provider, this.quartiereCorrente);
				
				double distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(quartierePrecedente, this.quartiereCorrente));
				int tempoSpostamentoTraQuartieri = (int)(distanza / 50) * 3600;
				
				for (Hotspot h : this.hotspotDaRevisionare) { // hotspot che devono essere revisionati
					Tecnico t = cercaTecnicoLibero();
					this.hotspotRevisionati.add(h);
					if (t != null) {
						t.setLibero(false);
						this.queue.add(new Event(this.durataTotale + tempoSpostamentoTraQuartieri, EventType.INIZIO_REVISIONE, t, h));
					}
				}
			}
			return null;
		}
		
		int scelto = (int)(Math.random()*candidati.size());
		
		return candidati.get(scelto); // se nessun hotspot è da revisionare restituisco null
	}

	private String cercaQuartierePiuVicino(String quartiere) {
		String quartierePiuVicino = null;
		double minimo = Double.MAX_VALUE;
		
		for (String q : this.grafo.vertexSet()) {
			if (!this.quartieriRevisionati.contains(q)) {
				if (this.grafo.getEdgeWeight(this.grafo.getEdge(q, quartiere)) < minimo) {
					minimo = this.grafo.getEdgeWeight(this.grafo.getEdge(q, quartiere));
					quartierePiuVicino = q;
				}
			}
		}
		
		return quartierePiuVicino;
	}

	public Map<Tecnico, Integer> getTecnicoHotspot() {
		return tecnicoHotspot;
	}

	public int getDurataTotale() {
		return durataTotale;
	}

}
