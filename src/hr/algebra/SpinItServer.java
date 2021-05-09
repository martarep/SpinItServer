
package hr.algebra;

import hr.algebra.controller.ServerScreenController;
import java.io.IOException;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class SpinItServer extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        Application.Parameters params = getParameters();
        Map<String, String> namedParams = params.getNamed();
        int numberOfPlayers = Integer.parseInt(namedParams.get("numberOfPlayers"));

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/algebra/view/ServerScreen.fxml"));
        Parent root = loader.load();
        ServerScreenController serverScreenController = loader.getController();

     
        primaryStage.setTitle("ServerScreen");
        primaryStage.setScene(new Scene(root));
        serverScreenController.setNumberOfPlayers(numberOfPlayers);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.show();

    }

  
    public static void main(int numberOfPlayers) {
        launch();
    }

}
