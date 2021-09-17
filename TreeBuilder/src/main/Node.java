package main;

import java.util.HashMap;

/**
 * Décrit les noeuds d'une base de données graphe 
 * @author Benjamin Bardy
 *
 */
public class Node {
	private long id;
	private String type;
	private HashMap<String, Object> args;
	
	public Node(long id, String type, HashMap<String, Object> args) {
		this.id = id;
		this.type = type;
		this.args = args;
	}

	public long getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public HashMap<String, Object> getArgs() {
		return args;
	}
	
	
}
