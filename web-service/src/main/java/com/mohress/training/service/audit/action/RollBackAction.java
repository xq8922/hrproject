package com.mohress.training.service.audit.action;


import com.mohress.training.dao.TblAuditFlowDao;
import com.mohress.training.dao.TblAuditNodeDao;
import com.mohress.training.dao.TblAuditTemplateDao;
import com.mohress.training.dao.TblAuditLogDao;
import com.mohress.training.entity.audit.TblAuditFlow;
import com.mohress.training.entity.audit.TblAuditNode;
import com.mohress.training.entity.audit.TblAuditTemplate;
import com.mohress.training.entity.audit.TblAuditLog;
import com.mohress.training.enums.ResultCode;
import com.mohress.training.exception.BusinessException;
import com.mohress.training.util.SequenceCreator;
import com.mohress.training.util.SpringContextHelper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.mohress.training.enums.AuditStatus.AUDIT_WAIT;

/**
 * 审核-回退动作
 * 将步骤退回至上一步骤，即返回至上一处理人处，若为首步骤，则不进行退回；
 *
 */
@Deprecated
public class RollBackAction extends AbstractAuditAction {

    private static final int ACTION_ID = 4;

    private String flowId;

    public RollBackAction(String flowId, String auditor, String auditResult) {
        super(auditor, auditResult);
        this.flowId = flowId;
    }

    public TblAuditFlow getAuditFlow() {
        return SpringContextHelper.getBean(TblAuditFlowDao.class).selectByFlowId(flowId);
    }

    @Transactional
    protected void doExecute() {
        TblAuditFlow auditFlow = getAuditFlow();

        TblAuditTemplate auditProject = SpringContextHelper.getBean(TblAuditTemplateDao.class).selectByTemplateId(auditFlow.getTemplateId());

        // 当前审核流程已进入终态，不能回退
        if (auditFlow.getFlowStatus() != AUDIT_WAIT.getStatus()){
            throw new BusinessException(ResultCode.AUDIT_FAIL, "当前流程已进入终态，不能执行回退操作。");
        }

        // 若为首步骤，直接返回
        if (auditProject.getStartNode().equals(auditFlow.getNodeId())){
            return;
        }

        TblAuditNode auditNode = SpringContextHelper.getBean(TblAuditNodeDao.class).selectByNodeId(auditFlow.getNodeId());

        // 审核记录存档
        TblAuditLog auditLog = new TblAuditLog();
        auditLog.setRecordId(SequenceCreator.getAuditRecordId());
        auditLog.setFlowId(flowId);
        auditLog.setNodeId(auditFlow.getNodeId());
        auditLog.setAction(ACTION_ID);
        auditLog.setAuditor(getAuditor());
        auditLog.setAuditResult(getAuditResult());
        auditLog.setCreateTime(new Date());
        auditLog.setUpdateTime(new Date());

        auditFlow.setNodeId(auditNode.getPreviousNode());
        auditFlow.setNodeStatus(AUDIT_WAIT.getStatus());
        auditFlow.setFlowStatus(AUDIT_WAIT.getStatus());

        SpringContextHelper.getBean(TblAuditLogDao.class).insert(auditLog);
        int updateResult = SpringContextHelper.getBean(TblAuditFlowDao.class).updateByFlowIdAndVersion(auditFlow);
        if (updateResult != 1){
            throw new BusinessException(ResultCode.AUDIT_FAIL, "更新审核流程信息失败，请重新审核。");
        }
    }
}
