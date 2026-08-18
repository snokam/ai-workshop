package com.example.aiworkshop.workshop;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * Wiring for an agent whose task has not been done yet.
 *
 * <p>{@link #wire} is what every agent bean is built through. When the task is finished it hands
 * back the real agent; when it is not, it hands back something with the same type that throws
 * {@link TaskNotImplementedException} the moment it is called. The application context still
 * starts, every screen that does not need that agent still works, and the one that does says what
 * to open.
 */
public final class UnfinishedTasks {

    private UnfinishedTasks() {}

    public static <T> T wire(Class<T> type, WorkshopTask task, boolean implemented, Supplier<T> agent) {
        return implemented ? agent.get() : stub(type, task);
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, WorkshopTask task) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if (method.getName().equals("toString")) {
                return "not implemented yet: task " + task.number();
            }
            throw new TaskNotImplementedException(task);
        });
    }
}
