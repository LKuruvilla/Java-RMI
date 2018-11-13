/****************************************************************************************
*Programmer: Lovin Kuruvilla
*
*Course: CSCI 5531
*
*Date: 03/26/2018
*
*Assignment: Program 2 Distributed Cryptocurrency Trading System
*
*Environment: Windows with JDK installed and connected to network
*
*Files Included: Coin.java, Cl.java
*
*Purpose: To run a server that creates,edits,views and removes cryptoins objects 
*   as well as create client objects. The server will use remote method 
*   invocation to enable communication between client and coins.
*
*Input: None
*
*Preconditions: Port 4444 and 4445 are used as registry ports.
*
*Output: Cryptocoin objects and client objects are created. Transaction is 
*   enabled between them. 
*
*Postconditions: Client objects are serialized and their login information
*   is stored in "login" txt file.
*
*Algorithm:
*   Create a registry at port 4444 to bind all cryptocoin objects
*   Create a registry at port 4445 to bind all client objects
*   While server has not fully set up
*       run the choice menu
*           if creating cryptocoin objects
*               get coin name
*               get coin abbreviated name
*               get coin description
*               get coin quantity
*               get coin opening price
*               create coin object with constructor
*               bind coin object to coin registry
*           if removing cryptocoin objects
*               get name of all current bound coinobjects
*               put the names in string array
*               get the abbreviated coin name from user
*               while array has contents 
*                   search the coin name in the array
*                      if coin name found
*                         unbind coin from coin registry
*                      else
*                          notify user that coinname is invalid
*           if viewing current coins bound to coin registry
*               get name of all current bound coinobjects
*               put the names in string array
*               while array has contents 
*                   print all coinnames with their quantity and price
*           if editing current coins bound to coin registry
*               get name of all current bound coinobjects
*               put the names in string array
*               get abbreviated name of coin for edit from user
*               while array has contents 
*                   if abbreviated name matches coin abbreviated name
*                       get new coin name
*                       get new coin abbreviated name
*                       get new coin description
*                       get new coin quantity
*                       get new coin opening price
*                   else
*                       wrong coinname
*           if creating client objects to bind to client registry
*                   get client username
*                   get client password
*                   get client buying power
*                   write to file login.txt client username and password
*                   bind client object to client registry.
*   End of user input    
*   Server runs transactions between clients and cryptcoins                           					  
****************************************************************************************/

//import java libraries
import java.io.BufferedWriter;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.Scanner;
import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

//class implements the Cl interface 
class Clients extends UnicastRemoteObject implements Kuruvilla2ClientInterface, Serializable {
    //fields used for the current class
    private String name;
    private String username;
    private String password;
    private double power;
    private String savefile;
    
    //ArrayList used to store buy and sell transactions
    private ArrayList<Kuruvilla2CryptocoinInterface> currentcoins = new ArrayList<Kuruvilla2CryptocoinInterface>();
    
    //Defined getters and setters
    public String getName() throws RemoteException {
        return this.name;
    }
    
    public void addtoarraylist(Kuruvilla2CryptocoinInterface c) throws RemoteException {
        currentcoins.add(c);
    }

    public void removearraylist(Kuruvilla2CryptocoinInterface c) throws RemoteException {
        currentcoins.remove(c);
    }

    public ArrayList<Kuruvilla2CryptocoinInterface> getCurrentcoins() throws RemoteException {
        return currentcoins;
    }

    public double getPower() throws RemoteException {
        return power;
    }

    public void setPower(double power) throws RemoteException {
        this.power = power;
    }

    //Constructors that takes client name, username, password,and buying power
    //as arguments.
    public Clients(String n, String u, String p, double b) throws
            RemoteException {
        this.name = n;
        this.username = u;
        this.password = p;
        this.power = b;
        this.savefile = n + ".ser";

        save();
    }

    //method used to deserialize object and load it from file   
    public void load() throws RemoteException {
        ObjectInputStream in = null;
        Clients c = null;
        try 
        {
            in = new ObjectInputStream(new FileInputStream(this.savefile));
            c = (Clients) in.readObject();
            System.out.println("data has been loaded");
        } catch (Exception e) 
        {
            System.out.println("Failed at loading file for client: "
                    + name + " " + e.getMessage());
        }
        this.power = c.power;

    }
    
    //method used to serialize object and store it in file 
    public void save() throws RemoteException {
        ObjectOutputStream out = null;
        try 
        {
            out = new ObjectOutputStream(new FileOutputStream(savefile));
            out.writeObject(this);

            out.close();
            System.out.println("Client: " + name + " has been saved "
                    + "and has exited");

        } catch (Exception e) 
        {
            System.out.println("Failed at saving client: "
                    + name + " " + e.getMessage());
        }
    }

    //method used to verify client's credntials and to load client's savefile
    public boolean login(String u, String p) throws RemoteException {
        if (u.equals(this.username)) {
            if (p.equals(this.password)) {
                load();
                System.out.println(name + " has logged in.");
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    //method to check client's current buying power
    public String status() throws RemoteException {
        return String.format("Client " + name + " has " + power + " left.");
    }

}

//class implements the Coin interface 
class Cryptocoin extends UnicastRemoteObject implements Kuruvilla2CryptocoinInterface, Serializable {
    //fields used for the coin class
    private String coinname;
    private String abvname;
    private String desc;
    private int quantity;
    private double marketcap;
    private int tradingvolume;
    private double openingprice;
    
    //Defined getters and setters
    public String getCoinname() throws RemoteException {
        return coinname;
    }

    public void setCoinname(String coinname) throws RemoteException {
        this.coinname = coinname;
    }

    public String getAbvname() throws RemoteException {
        return abvname;
    }

    public void setAbvname(String abvname) throws RemoteException {
        this.abvname = abvname;
    }

    public String getDesc() throws RemoteException {
        return desc;
    }

    public void setDesc(String desc) throws RemoteException{
        this.desc = desc;
    }

    public int getQuantity() throws RemoteException {
        return quantity;
    }

    public void setQuantity(int quantity) throws RemoteException {
        this.quantity = quantity;

    }

    public void getMarketcap()throws RemoteException {
        this.marketcap = this.quantity*this.openingprice;
    }

    public void setMarketcap()throws RemoteException {
        this.marketcap = this.quantity*this.openingprice;
    }

    public int getTradingvolume() {
        return tradingvolume;
    }

    public void setTradingvolume(int tv) throws RemoteException{
        this.tradingvolume = tradingvolume+tv;
    }

    public double getOpeningprice() throws RemoteException {
        return openingprice;
    }

    public void setOpeningprice(double openingprice) throws RemoteException {
        this.openingprice = openingprice;
    }
    
    //Defined a remote constructor for class that takes coin name, abbreviated 
    //name, quantity, opening price and description as arguments
    public Cryptocoin(String cname, String aname, int quant, double oprice, String d)
            throws RemoteException {
        this.coinname = cname;
        this.abvname = aname;
        this.quantity = quant;
        this.openingprice = oprice;
        this.desc = d;
        this.marketcap = this.quantity*this.openingprice;
        this.tradingvolume =0;
    }

    //Prints the coins name, current quantity and price
    public String print() throws RemoteException {   //System.out.println("Printing");
        return String.format(abvname + " quantity: " + quantity 
                + " price: " + openingprice);
    }
    
    //Returns a coin that is initialized based on coin b's values and the 
    //coint amount equal to the amount argument
    public Kuruvilla2CryptocoinInterface buy(Kuruvilla2CryptocoinInterface a, Kuruvilla2CryptocoinInterface b, int amount) throws RemoteException {
        try 
        {
            a = new Cryptocoin(b.getCoinname(), b.getAbvname(), 
                    amount, b.getOpeningprice(),b.getDesc());
        } 
        catch (RemoteException ex) {
            Logger.getLogger(Cryptocoin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return a;
    }

}

//Declaration of main class
public class Kuruvilla2Server {
    //main method
    public static void main(String[] argv) 
    {
        
        //Defined scanner and registry
        Scanner scan = new Scanner(System.in);
        Registry reg = null, cli = null;

        try 
        {   //created client registry at port 4445 and coin registry at 
            //port 4444
            reg = LocateRegistry.createRegistry(4444);
            cli = LocateRegistry.createRegistry(4445);
        } 
        catch (Exception e) 
        {
            System.out.println("Failed to create registry " + e.getMessage());
        }
        
        //method containing choices for the server's methods
        menu(scan, reg, cli);

    }
    
    public static void menu(Scanner x, Registry reg, Registry cli) 
    /***********************************************************************************
    *Purpose: To select the right method that allows users to add,remove,view,
    *         edit coins and add clients
    *
    *Parameters: Scanner object, Registry for client objects, Registry for coin 
    *            objects.
    *
    *Action: Selects the desierd user method.
    ************************************************************************************/
    {

        System.out.println("Please select from the following options.\n"
                + "1: addcoins, 2: removecoins, "
                + "3: editcoins, 4: view current coins, "
                + "5: add client, 6:Start Server");

        int choice = x.nextInt();

        while (choice != 0) {
            switch (choice) {
                case 1:
                    addcoins(x, reg);
                    break;
                case 2:
                    removecoins(x, reg);
                    break;
                case 4:
                    viewcurrentcoin(reg);
                    break;
                case 3:
                    editcoins(x,reg);
                    break;
                case 5:
                    createclient(x, cli);
                    break;
                case 6:
                    System.out.println("Server has started.");
                    int quit = 0;
                    if ((quit = x.nextInt()) == 0) {
                        return;
                    }
                    break;

            }
            System.out.println("Please select from the following options.\n"
                + "1: addcoins, 2:  removecoins, "
                + "3: editcoins, 4: view current coins, "
                + "5: add client, 6:Start Server");

            choice = x.nextInt();

        }

    }

    public static void addcoins(Scanner x, Registry reg) 
    /***********************************************************************************
    *Purpose: To create new coin objects and bind them to registry
    *
    *Parameters: Scanner object, Registry for coin objects.
    *
    *Action: Creates new coin objects and binds them to coin registry
    ************************************************************************************/
    {
        try {
            System.out.println("Please Enter the name of the coin");
            String cname = x.next();

            System.out.println("Please Enter the abv of the coin");
            String aname = x.next();
            
            System.out.println("Please Enter the a short description of the "
                    + "the coin");
            
            String desc = x.next();
            desc+=x.nextLine();
            
            
            System.out.println("Please Enter the quantity of "
                    + "the coin");
            int quant = x.nextInt();

            System.out.println("Please Enter the opening price of "
                    + "the coin");
            double op = (double) x.nextInt();
            
            
            
            
            

            reg.rebind(cname, new Cryptocoin(cname, aname, quant, op, desc));

            System.out.println("Server done binding");
        } catch (Exception e) {
            System.out.println("Coin failed to bind. " + e);
        }
    }

    public static void removecoins(Scanner x, Registry reg) 
    /***********************************************************************************
    *Purpose: To remove current coin objects and unbind them from registry
    *
    *Parameters: Scanner object, Registry for coin objects.
    *
    *Action: removes selected coin object and unbind them from coin registry
    ************************************************************************************/
    {
        String[] name = null;
        try {
        name = reg.list();
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println("Please enter the abbreviated coin name to remove");
        String remove = x.next();

        for (String y : name) 
        {
            Kuruvilla2CryptocoinInterface temp = null;
            try {
                temp = (Kuruvilla2CryptocoinInterface) reg.lookup(y);
                if (temp.getAbvname().contains(remove)) {
                    reg.unbind(y);
                    System.out.println(remove + " has been removed");
                }

            } catch (Exception ex) 
            {
                System.out.println(ex.getMessage());
            }

        }
        try 
        {
            name = reg.list();
        } 
        catch (RemoteException ex) 
        {
            System.out.println(ex.getMessage());
        }

    }
    
    public static void viewcurrentcoin(Registry reg)
    /***********************************************************************************
    *Purpose: To view current coin objects and their fields.
    *
    *Parameters: Registry for coin objects.
    *
    *Action: Prints all current coin objects in registry with their abbreviated 
    *        name,quanitity and opening price.
    ************************************************************************************/        
    {   try
        {
            String[]name = reg.list();
            for (String y : name) 
            {
                Kuruvilla2CryptocoinInterface temp = null;

                    temp = (Kuruvilla2CryptocoinInterface) reg.lookup(y);
                    System.out.println(temp.print());
            }
            
        }
        catch (Exception e)
        {System.out.println(e.getMessage());
        }
    }
        
    public static void editcoins(Scanner x, Registry reg)
    /***********************************************************************************
    *Purpose: To edit current coin object fields.
    *
    *Parameters: Scanner object, Registry for coin objects.
    *
    *Action: Edits selected current coin objects in registry. Changes done to
    *        coin name,abbreviated name, description, quantity and price.
    ************************************************************************************/
    {
        String[] name = null;
        try 
        {
        name = reg.list();
        } catch (RemoteException ex) 
        {
            System.out.println(ex.getMessage());
        }
        
        System.out.println("Please enter the abbreviated coin name to edit");
        String edit = x.next();

        for (String y : name) 
        {
            Kuruvilla2CryptocoinInterface temp = null;
            try {
                temp = (Kuruvilla2CryptocoinInterface) reg.lookup(y);
                if (temp.getAbvname().contains(edit)) {
                    System.out.println("Please Enter the name of the coin");
                    String cname = x.next();
                    temp.setCoinname(cname);
                    
                    System.out.println("Please Enter the abv of the coin");
                    String aname = x.next();
                    temp.setAbvname(aname);

                    System.out.println("Please Enter the a short description of the "
                            + "the coin");

                    String desc = x.next();
                    desc+=x.nextLine();
                    
                    temp.setDesc(desc);


                    System.out.println("Please Enter the quantity of "
                            + "the coin");
                    int quant = x.nextInt();
                    
                    temp.setQuantity(quant);

                    System.out.println("Please Enter the opening price of "
                            + "the coin");
                    double op = (double) x.nextInt();
                    temp.setOpeningprice(op);
                    
                    
                    
                    System.out.println(edit + " has been edited and saved");
                }

            } 
            catch (Exception ex)
            {System.out.println(ex.getMessage());

            }

        }
        
        
    }

    public static void createclient(Scanner x, Registry client) 
    /***********************************************************************************
    *Purpose: To create client objects and to bind them to client registry
    *
    *Parameters: Scanner object, Registry for client objects.
    *
    *Action: Creates client objects, binds them to client registry and stores 
    *        their login information to login.txt file
    ************************************************************************************/
    {
        System.out.println("Please enter client username");
        String username = x.next();

        System.out.println("Please enter client password");
        String password = x.next();

//        System.out.println("Please enter client save file name");
//        String savef = x.next();
        System.out.println("Please enter client power");
        double buy = (double) x.nextInt();

        System.out.println("Please enter the client name");
        String name = x.next();

        System.out.println("creating client now");

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("login.txt", true)));
            out.println(username + " " + password);
            out.close();
            System.out.println(username + " " + password);
        } catch (IOException e) {
            System.out.println("Failed at saving login "
                    + "information to file " + e.getMessage());
        }

        try {
            client.rebind(name, new Clients(name, username, password, buy));
        } catch (Exception e) {
            System.out.println("Failed at binding client"
                    + " to registry " + e.getMessage());
        }

    }

}
