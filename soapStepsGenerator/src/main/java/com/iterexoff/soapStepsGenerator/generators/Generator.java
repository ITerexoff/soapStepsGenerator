package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.generators.context.SoapCallGenerateContext;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;
import com.iterexoff.soapStepsGenerator.utils.ClassUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
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

        Optional<URLClassLoader> optionalURLClassLoader = ClassUtils.getURLClassLoader(classFilesPath, this.getClass().getClassLoader());
        if (!optionalURLClassLoader.isPresent()) {
            log.error("UrlClassLoader for path '{}' is null. Unable to generate soap steps.", classFilesPath);
            return;
        }
        handleWsdlInterfaces(generatorInputs, optionalURLClassLoader.get());
    }

    private void handleWsdlInterfaces(GeneratorInputs generatorInputs, URLClassLoader urlClassLoader) {
        Path classFilesPath = generatorInputs.getClassFilesPath();
        String classesPathByDot = ClassUtils.getClassesPathByDot(classFilesPath);
        Optional<Class<?>> wsdlServiceClassOptional = ClassUtils.loadClassByName(urlClassLoader, classFilesPath, classesPathByDot, generatorInputs.getPortTypeServiceClassName());

        if (!wsdlServiceClassOptional.isPresent()) {
            log.error("Unable to define wsdl service class by name '{}'. Unable to generate soap steps.", generatorInputs.getPortTypeServiceClassName());
            return;
        }

        generatorInputs.getWsdlInterfaces()
                .forEach(wsdlInterfaceStr -> {
                    log.info("Generating java files with steps for wsdl interface '{}'.", wsdlInterfaceStr);
                    Optional<Class<?>> optionalFoundWSDLInterface = ClassUtils.loadClassByName(urlClassLoader, classFilesPath, classesPathByDot, wsdlInterfaceStr);

                    if (!optionalFoundWSDLInterface.isPresent()) {
                        log.error("Unable to define wsdl interface class by name '{}'. Unable to generate soap steps.", wsdlInterfaceStr);
                        return;
                    }

                    Class<?> wsdlInterface = optionalFoundWSDLInterface.get();
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
                });
    }
}
