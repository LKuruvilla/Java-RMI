//import statements for Coin interface


import java.rmi.Remote;
import java.rmi.RemoteException;

//declaration of public interface 'Coin' used by both client and server
public interface Kuruvilla2CryptocoinInterface extends Remote {

    public String print() throws RemoteException;

    public String getCoinname() throws RemoteException;

    public String getAbvname() throws RemoteException;

    public int getQuantity() throws RemoteException;

    public void setOpeningprice(double openingprice) throws RemoteException;

    public double getOpeningprice() throws RemoteException;

    public void setQuantity(int quantity) throws RemoteException;

    public void setCoinname(String coinname) throws RemoteException;

    public Kuruvilla2CryptocoinInterface buy(Kuruvilla2CryptocoinInterface a, Kuruvilla2CryptocoinInterface b, int amount) throws RemoteException;
    
    public  void getMarketcap()throws RemoteException;
    
    public void setMarketcap()throws RemoteException;
    
    public String getDesc() throws RemoteException;
    
    public void setAbvname(String abvname) throws RemoteException ;
    
    public void setDesc(String desc) throws RemoteException;
    
    public void setTradingvolume(int tv) throws RemoteException;

}
