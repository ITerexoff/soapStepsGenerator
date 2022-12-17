package com.example.soapStepsGenerator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtils extends org.apache.commons.lang3.reflect.TypeUtils {

    public static Optional<Type> getFirstTypeArgument(ParameterizedType genericType) {
        return getTypeArguments(genericType)
                .values()
                .stream()
                .findFirst();
    }
}
