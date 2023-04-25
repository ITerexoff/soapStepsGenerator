package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.generators.context.SoapCallGenerateContext;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;
import com.iterexoff.soapStepsGenerator.utils.ClassUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Generator {

    private static class Holder {
        private static final Generator INSTANCE = new Generator();
    }

    public static Generator getInstance() {
        return Generator.Holder.INSTANCE;
    }

    public void generate(GeneratorInputs generatorInputs) {
        Path classFilesPath = generatorInputs.getClassFilesPath();

        if (classFilesPath != null) {
            ClassUtils.getURLClassLoader(classFilesPath, this.getClass().getClassLoader())
                    .ifPresentOrElse(
                            urlClassLoader -> handleWsdlInterfaces(generatorInputs, urlClassLoader),
                            () -> log.error("UrlClassLoader for path '{}' is null. Unable to generate soap steps.", classFilesPath)
                    );
        } else {
            log.debug("ClassFilesPath hasn't set. Using system class loader.");
            handleWsdlInterfaces(generatorInputs, this.getClass().getClassLoader());
        }
    }

    private void handleWsdlInterfaces(GeneratorInputs generatorInputs, ClassLoader classLoader) {
        Optional<Class<?>> wsdlServiceClassOptional = getWsdlServiceClass(generatorInputs, classLoader);

        if (wsdlServiceClassOptional.isEmpty()) {
            log.error("Unable to define wsdl service class by name '{}'. Unable to generate soap steps.", generatorInputs.getPortTypeServiceClassName());
            return;
        }

        generatorInputs.getWsdlInterfaces()
                .forEach(wsdlInterfaceStr -> {
                    log.info("Generating java files with steps for wsdl interface '{}'.", wsdlInterfaceStr);
                    Optional<Class<?>> optionalWSDLInterfaceClass = getWSDLInterfaceClass(generatorInputs, classLoader, wsdlInterfaceStr);

                    optionalWSDLInterfaceClass
                            .ifPresentOrElse(
                                    wsdlInterface -> {
                                        for (Method declaredMethod : wsdlInterface.getDeclaredMethods()) {
                                            log.debug("Generating java files with steps for wsdl interface '{}' and method '{}'.", wsdlInterfaceStr, declaredMethod);
                                            SoapCallGenerateContext generateContext = new SoapCallGenerateContext()
                                                    .setWsdlInterfaceClass(wsdlInterface)
                                                    .setWsdlMethod(declaredMethod)
                                                    .setWsdlServiceClass(wsdlServiceClassOptional.get())
                                                    .setInitialWsdlInterfaceName(wsdlInterfaceStr);
                                            generateContext.setGeneratorInputs(generatorInputs);
                                            SoapCallGenerator.getInstance().generate(generateContext);
                                            JavaFilesGenerator.getInstance().generate(generateContext);
                                        }
                                    },
                                    () -> log.error("Unable to define wsdl interface class by name '{}'. Unable to generate soap steps.", wsdlInterfaceStr)
                            );
                });
    }

    private static Optional<Class<?>> getWSDLInterfaceClass(GeneratorInputs generatorInputs, ClassLoader classLoader, String wsdlInterfaceStr) {
        Path classFilesPath = generatorInputs.getClassFilesPath();
        if (classFilesPath != null) {
            String classesPathByDot = ClassUtils.getClassesPathByDot(classFilesPath);
            return ClassUtils.loadClassFromPathByName(classLoader, classFilesPath, classesPathByDot, wsdlInterfaceStr);
        } else {
            return ClassUtils.loadClassByName(classLoader, wsdlInterfaceStr);
        }
    }

    private static Optional<Class<?>> getWsdlServiceClass(GeneratorInputs generatorInputs, ClassLoader classLoader) {
        Path classFilesPath = generatorInputs.getClassFilesPath();
        if (classFilesPath != null) {
            String classesPathByDot = ClassUtils.getClassesPathByDot(classFilesPath);
            return ClassUtils.loadClassFromPathByName(classLoader, classFilesPath, classesPathByDot, generatorInputs.getPortTypeServiceClassName());
        } else {
            return ClassUtils.loadClassByName(classLoader, generatorInputs.getPortTypeServiceClassName());
        }
    }
}
