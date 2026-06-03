package com.alex.app.help;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.alex.app.Logic;
import com.alex.controller.MainController;

public class ChildrensExtractor {
	private static MainController MAIN_CONTROLLER;
	
	private static Logic LOGIC;
	
	private DirectoryFile parent;
	
	private Long id;
	
	private MyFile thisFile;
	
	private static SortType sort = SortType.NAME;
	
	private static byte SORT_NAME_I = 0;
	
	private static byte SORT_SIZE_I = 0;
	
	private static List<MyFile> newFiles = new ArrayList<MyFile>();
	
	private static  List<DirectoryFile> oldFiles = new ArrayList<DirectoryFile>();
	
	public static List<NodeInfo> COPY_SHOWED_FILES;
	
	public static List<MyFile> onlyNew;
	
	private boolean isChanged;
	
	public static void setLogic(Logic l) {
		LOGIC = l;
	}
	
	public static void setMainController(MainController mc) {
		MAIN_CONTROLLER = mc;
	}
	
	public void autoCheckExists(DirectoryFile parent) {
		setFieldsFromParent(parent);
		
		oldFiles.clear();
		newFiles.clear();
		if (!checkExists()) {
			if (parent.equals(MAIN_CONTROLLER.controllerData.getOpenedInChildrenFiles())) {
				MAIN_CONTROLLER.controllerData.setOpenedInChildrenFiles(null);
			}
		}
	}

	public ViewPortsData getNewChildrenDirectoryFiles(DirectoryFile parent, RefreshViewPort refresh) {
		setFieldsFromParent(parent);
		
		if (!checkExists()) {
			if(parent.equals(MAIN_CONTROLLER.controllerData.getOpenedInChildrenFiles())) {
				MAIN_CONTROLLER.controllerData.setOpenedInChildrenFiles(null);
			}
				return new ViewPortsData();
			}
		
		List<DirectoryFile> allChildrens = getChildrenDirectoryFiles(refresh);
		
		if(isChanged) {
			refreshGUI(refresh, allChildrens);
		}
		var allViewPorts = new ViewPortsData();
		allViewPorts.setFileTree(allChildrens);
		allViewPorts.setChildrenFiles(getChildrenShowedFiles());
		
		return allViewPorts;
	}
	
	private List<DirectoryFile> getChildrenDirectoryFiles( RefreshViewPort refresh) {
		
		getNewFiles();
		
		COPY_SHOWED_FILES = LOGIC.getCopyShowedTree();
		if(!isChanged()) {
			List<DirectoryFile> childrenDirectoryFiles = new ArrayList<DirectoryFile>(oldFiles);
			oldFiles.clear();
			newFiles.clear();
			return childrenDirectoryFiles;
		}
		removeUnexists();
		
		return addNew();
	}
	
	private void setFieldsFromParent(DirectoryFile parent) {
		this.parent = parent;
		id = parent.getId();
		thisFile = parent.getThisFile();
		COPY_SHOWED_FILES = LOGIC.getCopyShowedTree();
	}

	private List<DirectoryFile> addNew() {
		findNewFiles();
		if(onlyNew.size()==0) {
			oldFiles.clear();
			return LOGIC.mySQL.getByParentOrderedByNameAsc(id);
		}
		
		for(MyFile file : onlyNew) {
			LOGIC.refreshingFiles.tryAddToMap(file);
		}
		LOGIC.mySQL.increaseAllParentAndThis(parent);
		
		List<DirectoryFile> allChildrens = addNewInGUI(onlyNew);
		oldFiles.clear();
		newFiles.clear();
		return allChildrens;
	}

	private List<DirectoryFile> addNewInGUI(List<MyFile> onlyNew) {
		List<DirectoryFile> allChildrens = LOGIC.mySQL.getByParentOrderedByNameAsc(id);
		return allChildrens;
	}

	private void getNewFiles() {
		if(!thisFile.exists()) {
			return;
		}
		List<File> files = new ArrayList<File>( Arrays.asList(thisFile.listFiles()));
		HashSet<File> newFilesSet = new HashSet<File>(files);
		HashSet<File> deniedSet = new HashSet<File>(LOGIC.getDeniedFolders());
		newFilesSet.removeAll(deniedSet);
		files = new ArrayList<File>(newFilesSet);
		newFiles = files.stream().map(MyFile::new).collect(Collectors.toList());
		
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean isChanged() {
		if(!thisFile.exists()) {
			isChanged = true;
			return true;
		}
		oldFiles = LOGIC.mySQL.getByParentOrderedByNameAsc(id);
		if(oldFiles.size()!=newFiles.size()) {
			isChanged = true;
			return true;
		}
		HashSet<DirectoryFile> oldSet = new HashSet<DirectoryFile>(oldFiles);
		isChanged = !oldSet.containsAll(newFiles);
		return isChanged;
		
	}
	/**
	 * ищет и удаляет все несуществующие файлы из БД,
	 * если файл открыт, то и из showed files.
	 */
	private void removeUnexists() {
		List<DirectoryFile> unexists = getUnexistFiles();
		
		if(unexists.size() == 0) {
			return;
		}
		
		LOGIC.mySQL.removeFormSQLAndShowedFiles(unexists);
		LOGIC.mySQL.setSizeFromSQL(parent);
	}
	
	private void removeUnexists(List<DirectoryFile> unexists) {
		
		if(unexists.size() == 0) {
			return;
		}
		LOGIC.mySQL.removeFormSQLAndShowedFiles(unexists);
		LOGIC.mySQL.setSizeFromSQL(parent);
	}
	
	@SuppressWarnings("unlikely-arg-type")
	private List<DirectoryFile> getUnexistFiles() {
		HashSet<DirectoryFile> oldFilesSet = new HashSet<DirectoryFile>(oldFiles);
		HashSet<MyFile> newFilesSet = new HashSet<MyFile>(newFiles);
		oldFilesSet.removeAll(newFilesSet);
		return new ArrayList<DirectoryFile>(oldFilesSet);
	}
	
	private boolean checkExists() {
		if(thisFile.exists()) {
			return true;
		}
		var unexists = new ArrayList<DirectoryFile>();
		unexists.add(parent);
		removeUnexists(unexists);
//		COPY_SHOWED_FILES = LOGIC.getCopyShowedTree();
//		COPY_SHOWED_FILES.remove(null);
		var newShowedFiles = new ArrayList<NodeInfo>(COPY_SHOWED_FILES);
		refreshFileTree(newShowedFiles);
		return false;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	private void findNewFiles() {
		LinkedHashSet<DirectoryFile> oldSet = new LinkedHashSet<DirectoryFile>(oldFiles);
		HashSet<MyFile> newSet = new HashSet<MyFile>(newFiles);
		newSet.removeAll(oldSet);
		onlyNew = new ArrayList<MyFile>(newSet);
	}
	/*
	 * создаем список отсортированных файлов для
	 * childrenBox
	 */
	private List<DirectoryFile> getChildrenShowedFiles() {
		if (sort == SortType.NAME) {
			return getOrderedByName();
		}
		else {
			return getOrderedBySize();
		}
	}
	
	private  List<DirectoryFile> getOrderedByName() {
		return switch(SORT_NAME_I) {
		case(0)->orderedByNameAsc();
		default -> orderedByNameDesc();
		};
	}
	
	private List<DirectoryFile> orderedByNameAsc(){
		return LOGIC.mySQL.getByParentOrderedByNameAsc(id);
	}
	
	private List<DirectoryFile> orderedByNameDesc(){
		return LOGIC.mySQL.getByParentOrderedByNameDesc(id);
	}
	
	private List<DirectoryFile> getOrderedBySize(){
		return switch(SORT_SIZE_I) {
		case(0)-> getOrderedBySizeDesc();
		case(1)->getOrderedBySizeAsc();
		default->getOrderedBySizeIgnoreDirectories();
		};
	}
	
	private List<DirectoryFile> getOrderedBySizeDesc() {
		return LOGIC.mySQL.getByParentOrderedBySizeDesc(id);
	}
	
	private List<DirectoryFile> getOrderedBySizeAsc(){
		return LOGIC.mySQL.getByParentOrderedBySizeAsc(id);
	}
	
	private List<DirectoryFile> getOrderedBySizeIgnoreDirectories(){
		return LOGIC.mySQL.getByParentOrderedBySizeIgnoreDirectories(id);
	}
	
	public void increaseSortNameIndex() {
		if(SORT_NAME_I==1) {
			SORT_NAME_I = 0;
		}
		else {
			SORT_NAME_I++;
		}
	}
	
	public void increaseSortSizeIndex() {
		if(SORT_SIZE_I==2) {
			SORT_SIZE_I = 0;
		}
		else {
			SORT_SIZE_I++;
		}
	}
	
	public void selectSortTypeByName() {
		if (sort != SortType.NAME) {
			sort = SortType.NAME;
			SORT_SIZE_I = -1;
		}
	}
	
	public void selectSortTypeBySize() {
		if (sort != SortType.SIZE) {
			sort = SortType.SIZE;
			SORT_NAME_I = -1;
		}
	}
	/*
	 * тут задаем обновление интерейса
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void refreshGUI(RefreshViewPort refresh, List<DirectoryFile> allChildrens) {
		HashSet<NodeInfo> showed = new HashSet<NodeInfo>(COPY_SHOWED_FILES);
		
		if (refresh == RefreshViewPort.CHILDREN_FILES
				&& parent.equals(MAIN_CONTROLLER.controllerData.getOpenedInChildrenFiles())) {
			synchronized (MAIN_CONTROLLER) {
				if (parent.isOpen() && showed.contains(parent)) {
					LOGIC.addInShowedTree(parent, allChildrens, onlyNew);
					
				} 
				var newShowedFiles = new ArrayList<NodeInfo>(COPY_SHOWED_FILES);
				refreshAllViewPorts(newShowedFiles, allChildrens);
			}
		}
		else if(refresh == RefreshViewPort.FILE_TREE) {
			
			synchronized (MAIN_CONTROLLER) {
				if (parent.isOpen() && showed.contains(parent)) {
					LOGIC.addInShowedTree(parent, allChildrens, onlyNew);
					
				}
				var newShowedFiles = new ArrayList<NodeInfo>(COPY_SHOWED_FILES);
				refreshFileTree(newShowedFiles);
			}
			
		}
		else if (refresh == RefreshViewPort.ALL) {
			synchronized (MAIN_CONTROLLER) {
				if (parent.isOpen() && showed.contains(parent)) {
					LOGIC.addInShowedTree(parent, allChildrens, onlyNew);
					
				}
				var newShowedFiles = new ArrayList<NodeInfo>(COPY_SHOWED_FILES);
				refreshAllViewPorts(newShowedFiles, allChildrens);
			}
			
		}

	}

	private void refreshFileTree(ArrayList<NodeInfo> newShowedFiles) {
		
		synchronized (MAIN_CONTROLLER) {
			LOGIC.setShowedTree(newShowedFiles);
			LOGIC.updateChildrenFiles(MAIN_CONTROLLER.controllerData.getOpenedInChildrenFiles());
			MAIN_CONTROLLER.controllerData.getFileTree().refreshFileTreeBox();
			MAIN_CONTROLLER.controllerData.getChildrenFiles().refreshChildrenFiles();
		}
	}
	
	private void refreshAllViewPorts(ArrayList<NodeInfo> newShowedFiles, List<DirectoryFile> showedChildrenFiles) {

		synchronized (MAIN_CONTROLLER) {
			LOGIC.setShowedChildrenFiles(showedChildrenFiles);
			LOGIC.setShowedTree(newShowedFiles);
			MAIN_CONTROLLER.controllerData.getChildrenFiles().refreshChildrenFiles();
			MAIN_CONTROLLER.controllerData.getFileTree().refreshFileTreeBox();
		}
	}
	

}
