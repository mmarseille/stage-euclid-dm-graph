package main;

import java.util.HashMap;
import java.util.List;

/**
 * Décrit les différentes opérations réalisables sur le graphe
 * @author Benjamin Bardy
 *
 */
public interface DBTreeModifier extends DBWrapper{
	public void insertNode(Node node);
	public void insertAllNodes(List<Node> nodes);
	public void generateAllNodes(int n, String type, HashMap<String, Object> nodeArgs);

	public void insertRelationship(Relationship relationship);
	public void insertAllRelationships(List<Relationship> relationships);
		
	public void deleteNode(long id);
	public void deleteAllNodes();
	
	public void deleteRelationship(long id);
	public void deleteAllRelationships();
}
