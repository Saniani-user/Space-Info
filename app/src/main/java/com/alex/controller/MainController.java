package com.alex.controller;
import java.nio.file.Path;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.alex.app.Logic;
import com.alex.app.SelectionLogic;
import com.alex.app.SpringContext;
import com.alex.app.help.ChildrensExtractor;
import com.alex.app.help.DirectoryFile;
import com.alex.app.help.InfoPanelChangeListener;
import com.alex.app.help.MyFile;
import com.alex.app.help.NodeInfo;
import com.alex.app.help.RefreshViewPort;
import com.alex.controller.MainController.ContainerFactory.ChildrenContainerFactory;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class MainController {
	MainController controller = this;
	public ControllerData controllerData;
	private Logic logic;
	private Up upClass;
	FileCreator cycleFilesCreator = new FileCreator();
	
	public Object guiMon = new Object();
	
	ContainerFactory containerFactory;
	SelectionLogic selectionLogic;
	FileTree fileTree;
	ChildrenFiles childrenFiles;
	GUISizeManager sizeManager;
	ChildrensExtractor extractor;
	WindowAlert alert = new WindowAlert();
//	private List<File> roots;
//	private List<DirectoryFile> directories;

	private Image openedFolder;
	private Image closedFolder;
	
	private final String mainContainerStatus = "container";
	private final PseudoClass mainContainerClass = PseudoClass.getPseudoClass("container");
	
	private final String selectedContainerStatus = "selected-container";
	private final PseudoClass selectedContainerClass = PseudoClass.getPseudoClass("selected-container");
	
	private final String mainContainerHoverStatus = "hover";
	private final PseudoClass mainContainerHoverClass = PseudoClass.getPseudoClass("hover");
	
	private final String selectedContainerHoverStatus = "selected-hover";
	private final PseudoClass selectedContainerHoverClass = PseudoClass.getPseudoClass("selected-hover");
	
	private final String notScannedStatus = "not-scanned";
	private final PseudoClass notScannedClass = PseudoClass.getPseudoClass(notScannedStatus);
	
	private final String notScannedHoverStatus = "not-scanned-hover";
	private final PseudoClass notScannedHoverClass = PseudoClass.getPseudoClass(notScannedHoverStatus);
	
	private final String selectedNotScannedStatus = "selected-not-scanned-container";
	private final PseudoClass selectedNotScannedClass = PseudoClass.getPseudoClass(selectedNotScannedStatus);
	
	private final static String maxFileSizeMarkerStatus = "table-splitter";
	private final static PseudoClass maxFileSizeMarkerClass = PseudoClass.getPseudoClass(maxFileSizeMarkerStatus);
	
	private final String selectedNotScannedHoverStatus = "selected-not-scanned-hover";
	private final PseudoClass selectedNotScannedHoverClass = PseudoClass.getPseudoClass(selectedNotScannedHoverStatus);
//	private final PseudoClass redStatus = PseudoClass.getPseudoClass("red-indicator");
	private final PseudoClass blueStatus = PseudoClass.getPseudoClass("blue-indicator");
	private final PseudoClass fileNameStatus = PseudoClass.getPseudoClass("filename");
	private final PseudoClass spaceIndicatorStatus = PseudoClass.getPseudoClass("space-indicator");
	
	
	private final PseudoClass splitterClass = PseudoClass.getPseudoClass("splitter");
	private final PseudoClass childrenIndicator = PseudoClass.getPseudoClass("children-indicator");
	
	private final double maxSizeInFolder = 20.0;
	private static final double STANDART_SCROLL_TRACK_X_HEIGHT = 15.0;
	private static final double STANDART_SCROLL_TRACK_Y_WIDTH = 15.0;
	private int firstSelectedFileTree = -1;
	private int lastSelectedFileTree = -1;
	boolean isShiftDown = false;
	
//	private List<Thread> runnedThreads = new ArrayList<Thread>();
	private CopyOnWriteArrayList<WeakReference<Thread>>  runnedThreads = new CopyOnWriteArrayList<WeakReference<Thread>>();
	
	private volatile boolean isAppClosing = false;
	
	private boolean isSpringStarted;
	
	@FXML
	VBox root;
	
	@FXML
	Button selectDisksForScan;
	
	@FXML
	Button up;
	
	@FXML
	Button clearMemory;
	
	@FXML
	MenuItem createFiles;
	
	@FXML
	AnchorPane mainWorkflow;
	
	@FXML
	ScrollPane treeViewPort;

	@FXML
	HBox mainTree;
	
	@FXML
	private VBox bottomPanel;
	
	@FXML
	private Label backgroundInfo;
	
	@FXML
	private TextField textPath;
	
	@FXML
	private Separator separator;
	
	@FXML
	private VBox fileTreeContainer;
	
	@FXML
	private VBox filesTreeBox;

	@FXML
	private VBox treeScrollTrackY;
	
	@FXML
	private HBox treeScrollTrackX;
	
	@FXML
	private Label treeSliderX;

	@FXML
	private Label treeSliderY;
	
	@FXML
	private HBox treeSliderXContainer;
	/*
	 * элементы списка файлов
	 * 
	 */
	@FXML
	private VBox childrenContainer;	
	
	@FXML
	private ScrollPane childrenViewPort;
	
	@FXML
	private VBox childrenSuperBox;
	
	@FXML
	private VBox childrenBox;
	
	@FXML
	private HBox childrenSliderXContainer;
	
	@FXML
	private HBox childrenScrollTrackX;
	
	@FXML
	private VBox childrenScrollTrackY;
	
	@FXML
	private Label childrenSliderY;
	
	@FXML
	private Label childrenSliderX;
	
	@FXML
	private HBox childrenCube;
	
	@FXML 
	HBox treeHbox;
	
    @FXML
    private Pane infoPanel;

    @FXML
    private MenuBar mainMenu;
	
	@FXML
	private HBox selectedContainer;
	
	public Image getOpenedFolder() {
		return openedFolder;
	}
	
	Thread mainThread = Thread.currentThread();
	public void initialize() {
		childrenSliderXContainer.setManaged(false);
		treeSliderXContainer.setManaged(false);
		controllerData = new ControllerData();
		containerFactory = new ContainerFactory();
		setChildrenViewPortSizeListeners();
		hideSliders();
		deactivateButtonsOnStart();
		upClass = new Up();
		
		fileTree = new FileTree();		
		sizeManager = new GUISizeManager();
		childrenFiles = new ChildrenFiles();
		setFileBoxStyles();
		initIcons();
		
		createFiles.setOnAction(_->{
			synchronized(createFiles) {
				cycleFilesCreator.action();
			}
		});
		backgroundInfo.setText("Старт приложения");
//		waitScene();
	}
	
	
	HBox startInfoBox;
	private void setExtractor() {
		
		ChildrensExtractor.setLogic(logic);
		ChildrensExtractor.setMainController(controller);
	}
	
	private void setChildrenViewPortSizeListeners() {
		childrenViewPort.heightProperty().addListener((_, _, heightPropery)->{
			double height = heightPropery.doubleValue();
			ChildrenFiles.VIEW_PORT_HEIGHT = height;
			double childrenBoxHeight = height - ChildrenFiles.HEADER_HEIGHT;
			childrenBox.setMinHeight(childrenBoxHeight);
			childrenBox.setPrefHeight(childrenBoxHeight);
			childrenBox.setMaxHeight(childrenBoxHeight);
			childrenBox.setTranslateY(ChildrenFiles.HEADER_HEIGHT);
			if(ChildrenFiles.TableView.Header.header!=null) {
				ChildrenFiles.TableView.Header.header.setTranslateY(-childrenBoxHeight);
			}
		});
	}
	
	private void hideSliders() {
		treeScrollTrackX.setVisible(false);
		treeScrollTrackY.setVisible(false);
		
		childrenScrollTrackX.setVisible(false);
		childrenScrollTrackY.setVisible(false);
	}
	
	class Up{
		Up(){
			setOnUpAction();
		}
		
		private void setOnUpAction() {
			up.setOnMouseClicked(click->{
				if (click.getClickCount() == 1 && click.getButton() == MouseButton.PRIMARY) {
					if(ChildrenFiles.openedInChildrenFiles == null || ChildrenFiles.openedInChildrenFiles.isRoot()) {
						return;
					}
					synchronized (MainController.this) {
						clickOnUp(ChildrenFiles.openedInChildrenFiles);
					}
				}
			});
		}
		
		private void clickOnUp(DirectoryFile children) {
			if(children.getParentObj()!=null&&children.getParentObj().getThisFile().exists()) {
//				children.getChildrenDirectoryFiles().clear();
				childrenFiles.openInChildrenBox(children.getParentObj());
			}
			else {
				childrenFiles.select.firstSelectedFileObj = null;
				ChildrenFiles.Select.FIRST_SELECTED_FILE = ChildrenFiles.Select.LAST_SELECTED_FILE = -1;
				controllerData.setOpenedInChildrenFiles(null);
				logic.getshowedChildrenFiles().clear();
				childrenFiles.refreshChildrenFiles();
			}
		}
		
		private void checkUpEnabled() {
			if (controllerData.getOpenedInChildrenFiles()==null||controllerData.getOpenedInChildrenFiles().isRoot()) {
				up.setDisable(true);
			}
			else {
				up.setDisable(false);
			}
		}
		
	}
	
	public HBox addInfoPanel(String text) {
		HBox infoBox = containerFactory.getScanInfoBox(text);
		Platform.runLater(()->{
			mainWorkflow.getChildren().add(infoBox);
		});
		
		return infoBox;
	}
	
	private void setFileBoxStyles() {
		childrenViewPort.getStyleClass().add("view-port");
		treeViewPort.getStyleClass().add("view-port");
		textPath.getStyleClass().add("text-path");
		filesTreeBox.getStyleClass().add("files-tree");
		childrenSuperBox.getStyleClass().add("children-box");
		PseudoClass filesTreeStatus = PseudoClass.getPseudoClass("all-files");
		PseudoClass childrenBoxStatus = PseudoClass.getPseudoClass("all-files");
		filesTreeBox.pseudoClassStateChanged(filesTreeStatus, true);
		childrenSuperBox.pseudoClassStateChanged(childrenBoxStatus, true);
	}

	private void initIcons() {
		openedFolder = new Image(getClass().getResourceAsStream("/icons/open-folder-32.png"));
		closedFolder = new Image(getClass().getResourceAsStream("/icons/close-folder-48.png"));
	}
	
	public void initAfterSetScene() {
		startWindow();
		Scene newScene = separator.getScene();
		setSceneHeightListener(newScene);
		setSceneWidthListener(newScene);
		DirectoryFile.setMainController(controller);
		
//		logic = SpringContext.getBean(Logic.class);
		logic = new Logic();
		extractor = new ChildrensExtractor();
		setLogic();
		setExtractor();
		DirectoryFile.setLogic(logic);
		
		setSelectDisksForScanAction();
		setTreeViewPortHeightListener();
		setTreeViewPortWidthListener();
		setChildrenViewPortHeightListener();
		setChildrenViewPortWidthListener();
		setChildrenViewPortAction();
		selectionLogic = new SelectionLogic(controller, logic);
		setOnKeyAction(newScene);
		
	}
	
	private void startWindow() {
	//		root.setMouseTransparent(true);
			startInfoBox = addInfoPanel("Старт программы");
			new InfoPanelChangeListener(backgroundInfo.getScene()
					, new WeakReference<HBox>(startInfoBox)
					, (infoPanel.getMaxHeight() + mainMenu.getPrefHeight()));
			
		}

	public void initAfterStartSpring() {
		logic.initLogic();
		initLogicDirectoriesRoots();
		Platform.runLater(()->{
			backgroundInfo.setText("Готов к работе!");
			activateButtonsAfterStart();
			hideStartWindow();
		});
	}

	private void hideStartWindow() {
		mainWorkflow.getChildren().remove(startInfoBox);
		startInfoBox = null;
	}

	private void setLogic() {
		logic.setMainController(controller);
		logic.setExtractor(extractor);
		
	}

	public void initLogicDirectoriesRoots() {
		
		Platform.runLater(() -> {
			fileTree.opening.showRoots();
			setOnScrollViewPort();
			logic.startUpdateFileTree();
		});
	}
	
	private void deactivateButtonsOnStart() {
		deactivatePanelButtons();
	}
	
	private void deactivatePanelButtons() {
		selectDisksForScan.setDisable(true);
		if(controllerData.getOpenedInChildrenFiles()==null) {
			up.setDisable(true);
		}
		else {
			up.setDisable(false);
		}
	}
	
	private void activateButtonsAfterStart() {
		activatePanelButtons();
	}
	
	private void activatePanelButtons() {
		selectDisksForScan.setDisable(false);
		if(controllerData.getOpenedInChildrenFiles()==null) {
			up.setDisable(true);
		}
		else {
			up.setDisable(false);
		}
	}
	
	private void setTreeViewPortHeightListener() {
		treeViewPort.heightProperty().addListener((_, _, _)->{
			refreshTreeViewPortHeight();
			
			treeScrollTrackY.setMinHeight(treeViewPort.getMaxHeight());
			treeScrollTrackY.setPrefHeight(treeViewPort.getMaxHeight());
			treeScrollTrackY.setMaxHeight(treeViewPort.getMaxHeight());
		});
	}

	private synchronized void refreshTreeViewPortHeight() {
		fileTree.scrollPanel.refreshSliderHeight();
		fileTree.scrollPanel.updateHeightsList();
		fileTree.scrollPanel.scrollTracks.checkAndShowSliderY();
		fileTree.scrollPanel.findSliderY();
	}
	
	
	
	private void setTreeViewPortWidthListener() {
		treeViewPort.widthProperty().addListener((_, _, _)->{
			double width = treeViewPort.getMaxWidth();
			treeScrollTrackX.setMinWidth(width);
			treeScrollTrackX.setPrefWidth(width);
			treeScrollTrackX.setMaxWidth(width);
//			fileTree.scrollPanel.findMaxWidthFile();
			refreshTreeViewPortWidth();		
		});
	}

	private synchronized void refreshTreeViewPortWidth() {
//		fileTree.scrollPanel.findMaxWidthFile();
		fileTree.scrollPanel.scrollTracks.checkAndShowSliderX();
		fileTree.scrollPanel.findSliderX();
	}
	
	private void setChildrenViewPortHeightListener() {
		childrenViewPort.heightProperty().addListener((_, _, height)->{
			ChildrenFiles.VIEW_PORT_HEIGHT = childrenViewPort.getMaxHeight();
			
			childrenScrollTrackY.setMinHeight(height.doubleValue());
			childrenScrollTrackY.setMaxHeight(height.doubleValue());
			childrenScrollTrackY.setPrefHeight(height.doubleValue());
			childrenFiles.scrollPanel.scrollTrack.refreshScrollYAfterResize();
		});
	}
	
	private void setChildrenViewPortWidthListener() {
		childrenViewPort.widthProperty().addListener((_, _, _)->{
//			ChildrenFiles.VIEW_PORT_WIDTH = ChildrenFiles.SCROLL_TRACK_X_WIDTH = width.doubleValue();
//			childrenFiles.scrollPanel.scrollTrack.refreshSliderXWidth();
			ChildrenFiles.VIEW_PORT_WIDTH = ChildrenFiles.SCROLL_TRACK_X_WIDTH = childrenViewPort.getMaxWidth();
			
			childrenScrollTrackX.setMinWidth(ChildrenFiles.SCROLL_TRACK_X_WIDTH);
			childrenScrollTrackX.setPrefWidth(ChildrenFiles.SCROLL_TRACK_X_WIDTH);
			childrenScrollTrackX.setMaxWidth(ChildrenFiles.SCROLL_TRACK_X_WIDTH);
			
			childrenFiles.scrollPanel.scrollTrack.resize();
		});
	}
	
	private void setChildrenViewPortAction() {
		childrenViewPort.setOnMouseClicked(_->{
			synchronized (controller) {
				childrenFiles.select.resetSelectionProperties();
				childrenFiles.refreshChildrenFiles();
			}
			
		});
	}
	
	private void setSceneHeightListener(Scene newScene) {
		
		newScene.heightProperty().addListener((_, _, newVal)->{
			double sceneHeight = newVal.doubleValue();
			double panelsHeight = infoPanel.getHeight() + mainMenu.getHeight() + STANDART_SCROLL_TRACK_X_HEIGHT + bottomPanel.getMinHeight();
			double height = sceneHeight - panelsHeight;
			
			treeViewPort.setMinHeight(height);
			treeViewPort.setPrefHeight(height);
			treeViewPort.setMaxHeight(height);
			
			treeSliderXContainer.setLayoutY(height);
			
			childrenViewPort.setMinHeight(height);
			childrenViewPort.setPrefHeight(height);
			childrenViewPort.setMaxHeight(height);
			
			childrenSliderXContainer.setLayoutY(height + 1.0);
			
			separator.setMinHeight(height);
			separator.setPrefHeight(height);
			separator.setMaxHeight(height);
			
			double workflowHeigh = height + STANDART_SCROLL_TRACK_X_HEIGHT;
			mainWorkflow.setMinHeight(workflowHeigh);
			mainWorkflow.setPrefHeight(workflowHeigh);
			mainWorkflow.setMaxHeight(workflowHeigh);
		});
	}
	
	private void setSceneWidthListener(Scene newScene) {
		newScene.widthProperty().addListener((_, _, _)->{
			sizeManager.updateMainWidth(separator.getScene());
		});
		
	}
	
	@FXML
	void closeApp(ActionEvent event) {
		Stage primaryStage = (Stage)childrenBox.getScene().getWindow();
		primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}
	
	private void setOnKeyAction(Scene newScene) {
		newScene.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.SHIFT) {
				lastSelectedFileTree = -1;
				isShiftDown = true;
			} else if (keyEvent.getCode() == KeyCode.ENTER) {
				childrenFiles.enterAction();
			}
			else if(keyEvent.getCode() == KeyCode.DELETE) {
				if(Desktop.isDesktopSupported()&&Desktop.getDesktop().isSupported(Action.MOVE_TO_TRASH)) {
					childrenFiles.delete.deleteAction();
				}
			}
		});
		newScene.setOnKeyReleased(keyEvent->{
			if(keyEvent.getCode() == KeyCode.SHIFT) {
				isShiftDown = false;
			}
		});
	}
	
	private void setOnScrollViewPort() {		
		treeViewPort.addEventFilter(ScrollEvent.SCROLL, event->{
			event.consume();
			synchronized (MainController.this) {
				fileTree.scrollPanel.drag.mouseScrollY(event);
			}
			
		});
		
		childrenViewPort.addEventFilter(ScrollEvent.SCROLL, event->{
			event.consume();
			synchronized(MainController.this) {
				childrenFiles.scrollPanel.drag.mouseScrollY(event);
			}
		});
	}
	
	private void setSelectDisksForScanAction() {
		selectDisksForScan.setOnAction(_->{
			showScanSelectionWindow();
		});
	}
	
	private void showScanSelectionWindow() {
		URL fxmlUrl = getClass().getResource("/fxml/ScanSelectorWindow.fxml");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(fxmlUrl);
		ScanSelectorController controller;
		Parent root = null;
		try {
			root = loader.load();
			controller = loader.getController();
			controller.setMainController(this);
			controller.prepareForShow();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if(root == null) {
			System.out.println("ошибка, не могу загрузить ScanSelectorWindow.fxml");
			return;
		}
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		Stage stage = new Stage();
		stage.setScene(scene);
		controller.setOnSceneKeyPressed();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(selectDisksForScan.getScene().getWindow());
		stage.setResizable(false);
		stage.showAndWait();
	}

	public void setSize(Region region, double height) {
		ContainerFactory.setHeight(region, height);
	}
	
	public static void setWidth(Region region, double width) {
		ContainerFactory.setWidth(region, width);
	}
	
	private void setGreyLabelStyle(Label label) {
		PseudoClass headerText = PseudoClass.getPseudoClass("grey-label");
		label.pseudoClassStateChanged(headerText, true);
		label.setFont(Font.font("Arial", 12.0));
	}
	
	private synchronized void refreshFileTreeAfterShowRoots() {
		fileTree.scrollPanel.updateHeightsList();
		fileTree.scrollPanel.findMaxWidthFile();
		fileTree.scrollPanel.scrollTracks.checkAndShowSliderY();
		fileTree.scrollPanel.refreshSliderHeight();
		fileTree.scrollPanel.showFiles();
	}
	
	private synchronized void refreshFileTreeAfterExpand() {
		fileTree.scrollPanel.updateHeightsList();
		fileTree.scrollPanel.scrollTracks.checkAndShowSliderY();
		fileTree.scrollPanel.refreshSliderHeight();
		fileTree.scrollPanel.findSliderY();
	}

	private static double TREE_VIEW_PORT_WIDTH;
	private static double CURRENT_TREE_VIEW_PORT_WIDTH;
	private static double CHILDREN_VIEW_PORT_WIDTH;
	private static final double LEFT_MARGIN = 2.0;
	private static final double RIGHT_MARGIN = 2.0;
	private static double STANDART_PROPORTION = 0.3;
	private static double REMAINING_SPACE;
	private static double TREE_SCROLL_TRACK_Y_WIDTH;
	private static double CHILDREN_SCROLL_TRACK_Y_WIDTH;
	
	private static double TREE_VIEW_PORT_OFFSET;
//	private static double CHILDREN_VIEW_PORT_OFFSET;
	
	class GUISizeManager {
		
		double startWidth = 800.0;
		double startHeight = 600.0;
		
		GUISizeManager(){
			setStartSize();
			setSeparatorAction();
		}
		
		private void setStartSize() {
			double separatorWidth = separator.getPrefWidth();
			double remainingSpace = startWidth - separatorWidth - LEFT_MARGIN - RIGHT_MARGIN - 0.0;
			TREE_VIEW_PORT_WIDTH = remainingSpace * STANDART_PROPORTION;
			CHILDREN_VIEW_PORT_WIDTH = remainingSpace * (1 - STANDART_PROPORTION);
			setStartViewPortsWidth();
			setStartViewPortsHeight();
		}
		
		private void findScrollTracksYWidth() {
			TREE_SCROLL_TRACK_Y_WIDTH = treeScrollTrackY.isVisible() ? treeScrollTrackY.getPrefWidth() : 0.0;
			CHILDREN_SCROLL_TRACK_Y_WIDTH = childrenScrollTrackY.isVisible() ? childrenScrollTrackY.getPrefWidth() : 0.0;
		}
		
		private void setStartViewPortsWidth() {
			findScrollTracksYWidth();
			
			double textPathWidth = startWidth - 16.0;
			textPath.setMinWidth(textPathWidth);
			textPath.setMaxWidth(textPathWidth);
			
			double treeViewPortWidth = TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH;
			treeViewPort.setMinWidth(treeViewPortWidth);
			treeViewPort.setPrefWidth(treeViewPortWidth);
			treeViewPort.setMaxWidth(treeViewPortWidth);	
			
			treeScrollTrackX.setMinWidth(treeViewPortWidth);
			treeScrollTrackX.setPrefWidth(treeViewPortWidth);
			treeScrollTrackX.setMaxWidth(treeViewPortWidth);
			
			CURRENT_TREE_VIEW_PORT_WIDTH = TREE_VIEW_PORT_WIDTH;
			
			double childrenWidth = CHILDREN_VIEW_PORT_WIDTH - CHILDREN_SCROLL_TRACK_Y_WIDTH;
			childrenViewPort.setMinWidth(childrenWidth);
			childrenViewPort.setPrefWidth(childrenWidth);
			childrenViewPort.setMaxWidth(childrenWidth);
			
			childrenScrollTrackX.setMinWidth(childrenWidth);
			childrenScrollTrackX.setPrefWidth(childrenWidth);
			childrenScrollTrackX.setMaxWidth(childrenWidth);
			ChildrenFiles.SCROLL_TRACK_X_WIDTH = childrenWidth;
			setStartLayout();
		}
		
		private void setStartViewPortsHeight() {

			double panelsHeight = infoPanel.getHeight() + mainMenu.getHeight() + 15.0 + bottomPanel.getMinHeight();
			double height = startHeight - panelsHeight;
			treeViewPort.setMinHeight(height);
			treeViewPort.setPrefHeight(height);
			treeViewPort.setMaxHeight(height);
			
			treeSliderXContainer.setLayoutY(height);
			
			childrenViewPort.setMinHeight(height);
			childrenViewPort.setPrefHeight(height);
			childrenViewPort.setMaxHeight(height);
			
			childrenSliderXContainer.setLayoutY(height + 1.0);
			
			childrenScrollTrackY.setMinHeight(height);
			childrenScrollTrackY.setPrefHeight(height);
			childrenScrollTrackY.setMaxHeight(height);
			
			separator.setMinHeight(height);
			separator.setPrefHeight(height);
			separator.setMaxHeight(height);
			
			
		}
		
		private void setStartLayout() {
			fileTreeContainer.setLayoutX(0);
			findScrollTracksYWidth();
			
			double separatorLayoutX = TREE_VIEW_PORT_WIDTH + LEFT_MARGIN;
			separator.setLayoutX(separatorLayoutX);
			
			double childrenContainerLayout = TREE_VIEW_PORT_WIDTH + separator.getPrefWidth();
			childrenContainer.setLayoutX(childrenContainerLayout);
			
		}
		
		private void setSeparatorAction() {
			separator.setOnMousePressed(press->{
				if(press.getButton()==MouseButton.PRIMARY) {
					setStartDragParam(press);
				}
			});
			separator.setOnMouseDragged(drag->{
				if(drag.getButton()==MouseButton.PRIMARY) {
					dragSeparator(drag);
				}
			});
		}
		
		private void setStartDragParam(MouseEvent press) {
			TREE_VIEW_PORT_OFFSET =  press.getSceneX() - CURRENT_TREE_VIEW_PORT_WIDTH;
//			CHILDREN_VIEW_PORT_OFFSET = press.getSceneX() - childrenViewPort.getMaxWidth();
		}
		
		private void dragSeparator(MouseEvent drag) {
			double currentX = drag.getSceneX();
			findScrollTracksYWidth();
			
			double tempTreeWidth = currentX - TREE_VIEW_PORT_OFFSET;
			
			double maxTreeWidth = getRemainingSpace() - 50.0;
			if(maxTreeWidth < 50.0) {
				maxTreeWidth = 50.0;
			}
			TREE_VIEW_PORT_WIDTH = Math.min(maxTreeWidth, Math.max(tempTreeWidth, 50.0)) ;
			
			double tempChildrenWidth = CHILDREN_VIEW_PORT_WIDTH + (CURRENT_TREE_VIEW_PORT_WIDTH - TREE_VIEW_PORT_WIDTH);
				
			CHILDREN_VIEW_PORT_WIDTH = Math.max(tempChildrenWidth, 50.0);
			CURRENT_TREE_VIEW_PORT_WIDTH = TREE_VIEW_PORT_WIDTH;
			
			setViewPortsWidth();
		}
		
		private void refreshSize() {
			findScrollTracksYWidth();
			setViewPortsWidth();
		}
		
		private void setViewPortsWidth() {
			double textPathWidth = textPath.getScene().getWidth() - 16.0;
			textPath.setMinWidth(textPathWidth);
			textPath.setMaxWidth(textPathWidth);
			
			treeViewPort.setMinWidth(TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH);
			treeViewPort.setPrefWidth(TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH);
			treeViewPort.setMaxWidth(TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH);
			
			treeSliderXContainer.setMinWidth(TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH);
			treeSliderXContainer.setMaxWidth(TREE_VIEW_PORT_WIDTH - TREE_SCROLL_TRACK_Y_WIDTH);
			
			childrenViewPort.setMinWidth(CHILDREN_VIEW_PORT_WIDTH - LEFT_MARGIN - CHILDREN_SCROLL_TRACK_Y_WIDTH );
			childrenViewPort.setPrefWidth(CHILDREN_VIEW_PORT_WIDTH - LEFT_MARGIN - CHILDREN_SCROLL_TRACK_Y_WIDTH );
			childrenViewPort.setMaxWidth(CHILDREN_VIEW_PORT_WIDTH - LEFT_MARGIN - CHILDREN_SCROLL_TRACK_Y_WIDTH );
			
			separator.setLayoutX(TREE_VIEW_PORT_WIDTH + LEFT_MARGIN);
			
			childrenContainer.setLayoutX(TREE_VIEW_PORT_WIDTH + separator.getPrefWidth());
		}
		
//		private void updateMainWidth(Scene scene) {
//			
//			findScrollTracksYWidth();
//			REMAINING_SPACE = getRemainingSpace();
//			if(PROPORTION == -1) {
//				setSelectedProportion(STANDART_PROPORTION);
//			}
//			else {
//				setSelectedProportion(PROPORTION);
//			}
//			CURRENT_TREE_VIEW_PORT_WIDTH = TREE_VIEW_PORT_WIDTH;
//			setViewPortsWidth();
//			
//		}		
		
		private void updateMainWidth(Scene scene) {

			findScrollTracksYWidth();
			REMAINING_SPACE = getRemainingSpace();
			updateChildrenViewPortWidth();
//			setChildrenViewPortWidth();
			setViewPortsWidth();
		}
		
//		private void setChildrenViewPortWidth() {
//			double width = CHILDREN_VIEW_PORT_WIDTH - LEFT_MARGIN - CHILDREN_SCROLL_TRACK_Y_WIDTH;
//			childrenViewPort.setMinWidth(width);
//			childrenViewPort.setPrefWidth(width);
//			childrenViewPort.setMaxWidth(width);
//		}
		
//		private void updateProportion() {
//			REMAINING_SPACE = TREE_VIEW_PORT_WIDTH + CHILDREN_VIEW_PORT_WIDTH;
//			PROPORTION = TREE_VIEW_PORT_WIDTH/REMAINING_SPACE;
//		}
		
//		private void setSelectedProportion(double proportion) {
//			double maxTreeWidth = REMAINING_SPACE - 50.0;
//			if(maxTreeWidth < 50.0) {
//				maxTreeWidth = 50.0;
//			}
//			double tempTreeWidth = REMAINING_SPACE * proportion;
//			
//			TREE_VIEW_PORT_WIDTH = Math.min(maxTreeWidth, Math.max(tempTreeWidth, 50.0));
//			CHILDREN_VIEW_PORT_WIDTH = REMAINING_SPACE - TREE_VIEW_PORT_WIDTH;
//		}
		
		private void updateChildrenViewPortWidth() {
			double maxChildrenWidth = REMAINING_SPACE - 50.0;
			if(maxChildrenWidth < 50.0) {
				maxChildrenWidth = 50.0;
			}
			double tempChildrenWidth = REMAINING_SPACE - treeViewPort.getMaxWidth() - TREE_SCROLL_TRACK_Y_WIDTH;
			CHILDREN_VIEW_PORT_WIDTH = Math.min(maxChildrenWidth, Math.max(tempChildrenWidth, 50.0));
			
			updateFileTreeWidth(tempChildrenWidth);
		}
		
		private void updateFileTreeWidth(double tempChildrenWidth) {
			if (tempChildrenWidth < 50.0) {
				double tempTreeWidth = REMAINING_SPACE - 50.0;
				TREE_VIEW_PORT_WIDTH = Math.max(tempTreeWidth, 50.0);
				CURRENT_TREE_VIEW_PORT_WIDTH = TREE_VIEW_PORT_WIDTH;
			}
			
		}
		
		private double getRemainingSpace() {
			double sceneWidth = separator.getScene().getWidth();
			double separatorWidth = separator.getPrefWidth();
			
			return sceneWidth - separatorWidth - LEFT_MARGIN - RIGHT_MARGIN - 0.0;
		}
	}
	
	class ContainerFactory{
		
		ChildrenContainerFactory childrenFactory;
		
		ContainerFactory(){
			childrenFactory = new ChildrenContainerFactory();
		}
		
		private HBox getTreeContainer(DirectoryFile file) {
			if(file.isRoot()) {
				return containerFactory.getRootContainer(file);
			}
			else {
				return containerFactory.getFileContainer(file);
			}
		}
		
		@SuppressWarnings("unlikely-arg-type")
		private HBox getRootContainer(DirectoryFile root) {
			Label rootName = new Label();
			rootName.setFont(Font.font("Arial", 13.0));
			HBox rootContainer = buildRootContainer(root, rootName);
			rootName.setText(root.getThisFile().toString());
			setOnContainerAction(root, rootContainer);
			if(selectionLogic.getSelectedFiles().contains(root)) {
				fileTree.select.changeVisualSelectedFile(root, rootContainer);
			}
			else {
				fileTree.select.changeVisualUsualFile(root, rootContainer);
			}
			return rootContainer;
		}

		private HBox buildRootContainer(DirectoryFile root, Label label) {
			HBox mainContainer = new HBox(label);
			setHeight(mainContainer, 25);
			mainContainer.setAlignment(Pos.CENTER);
			mainContainer.getStyleClass().add("my-file-tree");
//			mainContainer.setPrefHeight(25);
			return mainContainer;
		}
		
//		private void setRootContainerStatus(DirectoryFile root, HBox mainContainer) {
//			if (root.isScanned()) {
//				changeContainerStatus(mainContainer, mainContainerStatus);
//				setHoverStatus(mainContainer, mainContainerHoverStatus, mainContainerStatus);
//			}
//			else {
//				changeContainerStatus(mainContainer, notScannedStatus);
//				setHoverStatus(mainContainer, notScannedHoverStatus, notScannedStatus);
//			}
//		}
		
		
		private void setOnContainerAction(DirectoryFile file, HBox container) {
			if (file.isRegularFile()) {
				setOnFileAction(file, container);
			} 
			else if(file.isRoot()&&!file.isScanned()){
				setOnNotScannedRootAction(file, container);
			}
			else {
				setOnParentDirectoryAction(file, container);
			}
		}
		
		private void setOnNotScannedRootAction(DirectoryFile parent, HBox container) {
			container.setOnMouseClicked(event -> {
				
				Runnable task = ()->{
					notScannedRootAction(parent, event);
				};
				
				logic.updateFileTree.addTask(task);
				
			});
		}
		boolean calc = false;
		private void notScannedRootAction(DirectoryFile parent, MouseEvent event) {
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {

				synchronized (MainController.this) {
					fileTree.select.selectFile(parent);
					childrenFiles.scrollPanel.clearChildrenViewPort();
					ChildrenFiles.Select.FIRST_SELECTED_FILE = ChildrenFiles.Select.LAST_SELECTED_FILE = -1;
					controllerData.setOpenedInChildrenFiles(null);
					
				}
			}
		}
		
		

		private void setOnParentDirectoryAction(DirectoryFile parent, HBox container) {
			
			container.setOnMouseClicked(event -> {
				
				Runnable task = ()->{
					parentDirectoryAction(parent, event, container);
				};
				logic.updateFileTree.addTask(task);
			});
		}

		private void parentDirectoryAction(DirectoryFile parent, MouseEvent event, HBox container) {
			if (!parent.isOpen() && event.getButton() == MouseButton.PRIMARY && event.getClickCount()==2) {
				
				synchronized (MainController.this) {
					Platform.runLater(()->{
						backgroundInfo.setText("откртие папки");
					});
					fileTree.opening.expand(parent, container);
					Platform.runLater(()->{
						backgroundInfo.setText("");
					});
				}
				
			}
			else if (parent.isOpen() && event.getButton() == MouseButton.PRIMARY && event.getClickCount()==2) {
				synchronized (MainController.this) {
					fileTree.closing.collapse(parent);
				}					
			}
			else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
				
				synchronized (MainController.this) {
					if(ChildrenFiles.openedInChildrenFiles==null
							||!ChildrenFiles.openedInChildrenFiles.equals(parent)) {
						childrenFiles.showChildren(parent, RefreshViewPort.ALL);
					}
					Platform.runLater(()->{
						fileTree.select.selectFile(parent);
					});
					
				}
			}
		}
		
		private void setOnFileAction(DirectoryFile parent, HBox container) {
			container.setOnMouseClicked(event->{
				Runnable task = () -> {
					if (event.getButton() == MouseButton.PRIMARY) {
						fileTree.select.selectFile(parent);
					}
				};
				logic.updateFileTree.addTask(task);
			});
		}

		
		private void changeContainerStatus(HBox container, String status) {
			container.pseudoClassStateChanged(mainContainerClass, mainContainerStatus.equals(status));
			container.pseudoClassStateChanged(mainContainerHoverClass, mainContainerHoverStatus.equals(status));
			container.pseudoClassStateChanged(selectedContainerClass, selectedContainerStatus.equals(status));
			container.pseudoClassStateChanged(selectedContainerHoverClass, selectedContainerHoverStatus.equals(status));
			
			container.pseudoClassStateChanged(notScannedClass, notScannedStatus.equals(status));
			container.pseudoClassStateChanged(notScannedHoverClass, notScannedHoverStatus.equals(status));
			container.pseudoClassStateChanged(selectedNotScannedClass, selectedNotScannedStatus.equals(status));
			container.pseudoClassStateChanged(selectedNotScannedHoverClass, selectedNotScannedHoverStatus.equals(status));
			
		}
		
		@SuppressWarnings("unlikely-arg-type")
		private HBox getFileContainer(DirectoryFile df) {

			Label red = getRedLabel(df);
			Label blue = getBlueLabel();

			StackPane spaceIndicatorContainer = getSpaceIndicatorContainer(red, blue);
//			df.setSpaceIndicator(red);
			setIndicatorMargin(df, spaceIndicatorContainer);

			Label fileName = getNameLabel(df);
			
			HBox iconContainer = getIconContainer(df);
			
			HBox endSpace = getEndSpace(3.0, 25.0);

			HBox fileContainer = getFileContainer(spaceIndicatorContainer, iconContainer, fileName, endSpace);
			
			if(selectionLogic.getSelectedFiles().contains(df)) {
				fileTree.select.changeVisualSelectedFile(df, fileContainer);
			}
			else {
				fileTree.select.changeVisualUsualFile(df, fileContainer);
			}
//			fileContainer.setMinWidth(Region.USE_PREF_SIZE);
//			fileContainer.layout();
			
//			df.setContainer(fileContainer);
			setOnContainerAction(df, fileContainer);
			return fileContainer;

		}

		private Label getBlueLabel() {
			Label blue = new Label();
			blue.pseudoClassStateChanged(blueStatus, true);
			/*
			 * maxSizeInFolder = 20;
			 */
			setSize(blue, 5.0, maxSizeInFolder);

			return blue;
		}
		
		private Label getRedLabel(DirectoryFile df) {
			Label indicator = new Label();
			indicator.pseudoClassStateChanged(PseudoClass.getPseudoClass("red-indicator"), true);
			double height = maxSizeInFolder * df.getShare();
			indicator.setPrefSize(5.0, height);
			indicator.setMinSize(5.0, height);
			indicator.setMaxSize(5.0, height);
			return indicator;
		}

		private StackPane getSpaceIndicatorContainer(Label red, Label blue) {

			StackPane spaceIndicatorContainer = new StackPane(blue, red);
			spaceIndicatorContainer.getStyleClass().add("Stack-Pane");
			spaceIndicatorContainer.pseudoClassStateChanged(spaceIndicatorStatus, true);
			spaceIndicatorContainer.setAlignment(Pos.BOTTOM_CENTER);
			setSize(spaceIndicatorContainer, 35.0, 25.0);
			Insets indicatorInsets = new Insets(2.5, 15.0, 2.5, 15.0);
			StackPane.setMargin(red, indicatorInsets);
			StackPane.setMargin(blue, indicatorInsets);

			return spaceIndicatorContainer;
		}

		private void setIndicatorMargin(DirectoryFile df, StackPane spaceIndicatorContainer) {
			String parentFile = df.getThisFile().toString();
			double leftMargin = logic.getLeftMargin(parentFile);
			HBox.setMargin(spaceIndicatorContainer, new Insets(0.0, 0.0, 0.0, leftMargin));			
		}

		private HBox getSplitterContainer(Pos pos, double width, double height) {
			Label splitter = new Label();
			setSize(splitter, 1.5, 18);
			splitter.pseudoClassStateChanged(splitterClass, true);

			HBox splitterContainer = new HBox(splitter);
			setSize(splitterContainer, width, height);
			splitterContainer.setAlignment(pos);

			return splitterContainer;
		}
		
		private Label getNameLabel(DirectoryFile df) {
			Label fileName = new Label();
			fileName.setText(df.getThisFile().getName());
			fileName.pseudoClassStateChanged(fileNameStatus, true);
			fileName.setMinWidth(Region.USE_PREF_SIZE);
			fileName.setFont(Font.font("Arial", 12.0));
			return fileName;
		}
		private HBox getIconContainer(DirectoryFile df) {
			ImageView icon = new ImageView();
			icon.setFitHeight(18);
			icon.setFitWidth(18);
			icon.setPreserveRatio(true);

			setChildrenImage(df, icon, false);

//			df.setIcon(icon);

			HBox iconContainer = new HBox(icon);
			setSize(iconContainer, 25, 25);
			iconContainer.setAlignment(Pos.CENTER);

			return iconContainer;
		}

		private HBox getFileContainer(StackPane spaceIndicatorContainer, HBox iconContainer,
				Label folderName, HBox endSpace) {
			HBox firstSplitter = getSplitterContainer(Pos.CENTER_LEFT, 6.0, 25.0);
//			HBox secondSplitter = getSplitterContainer(Pos.CENTER, 6.0, 25.0);
			
			HBox fileContainer = new HBox(spaceIndicatorContainer, firstSplitter,
					iconContainer, folderName,endSpace);

			fileContainer.getStyleClass().add("my-file-tree");
			fileContainer.pseudoClassStateChanged(mainContainerClass, true);
			fileContainer.setAlignment(Pos.CENTER_LEFT);
			setHeight(fileContainer, 25.0);

			return fileContainer;
		}
		
		private HBox getEndSpace(double width, double height) {
			HBox endSpace = new HBox();
			setSize(endSpace, width, height);
			return endSpace;
		}
		
		private void setChildrenImage(DirectoryFile df, ImageView icon, boolean isChildren) {
			if(isChildren) {
				if(df.isDirectory()) {
					icon.setImage(closedFolder);
				}
				else {
					//TODO добавить иконку файла
				}
				return;
			}
			
			if(df.isDirectory()&&!df.isOpen()) {
				icon.setImage(closedFolder);
			}
			else if (df.isDirectory()&&df.isOpen()) {
				icon.setImage(openedFolder);
			}
			else {
				//TODO Добавить иконку файла
			}
		}
		
		
		
		
		
		class ChildrenContainerFactory{
			
			private static final String standartStatus = "standart-container";
			private static final PseudoClass standartClass = PseudoClass.getPseudoClass(standartStatus);
			
			private static final String selectedStatus = "selected-container";
			private static final PseudoClass selectedClass = PseudoClass.getPseudoClass(selectedStatus);
			
			private HBox getChildrenContainer(DirectoryFile df) {
				HBox maxFileSizeMarker = getMaxFileSizeMarker(ChildrenFiles.STANDART_HEIGHT);
				maxFileSizeMarker.setTranslateX(-0.5);
//				e;
				HBox container = new HBox(getChildrenFileNameContainer(df), getChildrenSizeContainer(df), maxFileSizeMarker, getChildrenIndicator(df));
				setChildrenContinerAction(df, container);
				
				container.getStyleClass().add("children-files");
				setChildrenContainerStatus(container, standartStatus);
				
				setSize(container, ChildrenFiles.FILES_WIDTH, ChildrenFiles.STANDART_HEIGHT);
				
				
				return container;
			}
			
			private static void setChildrenContainerStatus(HBox container, String status) {
				
				container.pseudoClassStateChanged(standartClass, standartStatus.equals(status));
				container.pseudoClassStateChanged(selectedClass, selectedStatus.equals(status));
			}
			
			private HBox getChildrenFileNameContainer(DirectoryFile df) {
				/*
				 * ширина: иконка 25.0, пробел 15.0, ширина имени
				 */
				Label fileName = new Label();
				fileName.setFont(Font.font("Arial", 12.0));
				fileName.setText(df.getThisFile().getName());
				
				HBox childrenFileNameContainer = new HBox(getChildrenIcon(df), getSpaceBox(5.0, ChildrenFiles.STANDART_HEIGHT), fileName);
				childrenFileNameContainer.setAlignment(Pos.CENTER_LEFT);
				double width = ChildrenFiles.TableView.TABLE_VIEW_NAME_WIDTH + ChildrenFiles.TableView.TABLE_VIEW_SPLITTER_WIDTH;
				setSize(childrenFileNameContainer, width, ChildrenFiles.STANDART_HEIGHT);
				
				return childrenFileNameContainer;
			}
			
			private HBox getChildrenIcon(DirectoryFile df) {
				ImageView icon = new ImageView();
				icon.setFitHeight(18.0);
				icon.setFitWidth(18.0);
				setChildrenImage(df, icon, true);
				HBox iconContainer = new HBox(icon);
				setSize(iconContainer, 25.0, ChildrenFiles.STANDART_HEIGHT);
				iconContainer.setAlignment(Pos.CENTER);
				return iconContainer;
			}
			
			private HBox getChildrenIndicator(DirectoryFile df) {
				HBox indicator = new HBox();
				indicator.getStyleClass().add("indicator-hbox");
				indicator.pseudoClassStateChanged(childrenIndicator, true);
				
				setChildrenIndicatorWidth(df, indicator);
//				df.setChildrenIndicator(indicator);
				indicator.setTranslateX(-ChildrenFiles.FILES_WIDTH);
				return indicator;
			}
			
			private void setChildrenIndicatorWidth(DirectoryFile df, HBox indicator) {
				setHeight(indicator, ChildrenFiles.STANDART_HEIGHT);
				
				double share = df.getShare();
				var size = (ChildrenFiles.FILES_WIDTH-ChildrenFiles.TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH)*share;
				indicator.setMinWidth(size);
				indicator.setMaxWidth(size);
			}
			
			private HBox getChildrenSizeContainer(DirectoryFile df) {
				Label sizeLabel = getSizeLabel(df);
				HBox childrenSizeContainer = new HBox(getSpaceBox(10.0, 25.0), sizeLabel);
				childrenSizeContainer.setAlignment(Pos.CENTER_LEFT);
				setSize(childrenSizeContainer, ChildrenFiles.TableView.TABLE_VIEW_SIZE_WIDTH, ChildrenFiles.STANDART_HEIGHT);
				return childrenSizeContainer;
			}
			
			private Label getSizeLabel(DirectoryFile df) {
				Label sizeLabel = new Label();
				setGreyLabelStyle(sizeLabel);
				StringBuilder sizeText = new StringBuilder();
				double size = df.getFileSize()/1024.0;
				if(size == 0.0) {
					sizeText.append("0").append(" кб");
				}
				else if(size<0.1) {
					sizeText.append("менее ").append(0.1).append(" кб");
				}
				else {
					
					if (size>=524288) {
						String formattedSize = getGygabyteTextSize(size);
						sizeText.append(formattedSize).append(" гб");
					}
					else if(size>=512) {
						size = Math.round((size/1024.0) * 10.0) / 10.0;
//						String formattedSize = formatToStringSize(size);
						sizeText.append(size).append(" мб");
					}
					else {
						size = Math.round(size * 10.0) / 10.0;
						sizeText.append(size).append(" кб");
					}
				}			
				sizeLabel.setText(sizeText.toString());
				setSizeSizeLabel(sizeLabel);
				return sizeLabel;
			}
			
			private String getGygabyteTextSize(double size) {
				size = Math.round((size/(1024*1024))*10.0)/10.0;
				
				return formatToStringSize(size);
			}
			
			private String formatToStringSize(double size) {
				if(size<1000) {
					return String.valueOf(size);
				}
				String stringSize = String.valueOf(size);
				int pointIndex = stringSize.indexOf(".");
				String point = stringSize.substring(pointIndex);
				stringSize = stringSize.substring(0, pointIndex);
				int start = stringSize.length()%3;
				String formattedSize = getSizePrefix(stringSize, start);
				
				int iterations = stringSize.length()/3;
				for (int i = 0;  i<iterations; i++) {
					String threeNums = stringSize.substring(start, start+3);
					formattedSize += " " + threeNums;
					start +=3;
				}
				return formattedSize + point;
			}
			
			private String getSizePrefix(String stringSize, int start) {
				if(start == 0) {
					return "";
				}
				else {
					return stringSize.substring(0, start);
				}
			}
			
			private void setSizeSizeLabel(Label sizeLabel) {
				sizeLabel.setMinWidth(ChildrenFiles.TableView.TABLE_VIEW_SIZE_WIDTH);	
				sizeLabel.setMaxWidth(ChildrenFiles.TableView.TABLE_VIEW_SIZE_WIDTH);
			}
			
			private static HBox getMaxFileSizeMarker(double height) {
				HBox marker = new HBox();
				setSize(marker, ChildrenFiles.TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH, height);
				marker.getStyleClass().add("table-hbox");
				marker.pseudoClassStateChanged(maxFileSizeMarkerClass, true);
				return marker;
			}
			
			private void setChildrenContinerAction(DirectoryFile df, HBox container) {
				container.setOnMouseClicked(click->{
					if(click.getClickCount()==1&&click.getButton()==MouseButton.PRIMARY) {
						synchronized (controller) {
							childrenFiles.select.findFilesToSelect(df);
							childrenFiles.scrollPanel.findFilesToShow();
							click.consume();
						}
						
					}
					else if (click.getClickCount() == 2 && click.getButton()==MouseButton.PRIMARY) {
						synchronized(MainController.this) {
							childrenFiles.openInChildrenBox(df);
							click.consume();
						}
					}
				});
			}
		}
		
		private HBox getSpaceBox(double width, double height) {
			HBox space = new HBox();
			space.setMinWidth(width);
			space.setMaxWidth(width);
			space.setMinHeight(height);
			space.setMaxHeight(height);
			return space;
		}

		private void setHoverStatus(HBox container, String entered, String exited) {
			container.setOnMouseEntered(_ -> {
				changeContainerStatus(container, entered);
				
			});
			container.setOnMouseExited(_ -> {
				changeContainerStatus(container, exited);
			});
		}

		private static void setSize(Region region, double width, double height) {
			region.setMinSize(width, height);
			region.setPrefSize(width, height);
			region.setMaxSize(width, height);
		}

		private static void setHeight(Region region, double height) {
			region.setMinHeight(height);
			region.setPrefHeight(height);
			region.setMaxHeight(height);
			
		}
		
		private static void setWidth(Region region, double width) {
			region.setMinWidth(width);
			region.setPrefWidth(width);
			region.setMaxWidth(width);
		}
		
		private HBox getScanInfoBox(String text) {
			double width = 170.0;
			double height = 100.0;			
			HBox scanInfoBox = new HBox(getInnerScanInfoBox(text, width-20.0, height - 20.0));
			scanInfoBox.setAlignment(Pos.CENTER);
			setSize(scanInfoBox, width, height);
			setTranslateScanInfoBox(width, height, scanInfoBox);
			setStyleScanInfoBox(scanInfoBox);
			return scanInfoBox;
		}

		private void setTranslateScanInfoBox(double width, double height, HBox scanInfoBox) {
			double delta = infoPanel.getMaxHeight() + mainMenu.getPrefHeight();
			double windowWidth = separator.getScene().getWidth();
			double windowHeight = separator.getScene().getHeight() - delta;
			double translateX = (windowWidth - width)/2.0;
			double translateY = (windowHeight - height - delta)/2.0;
			scanInfoBox.setTranslateX(translateX);
			scanInfoBox.setTranslateY(translateY);
			new InfoPanelChangeListener(separator.getScene(), new WeakReference<HBox>(scanInfoBox), delta);
		}
		
		private HBox getInnerScanInfoBox(String text, double width, double height) {
			Label scanText = new Label();
			scanText.setFont(Font.font("Arial", 12.0));
			scanText.setText(text);
			HBox innerBox = new HBox(scanText);
			innerBox.setAlignment(Pos.CENTER);
			setSize(innerBox, width, height);
			setStyleInnerScanInfoBox(innerBox);
			return innerBox;
		}
		
		private void setStyleScanInfoBox(HBox box) {
			box.getStyleClass().add("scan-info-panel");
			String status = "main-style";
			PseudoClass infoBoxClass = PseudoClass.getPseudoClass(status);
			box.pseudoClassStateChanged(infoBoxClass, true);
		}
		
		private void setStyleInnerScanInfoBox(HBox box) {
			box.getStyleClass().add("scan-info-panel");
			String status = "inner-style";
			PseudoClass infoBoxClass = PseudoClass.getPseudoClass(status);
			box.pseudoClassStateChanged(infoBoxClass, true);
		}
		
		private HBox getWhiteFiller(double height) {
			HBox filler = new HBox();
			double width = ChildrenFiles.TableView.TABLE_VIEW_NAME_WIDTH +
					ChildrenFiles.TableView.TABLE_VIEW_SPLITTER_WIDTH + 
					ChildrenFiles.TableView.TABLE_VIEW_SIZE_WIDTH;
			setSize(filler, width, height);
			HBox maxFileSizeMarker = ChildrenContainerFactory.getMaxFileSizeMarker(height);
			HBox whiteFiller = new HBox(filler, maxFileSizeMarker);
			maxFileSizeMarker.setTranslateX(-0.5);
			whiteFiller.getStyleClass().add("children-files");
			ChildrenContainerFactory.setChildrenContainerStatus(whiteFiller, ChildrenContainerFactory.standartStatus);
			setSize(whiteFiller, ChildrenFiles.FILES_WIDTH, height);
			return whiteFiller;
		}
	}

	public class FileTree {
		Opening opening = new Opening();
		Closing closing = new Closing();
		
		ScrollPanel scrollPanel = new ScrollPanel();
		Select select = new Select();
		Delete delete = new Delete();
		
		NodeInfo opened;
		
		public void refreshIndicators(List<DirectoryFile> openedFiles) {
			synchronized (controller) {
//				List<DirectoryFile> openedFiles = logic.getShowedTree();
				
				for (int i = 0; i<openedFiles.size(); i++) {
					DirectoryFile opened = openedFiles.get(i);
					if(opened.isRoot()) {
						continue;
					}
					if (filesTreeBox.getChildren().get(i) instanceof HBox container) {
						if (container.getChildren().getFirst() instanceof StackPane stack) {
							Label redLabel = (Label) stack.getChildren().getLast();
							double share = opened.getShare();
							redLabel.setMinHeight(share * maxSizeInFolder);
							redLabel.setPrefHeight(share * maxSizeInFolder);
							redLabel.setMaxHeight(share * maxSizeInFolder);

						}
					}
					
					
				}
			}
		}
				
		public void refreshFileTreeBox() {
			synchronized (controller) {
				fileTree.scrollPanel.updateHeightsList();
				fileTree.scrollPanel.scrollTracks.checkAndShowSliderY();
				fileTree.scrollPanel.refreshSliderHeight();
				fileTree.scrollPanel.findSliderY();
			}
		}
		
//		public void addNewFilesInGUI(List<MyFile> filesToAddInGUI) {
//			logic.addInShowedTree(null, null, filesToAddInGUI);
//			Platform.runLater(()->{
//				refreshFileTreeBox();
//			});
//		}
		
		public void updateAfterScan() {
			synchronized (controller) {
				childrenFiles.scrollPanel.clearChildrenViewPort();

				fileTree.opening.showRoots();
			}
		}
		
		public void closeFolder(DirectoryFile parent) {
			closing.collapse(parent);
		}

		class Opening {

			private void showRoots() {
				
					filesTreeBox.getChildren().clear();
				
				logic.getShowedFiles().clear();
				List<DirectoryFile> roots = logic.mySQL.getRoots();
				
				if (roots == null) {
					// TODO добавить alert диски не найдены
					System.out.println("err in showRoots() root is null");
					return;
				}

				for (DirectoryFile root : roots) {
					containerFactory.getRootContainer(root);
					logic.getShowedFiles().add(new NodeInfo(root));
				}
				refreshFileTreeAfterShowRoots();
			}

			
			//синхронизирован
			private void expand(DirectoryFile parent, HBox container) {
				synchronized (controller) {
					List<DirectoryFile> childrenDirectoryFiles = 
							extractor.getNewChildrenDirectoryFiles(parent, RefreshViewPort.CHILDREN_FILES).getfileTree();

					
					addShowedFiles(parent, childrenDirectoryFiles);
					parent.setOpen(true);
					logic.mySQL.saveEntity(parent);
					refreshFileTreeAfterExpand();
				}
			}

			
			@SuppressWarnings("unlikely-arg-type")
			private void addShowedFiles(DirectoryFile parent, List<DirectoryFile> childrenDirectoryFiles) {
				int index = -1;
				
				index = logic.getShowedFiles().indexOf(parent);
				if (index < 0) {
					return;
				}
				for (DirectoryFile children : childrenDirectoryFiles) {
					children.setOpen(false);
					containerFactory.getFileContainer(children);
				}
				List<NodeInfo> ni = childrenDirectoryFiles.stream().map(DirectoryFile::getNodeInfo).collect(Collectors.toList());
				
				logic.getShowedFiles().addAll(++index, ni);
			}
		}

		class Closing {

			private void collapse(DirectoryFile parent) {
				Runnable task = () -> {
					List<NodeInfo> remove = new ArrayList<NodeInfo>();
					List<DirectoryFile> closed = new ArrayList<DirectoryFile>();
					logic.mySQL.getAllOpenedChildrens(parent.getId(), remove, closed);
					
					LinkedHashSet<NodeInfo> showedFiles;
					synchronized (controller) {
						showedFiles = new LinkedHashSet<NodeInfo>(logic.getShowedFiles());
					}
					showedFiles.removeAll(remove);
					synchronized (controller) {
						logic.setShowedTree(showedFiles);
					}
					parent.setOpen(false);
					closed.add(parent);
					logic.mySQL.saveEntity(closed);
					refreshFileTreeAfterExpand();
				};
				logic.updateFileTree.addTask(task);
				
			}
			
		}
		
		class Select{
			
			private void selectFile(DirectoryFile file) {
//				selectionLogic.findSelectedFileIndex(new NodeInfo(file));
				selectOneFile(file);
				
//				Platform.runLater(()->{
//					refreshFileTreeBox();
//				});
				
				
//				if (isShiftDown) {
//					if(lastSelectedFileTree == -1) {
//						synchronized(controller) {
//							selectOneFile(file);
//						}
//						
//					}
//					else {
//						checkFirstAndLastFiles();
//					}
//				}
//				else {
//					synchronized(controller) {
//						selectOneFile(file);
//					}
//					
//				}
			}
			//синхронизирован
			@SuppressWarnings("unlikely-arg-type")
			private void selectOneFile(DirectoryFile file) {
				selectionLogic.getSelectedFiles().clear();
				selectionLogic.getSelectedFiles().add(new NodeInfo(file));
//				int index = firstSelectedFileTree - ScrollPanel.FIRST_FILE_INDEX;
				List<DirectoryFile> showedDirectoryFiles = logic.getShowedTree();
				
				for (int i = 0; i < filesTreeBox.getChildren().size(); i++) {
					
					if (filesTreeBox.getChildren().get(i) instanceof HBox container) {
						if(selectionLogic.getSelectedFiles().contains(showedDirectoryFiles.get(i))) {
							changeVisualSelectedFile(showedDirectoryFiles.get(i), container);
						}
						else {
							changeVisualUsualFile(showedDirectoryFiles.get(i), container);
						}
					}
				}
			}
			
			private void changeVisualSelectedFile(DirectoryFile selectedFile, HBox container) {
				if (selectedFile.isRoot() && !selectedFile.isScanned()) {
					containerFactory.changeContainerStatus(container, 
							selectedNotScannedHoverStatus);
					
					containerFactory.setHoverStatus(container, 
							selectedNotScannedHoverStatus,
							selectedNotScannedStatus);
				}
				
				else {
					containerFactory.changeContainerStatus(container, 
							selectedContainerHoverStatus);
					
					containerFactory.setHoverStatus(container, 
							selectedContainerHoverStatus, 
							selectedContainerStatus);
				}
			}
			
			private void changeVisualUsualFile(DirectoryFile usualFile, HBox container) {
				if(usualFile.isRoot()&&!usualFile.isScanned()) {
					containerFactory.changeContainerStatus(container, 
							notScannedStatus);
					
					containerFactory.setHoverStatus(container, 
							notScannedHoverStatus, 
							notScannedStatus);
				}
				else {
					containerFactory.changeContainerStatus(container, 
							mainContainerStatus);
					
					containerFactory.setHoverStatus(container, 
							mainContainerHoverStatus, mainContainerStatus);
					
				}
			}
			
			
//			private void checkFirstAndLastFiles() {
//				synchronized (controller) {
//					if (firstSelectedFileTree <= lastSelectedFileTree) {
//						selectMultipleFiles(firstSelectedFileTree, lastSelectedFileTree);
//					} else {
//						selectMultipleFiles(lastSelectedFileTree, firstSelectedFileTree);
//					}
//				}
//				
//			}
//			//синхронизирован
//			private void selectMultipleFiles(int first, int last) {
//				selectionLogic.getSelectedFiles().clear();
//				if(first == last) {
//					DirectoryFile file = logic.getShowedFiles().get(first);
//					selectionLogic.getSelectedFiles().add(file);
//					selectOneFile(file);
//					return;
//				}
//				setStandartStatus();
//				for (int i = first; i < last + 1; i++) {
//					DirectoryFile file = logic.getShowedFiles().get(i);
//					selectionLogic.getSelectedFiles().add(file);
//					containerFactory.changeContainerStatus(file.getContainer(), selectedContainerStatus);
//					containerFactory.setSelectedStandartlHoverStatus(file.getContainer());
//
//				}
//			}
//			
//			private void setStandartStatus() {
//				for(Node node : filesTreeBox.getChildren()) {
//					if(node instanceof HBox container) {
//						containerFactory.changeContainerStatus(container, mainContainerStatus);
//					}
//				}
//			}
			
		}
		
		class Delete {

			private void delete(File file) {
				if (file.isDirectory()) {

				}

				else {

				}
			}

			private void deleteFolder(DirectoryFile directory) {

			}

			private void deleteFile(DirectoryFile file) {

			}

		}

		public class ScrollPanel {

			Drag drag;
			ScrollTrack scrollTracks;

			private List<Double> cellHeights = new ArrayList<Double>();
//			private final static Double ROOT_HEIGHT = 25.0;
			private final static Double STANDART_HEIGHT = 25.0;
			
			private static double SCENE_OFFSET_Y = 0.0;
			private static double SLIDER_Y_HEIGHT = 0.0;
			private static double TARGET_SLIDER_Y = 0.0;
			private static double MAX_SLIDER_Y = 0.0;
			
			private static double SCROLL_TRACK_Y_HEIGHT = 0.0;
			private static double SHARE_VISIBLE_FILES_Y = 0.0;
			
//			private static double CURRENT_MOUSE_X;
//			private static double MOUSE_INTERVAL_X = 0.0;
			
			private static double SCENE_OFFSET_X = 0.0;
			private static double SLIDER_X_WIDTH = 20.0;
			private static double TARGET_SLIDER_X = 0.0;
			private static double MAX_SLIDER_X = 0.0;
			
			private static double SCROLL_TRACK_X_WIDTH = 0.0;
			private static double SHARE_VISIBLE_FILES_X = 0.0;

			private static double VIEW_PORT_HEIGHT;
			private static double VIEW_PORT_WIDTH;
			private static double CURRENT_VIEW_PORT_Y = 0.0;

			

			ScrollPanel() {

				drag = new Drag();
				scrollTracks = new ScrollTrack();
				setSliderYAction();
				setSliderXAction();
				setScrollTrackYAction();
				setScrollTrackXAction();
				
			}
			
			private void setSliderYAction() {
				treeSliderY.setOnMousePressed(press -> {
					synchronized(controller) {
						drag.setMouseStartDragParam(press);
					}
				});
				treeSliderY.setOnMouseReleased(_ -> {
					
				});
				treeSliderY.setOnMouseDragged(mouseDrag -> {
					Runnable task = () -> {
						synchronized (controller) {
							drag.mouseDragY(mouseDrag);
						}
					};
					logic.updateFileTree.addTask(task);
					
				});
				treeSliderY.setOnMouseClicked(event->{
					event.consume();
				});
			}
			
			private void setSliderXAction() {

				treeSliderX.setOnMousePressed(press -> {
					
					drag.setMouseStartDragXParam(press);
				});
				treeSliderX.setOnMouseReleased(_ -> {
					
				});
				treeSliderX.setOnMouseDragged(mouseDrag -> {
					synchronized (MainController.this) {
						drag.mouseDragX(mouseDrag);
					}
					
				});
				treeSliderX.setOnMouseClicked(event->{
					event.consume();
				});
			
			}
			
			private void setScrollTrackYAction(){
				treeScrollTrackY.setOnMouseClicked(event->{
					synchronized (MainController.this) {
						scrollTracks.clickOnScrollTrackY(event);
						showFiles();
					}
					
				});
			}
			
			private void setScrollTrackXAction() {
				treeScrollTrackX.setOnMouseClicked(event->{
					synchronized (MainController.this) {
						scrollTracks.clickOnScrollTrackX(event);
						findOffsetXAfterDrag();
						setOffsetX();
					}
				});
				
			}

			class Drag {
				private void setMouseStartDragParam(MouseEvent press) {
					SCENE_OFFSET_Y = press.getSceneY() - TARGET_SLIDER_Y;
					SLIDER_Y_HEIGHT = treeSliderY.getHeight();
					SCROLL_TRACK_Y_HEIGHT = treeViewPort.getHeight();
					MAX_SLIDER_Y = SCROLL_TRACK_Y_HEIGHT - SLIDER_Y_HEIGHT;
					
				}

				private void mouseDragY(MouseEvent drag) {
					double y = drag.getSceneY() - SCENE_OFFSET_Y;
					TARGET_SLIDER_Y = Math.min(MAX_SLIDER_Y, Math.max(y, 0.0));
					double ycoord = TARGET_SLIDER_Y;
					Platform.runLater(()->{
						treeSliderY.setTranslateY(ycoord);
					});
					
					showFilesDragY();
//					showFiles();
				}
				
				private void mouseScrollY(ScrollEvent scroll) {
					double deltaY = -scroll.getDeltaY()*scroll.getMultiplierY();
					
					if (scroll.isShiftDown()) {
						scrollX(-scroll.getDeltaX());
						findOffsetXAfterDrag();
						setOffsetX();
						return;
					}
					else {
						scrollY(deltaY);
					}
					
					showFilesDragY();
				}
				
				private void setMouseStartDragXParam(MouseEvent press) {					
					SCENE_OFFSET_X = press.getSceneX() - TARGET_SLIDER_X;

				}
				
				private void mouseDragX(MouseEvent drag) {
					
					double x = drag.getSceneX() - SCENE_OFFSET_X;
					TARGET_SLIDER_X = Math.min(MAX_SLIDER_X, Math.max(x, 0.0));
					treeSliderX.setTranslateX(TARGET_SLIDER_X);
					
					findOffsetXAfterDrag();
					setOffsetX();
				}
				
				private void scrollY(double deltaY) {
					double maxScrollValue = getMaxScrollYValue();
					double scroll = deltaY > 0 ? maxScrollValue : -maxScrollValue;
					if (deltaY>=0) {
						scroll = Math.min(deltaY*0.07, scroll);
					}
					else {
						scroll = Math.max(deltaY*0.07, scroll);
					}
					double preTargetY = MAX_SLIDER_Y * getShareOfScrollHeight(scroll);
					TARGET_SLIDER_Y = Math.max(0.0, Math.min(MAX_SLIDER_Y, preTargetY));
					double y = TARGET_SLIDER_Y;
					Platform.runLater(()->{
						treeSliderY.setTranslateY(y);
					});
					
				}
				
				private void scrollX(double deltaX) {
					double maxScrollValue = getMaxScrollXValue();
					double scroll = deltaX > 0 ? maxScrollValue : -maxScrollValue;
					if (deltaX>=0) {
						scroll = Math.min(deltaX, scroll);
					}
					else {
						scroll = Math.max(deltaX, scroll);
					}
					
					double preTargetX = MAX_SLIDER_X * getShareOfScrollWidth(scroll);
					TARGET_SLIDER_X = Math.max(0.0, Math.min(MAX_SLIDER_X, preTargetX));
					treeSliderX.setTranslateX(TARGET_SLIDER_X);
				}
				
				private double getShareOfScrollHeight(double scroll) {
					if ((CURRENT_VIEW_PORT_Y + scroll) >= MAX_VIEW_PORT_Y) {
						return 1.0;
					}
					else if((CURRENT_VIEW_PORT_Y + scroll) <= 0.0) {
						return 0.0;
					}
					else {
						return (CURRENT_VIEW_PORT_Y + scroll)/MAX_VIEW_PORT_Y;
					}
					
				}
				private double getShareOfScrollWidth(double scroll) {
					if ((FILE_OFFSET_X + scroll) >= MAX_VIEW_PORT_X) {
						return 1.0;
					}
					else if((FILE_OFFSET_X + scroll) <= 0.0) {
						return 0.0;
					}
					else {
						return (FILE_OFFSET_X + scroll)/MAX_VIEW_PORT_X;
					}
				}
				
				private double getMaxScrollYValue() {					
					return treeViewPort.getHeight()*0.2;
				}
				
				private double getMaxScrollXValue() {					
					return treeViewPort.getWidth()*0.2;
				}

			}
			
			class ScrollTrack {
				
				private void clickOnScrollTrackY(MouseEvent event) {
					double scroll;
					if(event.getY() > TARGET_SLIDER_Y + SLIDER_Y_HEIGHT) {
						scroll = treeViewPort.getHeight()-25;
						TARGET_SLIDER_Y = MAX_SLIDER_Y * drag.getShareOfScrollHeight(scroll);
						double y = TARGET_SLIDER_Y;
						Platform.runLater(()->{
							treeSliderY.setTranslateY(y);
						});
						
					}
					else if (event.getY() < TARGET_SLIDER_Y ) {
						scroll = -treeViewPort.getHeight()+25;
						TARGET_SLIDER_Y = MAX_SLIDER_Y * drag.getShareOfScrollHeight(scroll);
						double y = TARGET_SLIDER_Y;
						Platform.runLater(()->{
							treeSliderY.setTranslateY(y);
						});
					}
				}
				
				private void clickOnScrollTrackX(MouseEvent event) {
					double scroll;
					if (event.getX() > TARGET_SLIDER_X) {
						scroll = treeViewPort.getWidth();
					}
					else {
						scroll = -treeViewPort.getWidth();
					}
					
					TARGET_SLIDER_X = MAX_SLIDER_X * drag.getShareOfScrollWidth(scroll);
					treeSliderX.setTranslateX(TARGET_SLIDER_X);				
				}
				
				private void checkAndShowSliderY() {
					if (TOTAL_CELL_HEIGHTS + 50.0 >treeViewPort.getHeight()) {
						showScrollTrackY();
					}
					else {
						treeScrollTrackY.setVisible(false);
//						treeSliderY.setVisible(false);
						sizeManager.refreshSize();
					}
				}
				
				private void showScrollTrackY() {
					if(!treeScrollTrackY.isVisible()) {
//						setTreeScrollYWidth(15.0);
						
						treeScrollTrackY.setVisible(true);
//						treeSliderY.setVisible(true);
						sizeManager.refreshSize();
					}
				}
				
				private void checkAndShowSliderX() {
					if(MAX_WIDTH_FILE>treeViewPort.getMaxWidth()) {
						findSliderXWidth();
						showScrollTrackX();
					}
					else {
						treeScrollTrackX.setVisible(false);
//						setSize(treeSliderXContainer, 0);
					}
				}
				
				private void showScrollTrackX() {
					findMaxSliderX();
					if(!treeScrollTrackX.isVisible()) {
						treeSliderX.setTranslateX(TARGET_SLIDER_X = 0.0);
						treeScrollTrackX.setVisible(true);
						}
				}
				
			}

			private void refreshSliderHeight() {
				VIEW_PORT_HEIGHT = treeViewPort.getHeight();
				calculateSliderHeight();

			}

			private void calculateSliderHeight() {
				SHARE_VISIBLE_FILES_Y = VIEW_PORT_HEIGHT / (TOTAL_CELL_HEIGHTS + 50.0);

				if (SHARE_VISIBLE_FILES_Y < 1) {
					SLIDER_Y_HEIGHT = SHARE_VISIBLE_FILES_Y * VIEW_PORT_HEIGHT;					
				} else {
					SLIDER_Y_HEIGHT = VIEW_PORT_HEIGHT;
					TARGET_SLIDER_Y = 0.0;
					Platform.runLater(()->{
						treeSliderY.setTranslateY(0.0);
					});
				}

				SLIDER_Y_HEIGHT = Math.max(SLIDER_Y_HEIGHT, 20.0);
				setSliderHeight();

			}

			private void setSliderHeight() {

				treeSliderY.setMinHeight(SLIDER_Y_HEIGHT);
				treeSliderY.setPrefHeight(SLIDER_Y_HEIGHT);
				treeSliderY.setMaxHeight(SLIDER_Y_HEIGHT);
			}

			private void updateHeightsList() {
				synchronized (controller) {
					cellHeights = new ArrayList<Double>();
					int showedFilesSize = logic.getShowedTreeSize();
					TOTAL_CELL_HEIGHTS = (double)showedFilesSize * STANDART_HEIGHT;
					for (int i = 0; i<showedFilesSize; i++) {
						cellHeights.add(STANDART_HEIGHT);
					}
//					for (DirectoryFile file : logic.getShowedFiles()) {
//						if (file.isRoot()) {
//							cellHeights.add(ROOT_HEIGHT);
//							TOTAL_CELL_HEIGHTS += ROOT_HEIGHT;
//						} else {
//							cellHeights.add(STANDART_HEIGHT);
//							TOTAL_CELL_HEIGHTS += STANDART_HEIGHT;
//						}
//					}
				}
			}

			private static double MAX_VIEW_PORT_Y;
			private static double TOTAL_CELL_HEIGHTS;
			public static int FIRST_FILE_INDEX = -1;
			private static double FILE_OFFSET_Y = 0.0;
			public static int LAST_FILE_INDEX = -1;

			/*
			* обновляем позицию слайдера после раскрытия/сворачивания папки
			* ищем текущую позицию ползунка и устанавливаем слайдер в неё
			* точкой отсчета должен быть первый отображаемый файл, если этой позиции
			* нет в списке, тогда устанавливаем предыдущий первыйм. если и его нет, тогда
			* считаем что позиция слайдера должна быть вверху TARGET_SLIDER_Y = 0.0
			*/
			
			private void findSliderY() {
				if (SLIDER_Y_HEIGHT == VIEW_PORT_HEIGHT) {
					// показать файлы с текущей позиции
					findMaxSliderY();
					showFiles();
					return;
				}
				if (logic.getShowedTreeSize()<FIRST_FILE_INDEX+1) {
					//устанавливаем слайдер в максимально возможное значение 
					//MAX_SLIDER_Y
					findMaxSliderY();
					TARGET_SLIDER_Y = MAX_SLIDER_Y;
					double y = TARGET_SLIDER_Y;
					Platform.runLater(()->{
						treeSliderY.setTranslateY(y);
					});
					showFiles();
					return;
				}
				
				else {
					findMaxSliderY();
					findHeightFirstFile();
					showFiles();
				}
			}
			
			private void findHeightFirstFile(){
				double currentHeight = FILE_OFFSET_Y;
				for(int i = 0; i < FIRST_FILE_INDEX; i++) {
					currentHeight += cellHeights.get(i);
				}
				calculateShareScrollY(currentHeight);
			}
			
			private void calculateShareScrollY(double currentHeight) {				
				setMaxViewPortY();
				double share = currentHeight/MAX_VIEW_PORT_Y;
				calculateSliderY(share);
			}
			
			private void calculateSliderY(double share) {
				TARGET_SLIDER_Y = MAX_SLIDER_Y*share;
				TARGET_SLIDER_Y = Math.min(TARGET_SLIDER_Y, MAX_SLIDER_Y);
				double y = TARGET_SLIDER_Y;
				Platform.runLater(()->{
					treeSliderY.setTranslateY(y);
				});
				
			}
			
			private void findMaxSliderY() {
				SCROLL_TRACK_Y_HEIGHT = treeViewPort.getHeight();
				MAX_SLIDER_Y = Math.max(SCROLL_TRACK_Y_HEIGHT - SLIDER_Y_HEIGHT, 0.0);
				
			}
			
			private void showFilesDragY() {
				findCurrentViewPortHeight();
				findFirstAndLastFiles(CURRENT_VIEW_PORT_Y);
				if(logic.getShowedTreeSize() == 0) {
					return;
				}
				addChildrensInFilesTreeBoxDragY();
			}
			
			private void showFiles() {
				findCurrentViewPortHeight();
				findFirstAndLastFiles(CURRENT_VIEW_PORT_Y);
				if(logic.getShowedTreeSize() == 0) {
					return;
				}
				addChildrensInFilesTreeBox();
			}
			
			private void findCurrentViewPortHeight() {
				setMaxViewPortY();
				if (MAX_SLIDER_Y == 0.0) {
					CURRENT_VIEW_PORT_Y = 0.0;
				} else {
					CURRENT_VIEW_PORT_Y = MAX_VIEW_PORT_Y * (TARGET_SLIDER_Y / MAX_SLIDER_Y);
				}
			}
			//TODO treeViewPort.getMaxHeight(), возможно стоит заменить на treeViewPort.getHeight()
			private void setMaxViewPortY() {
				VIEW_PORT_HEIGHT = treeViewPort.getMaxHeight();
				MAX_VIEW_PORT_Y = TOTAL_CELL_HEIGHTS - VIEW_PORT_HEIGHT + 50.0;
			}

			private void findFirstAndLastFiles(double currentStartHeight) {
				double endWindow = currentStartHeight + VIEW_PORT_HEIGHT;
				double currentPosition = 0.0;
				int i = 0;
				FIRST_FILE_INDEX = -1;
				LAST_FILE_INDEX = -1;

				for (Double cellHeight : cellHeights) {

					if (FIRST_FILE_INDEX == -1 && isFirstFile(currentStartHeight, currentPosition, cellHeight)) {
						FIRST_FILE_INDEX = i;
						FILE_OFFSET_Y = currentStartHeight - currentPosition;
						currentPosition += cellHeight;
						i++;
						continue;
					}

					else if (LAST_FILE_INDEX == -1 && isLastFile(endWindow, currentPosition, cellHeight)) {
						
						LAST_FILE_INDEX = i;
						break;
					}

					else {
						currentPosition += cellHeight;
						i++;
					}
					
				}
			}

			private boolean isFirstFile(double currentStartHeight, double currentPosition, double cellHeight) {
				return (currentStartHeight >= currentPosition && currentStartHeight < (cellHeight + currentPosition));
			}

			private boolean isLastFile(double endWindow, double currentPosition, double cellHeight) {
				return (endWindow > currentPosition && endWindow <= (cellHeight + currentPosition));
			}
			
			private void addChildrensInFilesTreeBoxDragY() {
				if (LAST_FILE_INDEX>-1) {
					addFormFirstToLastFiles();
				}
				else {
					addAllOpenedFiles();
				}
				
			}
			
			private void addChildrensInFilesTreeBox() {
				findMaxWidthFile();
				scrollTracks.checkAndShowSliderX();
				findSliderX();
				if (LAST_FILE_INDEX>-1) {
					addFormFirstToLastFiles();
				}
				else {
					addAllOpenedFiles();
				}
			}
			
			private void addFormFirstToLastFiles() {
				synchronized (controller) {
					
//						MAX_WIDTH_FILE = 0.0;
//						int last = LAST_FILE_INDEX >= logic.getShowedTreeSize() ? logic.getShowedTreeSize() - 1
//								: LAST_FILE_INDEX;
//						int first = FIRST_FILE_INDEX;
						double offsetX = FILE_OFFSET_X;
						double offsetY = FILE_OFFSET_Y; 
						List<DirectoryFile> showedFiles = logic.getShowedTree();
						addInTreeBox(showedFiles, offsetX, offsetY);
//						Platform.runLater(() -> {
////							List<NodeInfo> showed = logic.getShowedFiles().subList(first, last);
////							List<Long> id = showed.stream().map(NodeInfo::getId).collect(Collectors.toList());
////							List<DirectoryFile> showedFiles = logic.mySQL.getAllById(id);
////							Map<Long, DirectoryFile> map = showedFiles.stream().collect(Collectors.toMap(DirectoryFile::getId, Function.identity()));
////							showedFiles = id.stream().map(map::get).collect(Collectors.toList());
//							synchronized (controller) {
//								
//								
//								fileTree.refreshIndicators(showedFiles);
//							}
//							
//						});
				}
				
			}
			
			private void addAllOpenedFiles() {
				
				synchronized (controller) {
//					int first = FIRST_FILE_INDEX;
					double offsetX = FILE_OFFSET_X;
					double offsetY = FILE_OFFSET_Y;
					List<DirectoryFile> showedFiles = logic.getShowedTree();
					
					addInTreeBox(showedFiles, offsetX, offsetY);
//					Platform.runLater(() -> {
////						List<NodeInfo> showed = logic.getShowedFiles().subList(first, logic.getShowedFiles().size());
////						List<Long> id = showed.stream().map(NodeInfo::getId).collect(Collectors.toList());
////						List<DirectoryFile> showedFiles = logic.mySQL.getAllById(id);
////						Map<Long, DirectoryFile> map = showedFiles.stream().collect(Collectors.toMap(DirectoryFile::getId, Function.identity()));
////						showedFiles = id.stream().map(map::get).collect(Collectors.toList());
//						synchronized (controller) {
//							
//						}
//					});
					
//					showInGui(boxes);
				}
				
			}

			private void addInTreeBox(List<DirectoryFile> showed, double offsetX, double offsetY) {
				List<HBox> boxes = new ArrayList<HBox>();
				List<DirectoryFile> nulls = new ArrayList<DirectoryFile>();
				for (DirectoryFile file : showed) {
					if(file==null) {
						nulls.add(file);
						continue;
					}
					HBox childrenContainer = containerFactory.getTreeContainer(file);
					childrenContainer.setTranslateY(-offsetY);
					childrenContainer.setTranslateX(-offsetX);
					boxes.add(childrenContainer);
				}
				
				showed.removeAll(nulls);
				
				Platform.runLater(() -> {
					synchronized (controller) {
						filesTreeBox.getChildren().clear();
						filesTreeBox.getChildren().addAll(boxes);
						fileTree.refreshIndicators(showed);
					}
				});
				
			}
			
			private static double MAX_WIDTH_FILE = 0.0;
			private static double FILE_OFFSET_X = 0.0;

			private void findMaxWidthFile() {
				synchronized (controller) {
					MAX_WIDTH_FILE = 0.0;
					for (NodeInfo file : logic.getShowedFiles()) {
						double width = file.getMinWidth(logic.getLeftMargin(file.getName()));
						if (width > MAX_WIDTH_FILE) {
							MAX_WIDTH_FILE = width;
						}
					}
				}
				
			}
			
			private void findSliderXWidth() {
				VIEW_PORT_WIDTH = treeViewPort.getMaxWidth();
				SCROLL_TRACK_X_WIDTH = treeScrollTrackX.getMaxWidth();
				SHARE_VISIBLE_FILES_X = VIEW_PORT_WIDTH/MAX_WIDTH_FILE;
				
				double tempSliderXWidth = SCROLL_TRACK_X_WIDTH*SHARE_VISIBLE_FILES_X;
				SLIDER_X_WIDTH = Math.max(tempSliderXWidth, 20);
				setSliderXWidth();
			}
			
			private void setSliderXWidth() {
				treeSliderX.setMinWidth(SLIDER_X_WIDTH);
				treeSliderX.setPrefWidth(SLIDER_X_WIDTH);
				treeSliderX.setMaxWidth(SLIDER_X_WIDTH);
			}
			
			private void findMaxSliderX() {
				MAX_SLIDER_X = SCROLL_TRACK_X_WIDTH - SLIDER_X_WIDTH;
			}
			private static double MAX_VIEW_PORT_X = 0.0;
			
			private void findSliderX() {
				findMaxSliderX();
				findMaxViewPortX();
				findSliderTargetX();
			}
			
			private void findMaxViewPortX() {
				if (MAX_WIDTH_FILE<VIEW_PORT_WIDTH) {
					MAX_VIEW_PORT_X = 0.0;
				}
				else {
					MAX_VIEW_PORT_X = MAX_WIDTH_FILE - VIEW_PORT_WIDTH;
				}
			}
			
			private void findSliderTargetX() {
				if(MAX_VIEW_PORT_X == 0.0) {
					FILE_OFFSET_X = TARGET_SLIDER_X = 0.0;
					setSliderX();
				}
				else if(FILE_OFFSET_X>MAX_VIEW_PORT_X) {
					FILE_OFFSET_X = MAX_VIEW_PORT_X;
					TARGET_SLIDER_X = MAX_SLIDER_X;
					setSliderX();
				}
				else {
					findAndSetTargetSliderX();
				}
				setOffsetX();
			}
			
			private void findAndSetTargetSliderX() {
				TARGET_SLIDER_X = (FILE_OFFSET_X/MAX_VIEW_PORT_X)*MAX_SLIDER_X;
				setSliderX();
			}
			
			private void setSliderX() {
				treeSliderX.setTranslateX(TARGET_SLIDER_X);
			}
			
			private void findOffsetXAfterDrag() {
				if (MAX_SLIDER_X == 0.0) {
					return;	
				}
				double share = TARGET_SLIDER_X/MAX_SLIDER_X;
				FILE_OFFSET_X = MAX_VIEW_PORT_X*share;
			}
			
			private void setOffsetX() {
				double offsetX = FILE_OFFSET_X;
				Platform.runLater(()->{
					for (Node node : filesTreeBox.getChildren()) {
					node.setTranslateX(-offsetX);
				}
				});
				
			}
			
		}

		

	}
	
	public class ChildrenFiles{
		TableView tableView;
		ScrollPanel scrollPanel;
		Delete delete;
		public Select select;
		private static DirectoryFile openedInChildrenFiles;
		
		private static List<DirectoryFile> FILES_IN_CHILDREN_VIEW_PORT;
		private static double SCENE_OFFSET_X;
		private static double SLIDER_X_WIDTH;
		private static double TARGET_SLIDER_X = 0.0;
		private static double MAX_SLIDER_X;
		private static double SCROLL_TRACK_X_WIDTH;
//		private static double SHARE_VISIBLE_FILES_X;
		
		private static double SCENE_OFFSET_Y;
		private static double SLIDER_Y_HEIGHT;
		private static double TARGET_SLIDER_Y = 0.0;
		private static double MAX_SLIDER_Y;
//		private static double SCROLL_TRACK_Y_WIDTH;
		private static double SHARE_VISIBLE_FILES_Y;
		private static double SCROLL_TRACK_Y_HEIGHT;
		
		private static double TOTAL_CELL_HEIGHTS;
		
		private static double VIEW_PORT_HEIGHT;
		private static double VIEW_PORT_WIDTH;
		private static double VIEW_PORT_MAX_X;
		private static double CURRENT_VIEW_PORT_X = 0.0;
		private static double VIEW_PORT_MAX_Y;
		private static double CURRENT_VIEW_PORT_Y;
		private static double FILE_OFFSET_Y;
		
		private static double FILES_WIDTH = 0.0;
		
		private static int FIRST_FILE_INDEX = -1;
		private static int LAST_FILE_INDEX = -1;
		
		private final static double STANDART_HEIGHT = 25.0;
		private static final double HEADER_HEIGHT = 20.0;
		
		ChildrenFiles(){
			FILES_IN_CHILDREN_VIEW_PORT = new ArrayList<DirectoryFile>();
			tableView = new TableView();
			scrollPanel = new ScrollPanel();
			select = new Select();
			delete = new Delete();
		}
		
		private void showChildren(DirectoryFile parent, RefreshViewPort refresh) {
			
//			scrollPanel.clearChildrenContainerInOldChildrenFiles();
			List<DirectoryFile> childrenDirectoryFiles;
			
			
			childrenDirectoryFiles = extractor.getNewChildrenDirectoryFiles(parent, refresh).getChildrenFiles();
//			childrenDirectoryFiles = parent.getNewChildrenDirectoryFiles(refresh);
			 
			logic.setShowedChildrenFiles(childrenDirectoryFiles);
//			clearListOpenedInChildrenFiles();
			childrenFiles.select.firstSelectedFileObj = null;
			Select.FIRST_SELECTED_FILE = Select.LAST_SELECTED_FILE = -1;
			controllerData.setOpenedInChildrenFiles(parent);
			
			scrollPanel.newOpenChildren();
			
		}
		
		private void clearFilesInChildrenViewPort() {
			
//			for(DirectoryFile df : FILES_IN_CHILDREN_VIEW_PORT) {
//				df.setChildrenIndicator(null);
//			}
			FILES_IN_CHILDREN_VIEW_PORT.clear();
		}
		
		@SuppressWarnings("unlikely-arg-type")
		public void deleteFromChildrenShowedFiles(DirectoryFile children) {
			synchronized (controller) {
				logic.getshowedChildrenFiles().remove(children);
				select.selectedFiles.remove(children);
			}
			
		}
		
		public void refreshChildrenFiles() {
//			scrollPanel.refreshChildrenIndicators();
			scrollPanel.updateChildrenFiles();
		}
		public void openInChildrenBox(DirectoryFile opened) {
			controllerData.setOpenedInChildrenFiles(opened);
			childrenFiles.select.firstSelectedFileObj = null;
			Select.FIRST_SELECTED_FILE = Select.LAST_SELECTED_FILE = -1;
			var openedChildrens = extractor.getNewChildrenDirectoryFiles(opened, RefreshViewPort.CHILDREN_FILES);
			logic.setShowedChildrenFiles(openedChildrens.getChildrenFiles());
			scrollPanel.newOpenChildren();
		}
		
		private void enterAction() {
			var file = controllerData.getOpenedInChildrenFiles();
			if(treeViewPort.isFocused()||childrenViewPort.isFocused()) {
				openInExplorer(file);
			}
		}

		private void openInExplorer(DirectoryFile file) {
			var selected = select.firstSelectedFileObj;
			if(selected!=null&&selected.getThisFile()!=null&&selected.getThisFile().exists()) {
				ProcessBuilder explorer = new ProcessBuilder("explorer.exe","/select," + selected.getThisFile().toString());
				try {
					explorer.start();
				} catch (IOException e) {
					
				}
			}
			else if(file!=null&&file.getThisFile().exists()) {
				ProcessBuilder explorer = new ProcessBuilder("explorer.exe", file.getThisFile().toString());
				try {
					explorer.start();
				} catch (IOException e) {
					
				}
			}
		}
		
		class TableView {
			
			Header header;
			
			private static double NAME_PROPORTION = 0.7;
			private static double TABLE_VIEW_NAME_WIDTH;
			private final static double TABLE_VIEW_SPLITTER_WIDTH = 2.0;
			private final static double TABLE_VIEW_SIZE_WIDTH = 100.0;
			private final static double TABLE_VIEW_MAX_FILE_MARKER_WIDTH = 2.0;
			
			private static double SPLITTER_OFFSET_X;
			
			TableView(){
				header = new Header();				
			}
			
			class Header {
				private static Pane header;
				HBox nameContainer;
				HBox splitter;
				HBox sizeContainer;
				HBox maxFileSizeMarker;
				
				private static final PseudoClass headerStatus = PseudoClass.getPseudoClass("header");
				
				Header() {
					addTableHeader();
					setFilesWidth();
					addWhiteFiller();
				}

				private void addTableHeader() {
					makeNameContainer();
					makeSplitter();
					makeSizeContainer();
					makeMaxFileSizeMarker();
					setTableHeaderLayouts(splitter, sizeContainer);
					header = new Pane(nameContainer, splitter, sizeContainer, maxFileSizeMarker);
					header.getStyleClass().add("pane-header");
					header.pseudoClassStateChanged(headerStatus, true);
					setHeaderSize(header);
					header.setTranslateY(-childrenBox.getMaxHeight());
					header.setOnMouseClicked(click->{
						click.consume();
					});
					childrenSuperBox.getChildren().add(header);
					setNameAction();
					setSizeAction();
				}

				private void makeNameContainer() {
					Label name = new Label();
					name.setText("имя файла");
					setGreyLabelStyle(name);
					nameContainer = new HBox(getLeftMargin(), name);
					nameContainer.setLayoutX(0.0);
					nameContainer.setAlignment(Pos.CENTER_LEFT);
					TABLE_VIEW_NAME_WIDTH = NAME_PROPORTION * childrenViewPort.getMaxWidth();
					
					ContainerFactory.setSize(nameContainer, TABLE_VIEW_NAME_WIDTH, HEADER_HEIGHT);
					
				}

				private void makeSplitter() {
					splitter = new HBox();
					ContainerFactory.setSize(splitter, TABLE_VIEW_SPLITTER_WIDTH, HEADER_HEIGHT);
					setSplitterStyle();
					setSplitterAction();
					
				}

				private void setSplitterStyle() {
					splitter.getStyleClass().clear();
					splitter.getStyleClass().add("table-hbox");
					PseudoClass splitterStyle = PseudoClass.getPseudoClass("table-splitter");
					splitter.pseudoClassStateChanged(splitterStyle, true);
					splitter.setCursor(Cursor.W_RESIZE);
				}

				private void makeSizeContainer() {
					Label size = new Label();
					size.setText("размер файла");
					setGreyLabelStyle(size);
					sizeContainer = new HBox(getLeftMargin(), size);
					sizeContainer.setAlignment(Pos.CENTER_LEFT);
					ContainerFactory.setSize(sizeContainer, TABLE_VIEW_SIZE_WIDTH, HEADER_HEIGHT);
					
				}
				
				private void makeMaxFileSizeMarker() {
					maxFileSizeMarker = ContainerFactory.ChildrenContainerFactory.getMaxFileSizeMarker(HEADER_HEIGHT);
				}

				private void setTableHeaderLayouts(HBox splitter, HBox sizeContainer) {
					splitter.setLayoutX(TABLE_VIEW_NAME_WIDTH);
					double sizeContainerLayoutX = TABLE_VIEW_NAME_WIDTH + splitter.getMaxWidth();
					sizeContainer.setLayoutX(sizeContainerLayoutX);
					double maxFileSizeMarkerLayoutX = sizeContainerLayoutX + TABLE_VIEW_SIZE_WIDTH;
					maxFileSizeMarker.setLayoutX(maxFileSizeMarkerLayoutX);
				}

				private HBox getLeftMargin() {
					HBox leftMargin = new HBox();
					ContainerFactory.setSize(leftMargin, 10.0, 25.0);
					return leftMargin;
				}

				private void setHeaderSize(Pane header) {
					double width = 0.0;
					for (Node node : header.getChildren()) {
						if (node instanceof HBox container) {
							width += container.getMaxWidth();
						}
					}
					ContainerFactory.setSize(header, width, HEADER_HEIGHT);
				}
				
				private void setFilesWidth() {
					FILES_WIDTH = TABLE_VIEW_NAME_WIDTH + TABLE_VIEW_SPLITTER_WIDTH + TABLE_VIEW_SIZE_WIDTH + TABLE_VIEW_MAX_FILE_MARKER_WIDTH;
				}
				
				private void addWhiteFiller() {
					double height = childrenViewPort.getMaxHeight() - HEADER_HEIGHT;
					HBox whiteFiller = containerFactory.getWhiteFiller(height);
					synchronized (guiMon) {
						childrenBox.getChildren().add(whiteFiller);
						Label label = new Label("TEXT");
						childrenBox.getChildren().add(label);
					}
					
				}
				
				private void setSplitterAction() {
					splitter.setOnMousePressed(press->{
						if(press.getClickCount()==1) {
							splitterPress(press);
						}
						else if(press.getClickCount()==2) {
							splitterMaximize();
						}
					});
					
					splitter.setOnMouseDragged(drag->{
						splitterDrag(drag);
					});
				}
				
				private void splitterPress(MouseEvent press) {
					SPLITTER_OFFSET_X = press.getSceneX() - TABLE_VIEW_NAME_WIDTH;
				}
				
				private void splitterDrag(MouseEvent drag){

					
					double currentSplitterX = drag.getSceneX() - SPLITTER_OFFSET_X;
//					double maxSize = childrenViewPort.getMaxWidth() - TABLE_VIEW_SPLITTER_WIDTH - 2.0;
					if (currentSplitterX <= 100.0) {
						setMinSplitterPosition();
					}
//					else if (currentSplitterX >= maxSize) {
//						setMaxSplitterPosition(maxSize);
//					}
					else {
						setSplitterPosition(currentSplitterX);
					}
					scrollPanel.scrollTrack.resize();
				}

				private void setMinSplitterPosition() {
					if(splitter.getLayoutX() == 100.0) {
						return;
					}
					else {
						setSplitterPosition(100.0);
					}
				}

//				private void setMaxSplitterPosition(double maxSize) {
//					if (splitter.getLayoutX() == maxSize) {
//						return;
//					}
//					else {
//						setSplitterPosition(maxSize);
//					}
//				}

				private void setSplitterPosition(double currentSplitterX) {
					TABLE_VIEW_NAME_WIDTH = currentSplitterX;
					setFilesWidth();
					setNewHeaderParameters();
					synchronized(guiMon) {
						scrollPanel.refreshAfterHeaderResize();
					}
					
				}

				private void setNewHeaderParameters() {
					ContainerFactory.setSize(nameContainer, TABLE_VIEW_NAME_WIDTH, HEADER_HEIGHT);
					splitter.setLayoutX(TABLE_VIEW_NAME_WIDTH);
					sizeContainer.setLayoutX(TABLE_VIEW_NAME_WIDTH + splitter.getMaxWidth());
					maxFileSizeMarker.setLayoutX(TABLE_VIEW_NAME_WIDTH + splitter.getMaxWidth() + TABLE_VIEW_SIZE_WIDTH);
					ContainerFactory.setWidth(header, FILES_WIDTH);
				}
				
				private void splitterMaximize() {
					double currentSplitterX = childrenViewPort.getMaxWidth() - TABLE_VIEW_SIZE_WIDTH - TABLE_VIEW_SPLITTER_WIDTH - TABLE_VIEW_MAX_FILE_MARKER_WIDTH;
					setSplitterPosition(currentSplitterX);
					scrollPanel.scrollTrack.resize();
				}
				
				private void setNameAction() {
					nameContainer.setOnMouseClicked(click->{
						if(click.getButton()==MouseButton.PRIMARY && click.getClickCount()==1) {
							Runnable task = ()->{
								nameClick();
							};
							logic.updateFileTree.addTask(task);
						}
					});
				}
				
				private void nameClick() {
					extractor.selectSortTypeByName();
					extractor.increaseSortNameIndex();
					synchronized(MainController.this) {
						if(openedInChildrenFiles!=null) {
							childrenFiles.openInChildrenBox(openedInChildrenFiles);
						}
						
					}
				}
				
				private void setSizeAction() {

					sizeContainer.setOnMouseClicked(click->{
						if(click.getButton()==MouseButton.PRIMARY && click.getClickCount()==1) {
							Runnable task = ()->{
								sizeClick();
							};
							logic.updateFileTree.addTask(task);
						}
					});
				
				}
				
				private void sizeClick() {
					extractor.selectSortTypeBySize();
					extractor.increaseSortSizeIndex();
					synchronized(MainController.this) {
						if(openedInChildrenFiles!=null) {
							childrenFiles.openInChildrenBox(openedInChildrenFiles);
						}
						
					}
				}
				
			}
			
		}
		
		class ScrollPanel{
			Drag drag;
			ScrollTrack scrollTrack = new ScrollTrack();
			
			ScrollPanel(){
				drag = new Drag();
				setSlidersAction();
				setScrollTracksAction();
			}
			
			private void setSlidersAction(){
				setSliderXAction();
				setSliderYAction();
			}
			
			private void setSliderXAction() {
				childrenSliderX.setOnMousePressed(press->{
					drag.setStartDragParamX(press);
					press.consume();
				});
				childrenSliderX.setOnMouseDragged(drag->{
					synchronized(controller) {
						this.drag.mouseDragX(drag);
					}
					
					drag.consume();
				});
			}
			
			private void setSliderYAction() {
				childrenSliderY.setOnMousePressed(press->{
					synchronized (controller) {
						drag.setStartDragParamY(press);
					}
					press.consume();
				});
				
				childrenSliderY.setOnMouseDragged(drag->{
					synchronized(controller) {
						this.drag.mouseDragY(drag);
					}
					
					drag.consume();
				});
			}
			
			private void setScrollTracksAction() {
				setScrollTrackXAction();
				setScrollTrackYAction();
			}
			
			private void setScrollTrackXAction() {
				childrenScrollTrackX.setOnMouseClicked(click->{
					synchronized(MainController.this) {
						scrollTrack.clickOnScrollTrackX(click);
					}
				});
			}
			
			private void setScrollTrackYAction() {
				childrenScrollTrackY.setOnMouseClicked(click->{
					synchronized(MainController.this) {
						scrollTrack.clickOnScrollTrackY(click);
					}
				});
			}
			
			private void newOpenChildren() {
				updateTotalCellHeight();
				TARGET_SLIDER_Y = 0.0;
				FILE_OFFSET_Y = 0.0;
				updateSliderY();
				scrollTrack.showHeaderFromStart();
				select.resetSelectionProperties();
				findFilesToShow();
			}
			
			//TODO Добавить после удаления
			private void updateChildrenFiles() {
				synchronized (controller) {
//					refreshScrollYAfterResize();
					updateTotalCellHeight();
					findViewPortMaxY();
					scrollTrack.updateVisibleSliderY();
					double share = CURRENT_VIEW_PORT_Y / VIEW_PORT_MAX_Y;
					if (share > 1.0) {
						CURRENT_VIEW_PORT_Y = VIEW_PORT_MAX_Y;
						share = 1.0;
					}
					scrollTrack.calculateSliderHeight();
					scrollTrack.setSliderYHeight();
					scrollTrack.findMaxSliderY();
					TARGET_SLIDER_Y = MAX_SLIDER_Y * share;
					double y = TARGET_SLIDER_Y;
					Platform.runLater(()->{
						synchronized(controller) {
							childrenSliderY.setTranslateY(y);
						}
					});

					findFilesToShow();					
					
//					updateTotalCellHeight();
//					updateSliderY();
//					TARGET_SLIDER_Y = Math.min(MAX_SLIDER_Y, Math.max(TARGET_SLIDER_Y, 0.0));
//					System.out.println("2955 TARGET_SLIDER_Y" + TARGET_SLIDER_Y);
//					double y = TARGET_SLIDER_Y;
//					Platform.runLater(()->{
//						synchronized(controller) {
//							childrenSliderY.setTranslateY(y);
//						}
//					});
//					findFilesToShow();
				}
				
			}
			
			private void updateTotalCellHeight() {
				TOTAL_CELL_HEIGHTS = logic.getshowedChildrenFilesSize()*STANDART_HEIGHT;
			}
			
			private void updateSliderY() {
				VIEW_PORT_WIDTH = childrenViewPort.getMaxWidth();
				scrollTrack.refreshSliderYHeight();
				scrollTrack.updateVisibleSliderY();
				scrollTrack.setSliderY();
				scrollTrack.findMaxSliderY();
			}
			
			private void findFilesToShow() {
				findFirstAndLastFileIndex();
				showFromFirstToLastFile();
//				if (SHARE_VISIBLE_FILES_Y<1) {
//					findFirstAndLastFileIndex();
//					showFromFirstToLastFile();
//				}
//				else {
//					showAllFiles();
//				}
				addWhiteFiller();
			}
			
			private void findFirstAndLastFileIndex() {
				findViewPortMaxY();
				findCurrentViewPortY();
				FIRST_FILE_INDEX = (int)(CURRENT_VIEW_PORT_Y/STANDART_HEIGHT);
				findFileOffsetY();
				findLastFileIndex();
			}
			
			private void findViewPortMaxY() {
				VIEW_PORT_MAX_Y = TOTAL_CELL_HEIGHTS - childrenViewPort.getMaxHeight()*0.75 + HEADER_HEIGHT;
				VIEW_PORT_MAX_Y = Math.max(VIEW_PORT_MAX_Y, 0.0);
			}
			
			private void findCurrentViewPortY() {
				double share = TARGET_SLIDER_Y/MAX_SLIDER_Y;
				if(Double.isNaN(share)) {
					share = 0.0;
				}
				CURRENT_VIEW_PORT_Y = VIEW_PORT_MAX_Y * share;
			}
			
			private void findFileOffsetY() {
				FILE_OFFSET_Y = CURRENT_VIEW_PORT_Y - (double)FIRST_FILE_INDEX*STANDART_HEIGHT;
				FILE_OFFSET_Y = Math.max(FILE_OFFSET_Y, 0.0);
			}
			
			private void findLastFileIndex() {
				double currentMaxHeight = CURRENT_VIEW_PORT_Y + VIEW_PORT_HEIGHT;
				LAST_FILE_INDEX = (int)(currentMaxHeight/STANDART_HEIGHT);
				LAST_FILE_INDEX = Math.min(LAST_FILE_INDEX, logic.getshowedChildrenFilesSize()-1);

			}
			
			@SuppressWarnings("unlikely-arg-type")
			private void showFromFirstToLastFile() {
				
				clearFilesInChildrenViewPort();
				synchronized (controller) {
					List<DirectoryFile> childrenDirectoryFiles = logic.getshowedChildrenFiles();
					List<HBox> containers = new ArrayList<HBox>();
					for (int i = FIRST_FILE_INDEX; i < LAST_FILE_INDEX + 1; i++) {
						DirectoryFile children = childrenDirectoryFiles.get(i);
						HBox container = containerFactory.childrenFactory.getChildrenContainer(children);
						if(select.selectedFiles.contains(children)) {
							select.setSelectedStatus(container);
						}
						else {
							select.setStandartStatus(container);
						}
						container.setTranslateY(-FILE_OFFSET_Y);
						containers.add(container);

						FILES_IN_CHILDREN_VIEW_PORT.add(children);
					}
					Platform.runLater(() -> {
							synchronized (guiMon) {
								childrenBox.getChildren().clear();
								childrenBox.getChildren().addAll(containers);
							}
						});
					
					
				}
				
				
			}
			
//			private void showAllFiles() {
//				clearFilesInChildrenViewPort();
//				Platform.runLater(()->{
//					synchronized(guiMon) {
//						
//					}
//				});
//				
//				
//				List<DirectoryFile> childrenDirectoryFiles = logic.getshowedChildrenFiles();
//				List<HBox> containers = new ArrayList<HBox>();
//				for (DirectoryFile children : childrenDirectoryFiles) {
//					HBox container = containerFactory.childrenFactory.getChildrenContainer(children);
//					if(select.selectedFiles.contains(children)) {
//						select.setSelectedStatus(container);
//					}
//					else {
//						select.setStandartStatus(container);
//					}
//					containers.add(container);
//					
//					
//					FILES_IN_CHILDREN_VIEW_PORT.add(children);
//				}
//				Platform.runLater(()->{
//					synchronized(guiMon) {
//						childrenBox.getChildren().clear();
//						childrenBox.getChildren().addAll(containers);
//					}
//				});
//			}
			//synchronized main controller
			private void refreshAfterHeaderResize() {
				if (childrenBox.getChildren().size()==0) {
					return;
				}
				double containerNewWidth = TableView.TABLE_VIEW_NAME_WIDTH + TableView.TABLE_VIEW_SPLITTER_WIDTH + TableView.TABLE_VIEW_SIZE_WIDTH + TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH;
				for (int i = 0; i < childrenBox.getChildren().size(); i++	) {
					if(childrenBox.getChildren().get(i) instanceof HBox container) {
						if (container.getChildren().size() == 2) {
							resizeWhiteFiller(container);
						}
						if (container.getChildren().size()<4) {
							return;
						}
						separateContainerPartsForResize(container, containerNewWidth);
					}
				}
			}
			
			private void resizeWhiteFiller(HBox whiteFiller) {
				if (whiteFiller.getChildren().size()!=2) {
					return;
				}
				if (whiteFiller.getChildren().get(0) instanceof HBox filler) {
					double width = ChildrenFiles.FILES_WIDTH-ChildrenFiles.TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH;
					ContainerFactory.setWidth(filler, width);
					
				}
				ContainerFactory.setWidth(whiteFiller, ChildrenFiles.FILES_WIDTH);
			}

			private void separateContainerPartsForResize(HBox container, double containerNewWidth) {
				double containerOldWidth = 0.0;
				if(container.getChildren().get(0) instanceof HBox nameContainer) {
					containerOldWidth += nameContainer.getMaxWidth();
					resizeNameContainer(nameContainer);
				}
				if(container.getChildren().get(1) instanceof HBox sizeContainer) {
					containerOldWidth += sizeContainer.getMaxWidth();
				}
				if(container.getChildren().get(3) instanceof HBox indicator) {
					resizeIndicator(indicator, containerOldWidth);
				}
				setNewContainerWidth(container);
			}
			
			private void resizeNameContainer(HBox nameContainer) {
				double width = TableView.TABLE_VIEW_NAME_WIDTH + TableView.TABLE_VIEW_SPLITTER_WIDTH;
				nameContainer.setMinWidth(width);
				nameContainer.setPrefWidth(width);
				nameContainer.setMaxWidth(width);
			}
			
			private void resizeIndicator(HBox indicator, double containerOldWidth){
				double indicatorOldWidth = indicator.getMaxWidth();
				double share = indicatorOldWidth/containerOldWidth;
				double indicatorNewWidth = share * (FILES_WIDTH-TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH);
				indicator.setMinWidth(indicatorNewWidth);
				indicator.setPrefWidth(indicatorNewWidth);
				indicator.setMaxWidth(indicatorNewWidth);
				indicator.setTranslateX(-FILES_WIDTH);				
			}
			
			private void setNewContainerWidth(HBox container) {
				container.setMinWidth(ChildrenFiles.FILES_WIDTH);
				container.setPrefWidth(ChildrenFiles.FILES_WIDTH);
				container.setMaxWidth(ChildrenFiles.FILES_WIDTH);
			}
			
			private void clearChildrenViewPort() {
				logic.setShowedChildrenFiles(new ArrayList<DirectoryFile>());
				controllerData.setOpenedInChildrenFiles(null);
				childrenFiles.clearFilesInChildrenViewPort();
				
				Platform.runLater(()->{
					synchronized(guiMon) {
						childrenBox.getChildren().clear();
					}
				});
				
				CURRENT_VIEW_PORT_X = 0.0;
				TOTAL_CELL_HEIGHTS = 0.0;
				TARGET_SLIDER_Y = 0.0;
				updateSliderY();
				scrollTrack.showHeaderFromStart();
				findFilesToShow();
			}
			
			

			private void addWhiteFiller() {
				double heightAfterCurrent = TOTAL_CELL_HEIGHTS - CURRENT_VIEW_PORT_Y;
				
				double height = VIEW_PORT_HEIGHT - heightAfterCurrent > 0 ? VIEW_PORT_HEIGHT - heightAfterCurrent : 0.0;
				if (height>0.0) {
					HBox whiteFiller = containerFactory.getWhiteFiller(height);
					whiteFiller.setTranslateY(- FILE_OFFSET_Y);
					Platform.runLater(()->{
						synchronized(guiMon) {
							childrenBox.getChildren().add(whiteFiller);
						}
					});
					
				}
			}
			
			private void refreshChildrenIndicators() {
				List<DirectoryFile> childrensViewPort = logic.getshowedChildrenFiles().subList(FIRST_FILE_INDEX, LAST_FILE_INDEX+1);
				for (int i = 0; i<childrensViewPort.size(); i ++) {
					HBox container = (HBox)childrenBox.getChildren().get(i);
					DirectoryFile children = childrensViewPort.get(i);
					separateContainerForRefresh(children, container);
				}
			}
			
			private void separateContainerForRefresh(DirectoryFile children, HBox container) {
				if(container.getChildren().size()!=4) {
					return;
				}
				
				if (container.getChildren().get(3) instanceof HBox indicator) {
					double indicatorWidth = children.getShare()*(FILES_WIDTH - TableView.TABLE_VIEW_MAX_FILE_MARKER_WIDTH);
					Platform.runLater(()->{
						setWidth(indicator, indicatorWidth);
					});
				}
			}

			class Drag {
				private void setStartDragParamY(MouseEvent press) {
					SCENE_OFFSET_Y = press.getSceneY() - TARGET_SLIDER_Y;
					SLIDER_Y_HEIGHT = childrenSliderY.getMaxHeight();
					SCROLL_TRACK_Y_HEIGHT = childrenViewPort.getMaxHeight();
					MAX_SLIDER_Y = SCROLL_TRACK_Y_HEIGHT - SLIDER_Y_HEIGHT;
				}

				private void mouseDragY(MouseEvent drag) {
					double y = drag.getSceneY() - SCENE_OFFSET_Y;
					TARGET_SLIDER_Y = Math.min(MAX_SLIDER_Y, Math.max(y, 0.0));
					childrenSliderY.setTranslateY(TARGET_SLIDER_Y);
					
					findFilesToShow();
				}
				
				private void setStartDragParamX(MouseEvent press) {
					SCENE_OFFSET_X = press.getSceneX() - TARGET_SLIDER_X;
					SLIDER_X_WIDTH = childrenSliderX.getMaxWidth();
					SCROLL_TRACK_X_WIDTH = childrenViewPort.getMaxWidth();
					MAX_SLIDER_X = SCROLL_TRACK_X_WIDTH - SLIDER_X_WIDTH;
				}
				
				private void mouseDragX(MouseEvent drag) {
					double x = drag.getSceneX() - SCENE_OFFSET_X;
					TARGET_SLIDER_X =  Math.min(Math.max(x, 0.0), MAX_SLIDER_X);
					childrenSliderX.setTranslateX(TARGET_SLIDER_X);
					
					scrollTrack.dragX();
				}
				
				private void mouseScrollY(ScrollEvent scroll) {

					
					if (scroll.isShiftDown()) {
						double deltaX = -scroll.getDeltaX()*scroll.getMultiplierX();
						scrollX(deltaX);
						scrollTrack.dragX();
						return;
					}
					else {
						double deltaY = -scroll.getDeltaY()*scroll.getMultiplierY();
						scrollY(deltaY);
					}
					findFilesToShow();
				}
				
				private void scrollY(double deltaY) {
					double maxScrollValue = getMaxScrollYValue();
					double scroll = deltaY > 0 ? maxScrollValue : -maxScrollValue;
					if (deltaY>=0) {
						scroll = Math.min(deltaY*0.07, scroll);
					}
					else {
						scroll = Math.max(deltaY*0.07, scroll);
					}
					double preTargetY = MAX_SLIDER_Y * getShareOfScrollHeight(scroll);
					TARGET_SLIDER_Y = Math.max(0.0, Math.min(MAX_SLIDER_Y, preTargetY));
					childrenSliderY.setTranslateY(TARGET_SLIDER_Y);
				}
				
				private double getMaxScrollYValue() {
					return childrenViewPort.getHeight()*0.2;
				}
				
				private double getShareOfScrollHeight(double scroll) {
					if ((CURRENT_VIEW_PORT_Y + scroll) >= VIEW_PORT_MAX_Y) {
						return 1.0;
					}
					else if((CURRENT_VIEW_PORT_Y + scroll) <= 0.0) {
						return 0.0;
					}
					else {
						return (CURRENT_VIEW_PORT_Y + scroll)/VIEW_PORT_MAX_Y;
					}
					
				}
				
				private void scrollX(double deltaX) {
					double maxScrollValue = getMaxScrollXValue();
					double scroll = deltaX > 0 ? maxScrollValue : -maxScrollValue;
					if (deltaX>=0) {
						scroll = Math.min(deltaX, scroll);
					}
					else {
						scroll = Math.max(deltaX, scroll);
					}
					
					double preTargetX = MAX_SLIDER_X * getShareOfScrollWidth(scroll);
					
					TARGET_SLIDER_X = Math.max(0.0, Math.min(MAX_SLIDER_X, preTargetX));
					
					childrenSliderX.setTranslateX(TARGET_SLIDER_X);
				}
				
				private double getMaxScrollXValue() {
					return childrenViewPort.getWidth()*0.2;
				}
				
				private double getShareOfScrollWidth(double scroll) {
					if ((CURRENT_VIEW_PORT_X + scroll) >= VIEW_PORT_MAX_X) {
						return 1.0;
					}
					else if((CURRENT_VIEW_PORT_X + scroll) <= 0.0) {
						return 0.0;
					}
					else {
						return (CURRENT_VIEW_PORT_X + scroll)/VIEW_PORT_MAX_X;
					}
				}
			}
			
			class ScrollTrack{
				
				ScrollTrack(){
					refreshSliderXWidth();
					refreshSliderYHeight();
				}
				
				private void refreshSliderYHeight() {
					VIEW_PORT_HEIGHT = childrenViewPort.getMaxHeight();
					calculateSliderHeight();
					setSliderYHeight();
					
				}

				private void calculateSliderHeight() {
					findShareVisibleFilesY();
					
					if (SHARE_VISIBLE_FILES_Y < 1) {
						SLIDER_Y_HEIGHT = SHARE_VISIBLE_FILES_Y * VIEW_PORT_HEIGHT;					
					} else {
						SLIDER_Y_HEIGHT = VIEW_PORT_HEIGHT;
						TARGET_SLIDER_Y = 0.0;
					}

					SLIDER_Y_HEIGHT = Math.max(SLIDER_Y_HEIGHT, 20.0);
				}
				
				private void findShareVisibleFilesY() {
					SHARE_VISIBLE_FILES_Y = VIEW_PORT_HEIGHT / (TOTAL_CELL_HEIGHTS + VIEW_PORT_HEIGHT * 0.25 + HEADER_HEIGHT);
				}

				private void setSliderYHeight() {

					childrenSliderY.setMinHeight(SLIDER_Y_HEIGHT);
					childrenSliderY.setPrefHeight(SLIDER_Y_HEIGHT);
					childrenSliderY.setMaxHeight(SLIDER_Y_HEIGHT);
				}
				
				private void findMaxSliderY() {
					SCROLL_TRACK_Y_HEIGHT = childrenViewPort.getMaxHeight();
					MAX_SLIDER_Y = SCROLL_TRACK_Y_HEIGHT - SLIDER_Y_HEIGHT;
				}
				
				
				private void setSliderY() {
					double y = TARGET_SLIDER_Y;
					Platform.runLater(() -> {
						synchronized (controller) {
							childrenSliderY.setTranslateY(y);
						}
					});
				}
				
				private void updateVisibleSliderY() {
					findViewPortMaxY();
					if (VIEW_PORT_MAX_Y <= 0.0
							&& childrenScrollTrackY.isVisible()) {
						childrenScrollTrackY.setVisible(false);
						VIEW_PORT_WIDTH += STANDART_SCROLL_TRACK_Y_WIDTH;
						setChildrenViewPortWidth();
					}
					
					else if(VIEW_PORT_MAX_Y > 0.0
							&& !childrenScrollTrackY.isVisible()) {
						VIEW_PORT_WIDTH -= STANDART_SCROLL_TRACK_Y_WIDTH;
						setChildrenViewPortWidth();		
						childrenScrollTrackY.setVisible(true);
					}
					
				}
				
				private void setChildrenViewPortWidth() {
					childrenViewPort.setMinWidth(VIEW_PORT_WIDTH);
					childrenViewPort.setPrefWidth(VIEW_PORT_WIDTH);
					childrenViewPort.setMaxWidth(VIEW_PORT_WIDTH);
				}
				
				private void refreshScrollYAfterResize() {
					synchronized (MainController.this) {
						findViewPortMaxY();
						updateVisibleSliderY();
						double share = CURRENT_VIEW_PORT_Y / VIEW_PORT_MAX_Y;
						if (share > 1.0) {
							CURRENT_VIEW_PORT_Y = VIEW_PORT_MAX_Y;
							share = 1.0;
						}
						calculateSliderHeight();
						setSliderYHeight();
						findMaxSliderY();
						TARGET_SLIDER_Y = MAX_SLIDER_Y * share;
						setSliderY();

						findFilesToShow();
					}
				}
				
				private void refreshSliderXWidth() {
					double share = childrenViewPort.getMaxWidth() / FILES_WIDTH;
					share = Math.min(share, 1.0);
					double width = share * SCROLL_TRACK_X_WIDTH;
					
					SLIDER_X_WIDTH = Math.max(width, 20.0);
					setSliderXWidth();
				}
				
				private void setSliderXWidth() {
					childrenSliderX.setMinWidth(SLIDER_X_WIDTH);
					childrenSliderX.setPrefWidth(SLIDER_X_WIDTH);
					childrenSliderX.setMaxWidth(SLIDER_X_WIDTH);
				}
				
				private void resize() {
					refreshSliderXWidth();
					MAX_SLIDER_X = SCROLL_TRACK_X_WIDTH - SLIDER_X_WIDTH;
					
					findViewPortMaxX();
					updateCurrentViewPortX();
					synchronized(MainController.this) {
						setCurrentViewPortX();
					}
					updateTargetSliderX();
					childrenSliderX.setTranslateX(TARGET_SLIDER_X);
					updateVisibleSliderX();
				}
				
				private void updateTargetSliderX() {					
					double share;
					if (VIEW_PORT_MAX_X == 0.0) {
						share = 0.0;
					}
					else {
						share = CURRENT_VIEW_PORT_X / VIEW_PORT_MAX_X;
					}
					
					TARGET_SLIDER_X = MAX_SLIDER_X * share;
				}
				
				private void updateCurrentViewPortX() {
					if (CURRENT_VIEW_PORT_X>VIEW_PORT_MAX_X) {
						CURRENT_VIEW_PORT_X = VIEW_PORT_MAX_X;
					}
				}
				
				private void dragX() {
					findViewPortMaxX();
					findCurrentViewPortX();
					setCurrentViewPortX();
					updateVisibleSliderX();
				}
				
				private void findViewPortMaxX() {
					VIEW_PORT_MAX_X = Math.max((FILES_WIDTH - VIEW_PORT_WIDTH), 0.0);
				}
				
				private void findCurrentViewPortX() {
					double share = 0.0;
					if(MAX_SLIDER_X!=0) {
						share = TARGET_SLIDER_X / MAX_SLIDER_X;
					}
					
					CURRENT_VIEW_PORT_X = VIEW_PORT_MAX_X * share;
				}
				
				private void setCurrentViewPortX() {
					
					synchronized (guiMon) {
						if(ChildrenFiles.TableView.Header.header != null) {
							ChildrenFiles.TableView.Header.header.setTranslateX(-CURRENT_VIEW_PORT_X);
						}
						if (childrenBox.getChildren().size() == 0) {
							return;
						}

						for (Node node : childrenBox.getChildren()) {
							node.setTranslateX(-CURRENT_VIEW_PORT_X);
						}
					}
					
				}
				
				private void updateVisibleSliderX() {
					if(VIEW_PORT_MAX_X == 0.0 && childrenScrollTrackX.isVisible()) {
						childrenScrollTrackX.setVisible(false);
					}
					else if(VIEW_PORT_MAX_X > 0.0 && !childrenScrollTrackX.isVisible()) {
						childrenScrollTrackX.setVisible(true);
					}
				}
				
				private void clickOnScrollTrackY(MouseEvent click) {
					findSliderYAfterClick(click);
					setSliderY();
					findFilesToShow();
				}
				
				private void findSliderYAfterClick(MouseEvent click) {
					double clickY = click.getY();
					
					if (clickY<TARGET_SLIDER_Y) {
						scrollYOnValue(-(VIEW_PORT_HEIGHT-HEADER_HEIGHT*2));
					}
					else if(clickY>TARGET_SLIDER_Y + SLIDER_Y_HEIGHT) {
						scrollYOnValue(VIEW_PORT_HEIGHT-HEADER_HEIGHT*2);
					}
				}
				
				private void scrollYOnValue(double viewPortHeight) {
					double tempY = CURRENT_VIEW_PORT_Y + viewPortHeight;
					tempY = Math.min(Math.max(tempY, 0.0), VIEW_PORT_MAX_Y);
					
					double share = tempY/VIEW_PORT_MAX_Y;
					TARGET_SLIDER_Y = share * MAX_SLIDER_Y;
				}
				
				private void clickOnScrollTrackX(MouseEvent click) {
					double clickX = click.getX();
					if(clickX < TARGET_SLIDER_X) {
						scrollXOnValue(-VIEW_PORT_WIDTH);
					}
					else if(clickX > TARGET_SLIDER_X + SLIDER_X_WIDTH) {
						scrollXOnValue(VIEW_PORT_WIDTH);
					}
				}
				
				private void scrollXOnValue(double viewPortWidth) {
					double tempX = CURRENT_VIEW_PORT_X + viewPortWidth;
					CURRENT_VIEW_PORT_X = Math.max(Math.min(tempX, VIEW_PORT_MAX_X), 0.0);
					
					double share = CURRENT_VIEW_PORT_X / VIEW_PORT_MAX_X;
					TARGET_SLIDER_X = share * MAX_SLIDER_X;
					
					setCurrentViewPortX();
					childrenSliderX.setTranslateX(TARGET_SLIDER_X);
				}
				
				private void showHeaderFromStart() {
					ChildrenFiles.TableView.Header.header.setTranslateX(CURRENT_VIEW_PORT_X = 0.0);
					childrenSliderX.setTranslateX(TARGET_SLIDER_X = 0.0);
				}
			}
		}
		
		public class Select{
			
			private static int FIRST_SELECTED_FILE = -1;
			private static int LAST_SELECTED_FILE = -1;
			private DirectoryFile firstSelectedFileObj = new DirectoryFile();
			HashSet<NodeInfo> selectedFiles = new HashSet<NodeInfo>();
			
			private void findFilesToSelect(DirectoryFile df) {
				if (isShiftDown) {
					if(FIRST_SELECTED_FILE == -1) {
						selectOneFile(df);
					}
					else {
						findLastSelectedFile(df);
					}
				}
				else {
					FIRST_SELECTED_FILE = -1;
					selectOneFile(df);
				}
			}
			
			private void selectOneFile(DirectoryFile df) {
				
				selectedFiles.clear();
				List<DirectoryFile> showedChildrenFiles = logic.getshowedChildrenFiles();
				FIRST_SELECTED_FILE = showedChildrenFiles.indexOf(df);
				LAST_SELECTED_FILE = -1;
				firstSelectedFileObj = df;
				selectedFiles.add(new NodeInfo(df));
				
			}
			
			private void findLastSelectedFile(DirectoryFile df) {
				List<DirectoryFile> showedChildrenFiles = logic.getshowedChildrenFiles();
				LAST_SELECTED_FILE = showedChildrenFiles.indexOf(df);
				if(LAST_SELECTED_FILE != -1) {
					
					if(FIRST_SELECTED_FILE <= LAST_SELECTED_FILE) {
						selectFilesRange(FIRST_SELECTED_FILE, LAST_SELECTED_FILE, showedChildrenFiles);
					}
					else {
						selectFilesRange(LAST_SELECTED_FILE, FIRST_SELECTED_FILE, showedChildrenFiles);
					}
				}
			}
			
			private void selectFilesRange(int first, int last, List<DirectoryFile> showedChildrenFiles) {
				selectedFiles.clear();
				selectedFiles.addAll(showedChildrenFiles.subList(first, last+1).stream().map(NodeInfo::new).collect(Collectors.toSet()));
				
			}
			
			public void setSelectedStatus(HBox container) {
				ChildrenContainerFactory.setChildrenContainerStatus(container, ChildrenContainerFactory.selectedStatus);
				
			}
			
			public void setStandartStatus(HBox container) {
				ChildrenContainerFactory.setChildrenContainerStatus(container, ChildrenContainerFactory.standartStatus);
				
			}
			
			public void changeFirstAndLastIndexesAfterDelete() {
				if (selectedFiles.size() == 0) {
					FIRST_SELECTED_FILE = LAST_SELECTED_FILE = -1;
					firstSelectedFileObj = new DirectoryFile();
				}
				else {
					FIRST_SELECTED_FILE = logic.getshowedChildrenFiles().indexOf(firstSelectedFileObj);
				}
			}
			
			public void changeFirstAndLastIndexesAfterAdd() {
				if(selectedFiles.size() == 0) {
					return;
				}
				else {
					FIRST_SELECTED_FILE = logic.getshowedChildrenFiles().indexOf(firstSelectedFileObj);
				}
			}
			
			private void resetSelectionProperties() {
				selectedFiles.clear();
				FIRST_SELECTED_FILE = -1;
				LAST_SELECTED_FILE = -1;
			}
		}
		
		class Delete{
			
			private void deleteAction() {
				List<MyFile> filesToDelete = getFilesToDelete();
				if (filesToDelete == null) {
					return;
				}
				
				boolean decision = false;
				decision = getDeleteDecision(filesToDelete);
				if(decision) {
					moveToTrash(filesToDelete);
				}
			}

			private List<MyFile> getFilesToDelete() {
				List<MyFile> filesToDelete;
				synchronized(MainController.this) {
					if(select.selectedFiles==null||select.selectedFiles.size()==0) {
						return null;
					}
					filesToDelete = select.selectedFiles.stream().map(MyFile::new).collect(Collectors.toList());
				}
				return filesToDelete;
			}

			private boolean getDeleteDecision(List<MyFile> filesToDelete) {
				boolean decision;
				if(filesToDelete.size()==1) {
					decision = getOneFileDeleteDecision(filesToDelete.getFirst());
				}
				else {
					decision = alert.showAlertConfirmation("Удаление", null, "Переместить в корзину выбранные файлы " + filesToDelete.size() + " шт.");
				}
				return decision;
			}

			private boolean getOneFileDeleteDecision(MyFile del) {
				boolean decision = false;
				if(del.isDirectory()) {
					decision = alert.showAlertConfirmation("Удаление", null, "Переместить в корзину папку " + del.getName());
				}
				else {
					decision = alert.showAlertConfirmation("Удаление", null, "Переместить в корзину файл " + del.getName());
				}
				
				return decision;
			}
			
			private void moveToTrash(List<MyFile> filesToRemove) {
				Desktop desktop = Desktop.getDesktop();
				List<MyFile> notRemoved = new ArrayList<MyFile>();
				for(MyFile file : filesToRemove) {
					if(file.exists()) {
						if(!desktop.moveToTrash(file)) {
							notRemoved.add(file);
						}
					}
				}
			}
		}
	}
	
	class FileCreator implements Runnable{
		boolean isStarted = false;
		Thread fileMaker;
		private final static String FOLDER_NAME = "E:\\FF\\3";
		FileCreator(){
			Runtime.getRuntime().addShutdownHook(new Thread(()->{
				clearDir();
			}, "Runtime hook file creator"));
		}
		private void action() {
			if(!isStarted) {
				startThread();
			}
			else {
				stopThread();
			}
		}
		private void startThread() {
			fileMaker = new Thread(this, "File creator");
			fileMaker.setDaemon(true);
			File folder = new File(FOLDER_NAME);
			folder.mkdirs();
			isStarted = true;
			fileMaker.start();
			
		}
		
		private void stopThread() {
			if(fileMaker!=null) {
				fileMaker.interrupt();
			}
			
		}
		
		private void clearDir() {
			File folder = new File(FOLDER_NAME);
			if(!folder.exists()||!folder.isDirectory()||folder.list().length==0) {
				return;
			}
			try {
				Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>(){
					long s = 125;
					int n =1;
					 @Override
					    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					        throws IOException
					    {
					       file.toFile().delete();
						if (n % 20 == 0) {
							try {
								Thread.sleep(s);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								s = 1;
							}
						}
					      
					       
					        return FileVisitResult.CONTINUE;
					    }
					 
					 @Override
					    public FileVisitResult visitFileFailed(Path file, IOException exc)
					        
					    {
						 return FileVisitResult.CONTINUE;
					    }

					    @Override
					    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					        
						{
						if (!dir.toFile().equals(folder)) {
							dir.toFile().delete();

						}
						
						return FileVisitResult.CONTINUE;
					}
					 
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void run() {
			while(!Thread.interrupted()||!isAppClosing) {
				for(int i = 0; i<15; i++) {
					File file = new File(FOLDER_NAME + File.separator + "test_" + i + ".tmp");
					if(file.exists()) {
						continue;
					}
					else {
						try {
							file.createNewFile();
						} catch (IOException e) {
							continue;
						}
					}
					
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					
				}
				clearDir();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
			
			clearDir();
			synchronized(createFiles) {
				isStarted = false;
			}
		}
	}
	
	class WindowAlert {

		public boolean showAlertConfirmation(String title, String header, String context) {
			Window owner = filesTreeBox.getScene().getWindow();
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.initOwner(owner);
			alert.setHeaderText(header);
			alert.setTitle(title);
			alert.setContentText(context);
			ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
			if (result == ButtonType.OK) {
				return true;
			}

			else {
				return false;
			}
		}

	}
	
	public class ControllerData {
		
		public boolean isShiftDown() {
			return isShiftDown;
		}
		
		public int getFirstSelectedFile() {
			return firstSelectedFileTree;
		}
		
		public void setFirstSelectedFie(int f) {
			firstSelectedFileTree = f;
		}
		
		public int getLastSelectedFile() {
			return lastSelectedFileTree;
		}
		
		public void setLastSelectedFile(int l) {
			lastSelectedFileTree = l;
		}
		
		public Logic getLogic() {
			return logic;
		}
		
		public FileTree getFileTree() {
			return fileTree;
		}
		
		public CopyOnWriteArrayList<WeakReference<Thread>> getRunnedThreads(){
			return runnedThreads;
		}
		
		public DirectoryFile getOpenedInChildrenFiles() {
			return ChildrenFiles.openedInChildrenFiles;
		}
		
		public void setOpenedInChildrenFiles(DirectoryFile opened) {
			ChildrenFiles.openedInChildrenFiles = opened;
			
			Platform.runLater(()->{
				if (opened == null) {
					textPath.setText("");
				} else {
					textPath.setText(opened.toString());
				}
			});
			synchronized (MainController.this) {
				upClass.checkUpEnabled();
			}
		}
		
		public ChildrenFiles getChildrenFiles() {
			return childrenFiles;
		}
		
		public boolean appClosing() {
			return isAppClosing;
		}
		
		public void setAppClosing(boolean c) {
			isAppClosing = c;
		}
	}

}
