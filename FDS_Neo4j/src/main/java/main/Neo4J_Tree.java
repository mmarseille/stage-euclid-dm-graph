package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

public class Neo4J_Tree implements AutoCloseable{
	public static int batchSize = 50000;
	private int N;
	private final String neo4JPath = "/home/benjamin/.config/Neo4j Desktop/Application/relate-data/dbmss/";
	private String dbID = "dbms-4b2d65a6-2f05-449b-a388-720fe839cc86";
	private final Driver driver;
	private BranchIter root;
	
	public Neo4J_Tree(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	public double createTree(int N, int max_depth, int max_children) {
		this.N = N;
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
			session.run("DROP CONSTRAINT uniq_id");
			if(N <= batchSize) {
				session.run("MATCH (n) DETACH DELETE n");
			} else {				
				for(int i=0; i<N/batchSize; i++) {
					System.out.println(String.format("%d/%d", i*batchSize,N));
					session.run("MATCH (n) "+
							"WITH n LIMIT $batch "+
							"DETACH DELETE n", parameters("batch",batchSize));
				}
				System.out.println(String.format("%d/%d", N,N));
			}
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
