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

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;

/**
 * Unit tests for the {@link ValidateDateLines} class.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidateDateLinesTest {

  private ValidateDateLines validator;

  @Mock
  private EntityNewEvent newEvent;

  @Mock
  private EntityUpdateEvent updateEvent;

  @Mock
  private IEConfigurationLine configLine;

  @Mock
  private Entity entity;

  @Mock
  private Property toDateProperty;

  @Mock
  private Property fromDateProperty;

  /**
   * Sets up mock objects and initializes the test environment before each test case.
   */
  @BeforeEach
  void setUp() {
    validator = spy(new ValidateDateLines() {
      @Override
      protected boolean isValidEvent(org.openbravo.client.kernel.event.EntityPersistenceEvent event) {
        return true;
      }
    });

    when(configLine.getEntity()).thenReturn(entity);
    when(entity.getProperty(IEConfigurationLine.PROPERTY_TODATE)).thenReturn(toDateProperty);
    when(entity.getProperty(IEConfigurationLine.PROPERTY_FROMDATE)).thenReturn(fromDateProperty);
  }

  /**
   * Tests the {@link ValidateDateLines#onSave(EntityNewEvent)} method to ensure it handles valid dates correctly.
   */
  @Test
  void testOnSaveValidDates() {
    // Arrange
    LocalDate fromDateLocal = LocalDate.of(2022, 1, 1);
    LocalDate toDateLocal = LocalDate.of(2022, 12, 31);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);

    when(newEvent.getTargetInstance()).thenReturn(configLine);
    when(newEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(newEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).then(invocation -> null);

      assertDoesNotThrow(() -> validator.onSave(newEvent));

      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }


  /**
   * Tests the {@link ValidateDateLines#onUpdate(EntityUpdateEvent)} method to ensure it handles valid dates correctly.
   */
  @Test
  void testOnUpdateValidDates() {
    // Arrange
    LocalDate fromDateLocal = LocalDate.of(2022, 1, 1);
    LocalDate toDateLocal = LocalDate.of(2022, 12, 31);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);

    when(updateEvent.getTargetInstance()).thenReturn(configLine);
    when(updateEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(updateEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).then(invocation -> null);

      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));

      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }

  /**
   * Tests the {@link ValidateDateLines#onSave(EntityNewEvent)} method to ensure it throws an exception for invalid dates.
   */
  @Test
  void testOnSaveInvalidDates() {
    // Arrange
    LocalDate fromDateLocal = LocalDate.of(2022, 12, 31);
    LocalDate toDateLocal = LocalDate.of(2022, 1, 1);

    Date fromDate = java.sql.Date.valueOf(fromDateLocal);
    Date toDate = java.sql.Date.valueOf(toDateLocal);

    when(newEvent.getTargetInstance()).thenReturn(configLine);
    when(newEvent.getCurrentState(fromDateProperty)).thenReturn(fromDate);
    when(newEvent.getCurrentState(toDateProperty)).thenReturn(toDate);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.validatedates(any(Date.class), any(Date.class))).thenThrow(
          new OBException("Invalid date range"));

      OBException exception = assertThrows(OBException.class, () -> validator.onSave(newEvent));
      assertEquals("Invalid date range", exception.getMessage());

      mockedUtils.verify(() -> ETBIUtils.validatedates(toDate, fromDate));
    }
  }


  /**
   * Tests the {@link ValidateDateLines#getObservedEntities()} method to ensure it returns the correct observed entities.
   */
  @Test
  void testGetObservedEntities() {
    try (MockedStatic<ModelProvider> mockedProvider = mockStatic(ModelProvider.class)) {
      Entity mockEntity = mock(Entity.class);
      ModelProvider mockModelProvider = mock(ModelProvider.class);

      mockedProvider.when(ModelProvider::getInstance).thenReturn(mockModelProvider);
      when(mockModelProvider.getEntity(IEConfigurationLine.ENTITY_NAME)).thenReturn(mockEntity);
      when(mockEntity.getName()).thenReturn(IEConfigurationLine.ENTITY_NAME);

      Entity[] observedEntities = validator.getObservedEntities();

      assertNotNull(observedEntities);
      assertEquals(1, observedEntities.length);
      assertEquals(IEConfigurationLine.ENTITY_NAME, observedEntities[0].getName());
    }
  }
}
