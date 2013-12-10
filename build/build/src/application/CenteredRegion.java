//See: http://stackoverflow.com/questions/15160181/javafx-image-png-transparency-crispness-being-lost-when-rendering

package application;

import javafx.scene.Node;
import javafx.scene.layout.Region;

class CenteredRegion extends Region {
	
	private Node content;

	CenteredRegion(Node content) {
		this.content = content;
		getChildren().add(content);
	}

	@Override
	protected void layoutChildren() {
		content.relocate(
				Math.round(getWidth() / 2 - content.prefWidth(USE_PREF_SIZE)
						/ 2),
				Math.round(getHeight() / 2 - content.prefHeight(USE_PREF_SIZE)
						/ 2));
	}

	public Node getContent() {
		return content;
	}
	
}
