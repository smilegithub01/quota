package com.bank.quota.core.dto.customerquota;

import lombok.Data;
import java.util.List;

@Data
public class CustomerQuotaQueryResponse {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<CustomerQuotaResponse> list;
}
