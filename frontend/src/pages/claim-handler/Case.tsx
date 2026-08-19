import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { openCase, reviewDocument } from "../../api";
import type { CaseDetail } from "../../api";
import { CaseChat } from "../../components/task_6_chat/CaseChat";
import { Checklist } from "../../components/task_1_first_agent/Checklist";
import { DocumentCard } from "../../components/task_2_document_agent/DocumentCard";
import {
  previewOf,
  standingOf,
} from "../../components/task_2_document_agent/standing";
import { STATUS_LABEL } from "../../lib/labels";
import { PageWait } from "../../components/feedback/Loader";
import { Failure } from "../../components/feedback/Failure";
import { TaskGate } from "../../components/workshop/TaskGate";

export function Case() {
  const { caseId = "" } = useParams();
  const [detail, setDetail] = useState<CaseDetail | null>(null);
  const [error, setError] = useState<Error | null>(null);

  const read = useCallback(async () => {
    setError(null);
    try {
      setDetail(await openCase(caseId));
    } catch (e) {
      setError(e as Error);
    }
  }, [caseId]);

  useEffect(() => {
    void read();
  }, [read]);

  async function review(documentId: string) {
    setError(null);
    try {
      await reviewDocument(documentId);
      await read();
    } catch (e) {
      setError(e as Error);
    }
  }

  if (!detail) {
    return (
      <>
        {error ? (
          <Failure error={error} />
        ) : (
          <PageWait>Reading the case…</PageWait>
        )}
      </>
    );
  }

  const { overview } = detail;

  return (
    <>
      <div className="with-chat">
        <div className="case-contents">
          <header>
            <h1>{overview.typeLabel}</h1>
            <p className="case-reference-line">{overview.reference}</p>
            <p className={`status ${overview.status.toLowerCase()}`}>
              {STATUS_LABEL[overview.status]}
            </p>
          </header>

          {error && <Failure error={error} />}

          <section className="agent-prose">
            <h2>Where this stands</h2>
            <p>{detail.statusNote}</p>
          </section>

          <Checklist
            chosen={overview}
            askedFor={detail.documentRequests}
            askedForHeading="You have also asked for"
          />

          <TaskGate
            task="SUMMARY"
            instead="This is what every document on the case says when read together, which is its own agent and has not been written yet."
          >
            <section className="agent-prose">
              <h2>Across the documents</h2>
              <p>{detail.summary}</p>
            </section>
          </TaskGate>

          <TaskGate
            task="POSTPROCESSING"
            instead="Nothing below has been screened. The checks that catch a duplicate upload, an edited photo or a figure that does not add up are plain Java after the answer, and you have not written them yet."
          >
            <section className="documents">
              {detail.documents.length === 0 && (
                <p className="empty">Nothing uploaded to this case yet.</p>
              )}
              {detail.documents.map((doc) => (
                <DocumentCard
                  key={doc.id}
                  doc={doc}
                  preview={previewOf(doc)}
                  standing={standingOf(doc, detail)}
                  blocking={detail.blockedDocumentIds.includes(doc.id)}
                  screening={detail.screenings.find(
                    (s) => s.documentId === doc.id,
                  )}
                  onReview={() => void review(doc.id)}
                />
              ))}
            </section>
          </TaskGate>
        </div>

        <CaseChat detail={detail} onCaseChanged={read} />
      </div>
    </>
  );
}
