package main;

import java.util.HashMap;

/**
 * Décrit les relations d'une base de données graphe 
 * @author Benjamin Bardy
 *
 */
public class Relationship {
	private long id;
	private String type;
	private Node nodeFrom, nodeTo;
	private HashMap<String, Object> args;
	
	public Relationship(long id, String type, Node nodeFrom, Node nodeTo, HashMap<String, Object> args) {
		this.id = id;
		this.type = type;
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
		this.args = args;
	}

	public long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public Node getNodeFrom() {
		return nodeFrom;
	}

	public Node getNodeTo() {
		return nodeTo;
	}

	public HashMap<String, Object> getArgs() {
		return args;
	}
	
	
}
