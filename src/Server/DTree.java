package Server;

import java.io.*;
import java.util.Vector;

/**
 * 
 * Decision tree class for representing the data obtained from players of the game
 * @author raphaelshejnberg
 *
 */
public class DTree implements Serializable {
	static final long serialVersionUID = -7588980448693010399L;
	static DTree instance;
	private static String objFileName = "DTree.ser";
	private static Vector<Node> vertices;
	/**
	 * Default constructor
	 */
	private DTree(Node root) {

		vertices = new Vector<Node>();
		setRoot(root);
	}
	/**
	 * Singleton instance retrieval method
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static DTree getInstance() {
		if(instance == null) {
			try {
				File DTFile = new File(objFileName);
				if(DTFile.exists() && !DTFile.isDirectory()) {
					if(!readFromFile())
						generateTree();
					if(DTree.getRoot() == null)
						throw new IOException("Root is nulL!!!");
				}
				else {
					generateTree();
				}
			} catch( ClassNotFoundException | IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return instance;
			
	}
	private static void generateTree() throws ClassNotFoundException, IOException {
		
		Node n = new Node();
		n.content = "Barrack Obama";
		instance = new DTree(n);
		
		writeToFile();
	}
	/**
	 * Writes the DTree instance to an object file.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static boolean writeToFile() {
		try {
    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(objFileName));
    	
    	oos.writeObject(instance);
    	oos.writeObject(instance.vertices);
    	for(int i=0 ;i<vertices.size(); i++) 
    		oos.writeObject(instance.vertices.get(i).content);
    	oos.flush();
    	oos.close();
		} catch (IOException e) {
			System.err.println("IOException occured while writing tree to file.");
			e.printStackTrace();
			return false;
		} finally {
			return true;
		}
		
    }
	/**
	 * Reads the DTree instance from an object file.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static boolean readFromFile() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(objFileName));
			instance = (DTree) ois.readObject();
			instance.vertices = (Vector<Node>) ois.readObject();
			for(int i=0 ;i<vertices.size(); i++) 
	    		instance.vertices.get(i).content = (String) ois.readObject();

			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("IOException occured while reading tree from file.");
			e.printStackTrace();
			return false;
		} 
		return true;
		
	}
	
	
	private void addNode(Node n) {
		if(!vertices.contains(n))
			vertices.add(n);
	}
	
	/**
	 * Finds a node in the tree based a specified attribute and argument
	 * @param method
	 * @param arg
	 * @return
	 */
	/*public Node[] findNodeBy(String method, String arg) {
		switch(method) {
			case "user":
				return findNodeByUser(arg.toLowerCase(), getRoot(), new Vector<Node>());
			case "celebrity":
				return findNodeByCelebrity(arg.toLowerCase(), getRoot(), new Vector<Node>());				
			default:
				break;
		}
		return null;
	}*/
	/**
	 * Recursively traverses the tree and builds a list of nodes with a certain creator
	 * @param username
	 * @param ptr
	 * @param matches
	 * @return
	 */
	/*public Node[] findNodeByUser(String username, Node ptr, Vector<Node> matches) {
			
			if(ptr.creator.toLowerCase().equals(username) && !matches.contains(ptr))
				matches.add(ptr);
			if(ptr.left != null) 
				findNodeByUser(username, ptr.left, matches);
			if(ptr.right != null) 
				findNodeByUser(username, ptr.right, matches);
			
			return (Node[]) matches.toArray();
	}*/
	/**
	 * Recursively traverses the tree and builds a list of nodes that have a certain celebrity 
	 * @param celeb
	 * @param ptr
	 * @param matches
	 * @return
	 */
	/*
	public Node[] findNodeByCelebrity(String celeb, Node ptr, Vector<Node> matches) {
		
		if(ptr.content.toLowerCase().equals(celeb) && !matches.contains(ptr))
			matches.add(ptr);
		if(ptr.left != null) 
			findNodeByUser(celeb, ptr.left, matches);
		if(ptr.right != null) 
			findNodeByUser(celeb, ptr.right, matches);
		
		return (Node[]) matches.toArray();
}*/
	/**
	 * Checks to see if the node provided is a leaf node
	 * @param node
	 * @return
	 */
	public boolean isLeaf(Node node) {
		if(node.left == null && node.right == null)
			return true;
		else
			return false;
	}
	/**
	 * Checks to see if the node provided is the left child 
	 * @param child
	 * @return
	 */
	public boolean isLeftChild(Node child) {
		if(child == getRoot())
			return false;
		if(child.parent.left != null && child.parent.left == child)
			return true;
		else
			return false;
	}
	/**
	 * Returns the left child of the node supplied
	 * @param pos
	 * @return
	 */
	public Node goLeft(Node pos) {
		return pos.left;
	}
	/**
	 * Returns the right child of the node supplied
	 * @param pos
	 * @return
	 */
	public Node goRight(Node pos) {
		return pos.right;
	}
	/**
	 * Shifts nodes upon the entry of a new celebrity
	 * @param currentPos
	 * @param parent
	 * @param lChild
	 * @param rChild
	 */
	private void shiftNodes(Node currentPos, Node parent, Node lChild, Node rChild) {
		if(currentPos != getRoot()) {
			if(isLeftChild(currentPos))
				currentPos.parent.left = parent;
			else 
				currentPos.parent.right = parent;
		}
		else 
			setRoot(parent);
		lChild.parent = parent;
		rChild.parent = parent;
		parent.left = lChild;
		parent.right = rChild;
	}
	/**
	 * Adds celebrities to the tree and locks the current area
	 * @param username
	 * @param pos
	 * @param celebName
	 * @param celebQuestion
	 * @param isQuestionTrueForNewCeleb
	 */
	public void addCelebrity(Client c, Node pos, String celebName, String celebQuestion, boolean isQuestionTrueForNewCeleb) {
		
		
			Node answerNode = new Node();
			Node questionNode = new Node();
		
			answerNode.content = celebName;
			questionNode.content = celebQuestion;
			
			answerNode.creatorAddress = c.address;
			questionNode.creatorAddress = c.address;
			//Assign children of questionNode
			if(isQuestionTrueForNewCeleb) 
				shiftNodes(pos, questionNode, answerNode, pos);
			else 
				shiftNodes(pos, questionNode, pos, answerNode);
			
			addNode(answerNode);
			addNode(questionNode);
			writeToFile();
		}
	/**
	 * Gets a list of vertices
	 * @return
	 */
	public static Vector<Node> getNodes() {
		return vertices;
	}
	/**
	 * Gets the root node of the tree
	 * @return
	 */
	public static Node getRoot() {
		return getNodes().get(0);
	}
	/**
	 * Sets the root node of the tree
	 * @param root
	 */
	private static void setRoot(Node root) {
		vertices.add(0, root);
	}

}

