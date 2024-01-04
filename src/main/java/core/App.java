package core;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;


public class App {
    public static void main(String[] args) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        String node1 = "Node 1";
        String node2 = "Node 2";
        graph.addVertex(node1);
        graph.addVertex(node2);

        graph.addEdge(node1,node2);

        System.out.println("Graph: "+graph.toString());

        System.out.println("Hello World!");
    }
}
