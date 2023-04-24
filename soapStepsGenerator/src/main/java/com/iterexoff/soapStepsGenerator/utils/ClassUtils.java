package com.iterexoff.soapStepsGenerator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassUtils extends org.apache.commons.lang3.ClassUtils {

    private static final Map<String, Class<?>> foundedClassesCache = new ConcurrentHashMap<>();

    public final static String CLASS_SUFFIX = ".class";

    public static boolean isClassFile(Path filePath) {
        return filePath.getFileName().toString().endsWith(CLASS_SUFFIX);
    }

    public static String getClassNameFrom(Path path) {
        return path.toFile().getName().replaceAll(CLASS_SUFFIX, "");
    }

    public static Optional<URLClassLoader> getURLClassLoader(Path pathWithClasses, ClassLoader parent) {
        try {
            log.debug("Get URLClassLoader for path '{}'", pathWithClasses);
            URL[] cp = new URL[]{pathWithClasses.toFile().toURI().toURL()};
            return Optional.of(new URLClassLoader(cp, parent));
        } catch (MalformedURLException e) {
            log.error("Cannot create class loader for autotests project. Exception:\n{}", e.getMessage());
            return Optional.empty();
        }
    }

    public static String getClassesPathByDot(Path pathWithClasses) {
        return pathWithClasses.toString()
                .replaceAll(StringEscapeUtils.escapeXSI(File.separator), ".") + ".";
    }

    //fixme pathWithClasses, classesPathByDot убрать
    public static Optional<Class<?>> loadClassByName(URLClassLoader urlClassLoader, Path pathWithClasses, String classesPathByDot, String className) {

        if (className == null)
            return Optional.empty();

        Class<?> classFromCache = foundedClassesCache.get(className.toLowerCase());
        if (classFromCache != null) {
            return Optional.of(classFromCache);
        }

        log.debug("There is searching class '{}' has been started.", className);
        List<Path> paths = getClassPathsFrom(className, pathWithClasses);
        log.debug("There is searching class '{}' has ended.", className);

        if (paths.isEmpty()) {
            log.error("Class with name '{}' has not found.", className);
            return Optional.empty();
        }

        if (paths.size() > 1) {
            log.error("There has found more than 1 classes with name '{}'. Paths: {}", className, paths);
            return Optional.empty();
        }

        if (urlClassLoader == null) {
            log.error("urlClassLoader is null. Cannot start search class {}", className);
            return Optional.empty();
        }

        String packageName = getPackageName(paths.get(0), className, classesPathByDot);
        try {
            Class<?> foundedClass = urlClassLoader.loadClass(String.format("%s.%s", packageName, getClassNameFrom(paths.get(0))));
            foundedClassesCache.put(className.toLowerCase(), foundedClass);
            return Optional.of(foundedClass);
        } catch (ClassNotFoundException e) {
            log.error("Error during load class '{}' from classLoader. Exception:\n{}", className, e.getMessage());
        }
        return Optional.empty();
    }

    public static List<Path> getClassPathsFrom(String className, Path walkingPath) {
        try {
            return Files.walk(walkingPath)
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(ClassUtils::isClassFile)
                    .filter(path -> path.toFile().getName().equalsIgnoreCase(className + CLASS_SUFFIX))
                    .toList();
        } catch (IOException e) {
            log.error("Error during search class '{}' in path '{}'. Exception:\n{}", className, walkingPath, ExceptionUtils.getMessage(e));
        }
        return new ArrayList<>();
    }

    public static String getPackageName(Path path, String className, String classesPathByDot) {
        String packageName = path.toString()
                .replaceAll("\\\\", ".")
                .replaceAll("/", ".")
                .replace(classesPathByDot, "");
        return StringUtils.replaceIgnoreCase(packageName, "." + className + CLASS_SUFFIX, "");
    }

    public static boolean isJavaBaseClass(Class<?> clazz) {
        return clazz.getPackageName().startsWith("java.") || clazz.getPackageName().startsWith("javax.");
    }

    public static String getGetterMethodNameForField(Class<?> classWithField, Field field) {
        String capitalizedFieldName = StringUtils.capitalize(field.getName());
        if (field.getType().isAssignableFrom(Boolean.class) || field.getType().isAssignableFrom(boolean.class)) {
            return Arrays.stream(classWithField.getDeclaredMethods())
                    .map(Method::getName)
                    .filter(methodName -> methodName.equals("is" + capitalizedFieldName))
                    .findFirst()
                    .orElseGet(() -> "get" + capitalizedFieldName);
        } else {
            return "get" + capitalizedFieldName;
        }
    }
}
