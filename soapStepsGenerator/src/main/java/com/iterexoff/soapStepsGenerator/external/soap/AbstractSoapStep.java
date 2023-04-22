package com.iterexoff.soapStepsGenerator.external.soap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.xml.ws.Service;
import java.net.URL;
import java.util.function.Function;

public abstract class AbstractSoapStep<TYPE_PORT, REQUEST, RESPONSE> {

    @Setter(AccessLevel.PRIVATE)
    protected URL wsdlLocation;

    protected TYPE_PORT port;

    @Getter
    protected REQUEST request;

    @Getter
    protected RESPONSE response;

    @Getter
    protected Exception exception;

    protected <T extends Service> T getService(Function<URL, T> createService) {
        return createService.apply(wsdlLocation);
    }

    protected <T extends Service> void setWsdlLocation(Class<T> clazz, String hostPort) {
    }

    protected abstract TYPE_PORT getPort();

    protected abstract RESPONSE callRequest(REQUEST requestBody) throws Exception;

    protected abstract String getRequestName();

    public void perform(@NonNull REQUEST request) {
    }

}
