
import type { CaseStatus, FraudScreening, MatchConfidence, Quality, ToolCall } from '../api'

export const QUALITY_LABEL: Record<Quality, string> = {
  GOOD: 'Looks good',
  ACCEPTABLE: 'Usable, with notes',
  POOR: 'Hard to read',
}

export const STATUS_LABEL: Record<CaseStatus, string> = {
  AWAITING_DOCUMENTS: 'Awaiting documents',
  NEEDS_REVIEW: 'Needs review',
  READY_FOR_DECISION: 'Ready for decision',
}

export const CONFIDENCE_LABEL: Record<MatchConfidence, string> = {
  HIGH: 'confident',
  MEDIUM: 'fairly sure',
  LOW: 'unsure',
}

export const INDICATOR_LABEL: Record<FraudScreening['indicators'][number]['kind'], string> = {
  ALREADY_UPLOADED: 'Sent before',
  EDITED_IN_SOFTWARE: 'Touched by an editor',
  NO_CAMERA_ORIGIN: 'No camera metadata',
  DATE_OUT_OF_PLACE: 'Capture date',
  ADDRESSED_THE_AGENT: 'Tried to instruct the agent',
}

export const TOOL_LABEL: Record<string, string> = {
  documentDetail: 'Looked up',
  readDocument: 'Read the file',
  proposeReview: 'Suggested reviewing',
  proposeDocumentRequest: 'Suggested asking for',
}

export const SUGGESTED_QUESTIONS = [
  'What is this case waiting on?',
  'Do any of the documents disagree with each other?',
  'Look at the poorest scan again — what can you make out?',
]

export function toolLabel(call: ToolCall): string {
  const name = TOOL_LABEL[call.name] ?? call.name
  try {
    const subject = Object.values(JSON.parse(call.arguments) as Record<string, unknown>)
      .map(String)
      .find((value) => value.length > 0)
    return subject ? `${name} ${subject}` : name
  } catch {
    return name
  }
}
