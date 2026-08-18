package com.example.aiworkshop.workshop;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public final class UnfinishedTasks {
    private UnfinishedTasks() {}

    public static <T> T wire(Class<T> type, WorkshopTask task, boolean implemented, Supplier<T> agent) {
        return implemented ? agent.get() : stub(type, task);
    }

    /**
     * A stand-in for any interface, for when the task that builds the real one is not written.
     *
     * <p>{@code ChatModel} needs this as much as an agent does: without a model bean nothing in the
     * application can be constructed and it will not start at all, which is the one thing the
     * workshop cannot afford.
     */
    public static <T> T notWrittenYet(Class<T> type, WorkshopTask task) {
        return stub(type, task);
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, WorkshopTask task) {
        Class<?>[] faces = {type, AutoCloseable.class};
        return (T) Proxy.newProxyInstance(type.getClassLoader(), faces, (proxy, method, args) -> switch (method.getName()) {
            case "toString" -> "not implemented yet: task " + task.number();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "close" -> null;
            default -> throw new TaskNotImplementedException(task);
        });
    }
}
