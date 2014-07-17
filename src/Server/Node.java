package Server;

import java.io.Serializable;
import java.net.InetAddress;
/**
 * Node element of decision tree
 * @author raphaelshejnberg
 *
 */
final class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	Node parent;
	Node left;
	Node right;
	String content;
	//String creator;
	InetAddress creatorAddress;
}