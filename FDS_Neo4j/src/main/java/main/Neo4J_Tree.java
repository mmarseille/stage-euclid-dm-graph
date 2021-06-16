package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Neo4J_Tree implements AutoCloseable{
	private final Driver driver;
	private BranchIter root;
	
	public Neo4J_Tree(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	public double createTree(int N, int max_depth, int max_children) {
		root = new BranchIter(driver, N, max_depth, max_children);
		return root.createTree();
	}
	
	public double createTree(int N, int max_depth, int max_children, boolean iter) {
		if (iter) {
			BranchIter root = new BranchIter(driver, N, max_depth, max_children);
			return root.createTree();
		} else {
			Branch root = new Branch(driver, N, max_depth, max_children);			
			return root.createTree();
		}
	}
	
	public double deleteTree() {
		double start, end;
		
		start = System.currentTimeMillis();
		try (Session session = driver.session()){
			session.run("MATCH (n) DETACH DELETE n");
		}
		end = System.currentTimeMillis() - start;
		System.out.println(String.format("TEMPS SUPPRESSION: %.3fs",(end/1000)));
		
		root.resetTree();
		
		return end/1000;
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	
}
