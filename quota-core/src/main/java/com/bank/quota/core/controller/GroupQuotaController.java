package com.bank.quota.core.controller;

import com.bank.quota.common.result.PageResult;
import com.bank.quota.common.result.Result;
import com.bank.quota.core.dto.groupquota.*;
import com.bank.quota.core.service.GroupQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 集团限额管理控制器
 * 
 * <p>提供银行级信贷额度管控平台的集团限额管理REST API接口。
 * 集团限额是整个额度体系的顶层，控制着银行对所有集团客户的最高总授信限额。</p>
 * 
 * <h3>功能概述：</h3>
 * <ul>
 *   <li>集团限额的创建、查询、更新、删除</li>
 *   <li>集团限额的冻结、解冻、停用操作</li>
 *   <li>集团限额的金额调整</li>
 *   <li>集团子限额的管理</li>
 *   <li>集团限额使用情况查询</li>
 * </ul>
 * 
 * <h3>业务规则：</h3>
 * <ul>
 *   <li>集团限额创建后默认状态为"有效"</li>
 *   <li>冻结状态下不允许额度占用操作</li>
 *   <li>停用状态下不允许任何操作</li>
 *   <li>调整金额时正数为增加，负数为减少</li>
 * </ul>
 * 
 * <h3>权限要求：</h3>
 * <p>所有接口需要用户认证，部分敏感操作需要特定权限。</p>
 * 
 * @author Quota System Team
 * @version 1.0.0
 * @since 2026-02-10
 * @see GroupQuotaService
 * @see GroupQuotaResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/group-quota")
@RequiredArgsConstructor
@Tag(name = "集团限额管理", description = "集团限额创建、查询、调整、冻结等操作接口")
public class GroupQuotaController {
    
    private final GroupQuotaService groupQuotaService;

    /**
     * 创建集团限额
     * 
     * <p>创建新的集团限额记录。集团限额是额度体系的顶层控制，
     * 定义了银行对特定集团客户的最高授信总额。</p>
     * 
     * <h4>业务规则：</h4>
     * <ul>
     *   <li>集团ID不能重复</li>
     *   <li>总额度必须大于0</li>
     *   <li>创建后默认状态为"有效"</li>
     * </ul>
     * 
     * <h4>请求示例：</h4>
     * <pre>{@code
     * {
     *   "groupId": 1001,
     *   "groupName": "测试集团",
     *   "totalQuota": 10000000.00,
     *   "description": "测试集团额度",
     *   "createBy": "admin"
     * }
     * }</pre>
     * 
     * @param request 创建集团限额请求
     * @return 创建成功的集团限额信息
     */
    @PostMapping
    @Operation(
        summary = "创建集团限额", 
        description = "创建新的集团限额记录，定义集团客户的最高授信总额"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(schema = @Schema(implementation = GroupQuotaResponse.class))),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "409", description = "集团限额已存在")
    })
    public Result<GroupQuotaResponse> createGroupQuota(
            @Valid @RequestBody CreateGroupQuotaRequest request) {
        log.info("Received create group quota request: groupId={}, groupName={}", 
                request.getGroupId(), request.getGroupName());
        
        GroupQuotaResponse response = groupQuotaService.createGroupQuota(request);
        return Result.success(response);
    }

    /**
     * 更新集团限额
     * 
     * <p>更新已存在的集团限额基本信息，如名称、描述等。
     * 不支持通过此接口修改额度金额，金额调整需使用专门的调整接口。</p>
     * 
     * @param groupId 集团ID
     * @param request 更新请求
     * @return 更新后的集团限额信息
     */
    @PutMapping("/{groupId}")
    @Operation(
        summary = "更新集团限额", 
        description = "更新集团限额的基本信息（名称、描述等），不支持修改额度金额"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在")
    })
    public Result<GroupQuotaResponse> updateGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupQuotaRequest request) {
        log.info("Received update group quota request: groupId={}", groupId);
        
        GroupQuotaResponse response = groupQuotaService.updateGroupQuota(groupId, request);
        return Result.success(response);
    }

    /**
     * 冻结集团限额
     * 
     * <p>冻结指定的集团限额。冻结后该集团下所有客户的额度操作将被暂停，
     * 包括额度占用、锁定等操作。通常用于风险控制场景。</p>
     * 
     * <h4>冻结影响：</h4>
     * <ul>
     *   <li>暂停所有额度占用操作</li>
     *   <li>暂停所有额度锁定操作</li>
     *   <li>不影响已占用的额度</li>
     *   <li>可随时解冻恢复</li>
     * </ul>
     * 
     * @param groupId 集团ID
     * @param reason 冻结原因
     * @param operator 操作人
     * @return 冻结后的集团限额信息
     */
    @PostMapping("/{groupId}/freeze")
    @Operation(
        summary = "冻结集团限额", 
        description = "冻结指定的集团限额，暂停所有额度操作"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "冻结成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在"),
        @ApiResponse(responseCode = "400", description = "集团限额已冻结或已停用")
    })
    public Result<GroupQuotaResponse> freezeGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "冻结原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received freeze group quota request: groupId={}, reason={}", groupId, reason);
        
        GroupQuotaResponse response = groupQuotaService.freezeGroupQuota(groupId, reason, operator);
        return Result.success(response);
    }

    /**
     * 解冻集团限额
     * 
     * <p>解冻已冻结的集团限额，恢复正常额度操作。
     * 只有处于冻结状态的集团限额才能执行解冻操作。</p>
     * 
     * @param groupId 集团ID
     * @param operator 操作人
     * @return 解冻后的集团限额信息
     */
    @PostMapping("/{groupId}/unfreeze")
    @Operation(
        summary = "解冻集团限额", 
        description = "解冻已冻结的集团限额，恢复正常额度操作"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "解冻成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在"),
        @ApiResponse(responseCode = "400", description = "集团限额未冻结")
    })
    public Result<GroupQuotaResponse> unfreezeGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received unfreeze group quota request: groupId={}", groupId);
        
        GroupQuotaResponse response = groupQuotaService.unfreezeGroupQuota(groupId, operator);
        return Result.success(response);
    }

    /**
     * 停用集团限额
     * 
     * <p>停用指定的集团限额。停用是不可逆操作，停用后集团限额将无法恢复。
     * 通常用于集团客户关系终止等场景。</p>
     * 
     * <h4>停用影响：</h4>
     * <ul>
     *   <li>永久停止所有额度操作</li>
     *   <li>不可恢复</li>
     *   <li>已占用额度需单独处理</li>
     * </ul>
     * 
     * @param groupId 集团ID
     * @param reason 停用原因
     * @param operator 操作人
     * @return 停用后的集团限额信息
     */
    @PostMapping("/{groupId}/disable")
    @Operation(
        summary = "停用集团限额", 
        description = "停用指定的集团限额（不可逆操作）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "停用成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在"),
        @ApiResponse(responseCode = "400", description = "集团限额已停用")
    })
    public Result<GroupQuotaResponse> disableGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "停用原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received disable group quota request: groupId={}, reason={}", groupId, reason);
        
        GroupQuotaResponse response = groupQuotaService.disableGroupQuota(groupId, reason, operator);
        return Result.success(response);
    }

    /**
     * 查询集团限额详情
     * 
     * <p>根据集团ID查询集团限额的详细信息，包括总额度、已用额度、可用额度等。</p>
     * 
     * @param groupId 集团ID
     * @return 集团限额详情
     */
    @GetMapping("/{groupId}")
    @Operation(
        summary = "查询集团限额详情", 
        description = "根据集团ID查询集团限额的详细信息"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在")
    })
    public Result<GroupQuotaResponse> getGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId) {
        log.info("Received get group quota request: groupId={}", groupId);
        
        GroupQuotaResponse response = groupQuotaService.getGroupQuota(groupId);
        return Result.success(response);
    }

    /**
     * 查询所有集团限额
     * 
     * <p>查询系统中所有集团限额列表，包括所有状态的记录。</p>
     * 
     * @return 集团限额列表
     */
    @GetMapping
    @Operation(
        summary = "查询所有集团限额", 
        description = "查询系统中所有集团限额列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<GroupQuotaResponse>> getAllGroupQuotas() {
        log.info("Received get all group quotas request");
        
        List<GroupQuotaResponse> responses = groupQuotaService.getAllGroupQuotas();
        return Result.success(responses);
    }

    /**
     * 查询有效集团限额
     * 
     * <p>查询所有状态为"有效"的集团限额列表。
     * 有效状态的集团限额可以进行正常的额度操作。</p>
     * 
     * @return 有效集团限额列表
     */
    @GetMapping("/enabled")
    @Operation(
        summary = "查询有效集团限额", 
        description = "查询所有状态为有效的集团限额列表"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<List<GroupQuotaResponse>> getEnabledGroupQuotas() {
        log.info("Received get enabled group quotas request");
        
        List<GroupQuotaResponse> responses = groupQuotaService.getEnabledGroupQuotas();
        return Result.success(responses);
    }

    /**
     * 分页查询集团限额
     * 
     * <p>支持条件筛选和分页的集团限额查询接口。
     * 可按状态、名称等条件进行筛选。</p>
     * 
     * @param request 查询请求
     * @return 分页查询结果
     */
    @PostMapping("/query")
    @Operation(
        summary = "分页查询集团限额", 
        description = "支持条件筛选和分页的集团限额查询"
    )
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<GroupQuotaQueryResponse> queryGroupQuotas(
            @RequestBody GroupQuotaQueryRequest request) {
        log.info("Received query group quotas request: pageNum={}, pageSize={}", 
                request.getPageNum(), request.getPageSize());
        
        GroupQuotaQueryResponse response = groupQuotaService.queryGroupQuotas(request);
        return Result.success(response);
    }

    /**
     * 查询集团限额使用情况
     * 
     * <p>查询指定集团限额的使用情况，包括使用率、各客户使用明细等。
     * 用于监控集团额度使用状态和风险预警。</p>
     * 
     * @param groupId 集团ID
     * @return 额度使用情况
     */
    @GetMapping("/{groupId}/usage")
    @Operation(
        summary = "查询集团限额使用情况", 
        description = "查询集团限额的使用率及各客户使用明细"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在")
    })
    public Result<GroupQuotaUsageResponse> getGroupQuotaUsage(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId) {
        log.info("Received get group quota usage request: groupId={}", groupId);
        
        GroupQuotaUsageResponse response = groupQuotaService.getGroupQuotaUsage(groupId);
        return Result.success(response);
    }

    /**
     * 创建集团子限额
     * 
     * <p>为集团创建子限额记录。子限额是集团总限额按风险类型拆分的细项限额，
     * 如敞口限额、低风险限额等。</p>
     * 
     * <h4>业务规则：</h4>
     * <ul>
     *   <li>子限额总额不能超过集团总限额</li>
     *   <li>同一类型子限额不能重复创建</li>
     * </ul>
     * 
     * @param groupId 集团ID
     * @param request 创建请求
     * @return 创建成功的子限额信息
     */
    @PostMapping("/{groupId}/sub-quota")
    @Operation(
        summary = "创建集团子限额", 
        description = "为集团创建子限额记录（如敞口限额、低风险限额等）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在"),
        @ApiResponse(responseCode = "400", description = "子限额超过总额限制")
    })
    public Result<GroupQuotaSubResponse> createGroupQuotaSub(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupQuotaSubRequest request) {
        log.info("Received create group quota sub request: groupId={}, subType={}", 
                groupId, request.getSubType());
        
        GroupQuotaResponse groupQuota = groupQuotaService.getGroupQuota(groupId);
        GroupQuotaSubResponse response = groupQuotaService.createGroupQuotaSub(
                groupQuota.getId(), request);
        return Result.success(response);
    }

    /**
     * 更新集团子限额
     * 
     * <p>更新已存在的集团子限额信息。</p>
     * 
     * @param subId 子限额ID
     * @param request 更新请求
     * @return 更新后的子限额信息
     */
    @PutMapping("/sub-quota/{subId}")
    @Operation(
        summary = "更新集团子限额", 
        description = "更新集团子限额信息"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "子限额不存在")
    })
    public Result<GroupQuotaSubResponse> updateGroupQuotaSub(
            @Parameter(description = "子限额ID", required = true) @PathVariable Long subId,
            @Valid @RequestBody UpdateGroupQuotaSubRequest request) {
        log.info("Received update group quota sub request: subId={}", subId);
        
        GroupQuotaSubResponse response = groupQuotaService.updateGroupQuotaSub(subId, request);
        return Result.success(response);
    }

    /**
     * 查询集团子限额列表
     * 
     * <p>查询指定集团的所有子限额记录。</p>
     * 
     * @param groupId 集团ID
     * @return 子限额列表
     */
    @GetMapping("/{groupId}/sub-quotas")
    @Operation(
        summary = "查询集团子限额列表", 
        description = "查询指定集团的所有子限额记录"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在")
    })
    public Result<List<GroupQuotaSubResponse>> getGroupQuotaSubs(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId) {
        log.info("Received get group quota subs request: groupId={}", groupId);
        
        GroupQuotaResponse groupQuota = groupQuotaService.getGroupQuota(groupId);
        List<GroupQuotaSubResponse> responses = groupQuotaService.getGroupQuotaSubs(groupQuota.getId());
        return Result.success(responses);
    }

    /**
     * 调整集团限额
     * 
     * <p>调整集团限额的金额。支持增加和减少两种操作。
     * 调整时会记录调整原因和操作人，用于审计追溯。</p>
     * 
     * <h4>调整规则：</h4>
     * <ul>
     *   <li>正数表示增加额度</li>
     *   <li>负数表示减少额度</li>
     *   <li>减少后的可用额度不能为负</li>
     *   <li>冻结状态下不允许调整</li>
     * </ul>
     * 
     * @param groupId 集团ID
     * @param adjustmentAmount 调整金额（正数增加，负数减少）
     * @param reason 调整原因
     * @param operator 操作人
     * @return 操作结果
     */
    @PostMapping("/{groupId}/adjust")
    @Operation(
        summary = "调整集团限额", 
        description = "调整集团限额金额（正数增加，负数减少）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "调整成功"),
        @ApiResponse(responseCode = "404", description = "集团限额不存在"),
        @ApiResponse(responseCode = "400", description = "调整金额无效或状态不允许调整")
    })
    public Result<Void> adjustGroupQuota(
            @Parameter(description = "集团ID", required = true) @PathVariable Long groupId,
            @Parameter(description = "调整金额（正数增加，负数减少）", required = true) 
            @RequestParam BigDecimal adjustmentAmount,
            @Parameter(description = "调整原因", required = true) @RequestParam String reason,
            @Parameter(description = "操作人", required = true) @RequestParam String operator) {
        log.info("Received adjust group quota request: groupId={}, adjustmentAmount={}", 
                groupId, adjustmentAmount);
        
        groupQuotaService.adjustGroupQuota(groupId, adjustmentAmount, reason, operator);
        return Result.success();
    }
}
