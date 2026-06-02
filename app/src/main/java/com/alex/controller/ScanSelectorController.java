package com.alex.controller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.alex.app.Logic;
import com.alex.app.help.DirectoryFile;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScanSelectorController {
	
	private final String mainContainerStatus = "container";
	private final PseudoClass mainContainerClass = PseudoClass.getPseudoClass(mainContainerStatus);
	
	private final String selectedContainerStatus = "selected-container";
	private final PseudoClass selectedContainerClass = PseudoClass.getPseudoClass(selectedContainerStatus);

    @FXML
    private Button cancel;
    
    @FXML
    private HBox scrollPanelContainer;

    @FXML
    private VBox rootViewPort;

    @FXML
    private Button scan;

    @FXML
    private VBox scrollTrackY;

    @FXML
    private Label sliderY;
    
    MainController mainController;
    Logic logic;
	ContainerFactory containerFactory;
	RootsView rootsView;
	Map<DirectoryFile, HBox> containerMap;
	List<DirectoryFile> roots;
	public void initialize() {
		containerFactory = new ContainerFactory();
		containerMap = new HashMap<DirectoryFile, HBox>();
		setButtonsAction();
		setViewPortStyle();
	}
	private void setViewPortStyle() {
		rootViewPort.getStyleClass().add("select-disks");
		PseudoClass rootViewPortStyle = PseudoClass.getPseudoClass("disks");
		rootViewPort.pseudoClassStateChanged(rootViewPortStyle, true);
	}
	
	public void prepareForShow() {
//		showRoots();
		rootsView = new RootsView();
	}
	
	public void setMainController(MainController controller) {
		mainController = controller;
		logic = mainController.controllerData.getLogic();
		roots = logic.mySQL.getRoots();
	}
	
	private void setButtonsAction() {
		scan.setOnMouseClicked(_->{
			findRootsToScan();
		});
		
		cancel.setOnMouseClicked(_->{
			Stage scanSelectionWindow = (Stage)cancel.getScene().getWindow();
			scanSelectionWindow.close();
		});
	}
	
	public void setOnSceneKeyPressed() {
		cancel.getScene().setOnKeyPressed(key->{
			if(key.getCode()==KeyCode.SHIFT) {
				RootsView.SelectRoot.LAST_FILE_INDEX = -1;
			}
		});
	}
	
	private void findRootsToScan() {
		List<DirectoryFile> rootsToScan = new ArrayList<DirectoryFile>();
		for (DirectoryFile root : roots) {
			HBox rootContainer = containerMap.get(root);
			if (rootContainer.getPseudoClassStates().contains(selectedContainerClass)) {
				rootsToScan.add(root);
			}
		}
		scanRoots(rootsToScan);
	}
	
	private void scanRoots(List<DirectoryFile> rootsToScan) {
		mainController.root.setMouseTransparent(true);
		stopUpdateThread();
		Stage stage = (Stage)scan.getScene().getWindow();
		stage.close();
		HBox infoBox = mainController.addInfoPanel("Сканирую диски");
		Thread scanThread = new Thread(new ScanThread(rootsToScan, infoBox), "Scan thread");
		scanThread.start();
		mainController.controllerData.getRunnedThreads().add(new WeakReference<Thread>(scanThread));
	}
	
	private void stopUpdateThread() {
		for(WeakReference<Thread> ref : mainController.controllerData.getRunnedThreads()) {
			Thread thread = ref.get();
			if (thread!=null&&thread.getName().equalsIgnoreCase(Logic.UpdateFileTree.THREAD_NAME)) {
				updateThread = thread;
				thread.interrupt();
			}
		}
	}
	Thread updateThread;
	class ScanThread implements Runnable{
		List<DirectoryFile> rootsToScan;
		HBox infoBox;
		ScanThread(List<DirectoryFile> rootsToScan, HBox infoBox){
			
			this.rootsToScan = rootsToScan;
			this.infoBox = infoBox;
		}

		@Override
		public void run() {
			
			while(updateThread.isAlive()) {
				try {
					wait(10);
				} catch (InterruptedException e) {
					Thread.interrupted();
					return;
				}
			}
			
//			if(updateThread!=null) {
//				mainController.controllerData.getRunnedThreads().remove(updateThread);
//			}
			
			for (DirectoryFile root : rootsToScan) {
				logic.mySQL.deleteAllChildrens(root);
				logic.calculateRootMap(root.getThisFile(), root);
				root.setScanned(true);
				root.setOpen(false);
				
				if (root.getThisFile().exists()) {
					try {
						logic.mySQL.saveEntity(root);
					} catch (ObjectOptimisticLockingFailureException e) {

					}
				}
				
				if(Thread.interrupted()||mainController.controllerData.appClosing()) {
//					mainController.controllerData.getRunnedThreads().remove(Thread.currentThread());
					Thread.currentThread().interrupt();
					return;
				}
			}
			
			System.gc();
			
			Platform.runLater(() -> {
				mainController.mainWorkflow.getChildren().remove(infoBox);
				mainController.controllerData.getFileTree().updateAfterScan();
				mainController.root.setMouseTransparent(false);
			});
			if(Thread.interrupted()||mainController.controllerData.appClosing()) {
				Thread.currentThread().interrupt();
				return;
			}
			else {
				logic.startUpdateFileTree();
			}
			
		}
		
	}
	
	class ContainerFactory{
		
		private static double STANDART_CONTAINER_HEIGHT = 25.0;
		
		private HBox getRootContainer(DirectoryFile root) {
			Label rootName = new Label(root.getThisFile().toString());
			HBox rootContainer = new HBox(rootName);
			containerMap.put(root, rootContainer);
			rootContainer.getStyleClass().add("scan-selector-container");
			setRootAction(root, rootContainer);
			rootContainer.setAlignment(Pos.CENTER);
			setSize(rootContainer, Region.USE_COMPUTED_SIZE, STANDART_CONTAINER_HEIGHT);
			return rootContainer;
		}
		
		private void setRootAction(DirectoryFile root, HBox rootContainer) {
			rootContainer.setOnMouseClicked(click->{
				rootsView.selectRoot.findSelectedFiles(click, root);
			});
		}
		
		private void setSize(Region region, double width, double height) {
			region.setMinSize(width, height);
			region.setPrefSize(width, height);
			region.setMaxSize(width, height);
		}
		
		private void setContainerStatus(HBox container, String status) {
			container.pseudoClassStateChanged(mainContainerClass, mainContainerStatus.equals(status));
			container.pseudoClassStateChanged(selectedContainerClass, selectedContainerStatus.equals(status));
			 
		}
	}
	
	class RootsView{
		ScrollPanel scrollPanel;
		SelectRoot selectRoot;
		
		RootsView(){
			
			scrollPanel = new ScrollPanel();
			selectRoot = new SelectRoot();
			
		}
		
		class ScrollPanel{
			
			private static double SCROLL_PANEL_MAX_WIDTH;
			private static double VIEW_PORT_WIDTH;
			private static double VIEW_PORT_HEIGHT;
			private static double VIEW_PORT_MAX_Y;
			private static double CURRENT_VIEW_PORT_Y = 0.0;
			
			private static double SCROLL_TRACK_Y_HEIGHT;
			private static double SLIDER_Y_HEIGHT;
			private static double TARGET_SLIDER_Y = 0.0;
			private static double MAX_SLIDER_Y;
			private static double SCENE_OFFSET_Y;
			private static double SHARE_VISIBLE_FILES;
			
			private static double TOTAL_CELL_HEIGHTS = 0.0;
			
			private static double FILE_OFFSET_Y = 0.0;
			
			private static int FIRST_FILE_INDEX;
			private static int LAST_FILE_INDEX;
			
			
			ScrollTrack scrollTrack;
			Drag drag;
			
			ScrollPanel(){
				SCROLL_PANEL_MAX_WIDTH = scrollPanelContainer.getMaxWidth();
				SCROLL_TRACK_Y_HEIGHT = VIEW_PORT_HEIGHT = rootViewPort.getMaxHeight();
				VIEW_PORT_WIDTH = rootViewPort.getMaxWidth();
				scrollTrack = new ScrollTrack();
				firstShowRoots();
				drag = new Drag();
				setSliderYAction();
			}
			
			private void firstShowRoots() {
				scrollTrack.findTotalCellHeights();
				scrollTrack.refreshSliderYHeight();
				scrollTrack.updateVisibleSliderY();
				scrollTrack.showRoots();
			}
			
			private void setSliderYAction() {
				sliderY.setOnMousePressed(press->{
					drag.setStartDragParam(press);
				});
				sliderY.setOnMouseDragged(drag->{
					this.drag.mouseDragY(drag);
				});
			}
			
			class Drag {
				private void setStartDragParam(MouseEvent press) {
					SCENE_OFFSET_Y = press.getSceneY() - TARGET_SLIDER_Y;
					MAX_SLIDER_Y = SCROLL_TRACK_Y_HEIGHT - SLIDER_Y_HEIGHT;
				}
				
				private void mouseDragY(MouseEvent drag) {
					double y = drag.getSceneY() - SCENE_OFFSET_Y;
					TARGET_SLIDER_Y = Math.max(Math.min(y, MAX_SLIDER_Y), 0.0);
					sliderY.setTranslateY(TARGET_SLIDER_Y);
					scrollTrack.showRootsAfterDrag();
				}
			}
			
			class ScrollTrack{
				private void findTotalCellHeights() {
					TOTAL_CELL_HEIGHTS = roots.size() * ContainerFactory.STANDART_CONTAINER_HEIGHT;
				}
				
				private void refreshSliderYHeight() {
					double share = TOTAL_CELL_HEIGHTS == 0.0 ? 1.0 : VIEW_PORT_HEIGHT / (TOTAL_CELL_HEIGHTS + VIEW_PORT_HEIGHT*0.25);
					SHARE_VISIBLE_FILES = Math.min(share, 1.0);
					
					SLIDER_Y_HEIGHT = SHARE_VISIBLE_FILES * SCROLL_TRACK_Y_HEIGHT;
					
					setSliderYHeight();
				}
				
				private void updateVisibleSliderY() {
					if(SHARE_VISIBLE_FILES == 1.0 && scrollTrackY.isVisible()) {
						hideSliderY();
					}
					if(SHARE_VISIBLE_FILES < 1.0 && !scrollTrackY.isVisible()) {
						showSliderY();
					}
				}
				
				private void showSliderY() {
					VIEW_PORT_WIDTH = SCROLL_PANEL_MAX_WIDTH - sliderY.getMaxWidth();
					setRootViewPortWidth();
					scrollTrackY.setVisible(true);
				}
				
				private void hideSliderY() {
					VIEW_PORT_WIDTH = SCROLL_PANEL_MAX_WIDTH;
					scrollTrackY.setVisible(false);
					setRootViewPortWidth();
				}
				
				private void setRootViewPortWidth() {
					
					rootViewPort.setMinWidth(VIEW_PORT_WIDTH);
					rootViewPort.setPrefWidth(VIEW_PORT_WIDTH);
					rootViewPort.setMaxWidth(VIEW_PORT_WIDTH);
				}

				private void showRoots() {
					if (SHARE_VISIBLE_FILES == 1.0) {
						showAllRoots();
					}
					else {
						findFileIndexes();
						showFromFirstToLastFile();
					}
				}
				
				private void setSliderYHeight() {
					sliderY.setMinHeight(SLIDER_Y_HEIGHT);
					sliderY.setPrefHeight(SLIDER_Y_HEIGHT);
					sliderY.setMaxHeight(SLIDER_Y_HEIGHT);
				}
				
				private void showRootsAfterDrag() {
					findViewPortMaxY();
					findCurrentViewPortY();
					findFileIndexes();
					showFromFirstToLastFile();
				}
				
				private void findViewPortMaxY() {
					VIEW_PORT_MAX_Y = TOTAL_CELL_HEIGHTS - VIEW_PORT_HEIGHT * 0.75;
				}
				
				private void findCurrentViewPortY() {
					SHARE_VISIBLE_FILES = TARGET_SLIDER_Y / MAX_SLIDER_Y;
					CURRENT_VIEW_PORT_Y = VIEW_PORT_MAX_Y * SHARE_VISIBLE_FILES;
				}
				
				private void findFileIndexes() {
					FIRST_FILE_INDEX = (int)(CURRENT_VIEW_PORT_Y / ContainerFactory.STANDART_CONTAINER_HEIGHT);
					FILE_OFFSET_Y = CURRENT_VIEW_PORT_Y - ((double)FIRST_FILE_INDEX * ContainerFactory.STANDART_CONTAINER_HEIGHT);
					
					LAST_FILE_INDEX = (int)((CURRENT_VIEW_PORT_Y + VIEW_PORT_HEIGHT) / ContainerFactory.STANDART_CONTAINER_HEIGHT);
					LAST_FILE_INDEX = Math.min(LAST_FILE_INDEX, roots.size()-1);
				}
				
				private void showFromFirstToLastFile() { 
					rootViewPort.getChildren().clear();
					if(roots==null) {
						return;
					}
					for(int i = FIRST_FILE_INDEX; i < LAST_FILE_INDEX+1; i++) {
						DirectoryFile root = roots.get(i);
						HBox container = containerFactory.getRootContainer(root);
						container.setTranslateY(-FILE_OFFSET_Y);
						rootViewPort.getChildren().add(container);
					}
				}
				
				private void showAllRoots() {
					rootViewPort.getChildren().clear();
					if(roots==null) {
						return;
					}
					for (DirectoryFile root : roots) {
						HBox rootContainer = containerFactory.getRootContainer(root);
						rootViewPort.getChildren().add(rootContainer);
					}
				}
			}

		}

		class SelectRoot {
			private static int FIRST_FILE_INDEX = -1;
			private static int LAST_FILE_INDEX = -1;
			private void findSelectedFiles(MouseEvent click, DirectoryFile root) {
				if (click.isShiftDown()) {
					findRangeRoots(root);
				}
				else {
					selectOneAndClearOtherRoot(root);
				}
			}
			
			private void findRangeRoots(DirectoryFile root) {
				
			}
			
			private void selectOneAndClearOtherRoot(DirectoryFile selectedRoot) {
				
				for(DirectoryFile root : roots) {
					
					if(root.equals(selectedRoot)) {
						selectOneRoot(root);
					}
				}
			}

			private void selectOneRoot(DirectoryFile root) {
				HBox container = containerMap.get(root);
				if(container.getPseudoClassStates().contains(selectedContainerClass)) {
					containerFactory.setContainerStatus(container, mainContainerStatus);
				}
				else {
					containerFactory.setContainerStatus(container, selectedContainerStatus);
				}
			}
		}
		
	}
}
