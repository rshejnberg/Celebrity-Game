package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
/**
 * Class for client connections
 * @author raphaelshejnberg
 *
 */
final class Client {
	
	public InetAddress address;
	public boolean available;
	public Socket socket;
	private OutputStream out;
	private BufferedReader in;
	private Thread game;
	public String name;
	public Vector<String> messages;
	public boolean loggedIn;
	/**
	 * Constructor
	 * @param socket
	 * @throws IOException
	 */
	public Client(Socket socket) throws IOException {
		available = true;
		messages = new Vector<>();
		address = socket.getInetAddress();
		resetSocket(socket);
        game = new Thread(new GameServer());
	}
	public String getThreadId() {
		return Thread.currentThread().getName();
	}
	
	public void sendNotification(String msg) {
		messages.add(msg);
	}
	public void writeNotifications() {
		for(String msg : messages) {
			this.write(msg);
			messages.remove(msg);
		}
	}
	
	public void resetSocket(Socket socket) {
		try {
			this.socket = socket;
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = this.socket.getOutputStream();
		} catch(IOException e) {
			System.err.println("Invalid Socket");
			e.printStackTrace();
		}
	}
	/**
	 * Sets this client's name and updates the thread name.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
		game.setName("Client: " + name + "-" + game.getId());
	}
	/**
	 * Start this client's threat
	 */
	public void startThread() {
		game.start();
	}
	/**
	 * Ends the thread
	 */
	public void endThread() {
		game.interrupt();
	}
	/**
	 * 
	 * @return
	 */
	public boolean availableForRead() {
		boolean available = false;
		try {
			available = socket.getInputStream().available() > 0;
		} catch (IOException e) {
			System.err.println("IOException occurred checking for client input.");
			if(!this.socket.isClosed())
				GameServer.serverKernel.clientDisconnect(this);
		} 
		return available;
		
	}

	/**
	 * Write to this client's socket.
	 * @param content
	 * @throws IOException
	 */
	public void write(String content) {
		try {
			out.write(content.getBytes());
		} catch (IOException e) {
			System.err.println("IOException occurred while writing to client.");
			if(!this.socket.isClosed())
				GameServer.serverKernel.clientDisconnect(this);
			e.printStackTrace();
		}
	}

	/**
	 * Read from this client's socket.
	 * @return
	 * @throws IOException
	 */
	public String getInput() {
		char[] input = new char[255];
		String inputStr = "";
		int status = 0;
		try {
			boolean receivedInput = false;
			while(!receivedInput) {
				while(availableForRead()) {	
					if(in.read(input) < 0)
						throw new IOException();
					inputStr += String.copyValueOf(input);
					if(!availableForRead())
						receivedInput = true;
				}
				Thread.currentThread().sleep(100);
			}
		} catch (IOException e) {
			GameServer.serverKernel.clientDisconnect(this);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return inputStr.trim().replace("\n", "").replace("\r", "");
		
	}
}