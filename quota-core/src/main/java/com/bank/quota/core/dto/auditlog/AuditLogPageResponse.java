package com.bank.quota.core.dto.auditlog;

import lombok.Data;
import java.util.List;

@Data
public class AuditLogPageResponse {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<AuditLogQueryResponse> list;
}
