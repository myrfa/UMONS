package gui.ctrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;

import framework.Article;
import framework.Category;
import framework.Machine;
import framework.RawMaterial;
import framework.modules.Module;
import framework.payement.Carte;
import framework.payement.Coin;
import framework.payement.Payment;
import framework.payement.Token;
import framework.stockage.Classic;
import framework.stockage.Stockage;
import gui.MainApp;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class VueUtilisateur extends Pane {    
    
	private Stage mainApp, stage;
	private Scene scene;
	private Machine machine;
	private Category focusCategory;
	private Article focusArticle;
	
    public VueUtilisateur(Stage parent, Machine machine) {
        this.mainApp = parent;
        this.machine = machine;
        this.focusCategory = machine.getCategory();
        this.focusArticle = null;
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/views/VueUtilisateur.fxml"));
        fxmlLoader.setController(this);
        
        try {
        	stage = new Stage();
            scene = new Scene(fxmlLoader.load()); 
            stage.setTitle("Cr�ation d'une nouvelle machine");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

        	initialize(stage);
        	parent.toBack();
        	stage.show();
        	
        } catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void initialize(Stage stage) {
    	TabPane tp = (TabPane)scene.lookup("#tabCategory");
    	tp.getTabs().clear();
    	
    	Category c = focusCategory;
    	do {
    		Tab tab = new Tab(c.toString());
        	FlowPane p = new FlowPane();
        	
        	if( c.getCategories() != null ) {
        		for( Category c2 : c.getCategories() ) {
        			Pane n = Utils.addNewArticle(p, c2);
        			n.setOnMousePressed(new EventHandler<MouseEvent>() {
                	    public void handle(MouseEvent e) {
                	    	focusCategory = c2;
                	    	initialize(stage);
                	    }
        			});
        		}
        	}
    		
        	if( c.getArticles() != null ) {
        		for( Article a : c.getArticles() ) {
        			Pane n = Utils.addNewArticle(p, a);
        			n.setOnMousePressed(new EventHandler<MouseEvent>() {
                	    public void handle(MouseEvent e) {
                	    	
                	    	MainApp.getState().setItemPrice(a.getPrice());
                	    	MainApp.getState().raiseAddItem();
                	    	MainApp.getState().runCycle();
                	    	
                	    	focusArticle = a;
                	    	updatePayement();
                	    }
        			});
        		}
    		}
        	
        	tab.setContent(p);
        	tp.getTabs().add( tab );
        	
        	c = c.getParent();
    	} while( c != null );
    	
    	((ImageView)scene.lookup("#PayByCard")).setVisible(false);
    	((ImageView)scene.lookup("#PayByCoin")).setVisible(false);
    	((ImageView)scene.lookup("#PayByTokken")).setVisible(false);
    	for( Module m : machine.getModules() ) {
			if( m instanceof Payment ) {
				if( m instanceof Carte ) {
					((ImageView)scene.lookup("#PayByCard")).setVisible(true);
					((Node)scene.lookup("#PayByCard")).setUserData((Payment)m);
				}
				else if( m instanceof Coin ) {
					((ImageView)scene.lookup("#PayByCoin")).setVisible(true);
					((Node)scene.lookup("#PayByCoin")).setUserData((Payment)m);
				}
				else if( m instanceof Token ) {
					((ImageView)scene.lookup("#PayByTokken")).setVisible(true);
					((Node)scene.lookup("#PayByTokken")).setUserData((Payment)m);
				}
			}
    	}
    }
    private void updatePayement() {
    	if( focusArticle != null ) {
    		((ImageView)scene.lookup("#image")).setVisible(true);
    		((Label)scene.lookup("#name")).setVisible(true);
    		((Label)scene.lookup("#price")).setVisible(true);
    		
    		((ImageView)scene.lookup("#image")).setImage(new Image(focusArticle.getImage().toURI().toString()));
    		((Label)scene.lookup("#name")).setText(focusArticle.getName());
    		((Label)scene.lookup("#price")).setText(focusArticle.getPrice()+"�");
    	}
    	else {
    		((ImageView)scene.lookup("#image")).setVisible(false);
    		((Label)scene.lookup("#name")).setVisible(false);
    		((Label)scene.lookup("#price")).setVisible(false);
    	}
    	
    	
    	((Label)scene.lookup("#solde")).setText("Solde: "+MainApp.getState().getTotalPaid()+" �");
    }
    @FXML
    private void handleExit() {
    	MainApp.getState().getSCInterface().raiseMaintenance();
		MainApp.getState().runCycle();
    	stage.close();
    }
    @FXML
    private void OnClick_Buy(Event e) {
       	Payment p = (Payment)((Node)e.getTarget()).getUserData();
    	
    	if( p instanceof Coin ) {
    		Coin c = (Coin)p;
    		
    		ChoiceDialog<Integer> dialog = new ChoiceDialog<Integer>();
	    	dialog.getItems().setAll(c.getModules());
	    	dialog.setTitle("Choisissez une pi�ce");
	    	dialog.setHeaderText("Choisissez une pi�ce");
	    	Optional<Integer> result = dialog.showAndWait();
	    	if (result.isPresent()){
	    		if( c.insertPiece(result.get()) ) {
	    			MainApp.getState().setPiece( result.get() );
	    			MainApp.getState().raiseInsertPiece();
	    			//solde += result.get();
	    		}
	    		updatePayement();
	    	}
    	}
    	
    	if( focusArticle != null  ) {
    		Machine.Delivery d =  machine.Buy(p, focusArticle, Math.toIntExact(MainApp.getState().getTotalPaid()));
    		
    		if( d.getArticle() != null ) {
    			Alert alert = new Alert(AlertType.CONFIRMATION);
    	    	alert.setTitle("Achat");
    	    	alert.setHeaderText("Votre achat s'est d�roul� avec succ�s.");
    	    	if( d.getOther() != null ) {
    	    		alert.setContentText("La machine a aussi distribu�: " + d.getOther());
    	    		
    	    		if( d.getOther() instanceof ArrayList ) {
    	    			ArrayList<Object> refund = (ArrayList<Object>) d.getOther();
    	    			/*if( refund.size() > 0 && refund.get(0) instanceof Integer ) {
    	    				for( Object a : refund ) {
    	    					solde -= (Integer)a;
    	    				}
    	    			}*/	
    	    		}
    	    	}
    	    	
    	    	//solde -= d.getArticle().getPrice();
    	    	focusArticle = null;
    	    	updatePayement();
    	    	
    	    	alert.show();
    	    }
    		else {
    			Alert alert = new Alert(AlertType.ERROR);
    	    	alert.setTitle("Erreur");
    	    	alert.setHeaderText("Le paiement a �chou�.");
    	    	alert.show();
    		}
    	}
    }
    @FXML
    private void OnClick_Cancel() {
    	focusArticle = null;
    	updatePayement();
    }
}