package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public class Branch {
	private final Driver driver;
	
	private int depth = 0;

	private static int nb_nodes = 0;
	private static int node_id = 0;
	private static List<Float> paramNode, paramRel;
	
	private static int N;
	private static int max_depth;
	private static int max_children;
	
	private static String script = "";

	private String nodename;
	private Branch parent = null;
	private List<Branch> children = new ArrayList<>();

	public void setDepth(int depth) {
		this.depth = depth;
	}


	public Branch(Driver driver, int N, int max_depth, int max_children) {
		Branch.N = N;
		Branch.max_children = max_children;
		Branch.max_depth = max_depth;
		
		this.driver = driver;
		this.nodename = String.format("n%s", node_id);
		
		Float[] data = new Float[80];
		Arrays.fill(data,Float.valueOf(0));
		paramNode = Arrays.asList(data);
		
		Float[] dataRel = new Float[10];
		Arrays.fill(data,Float.valueOf(0));
		paramRel = Arrays.asList(dataRel);
		
		Branch.script += String.format("CREATE (%s:Node{name: '%s', param:%s})\n", this.nodename, this.nodename, paramNode);
				
		node_id++;
		nb_nodes++;
	}
		
	
	public Branch(Driver driver, Branch parent) {
		this.driver = driver;
		this.parent = parent;
		this.nodename = String.format("n%s", node_id);
		
		Branch.script += String.format("CREATE (%s:Node{name: '%s', param:%s})\n", this.nodename, this.nodename, paramNode);
	
		node_id++;
	}
	
	public double createTree() {
		double start, end;
		
		//Génération script arbre
		start = System.currentTimeMillis();
		addChildren(depth);
		script += ";";
		end = System.currentTimeMillis()-start;
		System.out.println(String.format("ARBRE: %.3fs",(end/1000)));
		
		//Affichage du script généré
		//System.out.println(script);
		
		//Génération arbre
		start = System.currentTimeMillis();
		executeScript();
		end = System.currentTimeMillis()-start;
		System.out.println(String.format("SCRIPT: %.3fs",(end/1000)));
		
		return end/1000;
	}
	
	public void resetTree() {
		script = "";
		node_id = 0;
		nb_nodes = 0;
	}
	
	/*public void addChild(int nb) {
		nb_nodes += nb;
		
		try( Session session = driver.session() ){
			for (int i=0; i<nb; i++) {
				Branch child = new Branch(driver, parent, String.format("n%s",nb_nodes-nb+i+1));
				parent.children.add(child);
				
				session.writeTransaction(new TransactionWork<Void>() {

					@Override
					public Void execute(Transaction tx) {
						int nb_children = parent.children.size();
						Branch last_child = parent.children.get(nb_children-1);
						System.out.println(Branch.this.nodename+"  "+last_child.nodename);
						//tx.run(String.format("CREATE (%s)-[:CHILD_OF]->(%s)", last_child.nodename, Branch.this.nodename));
						tx.run("MATCH (n1:Node) WHERE n1.name = $name1 "+
								"MATCH (n2:Node) WHERE n2.name = $name2 "+
								"CREATE (n1)-[:CHILD_OF]->(n2)", parameters("name1",last_child.nodename, "name2", Branch.this.nodename));
						return null;
					}
					
				});
			}
		}
	}*/
	
	/*public void addChildren(int nb) {
		
		try( Session session = driver.session() ){
			
			for (int i=0; i<nb; i++) {
				Branch child = new Branch(driver);
				children.add(child);
				
				session.writeTransaction(new TransactionWork<Void>() {

					@Override
					public Void execute(Transaction tx) {
						int nb_children = Branch.this.children.size();
						Branch last_child = Branch.this.children.get(nb_children-1);
						//tx.run(String.format("CREATE (%s)-[:CHILD_OF]->(%s)", last_child.nodename, Branch.this.nodename));
						tx.run("MATCH (n1:Node) WHERE n1.name = $name1 "+
								"MATCH (n2:Node) WHERE n2.name = $name2 "+
								"CREATE (n1)-[:CHILD_OF]->(n2)", parameters("name1",last_child.nodename, "name2", Branch.this.nodename));
						return null;
					}
				});
			}
			
		}
	}*/
	
	private void addChildren(int depth) {
		if (nb_nodes == N || depth >= (int) (max_depth+new Random().nextGaussian())) return;

		int child_max = (depth == 0)? 20: max_children;
		double random = (depth == 0)? 1 : Math.random();
		
		//Arêtes random
		int nb = (int) (1+ (random * Math.min(child_max-1, N-nb_nodes-1)));	

		//Arêtes max
		//int nb = 1 + Math.min(child_max-1, N-nb_nodes-1);
		
		nb_nodes += nb;

//		System.out.println("DEPTH: "+depth);
//		System.out.println("NB_NODES: "+nb_nodes);
		
		depth++;
		
		for (int i=0; i<nb; i++) {
			Branch child = new Branch(driver, this);
			child.setDepth(depth);
			children.add(child);
			
			script += String.format("CREATE (%s)-[:CHILD_OF{param:%s}]->(%s)\n",child.nodename, paramRel, nodename);
			
			child.addChildren(depth);
		}
		

		
	}
	
	
	public void executeScript() {
		try(Session session = driver.session()){
			session.run(script);
		}
	}

}
