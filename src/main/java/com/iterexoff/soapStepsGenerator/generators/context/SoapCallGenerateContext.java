package com.iterexoff.soapStepsGenerator.generators.context;

import com.iterexoff.soapStepsGenerator.external.soap.AbstractSoapStep;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

@Getter
@Setter
@Accessors(chain = true)
public class SoapCallGenerateContext extends GenerateContext {

    private Class<?> wsdlInterfaceClass;
    private Class<?> wsdlServiceClass;
    private Method wsdlMethod;
    private String capitalizeWsdlMethod;
    private String uncapitalizeWsdlMethod;
    private Class<?> wsdlMethodRequestClass;
    private Class<?> wsdlMethodResponseClass;
    private String uncapitalizeMethodResponseClass;
    private String initialWsdlInterfaceName;
    private String requestName;
    private String resultWsdlInterfacesPackageName;
    private String externalAbstractSoapStepPackageName;

    public SoapCallGenerateContext setWsdlMethod(Method wsdlMethod) {
        this.wsdlMethod = wsdlMethod;
        this.uncapitalizeWsdlMethod = StringUtils.uncapitalize(wsdlMethod.getName());
        this.capitalizeWsdlMethod = StringUtils.capitalize(wsdlMethod.getName());
        this.wsdlMethodRequestClass = wsdlMethod.getParameters()[0].getType();//fixme [0]
        this.wsdlMethodResponseClass = wsdlMethod.getReturnType();
        this.uncapitalizeMethodResponseClass = StringUtils.uncapitalize(wsdlMethodResponseClass.getSimpleName());
        return this;
    }

    public SoapCallGenerateContext setInitialWsdlInterfaceName(String initialWsdlInterfaceName) {
        this.initialWsdlInterfaceName = initialWsdlInterfaceName;
        //fixme - remove from initialWsdlInterfaceName - uncapitalizeWsdlMethod - need for current work project
        this.requestName = String.format("/%s: %s", initialWsdlInterfaceName, this.uncapitalizeWsdlMethod);
        return this;
    }

    public String getExternalAbstractSoapStepPackageName() {
        return externalAbstractSoapStepPackageName == null ? AbstractSoapStep.class.getPackageName() : externalAbstractSoapStepPackageName;
    }

    public boolean isInputClassInner() {
        return ClassUtils.isInnerClass(wsdlInterfaceClass);
    }

}
