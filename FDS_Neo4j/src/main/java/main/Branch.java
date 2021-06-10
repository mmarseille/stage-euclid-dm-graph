package main;

import java.util.ArrayList;
import java.util.List;

public class Branch {
	private Branch parent = null;
	private List<Branch> children = new ArrayList<>();
	private int max_children = 3;
	private int max_depth = 10;
	private String nodename;
	private static int depth = 0;
	private final int N = 10;
	private static int nb_nodes = 0;
	private static int node_id = 0;
	
	public Branch() {
		this.nodename = String.format("n%s", node_id);
		System.out.println(String.format("CREATE (%s:Node)",this.nodename));
		node_id++;
	}
	
	public Branch(Branch parent, String nodeName) {
		this.parent = parent;
		this.nodename = String.format("n%s", node_id);
		System.out.println(String.format("CREATE (%s:Node)",this.nodename));
		node_id++;
	}
	
	public void createTree() {
		addChildren();
	}
	
	private void addChildren() {
		if (nb_nodes == N || depth == max_depth) return;		
		
		int nb = (int) (1+(Math.random() * Math.min(max_children, N-nb_nodes)));
		
		nb_nodes += nb;

		System.out.println("DEPTH: "+depth);
		System.out.println("NB_NODES: "+nb_nodes);
		
		depth++;
		
		for (int i=0; i<nb; i++) {
			Branch child = new Branch(parent, String.format("n%s",nb_nodes-nb+i+1));
			children.add(child);
			
			System.out.println(String.format("CREATE (%s)-[:CHILD_OF]->(%s)", child.nodename, this.nodename));
			
			child.addChildren();
		}
		
	}
	

}
