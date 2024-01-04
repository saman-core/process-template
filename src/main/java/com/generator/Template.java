package com.generator;

import java.util.List;

public class Template {

    String packageName;
    String name;
    List<Field> fields;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "Template{" +
                "packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", fields=" + fields +
                '}';
    }
}
