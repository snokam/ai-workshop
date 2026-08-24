import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listCases } from "../../api";
import type { CaseOverview } from "../../api";
import { STATUS_LABEL } from "../../lib/labels";
import { rememberedCaseIds } from "./openedCases";
import { Failure } from "../../components/feedback/Failure";
import { TaskGate } from "../../components/workshop/TaskGate";

export function MyCases() {
  const [cases, setCases] = useState<CaseOverview[] | null>(null);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const mine = new Set(rememberedCaseIds());
    listCases()
      .then((all) => setCases(all.filter((c) => mine.has(c.id))))
      .catch((e: Error) => setError(e));
  }, []);

  return (
    <>
      <header>
        <h1>My cases</h1>
        <p>
          The cases you have opened. Pick one to see what it still needs and
          send more in.
        </p>
      </header>

      {error && <Failure error={error} />}

      <TaskGate
        task="FIRST_AGENT"
        instead="This list is the cases task 1's agent opened. Until it is written there is nothing to list, however many times you try to report one."
      >
        <TaskGate
          task="DOCUMENT_AGENT"
          instead="Opening a case works, but sending documents into one does not yet — so every case here will sit at awaiting documents."
        >
          <section className="cases">
            {cases !== null && cases.length === 0 && (
              <p className="empty">
                You have not opened any cases yet. Report a new case to get
                started.
              </p>
            )}
            {cases?.map((c) => (
              <Link key={c.id} className="case-row" to={`/cases/${c.id}`}>
                <span className="reference">
                  {c.typeLabel}
                  <span className="case-reference"> · {c.reference}</span>
                </span>
                <span className={`status ${c.status.toLowerCase()}`}>
                  {STATUS_LABEL[c.status]}
                </span>
                <span className="outstanding-count">
                  {c.outstanding.length === 0
                    ? "Everything required has arrived"
                    : `Still needs ${c.outstanding.join(", ")}`}
                </span>
              </Link>
            ))}
          </section>
        </TaskGate>
      </TaskGate>
    </>
  );
}
