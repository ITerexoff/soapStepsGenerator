package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.generators.context.SoapCallGenerateContext;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;
import com.iterexoff.soapStepsGenerator.utils.ClassUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

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
        Optional<URLClassLoader> optionalUrlClassLoader = ClassUtils.getURLClassLoader(classFilesPath);
        String classesPathByDot = ClassUtils.getClassesPathByDot(classFilesPath);

        optionalUrlClassLoader.ifPresent(urlClassLoader -> {
            Class<?> wsdlServiceClass = ClassUtils.loadClassByName(urlClassLoader, classFilesPath, classesPathByDot, generatorInputs.getPortTypeServiceClassName())
                    .orElseThrow(() -> new RuntimeException(String.format("Unable to define wsdl service class by name '%s'", generatorInputs.getPortTypeServiceClassName())));
            generatorInputs.getWsdlInterfaces()
                    .forEach(wsdlInterfaceStr -> {
                        Optional<Class<?>> optionalFoundWSDLInterface = ClassUtils.loadClassByName(urlClassLoader, classFilesPath, classesPathByDot, wsdlInterfaceStr);

                        optionalFoundWSDLInterface
                                .ifPresent(wsdlInterface -> {
                                    for (Method declaredMethod : wsdlInterface.getDeclaredMethods()) {
                                        SoapCallGenerateContext generateContext = new SoapCallGenerateContext()
                                                .setWsdlInterfaceClass(wsdlInterface)
                                                .setWsdlMethod(declaredMethod)
                                                .setWsdlServiceClass(wsdlServiceClass)
                                                .setInitialWsdlInterfaceName(wsdlInterfaceStr);
                                        generateContext.setGeneratorInputs(generatorInputs);
                                        SoapCallGenerator.getInstance().generate(generateContext);
                                        JavaFilesGenerator.getInstance().generate(generateContext);
                                    }
                                });
                    });
        });
    }
}
