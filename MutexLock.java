import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexLock {
	public static class Tray {
		public String cook;
		public int numBurgers;
		
		public Tray(String cook, int numBurgers) {
			this.cook = cook;
			this.numBurgers = numBurgers;
		}
	}

	public static class UnsafeWindow {
		private Queue<Tray> window = new LinkedList<Tray>();
		private int maxSize;
		private int cooked;
		private int served;
		public UnsafeWindow(int maxSize) {
			this.maxSize = maxSize;
			this.cooked = 0;
			this.served = 0;
		}
		
		public boolean isFull() {
			return window.size() == maxSize;
		}
		
		public boolean isEmpty() {
			return window.size() == 0;
		}
		
		public int size() {
			return window.size();
		}
		
		public boolean add(Tray d) {
			if (isFull()) {
				return false;
			} else {
				window.add(d);
				this.cooked += d.numBurgers;
				return true;
			}
		}
		
		public Tray remove() {
			if (isEmpty()) {
				return null;
			} else {
				Tray d = window.poll();
				this.served += d.numBurgers;
				return d;
			}
		}
		
		public int getCooked() {
			return cooked;
		}
		
		public int getServed() {
			return served;
		}
	}
	
	public static class Cook extends Thread {
		private String cookName;
		private UnsafeWindow queue;
		private int maxRun;
		private Lock l;
		
		public Cook(String cookName, UnsafeWindow queue, int maxRun, Lock l) {
			this.cookName = cookName;
			this.queue = queue;
			this.maxRun = maxRun;
			this.l = l;
			
		}
		
		private Tray cook() {
			int numBurgers = (int)(Math.random()*5) + 1;
			System.out.println(">>> Cook: " + cookName + " Value: " + numBurgers + " Size: " + queue.size());
			return new Tray(cookName, numBurgers);
		}
		
		public void run() {
			
			int traysCooked = 0;
			while(traysCooked < maxRun) {
				l.lock();
				if (!queue.isFull()) {
					Tray tray = cook();
			        queue.add(tray);
			        traysCooked++;
			    }
				l.unlock();
				try {
					Thread.sleep((long)(100 + Math.random() * 400));
				}
				catch(Exception e){
					System.out.println(e.toString());
				}
			}		
			
		}
	}
	
	public static class Server extends Thread {
		private String serverName;
		private UnsafeWindow queue;
		private boolean canContinue;
		private Lock l;
		
		public Server(String serverName, UnsafeWindow queue, Lock l) {
			this.serverName = serverName;
			this.queue = queue;
			this.canContinue = true;
			this.l = l;
		}
		
		public synchronized boolean canContinue() {
			return this.canContinue;
		}
		
		public synchronized void stopRun() {
			this.canContinue = false;
		}
		
		private Tray serve() {
			Tray d = queue.remove();
			if (d == null) {
				return null;
			}
			System.out.println("<<< Cook: " + d.cook + " Server: " + serverName + " Value: " + d.numBurgers + " Size: " + queue.size());
			return d;
		}
		
		public void run() {
			while(canContinue() || !queue.isEmpty()) {
				l.lock();
				serve();
				l.unlock();
				try {
					Thread.sleep((long)(100 + Math.random() * 400));
				}
				catch(Exception e){
					System.out.println(e.toString());
				}
			}
			
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		int NUM_COOKS = 10;
		int COOK_MAXRUN = 100;
		int NUM_SERVERS = 5;
		int WINDOW_SIZE = 3;
		
		ReentrantLock lock = new ReentrantLock();
		UnsafeWindow window = new UnsafeWindow(WINDOW_SIZE);
		Cook[] cooks = new Cook[NUM_COOKS];
		Server[] servers = new Server[NUM_SERVERS];
		for (int i = 0; i < NUM_COOKS; i++) {
			cooks[i] = new Cook("p" + i, window, COOK_MAXRUN, lock);
			cooks[i].start();
		}
		
		for (int i = 0; i < NUM_SERVERS; i++) {
			servers[i] = new Server("c" + i, window, lock);
			servers[i].start();
		}
		
		for (int i = 0; i < NUM_COOKS; i++) {
			cooks[i].join();
		}
		
		for (int i = 0; i < NUM_SERVERS; i++) {
			servers[i].stopRun();
			servers[i].join();
		}
		
		System.out.println(window.getCooked() + " " + window.getServed());
		System.exit(0);
	}
}
