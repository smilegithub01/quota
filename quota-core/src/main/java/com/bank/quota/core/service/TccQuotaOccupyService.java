package com.bank.quota.core.service;

import com.bank.quota.core.dto.quotacontrol.QuotaOccupyRequest;
import com.bank.quota.core.dto.quotacontrol.QuotaOccupyResult;
import java.math.BigDecimal;

public interface TccQuotaOccupyService {

    QuotaOccupyResult tryOccupy(QuotaOccupyRequest request);

    boolean confirmOccupy(String xid);

    boolean cancelOccupy(String xid);

    QuotaOccupyResult occupyWithTcc(QuotaOccupyRequest request);
}
