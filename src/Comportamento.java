import java.util.concurrent.Semaphore;

public class Comportamento extends Thread implements IComportamento{
	protected boolean isToStop;
	protected boolean isToPause;
	private Semaphore semaphorePause;

	public Comportamento() {
		semaphorePause = new Semaphore(0);
		isToPause = false;
		isToStop = false;
	}

	@Override
	public void myPause() {
		semaphorePause.drainPermits();
		isToPause = true;
	}

	@Override
	public void myStop() {
		isToStop = true;
	}

	@Override
	public void myResume() {
		semaphorePause.release();
	}
	
	@Override
	public void autoSuspend()  {
		try {
			semaphorePause.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		isToPause = false;
	}
}
