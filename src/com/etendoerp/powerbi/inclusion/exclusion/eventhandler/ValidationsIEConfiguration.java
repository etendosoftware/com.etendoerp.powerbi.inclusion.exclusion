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
import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import javax.enterprise.event.Observes;

public class ValidationsIEConfiguration extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(IEConfiguration.ENTITY_NAME) };
  protected Logger logger = Logger.getLogger(this.getClass());
  private static final String LANGUAGE = OBContext.getOBContext().getLanguage().getLanguage();
  private static final ConnectionProvider conn = new DalConnectionProvider(false);

  @Override protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    //if the type of the configuration is null(using the method is empty from stringutils), the hasstring or the hasnumer or the hasyesno must be true
    IEConfiguration config = (IEConfiguration) event.getTargetInstance();
    if (StringUtils.isEmpty(
        config.getType()) && !config.isHasstring() && !config.isHasnumber() && !config.isHasyesno()) {
      throw new OBException(OBMessageUtils.messageBD("etbiie_config_need_checks"));
    }
    //cannot change the type of the configuration if exists lines.
    Property propType = event.getTargetInstance()
        .getEntity()
        .getProperty(IEConfiguration.PROPERTY_TYPE);
    String prevType = (String) event.getPreviousState(propType);
    String currType = (String) event.getCurrentState(propType);
    if (!StringUtils.equalsIgnoreCase(prevType, currType) && ETBIUtils.configHasLines(config)) {
      throw new OBException(OBMessageUtils.messageBD("etbiie_noTypeChangeWithLines"));
    }
  }

  public void onSave(@Observes EntityNewEvent event) {
      if (!isValidEvent(event)) {
        return;
      }
      //if the type of the configuration is null(using the method is empty from stringutils), the hasstring or the hasnumer or the hasyesno must be true
      IEConfiguration config = (IEConfiguration) event.getTargetInstance();
      if (StringUtils.isEmpty(
          config.getType()) && !config.isHasstring() && !config.isHasnumber() && !config.isHasyesno()) {
        throw new OBException(OBMessageUtils.messageBD("etbiie_config_need_checks"));
      }
    }

}

