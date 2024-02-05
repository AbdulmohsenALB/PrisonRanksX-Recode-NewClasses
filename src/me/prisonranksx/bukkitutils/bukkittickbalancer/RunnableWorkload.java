package me.prisonranksx.bukkitutils.bukkittickbalancer;

/**
 * Basic workload to run any action within a task.
 */
public class RunnableWorkload implements Workload {

	private Runnable runnable;

	public RunnableWorkload(Runnable runnable) {
		this.runnable = runnable;
	}

	@Override
	public boolean compute() {
		runnable.run();
		return true;
	}

}
