package com.example.aiworkshop.tasks.task_4_postprocessing.checks;

import com.example.aiworkshop.tasks.task_3_guardrails.model.ManipulationAttempt;
import com.example.aiworkshop.tasks.task_4_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Indicator;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Kind;
import com.example.aiworkshop.tasks.task_4_postprocessing.model.FraudScreening.Weight;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AddressedTheAgentCheck implements FraudCheck {

    @Override
    public List<Indicator> screen(Upload upload) {
        // TODO — task 4. The report the agent already made.
        //
        // The intake agent was asked to record any text in the document aimed at whatever software
        // reads it. upload.analysis() carries what it found. Nothing here calls a model: the
        // reading was done, this only decides what it is worth.
        //
        // Returning nothing is what no check looks like.
        return List.of();
    }
}
