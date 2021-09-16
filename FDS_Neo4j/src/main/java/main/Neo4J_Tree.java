package main;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import static org.neo4j.driver.Values.parameters;

/**
 * Gère la création et la suppression de l'arbre en Neo4J
 * 
 * @author Benjamin Bardy
 *
 */
public class Neo4J_Tree implements AutoCloseable{
	public static int batchSize = 50000;
	private int N;
	private final Driver driver;
	private BranchIter root;
	
	/**
	 * Constructeur faisant le lien avec la base de données Neo4J
	 * 
	 * @param uri l'URI de la base
	 * @param user le nom de l'utilisateur
	 * @param password le mot de passe
	 */
	public Neo4J_Tree(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	/**
	 * Crée l'arbre dans la base de données Neo4J
	 * 
	 * @param N le nombre de noeuds de l'arbre
	 * @param max_depth la profondeur maximale de l'arbre
	 * @param max_children le nombre d'enfants maximal pour chaque noeud
	 * @return la durée totale de création de l'arbre
	 */
	public double createTree(int N, int max_depth, int max_children) {
		this.N = N;
		root = new BranchIter(driver, N, max_depth, max_children);
		return root.createTree();
	}
	
	/**
	 * Supprime l'arbre dans la base de données Neo4J
	 * @return la durée de suppression de l'arbre
	 */
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
	
	/**
	 * Ferme la connection avec la base de données Neo4J
	 */
	@Override
	public void close() throws Exception {
		driver.close();
	}

	
}
