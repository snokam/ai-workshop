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

const SETTLE_MS = 1100;

const ENOUGH_TO_READ = 8;

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

  useEffect(() => {
    const text = description.trim();
    if (text.length < ENOUGH_TO_READ || questions.length > 0) {
      return;
    }
    const timer = setTimeout(() => {
      const controller = new AbortController();
      inFlight.current = controller;
      setJudging(true);
      let firstToken = true;
      void streamHelp(
        text,
        (token) => {
          if (inFlight.current !== controller) return;
          if (firstToken) {
            firstToken = false;
            setFeedback(token);
          } else {
            setFeedback((soFar) => soFar + token);
          }
        },
        controller.signal,
      )
        .catch(() => undefined)
        .finally(() => {
          if (inFlight.current === controller) setJudging(false);
        });
    }, SETTLE_MS);
    return () => {
      clearTimeout(timer);
      inFlight.current?.abort();
    };
  }, [description, questions.length]);

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

  const broke = feedback.indexOf("\n");
  const head = broke === -1 ? "" : feedback.slice(0, broke);
  const slotFilled = (slot: string) => {
    const found = head.match(new RegExp(`${slot}\\s*:([^|]*)`, "i"));
    const value = (found?.[1] ?? "").trim();
    return value.length > 0 && !/^[-—–]$/.test(value);
  };
  const verdict =
    broke === -1
      ? undefined
      : ["what", "when", "affected"].every(slotFilled)
        ? "ready"
        : "more";
  const said =
    broke === -1
      ? ""
      : feedback
          .slice(broke + 1)
          .replace(/^\s*lang\s*:[^\n]*/gim, "")
          .trim();

  const tooShortToJudge =
    questions.length === 0 && description.trim().length < ENOUGH_TO_READ;

  const nothingTypedYet =
    questions.length === 0 && description.trim().length === 0;
  const helpTone = tooShortToJudge ? undefined : verdict;
  const helpLabel = tooShortToJudge
    ? "Keep going"
    : verdict === "ready"
      ? "This is enough to go on"
      : verdict === "more"
        ? "Worth adding"
        : "Reading what you wrote";
  const missingSlots = [
    { slot: "what", said: "what happened" },
    { slot: "when", said: "roughly when it happened" },
    { slot: "affected", said: "what was damaged, lost or hurt" },
  ]
    .filter(({ slot }) => !slotFilled(slot))
    .map(({ said: name }) => name);

  const fromTheSlots =
    missingSlots.length === 0
      ? "You have said what happened, when, and what was affected — that is enough to open the claim."
      : missingSlots.length === 3
        ? "I cannot tell what has happened yet. Describe it in a sentence or two."
        : `Still worth adding: ${missingSlots.join(", ")}.`;

  const helpBody = tooShortToJudge
    ? "A sentence or two: what happened, roughly when, and what was affected."
    : broke !== -1 && missingSlots.length > 0
      ? fromTheSlots
      : said ||
        (judging ? (
          <Loader />
        ) : (
          "Keep writing — what happened, roughly when, and what was affected."
        ));

  const [helpShowing, setHelpShowing] = useState(false);
  useEffect(() => {
    if (!nothingTypedYet && questions.length === 0) {
      setHelpShowing(true);
    }
  }, [nothingTypedYet, questions.length]);

  const allAnswered =
    questions.length > 0 &&
    questions.every((_, i) => (replies[i] ?? "").trim().length > 0);

  return (
    <>
      <header>
        <h1>Report a claim, with help</h1>
        <p>
          Describe what happened. Something reads it as you write and tells you
          whether it is enough to work with, and the form asks for anything
          still missing before the claim is opened.
        </p>
      </header>

      <TaskGate
        task="DYNAMIC_FORM_WITH_STREAMING"
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

            {helpShowing && (
              <aside
                className={[
                  "typing-help",
                  helpTone ?? "",
                  judging && said ? "working" : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
                aria-live="polite"
              >
                <span className="typing-help-who">{helpLabel}</span>
                {helpBody}
              </aside>
            )}

            <button
              type="submit"
              disabled={busy || description.trim().length === 0}
            >
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
              A couple of things are still missing before this claim can be
              opened.
            </p>
            {questions.map((question, i) => (
              <label key={i} className="chat-question">
                <span>{question}</span>
                <input
                  type="text"
                  value={replies[i] ?? ""}
                  onChange={(e) =>
                    setReplies((r) => ({ ...r, [i]: e.target.value }))
                  }
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
