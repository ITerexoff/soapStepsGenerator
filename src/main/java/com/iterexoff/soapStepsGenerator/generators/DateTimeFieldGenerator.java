package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.external.utils.DateUtils;
import com.iterexoff.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import io.qameta.allure.Step;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static com.iterexoff.soapStepsGenerator.constants.GenerateCodeConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeFieldGenerator {

    private final CommonEntitiesGenerator commonEntitiesGenerator = CommonEntitiesGenerator.getInstance();

    private static class Holder {
        private static final DateTimeFieldGenerator INSTANCE = new DateTimeFieldGenerator();
    }

    public static DateTimeFieldGenerator getInstance() {
        return DateTimeFieldGenerator.Holder.INSTANCE;
    }

    //method customized for my currentProject (using custom static method toOffsetDateTime)
    public void addCheckFieldWithOffsetDateTimeMatcherMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка поля $N {0}\"", fieldName)
                .build();

        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        MethodSpec.Builder checkMethodSpecBuilder = MethodSpec.methodBuilder("check" + capitalizeFieldName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(OffsetDateTime.class))
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("$T $N = \"Значение поля $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName)
                .addStatement(
                        "$N($N($N), $T.toOffsetDateTime($N.get$N()), $N)",
                        ASSERT_THAT_METHOD_NAME,
                        GET_ERROR_MSG_METHOD_NAME,
                        ERROR_MSG_FIELD_NAME,
                        ClassName.get(stepForFieldGenerateContext.getExternalDateUtilPackageName(), DateUtils.class.getSimpleName()),
                        stepForFieldGenerateContext.getCheckClassFieldName(),
                        capitalizeFieldName,
                        MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkMethodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }

    //method customized for my currentProject (using custom static method toOffsetDateTime)
    public void addCheckFieldWithXMLGregorianCalendarMatcherMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка поля $N {0}\"", fieldName)
                .build();

        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        MethodSpec.Builder checkMethodSpecBuilder = MethodSpec.methodBuilder("check" + capitalizeFieldName + "WithXMLGregorianCalendarMatcher")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(field.getType()))
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("$T $N = \"Значение поля $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName);
        commonEntitiesGenerator.addAssertThatStatement(stepForFieldGenerateContext, checkMethodSpecBuilder, field, MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkMethodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }

}
