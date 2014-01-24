package main.java.net.krazyweb.helpers;

public class ArchiveFile {
	
	private byte[] data;
	private String path;
	private boolean folder;
	
	protected ArchiveFile() {
		
	}
	
	public ArchiveFile(final ArchiveFile f) {
		this.data = f.data;
		this.path = f.path;
		this.folder = f.folder;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}
	
}
