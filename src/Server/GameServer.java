package Server;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;


public class GameServer implements Runnable {

	static Vector<Client> clients;
    static private ServerSocket serverSocket;
    static GameServer serverKernel;
    boolean running;
    /**
     * Default constructor used for initializing new threads.
     * @see Client
     */
    public GameServer() {}
    /**
     * Constructor for initializing server
     * @param port
     * @throws IOException
     */
    public GameServer(int port) throws IOException {
    	if(serverSocket == null)
				serverSocket = new ServerSocket(port);
    	if(clients == null)
    		clients = new Vector<>();
    }
    /**
     * Closes the server socket.
     */
    private void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println("IOException occured while closing server socket.");
            }
        }
    }
    /**
     * Gets a list of clients connected
     * @return
     */
    public Vector<Client> getClients() {
    	return this.clients;
    }
    /**
     * Accepts connections in a loop
     * @throws IOException
     */
    public void acceptConnections() {

		System.out.println("Waiting for client...");
		running = true;
    	while(running) 
    	{	
			try {
				Client newClient;
				newClient = new Client(serverSocket.accept());
				clients.add(newClient);
	    		clients.lastElement().startThread();
			} catch (IOException e) {
				System.err.println("IOException occured while accepting a connection.");
			}
    		System.out.println("Connection accepted!");
    	}
    }
    /**
     * Thread method
     */
    public void run() {
    	Client newClient = clients.lastElement();
    	newClient.write("What is your name?\n");
    	
    	/*
    	boolean validName = true;
    	do {
    		tmp = newClient.getInput();
    		validName = true;
    		for(int i=0; i<clients.size(); i++) { 
    			Client c = clients.get(i);
    			if(c.name != null && c.name.equals(tmp) && c.loggedIn) {
    				newClient.write("Username already taken please choose something else.\n");
    				validName = false;
    				break;
    			}
    			else if (c.name != null && c.name.equals(tmp)) {
    				newClient.write("Logging in...\n");
    				c.resetSocket(newClient.socket);
    				clients.remove(newClient);
    				c.loggedIn = true;
    				newClient = c;
    			}
    		}
    	} while(!validName);
    	
    	if(newClient.name == null) 
    		newClient.setName(tmp);
		*/
    	
    	newClient.setName(newClient.getInput());
    	
    	CelebrityGame game = new CelebrityGame(newClient);
    	//   game.printTree();
    	game.menu();
    	closeConnection(newClient);

    }  
    /**
     * Closes a connection with the specified client
     * @param client
     * @throws IOException
     */
    private static void closeConnection(Client client) {
    	if(client.socket.isClosed()) {
    		System.err.println("Client socket already closed");
    		return;
    	}
    			try {
					client.socket.close();
					client.available = false;
					
				} catch (IOException e) {
					System.err.println("IOException occured while closing a connection.");
					e.printStackTrace();
				} finally {
    			client.endThread();
    			clients.remove(client);
				}
    }
    /**
     * Close all connections to server
     * @throws IOException
     */
    private static void closeAllConnections() {
    	for(Client c : clients)
    		closeConnection(c);
    }
    
    public void clientDisconnect(Client c) {
    	System.out.println(c.name + " has disconnected.");
    	closeConnection(c);
    	
    }

    /**
     * This method is used to send a message to another user informing them that
     * someone else was thinking of the celebrity they submitted.
     * @return author, The person to submitted the celebrity in Node n
     */
    public Client findRecipient(Node n) {
    	Client author = null;
    	for(Client c : clients)
    		if(c.address == n.creatorAddress)
    			author = c;
    	return author;
    }
    /**
     * Notifies the person who submitted a celebrity that someone else was thinking
     * of their celebrity
     * @param n
     * @param guesser
     * @throws IOException 
     */
    public void alertSubmitterAboutGuess(Node n, String guesser) throws IOException {
    	Client recipient = findRecipient(n);
    	int port;
    	if(recipient != null) 
    		recipient.write( guesser + " thought of your celebrity: " + n.content + "\n");
    }
    /**
     * Does validation checking on the supplied arguments
     * @param args
     * @return
     * @throws Exception
     */
    private static int validateArguments(String[] args) throws Exception {
    	Scanner reader = new Scanner(System.in);
    	int port = 0;
    	
		if(args.length != 1) {
    		System.err.println("Usage: GameServer <port number>");
    	}
    	else if(args.length == 1) 
    		try {
    		 port = Integer.parseInt(args[0]);
    		} catch(NumberFormatException e) {
    			System.err.println("Argument provided not an integer.");
    		}
    	
		return port;
    }
    private static void startServer(int port) {
		try {
			serverKernel = new GameServer(port);
			serverKernel.acceptConnections();
		} catch (IOException e) {
			String msg = "";
			if(e instanceof BindException)
				msg = "Port " + port + " already in use.";
			System.err.println("IOException occured while creating GameServer.\n" + msg + "\n");
		} 
    }
    /**
     * Main method
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    		System.setProperty("line.separator", "\r\n");
    		String[] args2 = new String[1];
    		args2[0] = "6001";
    		int port = validateArguments(args2);
    		if(port == 0)
    			return;
    		startServer(port);
    }
}
