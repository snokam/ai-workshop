import { useEffect, useRef, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import {
  listCases,
  listDocumentRequests,
  listDocuments,
  uploadDocument,
} from "../../api";
import type {
  CaseOverview,
  CreatedCase,
  DocumentRequest,
  UploadedDocument,
} from "../../api";
import { Checklist } from "../../components/task_1_first_agent/Checklist";
import { DocumentCard } from "../../components/task_3_document_agent/DocumentCard";
import { CONFIDENCE_LABEL } from "../../lib/labels";
import { previewOf } from "../../components/task_3_document_agent/standing";
import { Loader } from "../../components/feedback/Loader";
import { Failure } from "../../components/feedback/Failure";
import { TaskGate } from "../../components/workshop/TaskGate";

export function Case() {
  const { caseId = "" } = useParams();
  const intro = (useLocation().state as { intro?: CreatedCase } | null)?.intro;
  const [overview, setOverview] = useState<CaseOverview | null>(null);
  const [documents, setDocuments] = useState<UploadedDocument[]>([]);
  const [askedFor, setAskedFor] = useState<DocumentRequest[]>([]);
  const [busyWith, setBusyWith] = useState<string | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const fileInput = useRef<HTMLInputElement>(null);

  const checklist: CaseOverview =
    overview ??
    (intro
      ? {
          id: intro.id,
          reference: intro.reference,
          typeLabel: intro.typeLabel,
          status: intro.status,
          requiredDocuments: intro.requiredDocuments,
          outstanding: intro.requiredDocuments,
        }
      : {
          id: caseId,
          reference: "",
          typeLabel: "",
          status: "AWAITING_DOCUMENTS",
          requiredDocuments: [],
          outstanding: [],
        });

  async function refreshOverview() {
    const all = await listCases();
    setOverview(all.find((c) => c.id === caseId) ?? null);
    setAskedFor(await listDocumentRequests(caseId));
  }

  useEffect(() => {
    function refresh() {
      refreshOverview().catch((e: Error) => setError(e));
    }

    refresh();
    listDocuments()
      .then((all) => setDocuments(all.filter((d) => d.caseId === caseId)))
      .catch((e: Error) => setError(e));

    const polling = setInterval(refresh, 5000);
    return () => clearInterval(polling);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [caseId]);

  async function handleFiles(files: FileList | null) {
    if (!files?.length) return;
    setError(null);

    for (const file of Array.from(files)) {
      setBusyWith(file.name);
      try {
        const uploaded = await uploadDocument(caseId, file);
        setDocuments((current) => [uploaded, ...current]);
        await refreshOverview();
      } catch (e) {
        setError(e as Error);
      } finally {
        setBusyWith(null);
      }
    }
    if (fileInput.current) fileInput.current.value = "";
  }

  return (
    <>
      {intro ? (
        <section className={`detected ${intro.confidence.toLowerCase()}`}>
          <span className="detected-label">We have opened a case for you</span>
          <h1>{intro.typeLabel}</h1>
          <p className="reference">{intro.reference}</p>
          <p className="rationale">
            {intro.rationale}{" "}
            <span className="confidence-note">
              — the agent is {CONFIDENCE_LABEL[intro.confidence]}
            </span>
          </p>
        </section>
      ) : (
        <header>
          <h1>{checklist.typeLabel || "Your case"}</h1>
          <p className="case-reference-line">{checklist.reference}</p>
        </header>
      )}

      <header>
        <h2>What to send in</h2>
        <p>
          Upload each of these and an agent reads it, tells you what it is, says
          which item it counts as, and whether the file is good enough to work
          with.
        </p>
      </header>

      {checklist.requiredDocuments.length > 0 ? (
        <Checklist
          chosen={checklist}
          alsoSent={documents.filter(
            (d) => !d.analysis.matchedRequiredDocument,
          )}
          askedFor={askedFor}
        />
      ) : (
        <p className="empty">
          This case has no set list of documents. Send in anything relevant.
        </p>
      )}

      <TaskGate
        task="DOCUMENT_AGENT"
        instead="A file can be dropped here, but the agent that reads an uploaded PDF or photo has not been written yet."
      >
        <label
          className={`dropzone ${busyWith ? "busy" : ""}`}
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault();
            void handleFiles(e.dataTransfer.files);
          }}
        >
          <input
            ref={fileInput}
            type="file"
            accept="application/pdf,image/*"
            multiple
            disabled={busyWith !== null}
            onChange={(e) => void handleFiles(e.target.files)}
          />
          {busyWith ? (
            <span className="reading">
              <Loader />
              Reading <strong>{busyWith}</strong>…
            </span>
          ) : (
            <span>
              <strong>Drop a PDF or a photo here</strong>
              <small>or click to choose a file</small>
            </span>
          )}
        </label>
      </TaskGate>

      {error && <Failure error={error} />}

      <section className="documents">
        {documents.length === 0 && !busyWith && (
          <p className="empty">Nothing uploaded yet.</p>
        )}
        {documents.map((doc) => (
          <DocumentCard key={doc.id} doc={doc} preview={previewOf(doc)} />
        ))}
      </section>
    </>
  );
}
