package me.prisonranksx.bukkitutils.segmentedtasks;

public interface Workload {

	/**
	 * @return true if computation is need, false otherwise
	 */
	boolean compute();

}
