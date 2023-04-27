package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;


import javax.enterprise.event.Observes;

public class ValidateDateLines extends EntityPersistenceEventObserver {
    private static Entity[] entities = {
            ModelProvider.getInstance().getEntity(IEConfigurationLine.ENTITY_NAME)};
    protected Logger logger = Logger.getLogger(this.getClass());
    private static ValidateDate validator = new ValidateDate();

    @Override
    protected Entity[] getObservedEntities() {
        return entities;
    }

    public void onUpdate(@Observes EntityUpdateEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfigurationLine config = (IEConfigurationLine) event.getTargetInstance();
        if (config.getToDate() != null && config.getFromDate() != null) {
            if (validator.CompareDate(config.getToDate(), config.getFromDate())) {
                throw new OBException(OBMessageUtils.messageBD("etbiie_validatedate"));
            }
        }
    }

    public void onSave(@Observes EntityNewEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfigurationLine config = (IEConfigurationLine) event.getTargetInstance();
        if (config.getToDate() != null && config.getFromDate() != null) {
            if (validator.CompareDate(config.getToDate(), config.getFromDate())) {
                throw new OBException(OBMessageUtils.messageBD("etbiie_validatedate"));
            }
        }
    }
    }
