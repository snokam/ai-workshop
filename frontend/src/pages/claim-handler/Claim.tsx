import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { openClaim, reviewDocument } from "../../api";
import type { ClaimDetail } from "../../api";
import { ClaimChat } from "../../components/task_6_advisor_chat_with_tools_and_memory/ClaimChat";
import { Checklist } from "../../components/task_1_first_agent/Checklist";
import { DocumentCard } from "../../components/task_3_document_agent/DocumentCard";
import {
  previewOf,
  standingOf,
} from "../../components/task_3_document_agent/standing";
import { STATUS_LABEL } from "../../lib/labels";
import { PageWait } from "../../components/feedback/Loader";
import { Failure } from "../../components/feedback/Failure";
import { TaskGate } from "../../components/workshop/TaskGate";

export function Claim() {
  const { claimId = "" } = useParams();
  const [detail, setDetail] = useState<ClaimDetail | null>(null);
  const [error, setError] = useState<Error | null>(null);

  const read = useCallback(async () => {
    setError(null);
    try {
      setDetail(await openClaim(claimId));
    } catch (e) {
      setError(e as Error);
    }
  }, [claimId]);

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
          <PageWait>Reading the claim…</PageWait>
        )}
      </>
    );
  }

  const { overview } = detail;

  return (
    <>
      <div className="with-chat">
        <div className="claim-contents">
          <header>
            <h1>{overview.typeLabel}</h1>
            <p className="claim-reference-line">{overview.reference}</p>
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
            task="CLAIM_SUMMARY_CHOOSING_MODELS"
            instead="This is what every document on the claim says when read together, which is its own agent and has not been written yet."
          >
            <section className="agent-prose">
              <h2>Across the documents</h2>
              <p>{detail.summary}</p>
            </section>
          </TaskGate>

          <TaskGate
            task="DOCUMENT_AGENT"
            instead="Anything listed below was read by task 3's agent. Until that is written no documents reach a claim, so this list stays empty."
          >
            <section className="documents">
              {detail.documents.length === 0 && (
                <p className="empty">Nothing uploaded to this claim yet.</p>
              )}
              {detail.documents.map((doc) => (
                <DocumentCard
                  key={doc.id}
                  doc={doc}
                  preview={previewOf(doc)}
                  standing={standingOf(doc, detail)}
                  blocking={detail.blockedDocumentIds.includes(doc.id)}
                  onReview={() => void review(doc.id)}
                />
              ))}
            </section>
          </TaskGate>
        </div>

        <ClaimChat detail={detail} onClaimChanged={read} />
      </div>
    </>
  );
}
