package com.iterexoff.soapStepsGenerator;

import com.iterexoff.soapStepsGenerator.generators.Generator;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;

public class SoapStepGeneratorApplication {

    public static void main(String[] args) {
        GeneratorInputs generatorInputs = new GeneratorInputs()
                .setClassFilesPath("c:\\Projects\\\\YourProjectWithWsdlClasses\\target\\classes\\")
                .setWsdlInterfaces(
                        "YourWsdlInterfaceName"
                )
                .setPortTypeServiceClassName("YourWsdlService")
                .setExternalAbstractSoapStepPackageName("your.company.autotests.soap")
                .setResultStepsPackageName("your.company.autotests.steps.soap")
                .setExcludePathsFromPackageName("tro.lo.lo", "your.company.autotests.soap")
                .setExternalDateUtilPackageName("your.company.autotests.utils")
                .setResultsJavaFilesPath("target/generated-sources");

        Generator.getInstance().generate(generatorInputs);
    }

}
