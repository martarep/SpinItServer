/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.rmi;

import hr.algebra.controller.ServerScreenController;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChatServer {

    private static final String RMI_CLIENT = "client";
    private static final String RMI_SERVER = "server";
    private static final int REMOTE_PORT = 1099;
    private static final int RANDOM_PORT_HINT = 0;

    private ChatService server;
    private List<ChatService> clients;
    private ChatService client;
    private Registry registry;

    private final ServerScreenController serverScreenController;

    public ChatServer(ServerScreenController serverScreenController) {
        this.serverScreenController = serverScreenController;
        publishServer();
        waitForClient();
    }

    public void publishServer() {
        server = new ChatService() {

            @Override
            public void send(String message, String name) throws RemoteException {
                serverScreenController.postMessage(message, name);
            }
        };

        try {

            registry = LocateRegistry.createRegistry(REMOTE_PORT);
            ChatService stub = (ChatService) UnicastRemoteObject.exportObject(server, RANDOM_PORT_HINT);
            registry.rebind(RMI_SERVER, stub);

        } catch (RemoteException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void waitForClient() {
        clients = new ArrayList<ChatService>();

        Thread thread = new Thread(() -> {
            while (clients.size() != serverScreenController.numberOfPlayers) {
                try {
                    client = (ChatService) registry.lookup(RMI_CLIENT + String.valueOf(clients.size() + 1));
                    clients.add(client);

                } catch (RemoteException | NotBoundException ex) {
                  
                }
               
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });
        thread.setDaemon(true);
        thread.start();

    }

    public void sendMessage(String message, String name) {
        try {
            for (ChatService c : clients) {
                c.send(message, name);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
