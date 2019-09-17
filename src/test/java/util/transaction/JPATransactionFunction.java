package util.transaction;

import javax.persistence.EntityManager;
import java.util.function.Function;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
@FunctionalInterface
public interface JPATransactionFunction<T> extends Function<EntityManager, T> {
    default void beforeTransactionCompletion() {

    }

    default void afterTransactionCompletion() {

    }
}
