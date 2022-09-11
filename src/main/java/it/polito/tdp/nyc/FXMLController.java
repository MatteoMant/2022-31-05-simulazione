/**
 * Sample Skeleton for 'Scene.fxml' Controller Class
 */

package it.polito.tdp.nyc;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import it.polito.tdp.nyc.model.Adiacenza;
import it.polito.tdp.nyc.model.Model;
import it.polito.tdp.nyc.model.Tecnico;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class FXMLController {
	
	private Model model;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="btnAdiacenti"
    private Button btnAdiacenti; // Value injected by FXMLLoader

    @FXML // fx:id="btnCreaGrafo"
    private Button btnCreaGrafo; // Value injected by FXMLLoader

    @FXML // fx:id="btnCreaLista"
    private Button btnCreaLista; // Value injected by FXMLLoader

    @FXML // fx:id="cmbProvider"
    private ComboBox<String> cmbProvider; // Value injected by FXMLLoader

    @FXML // fx:id="cmbQuartiere"
    private ComboBox<String> cmbQuartiere; // Value injected by FXMLLoader

    @FXML // fx:id="txtMemoria"
    private TextField txtMemoria; // Value injected by FXMLLoader

    @FXML // fx:id="txtResult"
    private TextArea txtResult; // Value injected by FXMLLoader
    
    @FXML // fx:id="clQuartiere"
    private TableColumn<Adiacenza, String> clQuartiere; // Value injected by FXMLLoader
 
    @FXML // fx:id="clDistanza"
    private TableColumn<Adiacenza, Double> clDistanza; // Value injected by FXMLLoader
    
    @FXML // fx:id="tblQuartieri"
    private TableView<Adiacenza> tblQuartieri; // Value injected by FXMLLoader

    @FXML
    void doCreaGrafo(ActionEvent event) {
    	cmbQuartiere.getItems().clear();
    	tblQuartieri.getItems().clear();
    	String provider = cmbProvider.getValue();
    	if (provider == null) {
    		txtResult.appendText("Per favore selezionare un provider dalla tendina!\n");
    		return;
    	}
    	this.model.creaGrafo(provider);
    	txtResult.setText("Grafo creato!\n");
    	txtResult.appendText("# Vertici : " + this.model.getNumVertici() +"\n");
    	txtResult.appendText("# Archi : " + this.model.getNumArchi() +"\n");
    	
    	cmbQuartiere.getItems().addAll(this.model.getAllVertici());
    
    }

    @FXML
    void doQuartieriAdiacenti(ActionEvent event) {
    	String quartiere = cmbQuartiere.getValue();
    	if (quartiere == null) {
    		txtResult.appendText("Per favore selezionare un quartiere dalla tendina!\n");
    		return;
    	}
    	List<Adiacenza> adiacenti = this.model.getQuartieriAdiacenti(quartiere);
    	tblQuartieri.setItems(FXCollections.observableArrayList(adiacenti));
    }

    @FXML
    void doSimula(ActionEvent event) {
    	Integer n = null;
    	try {
    		n = Integer.parseInt(txtMemoria.getText());
        	} catch (NumberFormatException e) {
    		txtResult.appendText("Per favore inserire un numero di tecnici valido!\n");
    		return;
    	}
    	
    	String provider = cmbProvider.getValue();
    	if (provider == null) {
    		txtResult.appendText("Per favore selezionare un provider dalla tendina!\n");
    		return;
    	}
    	
    	String quartiere = cmbQuartiere.getValue();
    	if (quartiere == null) {
    		txtResult.appendText("Per favore selezionare un quartiere dalla tendina!\n");
    		return;
    	}
    
    	// qui possiamo far partire la simulazione
    	Map<Tecnico, Integer> tecnicoHotspot = this.model.simula(n, provider, quartiere);
    	int durataSimulazione = this.model.getDurataTotSimulazione();
    	txtResult.setText("La simulazione è durata " + durataSimulazione + " minuti\n");
    	for (Tecnico t : tecnicoHotspot.keySet()) {
    		txtResult.appendText(t.getIdTecnico() + " - " + tecnicoHotspot.get(t) + "\n");
    	}
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert btnAdiacenti != null : "fx:id=\"btnAdiacenti\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCreaLista != null : "fx:id=\"btnCreaLista\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbProvider != null : "fx:id=\"cmbProvider\" was not injected: check your FXML file 'Scene.fxml'.";
        assert cmbQuartiere != null : "fx:id=\"cmbQuartiere\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtMemoria != null : "fx:id=\"txtMemoria\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
        assert clDistanza != null : "fx:id=\"clDistanza\" was not injected: check your FXML file 'Scene.fxml'.";
        assert clQuartiere != null : "fx:id=\"clQuartiere\" was not injected: check your FXML file 'Scene.fxml'.";

        clQuartiere.setCellValueFactory(new PropertyValueFactory<Adiacenza, String>("s2"));
        clDistanza.setCellValueFactory(new PropertyValueFactory<Adiacenza, Double>("peso"));
    }
    
    public void setModel(Model model) {
    	this.model = model;
    	
    	cmbProvider.getItems().addAll(this.model.getAllProvider());
    	
    }

}
