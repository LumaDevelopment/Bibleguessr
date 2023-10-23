package gg.bibleguessr.service_wrapper.intake;

/**
 * An interface for all classes which take
 * requests over a communication protocol.
 */
public interface CommsIntake {

  /**
   * To the extent that is possible, attempt to
   * initialize all communication related objects
   * and return whether that was successful. This
   * is not always possible. For instance, the
   * HTTPIntake must be deployed by an external
   * object, so its initialize() method always
   * returns <code>true</code>.
   *
   * @return Whether communication initialization
   * was successful.
   */
  boolean initialize();

  /**
   * Shut down all communications, this probably
   * means the Service Wrapper is shutting off
   * completely.
   */
  void shutdown();

}
