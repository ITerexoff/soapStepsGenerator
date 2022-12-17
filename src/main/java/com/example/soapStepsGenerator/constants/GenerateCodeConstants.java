package com.example.soapStepsGenerator.constants;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import lombok.experimental.Accessors;
import org.hamcrest.Matcher;

public class GenerateCodeConstants {

    public static final AnnotationSpec ACCESSORS_ANNOTATION_SPEC = AnnotationSpec.builder(Accessors.class)
            .addMember("chain", "true")
            .build();

    public static final String GET_ERROR_MSG_METHOD_NAME = "getErrorMsg";
    public static final String ERROR_MSG_FIELD_NAME = "errorMsg";
    public static final String MATCHER_PARAMETER_NAME = "matcher";
    public static final ClassName MATCHER_CLASS_NAME = ClassName.get(Matcher.class);
    public static final String ASSERT_THAT_METHOD_NAME = "assertThat";
    public static final String NOT_NULL_VALUE_MATCHER_NAME = "notNullValue";
    public static final String NULL_VALUE_MATCHER_NAME = "nullValue";

    public static final String INDENT = "    ";

    //SOAP
    public static final String PORT_INSTANCE_FIELD_NAME = "portInstance";
    public static final String GET_PORT_INSTANCE_METHOD_NAME = "getPortInstance";
    public static final String SET_WSDL_LOCATION_METHOD_NAME = "setWsdlLocation";
    public static final String GET_PORT_METHOD_NAME = "getPort";
    public static final String PORT_FIELD_NAME = "port";
    public static final String CALL_REQUEST_METHOD_NAME = "callRequest";
    public static final String GET_SERVICE_METHOD_NAME = "getService";
    public static final String GET_PROPERTY_TEMPLATE = "TestProperties.getProperty(\"soap.request.X.url\")";
    public static final String GET_REQUEST_NAME_METHOD_NAME = "getRequestName";
    public static final String GET_RESPONSE_METHOD_NAME = "super.getResponse";
    public static final String GET_EXCEPTION_METHOD_NAME = "super.getException";
    public static final String CHECK_EXCEPTION_TYPE_METHOD_NAME = "super.checkExceptionType";

}
