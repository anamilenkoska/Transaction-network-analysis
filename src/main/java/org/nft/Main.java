package org.nft;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        String file="transactions.csv";
        String output="output.csv";
        List<String> cexFile= Arrays.asList(
                "CEX/tornado.json",
                "CEX/batch.json",
                "CEX/bridges.json",
                "CEX/cex.json",
                "CEX/defi.json",
                "CEX/dex.json"
        );

        Scanner s=new Scanner(System.in);

        //choose the method for execution
        System.out.println("Choose execution mode:");
        System.out.println("1-Sequential execution");
        System.out.println("2-Parallel execution");
        System.out.println("3-Distributed execution");
        int mode=s.nextInt();

        if(mode<1 || mode>3){
            System.out.println("Invalid mode. Please provide correct mode");
            s.close();
            return;
        }

        System.out.println("Enter the maximal depth of the traversal:");
        int maxDepth=s.nextInt();
        s.close();

        long start=System.nanoTime();

        //cases for mode
        switch(mode){
            case 1:
                ExecutionMode.Sequential(file,maxDepth,cexFile,output);
                break;
            case 2:
                ExecutionMode.Parallel(file,maxDepth,cexFile,output);
                break;
            case 3:
                ExecutionMode.Distributed(file,maxDepth,cexFile,output);
                break;
        }

//        //read file
//        readFile reader=new readFile(file,maxDepth,cexFile);
//        Map<String, List<Graph.Edge>> adjList=reader.read();
//
//
//
//        //print the adj list
//        reader.printAdjacencyList(adjList);
//
//        //build the linkability network
//        Map<String, List<Graph.Edge>> linkabilityNetwork = reader.buildLinkabilitynetwork(adjList);
//
//        //write the linkability network to csv file
//        reader.writeLinkabilityNetwork(adjList,output);
//        System.out.println("Linkability Network written to: " + output);
//
//        //print the count of links
//        reader.printLinkWeightCount(linkabilityNetwork);

        long end=System.nanoTime();
        System.out.println("Time to compute the linkability network: "+(end-start)/1000000000+"s");
    }
}
