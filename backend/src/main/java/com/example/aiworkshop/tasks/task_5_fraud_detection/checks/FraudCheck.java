package com.example.aiworkshop.tasks.task_5_fraud_detection.checks;

import com.example.aiworkshop.tasks.task_5_fraud_detection.FraudScreener.Upload;
import com.example.aiworkshop.tasks.task_5_fraud_detection.model.FraudScreening.Indicator;
import java.util.List;

public interface FraudCheck {

    List<Indicator> screen(Upload upload);
}
