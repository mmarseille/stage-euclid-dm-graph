package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4J_Tree implements AutoCloseable{
	private final Driver driver;
	
	public Neo4J_Tree(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
		
		Branch start = new Branch(driver);
		start.createTree();
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	
}
