package org.nft;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExecutionMode {
    public static void Sequential(String file, int maxDepth, List<String> cexFile,String output) throws IOException {
        //read file
        readFile reader=new readFile(file,maxDepth,cexFile);
        Map<String, List<Graph.Edge>> adjList=reader.read();

        //print the adj list
        reader.printAdjacencyList(adjList);

        //build the linkability network
        Map<String, List<Graph.Edge>> linkabilityNetwork = reader.buildLinkabilitynetwork(adjList);

        //write the linkability network to csv file
        reader.writeLinkabilityNetwork(adjList,output);
        System.out.println("Linkability Network written to: " + output);

        //print the count of links
        reader.printLinkWeightCount(linkabilityNetwork);
    }

    public static void Parallel(String file,int maxDepth,List<String> cexFile, String output){
        System.out.println("Not implemented");
    }

    public static void Distributed(String file,int maxDepth,List<String> cexFile, String output){
        System.out.println("Not implemented");
    }
}
