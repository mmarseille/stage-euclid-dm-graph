package main;

/**
 * Gère la connection avec la base de données graphe
 * @author Benjamin Bardy
 *
 */
public interface DBConnector {
	public void connect(String uri, String user, String password);
	public void close();
}
