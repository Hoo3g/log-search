package com.defi.config.eform;

import com.defi.common.api.BaseResponse;
import com.defi.common.api.CommonError;
import com.defi.common.util.jdbi.JdbiProvider;
import com.defi.common.util.log.EventLogger;
import com.defi.common.util.log.entity.EventLog;
import com.defi.config.ConfigSharedServices;
import com.defi.config.eform.dto.EformFilter;
import com.defi.config.eform.dto.EformPageResult;
import com.defi.config.eform.entity.Eform;
import com.defi.config.orchestrator.event.ConfigEventContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class EformManager {
    @Getter
    private static final EformManager instance = new EformManager();

    private EformManager() {
    }

    public BaseResponse<?> eformCreated(ConfigEventContext<Eform> eventContext) {
        EventLog event = eventContext.getEvent();
        Eform eform = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            Eform created = ConfigSharedServices.eformService.create(handle, eform);
            event.setTargetId(created.getCode());
            EventLogger.log(event);
            return BaseResponse.of(CommonError.SUCCESS, created);
        });
    }

    public BaseResponse<?> eformUpdated(ConfigEventContext<Eform> eventContext) {
        EventLog event = eventContext.getEvent();
        Eform eform = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean updated = ConfigSharedServices.eformService.update(handle, eform);
            if (updated) {
                event.setTargetId(eform.getCode());
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS, eform);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> eformDeleted(ConfigEventContext<String> eventContext) {
        EventLog event = eventContext.getEvent();
        String code = eventContext.getContext();
        return JdbiProvider.getInstance().getJdbi().inTransaction(handle -> {
            boolean deleted = ConfigSharedServices.eformService.delete(handle, code);
            if (deleted) {
                event.setTargetId(code);
                EventLogger.log(event);
                return BaseResponse.of(CommonError.SUCCESS);
            } else {
                return BaseResponse.of(CommonError.BAD_REQUEST);
            }
        });
    }

    public BaseResponse<?> getEformByCode(String code) {
        Eform eform = ConfigSharedServices.eformService.getByCode(code);
        if (eform == null) {
            return BaseResponse.of(CommonError.BAD_REQUEST);
        }
        return BaseResponse.of(CommonError.SUCCESS, eform);
    }

    public BaseResponse<?> listAllEforms() {
        List<Eform> eforms = ConfigSharedServices.eformService.listAll();
        return BaseResponse.of(CommonError.SUCCESS, eforms);
    }

    public BaseResponse<?> filterEforms(EformFilter filter) {
        if (!filter.isValid()) {
            return BaseResponse.of(CommonError.BAD_REQUEST);
        }

        EformPageResult result = ConfigSharedServices.eformService.filter(filter);
        return BaseResponse.of(CommonError.SUCCESS, result);
    }
}