
package hr.algebra.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;



public interface ChatService extends Remote {
    void send(String message, String name) throws RemoteException;
}