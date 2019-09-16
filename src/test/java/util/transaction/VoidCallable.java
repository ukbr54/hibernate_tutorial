package util.transaction;

import java.util.concurrent.Callable;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */

@FunctionalInterface
public interface VoidCallable extends Callable<Void> {

    void execute();

    default Void call() throws Exception {
        execute();
        return null;
    }
}
