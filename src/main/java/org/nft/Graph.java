package org.nft;

import java.util.List;
import java.util.Map;

public class Graph {
    public final Map<String, List<Edge>> adjList;
    public Graph(Map<String, List<Edge>> adjList){
        this.adjList=adjList;
    }
    public static class Edge{
        public final String to;
        public int weight;

        public Edge(String to,int weight){
            this.to=to;
            this.weight=weight;
        }
    }
}
