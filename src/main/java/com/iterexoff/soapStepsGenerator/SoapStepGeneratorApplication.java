package com.iterexoff.soapStepsGenerator;

import com.iterexoff.soapStepsGenerator.generators.JavaFilesGenerator;
import com.iterexoff.soapStepsGenerator.generators.SoapCallGenerator;
import com.iterexoff.soapStepsGenerator.generators.context.SoapCallGenerateContext;
import com.iterexoff.soapStepsGenerator.utils.ClassUtils;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;

public class SoapStepGeneratorApplication {

    public static void main(String[] args) {
        Path pathWithClasses = Path.of("");
        Optional<URLClassLoader> optionalUrlClassLoader = ClassUtils.getURLClassLoader(pathWithClasses);

        optionalUrlClassLoader.ifPresent(urlClassLoader -> {
            String classesPathByDot = ClassUtils.getClassesPathByDot(pathWithClasses);
            Optional<Class<?>> optionalFoundWSDLInterface = ClassUtils.loadClassByName(urlClassLoader, pathWithClasses, classesPathByDot, "");


            optionalFoundWSDLInterface
                    .ifPresent(wsdlInterface -> {
                        for (Method declaredMethod : wsdlInterface.getDeclaredMethods()) {
                            SoapCallGenerateContext generateContext = new SoapCallGenerateContext()
                                    .setWsdlInterfaceClass(wsdlInterface)
                                    .setWsdlMethod(declaredMethod)
                                    .setWsdlServiceClass(ClassUtils.loadClassByName(urlClassLoader, pathWithClasses, classesPathByDot, "").orElse(null))
                                    .setInitialWsdlInterfaceName("")
                                    .setExternalAbstractSoapStepPackageName("");
                            generateContext.setResultStepsPackageName("")
                                    .setExcludeFromPackageName("")
                                    .setExternalDateUtilPackageName("")
                                    .setResultsJavaFilePath(Path.of("target/generated-sources"));
                            SoapCallGenerator.getInstance().generate(generateContext);
                            JavaFilesGenerator.getInstance().generate(generateContext);
                        }
                    });
        });
    }

}
