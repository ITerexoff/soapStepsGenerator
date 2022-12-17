package com.example.soapStepsGenerator.generators.context;

import java.util.HashMap;
import java.util.Map;

public final class GenerateContextsHolder {

    private static final Map<Class<?>, GenerateContext> CLASSES_BY_GENERATED_CONTEXTS = new HashMap<>();

    public static boolean hasGenerateContextExistFor(Class<?> inputClass) {
        return getExistGenerateContextFor(inputClass) != null;
    }

    public static GenerateContext getExistGenerateContextFor(Class<?> inputClass) {
        return CLASSES_BY_GENERATED_CONTEXTS.get(inputClass);
    }

    public static GenerateContext putNewGenerateContext(Class<?> inputClass, GenerateContext generateContext) {
        return CLASSES_BY_GENERATED_CONTEXTS.put(inputClass, generateContext);
    }
}
