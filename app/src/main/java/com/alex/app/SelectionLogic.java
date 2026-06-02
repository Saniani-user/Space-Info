package com.alex.app;

import java.util.HashSet;

import com.alex.app.help.NodeInfo;
import com.alex.controller.MainController;

public class SelectionLogic {
	Logic logic;
	MainController mainController;
	private HashSet<NodeInfo> selectedFiles;
	
	public SelectionLogic (MainController mainController, Logic logic){
		this.mainController = mainController;
		this.logic = logic;
		selectedFiles = new HashSet<NodeInfo>();
	}
	
	public HashSet<NodeInfo> getSelectedFiles() {
		return selectedFiles;
	}
	
	public void findSelectedFileIndex(NodeInfo file) {
//		if (mainController.controllerData.isShiftDown()) {
//			if (mainController.controllerData.getFirstSelectedFile() == -1) {
//				findFirstSelectedFileIndex(file);
//			}
//			else {
//				findLastSelectedFileIndex(file);
//			}
//			
//		}
//		else {
//			findFirstSelectedFileIndex(file);
//		}
		
		findFirstSelectedFileIndex(file);
	}
	
	private void findFirstSelectedFileIndex(NodeInfo parent) {
		
		int firstSelectedFile = logic.getShowedFiles().indexOf(parent);
		mainController.controllerData.setFirstSelectedFie(firstSelectedFile);
//		mainController.controllerData.setLastSelectedFile(-1);
	}
	
//	private void findLastSelectedFileIndex(NodeInfo parent) {
//		int lastSelectedFile = logic.getShowedFiles().indexOf(parent);
//		mainController.controllerData.setLastSelectedFile(lastSelectedFile);
//	}
}
