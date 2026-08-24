package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.workshop.TaskNotImplementedException;
import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.tasks.task_3_document_agent.model.ManipulationAttempt;
import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Weight;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AddressedTheAgentCheck implements FraudCheck {

    @Override
    public List<Indicator> screen(Upload upload) {
        // ManipulationAttempt attempt = upload.analysis().manipulationAttempt();
        // if (attempt == null || attempt.attemptedInstruction() == null) {
        // return List.of();
        // }
        // return List.of(new Indicator(
        // Kind.ADDRESSED_THE_AGENT,
        // Weight.STRONG,
        // "The document contains text aimed at the software reading it: " + attempt.attemptedInstruction(),
        // attempt.quote() == null ? List.of() : List.of("The document says: " + attempt.quote())));
        //
        // 

        throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);

        // ── To set this task again ────────────────────────────────────────────────────────
        // TODO — task 4. The report the agent already made.
        //
        // The intake agent was asked to record any text in the document aimed at whatever software
        // reads it. upload.analysis() carries what it found. Nothing here calls a model: the
        // reading was done, this only decides what it is worth.
        //
        // Throwing is how the screener knows: it logs, skips, and keeps the other checks running —
        // which is the rule this task is really about.
        // throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);
    }
}
