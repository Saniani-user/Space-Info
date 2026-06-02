package com.alex.app.help;

import java.util.ArrayList;
import java.util.List;

public class ViewPortsData {
	private List<DirectoryFile> fileTreeFiles;
	private List<DirectoryFile> childrenFiles;
	
	public void setFileTree(List<DirectoryFile> files) {
		fileTreeFiles = files;
	}
	
	public List<DirectoryFile> getfileTree() {
		if(fileTreeFiles == null) {
			return new ArrayList<DirectoryFile>();
		}
		else {
			return fileTreeFiles;
		}
	}
	
	public void setChildrenFiles(List<DirectoryFile> files) {
		childrenFiles = files;
	}
	
	public List<DirectoryFile> getChildrenFiles() {
		if(childrenFiles == null) {
			return new ArrayList<DirectoryFile>();
		}
		else {
			return childrenFiles;
		}
	}
}
