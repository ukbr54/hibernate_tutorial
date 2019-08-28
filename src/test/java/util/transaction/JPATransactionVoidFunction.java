package util.transaction;

import javax.persistence.EntityManager;
import java.util.function.Consumer;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */

@FunctionalInterface
public interface JPATransactionVoidFunction extends Consumer<EntityManager> {
    default void beforeTransactionCompletion() { }

    default void afterTransactionCompletion() { }

}
