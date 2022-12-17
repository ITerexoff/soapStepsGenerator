package com.example.soapStepsGenerator.generators;

import com.example.soapStepsGenerator.external.utils.DateUtils;
import com.example.soapStepsGenerator.generators.context.HandleListFieldContext;
import com.example.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.*;
import io.qameta.allure.Step;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.soapStepsGenerator.constants.GenerateCodeConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StepsForListFieldGenerator {

    private final CommonEntitiesGenerator commonEntitiesGenerator = CommonEntitiesGenerator.getInstance();

    private static class Holder {
        private static final StepsForListFieldGenerator INSTANCE = new StepsForListFieldGenerator();
    }

    public static StepsForListFieldGenerator getInstance() {
        return StepsForListFieldGenerator.Holder.INSTANCE;
    }

    public void addFilterItemsInListField(StepForFieldGenerateContext stepForFieldGenerateContext) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        String filterItemsInListFieldName = handleListFieldContext.getFilterItemsInListFieldName();

        FieldSpec filterItemsInListField = FieldSpec.builder(handleListFieldContext.getTypeNameOfFieldOfFilteringItemsInList(), filterItemsInListFieldName, Modifier.PRIVATE)
                .addAnnotation(Setter.class)
                .addAnnotation(ACCESSORS_ANNOTATION_SPEC)
                .initializer("$N -> true", filterItemsInListFieldName)
                .build();

        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addField(filterItemsInListField);
    }

    public void addGetFilteredItemsStreamMethod(StepForFieldGenerateContext stepForFieldGenerateContext) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();

        MethodSpec getItemsStreamMethod = MethodSpec.methodBuilder(handleListFieldContext.getGetFilteredItemsStreamMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(Stream.class, handleListFieldContext.getItemType()))
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addCode("""
                                return $N.get$N()
                                        .stream()
                                        .filter($N);
                                """,
                        stepForFieldGenerateContext.getCheckClassFieldName(),
                        handleListFieldContext.getCapitalizeListFieldName(),
                        handleListFieldContext.getFilterItemsInListFieldName())
                .build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(getItemsStreamMethod);
    }

    public void addExtractItemStepsFromFilteredListMethod(StepForFieldGenerateContext stepForFieldGenerateContext) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверки полей {0}-го отфильтрованного элемента из списка $N:\"", handleListFieldContext.getListField().getName())
                .build();
        String indexParameterName = "index";
        String methodName = String.format("extractItemStepsFromFiltered%s", handleListFieldContext.getCapitalizeListFieldName());

        ClassName generatingItemStepsClassName = commonEntitiesGenerator.getGeneratingClassName(handleListFieldContext.getItemClass(), stepForFieldGenerateContext);
        MethodSpec extractItemStepsFromListMethod = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(generatingItemStepsClassName)
                .addParameter(int.class, indexParameterName)
                .addStatement(
                        "$T $N = $N($N).orElse(null)",
                        handleListFieldContext.getItemClass(),
                        handleListFieldContext.getUncapitalizeItemClassName(),
                        handleListFieldContext.getExtractItemFromFilteredListMethodName(),
                        indexParameterName
                )
                .addStatement("return new $T($N)", generatingItemStepsClassName, handleListFieldContext.getUncapitalizeItemClassName())
                .build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(extractItemStepsFromListMethod);
    }

    public void addExtractItemFromFilteredListMethod(StepForFieldGenerateContext stepForFieldGenerateContext) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        String indexParameterName = "index";

        MethodSpec extractItemFromListMethod = MethodSpec.methodBuilder(handleListFieldContext.getExtractItemFromFilteredListMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(handleListFieldContext.getTypeNameOfOptionalItem())
                .addParameter(int.class, indexParameterName)
                .addCode("""
                                return $N()
                                        .skip($N)
                                        .findFirst();
                                """,
                        handleListFieldContext.getGetFilteredItemsStreamMethodName(),
                        indexParameterName
                )
                .build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(extractItemFromListMethod);
    }

    public void addCheckFilteredItemMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка {0}-го отфильтрованного элемента списка $N {0}\"", handleListFieldContext.getCapitalizeListFieldName())
                .build();
        String indexParameterName = "index";
        String methodName = String.format("checkFilteredItemFrom%s", handleListFieldContext.getCapitalizeListFieldName());

        MethodSpec.Builder checkFilteredItemMethodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(int.class, indexParameterName)
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(handleListFieldContext.getItemType()))
                .addStatement(
                        "$T $N = $N($N).orElse(null)",
                        handleListFieldContext.getItemClass(),
                        handleListFieldContext.getUncapitalizeItemClassName(),
                        handleListFieldContext.getExtractItemFromFilteredListMethodName(),
                        indexParameterName
                )
                .addStatement("$T $N = \"Отфильтрованное значение элемента коллекции $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName);
        commonEntitiesGenerator.addAssertThatStatement(checkFilteredItemMethodBuilder, handleListFieldContext.getUncapitalizeItemClassName(), MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkFilteredItemMethodBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }

    public void addCheckFilteredItemAsOffsetDateTimeMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка {0}-го отфильтрованного элемента списка $N {0}\"", handleListFieldContext.getCapitalizeListFieldName())
                .build();
        String indexParameterName = "index";
        String methodName = String.format("checkFilteredItemAsOffsetDateTimeFrom%s", handleListFieldContext.getCapitalizeListFieldName());

        MethodSpec.Builder checkFilteredItemMethodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(int.class, indexParameterName)
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(OffsetDateTime.class))
                .addStatement(
                        "$T $N = $N($N).orElse(null)",
                        handleListFieldContext.getItemClass(),
                        handleListFieldContext.getUncapitalizeItemClassName(),
                        handleListFieldContext.getExtractItemFromFilteredListMethodName(),
                        indexParameterName
                )
                .addStatement("$T $N = \"Отфильтрованное значение элемента коллекции $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName)
                .addStatement(
                        "$N($N($N), $T.toOffsetDateTime($N), $N)",
                        ASSERT_THAT_METHOD_NAME,
                        GET_ERROR_MSG_METHOD_NAME,
                        ERROR_MSG_FIELD_NAME,
                        ClassName.get(stepForFieldGenerateContext.getExternalDateUtilPackageName(), DateUtils.class.getSimpleName()),
                        stepForFieldGenerateContext.getCheckClassFieldName(),
                        handleListFieldContext.getUncapitalizeItemClassName(),
                        MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkFilteredItemMethodBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }

    public void addCheckFilteredListMethod(StepForFieldGenerateContext stepForFieldGenerateContext, Field field) {
        String fieldName = field.getName();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверка отфильтрованного списка $N {0}\"", fieldName)
                .build();
        HandleListFieldContext handleListFieldContext = stepForFieldGenerateContext.getHandleListFieldContext();
        String filteredListFieldName = "filteredList";
        String capitalizeFieldName = StringUtils.capitalize(fieldName);

        MethodSpec.Builder checkMethodSpecBuilder = MethodSpec.methodBuilder("checkFiltered" + capitalizeFieldName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .returns(stepForFieldGenerateContext.getGeneratingClassName())
                .addParameter(commonEntitiesGenerator.getMatcherParameterSpec(field.getGenericType()))
                .addStatement("$T $N = \"Значение отфильтрованного списка $N не равно ожидаемому\"", String.class, ERROR_MSG_FIELD_NAME, fieldName)
                .addStatement("$T $N = $N().collect($T.toList())",
                        ParameterizedTypeName.get(List.class, handleListFieldContext.getItemType()),
                        filteredListFieldName,
                        handleListFieldContext.getGetFilteredItemsStreamMethodName(),
                        Collectors.class);
        commonEntitiesGenerator.addAssertThatStatement(checkMethodSpecBuilder, filteredListFieldName, MATCHER_PARAMETER_NAME);

        MethodSpec checkMethodSpec = checkMethodSpecBuilder.addStatement("return this").build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(checkMethodSpec);
    }
}
