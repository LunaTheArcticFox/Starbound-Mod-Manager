package net.krazyweb.starmodmanager.view;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.krazyweb.helpers.CSSHelper;
import net.krazyweb.helpers.FXHelper;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue;
import net.krazyweb.starmodmanager.dialogue.MessageDialogue.MessageType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AboutView implements Observer {
	
	private static final Logger log = LogManager.getLogger(AboutView.class);
	
	private VBox root;
	private AnchorPane browseRepoPane;
	
	private AboutViewController controller;

	private SettingsModelInterface settings;
	private LocalizerModelInterface localizer;
	
	private Text title;
	private Text versionName;
	private Text createdBy;
	private Text createdByPerson;
	private Text withContributionsFrom;
	private Text[] contributors;
	private Text writtenIn;
	private Text[] writtenUsing;
	private Text browseRepo;
	private ImageView browseRepoArrow;
	
	protected AboutView() {
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new AboutViewController(this);
	}
	
	protected void build() {
		
		root = new VBox();
		root.setPadding(new Insets(0, 0, 0, 18));
		
		title = new Text();
		title.setId("about-view-title");
		
		versionName = new Text();
		versionName.setId("about-view-version");
		
		createdBy = new Text();
		createdBy.setId("about-view-sub-title");
		createdByPerson = getLinkedText("KrazyTheFox", "https://github.com/KrazyTheFox");
		createdByPerson.setId("about-view-sub-title-link");
		
		HBox createdByContainer = new HBox();
		createdByContainer.getChildren().addAll(createdBy, createdByPerson);
		
		withContributionsFrom = new Text();
		withContributionsFrom.setId("about-view-sub-title");
		
		writtenIn = new Text();
		writtenIn.setId("about-view-sub-title");
		
		contributors = new Text[1];
		
		contributors[0] = getLinkedText("Kyr", "https://github.com/kxy");
		
		writtenUsing = new Text[8];
		
		writtenUsing[0] = getLicensedText("StarDB for Java", new LicenseView("StarDB for Java", "license_stardb4j.txt"));
		writtenUsing[1] = getLicensedText("7-Zip-JBinding", new LicenseView("7-Zip-JBinding", "license_7zjb.txt"));
		writtenUsing[2] = getLicensedText("hsqldb", new LicenseView("HyperSQL", "license_hsqldb.txt"));
		writtenUsing[3] = getLicensedText("log4j2", new LicenseView("log4j2", "license_log4j2.txt"));
		writtenUsing[4] = getLicensedText("icu4j", new LicenseView("International Components for Unicode", "license_icu4j.txt"));
		writtenUsing[5] = getLicensedText("junit", new LicenseView("JUnit", "license_junit.txt"));
		writtenUsing[6] = getLicensedText("commons-io", new LicenseView("Apache commons-io", "license_commons-io.txt"));
		writtenUsing[7] = getLicensedText("minimal-json", new LicenseView("minimal-json", "license_minimal-json.txt"));
		
		root.getChildren().addAll(
			title,
			versionName,
			new Text(),
			createdByContainer,
			new Text(),
			withContributionsFrom
		);
		
		for (Text t : contributors) {
			t.setId("about-view-contributor");
			root.getChildren().add(t);
		}
		
		root.getChildren().addAll(new Text(), writtenIn);
		
		for (Text t : writtenUsing) {
			t.setId("about-view-library");
			root.getChildren().add(t);
		}

		browseRepoPane = new AnchorPane();
		VBox.setVgrow(browseRepoPane, Priority.ALWAYS);
		
		browseRepoArrow = new ImageView(new Image(ModView.class.getClassLoader().getResourceAsStream("browse-repo-arrow.png")));
		browseRepoPane.getChildren().add(browseRepoArrow);
		AnchorPane.setBottomAnchor(browseRepoArrow, 3.0);
		AnchorPane.setLeftAnchor(browseRepoArrow, 0.0);
		
		root.getChildren().addAll(new Text(), browseRepoPane);
		
		updateStrings();
		updateColors();
		
	}
	
	private void updateStrings() {

		title.setText(localizer.getMessage("appname"));
		versionName.setText(localizer.formatMessage("versionwithapple", settings.getVersion(), settings.getApple()));
		createdBy.setText(localizer.getMessage("aboutview.createdby"));
		withContributionsFrom.setText(localizer.getMessage("aboutview.contributions"));
		writtenIn.setText(localizer.getMessage("aboutview.writtenin"));

		browseRepoPane.getChildren().remove(browseRepo);
		browseRepo = getLinkedText(localizer.getMessage("aboutview.browserepo"), "https://github.com/KrazyTheFox/Starbound-Mod-Manager");
		browseRepo.setId("about-view-browse-repo");
		AnchorPane.setBottomAnchor(browseRepo, 0.0);
		AnchorPane.setLeftAnchor(browseRepo, 22.0);
		browseRepoPane.getChildren().add(browseRepo);
		
	}
	
	private void updateColors() {
		FXHelper.setColor(browseRepoArrow, CSSHelper.getColor("browse-repo-arrow-color", settings.getPropertyString("theme")));
	}
	
	protected Node getContent() {
		return root;
	}
	
	private Text getLinkedText(final String text, final String url) {
		
		Text output = new Text(text);
		output.setId("about-view-linked-text");
		
		output.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				try {
					controller.openWebpage(url);
				} catch (URISyntaxException | IOException e) {
					log.error("", e);
					MessageDialogue dialogue = new MessageDialogue(localizer.getMessage("aboutview.linkerror"), localizer.getMessage("aboutview.linkerror.title"), MessageType.ERROR, new LocalizerFactory());
					dialogue.getResult();
				}
			}
			
		});
		
		return output;
		
	}
	
	private Text getLicensedText(final String text, final LicenseView license) {
		
		Text output = new Text(text);
		output.setId("about-view-linked-text");
		
		output.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				license.open();
			}
			
		});
		
		return output;
		
	}
	
	@Override
	public void update(final Observable observable, final Object message) {
		
		if (observable instanceof LocalizerModelInterface && message.equals("localechanged")) {
			updateStrings();
		}
		
	}
	
}
