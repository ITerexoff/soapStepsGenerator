package com.iterexoff;

import com.iterexoff.soapStepsGenerator.generators.Generator;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generateSoapSteps", defaultPhase = LifecyclePhase.COMPILE)
public class GenerateSoapStepsMojo extends AbstractMojo {

    /**
     * Path to compiled class files of wsdl.
     */
    @Parameter(property = "classFilesPath", required = true)
    private String classFilesPath;

    /**
     * List of names of classes (without extension) with wsdl interfaces for which need to generate steps files.
     */
    @Parameter(property = "wsdlInterfaces", required = true)
    private String[] wsdlInterfaces;

    /**
     * Name of portType class (without extension) for wsdl interfaces.
     */
    @Parameter(property = "portTypeServiceClassName", required = true)
    private String portTypeServiceClassName;

    /**
     * Name of package of class AbstractSoapStep.
     */
    @Parameter(property = "externalAbstractSoapStepPackageName", required = true)
    private String externalAbstractSoapStepPackageName;

    /**
     * Name of package of class DateUtil.
     */
    @Parameter(property = "externalDateUtilPackageName", required = true)
    private String externalDateUtilPackageName;

    /**
     * Name of package of generated soap steps.
     */
    @Parameter(property = "resultStepsPackageName", required = true)
    private String resultStepsPackageName;

    /**
     * List of names of packages (or part of package) that should be excluded from result name of package.
     */
    @Parameter(property = "excludePathsFromPackageName", required = true)
    private String[] excludePathsFromPackageName;

    /**
     * Path to generated soap step files.
     */
    @Parameter(property = "resultsJavaFilesPath", required = true)
    private String resultsJavaFilesPath;

    public void execute() throws MojoExecutionException {
        getLog().info("Generating soap steps has been started.");

        GeneratorInputs generatorInputs = new GeneratorInputs()
                .setClassFilesPath(classFilesPath)
                .setWsdlInterfaces(wsdlInterfaces)
                .setPortTypeServiceClassName(portTypeServiceClassName)
                .setExternalAbstractSoapStepPackageName(externalAbstractSoapStepPackageName)
                .setResultStepsPackageName(resultStepsPackageName)
                .setExcludePathsFromPackageName(excludePathsFromPackageName)
                .setExternalDateUtilPackageName(externalDateUtilPackageName)
                .setResultsJavaFilesPath(resultsJavaFilesPath);

        Generator.getInstance().generate(generatorInputs);

        getLog().info("Generating soap steps has been ended.");
    }
}
