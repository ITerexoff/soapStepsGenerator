package com.iterexoff.soapStepsGenerator.generators;

import com.iterexoff.soapStepsGenerator.generators.context.GenerateContextsHolder;
import com.iterexoff.soapStepsGenerator.generators.context.HandleListFieldContext;
import com.iterexoff.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.hamcrest.core.IsNull;

import javax.lang.model.element.Modifier;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static com.iterexoff.soapStepsGenerator.constants.GenerateCodeConstants.NOT_NULL_VALUE_MATCHER_NAME;
import static com.iterexoff.soapStepsGenerator.utils.ClassUtils.getDeclaredFieldsWithSuperclasses;
import static com.iterexoff.soapStepsGenerator.utils.ClassUtils.isJavaBaseClass;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StepsForFieldsGenerator {

    private final CommonEntitiesGenerator commonEntitiesGenerator = CommonEntitiesGenerator.getInstance();

    private final StepsForListFieldGenerator stepsForListFieldGenerator = StepsForListFieldGenerator.getInstance();

    private final DateTimeFieldGenerator dateTimeFieldGenerator = DateTimeFieldGenerator.getInstance();

    private final StepsForFieldGenerator stepsForFieldGenerator = StepsForFieldGenerator.getInstance();

    private static class Holder {
        private static final StepsForFieldsGenerator INSTANCE = new StepsForFieldsGenerator();
    }

    public static StepsForFieldsGenerator getInstance() {
        return StepsForFieldsGenerator.Holder.INSTANCE;
    }

    public void fillClassSpecBuilder(StepForFieldGenerateContext stepForFieldGenerateContext) {
        Class<?> inputClass = stepForFieldGenerateContext.getInputClass();
        log.debug("Generating steps for fields of class '{}'.", inputClass);

        ClassName generatingClassName = commonEntitiesGenerator.getGeneratingClassName(inputClass, stepForFieldGenerateContext);
        stepForFieldGenerateContext.setGeneratingClassName(generatingClassName)
                .setGeneratingClassSpecBuilder(TypeSpec.classBuilder(generatingClassName).addModifiers(Modifier.PUBLIC));

        this.addCheckClassField(stepForFieldGenerateContext);
//        this.addConstructor(stepForFieldGenerateContext);
        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addAnnotation(RequiredArgsConstructor.class);
        this.generateStepsForFields(stepForFieldGenerateContext);
        stepsForFieldGenerator.addCheckThatCheckFieldIsExistMethod(stepForFieldGenerateContext);
        commonEntitiesGenerator.addErrorMsgEntities(stepForFieldGenerateContext);
    }

    private void generateStepsForFields(StepForFieldGenerateContext stepForFieldGenerateContext) {

        Class<?> inputClass = stepForFieldGenerateContext.getInputClass();

        getDeclaredFieldsWithSuperclasses(inputClass)
                .forEach(field -> {
                    log.debug("Generating steps for field '{}'.", field);
                    Type fieldGenericType = field.getGenericType();
                    if (fieldGenericType instanceof ParameterizedType && TypeUtils.isAssignable(fieldGenericType, List.class)) {
                        log.debug("Type of field '{}' is List. Generating 'List type' steps.", field);

                        stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                        HandleListFieldContext handleListFieldContext = new HandleListFieldContext().setListField(field);
                        stepForFieldGenerateContext.setHandleListFieldContext(handleListFieldContext);
                        stepsForListFieldGenerator.addFilterItemsInListField(stepForFieldGenerateContext);
                        stepsForListFieldGenerator.addGetFilteredItemsStreamMethod(stepForFieldGenerateContext);
                        stepsForListFieldGenerator.addCheckFilteredListMethod(stepForFieldGenerateContext, field);
                        stepsForListFieldGenerator.addExtractItemFromFilteredListMethod(stepForFieldGenerateContext);

                        Class<?> itemClass = handleListFieldContext.getItemClass();
                        if (isJavaBaseClass(itemClass) || itemClass.isEnum()) {
                            log.debug("Type of item of List '{}' is '{}'. Generating check filtered item method.", field, itemClass);
                            //customised for my current project
                            if (TypeUtils.isAssignable(handleListFieldContext.getItemType(), XMLGregorianCalendar.class)) {
                                stepsForListFieldGenerator.addCheckFilteredItemAsOffsetDateTimeMethod(stepForFieldGenerateContext, field);
                            } else {
                                stepsForListFieldGenerator.addCheckFilteredItemMethod(stepForFieldGenerateContext, field);
                            }
                        } else {
                            stepsForListFieldGenerator.addExtractItemStepsFromFilteredListMethod(stepForFieldGenerateContext);

                            if (!GenerateContextsHolder.hasGenerateContextExistFor(itemClass)) {
                                StepForFieldGenerateContext newStepForFieldGenerateContext = new StepForFieldGenerateContext(
                                        stepForFieldGenerateContext,
                                        itemClass
                                );
                                this.fillClassSpecBuilder(newStepForFieldGenerateContext);
                            } else {
                                log.debug("There has been generated steps for type ('{}') of item of List '{}' yet (by generating for other classes).", field, itemClass);
                            }
                        }

                    } else if (fieldGenericType instanceof ParameterizedType) {
                        log.debug("Type of field '{}' is parametrized type ('{}'). Generating check field method.", field, fieldGenericType);
                        //todo
                        stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                    } else if (isJavaBaseClass(field.getType()) || field.getType().isEnum()) {
                        log.debug("Type of field '{}' is java base or enum ('{}'). Generating check field method.", field, fieldGenericType);
                        //customised for my current project
                        if (TypeUtils.isAssignable(field.getType(), XMLGregorianCalendar.class)) {
                            dateTimeFieldGenerator.addCheckFieldWithOffsetDateTimeMatcherMethod(stepForFieldGenerateContext, field);
                            dateTimeFieldGenerator.addCheckFieldWithXMLGregorianCalendarMatcherMethod(stepForFieldGenerateContext, field);
                        } else {
                            stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                        }
                    } else {
                        log.debug("Type of field '{}' is '{}'. Generating check field exist/absent methods and extract field method.", field, fieldGenericType);

                        stepsForFieldGenerator.addCheckThatFieldIsExistMethod(stepForFieldGenerateContext, field);
                        stepsForFieldGenerator.addCheckThatFieldIsAbsentMethod(stepForFieldGenerateContext, field);

                        StepForFieldGenerateContext newStepForFieldGenerateContext;
                        if (GenerateContextsHolder.hasGenerateContextExistFor(field.getType())) {
                            log.debug("There has been generated steps for type ('{}') of field '{}' yet (by generating for other classes).", field, fieldGenericType);
                            newStepForFieldGenerateContext = (StepForFieldGenerateContext) GenerateContextsHolder.getExistGenerateContextFor(field.getType());
                        } else {
                            newStepForFieldGenerateContext = new StepForFieldGenerateContext(stepForFieldGenerateContext, field.getType());
                            if (!ClassUtils.isInnerClass(field.getType())) {
                                this.fillClassSpecBuilder(newStepForFieldGenerateContext);
                            } else {
                                this.fillClassSpecBuilder(newStepForFieldGenerateContext);
                                stepForFieldGenerateContext.getGeneratingClassSpecBuilder()
                                        .addType(newStepForFieldGenerateContext.getGeneratingClassSpecBuilder().build());
                            }
                        }

                        commonEntitiesGenerator.addExtractFieldMethodWithStepAnnotation(stepForFieldGenerateContext, newStepForFieldGenerateContext, field);
                    }
                });
    }

    private void addCheckClassField(StepForFieldGenerateContext stepForFieldGenerateContext) {
        Class<?> inputClass = stepForFieldGenerateContext.getInputClass();
        FieldSpec checkClassField = FieldSpec.builder(inputClass, stepForFieldGenerateContext.getCheckClassFieldName(), Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(Getter.class)
                .build();

        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addField(checkClassField);
    }

    private void addConstructor(StepForFieldGenerateContext stepForFieldGenerateContext) {
        String constructorParameterName = stepForFieldGenerateContext.getCheckClassFieldName();
//        String uncapitalizeInputClassName = StringUtils.uncapitalize(stepForFieldGenerateContext.getInputClass().getSimpleName());
        MethodSpec.Builder constructorMethodSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(stepForFieldGenerateContext.getInputClass(), constructorParameterName);
//                .addStatement("$T $N = \"Отсутствует поле $N\"", String.class, ERROR_MSG_FIELD_NAME, uncapitalizeInputClassName);
//        commonEntitiesGenerator.addAssertThatStatement(constructorMethodSpecBuilder, constructorParameterName, NOT_NULL_VALUE_MATCHER_NAME + "()");
        MethodSpec constructorMethodSpec = constructorMethodSpecBuilder
                .addStatement("this.$N = $N", constructorParameterName, constructorParameterName)
                .build();

        stepForFieldGenerateContext.getGeneratingClassSpecBuilder().addMethod(constructorMethodSpec);
        stepForFieldGenerateContext.addStaticImport(IsNull.class, NOT_NULL_VALUE_MATCHER_NAME);
    }
}
