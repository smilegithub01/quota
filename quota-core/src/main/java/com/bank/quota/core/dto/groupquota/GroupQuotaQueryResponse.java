package com.bank.quota.core.dto.groupquota;

import lombok.Data;
import java.util.List;

@Data
public class GroupQuotaQueryResponse {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<GroupQuotaResponse> list;
}
