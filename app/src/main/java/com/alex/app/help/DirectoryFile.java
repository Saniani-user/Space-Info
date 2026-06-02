package com.alex.app.help;


import java.io.File;
import java.io.Serializable;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.alex.app.Logic;
import com.alex.controller.MainController;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
@Entity
@Table(name = "my_files", indexes = {
	@Index(name = "idx_parent_hierarchy", columnList = "parent_id, is_directory, name"),
	@Index(name = "idx_name", columnList = "name"),
	@Index(name = "idx_size", columnList = "parent_id, is_directory, file_size, name"),
	@Index(name = "idx_size_ignore_dirs", columnList = "file_size, name")
})
public class DirectoryFile implements Serializable{
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "parent_id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private DirectoryFile parent;
	@Column(name = "name", length = 1024, unique = true)
	private String name;
	
	@Column(name = "is_open")
	private boolean open = false;
	@Column(name = "is_root_directory")
	private boolean rootDirectory = false;
	@Column(name = "is_directory")
	private boolean directory = false;
	@Column(name = "is_scanned")
	private boolean scanned = false;
	@Column(name = "file_size")
	private double fileSize = 0l;
	
	
	
	@Transient
	private static final long serialVersionUID = 900L;
	@Transient
	private static transient Logic LOGIC;
	@Transient
	private static transient MainController MAIN_CONTROLLER;
//	@Transient
//	private ImageView iconContainer;
	@Transient
	private MyFile thisFile;
		
	public DirectoryFile(MyFile thisFile, TypeFile type) {

		setType(type);
		this.thisFile = thisFile;
//		initIsDirectory();s
		name = thisFile.toString();
	}
	
	public DirectoryFile() {
		
	}
	
	@PostLoad
	public void recoveryFromSQL() {
		thisFile = new MyFile(new File(name));
	}
	
//	private void makeSpaceIndicator() {
//		indicator = new Label();
//		indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("red-indicator"), true);
//		indicator.setPrefSize(5.0, 25*0.5);
//		indicator.setMinSize(5.0, 25*0.5);
//		indicator.setMaxSize(5.0, 25*0.5);
//	}
	
	public static void setLogic(Logic logic) {
		DirectoryFile.LOGIC = logic;
	}
	
	public static void setMainController (MainController mainController) {
		DirectoryFile.MAIN_CONTROLLER = mainController;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	
	public MyFile getRoot() {
		if (rootDirectory) {
			return thisFile;
		}
		else {
			return new MyFile(thisFile.toPath().getRoot().toFile());
		}
	}
	
	public DirectoryFile getParentObj() {
		if (rootDirectory) {
			return null;
		}
		else {
			return LOGIC.mySQL.getFile(thisFile.getParent());
		}
	}
	
	public void setParent(DirectoryFile parent) {
		this.parent = parent;
	}
	

	
//	public void setSpaceIndicator (Label i) {
//		indicator = i;
//	}
//	
//	public Label getSpaceIndicator() {
//		if(indicator == null) {
//			makeSpaceIndicator();
//		}
//		return indicator;
//	}
	
	public MyFile getThisFile() {
		return thisFile;
	}
	
	public void setName (String name) {
		if(thisFile == null) {
			thisFile = new MyFile(name);
		}
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean isRoot() {
		return rootDirectory;
	}

	public void setRootDirectory(boolean isRoot) {
		this.rootDirectory = isRoot;
	}

	public boolean isDirectory() {
		return directory;
	}
	
	public void setDirectory(boolean isDirectory) {
		directory = isDirectory;
	}

	public boolean isRegularFile() {
		return !directory;
	}

	public void setRegularFile(boolean isRegularFile) {
		this.directory = !isRegularFile;
	}

	public boolean isScanned() {
		return scanned;
	}

	public void setScanned (boolean scanned) {
		this.scanned = scanned;
	}
	
	public double getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(double size) {
		fileSize = size;
	}
		
	public void increaseSize(double s) {
		fileSize += s;
	}

	public double getShare() {
		double share = getParentObj().getFileSize() == 0.0 ? 0.0 : fileSize/getParentObj().getFileSize();
		return share;
	}
	
	public DirectoryFile getParent() {
		return parent;
	}

	public MyFile getParentFile() {
		return thisFile.getParentFile();
	}

	public String toString() {
		return thisFile.toString();
	}
	
	public NodeInfo getNodeInfo() {
		return new NodeInfo(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==null) {
			return false;
		}
		if(o instanceof DirectoryFile df) {
			return equals(df);
		}
		else if(o instanceof NodeInfo ni) {
			return equals(ni);
		}
		else if(o instanceof File file) {
			return equals(file);
		}
		else {
			return super.equals(o);
		}
	}
	
	public <T extends DirectoryFile> boolean equals(T o) {
		if (o == null || id == null) {
			return false;
		}
		else {
			if(thisFile.equals(o.getThisFile())) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	public <T extends NodeInfo> boolean equals (T o) {
		if(o==null || id == null) {
			return false;
		}
		if(id.equals(o.getId())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public <T extends File> boolean equals (T o) {
		if (o == null) {
			return false;
		}
		if(thisFile.equals(o)) {
			return true;
		}
		else return false;
	}
	
	@Override
	public int hashCode() {
		return thisFile.hashCode();
	}
	
	private void setType(TypeFile type) {
		if(type == TypeFile.root) {
			rootDirectory = true;
			directory = true;
		}
		else if(type == TypeFile.file) {
			directory = false;
		}
		else if(type == TypeFile.dir) {
			directory = true;
		}
	}
	
}
