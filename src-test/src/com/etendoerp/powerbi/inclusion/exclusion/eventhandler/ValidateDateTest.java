package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityUpdateEvent;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;

/**
 * Test class for ValidateDate event handler which validates date ranges in IEConfiguration entities.
 * Tests both creation and update scenarios for date validation functionality.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidateDateTest {

  private ValidateDate validator;

  @Mock
  private EntityNewEvent newEvent;

  @Mock
  private EntityUpdateEvent updateEvent;

  @Mock
  private IEConfiguration config;

  @Mock
  private Entity entity;

  @Mock
  private Property toDateProperty;

  @Mock
  private Property fromDateProperty;

  /**
   * Sets up test environment before each test.
   * Initialize mocks and configures basic behavior.
   */
  @BeforeEach
  void setUp() {
    validator = spy(new ValidateDate() {
      @Override
      protected boolean isValidEvent(org.openbravo.client.kernel.event.EntityPersistenceEvent event) {
        return true;
      }
    });

    when(config.getEntity()).thenReturn(entity);
    when(entity.getProperty(IEConfiguration.PROPERTY_TODATE)).thenReturn(toDateProperty);
    when(entity.getProperty(IEConfiguration.PROPERTY_FROMDATE)).thenReturn(fromDateProperty);
  }

  /**
   * Tests retrieval of observed entities.
   * Verifies that the validator correctly identifies IEConfiguration as the observed entity.
   */
  @Test
  void testGetObservedEntities() {
    try (MockedStatic<ModelProvider> mockedProvider = mockStatic(ModelProvider.class)) {
      Entity mockEntity = mock(Entity.class);
      ModelProvider mockModelProvider = mock(ModelProvider.class);

      mockedProvider.when(ModelProvider::getInstance).thenReturn(mockModelProvider);
      when(mockModelProvider.getEntity(IEConfiguration.ENTITY_NAME)).thenReturn(mockEntity);
      when(mockEntity.getName()).thenReturn(IEConfiguration.ENTITY_NAME);

      Entity[] observedEntities = validator.getObservedEntities();

      assertNotNull(observedEntities);
      assertEquals(1, observedEntities.length);
      assertEquals(IEConfiguration.ENTITY_NAME, observedEntities[0].getName());
    }
  }

  /**
   * Tests validation of valid date ranges during entity creation.
   * Verifies that no exception is thrown when fromDate precedes toDate.
   */
  @Test
  void testOnSaveValidDates() {
    LocalDate fromDateLocal = LocalDate.of(2022, 1, 1);
    LocalDate toDateLocal = LocalDate.of(2022, 12, 31);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);

    when(newEvent.getTargetInstance()).thenReturn(config);
    when(newEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(newEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).then(invocation -> null);
      assertDoesNotThrow(() -> validator.onSave(newEvent));
      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }

  /**
   * Tests validation of valid date ranges during entity update.
   * Verifies that no exception is thrown when fromDate precedes toDate.
   */
  @Test
  void testOnUpdateValidDates() {
    LocalDate fromDateLocal = LocalDate.of(2022, 1, 1);
    LocalDate toDateLocal = LocalDate.of(2022, 12, 31);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);

    when(updateEvent.getTargetInstance()).thenReturn(config);
    when(updateEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(updateEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).then(invocation -> null);
      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));
      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }

  /**
   * Tests validation of invalid date ranges during entity creation.
   * Verifies that appropriate exception is thrown when toDate precedes fromDate.
   */
  @Test
  void testOnSaveInvalidDates() {
    LocalDate fromDateLocal = LocalDate.of(2022, 12, 31);
    LocalDate toDateLocal = LocalDate.of(2022, 1, 1);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);
    String errorMessage = "Invalid date range";

    when(newEvent.getTargetInstance()).thenReturn(config);
    when(newEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(newEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).thenThrow(
          new OBException(errorMessage));
      OBException exception = assertThrows(OBException.class, () -> validator.onSave(newEvent));
      assertEquals(errorMessage, exception.getMessage());
      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }

  /**
   * Tests validation of invalid date ranges during entity update.
   * Verifies that appropriate exception is thrown when toDate precedes fromDate.
   */
  @Test
  void testOnUpdateInvalidDates() {
    LocalDate fromDateLocal = LocalDate.of(2022, 12, 31);
    LocalDate toDateLocal = LocalDate.of(2022, 1, 1);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);
    String errorMessage = "Invalid date range";

    when(updateEvent.getTargetInstance()).thenReturn(config);
    when(updateEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(updateEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).thenThrow(
          new OBException(errorMessage));
      OBException exception = assertThrows(OBException.class, () -> validator.onUpdate(updateEvent));
      assertEquals(errorMessage, exception.getMessage());
      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }
}
