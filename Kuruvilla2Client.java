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
*Purpose: To run a client that will interact with a server and do cryptocoin
*   transactions using remote method invocations. The client's file will be 
*   deserialized when logging in and serialized when logging out.
*
*Input: Ip address of the server.
*
*Preconditions: Port 4444 and 4445 are used as registry ports.
*
*Output: Client runs transactions and net cryptocoin transactions are recorded.
*
*Postconditions: Client file is serialized.
*
*Algorithm:
*   Get server ip either from commandline or use localhost
*   Create two String arrays
*   Use registry to get cient object at port 4445
*   use registry to get coin object at port 4444
*   Use first string array to get names of all client objects bound to regsitry
*   Use next string array to get names of all coin objects bound to regsitry
*   run authenticate method to authorize client's identity
*   use this as the client from here onwards
*   While client has not exited
*       run the choice menu
*           if browsing coin available in market
*               get all coin names and store in string array
*                   for each string in array
*                       use coin registry to lookup the coin
*                       print the coin's current values
*           if viewing client's current status
*               print client's current buying power
*           if buying coins from market
*               get abbreviated name from user
*               get all coin names and store in string array
*                   for each string in array
*                       if coinname match found
*                           get purchase amount
*                           update coin's market quantity
*                           update coins's market cap
*                           update coin's trading volume
*                           update client's buying power
*                           create new coin using purchased values
*                           add the new coin to client's arraylist
*           if selling coins from holdings
*               check to see if holdings is not empty
*               get abbreviated name from user
*               get amount to sell from user
*               get all coin names in holdings from client's arraylist
*                   for each coin in list
*                       if coinname match found and if enough quantity to sell
*                           update coin quantity
*                           for each coin in the coin registry
*                               if coin name in registry matches coin name
*                               in list
*                                   update coin's quantity in registry
*                                   update coin's market cap in registry
*                                   update coin's trading volume in registry
* *                                 update coin's quantity in registry
*                                   update client's power
*                                   if coin in list has 0 quantity
*                                       remove coin from arraylist
*           if viewing current coins in holdings
*               get arrraylist of coins from client
*               while arraylist is not empty
*                   print each coin's abbreviated name and quantity
*           if qutting
*               call client's serialization method
*   End of program                        					  
****************************************************************************************/
//import java libraries
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//Declaration of main class
public class Kuruvilla2Client 
{
    //main method
    public static void main(String[] args) 
    {
        String name = null;
        //using local host if no arguments given
        if (args.length == 0) 
        {
            name = "127.0.0.1";
        } 
        else 
        {
            name = args[0];
        }
        
        String[] coinnames = null;        
        String[] clientnames = null;
        Scanner x = new Scanner(System.in);
        
        Registry clients = null, reg = null;
        
        //used to store the right client's identity
        Kuruvilla2ClientInterface actualclient = null;
        try 
        {   //store client registry
            clients = LocateRegistry.getRegistry(name, 4445);
            //store coin registry
            reg = LocateRegistry.getRegistry(name, 4444);
            //store all coin objects in registry
            coinnames = reg.list();
            //store all client objects in registry
            clientnames = clients.list();
            
        } catch (Exception e) {
            System.out.println("Failed to bind at client side " + e.getMessage());
        }
        //store the client's identity
        actualclient = authenticate(x, clientnames, clients);
        //choice menu for client to interact
        menu(coinnames, actualclient, reg, x);
        
    }
    
    public static Kuruvilla2ClientInterface authenticate(Scanner x, String[] clientreg, Registry c) 
    /***********************************************************************************
    *Purpose: To select the right client among the list of client objects bound 
    *         in client registry
    *
    *Parameters: Scanner object, String array holding client object names, 
    *            client registry.
    *
    *Action: Selects the right client by verifying username and password
    *        with all the client objects bound in client registry. Returns
    *        the client that matches.
    ************************************************************************************/
    {
        boolean status = false;
        String username = null, password = null;
        //while user is not authenticated
        while (status == false) {
            System.out.println("Please enter your username");
            username = x.next();
            
            System.out.println("Please enter your password");
            password = x.next();
            //for each client objects 
            for (String y : clientreg) {
                Kuruvilla2ClientInterface temp = null;
                try {
                    temp = (Kuruvilla2ClientInterface) c.lookup(y);
                    
                    //if client username and password matches user's
                    if (temp.login(username, password) == true) {
                        status = true;
                        System.out.println(temp.getName() + " logged in");
                        //returns the valid client
                        return temp;
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error during authentication:"
                            + " " + e.getMessage());
                }
            }
            System.out.println("Wrong username/password");
        }
        return null;
    }
    
    public static void menu(String[] coinreg, Kuruvilla2ClientInterface client, Registry c, Scanner x) 
    /***********************************************************************************
    *Purpose: To select the right method among the list to allow user to
    *         view coins in market, check status of client, buy coins from 
    *         market, sell coins from holdings, view current coins and to
    *         quit after saving file.
    *
    *Parameters: String array holding coin object names, client, coin registry,
    *            and Scanner object.
    *
    *Action: Selects the user choice of methods to interact with client
    ************************************************************************************/
    {
        System.out.println("Please select from the following options:\n"
                + "1: browse coins, 2: current buying power, 3: buycoins, "
                + "4:  sellcoins, 5:view inventory " + "6: quit");
        
        int choice = x.nextInt();
        while (choice != 0) {
            switch (choice) {
                case 1:
                    browsecoin(coinreg, c);
                    break;
                case 2:
                    status(client);                    
                    break;
                case 3:
                    buy(x, coinreg, client, c);                    
                    break;
                case 4:
                    sell(x, coinreg, client, c);
                    break;
                case 5:
                    currentcoins(client);
                    break;
                
                case 6:
                    quit(client);
                    System.out.println("Client has saved and is now exiting.");
                    System.exit(0);
                    break;
            }
            
            System.out.println("Please select from the following options:\n"
                    + "1: browse coins, 2: current buying power, 3: buycoins, "
                    + "4:  sellcoins, 5:view inventory " + "6: quit");
            choice = x.nextInt();
            
        }
        
    }
    
    public static void browsecoin(String[] coinreg, Registry c) 
    /***********************************************************************************
    *Purpose: To view the current coins in server's market
    *
    *Parameters: String array holding coin object names, and coin registry.
    *
    *Action: Prints each coin's name, quantity and price found in the coin 
    *        regstry 
    ************************************************************************************/
    {
        try 
        {   //get updated coins in coin registry
            coinreg = c.list();
        } 
        catch (RemoteException ex) 
        {
            System.out.println(ex.getMessage());
        }
        
        Kuruvilla2CryptocoinInterface coin = null;
        for (String y : coinreg) 
        {
            try 
            {
                //for each coin in array lookup in coin registry 
                coin = (Kuruvilla2CryptocoinInterface) c.lookup(y);
                System.out.println(coin.print());
            } 
            catch (Exception e) 
            {
                System.out.println("Error during browsing:"
                        + " " + e.getMessage());
            }
            
        }
        
    }
    
    public static void status(Kuruvilla2ClientInterface c) 
    /***********************************************************************************
    *Purpose: To view the client's current buying power
    *
    *Parameters: Client
    *
    *Action: Prints the client's current buying power
    ************************************************************************************/
    {
        try 
        {
            System.out.println(c.status());
        } catch (RemoteException e) {
            System.out.println("Failed to print status " + e.getMessage());
        }
    }
    
    public static synchronized void buy(Scanner x, String[] coinreg, Kuruvilla2ClientInterface client, Registry c)
    /***********************************************************************************
    *Purpose: To buy the user desiderd coin and amount from the server
    *
    *Parameters: Scanner object, String array holding coin object names, client
    *            and coin registry.
    * 
    *Action: Get's the user's coin name and quantity and verifies its 
    *        availability on the sever. The client's holdings and coin registry
    *        is updated after the transaction.
    ************************************************************************************/        
    {
        System.out.println("Please enter the abbreviated "
                + "coin name you wish to purchase");
        Kuruvilla2CryptocoinInterface coin = null;
        String coinname = x.next();
        
        //for each coin name in string array
        for (String y : coinreg) {
            try {
                
                coin = (Kuruvilla2CryptocoinInterface) c.lookup(y);
                //find the correct coin in coin registry
                if (coin.getAbvname().equalsIgnoreCase(coinname)) {                    
                    System.out.println("buying " + coin.print());
                    
                    System.out.println("Please enter purchase amount");
                    int buy = x.nextInt();
                    
                    //update coin in coin registry
                    coin.setQuantity(coin.getQuantity() - buy);
                    coin.setMarketcap();
                    coin.setTradingvolume(buy);
                    
                    //update client's power 
                    client.setPower(client.getPower() - (buy * coin.getOpeningprice()));
                    Kuruvilla2CryptocoinInterface newc = null;
                    //create new coin that in the quantity just purchased
                    newc = coin.buy(newc, coin, buy);
                    
                    //add it to client's holdings
                    client.addtoarraylist(newc);
                    
                    System.out.println("Purchase successfull");
                    return;
                }
                
            } catch (Exception e) {
                System.out.println("Error during buying:"
                        + " " + e.getMessage());
            }
            
        }
        System.out.println("Coin does not exist or abvname is wrong");
        
    }
    
    public static synchronized void sell(Scanner x, String[] coinreg, Kuruvilla2ClientInterface client, Registry c)
    /***********************************************************************************
    *Purpose: To sell the user desiderd coin and amount to the server
    *
    *Parameters: Scanner object, String array holding coin object names, client
    *            and coin registry.
    * 
    *Action: Get's the user's coin name and quantity and verifies its 
    *        availability in the client holdings. The client's holdings and coin 
    *        registry is updated after the transaction.
    ************************************************************************************/         
    {
        Kuruvilla2CryptocoinInterface coin = null;
        try {
            //load client's holdings
            ArrayList<Kuruvilla2CryptocoinInterface> xy = client.getCurrentcoins();
            
            //check to see if holdings is empty
            if (xy.isEmpty() == true) {
                System.out.println("You currently do not have any coins");
                return;
                
            }
            
            //get input from user
            System.out.println("Enter abvname of coin you want to sell");
            String coinname = x.next();
            
            System.out.println("How many coins would you like to sell?");
            int amount = x.nextInt();
            
            //for each coin in the holding arraylist
            for (Kuruvilla2CryptocoinInterface s : xy) {
                //check to see if coin exists in the arraylist
                if (s.getAbvname().equalsIgnoreCase(coinname)) {
                    //check to see if enough quantity to sell
                    if (amount <= s.getQuantity()) {
                        //update holding arraylist
                        s.setQuantity(s.getQuantity() - amount);
                        
                        //load all coins from coin regsitry and go through each
                        for (String y : coinreg) {
                            
                            coin = (Kuruvilla2CryptocoinInterface) c.lookup(y);
                            //if correct coin found
                            if (coin.getAbvname().equalsIgnoreCase(coinname)) {
                                
                                //update coin status
                                coin.setQuantity(coin.getQuantity() + amount);
                                coin.setMarketcap();
                                coin.setTradingvolume(amount);
                                double sellprice = coin.getOpeningprice() * amount;
                                
                                //update client status
                                client.setPower(client.getPower() + sellprice);
                                
                                //if no more coin quantity for client
                                //then remove coin from holdings.
                                if(amount==s.getQuantity())
                                    client.removearraylist(s);
                                
                                System.out.println("Sold sucessfully");
                                return;
                            }
                        }
                        
                    }
                    System.out.println("Insufficient quantity");                    
                }
                
            }
            
            System.out.println("Incorrect/Invalid coinname");
            
        } catch (Exception e)
        {
         System.out.println(e.getMessage());   
        }
    }
    
    public static void currentcoins(Kuruvilla2ClientInterface client) 
    /***********************************************************************************
    *Purpose: To view the client's current holdings.
    *
    *Parameters: Client
    *
    *Action: Prints the client's current coin names and their quantity 
    ************************************************************************************/
    {        
        
        try {
            //get a list of all holdings
            ArrayList<Kuruvilla2CryptocoinInterface> xd = client.getCurrentcoins();
            
            //check to see if list is empty.
            if (xd.isEmpty() == true) {
                System.out.println("You currently do not have any coins");
                return;
                
            }
            System.out.println("Current coins held are");            
            
            //go through holdings and print coin status
            for (Kuruvilla2CryptocoinInterface x : xd) {
                System.out.println(x.getAbvname() + " quantity:"
                        + "" + x.getQuantity());
                
            }
        } catch (RemoteException e) 
        {
         System.out.println(e.getMessage());   
        }
        
    }
    
    public static void quit(Kuruvilla2ClientInterface client) 
    /***********************************************************************************
    *Purpose: To save the client's current status and coin holdings
    *
    *Parameters: Client
    *
    *Action: Serializes the clients before exiting client.
    ************************************************************************************/
    {
        try {
            //invoke client serialization function
            client.save();
        } catch (RemoteException e) 
        {
            System.out.println("Error: failed to save."
                    + " " + e.getLocalizedMessage());
        }
    }
}
