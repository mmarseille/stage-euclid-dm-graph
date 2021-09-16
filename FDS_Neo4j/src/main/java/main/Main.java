package main;

import java.util.ArrayList;
import java.util.List;

/**
 * Réalise les mesures sur l'arbre créé en Neo4J
 * @author Benjamin Bardy
 *
 */
public class Main {
	/**
	 * Donne la durée moyenne de l'expérience
	 * @param timeList la liste des mesures temporelles
	 * @return la durée moyenne
	 */
	public static double getAverage(List<Double> timeList) {
		double total = 0;
		
		for (double time: timeList) {
			total += time;
		}
		
		return total/timeList.size();
	}

	public static void main(String[] args) throws Exception{
		int nb_iter = 10;
		
		int N = 1000;
		int max_depth = 12;
		int max_children = 5;
		
		String titleSep = "************";
		String itSep = "----------------------";
		String resSep = "===============================";
		
		List<Double> createTimes = new ArrayList<Double>();
		List<Double> deleteTimes = new ArrayList<Double>();
		
		try( Neo4J_Tree tree = new Neo4J_Tree("bolt://localhost:7687", "neo4j", "0") ){
			//deleteTimes.add(tree.deleteTree());
			
			for (int i=0; i<nb_iter; i++) {
				if ( (i+1)%5 == 0 || (i+1) == nb_iter){
					System.out.println(titleSep);
					System.out.println(String.format("ITERATION %d/%d", (i+1), nb_iter));
					System.out.println(titleSep);
					}
				
				//Temps génération arbre	
				createTimes.add(tree.createTree(N, max_depth, max_children));	
				
				//Temps suppression arbre
				deleteTimes.add(tree.deleteTree());
				
				System.out.println(itSep);
			}
			
			//createTimes.add(tree.createTree(N, max_depth, max_children));	
						
			System.out.println(resSep);
			System.out.println(String.format("MOYENNE DE %d ITERATIONS", nb_iter));
			System.out.println(resSep);
			System.out.println(String.format("GENERATION ARBRE: %.3fs", getAverage(createTimes)));
			System.out.println(String.format("SUPPRESSION ARBRE: %.3fs", getAverage(deleteTimes)));
		}

	}

}
