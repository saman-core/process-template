package io.samancore;

public class Field {
    String key;
    String dataType;
    boolean isPersistent;
    boolean isRequired;
    boolean isUnique;
    boolean isDecimal;
    int decimalLimit;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }

    public boolean isDecimal() {
        return isDecimal;
    }

    public void setIsDecimal(boolean decimal) {
        isDecimal = decimal;
    }

    public int getDecimalLimit() {
        return decimalLimit;
    }

    public void setDecimalLimit(int decimalLimit) {
        this.decimalLimit = decimalLimit;
    }
}
