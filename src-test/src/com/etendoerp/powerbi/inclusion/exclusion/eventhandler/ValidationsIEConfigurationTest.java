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
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.accounting.coa.Element;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;

/**
 * Test class for ValidationsIEConfiguration which validates Inclusion/Exclusion configurations
 * in PowerBI integration.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidationsIEConfigurationTest {

  private ValidationsIEConfiguration validator;

  @Mock
  private EntityNewEvent newEvent;

  @Mock
  private EntityUpdateEvent updateEvent;

  @Mock
  private IEConfiguration ieConfig;

  @Mock
  private Entity entity;

  @Mock
  private Property accountProperty;

  @Mock
  private Property typeProperty;

  @Mock
  private Element element;

  /**
   * Sets up the test environment before each test.
   * Initializes the validator and configures basic mock behaviors.
   */
  @BeforeEach
  void setUp() {
    validator = spy(new ValidationsIEConfiguration() {
      @Override
      protected boolean isValidEvent(org.openbravo.client.kernel.event.EntityPersistenceEvent event) {
        return true;
      }
    });

    when(ieConfig.getEntity()).thenReturn(entity);
    when(entity.getProperty(IEConfiguration.PROPERTY_ACCOUNTTREE)).thenReturn(accountProperty);
    when(entity.getProperty(IEConfiguration.PROPERTY_TYPE)).thenReturn(typeProperty);
  }

  /**
   * Tests successful save operation with valid account configuration.
   * Verifies that no exception is thrown when saving with valid account and type A.
   */
  @Test
  void testOnSaveValidAccountConfiguration() {
    when(newEvent.getTargetInstance()).thenReturn(ieConfig);
    when(newEvent.getCurrentState(accountProperty)).thenReturn(element);
    when(newEvent.getCurrentState(typeProperty)).thenReturn("A");

    assertDoesNotThrow(() -> validator.onSave(newEvent));
  }

  /**
   * Tests save operation validation when account is missing for type A.
   * Verifies that appropriate exception is thrown with correct error message.
   */
  @Test
  void testOnSaveWithMissingAccountForTypeA() {
    when(newEvent.getTargetInstance()).thenReturn(ieConfig);
    when(newEvent.getCurrentState(accountProperty)).thenReturn(null);
    when(newEvent.getCurrentState(typeProperty)).thenReturn("A");

    try (MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {
      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_noAccountTree")).thenReturn(
          "Account tree is required");

      OBException exception = assertThrows(OBException.class, () -> validator.onSave(newEvent));
      assertEquals("Account tree is required", exception.getMessage());
    }
  }

  /**
   * Tests successful update operation with valid configuration.
   * Verifies that no exception is thrown when updating with valid parameters.
   */
  @Test
  void testOnUpdateValidConfiguration() {
    when(updateEvent.getTargetInstance()).thenReturn(ieConfig);
    when(updateEvent.getCurrentState(accountProperty)).thenReturn(element);
    when(updateEvent.getCurrentState(typeProperty)).thenReturn("A");
    when(updateEvent.getPreviousState(typeProperty)).thenReturn("A");

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.configHasLines(any(IEConfiguration.class))).thenReturn(false);

      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));
    }
  }

  /**
   * Tests update operation when attempting to change type with existing lines.
   * Verifies that appropriate exception is thrown when trying to change type
   * from A to B with existing configuration lines.
   */
  @Test
  void testOnUpdateTypeChangeWithExistingLines() {
    when(updateEvent.getTargetInstance()).thenReturn(ieConfig);
    when(updateEvent.getCurrentState(accountProperty)).thenReturn(element);
    when(updateEvent.getCurrentState(typeProperty)).thenReturn("B");
    when(updateEvent.getPreviousState(typeProperty)).thenReturn("A");

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(
        ETBIUtils.class); MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {

      mockedUtils.when(() -> ETBIUtils.configHasLines(any(IEConfiguration.class))).thenReturn(true);
      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_noTypeChangeWithLines")).thenReturn(
          "Cannot change type when lines exist");

      OBException exception = assertThrows(OBException.class, () -> validator.onUpdate(updateEvent));
      assertEquals("Cannot change type when lines exist", exception.getMessage());
    }
  }

  /**
   * Tests retrieval of observed entities.
   * Verifies that the correct entity is returned in the array of observed entities.
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
}
