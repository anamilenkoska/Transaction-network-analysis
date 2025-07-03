//package org.nft;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ForkJoinPool;
//import java.util.concurrent.RecursiveTask;
//
////uses fork/join framework
//public class Parallel {
//    //defines task that can be forked and joined-RecursiveTask
//    static class Linkabillity extends RecursiveTask<Map<String, List<Graph.Edge>>>{
//        private final Map<String, List<Graph.Edge>> adjList;        //stores the full adj list read from the input file
//        private final readFile reader;      //reference to the sequential part that provides bfs and cex filtering
//        private final int maxDepth;
//        private final int THRESHOLD;     //to determine when to stop forkig and compute sequentially
//        private final List<String> nodes;       //list of nodes addr this task is responsible for
//
//        public Linkabillity(Map<String, List<Graph.Edge>> adjList,readFile reader,int maxDepth,List<String>nodes){
//            this.adjList=adjList;
//            this.reader=reader;
//            this.maxDepth=maxDepth;
//            this.nodes=nodes;
//            //ensures that each forked task works on reasonable chunk of nodes to balance load
//            int cores=Runtime.getRuntime().availableProcessors();
//            this.THRESHOLD=Math.max(1000,nodes.size()/(cores*4));
//        }
//
//        //main logic for forking class
//        @Override
//        protected Map<String, List<Graph.Edge>> compute(){
//            //List<String> nodes=new ArrayList<>(adjList.keySet());
//            if(nodes.size()<=THRESHOLD){        //if the file is small compute sequentially
//                return computeDirectly(nodes);
//            }
//            int middle=nodes.size()/2;      //otherwise split into 2
//            Linkabillity left=new Linkabillity(adjList,reader,maxDepth,nodes.subList(0,middle));
//            Linkabillity right=new Linkabillity(adjList,reader,maxDepth,nodes.subList(middle,nodes.size()));
//
//            left.fork();
//            Map<String, List<Graph.Edge>> rResult=right.compute();
//            Map<String, List<Graph.Edge>> lResult=left.join();
//            //fork the left half and process the right side immediately, then join the result of the left side
//
//            //lResult.putAll(rResul);
//
//            // Merge the results, ensuring no duplicates
//            for (Map.Entry<String, List<Graph.Edge>> entry : rResult.entrySet()) {
//                String node = entry.getKey();
//                List<Graph.Edge> edges = entry.getValue();
//
//                synchronized (lResult){
//                    if (!lResult.containsKey(node)) {
//                        lResult.put(node, new ArrayList<>(edges));
//                    } else {
//                        // Merge edges in a way that avoids duplicates
//                        for (Graph.Edge edge : edges) {
//                            if (!lResult.get(node).contains(edge)) {
//                                lResult.get(node).add(edge);
//                            }
//                        }
//                    }
//                }
//            }
//            return lResult;
//        }
//
//        private Map<String, List<Graph.Edge>> subMap(List<String> nodes,int start,int end){     //returns submap of adjacency list containing only nodes from the start index to the end index
//            Map<String, List<Graph.Edge>> sub=new HashMap<>();
//            for(int i=start;i<end;i++){
//                String node=nodes.get(i);
//                sub.put(node,adjList.get(node));
//            }
//            return sub;
//        }
//
//        private Map<String, List<Graph.Edge>> computeDirectly(List<String> nodes){      //for sequential part, where the list node is small
//            Map<String, List<Graph.Edge>> result=new HashMap<>();
//            for(String node:nodes){
//                if(reader.cexAddr.contains((node))){
//                    continue;
//                }
//                result.put(node,new ArrayList<>());
//
//                Map<String,Integer> shortestPath=reader.BFS(adjList,node);
//                for(Map.Entry<String,Integer>entry:shortestPath.entrySet()){
//                    String neighbour=entry.getKey();
//                    int pathLength=entry.getValue();
//                    if(!node.equals(neighbour) && pathLength<=maxDepth && !reader.cexAddr.contains(neighbour)){
//                        result.get(node).add(new Graph.Edge(neighbour,pathLength));
//                    }
//                }
////                if(!reader.cexAddr.contains(node)){
////                    Map<String, Integer> distances=reader.BFS(adjList,node);
////                    List<Graph.Edge> edges=new ArrayList<>();
////                    for(Map.Entry<String, Integer> entry:distances.entrySet()){
////                        String to=entry.getKey();
////                        int dist=entry.getValue();
////                        if(!node.equals(to) && dist<=maxDepth && !reader.cexAddr.contains(to)){
////                            edges.add(new Graph.Edge(to,dist));
////                        }
////                    }
////                    if(!edges.isEmpty()) {
////                        result.put(node, edges);
////                    }
////                }
//            }
//            return result;
//        }
//    }
//
//    public static void run(String filePath,int maxDepth,List<String> cexFile,String output) throws IOException{
//        readFile reader=new readFile(filePath,maxDepth,cexFile);
//        Map<String,List<Graph.Edge>> adjList=reader.read();
//        ForkJoinPool pool=new ForkJoinPool();       //creates pool to run parallel tasks
//
//        List<String> allNodes=new ArrayList<>(adjList.keySet());        //gets all node addr for splitting
//
//        //executes the parallel computation and gets the result
//        Linkabillity task=new Linkabillity(adjList,reader,maxDepth,allNodes);
//        Map<String, List<Graph.Edge>> linkabilityNetwork=pool.invoke(task);
//
//        //reader.printAdjacencyList(linkabilityNetwork);
//        reader.writeLinkabilityNetwork(linkabilityNetwork,output);
//        System.out.println("Linkability Network written to: " + output);
//        printLinkWeightCount(linkabilityNetwork);
//    }
//
//    public static void printLinkWeightCount(Map<String, List<Graph.Edge>> linkbilityNetwork){
//        Map<Integer, Integer> weightC=new HashMap<>();
//        //count how many edges exist for each weight
//        for(List<Graph.Edge> edges:linkbilityNetwork.values()){
//            for(Graph.Edge edge:edges){
//                weightC.put(edge.weight,weightC.getOrDefault(edge.weight,0)+1);
//            }
//        }
//        System.out.println("Link counts by weight:");
//        for(Map.Entry<Integer, Integer> entry:weightC.entrySet()){
//            System.out.println("Weight "+entry.getKey()+": "+entry.getValue()+" links");
//        }
//    }
//}
