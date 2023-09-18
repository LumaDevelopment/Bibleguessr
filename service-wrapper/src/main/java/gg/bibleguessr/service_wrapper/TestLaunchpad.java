package gg.bibleguessr.service_wrapper;

import gg.bibleguessr.service_wrapper.example_service.ExampleService;

public class TestLaunchpad {

  public static void main(String[] args) {

    // Our current test is to just run the example microservice.
    new ServiceWrapper().run(new ExampleService());

  }

}
