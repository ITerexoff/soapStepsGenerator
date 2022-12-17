package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.generators.context.GenerateContext;
import com.squareup.javapoet.JavaFile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hamcrest.MatcherAssert;

import java.io.IOException;
import java.nio.file.Path;

import static com.iterexoff.soapStepsGenerator.constants.GenerateCodeConstants.ASSERT_THAT_METHOD_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaFilesGenerator {

    private static class Holder {
        private static final JavaFilesGenerator INSTANCE = new JavaFilesGenerator();
    }

    public static JavaFilesGenerator getInstance() {
        return JavaFilesGenerator.Holder.INSTANCE;
    }

    public void generate(GenerateContext mainGenerateContext) {
        mainGenerateContext.extractAllGenerateContexts(generateContext -> !generateContext.isInputClassInner())
                .stream()
                .map(generateContext ->
                        generateContext.getJavaFileBuilder()
                                .addStaticImport(MatcherAssert.class, ASSERT_THAT_METHOD_NAME)
                                .build()
                )
                .forEach(javaFile -> writeToFile(javaFile, mainGenerateContext.getResultsJavaFilePath()));
    }

    private void writeToFile(JavaFile javaFile, Path path) {
        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);//fixme
        }
    }
}
