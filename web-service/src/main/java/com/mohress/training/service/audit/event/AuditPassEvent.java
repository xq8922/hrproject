package com.mohress.training.service.audit.event;

import com.mohress.training.service.audit.action.PassAction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 审核通过事件
 *
 */
@Getter
public class AuditPassEvent extends ApplicationEvent{

    private String recordId;

    public AuditPassEvent(String recordId, PassAction source) {
        super(source);
        this.recordId = recordId;
    }
}
