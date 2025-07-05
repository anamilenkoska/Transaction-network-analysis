package org.nft;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExecutionMode {
    public static void Sequential(String etnFile,int maxDepth,List<String> cexFile,String baycFile,String output) throws IOException {
        //read file
        readFile reader=new readFile(baycFile,etnFile,maxDepth,cexFile);        //reads teh file,process the data and build the graph, takes the csv file,bfs and list of cex files
        Map<String, List<Graph.Edge>> adjList=reader.read();        //parses the csv file, loads cex addr and builds the initial directed graph

        //print the adj list
        //reader.printAdjacencyList(adjList);

        //build the linkability network
        Map<String, List<Graph.Edge>> linkabilityNetwork = reader.buildLinkabilitynetwork(adjList);     //runs the bfs method

        //write the linkability network to csv file
        reader.writeLinkabilityNetwork(linkabilityNetwork,output);      //writes to the output file
        System.out.println("Linkability Network written to: " + output);

        //print the count of links
        reader.printLinkWeightCount(linkabilityNetwork);
    }

    /*public static void Parallel(String file,int maxDepth,List<String> cexFile, String output) throws IOException {
        //Parallel.run(file,maxDepth,cexFile,output);
    }*/

    /*public static void Distributed(String file,int maxDepth,List<String> cexFile, String output){
        System.out.println("Not implemented");
    }*/
}
