package com.example.aiworkshop.workshop;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

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
