package net.krazyweb.starmodmanager.view;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.Localizer;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Mod;
import net.krazyweb.starmodmanager.data.ModList;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModView implements Observer {
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ModView.class);
	
	private GridPane collapsedRoot;
	
	private Button collapsedInstallButton;
	private Button collapsedExpandButton;
	private Text collapsedStatusText;
	private Text collapsedDisplayName;
	private Text collapsedModVersion;
	
	private HBox collapsedButtons;
	private Button collapsedDeleteButton;
	private Button collapsedHideButton;
	private Button collapsedLinkButton;
	
	private GridPane expandedRoot;
	private BorderPane expandedHeader;
	
	private Button expandedInstallButton;
	private Button expandedExpandButton;
	private Text expandedStatusText;
	private Text expandedDisplayName;
	private Text expandedModVersion;
	private Text expandedAuthor;
	private Text expandedDescription;

	private VBox expandedButtons;
	private Button expandedDeleteButton;
	private Button expandedHideButton;
	private Button expandedLinkButton;
	
	private RowConstraints descriptionConstraints;
	private RowConstraints expandButtonConstraints;

	private Mod mod;
	private ModViewController controller;
	protected boolean moving;
	protected boolean showingMoreInfo;
	protected boolean expanded;
	
	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	protected ModView(final Mod mod, final ModList modList) {
		this.mod = mod;
		this.mod.addObserver(this);
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		settings = new SettingsFactory().getInstance();
		controller = new ModViewController(this, modList, localizer);
	}
	
	protected void build(final boolean expanded) {

		this.expanded = expanded;
		
		buildExpanded();
		buildUnexpanded();
		
		createListeners();
		updateStrings();
		updateImages();
		updateColors();
		
	}
	
	private void buildExpanded() {
		
		expandedRoot = new GridPane();
		//expandedRoot.setGridLinesVisible(true);
		expandedRoot.setId("modview-expanded-container");
		
		if (mod.hasImage()) {
			expandedRoot.setStyle(
				"-fx-background-image: url(\"" + mod.getImageLocation() + "\");" +
				"-fx-background-position: 29px 0px;"
			);
		} else {
			expandedRoot.setStyle(
				"-fx-background-color: " + CSSHelper.getColorHex("modview-expanded-not-installed-background-color", settings.getPropertyString("theme")) + ";" +
				"-fx-background-position: 29px 0px;"
			);
		}
		
		expandedHeader = new BorderPane();
		expandedHeader.setPrefHeight(29);
		expandedHeader.setMinHeight(29);

		expandedInstallButton = new Button();
		expandedInstallButton.setId("modview-install-button");
		expandedInstallButton.setFocusTraversable(false);
		expandedInstallButton.setPrefHeight(30);
		//expandedInstallButton.setPrefWidth(73);
		//expandedInstallButton.setMinWidth(73);
		expandedInstallButton.setMinHeight(30);
		expandedInstallButton.setAlignment(Pos.CENTER);
		
		expandedExpandButton = new Button();
		expandedExpandButton.setId("modview-expand-button");
		
		expandedStatusText = new Text();
		expandedStatusText.setId("modview-expanded-header-text");
		
		expandedDisplayName = new Text();
		expandedDisplayName.setId("modview-expanded-title");
		
		expandedModVersion = new Text();
		expandedModVersion.setId("modview-expanded-header-text");
		
		expandedAuthor = new Text();
		expandedAuthor.setId("modview-expanded-sub-text");
		
		expandedDescription = new Text();
		expandedDescription.setId("modview-expanded-sub-text");
		expandedDescription.wrappingWidthProperty().bind(expandedRoot.widthProperty().subtract(210));
		
		expandedButtons = new VBox();
		expandedButtons.setAlignment(Pos.TOP_CENTER);
		expandedButtons.setSpacing(12);
		expandedDeleteButton = new Button();
		expandedDeleteButton.setId("modview-action-button");
		expandedHideButton = new Button();
		expandedHideButton.setId("modview-action-button");
		expandedLinkButton = new Button();
		expandedLinkButton.setId("modview-action-button");
		
		expandedDeleteButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("delete-icon-large.png"))));
		expandedHideButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("hide-icon-large.png"))));
		expandedLinkButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("link-icon-large.png"))));
		
		Color color = CSSHelper.getColor("modview-not-installed-button-color", settings.getPropertyString("theme"));
		FXHelper.setColor(expandedDeleteButton.getGraphic(), color);
		FXHelper.setColor(expandedHideButton.getGraphic(), color);
		FXHelper.setColor(expandedLinkButton.getGraphic(), color);
		
		expandedHeader.setPadding(new Insets(8, 10, 0, 10));
		expandedHeader.setLeft(expandedStatusText);
		expandedHeader.setRight(expandedModVersion);
		
		expandedButtons.getChildren().addAll(
			expandedDeleteButton,
			expandedHideButton,
			expandedLinkButton
		);
		
		expandedRoot.add(expandedHeader, 1, 1);
		expandedRoot.add(expandedDisplayName, 1, 2);
		expandedRoot.add(expandedAuthor, 1, 3);
		expandedRoot.add(expandedInstallButton, 2, 2);
		expandedRoot.add(expandedExpandButton, 3, 2);
		
		GridPane.setColumnSpan(expandedHeader, 3);
		GridPane.setRowSpan(expandedExpandButton, 2);
		GridPane.setRowSpan(expandedInstallButton, 2);
		GridPane.setRowSpan(expandedButtons, 3);
		
		GridPane.setHgrow(expandedDisplayName, Priority.ALWAYS);
		GridPane.setMargin(expandedDisplayName, new Insets(6, 0, 0, 15));
		GridPane.setMargin(expandedAuthor, new Insets(2, 0, 9, 15));
		GridPane.setMargin(expandedExpandButton, new Insets(1, 17, 0, 19));
		GridPane.setMargin(expandedButtons, new Insets(23, 0, 0, 0));
		GridPane.setMargin(expandedDescription, new Insets(10, 0, 0, 35));
		
		GridPane.setValignment(expandedDescription, VPos.TOP);
		
		RowConstraints fillerConstraints = new RowConstraints();
		expandedRoot.getRowConstraints().add(fillerConstraints);
		fillerConstraints = new RowConstraints();
		expandedRoot.getRowConstraints().add(fillerConstraints);
		fillerConstraints = new RowConstraints();
		expandedRoot.getRowConstraints().add(fillerConstraints);
		fillerConstraints = new RowConstraints();
		expandedRoot.getRowConstraints().add(fillerConstraints);
		
		descriptionConstraints = new RowConstraints();
		descriptionConstraints.setMinHeight(130);
		
		expandButtonConstraints = new RowConstraints();
		expandButtonConstraints.setMinHeight(45);
		
	}
	
	private void buildUnexpanded() {
		
		collapsedRoot = new GridPane();
		collapsedRoot.setId("modview-container");
		collapsedRoot.setMinHeight(44.0);
		collapsedRoot.setPadding(new Insets(7, 11, 6, 16));
		
		collapsedDisplayName = new Text();
		collapsedDisplayName.setId("modview-title");
		
		collapsedStatusText = new Text();
		collapsedStatusText.setId("modview-small-info");
		
		collapsedModVersion = new Text();
		collapsedModVersion.setId("modview-small-info");
		
		collapsedInstallButton = new Button();
		collapsedInstallButton.setId("modview-install-button");
		collapsedInstallButton.setFocusTraversable(false);
		collapsedInstallButton.setPrefHeight(30.0);
		//collapsedInstallButton.setPrefWidth(73.0);
		collapsedInstallButton.setAlignment(Pos.CENTER);
		
		collapsedExpandButton = new Button();
		collapsedExpandButton.setId("modview-expand-button");
		
		collapsedButtons = new HBox();
		collapsedDeleteButton = new Button();
		collapsedDeleteButton.setId("modview-action-button");
		collapsedHideButton = new Button();
		collapsedHideButton.setId("modview-action-button");
		collapsedLinkButton = new Button();
		collapsedLinkButton.setId("modview-action-button");
		
		collapsedDeleteButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("delete-icon.png"))));
		collapsedHideButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("hide-icon.png"))));
		collapsedLinkButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("link-icon.png"))));
		
		collapsedButtons.setAlignment(Pos.CENTER);
		collapsedButtons.setSpacing(15.0);
		collapsedButtons.setMinHeight(25);
		collapsedButtons.setPadding(new Insets(0, 0, 5, 0));
		collapsedButtons.getChildren().addAll(
			collapsedDeleteButton,
			collapsedHideButton,
			collapsedLinkButton
		);
		
		collapsedRoot.add(collapsedDisplayName, 1, 1);
		collapsedRoot.add(collapsedStatusText, 2, 1);
		collapsedRoot.add(collapsedModVersion, 2, 2);
		collapsedRoot.add(collapsedInstallButton, 3, 1);
		collapsedRoot.add(collapsedExpandButton, 4, 1);

		GridPane.setRowSpan(collapsedDisplayName, 2);
		GridPane.setRowSpan(collapsedInstallButton, 2);
		GridPane.setRowSpan(collapsedButtons, 2);
		GridPane.setColumnSpan(collapsedButtons, 2);
		GridPane.setRowSpan(collapsedExpandButton, 2);
		GridPane.setHgrow(collapsedDisplayName, Priority.ALWAYS);
		GridPane.setVgrow(collapsedDisplayName, Priority.ALWAYS);
		
		GridPane.setHalignment(collapsedStatusText, HPos.RIGHT);
		GridPane.setHalignment(collapsedModVersion, HPos.RIGHT);
		GridPane.setMargin(collapsedStatusText, new Insets(0, 10, 0, 0));
		GridPane.setMargin(collapsedModVersion, new Insets(1, 9, 0, 0));
		GridPane.setMargin(collapsedInstallButton, new Insets(0, 11, 0, 0));
		GridPane.setMargin(collapsedExpandButton, new Insets(2, 0, 0, 0));
		GridPane.setMargin(collapsedButtons, new Insets(5, 11, 0, 0));
		
	}
	
	protected void createListeners() {
		
		collapsedInstallButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				if (mod.isInstalled()) {
					controller.uninstallButtonClicked();
				} else {
					controller.installButtonClicked();
				}
			}
		});
		
		collapsedExpandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.moreInfoButtonClicked();
			}
		});

		collapsedDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.deleteButtonClicked();
			}
		});
		
		collapsedHideButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.hideButtonClicked();
			}
		});
		
		collapsedLinkButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.linkButtonClicked();
			}
		});
		
		expandedInstallButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				if (mod.isInstalled()) {
					controller.uninstallButtonClicked();
				} else {
					controller.installButtonClicked();
				}
			}
		});
		
		expandedExpandButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.moreInfoButtonClicked();
			}
		});

		expandedDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.deleteButtonClicked();
			}
		});
		
		expandedHideButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				controller.hideButtonClicked();
			}
		});
		
		expandedLinkButton.setOnAction(new EventHandler<ActionEvent>() {
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

		expandedInstallButton.setText(localizer.getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
		expandedStatusText.setText(localizer.getMessage(mod.isInstalled() ? "modview.installed" : "modview.notinstalled").toUpperCase());
		expandedDisplayName.setText(mod.getDisplayName());
		expandedModVersion.setText(mod.getModVersion().toUpperCase());
		expandedAuthor.setText(mod.getAuthor());
		expandedDescription.setText(mod.getDescription());
		
		collapsedInstallButton.setText(localizer.getMessage(mod.isInstalled() ? "modview.uninstall" : "modview.install"));
		collapsedStatusText.setText(localizer.getMessage(mod.isInstalled() ? "modview.installed" : "modview.notinstalled").toUpperCase());
		collapsedDisplayName.setText(mod.getDisplayName());
		collapsedModVersion.setText(mod.getModVersion().toUpperCase());

		Text test = new Text();
		test.setFont(Font.loadFont(ModView.class.getClassLoader().getResourceAsStream("Lato-Medium.ttf"), 12));
		test.setId("modview-install-button");

		VBox t = new VBox();
		t.getChildren().add(test);
		
		Stage s = new Stage();
		s.setOpacity(0);
		s.setScene(new Scene(t, 500, 500));
		s.show();

		test.setText(localizer.getMessage("modview.uninstall"));
		int width = (int) (test.getLayoutBounds().getWidth() + 24);
		expandedInstallButton.setPrefWidth(width);
		expandedInstallButton.setMinWidth(width);
		collapsedInstallButton.setPrefWidth(width);
		collapsedInstallButton.setMinWidth(width);
		
		s.close();
		
	}
	
	private void updateImages() {

		if (showingMoreInfo) {
			
			expandedExpandButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("collapse-arrow-large.png"))));
			collapsedExpandButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("collapse-arrow.png"))));
			
		} else {
			
			expandedExpandButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("expand-arrow-large.png"))));
			collapsedExpandButton.setGraphic(new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("expand-arrow.png"))));
			
		}

		Color color = CSSHelper.getColor("modview-expand-button-color", settings.getPropertyString("theme"));
		
		FXHelper.setColor(expandedExpandButton.getGraphic(), color);
		FXHelper.setColor(collapsedExpandButton.getGraphic(), color);

	}
	
	protected Pane getContent() {
		return expanded ? expandedRoot : collapsedRoot;
	}
	
	protected void toggleMoreInfo() {
		
		if (showingMoreInfo) {

			collapsedRoot.getChildren().remove(collapsedButtons);

			collapsedRoot.add(collapsedStatusText, 2, 1);
			collapsedRoot.add(collapsedModVersion, 2, 2);
			collapsedRoot.add(collapsedInstallButton, 3, 1);

			expandedRoot.getChildren().remove(expandedExpandButton);
			expandedRoot.getChildren().remove(expandedButtons);
			expandedRoot.getChildren().remove(expandedDescription);
			expandedRoot.add(expandedExpandButton, 3, 2);
			
			GridPane.setValignment(expandedExpandButton, VPos.CENTER);
			
			expandedRoot.getRowConstraints().remove(descriptionConstraints);
			expandedRoot.getRowConstraints().remove(expandButtonConstraints);
			GridPane.setRowSpan(expandedExpandButton, 2);
			
			showingMoreInfo = false;
			
		} else {
			
			collapsedRoot.getChildren().remove(collapsedInstallButton);
			collapsedRoot.getChildren().remove(collapsedModVersion);
			collapsedRoot.getChildren().remove(collapsedStatusText);
			
			collapsedRoot.add(collapsedButtons, 2, 1);

			expandedRoot.getChildren().remove(expandedExpandButton);
			expandedRoot.add(expandedButtons, 3, 2);
			expandedRoot.add(expandedExpandButton, 3, 5);
			expandedRoot.add(expandedDescription, 1, 4);
			
			GridPane.setHalignment(expandedButtons, HPos.CENTER);
			GridPane.setValignment(expandedButtons, VPos.CENTER);
			GridPane.setValignment(expandedExpandButton, VPos.TOP);
			
			expandedRoot.getRowConstraints().add(descriptionConstraints);
			expandedRoot.getRowConstraints().add(expandButtonConstraints);
			GridPane.setRowSpan(expandedExpandButton, 1);
			
			showingMoreInfo = true;
			
		}
		
		updateImages();
		
	}
	
	public void expand(final boolean expand) {
		expanded = expand;
	}
	
	private void updateColors() {

		collapsedRoot.getStyleClass().clear();
		collapsedDisplayName.getStyleClass().clear();
		collapsedStatusText.getStyleClass().clear();
		collapsedModVersion.getStyleClass().clear();
		
		collapsedInstallButton.getStyleClass().remove("modview-installed-button-color");
		collapsedInstallButton.getStyleClass().remove("modview-not-installed-button-color");

		expandedStatusText.getStyleClass().clear();
		expandedDisplayName.getStyleClass().clear();
		expandedModVersion.getStyleClass().clear();
		expandedAuthor.getStyleClass().clear();
		expandedDescription.getStyleClass().clear();
		
		expandedInstallButton.getStyleClass().remove("modview-installed-button-color");
		expandedInstallButton.getStyleClass().remove("modview-not-installed-button-color");
		
		if (mod.isInstalled()) {
			
			collapsedRoot.getStyleClass().add("modview-installed");
			collapsedDisplayName.getStyleClass().add("modview-installed-text-color");
			collapsedStatusText.getStyleClass().add("modview-installed-text-color");
			collapsedModVersion.getStyleClass().add("modview-installed-text-color");
			collapsedInstallButton.getStyleClass().add("modview-installed-button-color");
			
			expandedHeader.setId("modview-expanded-header-installed");
			expandedStatusText.getStyleClass().add("modview-expanded-installed-header-color");
			expandedDisplayName.getStyleClass().add("modview-expanded-installed-text-color");
			expandedAuthor.getStyleClass().add("modview-expanded-installed-text-color");
			expandedModVersion.getStyleClass().add("modview-expanded-installed-header-color");
			expandedDescription.getStyleClass().add("modview-expanded-installed-description-text-color");
			expandedInstallButton.getStyleClass().add("modview-installed-button-color");
			
			Color color = CSSHelper.getColor("modview-installed-button-color", settings.getPropertyString("theme"));
			
			FXHelper.setColor(collapsedDeleteButton.getGraphic(), color);
			FXHelper.setColor(collapsedHideButton.getGraphic(), color);
			FXHelper.setColor(collapsedLinkButton.getGraphic(), color);
			
		} else {
			
			collapsedRoot.getStyleClass().add("modview-not-installed");
			collapsedDisplayName.getStyleClass().add("modview-not-installed-text-color");
			collapsedStatusText.getStyleClass().add("modview-not-installed-text-color");
			collapsedModVersion.getStyleClass().add("modview-not-installed-text-color");
			collapsedInstallButton.getStyleClass().add("modview-not-installed-button-color");
			
			expandedHeader.setId("modview-expanded-header-not-installed");
			expandedStatusText.getStyleClass().add("modview-expanded-not-installed-header-color");
			expandedDisplayName.getStyleClass().add("modview-expanded-not-installed-text-color");
			expandedModVersion.getStyleClass().add("modview-expanded-not-installed-header-color");
			expandedAuthor.getStyleClass().add("modview-expanded-not-installed-text-color");
			expandedDescription.getStyleClass().add("modview-expanded-not-installed-description-text-color");
			expandedInstallButton.getStyleClass().add("modview-not-installed-button-color");
			
			Color color = CSSHelper.getColor("modview-not-installed-button-color", settings.getPropertyString("theme"));
			
			FXHelper.setColor(collapsedDeleteButton.getGraphic(), color);
			FXHelper.setColor(collapsedHideButton.getGraphic(), color);
			FXHelper.setColor(collapsedLinkButton.getGraphic(), color);
			
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
					updateImages();
					updateColors();
					updateStrings();
					break;
			}
			
		}
		
	}

	
}