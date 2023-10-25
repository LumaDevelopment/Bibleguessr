package gg.bibleguessr.api_gateway;

import gg.bibleguessr.backend_utils.RabbitMQConfiguration;

public record ServiceWrapperInfo(
        String wrapperID,
        String url,
        RabbitMQConfiguration rabbitMQConfig
) {

    public ServiceWrapperInfo {

        if (wrapperID == null) {
            String className = ServiceWrapperInfo.class.getSimpleName();
            throw new IllegalArgumentException("Wrapper ID cannot be null in " + className + "!");
        }

    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (!(other instanceof ServiceWrapperInfo otherConfig)) {
            return false;
        }

        return wrapperID.equals(otherConfig.wrapperID);

    }

    @Override
    public int hashCode() {

        return wrapperID.hashCode();

    }

}
