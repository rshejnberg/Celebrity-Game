package Server;

import java.io.IOException;

/**
 * Helper class for displaying and formatting strings
 * @author raphaelshejnberg
 *
 */
public class Display {
	Client client;
	String nl;
	/**
	 * Constructor
	 * @param client
	 */
	public Display(Client client) {
		this.client = client;
		this.nl = System.getProperty("line.separator");
	}
	
	public Client getClient() {
		return this.client;
	}
	/**
	 * Loops asking user for answer until it receives 'yes' or 'no'
	 * @return
	 * @throws IOException
	 */
	public String getYesNo() {
		boolean invalid = true;
		String input = null;
		while(invalid) {
			input = client.getInput().toLowerCase().trim();
			if(input.equals("yes") || input.equals("no"))
				invalid = false;
			else
				client.write("Invalid input. Please enter 'yes' or 'no'.\n");
		}
		return input;
	}
	/**
	 * Prints the content of a node
	 * @param tmp
	 * @throws NullPointerException 
	 * @throws IOException 
	 */
	public void printNode(Node tmp) throws NullPointerException, IOException {
		if(tmp == null)
			throw new NullPointerException("Node cannot be null.");
		if(DTree.getInstance().isLeaf(tmp))
			printAnswer(tmp.content);
		else
			printQuestion(tmp.content);
	}
	/**
	 * Prints the whole decision tree
	 * @throws IOException
	 */
	public void printTree() throws IOException {
		client.write("Contents of Decision Tree:\n________________________________\n");
		printChildren(DTree.getInstance().getRoot(), 0);
	}
	/**
	 * Prints the children of a given node recursively
	 * @param ptr
	 * @param numIndents
	 * @throws IOException
	 */
	private void printChildren(Node ptr, int numIndents) throws IOException {
		if(DTree.getInstance().isLeaf(ptr)) 
			printAnswerVerbose(ptr.content, DTree.getInstance().isLeftChild(ptr), numIndents);	
		
		else 
		{
			printQuestionVerbose(ptr.content, numIndents);
			if(ptr.left != null) 
				printChildren(ptr.left, numIndents+1);
			if(ptr.right != null) 
				printChildren(ptr.right, numIndents+1);
		}
	}
	/**
	 * Gets a String of a specified number of tab characters
	 * @param numIndents
	 * @return indents, String of tab characters
	 */
	public String getIndents(int numIndents) {
		String indents = "";
		for(int i=0; i<numIndents; i++)
			indents += "\t";
		return indents;
	}
	/**
	 * Converts a boolean to a string 
	 * @param input
	 * @return String, value of 'yes' or 'no'
	 */
	public String boolToYesNo(boolean input) {
		if(input)
			return "yes";
		else
			return "no";
	}
	/**
	 * Converts a string to a boolean
	 * @param yesOrNo
	 * @return boolean value
	 */
	public boolean StringToBool(String yesOrNo) {
		if(yesOrNo.toLowerCase().equals("yes"))
			return true;
		else
			return false;
	}
	/**
	 * Capitalizes the first character of every word in a string
	 * @param str
	 * @return capStr
	 */
	public String capitalize(String str) {
		String[] words = str.split(" ");
		String capStr = "";
		for(int i = 0; i<words.length; i++) {
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
			capStr += words[i] + " ";
		}
		return capStr;
	}
	/**
	 * Printing method used when printing the whole tree.
	 * @param answer
	 * @param onYesPath
	 * @param numIndents
	 * @throws IOException
	 */
	public void printAnswerVerbose(String answer, boolean onYesPath, int numIndents) throws IOException {
		String msg = getIndents(numIndents);
		msg += capitalize(boolToYesNo(onYesPath)) + "? Is your celebrity " + answer; 
		
		client.write(msg + nl);
	}
	/**
	 * Printing method used when printing whole tree.
	 * @param question
	 * @param numIndents
	 * @throws IOException
	 */
	public void printQuestionVerbose(String question, int numIndents) throws IOException {
		client.write(getIndents(numIndents) + question + nl);
	}
	/**
	 * Printing method used when playing the game.
	 * @param answer
	 * @throws IOException
	 */
	public void printAnswer(String answer) throws IOException {
		client.write("Is your celebrity " + answer + nl);
	}
	/**
	 * Printing method used when playing the game.
	 * @param question
	 * @throws IOException
	 */
	public void printQuestion(String question) throws IOException {
		client.write(question + nl);
	}
	/**
	 * Outputs a message to the clients socket
	 * @param msg
	 * @throws IOException
	 */
	public void writeToClient(String msg) throws IOException {
		client.write(msg + nl);
	}
}
