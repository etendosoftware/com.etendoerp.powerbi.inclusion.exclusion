package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;

import javax.enterprise.event.Observes;
import java.util.Date;

public class ValidateDateLines extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(IEConfigurationLine.ENTITY_NAME) };
  private static ValidateDate validator = new ValidateDate();
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    IEConfigurationLine config = (IEConfigurationLine) event.getTargetInstance();
    final Property toDateProperty = config.getEntity()
        .getProperty(IEConfigurationLine.PROPERTY_TODATE);
    final Property fromDateProperty = config.getEntity()
        .getProperty(IEConfigurationLine.PROPERTY_FROMDATE);
    Date dateTo = (Date) event.getCurrentState(toDateProperty);
    Date dateFrom = (Date) event.getCurrentState(fromDateProperty);
    ETBIUtils.validatedates(dateTo, dateFrom);
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    IEConfigurationLine config = (IEConfigurationLine) event.getTargetInstance();
    final Property toDateProperty = config.getEntity()
        .getProperty(IEConfigurationLine.PROPERTY_TODATE);
    final Property fromDateProperty = config.getEntity()
        .getProperty(IEConfigurationLine.PROPERTY_FROMDATE);
    Date dateTo = (Date) event.getCurrentState(toDateProperty);
    Date dateFrom = (Date) event.getCurrentState(fromDateProperty);
    ETBIUtils.validatedates(dateTo, dateFrom);
  }
}
