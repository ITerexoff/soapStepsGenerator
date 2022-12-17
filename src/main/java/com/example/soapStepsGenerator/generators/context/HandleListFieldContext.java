package com.example.soapStepsGenerator.generators.context;

import com.example.soapStepsGenerator.utils.TypeUtils;
import com.squareup.javapoet.ParameterizedTypeName;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@Accessors(chain = true)
public class HandleListFieldContext {

    private Field listField;
    private Type itemType;
    private Class<?> itemClass;
    private ParameterizedTypeName typeNameOfFieldOfFilteringItemsInList;
    private String capitalizeListFieldName;
    private String uncapitalizeItemClassName;
    private String filterItemsInListFieldName;
    private String getFilteredItemsStreamMethodName;
    private String extractItemFromFilteredListMethodName;
    private ParameterizedTypeName typeNameOfOptionalItem;

    public HandleListFieldContext setListField(Field listField) {
        this.listField = listField;
        this.itemType = TypeUtils.getFirstTypeArgument((ParameterizedType) listField.getGenericType()).orElse(null);
        if (this.itemType != null) {
            this.itemClass = (Class<?>) itemType;
            this.typeNameOfFieldOfFilteringItemsInList = ParameterizedTypeName.get(Predicate.class, itemType);
            this.capitalizeListFieldName = StringUtils.capitalize(listField.getName());
            this.uncapitalizeItemClassName = StringUtils.uncapitalize(getItemClassName());
            this.filterItemsInListFieldName = listField.getName() + "Filter";
            this.getFilteredItemsStreamMethodName = "getFiltered" + capitalizeListFieldName + "Stream";
            this.extractItemFromFilteredListMethodName = String.format("extractItemFromFiltered%s", capitalizeListFieldName);
            typeNameOfOptionalItem = ParameterizedTypeName.get(Optional.class, itemType);
        }
        return this;
    }

    public String getItemClassName() {
        return itemClass.getSimpleName();
    }
}
