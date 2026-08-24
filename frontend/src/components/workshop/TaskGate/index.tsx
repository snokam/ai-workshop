import { useEffect, useState, type ReactNode } from "react";
import { TASK_ATTEMPTED } from "../../../api/client";
import type { TaskKey } from "../../../api/workshop";
import { useTask } from "../../../lib/task-state";

/**
 * Wraps a control and, while its task is unwritten, explains what is missing underneath it.
 *
 * Children render before the panel, so a nested gate's panel appears above its parent's. When two
 * gates stack around the same control, put the *higher* task number on the outside — the boxes then
 * read 1, 2, 3 down the page rather than in the order somebody happened to nest them.
 */
export function TaskGate({
  task: key,
  children,
  instead,
}: {
  task: TaskKey;
  children: ReactNode;
  instead?: ReactNode;
}) {
  const task = useTask(key);
  const justTried = useJustTried(key);

  if (task === null || task.done) return <>{children}</>;

  return (
    <div className="task-pending">
      {children}

      <section
        className={`task-todo ${justTried ? "just-tried" : ""}`}
        role="status"
      >
        {justTried && (
          <p className="task-todo-tried">That is what you just tried.</p>
        )}
        <p className="task-todo-label">
          Task {task.number} — {task.title}
        </p>
        {instead && <p>{instead}</p>}
        <p>{task.todo}</p>
        <dl>
          <dt>Open</dt>
          <dd>
            <code>{task.file}</code>
          </dd>
          <dt>Brief</dt>
          <dd>
            <code>{task.brief}</code>
          </dd>
        </dl>
        <p className="task-todo-note">
          The controls above still work — using them is how you see what is
          missing. Everything else in the app is unaffected.
        </p>
      </section>
    </div>
  );
}

/** True for a few seconds after something on the page hit this task's missing agent. */
function useJustTried(key: TaskKey): boolean {
  const [tried, setTried] = useState(false);

  useEffect(() => {
    function onAttempt(event: Event) {
      if ((event as CustomEvent<TaskKey>).detail !== key) return;
      setTried(true);
    }
    window.addEventListener(TASK_ATTEMPTED, onAttempt);
    return () => window.removeEventListener(TASK_ATTEMPTED, onAttempt);
  }, [key]);

  useEffect(() => {
    if (!tried) return;
    const clear = setTimeout(() => setTried(false), 6000);
    return () => clearTimeout(clear);
  }, [tried]);

  return tried;
}
