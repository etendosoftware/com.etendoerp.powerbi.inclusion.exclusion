package com.etendoerp.powerbi.inclusion.exclusion.eventhandler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductCategory;
import org.openbravo.model.financialmgmt.gl.GLItem;

import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfiguration;
import com.etendoerp.powerbi.inclusion.exclusion.data.IEConfigurationLine;
import com.etendoerp.powerbi.inclusion.exclusion.util.ETBIUtils;

/**
 * Test class for ValidationsIEConfigurationLines which validates IE Configuration Lines.
 * Uses JUnit 5 and Mockito for testing validation logic of different configuration types.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ValidationsIEConfigurationLinesTest {

  private ValidationsIEConfigurationLines validator;

  @Mock
  private EntityNewEvent newEvent;

  @Mock
  private EntityUpdateEvent updateEvent;

  @Mock
  private IEConfigurationLine configLine;

  @Mock
  private IEConfiguration ieConfig;

  @Mock
  private OBCriteria<IEConfigurationLine> criteria;

  @Mock
  private DocumentType documentType;

  @Mock
  private Product product;

  @Mock
  private ProductCategory productCategory;

  @Mock
  private BusinessPartner businessPartner;

  @Mock
  private Category businessPartnerCategory;

  @Mock
  private GLItem glItem;

  /**
   * Sets up the test environment before each test.
   * Initializes the validator and configures basic mock behaviors.
   */
  @BeforeEach
  void setUp() {
    validator = spy(new ValidationsIEConfigurationLines() {
      @Override
      protected boolean isValidEvent(org.openbravo.client.kernel.event.EntityPersistenceEvent event) {
        return true;
      }
    });

    when(configLine.getEtbiieIeConfiguration()).thenReturn(ieConfig);
    when(configLine.getId()).thenReturn("testId");
  }

  /**
   * Tests document type validation for non-document type configurations.
   * Verifies that document type is set to null when configuration type is not 'D'.
   */
  @Test
  void testFixDoctypeForNonDocTypeConfiguration() {
    when(ieConfig.getType()).thenReturn("P");
    when(configLine.getDocumentType()).thenReturn(documentType);
    when(newEvent.getTargetInstance()).thenReturn(configLine);
    when(configLine.getProduct()).thenReturn(mock(Product.class));

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(
        ETBIUtils.class); MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {

      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      mockedMessageUtils.when(() -> OBMessageUtils.messageBD(anyString())).thenReturn("test message");

      validator.onSave(newEvent);

      verify(configLine).setDocumentType(null);
    }
  }

  /**
   * Tests product type configuration with valid product.
   * Verifies that no exception is thrown when product is properly set.
   */
  @Test
  void testCheckEntityForProductTypeWithValidProduct() {
    when(ieConfig.getType()).thenReturn("P");
    when(configLine.getProduct()).thenReturn(product);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onSave(newEvent));
    }
  }

  /**
   * Tests product type configuration with invalid product.
   * Verifies that OBException is thrown when product is null.
   */
  @Test
  void testCheckEntityForProductTypeWithInvalidProduct() {
    when(ieConfig.getType()).thenReturn("P");
    when(configLine.getProduct()).thenReturn(null);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {
      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_LineNoValidData")).thenReturn("Invalid line data");

      OBException exception = assertThrows(OBException.class, () -> validator.onSave(newEvent));
      assertEquals("Invalid line data", exception.getMessage());
    }
  }

  /**
   * Tests duplicate line detection for product configurations.
   * Verifies that OBException is thrown when duplicate line is found.
   */
  @Test
  void testCheckRepeatedLinesForProduct() {
    when(ieConfig.getType()).thenReturn("P");
    when(configLine.getProduct()).thenReturn(product);
    Date fromDate = new Date();
    Date toDate = new Date();
    when(configLine.getFromDate()).thenReturn(fromDate);
    when(configLine.getToDate()).thenReturn(toDate);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    List<IEConfigurationLine> existingLines = new ArrayList<>();
    existingLines.add(mock(IEConfigurationLine.class));

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(
        ETBIUtils.class); MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {

      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(existingLines);

      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_repeatedLine")).thenReturn("Repeated line found");

      OBException exception = assertThrows(OBException.class, () -> validator.onSave(newEvent));
      assertEquals("Repeated line found", exception.getMessage());
    }
  }

  /**
   * Tests valid document type configuration.
   * Verifies that document type is not cleared when configuration type is 'D'.
   */
  @Test
  void testValidDocTypeConfiguration() {
    when(ieConfig.getType()).thenReturn("D");
    when(configLine.getDocumentType()).thenReturn(documentType);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onSave(newEvent));
      verify(configLine, never()).setDocumentType(null);
    }
  }

  /**
   * Tests retrieval of observed entities.
   * Verifies correct entity name and count for IEConfigurationLine.
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

  /**
   * Tests business partner configuration validation.
   * Verifies that no exception is thrown for valid business partner setup.
   */
  @Test
  void testValidBusinessPartnerConfiguration() {
    when(ieConfig.getType()).thenReturn("BP");
    when(configLine.getBusinessPartner()).thenReturn(businessPartner);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onSave(newEvent));
    }
  }

  /**
   * Tests business partner category configuration validation.
   * Verifies that no exception is thrown for valid category setup.
   */
  @Test
  void testValidBusinessPartnerCategoryConfiguration() {
    when(ieConfig.getType()).thenReturn("BPC");
    when(configLine.getBusinessPartnerCategory()).thenReturn(businessPartnerCategory);
    when(newEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onSave(newEvent));
    }
  }

  /**
   * Tests document type handling during update operations.
   * Verifies document type behavior when updating configuration type 'D'.
   */
  @Test
  void testOnUpdateFixDoctypeForDocTypeConfiguration() {
    when(ieConfig.getType()).thenReturn("D");
    when(configLine.getDocumentType()).thenReturn(documentType);
    when(updateEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      validator.onUpdate(updateEvent);

      verify(configLine, never()).setDocumentType(null);
      verify(configLine, atLeastOnce()).getDocumentType();
    }
  }

  /**
   * Tests GL Item configuration validation.
   * Verifies that no exception is thrown for valid GL Item setup.
   */
  @Test
  void testGLItemConfiguration() {
    when(ieConfig.getType()).thenReturn("G");
    when(configLine.getGLItem()).thenReturn(glItem);
    when(updateEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));
      verify(configLine, atLeastOnce()).getGLItem();
    }
  }

  /**
   * Tests product category configuration validation.
   * Verifies that no exception is thrown for valid category setup.
   */
  @Test
  void testProductCategoryConfiguration() {
    when(ieConfig.getType()).thenReturn("C");
    when(configLine.getProductCategory()).thenReturn(productCategory);
    when(updateEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));
      verify(configLine, atLeastOnce()).getProductCategory();
    }
  }

  /**
   * Tests entity validation with null configuration type.
   * Verifies handling of null configuration scenarios.
   */
  @Test
  void testCheckEntityWithNullConfiguration() {
    when(ieConfig.getType()).thenReturn(null);
    when(updateEvent.getTargetInstance()).thenReturn(configLine);

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(ETBIUtils.class)) {
      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(new ArrayList<>());

      assertDoesNotThrow(() -> validator.onUpdate(updateEvent));
    }
  }

  /**
   * Tests duplicate line detection functionality.
   * Verifies exception throwing for duplicate sales representative configurations.
   */
  @Test
  void testCheckRepeatedLinesThrowsExceptionWhenDuplicateFound() {
    when(ieConfig.getType()).thenReturn("SR");
    when(configLine.getSalesRepresentative()).thenReturn(mock(String.valueOf(User.class)));
    when(updateEvent.getTargetInstance()).thenReturn(configLine);

    List<IEConfigurationLine> existingLines = new ArrayList<>();
    existingLines.add(mock(IEConfigurationLine.class));

    try (MockedStatic<ETBIUtils> mockedUtils = mockStatic(
        ETBIUtils.class); MockedStatic<OBMessageUtils> mockedMessageUtils = mockStatic(OBMessageUtils.class)) {

      mockedUtils.when(() -> ETBIUtils.getLinesCriteria(any())).thenReturn(criteria);
      when(criteria.list()).thenReturn(existingLines);

      mockedMessageUtils.when(() -> OBMessageUtils.messageBD("etbiie_repeatedLine")).thenReturn("Repeated line found");

      OBException exception = assertThrows(OBException.class, () -> validator.onUpdate(updateEvent));
      assertEquals("Repeated line found", exception.getMessage());
    }
  }
}
