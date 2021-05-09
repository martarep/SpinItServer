
package hr.algebra.controller;

import hr.algebra.model.Player;
import hr.algebra.multicast.ServerThread;
import hr.algebra.rmi.ChatServer;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;


public class ServerScreenController implements Initializable {

    private ServerThread serverThread;
    private final String TIME_FORMAT = "HH:mm:ss";
    private static final String MESSAGE_FORMAT = "%s [%s]: %s";
    private static final String SERVER_NAME = "Server";
    private static final int MESSAGE_LENGTH = 60;

    private ObservableList<Node> messages;

    private ChatServer chatServer;

    @FXML
    private Label lblId;
    @FXML
    private Label lblName;
    @FXML
    private Label lblPoints;
    @FXML
    private Label lblGame;
    @FXML
    private Label lblChallenge;

    public int numberOfPlayers;
    @FXML
    private ScrollPane spContainer;
    @FXML
    private VBox vbMessages;
    @FXML
    private Button btnSend;
    @FXML
    private TextField tfMessage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        serverThread = new ServerThread(this);
        serverThread.setDaemon(true);
        serverThread.start();
 
    }

    public void setNumberOfPlayers(int number) {
        numberOfPlayers = number;
        initChatServer();
    }

    public void showPlayersData(Player player) {
        lblId.setText(String.valueOf(player.getId()));
        lblPoints.setText(String.valueOf(player.getPoints()));
        lblName.setText(player.getName());
        lblChallenge.setText(player.getChallenge());
        lblGame.setText(player.getGame());
       
    }

    @FXML
    private void send(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }

    @FXML
    private void sendMessage() {
         if (tfMessage.getText().trim().length() > 0) {
            chatServer.sendMessage(tfMessage.getText().trim(), SERVER_NAME);
            addMessage(tfMessage.getText().trim(), SERVER_NAME);
            tfMessage.clear();
        }
    }

     private void addMessage(String message, String name) {
        Label label = new Label();
        label.setText(String.format(MESSAGE_FORMAT, LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT)), name, message));
        label.setId("message");
        label.wrapTextProperty().setValue(true);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        messages.add(label);
        moveScrollPane();
    }
 public void postMessage(String message, String name) {
        Platform.runLater(() -> addMessage(message, name));
        if (name != SERVER_NAME) {
         chatServer.sendMessage(message, name);
     }
    }

    private void initChatServer() {
       chatServer = new ChatServer(this);
        messages = FXCollections.observableArrayList();
      
        Bindings.bindContentBidirectional(messages, vbMessages.getChildren());
        
        tfMessage.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.length() >= MESSAGE_LENGTH) {
                        ((StringProperty) observable).setValue(oldValue);
                    }
                }
        ); }
        private void moveScrollPane() {
        spContainer.applyCss();
        spContainer.layout();
        spContainer.setVvalue(1D);
    }
}
