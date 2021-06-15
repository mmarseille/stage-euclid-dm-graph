package main;

import static org.neo4j.driver.Values.parameters;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class BranchIter {
	private final Driver driver;
	
	private static int nb_nodes = 0;
	private static int node_id = 0;
	private int depth;
	
	private static int N;
	private static int max_depth;
	private static int max_children;
	
	private static String script = "";

	private String nodename;
	private BranchIter parent = null;
	private List<BranchIter> children = new ArrayList<>();

	
	public BranchIter(Driver driver, int N, int max_depth, int max_children) {
		BranchIter.N = N;
		BranchIter.max_children = max_children;
		BranchIter.max_depth = max_depth;
		
		this.driver = driver;
		this.nodename = String.format("n%s", node_id);
		this.depth = 0;
		
		BranchIter.script += String.format("CREATE (%s:Node{name: '%s'})\n", this.nodename, this.nodename);
				
		node_id++;
		nb_nodes++;
	}
		
	
	public BranchIter(Driver driver, BranchIter parent) {
		this.driver = driver;
		this.parent = parent;
		this.nodename = String.format("n%s", node_id);
		this.depth = parent.depth + 1;

		BranchIter.script += String.format("CREATE (%s:Node{name: '%s'})\n", this.nodename, this.nodename);
	
		node_id++;
	}
	
	public double createTree() {
		double start, end;
		
		//Génération script arbre
		start = System.currentTimeMillis();
		addChildren();
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
		
		return end;
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
	
	private void addChildren() {		
		Stack<BranchIter> branches = new Stack<BranchIter>();
		branches.add(this);
		
		while (!branches.empty()) {
			BranchIter currentNode = branches.pop();
			
			if (nb_nodes == N || currentNode.depth >= (int) (max_depth+new Random().nextGaussian())) {		
				continue;
			}


			int child_max = (currentNode.depth == 0)? Math.max(nb_nodes, 20) : max_children;
			double random = (currentNode.depth == 0)? 1 : Math.random();
			
			//Arêtes random
			int nb = (int) (1+ (random * Math.min(child_max-1, N-nb_nodes-1)));	
	
			//Arêtes max
			//int nb = 1 + Math.min(child_max-1, N-nb_nodes-1);
			
			nb_nodes += nb;
	
	//		System.out.println("DEPTH: "+depth);
	//		System.out.println("NB_NODES: "+nb_nodes);
						
			for (int i=0; i<nb; i++) {
				BranchIter child = new BranchIter(driver, currentNode);
				children.add(child);
				
				script += String.format("CREATE (%s)-[:CHILD_OF]->(%s)\n",child.nodename, currentNode.nodename);
				
				branches.push(child);
			}
			
		}
		
	}
	
	
	public void executeScript() {
		try(Session session = driver.session()){
			session.writeTransaction(new TransactionWork<Void>() {

				@Override
				public Void execute(Transaction tx) {
					tx.run(script);
					return null;
				}

			});
		}
	}

}
