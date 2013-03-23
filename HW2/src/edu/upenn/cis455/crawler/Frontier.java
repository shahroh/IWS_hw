package edu.upenn.cis455.crawler;

import java.net.URL;
import java.util.PriorityQueue;

public class Frontier {

	private PriorityQueue<URL> frontierQueue;
	private static Frontier frontier;
	private static int maxContentLength;
	
	// Constructor takes no arg and just instantiates a priority queue
	private Frontier(int maxDocSize) {
		frontierQueue = new PriorityQueue<URL>();
		maxContentLength = maxDocSize;
	}

	// Singleton class, method to get singleton object
	public static Frontier GetSingleton(int maxDocSize){
		if(frontier == null){
			frontier = new Frontier(maxDocSize);
		}
		return frontier;
	}

	public static int GetMaxContentLength(){
		return maxContentLength;
	}
	
	// Add URL objects to the priority queue frontier
	public synchronized void addToFrontier(URL newURL){
		frontierQueue.add(newURL);
		notify();
	}

	// Poll URL object from the Frontier
	public synchronized URL pollFromFrontier() throws InterruptedException{
		if(frontierQueue.peek() == null){
			wait();
		}
		return frontierQueue.poll();
	}

}
