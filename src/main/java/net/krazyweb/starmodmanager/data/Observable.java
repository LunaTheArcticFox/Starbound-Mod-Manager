package net.krazyweb.starmodmanager.data;

public interface Observable {
	
	public void addObserver(final Observer observer);
	public void removeObserver(final Observer observer);

}
