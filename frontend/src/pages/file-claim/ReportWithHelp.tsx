import { useEffect, useRef, useState } from "react";
import { useNavigate, type NavigateFunction } from "react-router-dom";
import {
  interviewIntake,
  streamHelp,
  type CreatedClaim,
  type InterviewAnswer,
} from "../../api";
import { Failure } from "../../components/feedback/Failure";
import { Loader } from "../../components/feedback/Loader";
import { TaskGate } from "../../components/workshop/TaskGate";
import { rememberClaim } from "./openedClaims";

/** Long enough not to chase every keystroke, short enough to land while they are still writing. */
const SETTLE_MS = 700;

/** Below this there is nothing to judge yet. */
const ENOUGH_TO_READ = 20;

function openCreated(navigate: NavigateFunction, created: CreatedClaim) {
  rememberClaim(created.id);
  navigate(`/claims/${created.id}`, { state: { intro: created } });
}

export function ReportWithHelp() {
  const navigate = useNavigate();
  const [description, setDescription] = useState("");
  const [answered, setAnswered] = useState<InterviewAnswer[]>([]);
  const [questions, setQuestions] = useState<string[]>([]);
  const [replies, setReplies] = useState<Record<number, string>>({});
  const [feedback, setFeedback] = useState("");
  const [judging, setJudging] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const inFlight = useRef<AbortController | null>(null);

  // Nothing about the form waits on this. When the typing settles, whatever is in the box goes to a
  // streaming agent and its verdict is written in beside it a word at a time — is this enough to
  // work with, and if not what is missing. Somebody is writing, not waiting, so it has to arrive
  // while they are still at it to be worth anything.
  useEffect(() => {
    const text = description.trim();
    if (text.length < ENOUGH_TO_READ || questions.length > 0) {
      setFeedback("");
      return;
    }
    const timer = setTimeout(() => {
      inFlight.current?.abort();
      const controller = new AbortController();
      inFlight.current = controller;
      setFeedback("");
      setJudging(true);
      void streamHelp(text, (t) => setFeedback((soFar) => soFar + t), controller.signal)
        .catch(() => undefined)
        .finally(() => setJudging(false));
    }, SETTLE_MS);
    return () => clearTimeout(timer);
  }, [description, questions.length]);

  // The form is dynamic: the agent reads what it has and either asks for more, as fields, or
  // decides and the claim is opened.
  async function send(nextAnswers: InterviewAnswer[]) {
    setBusy(true);
    setError(null);
    try {
      const response = await interviewIntake(description.trim(), nextAnswers);
      if (response.status === "DECIDED" && response.createdClaim) {
        openCreated(navigate, response.createdClaim);
        return;
      }
      setAnswered(nextAnswers);
      setQuestions(response.questions);
      setReplies({});
    } catch (e) {
      setError(e as Error);
    } finally {
      setBusy(false);
    }
  }

  function answer() {
    const additions = questions
      .map((question, i) => ({ question, answer: (replies[i] ?? "").trim() }))
      .filter((qa) => qa.answer.length > 0);
    if (additions.length === 0 || busy) return;
    void send([...answered, ...additions]);
  }

  const allAnswered =
    questions.length > 0 && questions.every((_, i) => (replies[i] ?? "").trim().length > 0);

  return (
    <>
      <header>
        <h1>Report a claim, with help</h1>
        <p>
          Describe what happened. Something reads it as you write and tells you whether it is enough
          to work with, and the form asks for anything still missing before the claim is opened.
        </p>
      </header>

      <TaskGate
        task="STREAMING_FORM_HELP"
        instead="Describing a situation is how this claim gets opened, and neither the agent that reads it nor the one that judges what you wrote has been written yet."
      >
        {questions.length === 0 ? (
          <form
            className="describe"
            onSubmit={(e) => {
              e.preventDefault();
              if (description.trim() && !busy) void send([]);
            }}
          >
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="For example: my suitcase never turned up after my flight home, and I had to buy clothes and toiletries."
              rows={5}
              disabled={busy}
            />

            {(feedback || judging) && (
              <aside className="typing-help" aria-live="polite">
                <span className="typing-help-who">About what you have written</span>
                {feedback || <Loader />}
              </aside>
            )}

            <button type="submit" disabled={busy || description.trim().length === 0}>
              {busy ? (
                <span className="reading">
                  <Loader />
                  Reading…
                </span>
              ) : (
                "Open the claim"
              )}
            </button>
          </form>
        ) : (
          <form
            className="chat-questions"
            onSubmit={(e) => {
              e.preventDefault();
              answer();
            }}
          >
            <p className="chat-note">
              A couple of things are still missing before this claim can be opened.
            </p>
            {questions.map((question, i) => (
              <label key={i} className="chat-question">
                <span>{question}</span>
                <input
                  type="text"
                  value={replies[i] ?? ""}
                  onChange={(e) => setReplies((r) => ({ ...r, [i]: e.target.value }))}
                  disabled={busy}
                  autoFocus={i === 0}
                />
              </label>
            ))}
            <button type="submit" disabled={busy || !allAnswered}>
              {busy ? (
                <span className="reading">
                  <Loader />
                  Reading…
                </span>
              ) : (
                "Send answers"
              )}
            </button>
          </form>
        )}
      </TaskGate>

      {error && <Failure error={error} />}
    </>
  );
}
