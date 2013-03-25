package edu.upenn.cis455.crawler;

import java.net.URL;
import java.util.PriorityQueue;

public class Frontier {

	private PriorityQueue<URL> frontierQueue;
	private static Frontier frontier;
	private static int maxContentLength;

	// Constructor takes no arg and just instantiates a priority queue
	private Frontier() {
		frontierQueue = new PriorityQueue<URL>();
	}

	// Singleton class, method to get singleton object
	public static Frontier GetSingleton(){
		if(frontier == null){
			frontier = new Frontier();
		}
		return frontier;
	}

	public void setMaxDocSize(int maxDocSize){
		maxContentLength = maxDocSize;
	}

	public static int GetMaxContentLength(){
		return maxContentLength;
	}

	// Add URL objects to the priority queue frontier
	public synchronized void addToFrontier(URL newURL){
		if(!frontierQueue.contains(newURL)){
			frontierQueue.add(newURL);
			notify();
		}
	}

	// Poll URL object from the Frontier
	public synchronized URL pollFromFrontier() throws InterruptedException{
		if(frontierQueue.peek() == null){
			wait();
		}
		return frontierQueue.poll();
	}

}
