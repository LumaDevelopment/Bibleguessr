package gg.bibleguessr.api_gateway;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a service wrapper, accessed through any type of communication protocol.
 *
 * @param <T> The configuration type of the service wrapper, for
 *            instance, this is String for HTTP because we store
 *            the URL.
 */
public class ServiceWrapperInfo<T> implements Comparable<ServiceWrapperInfo<?>> {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The configuration of how to access the service wrapper.
     */
    private final T config;

    /**
     * The last time we made a request to this service wrapper.
     */
    private long lastRequestTimestamp;

    /* ---------- CONSTRUCTORS ---------- */

    /**
     * Creates a ServiceWrapperInfo instance with the given configuration,
     * and the last request timestamp is set at the current system
     * time in milliseconds since the epoch.
     *
     * @param config The configuration of how to access the service wrapper.
     */
    public ServiceWrapperInfo(T config) {
        this(config, System.currentTimeMillis());
    }

    /**
     * Creates a ServiceWrapperInfo instance with the given configuration,
     * and the last request timestamp is set at the given timestamp.
     *
     * @param config           The configuration of how to access the service wrapper.
     * @param initialTimestamp The initial timestamp to set the last request timestamp to.
     */
    public ServiceWrapperInfo(T config, long initialTimestamp) {

        if (config == null) {
            throw new RuntimeException("Cannot create ServiceWrapperInfo will null service wrapper configuration!");
        }

        this.config = config;
        this.lastRequestTimestamp = initialTimestamp;

    }

    /* ---------- METHODS ---------- */

    /**
     * Get the configuration of how to access the service wrapper.
     *
     * @return The configuration.
     */
    public T getConfig() {
        return this.config;
    }

    /**
     * Set the last time that we sent a request
     * to this service wrapper as the current
     * time.
     */
    public void updateWhenLastRequestSent() {
        this.lastRequestTimestamp = System.currentTimeMillis();
    }

    /**
     * Compare this service wrapper info object to
     * another service wrapper info object on the
     * basis of the last request timestamp.
     *
     * @param o the object to be compared.
     * @return 0 if the timestamps are equal, a negative
     * number if this object's timestamp is less
     * than the other object's timestamp, and a
     * positive number if this object's timestamp
     * is greater than the other object's timestamp.
     */
    @Override
    public int compareTo(@NotNull ServiceWrapperInfo<?> o) {
        return Long.compare(this.lastRequestTimestamp, o.lastRequestTimestamp);
    }

}
