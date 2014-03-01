package net.krazyweb.starmodmanager.view;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.krazyweb.starmodmanager.data.LocalizerFactory;
import net.krazyweb.starmodmanager.data.LocalizerModelInterface;
import net.krazyweb.starmodmanager.data.Observable;
import net.krazyweb.starmodmanager.data.Observer;
import net.krazyweb.starmodmanager.data.SettingsFactory;
import net.krazyweb.starmodmanager.data.SettingsModelInterface;


public class AboutView implements Observer {
	
	private VBox root;
	
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
	
	protected AboutView() {
		settings = new SettingsFactory().getInstance();
		localizer = new LocalizerFactory().getInstance();
		localizer.addObserver(this);
		controller = new AboutViewController(this);
	}
	
	protected void build() {
		
		root = new VBox();

		title = new Text();
		versionName = new Text();
		createdBy = new Text();
		withContributionsFrom = new Text();
		writtenIn = new Text();
		browseRepo = new Text();
		
		createdByPerson = getLinkedText("KrazyTheFox", "https://github.com/KrazyTheFox");
		
		contributors = new Text[1];
		
		contributors[0] = getLinkedText("Kyr", "https://github.com/kxy");
		
		writtenUsing = new Text[8];
		
		writtenUsing[0] = getLicensedText("StarDB for Java", new LicenseView("StarDB for Java", "license_stardb4j.txt"));
		writtenUsing[1] = getLicensedText("7-Zip-JBinding", new LicenseView("7-Zip-JBinding", "license_7zjb.txt"));
		writtenUsing[2] = getLicensedText("hsqldb", new LicenseView("", "license_hsqldb.txt"));
		writtenUsing[3] = getLicensedText("log4j2", new LicenseView("", "license_log4j2.txt"));
		writtenUsing[4] = getLicensedText("icu4j", new LicenseView("", "license_icu4j.txt"));
		writtenUsing[5] = getLicensedText("junit", new LicenseView("", "license_junit.txt"));
		writtenUsing[6] = getLicensedText("commons-io", new LicenseView("", "license_commons-io.txt"));
		writtenUsing[7] = getLicensedText("minimal-json", new LicenseView("", "license_minimal-json.txt"));
		
		root.getChildren().addAll(
			title,
			versionName,
			createdBy,
			createdByPerson,
			withContributionsFrom
		);
		
		for (Text t : contributors) {
			root.getChildren().add(t);
		}
		
		root.getChildren().add(writtenIn);
		
		for (Text t : writtenUsing) {
			root.getChildren().add(t);
		}
		
		root.getChildren().add(browseRepo);
		
		updateStrings();
		
	}
	
	private void updateStrings() {

		title.setText(localizer.getMessage("appname"));
		versionName.setText(localizer.formatMessage("version", settings.getVersion()));
		createdBy.setText(localizer.getMessage("aboutview.createdby"));
		withContributionsFrom.setText(localizer.getMessage("aboutview.contributions"));
		writtenIn.setText(localizer.getMessage("aboutview.writtenin"));

		root.getChildren().remove(browseRepo);
		browseRepo = getLinkedText(localizer.getMessage("aboutview.browserepo"), "https://github.com/KrazyTheFox/Starbound-Mod-Manager");
		root.getChildren().add(browseRepo);
		
	}
	
	protected Node getContent() {
		return root;
	}
	
	private Text getLinkedText(final String text, final String url) {
		
		Text output = new Text(text);
		
		output.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				try {
					controller.openWebpage(url);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		
		return output;
		
	}
	
	private Text getLicensedText(final String text, final LicenseView license) {
		
		Text output = new Text(text);
		
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
