package main.java.net.krazyweb.starmodmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import main.java.net.krazyweb.starmodmanager.data.Mod;
import main.java.net.krazyweb.starmodmanager.data.ModList;

import org.apache.log4j.Logger;

public class ModView extends GridPane {
	
	private static final Logger log = Logger.getLogger(ModView.class);
	
	protected Mod mod;
	protected boolean moving = false;
	private ModList modList;
	
	protected ModView(final Mod mod, final ModList modList) {
		
		this.mod = mod;
		this.modList = modList;

		setGridLinesVisible(true);
		
		setHgap(25.0);
		Text displayName = new Text(mod.getDisplayName());
		add(displayName, 1, 1);
		add(new Text(mod.isInstalled() ? "Installed" : "Not Installed"), 2, 1);
		add(new Text(mod.getModVersion()), 2, 2);
		
		Button installButton = new Button(mod.isInstalled() ? "Uninstall" : "Install");
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
		
		add(installButton, 3, 1);
		
		final Button expandButton = new Button("↓");
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				expandButton.setText("↑");
			}
		});
		
		add(expandButton, 4, 1);
		
		GridPane.setRowSpan(displayName, 2);
		GridPane.setRowSpan(installButton, 2);
		GridPane.setRowSpan(expandButton, 2);
		
	}
	
	public void update() {
		
		//getChildren().clear();
		
	}
	
}