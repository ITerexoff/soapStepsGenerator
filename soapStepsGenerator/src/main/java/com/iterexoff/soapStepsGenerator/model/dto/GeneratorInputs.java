package com.iterexoff.soapStepsGenerator.model.dto;

import com.iterexoff.soapStepsGenerator.model.dto.java.StaticImport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class GeneratorInputs {

    private Path classFilesPath;
    private List<String> wsdlInterfaces = new ArrayList<>();
    ;
    private String portTypeServiceClassName;
    private String externalAbstractSoapStepPackageName;
    protected String externalDateUtilPackageName;
    protected List<StaticImport> additionalStaticImportsToWsdlCallClassSteps = new ArrayList<>();
    ;
    protected String getWsdlInterfaceLocationTemplate;
    protected String resultStepsPackageName;
    protected List<String> excludePathsFromPackageName = new ArrayList<>();
    protected Path resultsJavaFilesPath;

    public GeneratorInputs setClassFilesPath(String classFilesPath) {
        this.classFilesPath = Path.of(classFilesPath);
        return this;
    }

    public GeneratorInputs setWsdlInterface(String wsdlInterface) {
        CollectionUtils.addIgnoreNull(wsdlInterfaces, wsdlInterface);
        return this;
    }

    public GeneratorInputs setWsdlInterfaces(String... wsdlInterfaces) {
        this.wsdlInterfaces = Arrays.asList(wsdlInterfaces);
        return this;
    }

    public GeneratorInputs setWsdlInterfaces(List<String> wsdlInterfaces) {
        this.wsdlInterfaces = wsdlInterfaces;
        return this;
    }

    public GeneratorInputs setExcludePathsFromPackageName(String excludePathFromPackageName) {
        CollectionUtils.addIgnoreNull(excludePathsFromPackageName, excludePathFromPackageName);
        return this;
    }

    public GeneratorInputs setExcludePathsFromPackageName(String... excludeFromPackageName) {
        this.excludePathsFromPackageName = Arrays.asList(excludeFromPackageName);
        return this;
    }

    public GeneratorInputs setExcludePathsFromPackageName(List<String> excludeFromPackageName) {
        this.excludePathsFromPackageName = excludeFromPackageName;
        return this;
    }

    public GeneratorInputs setResultsJavaFilesPath(String resultsJavaFilesPath) {
        this.resultsJavaFilesPath = Path.of(resultsJavaFilesPath);
        return this;
    }

    public GeneratorInputs setAdditionalStaticImportsToWsdlCallClassStep(StaticImport additionalStaticImportInWsdlCallClassSteps) {
        CollectionUtils.addIgnoreNull(additionalStaticImportsToWsdlCallClassSteps, additionalStaticImportInWsdlCallClassSteps);
        return this;
    }

    public GeneratorInputs setAdditionalStaticImportsToWsdlCallClassSteps(StaticImport... additionalStaticImportsToWsdlCallClassSteps) {
        this.additionalStaticImportsToWsdlCallClassSteps = Arrays.asList(additionalStaticImportsToWsdlCallClassSteps);
        return this;
    }

    public GeneratorInputs setAdditionalStaticImportsToWsdlCallClassSteps(List<StaticImport> additionalStaticImportsToWsdlCallClassSteps) {
        this.additionalStaticImportsToWsdlCallClassSteps = additionalStaticImportsToWsdlCallClassSteps;
        return this;
    }
}
