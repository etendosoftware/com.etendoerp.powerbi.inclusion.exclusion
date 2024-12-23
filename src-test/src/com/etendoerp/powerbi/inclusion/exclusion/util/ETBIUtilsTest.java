package com.etendoerp.powerbi.inclusion.exclusion.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;

/**
 * Test class for ETBIUtils utility methods used in PowerBI inclusion/exclusion functionality.
 */
@ExtendWith(MockitoExtension.class)
class ETBIUtilsTest {

  @Mock
  private OBDal obDal;

  @Mock
  private OBCriteria<IEConfigurationLine> criteria;

  @Mock
  private IEConfiguration mockConfig;

  /**
   * Initializes test environment before each test execution.
   * Sets up basic mock behaviors for database operations.
   */
  @BeforeEach
  void setUp() {
    lenient().when(obDal.createCriteria(IEConfigurationLine.class)).thenReturn(criteria);
  }

  /**
   * Tests creation of criteria for configuration lines.
   * Verifies proper creation and configuration of query criteria.
   */
  @Test
  void getLinesCriteriaShouldCreateCriteriaWithConfiguration() {
    try (MockedStatic<OBDal> mockedStatic = mockStatic(OBDal.class)) {
      mockedStatic.when(OBDal::getInstance).thenReturn(obDal);

      OBCriteria<IEConfigurationLine> result = ETBIUtils.getLinesCriteria(mockConfig);

      verify(obDal).createCriteria(IEConfigurationLine.class);
      verify(criteria).add(any());
      assertNotNull(result);
    }
  }

  /**
   * Tests detection of existing configuration lines.
   * Verifies true return when configuration has associated lines.
   */
  @Test
  void configHasLinesWithExistingLinesShouldReturnTrue() {
    try (MockedStatic<OBDal> mockedStatic = mockStatic(OBDal.class)) {
      mockedStatic.when(OBDal::getInstance).thenReturn(obDal);
      List<IEConfigurationLine> lines = new ArrayList<>();
      lines.add(mock(IEConfigurationLine.class));
      when(criteria.list()).thenReturn(lines);

      boolean result = ETBIUtils.configHasLines(mockConfig);

      assertTrue(result);
    }
  }

  /**
   * Tests detection of empty configuration.
   * Verifies false return when configuration has no lines.
   */
  @Test
  void configHasLinesWithNoLinesShouldReturnFalse() {
    try (MockedStatic<OBDal> mockedStatic = mockStatic(OBDal.class)) {
      mockedStatic.when(OBDal::getInstance).thenReturn(obDal);
      when(criteria.list()).thenReturn(new ArrayList<>());

      boolean result = ETBIUtils.configHasLines(mockConfig);

      assertFalse(result);
    }
  }

  /**
   * Tests date validation with valid date ranges.
   * Verifies no exception thrown for valid from/to dates.
   */
  @Test
  void validateDatesWithValidDatesShouldNotThrowException() {
    Calendar cal = Calendar.getInstance();
    Date dateFrom = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date dateTo = cal.getTime();

    assertDoesNotThrow(() -> ETBIUtils.validatedates(dateTo, dateFrom));
  }

  /**
   * Tests date validation with invalid date ranges.
   * Verifies exception thrown when to date precedes from date.
   */
  @Test
  void validateDatesWithInvalidDatesShouldThrowOBException() {
    try (MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {
      Calendar cal = Calendar.getInstance();
      Date dateTo = cal.getTime();
      cal.add(Calendar.DAY_OF_MONTH, 1);
      Date dateFrom = cal.getTime();

      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_validatedate")).thenReturn(
          "Date validation error message");

      assertThrows(OBException.class, () -> ETBIUtils.validatedates(dateTo, dateFrom));
    }
  }

  /**
   * Tests date validation with null dates.
   * Verifies no exception thrown when both dates are null.
   */
  @Test
  void validateDatesWithNullDatesShouldNotThrowException() {
    assertDoesNotThrow(() -> ETBIUtils.validatedates(null, null));
  }
}
