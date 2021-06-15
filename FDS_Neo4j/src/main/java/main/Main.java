package main;

import java.util.ArrayList;
import java.util.List;

public class Main {
	public static double getAverage(List<Double> timeList) {
		double total = 0;
		
		for (double time: timeList) {
			total += time;
		}
		
		return total/timeList.size();
	}

	public static void main(String[] args) throws Exception{
		/*try(HelloWorld hello = new HelloWorld("bolt://localhost:11005","neo4j","0")){
			hello.deleteNodes();
			hello.printGreeting("bien ou quoi");
			hello.deleteNodes();
			hello.printGreeting2("t'habites dans le coin ou quoi");
		}*/
		int nb_iter = 14;
		
		int N = 100;
		int max_depth = 10;
		int max_children = 5;
		
		String titleSep = "************";
		String itSep = "----------------------";
		String resSep = "===============================";
		
		List<Double> createTimes = new ArrayList<Double>();
		List<Double> deleteTimes = new ArrayList<Double>();
		
		try( Neo4J_Tree tree = new Neo4J_Tree("bolt://localhost:11005", "neo4j", "0") ){
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
			
			System.out.println(resSep);
			System.out.println(String.format("MOYENNE DE %d ITERATIONS", nb_iter));
			System.out.println(resSep);
			System.out.println(String.format("GENERATION ARBRE: %.3fs", getAverage(createTimes)));
			System.out.println(String.format("SUPPRESSION ARBRE: %.3fs", getAverage(deleteTimes)));
		}

	}

}
