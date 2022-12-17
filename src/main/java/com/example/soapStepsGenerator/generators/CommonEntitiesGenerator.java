package com.example.soapStepsGenerator.generators;

import com.example.soapStepsGenerator.generators.context.GenerateContext;
import com.example.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.example.soapStepsGenerator.utils.TypeUtils;
import com.squareup.javapoet.*;
import io.qameta.allure.Step;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.example.soapStepsGenerator.constants.GenerateCodeConstants.*;
import static com.example.soapStepsGenerator.utils.ClassUtils.getGetterMethodNameForField;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonEntitiesGenerator {

    private static class Holder {
        private static final CommonEntitiesGenerator INSTANCE = new CommonEntitiesGenerator();
    }

    public static CommonEntitiesGenerator getInstance() {
        return CommonEntitiesGenerator.Holder.INSTANCE;
    }

    public ParameterSpec getMatcherParameterSpec(Type type) {
        if (type instanceof ParameterizedType && TypeUtils.isAssignable(type, List.class)) {
            Type firstArgumentType = TypeUtils.getFirstTypeArgument((ParameterizedType) type)
                    .orElseThrow(() -> new RuntimeException("Cannot get type of first argument for parametrized type " + type));
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Collection.class), WildcardTypeName.subtypeOf(firstArgumentType));
            return ParameterSpec.builder(ParameterizedTypeName.get(MATCHER_CLASS_NAME, parameterizedTypeName), MATCHER_PARAMETER_NAME).build();
            //todo - else ветку проработать
        } else {
            return ParameterSpec.builder(ParameterizedTypeName.get(MATCHER_CLASS_NAME, ClassName.get(type).box()), MATCHER_PARAMETER_NAME).build();
        }
    }

    public void addAssertThatStatement(StepForFieldGenerateContext stepForFieldGenerateContext, MethodSpec.Builder checkMethodSpecBuilder, Field checkField, String matcher) {
        checkMethodSpecBuilder.addStatement(
                "$N($N($N), $N.$N(), $N)",
                ASSERT_THAT_METHOD_NAME,
                GET_ERROR_MSG_METHOD_NAME,
                ERROR_MSG_FIELD_NAME,
                stepForFieldGenerateContext.getCheckClassFieldName(),
                getGetterMethodNameForField(stepForFieldGenerateContext.getInputClass(), checkField),
                matcher);
    }

    public void addAssertThatStatement(MethodSpec.Builder methodSpecBuilder, String fieldNameWithActualValue, String matcher) {
        methodSpecBuilder.addStatement(
                "$N($N($N), $N, $N)",
                ASSERT_THAT_METHOD_NAME,
                GET_ERROR_MSG_METHOD_NAME,
                ERROR_MSG_FIELD_NAME,
                fieldNameWithActualValue,
                matcher);
    }

    public ClassName getGeneratingClassName(Class<?> inputClass, GenerateContext generateContext) {
        if (ClassUtils.isInnerClass(inputClass) && generateContext instanceof StepForFieldGenerateContext stepForFieldGenerateContext) {
            return getGeneratingClassNameForInnerClass(inputClass, stepForFieldGenerateContext);
        } else {
            return getGeneratingClassName(ClassName.get(inputClass), generateContext);
        }
    }

    private ClassName getGeneratingClassNameForInnerClass(Class<?> inputClass, StepForFieldGenerateContext stepForFieldGenerateContext) {
        GenerateContext parentGenerateContext = stepForFieldGenerateContext.getParentGenerateContext();
        ClassName parentGeneratingClassName;
        if (parentGenerateContext instanceof StepForFieldGenerateContext stepForFieldParentGenerateContext) {
            parentGeneratingClassName = stepForFieldParentGenerateContext.getGeneratingClassName();
            if (parentGeneratingClassName == null)
                throw new RuntimeException(String.format("'%s' is inner class. Can not define ClassName of steps " +
                        "for this class because 'generatingClassName' is null for 'parentGenerateContext'" +
                        "in current context object.", inputClass));
        } else {
            parentGeneratingClassName = parentGenerateContext.getGeneratingClassName();
        }

        List<String> parentClassSimpleNames = new ArrayList<>(parentGeneratingClassName.simpleNames());
        String firstParentClassSimpleName = parentClassSimpleNames.get(0);
        parentClassSimpleNames.remove(0);
        parentClassSimpleNames.add(inputClass.getSimpleName() + "Steps");
        return ClassName.get(
                parentGeneratingClassName.packageName(),
                firstParentClassSimpleName,
                parentClassSimpleNames.toArray(String[]::new)
        );
    }

    public ClassName getGeneratingClassName(ClassName inputClassName, GenerateContext generateContext) {
        String classStepsPackageName = getFinalStepsPackageName(generateContext, inputClassName);
        return ClassName.get(classStepsPackageName, inputClassName.simpleName() + "Steps");
    }

    public void addErrorMsgField(GenerateContext generateContext) {
        FieldSpec errorMsgField = FieldSpec.builder(String.class, "errorMsg", Modifier.PRIVATE)
                .addAnnotation(Setter.class)
                .addAnnotation(ACCESSORS_ANNOTATION_SPEC)
                .build();

        generateContext.getGeneratingClassSpecBuilder().addField(errorMsgField);
    }

    public void addGetErrorMsgMethod(GenerateContext generateContext) {
        String defaultErrorMsgFieldName = "defaultErrorMsg";
        MethodSpec getErrorMsgMethodSpec = MethodSpec.methodBuilder(GET_ERROR_MSG_METHOD_NAME)
                .addModifiers(Modifier.PRIVATE)
                .returns(String.class)
                .addParameter(String.class, defaultErrorMsgFieldName)
                .addStatement("$T $N = $T.defaultString(this.$N, $N)", String.class, ERROR_MSG_FIELD_NAME, StringUtils.class, ERROR_MSG_FIELD_NAME, defaultErrorMsgFieldName)
                .addStatement("this.$N = null", ERROR_MSG_FIELD_NAME)
                .addStatement("return $N", ERROR_MSG_FIELD_NAME)
                .build();

        generateContext.getGeneratingClassSpecBuilder().addMethod(getErrorMsgMethodSpec);
    }

    public void addErrorMsgEntities(GenerateContext generateContext) {
        addErrorMsgField(generateContext);
        addGetErrorMsgMethod(generateContext);
    }

    public void addExtractFieldMethodWithStepAnnotation(StepForFieldGenerateContext stepForFieldGenerateContext, StepForFieldGenerateContext newStepForFieldGenerateContext, Field field) {
        FieldSpec fieldSpec = FieldSpec.builder(field.getType(), field.getName()).build();
        AnnotationSpec annotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Проверки полей элемента $N:\"", fieldSpec.name)
                .build();
        ClassName newStepsClassName = getGeneratingClassName(field.getType(), newStepForFieldGenerateContext);
        MethodSpec extractMethodSpec = getExtractFieldMethodSpec(stepForFieldGenerateContext, fieldSpec, newStepsClassName)
                .addAnnotation(annotationSpec)
                .build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(extractMethodSpec);
    }

    public void addExtractFieldMethod(StepForFieldGenerateContext stepForFieldGenerateContext, FieldSpec fieldSpec) {
        ClassName newStepsClassName = getGeneratingClassName(((ClassName) fieldSpec.type), stepForFieldGenerateContext);
        MethodSpec extractMethodSpec = getExtractFieldMethodSpec(stepForFieldGenerateContext, fieldSpec, newStepsClassName).build();
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(extractMethodSpec);
    }

    private MethodSpec.Builder getExtractFieldMethodSpec(StepForFieldGenerateContext stepForFieldGenerateContext, FieldSpec fieldSpec, ClassName newStepsClassName) {
        String capitalizeFieldName = StringUtils.capitalize(fieldSpec.name);
        return MethodSpec.methodBuilder(String.format("extract%sSteps", capitalizeFieldName))
                .addModifiers(Modifier.PUBLIC)
                .returns(newStepsClassName)
                .addStatement("$N()", stepForFieldGenerateContext.getCheckThatCheckFieldIsExistMethodName())
                .addStatement("return new $T($N.get$N())", newStepsClassName, stepForFieldGenerateContext.getCheckClassFieldName(), capitalizeFieldName);
    }

    public String getFinalStepsPackageName(GenerateContext generateContext, ClassName inputClassName) {

        String resultStepsPackageName = generateContext.getResultStepsPackageName();
        if (inputClassName.packageName().startsWith(resultStepsPackageName)) {
            return inputClassName.packageName();
        }

        String preparedInputClassPackageName = inputClassName.packageName()
                .replace(generateContext.getExcludeFromPackageName(), "");

        if (preparedInputClassPackageName.startsWith("."))
            return resultStepsPackageName + preparedInputClassPackageName;

        String[] resultStepsPackageNameSplitByDot = resultStepsPackageName.split("\\.");
        String[] preparedInputClassPackageNameSplitByDot = preparedInputClassPackageName.split("\\.");
        List<String> finalPackages = new ArrayList<>();

        for (int i = 0; i < resultStepsPackageNameSplitByDot.length; i++) {
            if (resultStepsPackageNameSplitByDot[i].equals(preparedInputClassPackageNameSplitByDot[i])) {
                finalPackages.add(resultStepsPackageNameSplitByDot[i]);
            } else {
                finalPackages.addAll(List.of(Arrays.copyOfRange(resultStepsPackageNameSplitByDot, i, resultStepsPackageNameSplitByDot.length)));
                finalPackages.addAll(List.of(Arrays.copyOfRange(preparedInputClassPackageNameSplitByDot, i, preparedInputClassPackageNameSplitByDot.length)));
            }
        }
        return String.join(".", finalPackages);
    }
}
