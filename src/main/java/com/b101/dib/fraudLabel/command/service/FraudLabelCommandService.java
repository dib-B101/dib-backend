package com.b101.dib.fraudLabel.command.service;

import com.b101.dib.fraudLabel.command.dto.CreateFraudLabelRequest;
import com.b101.dib.fraudLabel.domain.FraudLabel;

public interface FraudLabelCommandService {
    FraudLabel create(CreateFraudLabelRequest request);
}
