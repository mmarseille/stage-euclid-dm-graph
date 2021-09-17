package main;

/**
 * Sert de lien direct avec la base de données graphe
 * @author Benjamin Bardy
 *
 */
public interface DBWrapper {
	public void execQuery(String query);
	public void execBatchQuery(String query, int batchSize);
}
