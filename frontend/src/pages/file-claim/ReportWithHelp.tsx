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
const SETTLE_MS = 450;

/**
 * Below this there is nothing to judge yet, so no call is made.
 *
 * Kept low on purpose. It was 20, which is longer than most real openings — "my bag is gone" is 14
 * characters and "my car got damaged" is 18, so the two descriptions most likely to need help were
 * the two that got none.
 *
 * Somebody who has typed less than this still gets an answer, just not from a model: silence at that
 * point reads as broken rather than as "keep going", and it is the one moment where the right thing
 * to say is known in advance.
 */
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

  // Nothing about the form waits on this. When the typing settles, whatever is in the box goes to a
  // streaming agent and its verdict is written in beside it a word at a time — is this enough to
  // work with, and if not what is missing. Somebody is writing, not waiting, so it has to arrive
  // while they are still at it to be worth anything.
  useEffect(() => {
    const text = description.trim();
    if (text.length < ENOUGH_TO_READ || questions.length > 0) {
      return;
    }
    const timer = setTimeout(() => {
      const controller = new AbortController();
      inFlight.current = controller;
      setJudging(true);
      // The previous answer stays up until the new one starts arriving. Blanking it first empties
      // the box for as long as the model takes to think, which reads as the help disappearing.
      let firstToken = true;
      void streamHelp(
        text,
        (token) => {
          // Tokens from a request that has been superseded are dropped. Without this they append to
          // whatever the current one has written, and two answers arrive woven together — marker
          // lines and all, because only the first of them ever gets stripped.
          if (inFlight.current !== controller) return;
          // The flag is flipped out here, never inside the updater. React calls a state updater
          // twice under StrictMode to catch exactly this: an updater that changes something outside
          // itself runs its side effect on the first call and then takes the second call's result.
          // The first token of a new answer would append to the old one instead of replacing it, so
          // two answers arrived stacked, marker line and all.
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
    // Typing again makes the answer in flight stale, so stop it here rather than when the next one
    // starts: between those two moments it is still writing into the box.
    return () => {
      clearTimeout(timer);
      inFlight.current?.abort();
    };
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

  // The answer opens with a line the reader never sees, carrying three slots: what happened, when,
  // and what was affected — each quoted from their own words, or dashed where their text does not
  // answer it. The verdict is not on that line, because it does not have to be: a description is
  // ready when none of the three is a dash, and counting to three is something this side does
  // perfectly and a small model does not. Asked for the verdict itself, it would emit "not enough"
  // with all three slots filled, or stop before writing it at all.
  const broke = feedback.indexOf("\n");
  const head = broke === -1 ? "" : feedback.slice(0, broke);
  const slotFilled = (slot: string) => {
    const found = head.match(new RegExp(`${slot}\\s*:([^|]*)`, "i"));
    const value = (found?.[1] ?? "").trim();
    return value.length > 0 && !/^[-—–]$/.test(value);
  };
  const verdict = broke === -1
    ? undefined
    : ["what", "when", "affected"].every(slotFilled)
      ? "ready"
      : "more";
  // Everything after that line is the message. Before it closes there is nothing to show yet.
  //
  // Any further marker line is stripped too. One should never arrive — they only did when two
  // answers were interleaving — but a marker reaching the reader is the worst way for that to
  // surface, so it is cheap to make impossible rather than unlikely.
  const said =
    broke === -1 ? "" : feedback.slice(broke + 1).replace(/^\s*lang\s*:[^\n]*/gim, "").trim();

  // One box, and once it is up it stays up.
  //
  // It used to be two elements — a local hint below the threshold, the model's answer above it —
  // which swapped as the text grew, so the help vanished and came back at the moment somebody was
  // reading it. They are one element now, and it never unmounts once shown: only its words change.

  // Not enough typed to be worth a call, so the answer comes from here instead of the model: it
  // costs nothing and lands on the keystroke rather than after the debounce.
  //
  // An empty box counts, on purpose. Clearing the text leaves the last answer — or a half-arrived
  // one from a request aborted mid-word — describing something that is no longer there, and a
  // spinner over that reads as stuck. There is nothing to judge, so it says so.
  const tooShortToJudge =
    questions.length === 0 && description.trim().length < ENOUGH_TO_READ;

  const nothingTypedYet = questions.length === 0 && description.trim().length === 0;
  const helpTone = tooShortToJudge ? undefined : verdict;
  const helpLabel = tooShortToJudge
    ? "Keep going"
    : verdict === "ready"
      ? "This is enough to go on"
      : verdict === "more"
        ? "Worth adding"
        : "Reading what you wrote";
  // What the slots say, in words, for when the model writes the hidden line and then stops.
  //
  // It does that perhaps one time in four on the small model, and no amount of telling it not to has
  // got that to zero. It does not have to: the line it did write says which of the three are
  // missing, and naming them is not a thing that needs a language model. The prose is nicer when it
  // arrives, and the box is never empty when it does not.
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
    : said || (judging ? <Loader /> : fromTheSlots);

  // Latched: once the box has been shown it is never taken away, only rewritten.
  const [helpShowing, setHelpShowing] = useState(false);
  useEffect(() => {
    if (!nothingTypedYet && questions.length === 0) {
      setHelpShowing(true);
    }
  }, [nothingTypedYet, questions.length]);

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
                  // Dimmed while a fresh answer is on its way, with the previous one still readable
                  // underneath. Better than emptying the box: the words that were there are usually
                  // still true, and they stop the layout jumping.
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
