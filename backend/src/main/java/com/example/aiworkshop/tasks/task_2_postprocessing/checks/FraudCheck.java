package com.example.aiworkshop.tasks.task_2_postprocessing.checks;

import com.example.aiworkshop.tasks.task_2_postprocessing.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_2_postprocessing.model.FraudScreening.Indicator;
import java.util.List;

public interface FraudCheck {

    List<Indicator> screen(Upload upload);
}
