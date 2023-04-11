package com.iterexoff.soapStepsGenerator.generators.context;

import com.iterexoff.soapStepsGenerator.external.utils.DateUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.iterexoff.soapStepsGenerator.constants.GenerateCodeConstants.INDENT;

@Getter
@Setter
@Accessors(chain = true)
public abstract class GenerateContext {

    protected ClassName generatingClassName;
    protected TypeSpec.Builder generatingClassSpecBuilder;
    protected Set<StaticImport> staticImports = new HashSet<>();
    protected List<String> excludeFromPackageName;
    protected String resultStepsPackageName;
    protected String externalDateUtilPackageName;
    protected List<GenerateContext> childGenerateContexts = new ArrayList<>();
    protected GenerateContext parentGenerateContext;
    protected Path resultsJavaFilePath;

    public JavaFile.Builder getJavaFileBuilder() {
        JavaFile.Builder javaFileBuilder = JavaFile.builder(generatingClassName.packageName(), generatingClassSpecBuilder.build())
                .indent(INDENT);
        staticImports.forEach(staticImport -> staticImport.addStaticImportTo(javaFileBuilder));
        return javaFileBuilder;
    }

    public void addStaticImport(Class<?> clazz, String... names) {
        staticImports.add(new StaticImport(clazz, names));
    }

    public String getExternalDateUtilPackageName() {
        return externalDateUtilPackageName == null ? DateUtils.class.getPackageName() : externalDateUtilPackageName;
    }

    public List<GenerateContext> extractAllGenerateContexts(Predicate<GenerateContext> filterGenerateContext) {
        List<GenerateContext> generateContexts = childGenerateContexts.stream()
                .map(generateContext -> generateContext.extractAllGenerateContexts(filterGenerateContext))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (filterGenerateContext.test(this))
            generateContexts.add(this);
        return generateContexts;
    }

    public GenerateContext setExcludeFromPackageName(String... excludeFromPackageNames) {
        this.excludeFromPackageName = Arrays.asList(excludeFromPackageNames);
        return this;
    }

    public abstract boolean isInputClassInner();

    @Getter
    @Setter
    public static class StaticImport {
        private Class<?> clazz;
        private String[] names;

        public StaticImport(Class<?> clazz, String... names) {
            this.clazz = clazz;
            this.names = names;
        }

        public void addStaticImportTo(JavaFile.Builder javaFileBuilder) {
            javaFileBuilder.addStaticImport(clazz, names);
        }
    }
}
