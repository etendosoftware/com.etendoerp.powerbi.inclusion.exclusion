package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import javax.enterprise.event.Observes;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;

public class ValidateDate extends EntityPersistenceEventObserver {
    private static Entity[] entities = {
            ModelProvider.getInstance().getEntity(IEConfiguration.ENTITY_NAME) };
    protected Logger logger = Logger.getLogger(this.getClass());


    @Override protected Entity[] getObservedEntities() {
        return entities;
    }

    public void onUpdate(@Observes EntityUpdateEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfiguration config = (IEConfiguration) event.getTargetInstance();
        ETBIUtils.validatedates(config.getToDate(), config.getFromDate());
    }

    public void onSave(@Observes EntityNewEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfiguration config = (IEConfiguration) event.getTargetInstance();
        ETBIUtils.validatedates(config.getToDate(), config.getFromDate());
    }
    }
