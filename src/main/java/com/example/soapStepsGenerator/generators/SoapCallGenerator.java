package com.example.soapStepsGenerator.generators;

import com.example.soapStepsGenerator.external.soap.AbstractSoapStep;
import com.example.soapStepsGenerator.generators.context.GenerateContextsHolder;
import com.example.soapStepsGenerator.generators.context.SoapCallGenerateContext;
import com.example.soapStepsGenerator.generators.context.StepForFieldGenerateContext;
import com.squareup.javapoet.*;
import io.qameta.allure.Step;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.lang.model.element.Modifier;
import java.util.function.Supplier;

import static com.example.soapStepsGenerator.constants.GenerateCodeConstants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SoapCallGenerator {

    private final CommonEntitiesGenerator commonEntitiesGenerator = CommonEntitiesGenerator.getInstance();

    private final StepsForFieldsGenerator stepsForFieldsGenerator = StepsForFieldsGenerator.getInstance();

    private static class Holder {
        private static final SoapCallGenerator INSTANCE = new SoapCallGenerator();
    }

    public static SoapCallGenerator getInstance() {
        return SoapCallGenerator.Holder.INSTANCE;
    }

    public void generate(SoapCallGenerateContext soapCallGenerateContext) {
        Class<?> wsdlInterfaceClass = soapCallGenerateContext.getWsdlInterfaceClass();
        //fixme take out in SoapCallGenerateContext if this naming will approve.
        ClassName soapMethodCallStepsClassName = ClassName.get(wsdlInterfaceClass.getPackageName(), soapCallGenerateContext.getCapitalizeWsdlMethod());
        ClassName generatingClassName = commonEntitiesGenerator.getGeneratingClassName(soapMethodCallStepsClassName, soapCallGenerateContext);
        soapCallGenerateContext.setGeneratingClassName(generatingClassName)
                .setGeneratingClassSpecBuilder(TypeSpec.classBuilder(generatingClassName).addModifiers(Modifier.PUBLIC));

        ParameterizedTypeName extendClassParameterizedTypeName = ParameterizedTypeName.get(
                ClassName.get(soapCallGenerateContext.getExternalAbstractSoapStepPackageName(), AbstractSoapStep.class.getSimpleName()),
                ClassName.get(wsdlInterfaceClass),
                ClassName.get(soapCallGenerateContext.getWsdlMethodRequestClass()),
                ClassName.get(soapCallGenerateContext.getWsdlMethodResponseClass())
        );
        soapCallGenerateContext.getGeneratingClassSpecBuilder()
                .superclass(extendClassParameterizedTypeName);

        addPortInstanceField(soapCallGenerateContext);

        addConstructor(soapCallGenerateContext);
        addGetPortInstanceMethod(soapCallGenerateContext);
        addGetPortMethod(soapCallGenerateContext);
        addCallRequestMethod(soapCallGenerateContext);
        addGetRequestNameMethod(soapCallGenerateContext);
        addExtractResponseStepsMethod(soapCallGenerateContext);

        if (!GenerateContextsHolder.hasGenerateContextExistFor(soapCallGenerateContext.getWsdlMethodResponseClass())) {
            StepForFieldGenerateContext stepForResponseFieldsGenerateContext = new StepForFieldGenerateContext(
                    soapCallGenerateContext,
                    soapCallGenerateContext.getWsdlMethodResponseClass()
            );
            stepsForFieldsGenerator.fillClassSpecBuilder(stepForResponseFieldsGenerateContext);
        }

        for (Class<?> methodExceptionClass : soapCallGenerateContext.getWsdlMethod().getExceptionTypes()) {
            StepForFieldGenerateContext stepForExceptionGenerateContext = new StepForFieldGenerateContext(soapCallGenerateContext, methodExceptionClass);
            stepsForFieldsGenerator.fillClassSpecBuilder(stepForExceptionGenerateContext);
            soapCallGenerateContext.getGeneratingClassSpecBuilder()
                    .addMethod(getExtractExceptionStepsMethodSpec(stepForExceptionGenerateContext));
        }
    }

    private void addPortInstanceField(SoapCallGenerateContext soapCallGenerateContext) {
        FieldSpec portInstanceFieldSpec = FieldSpec.builder(
                        soapCallGenerateContext.getWsdlInterfaceClass(),
                        PORT_INSTANCE_FIELD_NAME,
                        Modifier.PRIVATE,
                        Modifier.STATIC)
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addField(portInstanceFieldSpec);
    }

    private void addConstructor(SoapCallGenerateContext soapCallGenerateContext) {
        MethodSpec constructorMethodSpec = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(
                        "$N($T.class, \"\"/*$S*/)",
                        SET_WSDL_LOCATION_METHOD_NAME,
                        soapCallGenerateContext.getWsdlServiceClass(),
                        GET_PROPERTY_TEMPLATE)
                .addStatement(GET_PORT_METHOD_NAME + "()")
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(constructorMethodSpec);
    }

    private void addGetPortInstanceMethod(SoapCallGenerateContext soapCallGenerateContext) {
        Class<?> wsdlInterfaceClass = soapCallGenerateContext.getWsdlInterfaceClass();
        ParameterizedTypeName createPortParameterTypeName = ParameterizedTypeName.get(Supplier.class, wsdlInterfaceClass);
        ParameterSpec createPortParameterSpec = ParameterSpec.builder(createPortParameterTypeName, "createPort").build();
        MethodSpec addGetPortInstanceMethodSpec = MethodSpec.methodBuilder(GET_PORT_INSTANCE_METHOD_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(createPortParameterSpec)
                .returns(wsdlInterfaceClass)
                .beginControlFlow("if ($N != null)", PORT_INSTANCE_FIELD_NAME)
                .addStatement("return $N", PORT_INSTANCE_FIELD_NAME)
                .endControlFlow()
                .beginControlFlow("synchronized ($T.class)", soapCallGenerateContext.getGeneratingClassName())
                .beginControlFlow("if ($N != null)", PORT_INSTANCE_FIELD_NAME)
                .addStatement("return $N", PORT_INSTANCE_FIELD_NAME)
                .endControlFlow()
                .addStatement("$N = $N.get()", PORT_INSTANCE_FIELD_NAME, createPortParameterSpec.name)
                .addStatement("return $N", PORT_INSTANCE_FIELD_NAME)
                .endControlFlow()
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(addGetPortInstanceMethodSpec);
    }

    private void addGetPortMethod(SoapCallGenerateContext soapCallGenerateContext) {
        MethodSpec addGetPortMethodSpec = MethodSpec.methodBuilder(GET_PORT_METHOD_NAME)
                .addModifiers(Modifier.PROTECTED)
                .returns(soapCallGenerateContext.getWsdlInterfaceClass())
                .addAnnotation(Override.class)
                .addCode("""
                                return $N(
                                        () -> $N($T::new)
                                                .$N($T.class)
                                );
                                """,
                        GET_PORT_INSTANCE_METHOD_NAME,
                        GET_SERVICE_METHOD_NAME,
                        soapCallGenerateContext.getWsdlServiceClass(),
                        GET_PORT_METHOD_NAME,
                        soapCallGenerateContext.getWsdlInterfaceClass()
                )
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(addGetPortMethodSpec);
    }

    private void addCallRequestMethod(SoapCallGenerateContext soapCallGenerateContext) {
        AnnotationSpec stepAnnotationSpec = AnnotationSpec.builder(Step.class)
                .addMember("value", "\"Выполнение SOAP-запроса $N\"", soapCallGenerateContext.getRequestName())
                .build();
        String requestBodyParameterName = "requestBody";
        MethodSpec addGetPortMethodSpec = MethodSpec.methodBuilder(CALL_REQUEST_METHOD_NAME)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ParameterSpec.builder(soapCallGenerateContext.getWsdlMethodRequestClass(), requestBodyParameterName).build())
                .addException(Exception.class)
                .returns(soapCallGenerateContext.getWsdlMethodResponseClass())
                .addAnnotation(Override.class)
                .addAnnotation(stepAnnotationSpec)
                .addStatement("return $N.$N($N)", PORT_FIELD_NAME, soapCallGenerateContext.getUncapitalizeWsdlMethod(), requestBodyParameterName)
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(addGetPortMethodSpec);
    }

    private void addGetRequestNameMethod(SoapCallGenerateContext soapCallGenerateContext) {
        MethodSpec addGetPortMethodSpec = MethodSpec.methodBuilder(GET_REQUEST_NAME_METHOD_NAME)
                .addModifiers(Modifier.PROTECTED)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement("return $S", soapCallGenerateContext.getRequestName())
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(addGetPortMethodSpec);
    }

    private void addExtractResponseStepsMethod(SoapCallGenerateContext soapCallGenerateContext) {
        ClassName newStepsClassName = commonEntitiesGenerator.getGeneratingClassName(
                soapCallGenerateContext.getWsdlMethodResponseClass(),
                soapCallGenerateContext
        );
        MethodSpec addExtractResponseStepsMethodSpec = MethodSpec.methodBuilder("extractResponseSteps")
                .addModifiers(Modifier.PUBLIC)
                .returns(newStepsClassName)
                .addStatement("return new $T($N())", newStepsClassName, GET_RESPONSE_METHOD_NAME)
                .build();

        soapCallGenerateContext.getGeneratingClassSpecBuilder().addMethod(addExtractResponseStepsMethodSpec);
    }

    private MethodSpec getExtractExceptionStepsMethodSpec(StepForFieldGenerateContext stepForExceptionGenerateContext) {
        Class<?> exceptionClass = stepForExceptionGenerateContext.getInputClass();
        ClassName newExceptionStepsClassName = commonEntitiesGenerator.getGeneratingClassName(
                exceptionClass,
                stepForExceptionGenerateContext
        );

        String extractExceptionStepsMethodName = "extract" + exceptionClass.getSimpleName() + "Steps";
        return MethodSpec.methodBuilder(extractExceptionStepsMethodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(newExceptionStepsClassName)
                .addStatement("$N($T.class)", CHECK_EXCEPTION_TYPE_METHOD_NAME, exceptionClass)
                .beginControlFlow("if ($N() instanceof $T)", GET_EXCEPTION_METHOD_NAME, exceptionClass)
                .addStatement("return new $T(($T) $N())", newExceptionStepsClassName, exceptionClass, GET_EXCEPTION_METHOD_NAME)
                .nextControlFlow("else")
                .addStatement("return new $T(null)", newExceptionStepsClassName)
                .endControlFlow()
                .build();
    }
}
