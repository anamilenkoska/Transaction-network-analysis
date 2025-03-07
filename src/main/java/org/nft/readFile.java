package org.nft;

import java.io.*;
import java.util.*;

public class readFile {
    private final String filePath;
    private final int maxDepth;     //maximum traversal depth(d)
    private final Set<String> cexAddr;

    public readFile(String filePath,int maxDepth,List<String> cexFile) throws IOException {
        this.filePath = filePath;
        this.maxDepth=maxDepth;
        this.cexAddr=loadCexAddr(cexFile);
    }

    private Set<String> loadCexAddr(List<String> cexFile) throws IOException{
        Set<String> cexAddr=new HashSet<>();
        for(String cexF:cexFile){
            try(BufferedReader bufferedReader=new BufferedReader(new FileReader(cexF))){
                StringBuilder json=new StringBuilder();
                String line;
                while((line=bufferedReader.readLine())!=null){
                    json.append(line.trim());
                }
                String content=json.toString();
                if(content.startsWith("[") && content.endsWith("]")){
                    content=content.substring(1,content.length()-1);
                    String[] addresses=content.split(",");
                    for(String addr:addresses){
                        addr=addr.trim().replace("\"","");
                        if(!addr.isEmpty()){
                            cexAddr.add(addr);
                        }
                    }
                }
            }
        }
        return cexAddr;
    }

    public Map<String, List<Graph.Edge>> read() throws IOException {
        Map<String, List<Graph.Edge>> adjList=new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean header=true;
            while ((line=bufferedReader.readLine())!= null) {
                if(header){
                    header=false;
                    continue;
                }
                String[] columns = line.split(",");
                if (columns.length < 6) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }
                String from = columns[4].trim();
                String to = columns[5].trim();
                if(from.equals(to) || cexAddr.contains(from) || cexAddr.contains(to)){
                    continue;       //skip self-loops
                }
                adjList.putIfAbsent(from, new ArrayList<>());
                adjList.get(from).add(new Graph.Edge(to, 0));
            }
        }
        return adjList;
    }

    public Map<String,List<Graph.Edge>> buildLinkabilitynetwork(Map<String,List<Graph.Edge>> adjList){
        Map<String,List<Graph.Edge>> linkabilityNetwork=new HashMap<>();
        for(String node:adjList.keySet()){
            if(cexAddr.contains(node)){
                continue;   //skip CEX addresses
            }
            linkabilityNetwork.put(node,new ArrayList<>());
            //perform BFS to find the shortest paths up to the maxDepth
            Map<String,Integer> shortestPaths=BFS(adjList,node);
            for(Map.Entry<String,Integer> entry:shortestPaths.entrySet()){
                String neighbour=entry.getKey();
                int pathLength=entry.getValue();
                if(pathLength<=maxDepth && !cexAddr.contains(neighbour)){
                    linkabilityNetwork.get(node).add(new Graph.Edge(neighbour,pathLength));
                }
            }
        }
        return linkabilityNetwork;
    }

    public Map<String,Integer> BFS(Map<String,List<Graph.Edge>> adjList,String start){
        Map<String,Integer> distances=new HashMap<>();
        Queue<String> queue=new LinkedList<>();
        distances.put(start,0);
        queue.add(start);

        while(!queue.isEmpty()){
            String current=queue.poll();
            int currentDistance=distances.get(current);
            if(currentDistance>=maxDepth){
                continue;       //stop exploring paths longer than maxDepth
            }
            for(Graph.Edge edge:adjList.getOrDefault(current,new ArrayList<>())){
                if(!distances.containsKey(edge.to) && !cexAddr.contains(edge.to)){
                    distances.put(edge.to,currentDistance+1);
                    queue.add(edge.to);
                }
            }
        }
        return distances;
    }

    public void writeLinkabilityNetwork(Map<String, List<Graph.Edge>> adjList, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            writer.write("from,to,weight\n"); // Write CSV header
            boolean data=false;
            for (Map.Entry<String, List<Graph.Edge>> entry : adjList.entrySet()) {
                String from = entry.getKey();
                for (Graph.Edge edge : entry.getValue()) {
                    if(!cexAddr.contains(from) && !cexAddr.contains(edge.to)) {
                        writer.write(from + "," + edge.to + "," + edge.weight + "\n");
                        data=true;
                    }
                }
            }
            if(!data){
                System.out.println("No valid data");
            }
        }
    }

    public void printAdjacencyList(Map<String,List<Graph.Edge>> adjList){
        System.out.println("Ethereum Transaction Network(ETN):");
        for(Map.Entry<String,List<Graph.Edge>> entry:adjList.entrySet()){
            String sender=entry.getKey();
            if(cexAddr.contains(sender)){
                continue;
            }
            System.out.print(sender+" -> ");
            for(Graph.Edge edge:entry.getValue()){
                if (cexAddr.contains(edge.to)) {
                    continue;
                }
                System.out.print(edge.to+", ");
            }
            System.out.println();
        }
    }

    public void printLinkWeightCount(Map<String, List<Graph.Edge>> linkabilityNetwork){
        Map<Integer, Integer> weightC=new HashMap<>();
        for(List<Graph.Edge> edges:linkabilityNetwork.values()){
            for(Graph.Edge edge:edges){
                weightC.put(edge.weight,weightC.getOrDefault(edge.weight,0)+1);
            }
        }
        System.out.println("Link counts by weight:");
        for(Map.Entry<Integer,Integer> entry:weightC.entrySet()){
            System.out.println("Weight "+entry.getKey()+": "+entry.getValue()+" links");
        }
    }
}
