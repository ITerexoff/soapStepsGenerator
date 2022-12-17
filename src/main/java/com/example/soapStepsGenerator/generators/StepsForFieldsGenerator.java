package com.example.soapStepsGenerator.generators;

import com.example.soapStepsGenerator.generators.context.GenerateContextsHolder;
import com.example.soapStepsGenerator.generators.context.HandleListFieldContext;
import com.example.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.hamcrest.core.IsNull;

import javax.lang.model.element.Modifier;
import javax.xml.datatype.XMLGregorianCalendar;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static com.example.soapStepsGenerator.constants.GenerateCodeConstants.NOT_NULL_VALUE_MATCHER_NAME;
import static com.example.soapStepsGenerator.utils.ClassUtils.isJavaBaseClass;

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

        Arrays.stream(inputClass.getDeclaredFields())
                .forEach(field -> {
                    Type fieldGenericType = field.getGenericType();
                    if (fieldGenericType instanceof ParameterizedType && TypeUtils.isAssignable(fieldGenericType, List.class)) {

                        stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                        HandleListFieldContext handleListFieldContext = new HandleListFieldContext().setListField(field);
                        stepForFieldGenerateContext.setHandleListFieldContext(handleListFieldContext);
                        stepsForListFieldGenerator.addFilterItemsInListField(stepForFieldGenerateContext);
                        stepsForListFieldGenerator.addGetFilteredItemsStreamMethod(stepForFieldGenerateContext);
                        stepsForListFieldGenerator.addCheckFilteredListMethod(stepForFieldGenerateContext, field);
                        stepsForListFieldGenerator.addExtractItemFromFilteredListMethod(stepForFieldGenerateContext);

                        if (isJavaBaseClass(handleListFieldContext.getItemClass())) {
                            //customised for my current project
                            if (TypeUtils.isAssignable(handleListFieldContext.getItemType(), XMLGregorianCalendar.class)) {
                                stepsForListFieldGenerator.addCheckFilteredItemAsOffsetDateTimeMethod(stepForFieldGenerateContext, field);
                            } else {
                                stepsForListFieldGenerator.addCheckFilteredItemMethod(stepForFieldGenerateContext, field);
                            }
                        } else {
                            stepsForListFieldGenerator.addExtractItemStepsFromFilteredListMethod(stepForFieldGenerateContext);

                            if (!GenerateContextsHolder.hasGenerateContextExistFor(handleListFieldContext.getItemClass())) {
                                StepForFieldGenerateContext newStepForFieldGenerateContext = new StepForFieldGenerateContext(
                                        stepForFieldGenerateContext,
                                        handleListFieldContext.getItemClass()
                                );
                                this.fillClassSpecBuilder(newStepForFieldGenerateContext);
                            }
                        }

                    } else if (fieldGenericType instanceof ParameterizedType) {
                        //todo
                        stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                    } else if (isJavaBaseClass(field.getType()) || field.getType().isEnum()) {
                        //customised for my current project
                        if (TypeUtils.isAssignable(field.getType(), XMLGregorianCalendar.class)) {
                            dateTimeFieldGenerator.addCheckFieldWithOffsetDateTimeMatcherMethod(stepForFieldGenerateContext, field);
                            dateTimeFieldGenerator.addCheckFieldWithXMLGregorianCalendarMatcherMethod(stepForFieldGenerateContext, field);
                        } else {
                            stepsForFieldGenerator.addCheckFieldMethod(stepForFieldGenerateContext, field);
                        }
                    } else {
                        stepsForFieldGenerator.addCheckThatFieldIsExistMethod(stepForFieldGenerateContext, field);
                        stepsForFieldGenerator.addCheckThatFieldIsAbsentMethod(stepForFieldGenerateContext, field);

                        StepForFieldGenerateContext newStepForFieldGenerateContext;
                        if (GenerateContextsHolder.hasGenerateContextExistFor(field.getType())) {
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
