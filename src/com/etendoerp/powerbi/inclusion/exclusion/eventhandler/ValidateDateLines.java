package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;


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
        ETBIUtils.validatedates(config.getToDate(), config.getFromDate());
    }

    public void onSave(@Observes EntityNewEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfigurationLine config = (IEConfigurationLine) event.getTargetInstance();
        ETBIUtils.validatedates(config.getToDate(), config.getFromDate());
    }
    }
