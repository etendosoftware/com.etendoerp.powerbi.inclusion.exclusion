package com.etendoerp.powerbi.util;

import com.etendoerp.powerbi.data.IEConfiguration;
import com.etendoerp.powerbi.data.IEConfigurationLine;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

import java.util.List;

public class ETBIUtils {
  public static boolean configHasLines(IEConfiguration config) {
    return !getConfigLines(config).isEmpty();
  }

  private static List<IEConfigurationLine> getConfigLines (IEConfiguration config) {

    return getLinesCriteria(config).list();
  }

  public static OBCriteria<IEConfigurationLine> getLinesCriteria(IEConfiguration config) {
    // create a criteria to get the lines of the configuration
    OBCriteria<IEConfigurationLine> linesCriteria = OBDal.getInstance()
        .createCriteria(IEConfigurationLine.class);
    linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_ETBIIECONFIGURATION, config));
    return linesCriteria;
  }
}

