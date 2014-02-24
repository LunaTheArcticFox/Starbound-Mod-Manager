package net.krazyweb.helpers;

import java.nio.file.Path;

public class ArchiveFile {
	
	private byte[] data;
	private Path path;
	private boolean folder;
	
	protected ArchiveFile() {
		
	}
	
	public ArchiveFile(final byte[] data, final Path path, final boolean folder) {
		this.data = data;
		this.path = path;
		this.folder = folder;
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
	
	public Path getPath() {
		return path;
	}
	
	public void setPath(Path path) {
		this.path = path;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}
	
}
