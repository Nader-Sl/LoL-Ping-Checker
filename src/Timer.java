

public class Timer {

	private long start;
	private long limit;
	private boolean paused = false;
	private long pauseTime = -1;

	/**
	 * Instantiates a new Timer with a given time period in milliseconds.
	 * 
	 * @param period
	 *            Time period in milliseconds.
	 */
	public Timer(final long limit) {
		this.limit = limit;
		start = System.currentTimeMillis();
	}

	public void setLimit(final long limit) {
		this.limit = limit;
	}

	public long getLimit() {
		return limit;
	}

	/**
	 * Returns the number of milliseconds elapsed since the start time.
	 * 
	 * @return The elapsed time in milliseconds.
	 */
	public long getElapsed() {
		long timeSincePause = 0;
		if (paused) {
			timeSincePause = (System.currentTimeMillis() - pauseTime);
		}
		return System.currentTimeMillis() - (start + timeSincePause);
	}

	/**
	 * Returns the number of milliseconds remaining until the timer is up.
	 * 
	 * @return The remaining time in milliseconds.
	 */
	public long getRemaining() {
		return limit - getElapsed();
	}

	/**
	 * Returns <tt>true</tt> if this timer's time period has not yet elapsed.
	 * 
	 * @return <tt>true</tt> if the time period has not yet passed.
	 */
	public boolean isRunning() {
		return getRemaining() > 0;
	}

	public boolean isUp() {
		return getRemaining() <= 0;
	}

	public boolean isUpThenReset() {
		if (isUp()) {
			reset();
			return true;
		}
		return false;
	}

	/**
	 * Restarts this timer using its period.
	 */
	public void reset() {
		reset(limit);
	}

	public void reset(final long limit) {
		setLimit(limit);
		start = System.currentTimeMillis();
	}

	public void pause() {
		if (!paused) {
			paused = true;
			pauseTime = System.currentTimeMillis();
		}
	}

	public void resume() {
		if (paused) {
			start += (System.currentTimeMillis() - pauseTime);
			paused = false;
		}
	}

}