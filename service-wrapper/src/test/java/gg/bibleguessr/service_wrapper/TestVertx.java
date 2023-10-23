package gg.bibleguessr.service_wrapper;

import gg.bibleguessr.service_wrapper.intake.HTTPIntake;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestVertx {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    ServiceWrapper wrapper = new ServiceWrapper();
    wrapper.initializeConfig();
    vertx.deployVerticle(
      new HTTPIntake(wrapper.getIntakeMgr(), wrapper.getConfig().vertxPort()),
      testContext.succeeding(id -> testContext.completeNow())
    );
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
}
