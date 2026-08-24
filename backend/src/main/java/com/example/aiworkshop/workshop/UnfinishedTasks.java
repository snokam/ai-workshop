package com.example.aiworkshop.workshop;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.SystemMessage;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

/**
 * Whether a task has been written, and what to hand back while it has not.
 *
 * <p>Nothing declares this. An agent is unwritten while its {@code @SystemMessage} still holds the
 * paragraph it shipped with, and everything else is unwritten while it still throws {@link
 * TaskNotImplementedException}. Both are the code itself saying so, which is the point: a flag is
 * one more thing to forget, and a flag that disagrees with the code is worse than no flag.
 */
public final class UnfinishedTasks {

    private static final String UNWRITTEN = "TODO — task";

    private UnfinishedTasks() {}

    /** True while any of the type's prompts is still the one it shipped with. */
    public static boolean promptWritten(Class<?> agent) {
        for (Method method : agent.getMethods()) {
            SystemMessage prompt = method.getAnnotation(SystemMessage.class);
            if (prompt != null && String.join("\n", prompt.value()).contains(UNWRITTEN)) {
                return false;
            }
        }
        return true;
    }

    /**
     * True while every {@code @Description} on the record has been written.
     *
     * <p>Read off the field rather than the record component: {@code @Description} is declared
     * {@code @Target({FIELD, TYPE})}, so asking the component or the accessor returns null and the
     * check would quietly pass for everything.
     */
    public static boolean descriptionsWritten(Class<?> record) {
        for (RecordComponent component : record.getRecordComponents()) {
            try {
                Description described = record.getDeclaredField(component.getName())
                        .getAnnotation(Description.class);
                if (described != null && String.join(" ", described.value()).contains("TODO")) {
                    return false;
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("a record component always has a field", e);
            }
        }
        return true;
    }

    /** True unless calling this says outright that it has not been written. */
    public static boolean written(Runnable probe) {
        try {
            probe.run();
            return true;
        } catch (TaskNotImplementedException e) {
            return false;
        } catch (RuntimeException e) {
            return true;
        }
    }

    /**
     * The agent, or something of the same type that says which file to open.
     *
     * <p>The stand-in is what keeps the application startable: an unwritten task stops the one
     * feature it provides, not the context.
     */
    public static <T> T wire(Class<T> type, WorkshopTask task, Supplier<T> agent) {
        return promptWritten(type) ? agent.get() : notWrittenYet(type, task);
    }

    public static <T> T notWrittenYet(Class<T> type, WorkshopTask task) {
        return stub(type, task);
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type, WorkshopTask task) {
        Class<?>[] faces = {type, AutoCloseable.class};
        return (T) Proxy.newProxyInstance(type.getClassLoader(), faces, (proxy, method, args) -> switch (method.getName()) {
            case "toString" -> "not written yet: task " + task.number();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "close" -> null;
            default -> throw new TaskNotImplementedException(task);
        });
    }
}
