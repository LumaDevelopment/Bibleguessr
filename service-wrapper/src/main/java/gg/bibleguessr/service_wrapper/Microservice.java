package gg.bibleguessr.service_wrapper;

// TODO IMPLEMENT

public abstract class Microservice {

  /* ---------- VARIABLES ---------- */

  private final String id;

  /* ---------- CONSTRUCTORS ---------- */

  public Microservice(String id) {
    this.id = id;
  }

  /* ---------- METHODS ---------- */

  public String getID() {
    return id;
  }

}
