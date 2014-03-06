package net.krazyweb.jfx.controls;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProgressIndicatorBar extends StackPane {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ProgressIndicatorBar.class);

	private static final double DEFAULT_PADDING = 5.0;
	
	private ProgressBar bar;
	private Text text;
	
	private DoubleBinding workDone;
	private ReadOnlyDoubleProperty workDoneReadOnly;
	private double totalWork;

	public ProgressIndicatorBar() {
		
		bar = new ProgressBar();
		text = new Text();
		text.setId("progress-text");

		bar.setMaxWidth(Double.MAX_VALUE);
		getChildren().setAll(bar, text);
		setAlignment(Pos.CENTER_LEFT);
		
	}

	private void updateProgress() {
		
		if ((workDone == null && workDoneReadOnly == null) || totalWork == 0) {
			text.setText("");
			bar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		} else if (workDone != null) {
			text.setText(Math.round(workDone.get() * 100.0) + "%");
			bar.setProgress(workDone.get());
		} else {
			text.setText(Math.round(workDoneReadOnly.get() * 100.0) + "%");
			bar.setProgress(workDoneReadOnly.get());
		}
		
		bar.setMinHeight(text.getBoundsInLocal().getHeight() + DEFAULT_PADDING * 2);
		bar.setMinWidth(text.getBoundsInLocal().getWidth() + DEFAULT_PADDING * 2);

	}
	
	public void bind(final DoubleBinding doubleBinding, final double totalWork) {
		
		this.workDone = doubleBinding;
		this.totalWork = totalWork;

		updateProgress();
		workDone.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observableValue, final Number oldValue, final Number newValue) {
				updateProgress();
			}
		});
		
	}

	public void bind(final ReadOnlyDoubleProperty progressProperty, final double totalWork) {
		
		this.workDoneReadOnly = progressProperty;
		this.totalWork = totalWork;

		updateProgress();
		workDoneReadOnly.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observableValue, final Number oldValue, final Number newValue) {
				updateProgress();
			}
		});
		
	}
	
	public void setSize(final double width, final double height) {
		bar.setPrefWidth(width);
		bar.setPrefHeight(height);
		text.setTranslateX(21.0);
		setMaxWidth(width);
		setMaxHeight(height);
	}

}