package com.alex.app.help;

import java.io.File;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class NodeInfo {
	private Long id;
	private String name;
	private boolean rootDirectory;
	
	public NodeInfo(DirectoryFile df){
		setId(df.getId());
		setName(df.getName());
		setRootDirectory(df.isRoot());
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the directory
	 */
//	public boolean isDirectory() {
//		return directory;
//	}
	/**
	 * @param directory the directory to set
	 */
//	public void setDirectory(boolean directory) {
//		this.directory = directory;
//	}
	/**
	 * @return true if root, otherwise return false
	 */
	public boolean isRootDirectory() {
		return rootDirectory;
	}
	/**
	 * @param set rootDirectory true if this NodeInfo is root
	 */
	public void setRootDirectory(boolean rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || id == null) {
			return false;
		}
		if(o instanceof NodeInfo ni) {
			return equals(ni);
		}
		else if(o instanceof DirectoryFile df) {
			return equals(df);
		}
		else {
			return super.equals(o);
		}
	}
	
	public <T extends NodeInfo> boolean equals(T o) {
		if(o == null || id == null) {
			return false;
		}
		if(id.equals(o.getId())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public <T extends DirectoryFile> boolean equals(T df) {
		if(df == null||id == null) {
			return false;
		}
		if(id.equals(df.getId())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return new File(name).toPath().toFile().hashCode();
	}
	
	public double getMinWidth(double leftMargin) {
		double widthContainers = 35.0 + 6.0 + 25.0 + 6.0 + 25.0;
		
		Text textNode;
		if (rootDirectory) {
			textNode = new Text(name);
			textNode.setFont(Font.font("Arial", 13.0));
			
			return textNode.getLayoutBounds().getWidth() + 8.0;
		}
		else {
			textNode = new Text(new File(name).getName());
			textNode.setFont(Font.font("Arial", 12.0));
		}
		
		return widthContainers + textNode.getLayoutBounds().getWidth() + leftMargin + 8.0;
	}
	
}
