package gg.bibleguessr.service_wrapper;

import gg.bibleguessr.service_wrapper.example_service.ExampleRequest;
import gg.bibleguessr.service_wrapper.example_service.ExampleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestServiceWrapper {

  ServiceWrapper wrapper;

  @BeforeEach
  void setup() {

    wrapper = new ServiceWrapper();

    ServiceWrapperConfig config = new ServiceWrapperConfig(
      false,
      0,
      false,
      "",
      "",
      "",
      0,
      "",
      "",
      "",
      "",
      "",
      ""
    );

    wrapper.setConfig(config);

  }

  @Test
  @DisplayName("Logger Name Is Valid")
  void testLoggerNameIsValid() {
    Assertions.assertNotNull(ServiceWrapper.LOGGER_NAME);
    Assertions.assertFalse(ServiceWrapper.LOGGER_NAME.isBlank());
  }

  @Test
  @DisplayName("Default Config File Path Is Valid")
  void testDefaultConfigFilePathIsValid() {
    Assertions.assertNotNull(ServiceWrapper.DEFAULT_CONFIG_FILE_PATH);
    Assertions.assertFalse(ServiceWrapper.DEFAULT_CONFIG_FILE_PATH.isBlank());
  }

  @Test
  @DisplayName("Can Run Microservice")
  void canRunMicroservice() {
    wrapper.run(new ExampleService());
    Assertions.assertNotNull(wrapper.getRunningMicroservice(ExampleService.ID));
  }

  @Test
  @DisplayName("Can Stop Microservice")
  void canStopMicroservice() {
    ExampleService service = new ExampleService();
    wrapper.run(service);
    wrapper.stop(service.getID());
    Assertions.assertTrue(service.hasBeenStopped());
  }

  @Test
  @DisplayName("Can Create New Request")
  void canCreateNewRequest() {
    Map<String, String> content = Map.of("msg", "Divisible by 2");
    Assertions.assertNotNull(Request.parse(ExampleRequest.class, content));
  }

  @Test
  @DisplayName("Can Execute Request")
  void canExecuteRequest() {

    ExampleService service = new ExampleService();
    wrapper.run(service);

    Map<String, String> content = Map.of("msg", "Divisible by 2");
    Request request = Request.parse(ExampleRequest.class, content);

    Assertions.assertNotNull(wrapper.executeRequest(request));

  }

  @Test
  @DisplayName("Can Get Clean Service Name")
  void canGetCleanServiceName() {
    Assertions.assertEquals("<NULL>", wrapper.getCleanServiceName(null));
    Assertions.assertEquals("ExampleService", wrapper.getCleanServiceName(new ExampleService()));
  }

  @Test
  @DisplayName("Can Get Request Type From Path")
  void canGetRequestTypeFromPath() {
    ExampleService service = new ExampleService();
    Assertions.assertEquals(service.getRequestTypeFromPath("example-request"), ExampleRequest.class);
  }

  @Test
  @DisplayName("Microservice Initializes Request Types")
  void microserviceInitializesRequestTypes() {
    ExampleService service = new ExampleService();
    Assertions.assertTrue(service.getRequestTypes().contains(ExampleRequest.class));
  }

  @Test
  @DisplayName("Can Get Request Object From Class")
  void canGetRequestObjectFromClass() {
    Assertions.assertNotNull(Request.requestObjFromClass(ExampleRequest.class));
  }

  @Test
  @DisplayName("Is Request Identifiable When UUID Present")
  void isRequestIdentifiableWhenUUIDPresent() {
    Map<String, String> content = new HashMap<>();
    content.put("msg", "Hi");
    content.put("uuid", "d2004b89-ffb4-449f-9888-85188d5bc75a");
    Request request = Request.parse(ExampleRequest.class, content);

    Assertions.assertTrue(request.isIdentifiable());
    Assertions.assertNotNull(request.getUUID());
  }

  @Test
  @DisplayName("Are Responses Serializable")
  void areResponsesSerializable() {
    Response response = new Response(Map.of("msg", "Hi"), null);
    Assertions.assertNotNull(response.toJSONNode());
    Assertions.assertNotNull(response.toJSONString());
  }

}
