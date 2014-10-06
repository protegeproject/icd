package edu.stanford.bmir.whofic;

import edu.stanford.smi.protege.model.ValueType;

public class PrecoordinationDefinitionComponent {
	private String property;
	private String value;
	private ValueType valueType;
	private boolean isDefinitional;

	public PrecoordinationDefinitionComponent(String property,
			String value, ValueType valueType, boolean isDefinitional) {
		this.property = property;
		this.value = value;
		this.valueType = valueType;
		this.isDefinitional = isDefinitional;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public boolean isDefinitional() {
		return isDefinitional;
	}

	public ValueType getValueType() {
		return valueType;
	}


	@Override
	public String toString() {
		return "PrecoordinationClassExpressionData(" +
				property + ", " +
				value + ", " +
				valueType + ", " +
				isDefinitional + ")";
	}

}