//import statements for Coin interface
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

//declaration of public interface 'Client' used by both client and server
public interface Kuruvilla2ClientInterface extends Remote {

    public boolean login(String u, String p) throws RemoteException;

    public String status() throws RemoteException;

    public void load() throws RemoteException;

    public void save() throws RemoteException;

    public String getName() throws RemoteException;

    public  double getPower() throws RemoteException;

    public void setPower(double power) throws RemoteException;

    public ArrayList<Kuruvilla2CryptocoinInterface> getCurrentcoins() throws RemoteException;

    public void addtoarraylist(Kuruvilla2CryptocoinInterface c) throws RemoteException;

    public void removearraylist(Kuruvilla2CryptocoinInterface c) throws RemoteException;

}
