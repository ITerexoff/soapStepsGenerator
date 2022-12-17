package com.iterexoff.soapStepsGenerator.generators.context;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class StepForFieldGenerateContext extends GenerateContext {

    //Не получается заюзать Field. Разумно имя филда прописывать в @Step. Но в одном случае имя может быть одно, а в
    // другом - иное. При этом все это может описываться одним типом. И не хочется из-за этого плодить классы степов.
    //private Field inputField;
    private Class<?> inputClass;
    private String checkClassFieldName;
    private HandleListFieldContext handleListFieldContext;
    private String checkThatCheckFieldIsExistMethodName;

    public StepForFieldGenerateContext(GenerateContext generateContext, Class<?> inputClass) {
        this.inputClass = inputClass;
        this.checkClassFieldName = "check" + inputClass.getSimpleName();
        this.resultStepsPackageName = generateContext.getResultStepsPackageName();
        this.excludeFromPackageName = generateContext.getExcludeFromPackageName();
        this.externalDateUtilPackageName = generateContext.getExternalDateUtilPackageName();
        generateContext.getChildGenerateContexts().add(this);
        super.parentGenerateContext = generateContext;
        GenerateContextsHolder.putNewGenerateContext(inputClass, this);
        this.checkThatCheckFieldIsExistMethodName = "checkThat" + StringUtils.capitalize(this.checkClassFieldName) + "IsExist";
    }

    public boolean isInputClassInner() {
        return ClassUtils.isInnerClass(inputClass);
    }

}
