package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.accounting.coa.Element;


import javax.enterprise.event.Observes;

public class ValidationsIEConfiguration extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
          ModelProvider.getInstance().getEntity(IEConfiguration.ENTITY_NAME)};
  protected Logger logger = Logger.getLogger(this.getClass());

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Property propAccount = event.getTargetInstance()
            .getEntity()
            .getProperty(IEConfiguration.PROPERTY_ACCOUNTTREE);
    Element account = (Element) event.getCurrentState(propAccount);

    Property propType = event.getTargetInstance()
            .getEntity()
            .getProperty(IEConfiguration.PROPERTY_TYPE);
    String currType = (String) event.getCurrentState(propType);
    validateAccount(currType, account);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    IEConfiguration config = (IEConfiguration) event.getTargetInstance();
    Property propAccount = event.getTargetInstance()
            .getEntity()
            .getProperty(IEConfiguration.PROPERTY_ACCOUNTTREE);
    Element account = (Element) event.getCurrentState(propAccount);

    //cannot change the type of the configuration if exists lines.
    Property propType = event.getTargetInstance()
            .getEntity()
            .getProperty(IEConfiguration.PROPERTY_TYPE);
    String prevType = (String) event.getPreviousState(propType);
    String currType = (String) event.getCurrentState(propType);
    validateAccount(currType, account);
    if (!StringUtils.equalsIgnoreCase(prevType, currType) && ETBIUtils.configHasLines(config)) {
      throw new OBException(OBMessageUtils.messageBD("etbiie_noTypeChangeWithLines"));
    }
  }
  public void validateAccount (String currType, Element account){
    if (currType != null) {
      validateAccountTree(account, currType);
    }
  }
  public void validateAccountTree (Element account, String type){
    if (StringUtils.equalsIgnoreCase(type, "A") && account == null) {
      throw new OBException(OBMessageUtils.messageBD("etbiie_noTypeChangeWithLines"));
      }
    }
}

