package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import static org.neo4j.driver.Values.parameters;

import java.time.Duration;
import java.time.Instant;

public class HelloWorld implements AutoCloseable{
	
	private final Driver driver;
	
	public HelloWorld(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	
	public void close() throws Exception {
		driver.close();		
	}
	
	public void printGreeting(final String message) {
		try ( Session session = driver.session() ){
			Instant start = Instant.now();
			for (int i=0; i<1000; i++) {
				session.writeTransaction(new TransactionWork<String>() {

					@Override
					public String execute(Transaction tx) {
						Result result = tx.run("CREATE (a:Greeting) " +
									"SET a.message = $message " +
									"RETURN a.message + ', from node ' + id(a)",
								parameters("message",message)
								);
						return result.single().get(0).asString();
					}
				
				
				});
			}
			Instant end = Instant.now();
			Duration delta = Duration.between(start, end);
			System.out.println("PG: "+delta.getNano());
		}
	}
	
	public void printGreeting2(final String message) {
		try ( 	Session session = driver.session();  
				Transaction tx = session.beginTransaction()){
					Instant start = Instant.now();
					for (int i=0; i<1000; i++) {
						tx.run("CREATE (a:Greeting) " +
								"SET a.message = $message " +
								"RETURN a.message + ', from node ' + id(a)",
							parameters("message",message)
							);
					}
				tx.commit();
				Instant end = Instant.now();
				Duration delta = Duration.between(start, end);
				System.out.println("PG2: "+delta.getNano());
			}
	}
		
	
	public void deleteNodes() {
		try( Session session = driver.session() ){
			session.writeTransaction(new TransactionWork<Void>() {

				@Override
				public Void execute(Transaction tx) {
					tx.run("MATCH (n) DETACH DELETE n");
					return null;
				}
			});
		}
	}

}


