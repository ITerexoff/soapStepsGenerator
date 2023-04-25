package ru.tele2.autotests.soap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.xml.ws.Service;
import java.net.URL;
import java.util.function.Function;

import static io.qameta.allure.Allure.step;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

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

    protected void checkExceptionType(Class<?> expectedExceptionClass) {
        step(
                String.format("Проверка типа fault-ответа = %s", expectedExceptionClass.getSimpleName()),
                () -> {
                    String errorMsg = String.format("Тип fault-ответа soap-запроса %s не соответствует ожидаемому.", getRequestName());
                    assertThat(errorMsg, getException(), instanceOf(expectedExceptionClass));
                }
        );
    }
}
