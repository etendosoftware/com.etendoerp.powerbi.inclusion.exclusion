package com.etendoerp.powerbi.inclusion.exclusion.util;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;

public class TestSonarRules {

  // Test Issue "'OBContext.getOBContext().getCurrent<Type>()' methods should not be evaluated at global scope"
  private static final Client testSonarIssue3 = OBContext.getOBContext().getCurrentClient();

  // Test Issue "Early-returns should be used whenever possible"
  public boolean testSonarIssue1(int number) {
    if (number > 0) {
      return true;
    } else {
      return false;
    }
  }

  // Test Issue "The OBCriteria.list() method should not be used for single result queries"
  public Invoice testSonarIssue2(String invoiceId) {
    OBCriteria<Invoice> invCriteria = OBDal.getInstance().createCriteria(Invoice.class);
    invCriteria.add(Restrictions.eq(Invoice.PROPERTY_ID, invoiceId));

    return invCriteria.list().get(0);
  }

  // Test Issue "OBCriteria.list() method calls inside loop conditions should be extracted to outside variables"
  public void testSonarIssue4() {
    OBCriteria<Product> criteria = OBDal.getInstance().createCriteria(Product.class);
    for (Product product : criteria.list()) {
      // ...
    }
  }

  // Test Issue "OBContext.restorePreviousMode() should be called in finally block if OBContext.setAdminMode(true) was called"
  public void testSonarIssue5() throws Exception {
    try {
      OBContext.setAdminMode(true);
    } catch (Exception e) {
      throw new Exception("This is an exception");
    }
  }

  // Test Issue "OBContext.setAdminMode() should be called inside try-catch blocks"
  public void testSonarIssue6() {
    // ...
    OBContext.setAdminMode(true);
    try {
      // ...
    } catch (Exception e) {
      // ...
    }
  }

  // Test Issue "Massive String concatenation should be made using StringBuilder"
  public void testSonarIssue7() {
    String variable = "trigger an Issue here because the rule ";
    String str = "This is an example for a " +
        "case where multiple Strings are " +
        "being concatenated in a single variable. " +
        "Normal String concatenation using + will " +
        variable +
        "is designed to report this kind of behaviour";
    // ...
  }

  // Test Issue "String methods should be replaced with StringUtils class methods (org.apache.commons.lang.StringUtils) whenever possible"
  public boolean testSonarIssue8(String str1, String str2) {
    return str1.equals(str2);
  }
}