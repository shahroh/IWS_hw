package edu.upenn.cis455.crawler;

import java.util.ArrayList;

public class WorkerPool {

	private static WorkerPool workerPool;
	private ArrayList<Thread> workers;
	int mNumThreads = 5;
	WorkerThread runnable;
	
	// Constructor instantiates pool of worker threads
	private WorkerPool() {
		workers = new ArrayList<Thread>();
		runnable = new WorkerThread();
		
		for(int i=0; i<mNumThreads; i++){
			workers.add(i, new Thread(runnable));
			workers.get(i).start();
		}
	}
	
	// Static method to return singleton class object
	public static WorkerPool GetSingleton(){
		if(workerPool == null){
			workerPool = new WorkerPool();
		}
		return workerPool;
	}
	
}
