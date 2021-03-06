package org.folio.rest.impl;

import static org.folio.rest.RestVerticle.MODULE_SPECIFIC_ARGS;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.folio.rest.jaxrs.model.Parameter;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.TenantLoading;
import org.folio.rest.tools.utils.TenantTool;

public class TenantReferenceAPI extends TenantAPI {
  private static final Logger log = LogManager.getLogger(TenantReferenceAPI.class);

  private static final String PARAMETER_LOAD_SAMPLE = "loadSample";
  private static final String PARAMETER_LOAD_SYSTEM = "loadSystem";

  @Override
  public Future<Integer> loadData(TenantAttributes attributes, String tenantId, Map<String, String> headers, Context vertxContext) {
    log.info("postTenant");
    Vertx vertx = vertxContext.owner();
    Promise<Integer> promise = Promise.promise();

    //Always load this system data
    Parameter parameter = new Parameter().withKey(PARAMETER_LOAD_SYSTEM).withValue("true");
    attributes.getParameters().add(parameter);

    TenantLoading tl = new TenantLoading();
    buildDataLoadingParameters(attributes, tl);

    tl.perform(attributes, headers, vertx, res1 -> {
      if (res1.failed()) {
        promise.fail(res1.cause());

      } else {
        promise.complete(res1.result());
      }

    });

    return promise.future();
  }

  private boolean buildDataLoadingParameters(TenantAttributes tenantAttributes, TenantLoading tl) {
    tl.withKey(PARAMETER_LOAD_SYSTEM)
      .withLead("data/system")
      .add("reasons-for-closure", "orders-storage/configuration/reasons-for-closure");
    if (isLoadSample(tenantAttributes)) {
      tl.withKey(PARAMETER_LOAD_SAMPLE)
        .withLead("data")
        .add("alerts", "orders-storage/alerts")
        .add("reporting-codes", "orders-storage/reporting-codes")
        .add("purchase-orders", "orders-storage/purchase-orders")
        .add("po-lines", "orders-storage/po-lines")
        .add("titles", "orders-storage/titles")
        .add("order-templates", "orders-storage/order-templates")
        .add("acquisitions-units", "acquisitions-units-storage/units")
        .add("acquisitions-units-memberships", "acquisitions-units-storage/memberships")
        .add("configuration/reasons-for-closure", "orders-storage/configuration/reasons-for-closure")
        .add("configuration/prefixes", "orders-storage/configuration/prefixes")
        .add("configuration/suffixes", "orders-storage/configuration/suffixes");
    }
    return true;
  }

  private boolean isLoadSample(TenantAttributes tenantAttributes) {
    // if a system parameter is passed from command line, ex: loadSample=true
    // that value is considered,Priority of Parameters:
    // Tenant Attributes > command line parameter > default(false)
    boolean loadSample = Boolean.parseBoolean(MODULE_SPECIFIC_ARGS.getOrDefault(PARAMETER_LOAD_SAMPLE,
        "false"));
    List<Parameter> parameters = tenantAttributes.getParameters();
    for (Parameter parameter : parameters) {
      if (PARAMETER_LOAD_SAMPLE.equals(parameter.getKey())) {
        loadSample = Boolean.parseBoolean(parameter.getValue());
      }
    }
    return loadSample;

  }

  @Override
  public void deleteTenantByOperationId(String operationId, Map<String, String> headers, Handler<AsyncResult<Response>> hndlr,
    Context cntxt) {
    log.info("deleteTenant");
    super.deleteTenantByOperationId(operationId, headers, res -> {
      Vertx vertx = cntxt.owner();
      String tenantId = TenantTool.tenantId(headers);
      PostgresClient.getInstance(vertx, tenantId)
        .closeClient(event -> hndlr.handle(res));
    }, cntxt);
  }
}
