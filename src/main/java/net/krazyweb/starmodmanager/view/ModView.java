package main.java.net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import main.java.net.krazyweb.starmodmanager.data.Localizer;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModView extends GridPane {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ModView.class);
	
	protected boolean moving = false;
	protected final Mod mod;
	private final ModList modList;
	
	private final Button installButton;
	private final Text statusText;
	private final Text displayName;
	private final Text modVersion;
	private final HBox buttons;
	
	protected ModView(final Mod mod, final ModList modList) {
		
		this.mod = mod;
		this.modList = modList;

		setGridLinesVisible(true);
		setHgap(25.0);
		
		displayName = new Text(mod.getDisplayName());
		statusText = new Text(Localizer.getMessage(mod.isInstalled() ? "modview.installed" : "modview.notinstalled"));
		modVersion = new Text(mod.getModVersion());
		
		installButton = new Button(Localizer.getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
		
		Button hideButton = new Button("HID");
		hideButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				modList.hideMod(mod);
			}
			
		});
		
		Button deleteButton = new Button("DEL");
		deleteButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				//TODO Ask first!
				modList.deleteMod(mod);
			}
			
		});
		
		
		buttons = new HBox();
		buttons.getChildren().add(deleteButton);
		buttons.getChildren().add(hideButton);
		buttons.getChildren().add(new Button("LNK"));
		
		add(displayName, 1, 1);
		add(statusText, 2, 1);
		add(modVersion, 2, 2);
		add(installButton, 3, 1);
		//
		
		final Button expandButton = new Button("↓");
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (expandButton.getText().equals("↑")) {
					expandButton.setText("↓");
					statusText.setVisible(true);
					modVersion.setVisible(true);
					getChildren().remove(buttons);
					add(statusText, 2, 1);
					add(modVersion, 2, 2);
					add(installButton, 3, 1);
				} else {
					expandButton.setText("↑");
					statusText.setVisible(false);
					modVersion.setVisible(false);
					getChildren().remove(installButton);
					getChildren().remove(statusText);
					getChildren().remove(modVersion);
					add(buttons, 2, 1);
				}
			}
		});
		
		add(expandButton, 4, 1);
		
		GridPane.setRowSpan(displayName, 2);
		GridPane.setRowSpan(installButton, 2);
		GridPane.setRowSpan(buttons, 2);
		GridPane.setColumnSpan(buttons, 2);
		GridPane.setRowSpan(expandButton, 2);
		
	}
	
	public void update() {
		
		installButton.setText(Localizer.getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
		
		installButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (mod.isInstalled()) {
					modList.uninstallMod(mod);
				} else {
					modList.installMod(mod);
				}
			}
		});
		
		statusText.setText(Localizer.getMessage(mod.isInstalled() ? "modview.installed" : "modview.notinstalled"));
		
	}
	
}