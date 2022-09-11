package it.polito.tdp.nyc.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.nyc.model.Event.EventType;

public class Simulatore {
	
	// Parametri della simulazione
	private int numTecnici;
	private List<Tecnico> tecnici;
	private List<Hotspot> hotspotRevisionati;
	private List<Hotspot> hotspotDaRevisionare;
	private List<String> quartieriRevisionati;
	private String quartiereCorrente;
	private String provider;
	
	// Output della simulazione
	private int durataTotale; // durata in minuti della simulazione
	private Map<Tecnico, Integer> tecnicoHotspot; // Tecnico --> numero di hotspot da lui revisionati 	
	
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
		this.provider = provider;
		this.quartiereCorrente = quartiere;
		this.tecnicoHotspot = new HashMap<>();
		this.quartieriRevisionati = new LinkedList<>(); 
		this.hotspotRevisionati = new LinkedList<>(); 
		this.tecnici = new LinkedList<>();
		this.durataTotale = 0;
		
		for (int nTecnico = 0; nTecnico < this.numTecnici; nTecnico++) {
			this.tecnici.add(new Tecnico(nTecnico)); // creo un numero di tecnici pari a this.numTecnici 
		}
		
		// questi sono gli hotspot del provider selezionato presenti nel quartiere selezionato
		this.hotspotDaRevisionare = this.model.getAllHotspotWithProviderAndQuartiere(provider, quartiere);
		
		this.queue = new PriorityQueue<>();
		
		for (Hotspot h : this.hotspotDaRevisionare) { // hotspot che devono essere revisionati
			Tecnico t = cercaTecnicoLibero();
			if (t != null) {
				t.setLibero(false);
				this.hotspotRevisionati.add(h);
				this.queue.add(new Event(0, EventType.INIZIO_REVISIONE, t, h));
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
			// perchè tale revisione prima o poi verrà portata a termine
			
			int durataRevisione = 0; 
			if (Math.random() < 0.10) {
				durataRevisione = 10+15;
			} else {
				durataRevisione = 10;
			}
			this.queue.add(new Event(e.getTempo()+durataRevisione, EventType.FINE_REVISIONE, e.getTecnico(), e.getHotspot()));
			
			break;
			
		case FINE_REVISIONE:
			e.getTecnico().setLibero(true);
			if (!this.tecnicoHotspot.containsKey(e.getTecnico())) {
				this.tecnicoHotspot.put(e.getTecnico(), 1);
			} else {
				this.tecnicoHotspot.put(e.getTecnico(), this.tecnicoHotspot.get(e.getTecnico())+1);
			}
			Hotspot hotspot = cercaHotspotDaRevisionare(); // devo capire quale hotspot fare revisionare da questo tecnico 
			if (hotspot != null) {
				int spostamento = 10 + (int)(Math.random()*11);
				this.queue.add(new Event(e.getTempo()+spostamento, EventType.INIZIO_REVISIONE, e.getTecnico(), hotspot));
				this.hotspotRevisionati.add(hotspot);
				e.getTecnico().setLibero(false);
			} else {
				// NON faccio nulla perchè non c'è nessun hotspot da revisionare
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
			return null; // se nessun tecnico è libero restituisco null
		}
		
		int scelto = (int)(Math.random()*candidati.size());
		
		return candidati.get(scelto); 
	}

	private Hotspot cercaHotspotDaRevisionare() { // metodo che mi gestisce le varie casistiche
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
			if (quartierePrecedente != null) {
				this.quartiereCorrente = cercaQuartierePiuVicino(quartierePrecedente);
			}
			if (this.quartiereCorrente != null) {
				this.hotspotDaRevisionare = new LinkedList<>(this.model.getAllHotspotWithProviderAndQuartiere(this.provider, this.quartiereCorrente));
				
				double distanza = this.grafo.getEdgeWeight(this.grafo.getEdge(quartierePrecedente, this.quartiereCorrente));
				int tempoSpostamentoTraQuartieri = (int)(distanza / 50) * 3600;
				
				for (Hotspot h : this.hotspotDaRevisionare) { // hotspot che devono essere revisionati
					Tecnico t = cercaTecnicoLibero();
					if (t != null) {
						t.setLibero(false);
						this.hotspotRevisionati.add(h);
						this.queue.add(new Event(this.durataTotale + tempoSpostamentoTraQuartieri, EventType.INIZIO_REVISIONE, t, h));
					} 
				}
			} else {
				// se invece 'this.quartiereCorrente == null' significa che non ci sono più quartieri che devono essere
				// revisionati e quindi non modifico la lista 'this.hotspotDaRevisionare' che quindi conterrà
				// solo hotspot già revisionati e quindi la simulazione non può andare avanti
			}
			return null;
		}
		
		int scelto = (int)(Math.random()*candidati.size());
		
		return candidati.get(scelto);
	}

	private String cercaQuartierePiuVicino(String quartiere) {
		String quartierePiuVicino = null;
		double minimo = Double.MAX_VALUE;

		for (String q : Graphs.neighborListOf(this.grafo, quartiere)) {
			if (!this.quartieriRevisionati.contains(q)) {
				if (this.grafo.getEdgeWeight(this.grafo.getEdge(quartiere, q)) < minimo) {
					minimo = this.grafo.getEdgeWeight(this.grafo.getEdge(quartiere, q));
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
