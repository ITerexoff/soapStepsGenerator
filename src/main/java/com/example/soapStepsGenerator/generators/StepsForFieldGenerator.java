package com.example.soapStepsGenerator.generators;

import com.example.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.qameta.allure.Step;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.core.IsNull;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;

import static com.example.soapStepsGenerator.constants.GenerateCodeConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StepsForFieldGenerator {

    private final CommonEntitiesGenerator commonEntitiesGenerator = CommonEntitiesGenerator.getInstance();

    private static class Holder {
        private static final StepsForFieldGenerator INSTANCE = new StepsForFieldGenerator();
    }

    public static StepsForFieldGenerator getInstance() {
        return StepsForFieldGenerator.Holder.INSTANCE;
    }

    public void addCheckFieldMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка поля $N {0}\"", fieldName)
                .build();

        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        MethodSpec.Builder checkMethodSpecBuilder = MethodSpec.methodBuilder("check" + capitalizeFieldName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(field.getGenericType()))
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("$T $N = \"Значение поля $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName);
        commonEntitiesGenerator.addAssertThatStatement(stepForFieldGenerateContext, checkMethodSpecBuilder, field, MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkMethodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }

    public void addCheckThatFieldIsExistMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();

        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка присутствия поля $N\"", fieldName)
                .build();

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("checkThat" + capitalizeFieldName + "IsExist")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("$T $N = \"Отсутствует поле $N\"", String.class, ERROR_MSG_FIELD_NAME, fieldName);
        commonEntitiesGenerator.addAssertThatStatement(stepForFieldGenerateContext, methodSpecBuilder, field, NOT_NULL_VALUE_MATCHER_NAME + "()");

        MethodSpec checkMethodSpec = methodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
        stepForFieldGenerateContext.addStaticImport(IsNull.class, NOT_NULL_VALUE_MATCHER_NAME);
    }

    public void addCheckThatCheckFieldIsExistMethod(StepForFieldGenerateContext stepForFieldGenerateContext) {
        String fieldName = stepForFieldGenerateContext.getCheckClassFieldName();

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addModifiers(Modifier.PRIVATE)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addStatement("$T $N = \"Отсутствует родительское поле $N\"", String.class, ERROR_MSG_FIELD_NAME, stepForFieldGenerateContext.getInputClass().getSimpleName());
        commonEntitiesGenerator.addAssertThatStatement(methodSpecBuilder, fieldName, NOT_NULL_VALUE_MATCHER_NAME + "()");

        MethodSpec checkMethodSpec = methodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
        stepForFieldGenerateContext.addStaticImport(IsNull.class, NOT_NULL_VALUE_MATCHER_NAME);
    }

    public void addCheckThatFieldIsAbsentMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка отсутствия поля $N\"", fieldName)
                .build();

        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        MethodSpec.Builder checkMethodSpecBuilder = MethodSpec.methodBuilder("checkThat" + capitalizeFieldName + "IsAbsent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("$T $N = \"Присутствует поле $N\"", String.class, ERROR_MSG_FIELD_NAME, fieldName);
        commonEntitiesGenerator.addAssertThatStatement(stepForFieldGenerateContext, checkMethodSpecBuilder, field, NULL_VALUE_MATCHER_NAME + "()");

        MethodSpec checkMethodSpec = checkMethodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
        stepForFieldGenerateContext.addStaticImport(IsNull.class, NULL_VALUE_MATCHER_NAME);
    }

}
