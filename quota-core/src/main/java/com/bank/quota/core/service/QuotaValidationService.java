package com.bank.quota.core.service;

import com.bank.quota.core.dto.quotacontrol.QuotaValidateRequest;
import com.bank.quota.core.dto.quotacontrol.QuotaValidateResult;
import java.util.List;

public interface QuotaValidationService {

    QuotaValidateResult validate(QuotaValidateRequest request);

    QuotaValidateResult validateByLevel(QuotaValidateRequest request, int level);

    List<QuotaValidateResult> validateAllLevels(QuotaValidateRequest request);

    void reloadRules();

    void clearRuleCache();
}
