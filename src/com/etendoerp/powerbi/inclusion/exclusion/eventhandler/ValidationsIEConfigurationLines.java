package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DalConnectionProvider;

import javax.enterprise.event.Observes;

public class ValidationsIEConfigurationLines extends EntityPersistenceEventObserver {
    public static final String CONFIGURATION_TYPE_DOCTYPE = "D";
    private static Entity[] entities = {
            ModelProvider.getInstance().getEntity(IEConfigurationLine.ENTITY_NAME) };
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
        //if is not configuration of type "doctype", deletes the default doctype.
        fixDoctype((IEConfigurationLine) event.getTargetInstance());
        //the Entity cannot be null
        checkEntity((IEConfigurationLine) event.getTargetInstance());
        //Check for repeated lines
        checkRepeatedLines((IEConfigurationLine) event.getTargetInstance());
    }

    public void onSave(@Observes EntityNewEvent event) {
        if (!isValidEvent(event)) {
            return;
        }
        //if is not configuration of type "doctype", deletes the default doctype.
        fixDoctype((IEConfigurationLine) event.getTargetInstance());
        //the Entity cannot be null
        checkEntity((IEConfigurationLine) event.getTargetInstance());
        //Check for repeated lines
        checkRepeatedLines((IEConfigurationLine) event.getTargetInstance());
    }

    private void fixDoctype(IEConfigurationLine targetInstance) {
        if (targetInstance.getEtbiieIeConfiguration().getType() == null || !StringUtils.equalsIgnoreCase(
                targetInstance.getEtbiieIeConfiguration().getType(), CONFIGURATION_TYPE_DOCTYPE)) {
            targetInstance.setDocumentType(null);
        }
    }

    private void checkRepeatedLines(IEConfigurationLine line) {
        OBCriteria<IEConfigurationLine> linesCriteria = ETBIUtils.getLinesCriteria(
                line.getEtbiieIeConfiguration());
        //if the config not has type, dont check.
        if (line.getEtbiieIeConfiguration().getType() == null) {
            return;
        }
        //check if exists other line with the same entity
        switch (line.getEtbiieIeConfiguration().getType()){
            case "G":
                linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_GLITEM, line.getGLItem()));
                break;
            case "C":
                linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_PRODUCTCATEGORY,line.getProductCategory()));
                break;
            case "P":
                linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_PRODUCT, line.getProduct()));
                break;
            case CONFIGURATION_TYPE_DOCTYPE:
                linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_DOCUMENTTYPE, line.getDocumentType()));
                break;
        }
        //and its id is different from the current line
        linesCriteria.add(Restrictions.ne(IEConfigurationLine.PROPERTY_ID, line.getId()));
        if( !linesCriteria.list().isEmpty()) {
            throw new OBException(OBMessageUtils.messageBD("etbiie_repeatedLine"));
        }

    }

    private void checkEntity(IEConfigurationLine line) {
        // can be G, C, P, D or null
        boolean error = false;
        if (line.getEtbiieIeConfiguration().getType() == null) {
            return;
        }
        switch (line.getEtbiieIeConfiguration().getType()){
            case "G":
                error = line.getGLItem() == null;
                break;
            case "C":
                error = line.getProductCategory() == null;
                break;
            case "P":
                error = line.getProduct() == null;
                break;
            case CONFIGURATION_TYPE_DOCTYPE:
                error = line.getDocumentType() == null;
                break;
        }
        if (error) {
            throw new OBException(OBMessageUtils.messageBD("etbiie_LineNoValidData"));
        }
    }

}

