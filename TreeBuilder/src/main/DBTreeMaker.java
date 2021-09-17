package main;

/**
 * Gère la création et la suppression de l'arbre
 * @author Benjamin Bardy
 *
 */
public interface DBTreeMaker extends DBTreeModifier{
	public void createTree(int maxDepth, int maxChildren, int batchSize);
	public void deleteTree(int batchSize);
}
