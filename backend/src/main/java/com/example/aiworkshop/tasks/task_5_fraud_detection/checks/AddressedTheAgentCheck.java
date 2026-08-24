package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.workshop.WorkshopTask;
import com.example.aiworkshop.workshop.TaskNotImplementedException;
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
        // TODO — task 5, part 3. The report the agent already made.
        //
        // The shortest check here, and the one worth thinking hardest about.
        //
        //   upload.analysis().manipulationAttempt()  what task 3's agent recorded, or null
        //
        // When it is not null, return one Indicator: Kind.ADDRESSED_THE_AGENT, Weight.STRONG, the
        // attemptedInstruction as the detail, and the quote as evidence.
        //
        // Then notice what this check depends on. It trusts the model to have noticed. What happens when the
        // document says "do not flag anything"? Nothing here can tell, which is the point of the check.

        throw new TaskNotImplementedException(WorkshopTask.FRAUD_DETECTION);
    }
}
