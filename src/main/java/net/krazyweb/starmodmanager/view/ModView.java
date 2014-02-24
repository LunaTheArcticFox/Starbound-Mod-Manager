package net.krazyweb.starmodmanager.view;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Mod;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ModView.class);
	
	private GridPane root;
	
	private Button installButton;
	private Button expandButton;
	private Text statusText;
	private Text displayName;
	private Text modVersion;
	
	private HBox buttons;
	private Button deleteButton;
	private Button hideButton;
	private Button linkButton;

	private Mod mod;
	private ModViewController controller;
	protected boolean moving;
	protected boolean expanded;
	
	private LocalizerModelInterface localizer;
	
	protected ModView(final Mod mod, final ModList modList) {
		this.mod = mod;
		this.mod.addObserver(this);
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new ModViewController(this, modList, localizer);
	}
	
	protected void build() {
		
		root = new GridPane();
		root.setGridLinesVisible(true);
		root.setHgap(25.0);
		
		displayName = new Text();
		statusText = new Text();
		modVersion = new Text();
		installButton = new Button();
		expandButton = new Button();
		
		buttons = new HBox();
		deleteButton = new Button();
		hideButton = new Button();
		linkButton = new Button();
		
		buttons.getChildren().addAll(
			deleteButton,
			hideButton,
			linkButton
		);
		
		root.add(displayName, 1, 1);
		root.add(statusText, 2, 1);
		root.add(modVersion, 2, 2);
		root.add(installButton, 3, 1);
		root.add(expandButton, 4, 1);

		GridPane.setRowSpan(displayName, 2);
		GridPane.setRowSpan(installButton, 2);
		GridPane.setRowSpan(buttons, 2);
		GridPane.setColumnSpan(buttons, 2);
		GridPane.setRowSpan(expandButton, 2);
		GridPane.setHgrow(displayName, Priority.ALWAYS);
		
		createListeners();
		updateStrings();
		
	}
	
	protected void createListeners() {
		
		installButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				if (mod.isInstalled()) {
					controller.uninstallButtonClicked();
				} else {
					controller.installButtonClicked();
				}
			}
		});
		
		expandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.expandButtonClicked();
			}
		});

		deleteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.deleteButtonClicked();
			}
		});
		
		hideButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.hideButtonClicked();
			}
		});
		
		linkButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.linkButtonClicked();
			}
		});
		
	}
	
	protected Mod getMod() {
		return mod;
	}

	private void updateStrings() {

		displayName.setText(mod.getDisplayName());
		statusText.setText(localizer.getMessage(mod.isInstalled() ? "modview.installed" : "modview.notinstalled"));
		modVersion.setText(mod.getModVersion() + " - " + mod.getGameVersion());
		installButton.setText(localizer.getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
		
		//TODO Replace with images
		expandButton.setText("^");
		deleteButton.setText("DEL");
		hideButton.setText("HID");
		linkButton.setText("LNK");
		
	}
	
	protected GridPane getContent() {
		return root;
	}
	
	protected void toggleExpand() {
		
		if (!expanded) {
			
			root.getChildren().remove(installButton);
			root.getChildren().remove(modVersion);
			root.getChildren().remove(statusText);
			
			root.add(buttons, 2, 1);
			
			expanded = true;
			
		} else {
			
			root.getChildren().remove(buttons);

			root.add(statusText, 2, 1);
			root.add(modVersion, 2, 2);
			root.add(installButton, 3, 1);
			
			expanded = false;
			
		}
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof Localizer && message.equals("localechanged")) {
			updateStrings();
		} else if (observable == mod) {
			
			String msg = (String) message;
			
			switch (msg) {
				case "installstatuschanged":
					updateStrings();
					//installButton.setText(Localizer.getInstance().getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
					break;
			}
			
		}
		
	}

	
}