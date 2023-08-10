package com.iterexoff;

import com.iterexoff.soapStepsGenerator.generators.Generator;
import com.iterexoff.soapStepsGenerator.model.dto.GeneratorInputs;
import com.iterexoff.soapStepsGenerator.model.dto.java.StaticImport;
import com.squareup.javapoet.ClassName;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Arrays;
import java.util.List;

@Mojo(name = "generateSoapSteps", defaultPhase = LifecyclePhase.NONE)
public class GenerateSoapStepsMojo extends AbstractMojo {

    /**
     * Path to compiled class files of wsdl.
     */
    @Parameter(property = "classFilesPath")
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

    //fixme incorrect formatting of description in pom.xml (where this plugin is using).
    /**
     * <p>
     * Imports that you need to add to generated class of steps for wsdl method.
     * </p>
     *
     * <pre>
     * &lt;configuration&gt;
     *   &lt;additionalStaticImportsInWsdlCallClassSteps&gt;
     *     &lt;additionalStaticImportInWsdlCallClassSteps&gt;
     *       &lt;packageName&gt;Package name of class for static import.&lt;/packageName&gt;
     *       &lt;simpleName&gt;Simple name of class for static import.&lt;/simpleName&gt;
     *       &lt;paramName&gt;Name of static param for static import.&lt;/paramName&gt;
     *     &lt;/additionalStaticImportInWsdlCallClassSteps&gt;
     *     &lt;!-- ... more ... --&gt;
     *   &lt;/additionalStaticImportsInWsdlCallClassSteps&gt;
     * &lt;/configuration&gt;
     * </pre>
     */
    @Parameter(property = "additionalStaticImportsInWsdlInterfaceSteps", required = true)
    protected StaticImportMojo[] additionalStaticImportsInWsdlCallClassSteps;

    /**
     * This if code template for get wsdl server for wsdl url.
     * Example: System.getProperty("myWsdlLocation")
     * Example: "127.0.0.1:9001"
     */
    @Parameter(property = "getWsdlInterfaceLocationTemplate", required = true)
    protected String getWsdlInterfaceLocationTemplate;

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
                .setWsdlInterfaces(wsdlInterfaces)
                .setPortTypeServiceClassName(portTypeServiceClassName)
                .setExternalAbstractSoapStepPackageName(externalAbstractSoapStepPackageName)
                .setResultStepsPackageName(resultStepsPackageName)
                .setExcludePathsFromPackageName(excludePathsFromPackageName)
                .setExternalDateUtilPackageName(externalDateUtilPackageName)
                .setGetWsdlInterfaceLocationTemplate(getWsdlInterfaceLocationTemplate)
                .setResultsJavaFilesPath(resultsJavaFilesPath);

        List<StaticImport> additionalStaticImportsInWsdlCallClassSteps = Arrays.stream(this.additionalStaticImportsInWsdlCallClassSteps)
                .map(staticImportMojo ->
                        new StaticImport(
                                ClassName.get(staticImportMojo.getPackageName(), staticImportMojo.getSimpleName()),
                                staticImportMojo.getParamName()
                        )
                )
                .toList();
        generatorInputs.setAdditionalStaticImportsToWsdlCallClassSteps(additionalStaticImportsInWsdlCallClassSteps);

        if (StringUtils.isNotBlank(classFilesPath))
            generatorInputs.setClassFilesPath(classFilesPath);

        Generator.getInstance().generate(generatorInputs);

        getLog().info("Generating soap steps has been ended.");
    }
}
