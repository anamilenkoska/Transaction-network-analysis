package org.nft;

import java.util.List;
import java.util.Map;

public class Graph {
    public final Map<String, List<Edge>> adjList;   //each key is node and its value is List<Edge>-all the outgoing edges from that node
    public Graph(Map<String, List<Edge>> adjList){
        this.adjList=adjList;
    }
    //static means this inner class doesn`t require an instance of Graph to be created
    public static class Edge{       //represents edge from one node to another
        public final String to;     //target node
        public int weight;          //distance

        public Edge(String to,int weight){
            this.to=to;
            this.weight=weight;
        }
    }
}       //groups the graph structure into a reusable object, for representing edges
