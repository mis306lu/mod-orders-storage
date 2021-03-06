package org.folio.rest.impl;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.folio.rest.impl.StorageTestSuite.storageUrl;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import org.folio.HttpStatus;
import org.folio.rest.jaxrs.model.PurchaseOrder;
import org.folio.rest.jaxrs.model.PurchaseOrderCollection;
import org.folio.rest.persist.EntitiesMetadataHolder;
import org.folio.rest.persist.PgUtil;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLQueryValidationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.vertx.core.Context;
import mockit.Mock;
import mockit.MockUp;

public class HelperUtilsTest extends TestBase {

  private static final String ORDERS_ENDPOINT = "/orders-storage/orders";
  private static final String RECEIVING_HISTORY_ENDPOINT = "/orders-storage/receiving-history";

  @Test
  void getEntitiesCollectionWithDistinctOnFailCqlExTest() throws Exception {
    new MockUp<PgUtil>() {
      @Mock
      PostgresClient postgresClient(Context vertxContext, Map<String, String> okapiHeaders) {
        throw new CQLQueryValidationException(null);
      }
    };
    get(storageUrl(ORDERS_ENDPOINT)).statusCode(HttpStatus.HTTP_BAD_REQUEST.toInt())
      .contentType(TEXT_PLAIN);
  }

  @Test
  void getReceivingHistoryCollectionWithDistinctOnFailCqlExTest() throws Exception {
    new MockUp<PgUtil>() {
      @Mock
      PostgresClient postgresClient(Context vertxContext, Map<String, String> okapiHeaders) {
        throw new CQLQueryValidationException(null);
      }
    };
    get(storageUrl(RECEIVING_HISTORY_ENDPOINT)).statusCode(HttpStatus.HTTP_BAD_REQUEST.toInt())
      .contentType(TEXT_PLAIN);
  }

  @Test
  @Disabled("disabled due to incompatibility of jdk11 + jmockit + jacoco"
      + "remove annotation and check with command: 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install' ")
  void entitiesMetadataHolderRespond400FailTest() throws Exception {
    new MockUp<EntitiesMetadataHolder<PurchaseOrder, PurchaseOrderCollection>>() {
      @Mock
      Method getRespond400WithTextPlainMethod() throws NoSuchMethodException {
        throw new NoSuchMethodException();
      }
    };
    get(storageUrl(ORDERS_ENDPOINT)).statusCode(HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt())
      .contentType(TEXT_PLAIN);
  }

  @Test
  @Disabled("disabled due to incompatibility of jdk11 + jmockit + jacoco"
      + "remove annotation and check with command: 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install' ")
  void entitiesMetadataHolderRespond500FailTest() throws Exception {
    new MockUp<EntitiesMetadataHolder<PurchaseOrder, PurchaseOrderCollection>>() {
      @Mock
      Method getRespond500WithTextPlainMethod() throws NoSuchMethodException {
        throw new NoSuchMethodException();
      }
    };
    get(storageUrl(ORDERS_ENDPOINT)).statusCode(HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt())
      .contentType(TEXT_PLAIN);
  }

  @Test
  @Disabled("disabled due to incompatibility of jdk11 + jmockit + jacoco"
      + "remove annotation and check with command: 'mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install' ")
  void entitiesMetadataHolderRespond200FailTest() throws Exception {
    new MockUp<EntitiesMetadataHolder<PurchaseOrder, PurchaseOrderCollection>>() {
      @Mock
      Method getRespond200WithApplicationJson() throws NoSuchMethodException {
        throw new NoSuchMethodException();
      }
    };
    get(storageUrl(ORDERS_ENDPOINT)).statusCode(HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt())
      .contentType(TEXT_PLAIN);
  }

  @Test
  void getEntitiesCollectionWithDistinctOnFailNpExTest() throws Exception {
    new MockUp<PgUtil>() {
      @Mock
      PostgresClient postgresClient(Context vertxContext, Map<String, String> okapiHeaders) {
        throw new NullPointerException();
      }
    };
    get(storageUrl(ORDERS_ENDPOINT)).statusCode(HttpStatus.HTTP_INTERNAL_SERVER_ERROR.toInt())
      .contentType(TEXT_PLAIN);
  }

  private ValidatableResponse get(URL endpoint) {
    return RestAssured.with()
      .header(TENANT_HEADER)
      .get(endpoint)
      .then();
  }
}
