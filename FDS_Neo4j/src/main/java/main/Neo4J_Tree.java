package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class Neo4J_Tree implements AutoCloseable{
	private final Driver driver;
	
	public Neo4J_Tree(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	public void createTree(int N, int max_depth, int max_children) {
		BranchIter start = new BranchIter(driver, N, max_depth, max_children);
		start.createTree();
	}
	
	public void createTree(int N, int max_depth, int max_children, boolean iter) {
		if (iter) {
			BranchIter start = new BranchIter(driver, N, max_depth, max_children);
			start.createTree();
		} else {
			Branch start = new Branch(driver, N, max_depth, max_children);			
			start.createTree();
		}
	}
	
	public void deleteTree() {
		try (Session session = driver.session()){
			session.writeTransaction(new TransactionWork<Void>() {

				@Override
				public Void execute(Transaction tx) {
					tx.run("MATCH (n) DETACH DELETE n");
					return null;
				}
				
			});
		}
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	
}
