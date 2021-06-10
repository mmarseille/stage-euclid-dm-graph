package main;

import static org.neo4j.driver.Values.parameters;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class Branch {
	private final Driver driver;
	private Branch parent = null;
	private List<Branch> children = new ArrayList<>();
	private int max_children = 3;
	private int max_depth = 10;
	private String nodename;
	private static int depth = 0;
	private final int N = 10;
	private static int nb_nodes = 0;
	private static int node_id = 0;
	
	public Branch(Driver driver) {
		this.driver = driver;
		this.nodename = String.format("n%s", node_id);
		
		try ( Session session = driver.session() ){
				session.writeTransaction(new TransactionWork<Void>() {

					@Override
					public Void execute(Transaction tx) {
						tx.run(String.format("CREATE (%s:Node{name: $name}) ",Branch.this.nodename), parameters("name",Branch.this.nodename));
						return null;
					}
				
				
				});
			}
		
		node_id++;
		
		}
		
	
	public Branch(Driver driver, Branch parent, String nodeName) {
		this.driver = driver;
		this.parent = parent;
		this.nodename = String.format("n%s", node_id);
		try ( Session session = driver.session() ){
			session.writeTransaction(new TransactionWork<Void>() {

				@Override
				public Void execute(Transaction tx) {
					tx.run(String.format("CREATE (%s:Node{name: $name}) ",Branch.this.nodename), parameters("name",Branch.this.nodename));
					return null;
				}
			
			
			});
		}
	
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
		
		try ( Session session = driver.session() ){
			for (int i=0; i<nb; i++) {
				Branch child = new Branch(driver, parent, String.format("n%s",nb_nodes-nb+i+1));
				children.add(child);
				
				session.writeTransaction(new TransactionWork<Void>() {

					@Override
					public Void execute(Transaction tx) {
						int nb_children = Branch.this.children.size();
						Branch last_child = Branch.this.children.get(nb_children-1);
						System.out.println("NAME:: "+last_child.nodename);
						//tx.run(String.format("CREATE (%s)-[:CHILD_OF]->(%s)", last_child.nodename, Branch.this.nodename));
						tx.run("MATCH (n1:Node) WHERE n1.name = $name1 "+
								"MATCH (n2:Node) WHERE n2.name = $name2 "+
								"CREATE (n1)-[:CHILD_OF]->(n2)", parameters("name1",last_child.nodename, "name2", Branch.this.nodename));
						return null;
					}
				
				
				});
				node_id++;
				child.addChildren();
			}
		
		}
		
	}
	

}
