package com.mohress.training.controller;

import com.google.common.collect.Lists;
import com.mohress.training.dto.audit.*;
import com.mohress.training.dto.Response;
import com.mohress.training.entity.audit.TblClassAuditRecord;
import com.mohress.training.enums.ResultCode;
import com.mohress.training.service.audit.AuditService;
import com.mohress.training.service.audit.action.*;
import com.mohress.training.service.audit.impl.ClassAuditRecordServiceImpl;
import com.mohress.training.service.audit.impl.LedgerAuditRecordServiceImpl;
import com.mohress.training.util.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

import java.util.List;

import static com.mohress.training.enums.ResultCode.AUDIT_SUCCESS;
import static com.mohress.training.enums.ResultCode.SUCCESS;

/**
 * 审核接口
 *
 */
@Controller
@RequestMapping("api/audit/")
public class AuditController {

    @Resource
    private AuditService auditService;

    @Resource
    private ClassAuditRecordServiceImpl classAuditRecordService;

    @Resource
    private LedgerAuditRecordServiceImpl ledgerAuditRecordService;



    /**
     * 审核通过
     * 当前步骤处理通过，进入下一步骤，若为末步骤，则流程处理完成；
     *
     * @param auditActionDto
     * @return
     */
    @ResponseBody
    @RequestMapping("pass")
    public Response auditPass(@CookieValue("token") String userId, @RequestBody AuditActionDto auditActionDto){
        auditActionDto.setAuditor(userId);
        PassAction passAction = new PassAction(auditActionDto.getFlowId(), auditActionDto.getAuditor(), "");

        return audit(passAction);
    }

    /**
     * 审核驳回
     * 将步骤直接结束，执行结束动作拒绝活动，不再进行操作，或者回退至第一步骤；
     *
     * @param auditActionDto
     * @return
     */
    @ResponseBody
    @RequestMapping("reject")
    public Response auditReject(@CookieValue("token") String userId, @RequestBody AuditActionDto auditActionDto){
        auditActionDto.setAuditor(userId);
        RejectAction rejectAction = new RejectAction(auditActionDto.getFlowId(), auditActionDto.getAuditor(), "");

        return audit(rejectAction);
    }

    /**
     * 撤回操作
     * 若当前步骤已处理，下一处理人未处理的情况下可进行撤回操作。
     *
     * @param auditActionDto
     * @return
     */
    @Deprecated
    @ResponseBody
    @RequestMapping("retract")
    public Response auditRetract(@CookieValue("token") String userId, @RequestBody AuditActionDto auditActionDto){
        auditActionDto.setAuditor(userId);
        RetractAction retractAction = new RetractAction(auditActionDto.getFlowId(), auditActionDto.getAuditor(), auditActionDto.getAuditResult());

        return audit(retractAction);
    }

    /**
     * 回退操作
     * 将步骤退回至上一步骤，即返回至上一处理人处，若为首步骤，则不进行退回；
     *
     * @param auditActionDto
     * @return
     */
    @Deprecated
    @ResponseBody
    @RequestMapping("rollBack")
    public Response auditRollback(@CookieValue("token") String userId, @RequestBody AuditActionDto auditActionDto){
        auditActionDto.setAuditor(userId);
        RollBackAction rollBackAction = new RollBackAction(auditActionDto.getFlowId(), auditActionDto.getAuditor(), auditActionDto.getAuditResult());

        return audit(rollBackAction);
    }

    @ResponseBody
    @RequestMapping("manager/classAudit")
    public Response<List<ClassAuditItemDto>> queryClassAuditRecord(@CookieValue("token") String userId,  String agencyId, Integer pageSize, Integer pageIndex){

        ClassAuditQueryDto classAuditQueryDto = new ClassAuditQueryDto();
        classAuditQueryDto.setUserId(userId);
        classAuditQueryDto.setPageIndex(pageIndex);
        classAuditQueryDto.setPageSize(pageSize);
        classAuditQueryDto.setAgencyId(agencyId);

        List<TblClassAuditRecord> classAuditRecordList = classAuditRecordService.queryByPage(classAuditQueryDto);

        List<ClassAuditItemDto> result = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(classAuditRecordList)){
            for (TblClassAuditRecord it: classAuditRecordList){
                ClassAuditItemDto classAuditItemDto = new ClassAuditItemDto();
                classAuditItemDto.setFlowId(it.getFlowId());
                classAuditItemDto.setAgencyName(it.getAgencyName());
                classAuditItemDto.setClassName(it.getClassName());
                classAuditItemDto.setAuditStatus(it.getAuditStatus());
                classAuditItemDto.setApplyTime(DateUtil.format(it.getApplyTime(), "yyyy-MM-dd HH:mm"));
                result.add(classAuditItemDto);
            }
        }

        return new Response<>(ResultCode.SUCCESS.getCode(), "", result);
    }


    @ResponseBody
    @RequestMapping("manager/ledgerAudit")
    public Response<List<LedgerAuditItemDto>> queryLedgerAuditRecord(@CookieValue("token") String userId, String agencyId, Integer pageSize, Integer pageIndex){
        LedgerAuditQueryDto ledgerAuditQueryDto = new LedgerAuditQueryDto();

        ledgerAuditQueryDto.setAgencyId(agencyId);
        ledgerAuditQueryDto.setUserId(userId);
        ledgerAuditQueryDto.setAuditRoleId("");
        ledgerAuditQueryDto.setPageSize(pageSize);
        ledgerAuditQueryDto.setPageIndex(pageIndex);

        List<LedgerAuditItemDto> ledgerAuditItemDtoList = ledgerAuditRecordService.queryByPage(ledgerAuditQueryDto);

        return new Response<>(ResultCode.SUCCESS.getCode(), "", ledgerAuditItemDtoList);
    }

    private Response audit(AuditAction auditAction){

        auditService.audit(auditAction);

        return new Response(SUCCESS.getCode(), AUDIT_SUCCESS.getDesc());
    }
}
