package main;

public class Main {

	public static void main(String[] args) throws Exception{
			/*try(HelloWorld hello = new HelloWorld("bolt://localhost:11005","neo4j","0")){
				hello.deleteNodes();
				hello.printGreeting("bien ou quoi");
				hello.deleteNodes();
				hello.printGreeting2("t'habites dans le coin ou quoi");
			}*/
		
		Branch start = new Branch();
		start.createTree();
	}
	
}
