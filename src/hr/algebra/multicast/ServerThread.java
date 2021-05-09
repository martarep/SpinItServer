
package hr.algebra.multicast;

import hr.algebra.controller.ServerScreenController;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import hr.algebra.model.Player;
import hr.algebra.utils.ByteUtils;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import javafx.application.Platform;


public class ServerThread extends Thread {

    public static int SERVER_PORT;
    public static int CLIENT_PORT;
    public static String GROUP;

    static {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File("socket.properties")));
            SERVER_PORT = Integer.valueOf(properties.getProperty("SERVER_PORT"));
            CLIENT_PORT = Integer.valueOf(properties.getProperty("CLIENT_PORT"));
            GROUP = properties.getProperty("GROUP");
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final LinkedBlockingDeque<Player> players = new LinkedBlockingDeque<>();

    private final ServerScreenController controller;
    private Object lock = new Object();

    public ServerThread(ServerScreenController controller) {
        this.controller = controller;
    }

    public void trigger(Player player) {
        players.add(player);
    }

    @Override
    public void run() {
        try (DatagramSocket server = new DatagramSocket(SERVER_PORT)) {
         

            while (true) {

                if (!players.isEmpty()) {
                

                    byte[] playerBytes;
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos)) {

                        oos.writeObject(players.getFirst());
                        players.clear();
                        oos.flush();
                        playerBytes = baos.toByteArray();
                    }

                    byte[] numberOfPlayerBytes = ByteUtils.intToByteArray(playerBytes.length);
                    InetAddress groupAddress = InetAddress.getByName(GROUP);
                    DatagramPacket packet = new DatagramPacket(numberOfPlayerBytes, numberOfPlayerBytes.length, groupAddress, CLIENT_PORT);
                    server.send(packet);

                    packet = new DatagramPacket(playerBytes, playerBytes.length, groupAddress, CLIENT_PORT);
                    server.send(packet);
                }

                
                byte[] numberOfPlayerBytes = new byte[4];
                DatagramPacket packet = new DatagramPacket(numberOfPlayerBytes, numberOfPlayerBytes.length);
                server.receive(packet);
                int length = ByteUtils.byteArrayToInt(numberOfPlayerBytes);

                byte[] playerBytes = new byte[length];
                packet = new DatagramPacket(playerBytes, playerBytes.length);
                server.receive(packet);
                try (ByteArrayInputStream baos = new ByteArrayInputStream(playerBytes);
                        ObjectInputStream oos = new ObjectInputStream(baos)) {
                    Player player = (Player) oos.readObject();

                    trigger(player);
                    Platform.runLater(() -> {
                        controller.showPlayersData(player);

                    });

                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
