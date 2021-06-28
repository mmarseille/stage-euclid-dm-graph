package main;

import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

public class BranchIter {
	private final Driver driver;
	
	private static ArrayList<HashMap<String, Object>> nodeList, relList;
	
	private final int batchSize = 50000;
	private static int nb_nodes = 0;
	private static int node_id = 0;
	private static List<Float> paramNode;
	
	
	private static int N;
	private static int max_depth;
	private static int max_children;
	
	private int node_maxDepth;
	private int depth;

	private String nodename;
	private BranchIter parent = null;

	private List<String> children_ids;

	
	public BranchIter(Driver driver, int N, int max_depth, int max_children) {
		BranchIter.N = N;
		BranchIter.max_children = max_children;
		BranchIter.max_depth = max_depth;
		this.node_maxDepth = max_depth;
		
		this.driver = driver;
		this.nodename = String.format("n%s", node_id);
		this.depth = 0;
		
		Float[] data = new Float[80];
		Arrays.fill(data,Float.valueOf(0));
		paramNode = Arrays.asList(data);
		
		node_id++;
		nb_nodes++;
	}
		
	
	public BranchIter(Driver driver, BranchIter parent) {
		this.driver = driver;
		this.parent = parent;
		this.nodename = String.format("n%s", node_id);
		this.depth = parent.depth + 1;
		this.node_maxDepth = (int) (BranchIter.max_depth + new Random().nextGaussian());
	
		node_id++;
	}
	
	public double createTree() {
		double start, startTree, startMid, endTree, end;
		
		//Génération script arbre
		start = System.currentTimeMillis();
		createNodes();
		end = System.currentTimeMillis()-start;
		System.out.println(String.format("NOEUDS: %.3fs",(end/1000)));
		
		//Affichage du script généré
		//System.out.println(script);
		startTree = System.currentTimeMillis();
		addChildren();
		endTree = System.currentTimeMillis() - startTree;
		System.out.println(String.format("treebuild: %.3fs",(endTree/1000)));
		
		//Génération arbre
		startMid = System.currentTimeMillis();
		executeScript();
		end = System.currentTimeMillis() - startMid;
		System.out.println(String.format("REL: %.3fs",(end/1000)));
		
		end = System.currentTimeMillis() - start;
		System.out.println(String.format("TOTAL: %.3fs",(end/1000)));
		
		return (end-endTree)/1000;
	}
	

	private void createNodes() {		
		nodeList = new ArrayList<>(N);
		
		HashMap<String, Object> node;
		
		for (int i=0; i<N; i++) {
			node = new HashMap<>();
			node.put("name", "n"+i);
			node.put("param", paramNode);
			nodeList.add(i, node);
		}
		
		try(Session session = driver.session()){
			session.run("CREATE CONSTRAINT uniq_id IF NOT EXISTS ON (n:Node) ASSERT n.name IS UNIQUE");
			
			if (N <= batchSize) {
				session.run("UNWIND $nodes AS node "
						+"CREATE (n:Node) "
						+"SET n += node"
						, parameters("nodes", nodeList));	
			} else {
				for(int i=0; i<N; i += batchSize) {
					session.run("UNWIND $nodes AS node "
							+"CREATE (n:Node) "
							+"SET n += node"
							, parameters("nodes", nodeList.subList(i, i+Math.min(batchSize, N-i))));
					System.out.println(String.format("%d/%d",i,N));
				}
				System.out.println(String.format("%d/%d",N,N));
			}			

		}
	}
	
	public void resetTree() {
		node_id = 0;
		nb_nodes = 0;
	}
	
	
	private void addChildren() {		
		Float[] data = new Float[10];
		Arrays.fill(data,Float.valueOf(0));
		List<Float> paramRel = Arrays.asList(data);
		
		relList = new ArrayList<>();
		HashMap<String, Object> rel;
		
		Stack<BranchIter> branches = new Stack<BranchIter>();
		branches.add(this);
		
		int id = 0;
		int total = 0;

		while (!branches.empty()) {
			BranchIter currentNode = branches.pop();
			
			if (nb_nodes == N || currentNode.depth >= currentNode.node_maxDepth) {		
				continue;
			}


			int child_max = (currentNode.depth == 0)? Math.max(nb_nodes, 20) : max_children;
			double random = (currentNode.depth == 0)? 1 : Math.random();
			
			//Arêtes random
			int nb = (int) (1+ (random * Math.min(child_max-1, N-nb_nodes-1)));	
	
			//Arêtes max
			//int nb = 1 + Math.min(child_max-1, N-nb_nodes-1);
			
			nb_nodes += nb;
	
	//		System.out.println("DEPTH: "+depth);
	//		System.out.println("NB_NODES: "+nb_nodes);
						
			
			rel = new HashMap<>();

			currentNode.children_ids = new ArrayList<String>(nb);
						
			for (int i=0; i<nb; i++) {
				BranchIter child = new BranchIter(driver, currentNode);
				currentNode.children_ids.add(i, child.nodename);
				
				branches.push(child);
			}
			
			total += currentNode.children_ids.size();
			rel.put("children", currentNode.children_ids);
			rel.put("parent", currentNode.nodename);
			rel.put("param", paramRel);
			relList.add(rel);
			
		}
				
	}
	
	public void executeScript() {
		int relSize = relList.size();
		
		try(Session session = driver.session()){
			if (relSize <= batchSize) {
				session.run("UNWIND $rels AS r "
						+"MATCH (a:Node), (b:Node) WHERE a.name IN r.children AND b.name = r.parent "
						+"MERGE (a)-[:CHILD_OF{param: r.param}]->(b)", parameters("rels", relList));	
			} else {
				for(int i=0; i<relSize; i += batchSize) {
					System.out.println(String.format("%d/%d",i,relSize));
					session.run("UNWIND $rels AS r "
							+"MATCH (a:Node), (b:Node) WHERE a.name IN r.children AND b.name = r.parent "
							+"MERGE (a)-[:CHILD_OF{param: r.param}]->(b)", parameters("rels", relList.subList(i, i+Math.min(batchSize, relSize-i))));
				}
				System.out.println(String.format("%d/%d",relSize,relSize));
			}

		}
	}
	
	public BranchIter getParent() {
		return parent;
	}


}
