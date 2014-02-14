package net.krazyweb.jfx.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumericTextField extends TextField {
	
	private static final Logger log = LogManager.getLogger(NumericTextField.class);

	private IntegerProperty minValue;
	private IntegerProperty maxValue;
	private IntegerProperty maxLength;
	private IntegerProperty defaultValue;
	
	private boolean ignoreNextUpdate = false;
	
	public NumericTextField() {
		
		super();

		textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
				
				if (ignoreNextUpdate) {
					ignoreNextUpdate = false;
					return;
				}
				
				if (!newValue.isEmpty()) {
					
					if (maxLength != null && newValue.length() > maxLength.get()) {
						ignoreNextUpdate = true;
						textProperty().set(oldValue);
						log.debug("'" + newValue + "' exceeds maximum length (" + maxLength.get() + "). Ignoring Input.");
						return;
					}
					
					try {
						
						int value = Integer.parseInt(newValue);
						
						if (maxValue != null && value > maxValue.get()) {
							ignoreNextUpdate = true;
							textProperty().set("" + maxValue.get());
							log.debug("Value '" + value + "' is over max value (" + maxValue.get() + ") -- Setting to max value.");
							return;
						} else if (minValue != null && value < minValue.get()) {
							ignoreNextUpdate = true;
							textProperty().set("" + minValue.get());
							log.debug("Value '" + value + "' is under min value (" + minValue.get() + ") -- Setting to min value.");
							return;
						}
						
					} catch (final NumberFormatException e) {
						ignoreNextUpdate = true;
						textProperty().set(oldValue);
						log.debug("Invalid Input Detected: " + newValue);
					}
					
				}
				
			}
			
		});
		
		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					if (textProperty().getValue().isEmpty() && defaultValue != null) {
						textProperty().setValue("" + defaultValue.get());
					}
				}
			}
		});
		
	}
	
	public void setMinValue(final int value) {
		minValue = new SimpleIntegerProperty(value);
	}
	
	public void setMaxValue(final int value) {
		maxValue = new SimpleIntegerProperty(value);
		maxLength = null;
	}
	
	public void setMaxLength(final int value) {
		maxLength = new SimpleIntegerProperty(value);
		maxValue = null;
	}
	
	public void setDefaultValue(final int value) {
		defaultValue = new SimpleIntegerProperty(value);
	}
	
}