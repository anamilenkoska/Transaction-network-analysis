package org.nft;

import java.io.*;
import java.util.*;

public class readFile {
    private final String filePath;
    private final int maxDepth;     //maximum traversal depth(d)
    private final Set<String> cexAddr;
    public final Set<String> baycAddr;

    public readFile(String baycFile,String filePath,int maxDepth,List<String> cexFile) throws IOException {
        this.filePath = filePath;
        this.maxDepth=maxDepth;
        this.cexAddr=loadCexAddr(cexFile);      //calls the method to read all addr from the json
        this.baycAddr=loadBAYCAddr(baycFile);
    }

    private Set<String> loadCexAddr(List<String> cexFile) throws IOException{
        Set<String> cexAddr=new HashSet<>();    //initializes empty hash set to store addresses, hashset used because is the fastest
        for(String cexF:cexFile){       //iterate over all json file paths
            try(BufferedReader bufferedReader=new BufferedReader(new FileReader(cexF))){        //open each file with a buffered reader, faster than reading one char at a time
                StringBuilder json=new StringBuilder();     //builds the full json content as string
                String line;
                while((line=bufferedReader.readLine())!=null){      //reads all lines and trims whitespaces
                    json.append(line.trim());
                }
                String content=json.toString();         //converts to a single json string
                if(content.startsWith("[") && content.endsWith("]")){   //check if the content is valid json array
                    content=content.substring(1,content.length()-1);       //removes the [ ]
                    String[] addresses=content.split(",");          //split the addresses by commas
                    for(String addr:addresses){
                        addr=addr.trim().replace("\"","");      //cleans each addr and adds to the set if non-empty
                        if(!addr.isEmpty()){
                            cexAddr.add(addr);
                        }
                    }
                }
            }
        }
        return cexAddr;     //returns the complete set of addr
    }
    public Set<String> loadBAYCAddr(String baycFile) throws IOException{
        Set<String> addresses=new HashSet<>();
        BufferedReader reader=new BufferedReader(new FileReader(baycFile));
        String line;
        while((line=reader.readLine())!=null){
            String[] parts=line.split(",");
            for(String part:parts){
                addresses.add(part.trim());
            }
        }
        //reader.close();
        return addresses;
    }

    public Map<String, List<Graph.Edge>> read() throws IOException {    //reads the csv file and builds adjacency list
        //the values are lists of Edge objects, each representing transaction to a 'to' addr with weight of 0-initially
        Map<String, List<Graph.Edge>> adjList=new HashMap<>();      //initialization of the adjacency list to store outgoing edges per addr
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {    //opens the csv file
            String line;        //for storing each line of the file
            boolean header=true;    //to skip the first line
            while ((line=bufferedReader.readLine())!= null) {
                if(header){
                    header=false;
                    continue;   //skip the header
                }
                String[] columns = line.split(",");
                if (columns.length < 7) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;       //skip malformed lines-line with fewer columns than 6
                }
                String from = columns[5].trim();    //get the from addr columns, trim whitespaces
                String to = columns[6].trim();      //get the to addr columns, trim whitespaces
                if(from.equals(to) || cexAddr.contains(from) || cexAddr.contains(to)){
                    continue;       //skip self-loops and transactions involving CEX addr
                }
                adjList.putIfAbsent(from, new ArrayList<>());           //ensures the 'from' addr exists as a key in the map
                adjList.get(from).add(new Graph.Edge(to, 0));   //add the edge with weight 0 to the adjacency list
            }
        }
        return adjList; //return the complete list
    }

    public Map<String,List<Graph.Edge>> buildLinkabilitynetwork(Map<String,List<Graph.Edge>> adjList){      //builds new graph from the original one using bfs
        Map<String,List<Graph.Edge>> linkabilityNetwork=new HashMap<>();    //initialization of the new graph
        for(String node:adjList.keySet()){      //iterates over the nodes in the original adj list from the read method
            if(cexAddr.contains(node)){
                continue;   //skip CEX addresses
            }
            //linkabilityNetwork.put(node,new ArrayList<>());     //initializes empty list of edges for the current node
            //perform BFS to find the shortest paths up to the maxDepth
            Map<String,Integer> shortestPaths=BFS(adjList,node);
            for(Map.Entry<String,Integer> entry:shortestPaths.entrySet()){      //iterates over each node reachable from node
                String neighbour=entry.getKey();        //extracts the target addr
                int pathLength=entry.getValue();        //the number of hops to reach it from node
                //ensures the node isn`t linking to itself, the path is within the allowed depth, the target node isn`t CEX
                if(baycAddr.contains(neighbour) && !neighbour.equals(node) && pathLength<=maxDepth){
                    linkabilityNetwork.putIfAbsent(node,new ArrayList<>());
                    linkabilityNetwork.get(node).add(new Graph.Edge(neighbour,pathLength)); //adds an edge from node to neighbour
                }
            }
        }
        return linkabilityNetwork;      //return graph of all reachable nodes with the path length as the weight
    }

    public Map<String,Integer> BFS(Map<String,List<Graph.Edge>> adjList,String start){  //perform bfs from start
        Map<String,Integer> distances=new HashMap<>();      //stores teh shortest path length from start to every other node
        Queue<String> queue=new LinkedList<>();
        distances.put(start,0);
        queue.add(start);

        while(!queue.isEmpty()){        //visit all nodes
            String current=queue.poll();        //retrieves and removes the node at the front of the queue-the current node being explored
            int currentDistance=distances.get(current);     //retrieves the known shortest distance from start to current
            if(currentDistance>=maxDepth){
                continue;       //stop exploring paths longer than maxDepth
            }
            for(Graph.Edge edge:adjList.getOrDefault(current,new ArrayList<>())){      //iterates over all neighbours of the current node using the original adj list
                if(!distances.containsKey(edge.to) && !cexAddr.contains(edge.to)){      //ensure that this neighbour hasn`t been visited before
                    distances.put(edge.to,currentDistance+1);   //records shortest path, one more than the current node's distance
                    queue.add(edge.to);             //adds the neighbour to the queue to explore its neighbours next
                }
            }
        }       //updates distances for unvisited and non-cex neighbours
        return distances;
    }

    public void writeLinkabilityNetwork(Map<String, List<Graph.Edge>> adjList, String outputFilePath) throws IOException {  //write the graph to csv file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {      //opens the file for writing
            writer.write("from,to,weight\n"); // Write CSV header
            boolean data=false;
            for (Map.Entry<String, List<Graph.Edge>> entry : adjList.entrySet()) {      //iterates through the entries of the adj list
                String from = entry.getKey();       //extracts the from node of the current adj list entry
                for (Graph.Edge edge : entry.getValue()) {      //iterates through all neighbours of the from node
                    if(!cexAddr.contains(from) && !cexAddr.contains(edge.to)) {     //filters out edges with CEX addr
                        writer.write(from + "," + edge.to + "," + edge.weight + "\n");      //writes the edge
                        data=true;
                    }
                }
            }           //write valid edges
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
    }   //prints readable version of the adjacency list

    public void printLinkWeightCount(Map<String, List<Graph.Edge>> linkabilityNetwork){     //counts and prints how many edges exist in the linkability network
        Map<Integer, Integer> weightC=new HashMap<>();      //count how many edges exist for each weight
        for(List<Graph.Edge> edges:linkabilityNetwork.values()){        //iterates over each list of edges from each node
            for(Graph.Edge edge:edges){         //iterates over every edge of the current node
                weightC.put(edge.weight,weightC.getOrDefault(edge.weight,0)+1); //increments the count for the current edge`s weight
                //if the weight hasnot been seen before, return 0, then add 1 to it
            }
        }
        System.out.println("Link counts by weight:");
        for(Map.Entry<Integer,Integer> entry:weightC.entrySet()){       //iterates over the entries of the weight count map
            System.out.println("Weight "+entry.getKey()+": "+entry.getValue()+" links");        
        }       //prints the count of links for each weight value


    }
}
