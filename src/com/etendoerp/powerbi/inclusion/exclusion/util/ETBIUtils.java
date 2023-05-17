package com.etendoerp.powerbi.inclusion.exclusion.util;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import java.util.Date;
import java.util.List;

public class ETBIUtils {
  public static boolean configHasLines(IEConfiguration config) {
    return !getConfigLines(config).isEmpty();
  }
  private ETBIUtils() {
    
  }
  private static List<IEConfigurationLine> getConfigLines (IEConfiguration config) {

    return getLinesCriteria(config).list();
  }

  public static OBCriteria<IEConfigurationLine> getLinesCriteria(IEConfiguration config) {
    // create a criteria to get the lines of the configuration
    OBCriteria<IEConfigurationLine> linesCriteria = OBDal.getInstance()
        .createCriteria(IEConfigurationLine.class);
    linesCriteria.add(Restrictions.eq(IEConfigurationLine.PROPERTY_ETBIIEIECONFIGURATION, config));
    return linesCriteria;
  }

  public static void validatedates(Date dateto, Date datefrom){
    if ((dateto != null && datefrom != null) && dateto.before(datefrom)) {
      throw new OBException(OBMessageUtils.messageBD("etbiie_validatedate"));
    }
  }
}

