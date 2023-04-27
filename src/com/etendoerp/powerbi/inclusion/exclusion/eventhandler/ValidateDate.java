package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import javax.enterprise.event.Observes;
import java.util.Date;

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
        if (config.getToDate() != null && config.getFromDate() != null) {
            if (CompareDate(config.getToDate(), config.getFromDate())) {
                throw new OBException(OBMessageUtils.messageBD("etbiie_validatedate"));
            }
        }
    }

    public void onSave(@Observes EntityNewEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        IEConfiguration config = (IEConfiguration) event.getTargetInstance();
        if (config.getToDate() != null && config.getFromDate() != null) {
            if (CompareDate(config.getToDate(), config.getFromDate())) {
                throw new OBException(OBMessageUtils.messageBD("etbiie_validatedate"));
            }
        }
    }

    public boolean CompareDate(Date dateto, Date datefrom){
        if (dateto.before(datefrom)) {
            return true;
        } else {
            return false;
        }
    }

}
