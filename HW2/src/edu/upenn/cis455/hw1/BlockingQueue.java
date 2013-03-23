package edu.upenn.cis455.hw1;

import java.net.Socket;
import java.util.LinkedList;

// Singleton class that has a queue (LinkedList) and has methods to add/notify and for threads to dequeue/wait
public class BlockingQueue {

	public static boolean shutdown_flag;
	private static BlockingQueue blockingQueue;
	private LinkedList<Socket> reqQueue;
	
	// Constructor initializes the queue
	private BlockingQueue() {
		reqQueue = new LinkedList<Socket>();
	}
	
	public static BlockingQueue GetSingleton(){
		if(blockingQueue == null)
			blockingQueue = new BlockingQueue();
		return blockingQueue;
	}

	// Method to add new request to the queue
	// Called when a request is heard on the socket
	// Also sends a notification to all threads waiting on it
	public synchronized void addToQueue(Socket new_request){
		this.reqQueue.add(new_request);
		notify();
		return;
	}

	// Method to remove element from end of queue
	// Called by a worker thread
	// First checks if queue empty
	// If empty, calls wait()
	// Else, thread dequeues and starts working on dequeued request
	public synchronized Socket dequeue() throws InterruptedException{
		
		if(this.reqQueue.peekFirst() == null){
				ThreadPool.registerURI(Thread.currentThread().getId(), "waiting");
				wait();
			}
			else{
				return this.reqQueue.poll();
			}
		
		return this.reqQueue.poll();
	}
}
