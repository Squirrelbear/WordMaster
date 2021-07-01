/**
 * Word Master
 * Author: Peter Mitchell (2021)
 *
 * ActionTimer class:
 * Can be used to keep track of progress timers.
 * Will tick on a fixed increment defined in GamePanel when the update() method is called.
 * The isTriggered() will become true once the timer reaches 0.
 * reset() can be used to reset back to the default time and setTimer() to clear
 * with a new time.
 */
public class ActionTimer {
    /**
     * Time used when resetting the timer.
     */
    private int startTime;
    /**
     * The timer's progress that ticks down continually every update().
     */
    private int timeRemaining;
    /**
     * True when the timeRemaining has reached 0 and stays true until reset.
     */
    private boolean triggered;

    /**
     * Sets the initial timer on the timer and makes it ready to begin ticking on updates.
     *
     * @param startTime The time to start with and is used for each reset.
     */
    public ActionTimer(int startTime) {
        this.startTime = startTime;
        reset();
    }

    /**
     * Ticks the time remaining down on a fixed interval defined in GamePanel.
     * If the timer reaches 0 it will set the triggered to true.
     * Timer is stopped at 0.
     *
     * @param deltaTime Amount of time to update by.
     */
    public void update(int deltaTime) {
        timeRemaining -= deltaTime;
        if(timeRemaining <= 0) {
            triggered = true;
            timeRemaining = 0;
        }
    }

    /**
     * Gets the current triggered state of the timer.
     *
     * @return True when the timer has recently triggered.
     */
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Sets the timer immediately to the specified time and changes
     * the time that will be used for resets.
     *
     * @param time The timer to set the new timer interval to.
     */
    public void setTimer(int time) {
        startTime = time;
        reset();
    }

    /**
     * Resets the time remaining back to the full interval again.
     * And changes the triggered state back to the default of false.
     */
    public void reset() {
        timeRemaining = startTime;
        triggered = false;
    }

    /**
     * Gets the current remaining time on the timer.
     *
     * @return Time remaining in ms.
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Gets a String representation of the current time remaining.
     *
     * @return Time represented as 00:00 format with minutes and seconds.
     */
    public String toString() {
        int timeInSeconds = timeRemaining / 1000;
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        String minutesTime = String.valueOf(minutes);
        while(minutesTime.length() < 2) minutesTime = "0" + minutesTime;
        String secondsTime = String.valueOf(seconds);
        while(secondsTime.length() < 2) secondsTime = "0" + secondsTime;
        return minutesTime + ":" + secondsTime;
    }
}
