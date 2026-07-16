package rotation;

/**
 * Scheduling boundary so rotation logic does not create or own executor threads directly.
 */
public interface RotationScheduler {

    void start(Runnable task);

    void stop();

    boolean isRunning();
}
