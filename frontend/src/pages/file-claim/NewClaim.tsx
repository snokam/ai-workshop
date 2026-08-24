import { useState } from "react";
import { useNavigate, type NavigateFunction } from "react-router-dom";
import { createClaim, listClaimTypes } from "../../api";
import type { CreatedClaim, SupportedClaimType } from "../../api";
import { rememberClaim } from "./openedClaims";
import { useEffect } from "react";
import { Loader } from "../../components/feedback/Loader";
import { Failure } from "../../components/feedback/Failure";
import { TaskGate } from "../../components/workshop/TaskGate";

function openCreated(navigate: NavigateFunction, created: CreatedClaim) {
  rememberClaim(created.id);
  navigate(`/claims/${created.id}`, { state: { intro: created } });
}

export function NewClaim() {
  const navigate = useNavigate();
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [types, setTypes] = useState<SupportedClaimType[]>([]);

  useEffect(() => {
    let live = true;
    listClaimTypes()
      .then((t) => live && setTypes(t))
      .catch(() => {});
    return () => {
      live = false;
    };
  }, []);

  async function submit() {
    const text = description.trim();
    if (!text || submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      openCreated(navigate, await createClaim(text));
    } catch (e) {
      setError(e as Error);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <header>
        <h1>Report a claim</h1>
        <p>
          Describe what happened in your own words. An agent reads it, opens the
          right kind of claim for you, and tells you which documents you will
          need to send in.
        </p>
      </header>

      <TaskGate
        task="GUARDRAILS"
        instead="Anything typed here reaches the model, including a greeting or an empty box. Nothing yet refuses text nobody could open a claim from."
      >
        <TaskGate
          task="FIRST_AGENT"
          instead="Describing a situation is how a claim gets opened, and the agent that reads it has not been written yet."
        >
          <form
            className="describe"
            onSubmit={(e) => {
              e.preventDefault();
              void submit();
            }}
          >
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="For example: my suitcase never turned up after my flight home, and I had to buy clothes and toiletries."
              rows={5}
              disabled={submitting}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  void submit();
                }
              }}
            />
            <button
              type="submit"
              disabled={submitting || description.trim().length === 0}
            >
              {submitting ? (
                <span className="reading">
                  <Loader />
                  Opening your claim…
                </span>
              ) : (
                "Open the claim"
              )}
            </button>
          </form>
        </TaskGate>
      </TaskGate>

      {error && <Failure error={error} />}

      {types.length > 0 && (
        <section className="supported">
          <h2>Insurance we can help with</h2>
          <ul>
            {types.map((type) => (
              <li key={type.label}>
                <span className="supported-label">{type.label}</span>
                <span className="supported-desc">{type.description}</span>
              </li>
            ))}
          </ul>
        </section>
      )}
    </>
  );
}
