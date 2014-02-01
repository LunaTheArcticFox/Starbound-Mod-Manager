package main.java.net.krazyweb.starmodmanager.data;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface Progressable {

	public ReadOnlyDoubleProperty getProgressProperty();
	public ReadOnlyStringProperty getMessageProperty();
	
	public double getProgress();
	public boolean isDone();
	
	public void processTask();
	
}