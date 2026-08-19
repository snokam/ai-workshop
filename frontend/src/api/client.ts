import type { TaskKey } from "./workshop";

export interface TaskNotImplemented {
  taskNotImplemented: true;
  task: number;
  key: TaskKey;
  title: string;
  file: string;
  todo: string;
  brief: string;
}

export class TaskNotImplementedError extends Error {
  detail: TaskNotImplemented;

  constructor(detail: TaskNotImplemented) {
    super(`Task ${detail.task} (${detail.title}) is not implemented yet`);
    this.name = "TaskNotImplementedError";
    this.detail = detail;
  }
}

export function isTaskNotImplemented(e: unknown): e is TaskNotImplementedError {
  return e instanceof TaskNotImplementedError;
}

/**
 * Announced whenever something is tried that an unwritten task would have done.
 *
 * A screen already carries the standing explanation of what is missing, so repeating it under the
 * button would say the same thing twice. But a press that does nothing at all reads as a broken
 * button rather than an unwritten agent, so the explanation already on the page answers instead.
 */
export const TASK_ATTEMPTED = "workshop:task-attempted";

function announce(detail: TaskNotImplemented) {
  if (typeof window !== "undefined") {
    window.dispatchEvent(
      new CustomEvent(TASK_ATTEMPTED, { detail: detail.key }),
    );
  }
}

export async function failureMessage(response: Response): Promise<string> {
  try {
    const body = await response.json();
    if (body && typeof body.message === "string") return body.message;
  } catch {}
  return `${response.status} ${response.statusText}`;
}

export async function json<T>(response: Response): Promise<T> {
  if (!response.ok) throw await failure(response);
  return response.json() as Promise<T>;
}

export async function failure(response: Response): Promise<Error> {
  if (response.status === 501) {
    try {
      const body = (await response.clone().json()) as TaskNotImplemented;
      if (body?.taskNotImplemented) {
        announce(body);
        return new TaskNotImplementedError(body);
      }
    } catch {}
  }
  return new Error(await failureMessage(response));
}

export interface SupportedCaseType {
  label: string;
  description: string;
}
