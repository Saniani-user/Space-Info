package com.alex.app;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alex.app.data.FileRepository;
import com.alex.app.help.ChildrensExtractor;
import com.alex.app.help.DirectoryFile;
import com.alex.app.help.MyFile;
import com.alex.app.help.NodeInfo;
import com.alex.app.help.RefreshViewPort;
import com.alex.app.help.RepositoryDeliver;
import com.alex.app.help.TypeFile;
import com.alex.controller.MainController;


public class Logic {
	public FileRepository repository;
	public RefreshingFiles refreshingFiles;
	public DeleteFile deleteFile;
	private MainController mainController;
	private ChildrensExtractor extractor;
	public UpdateFileTree updateFileTree;
	public MySQL mySQL;
	public static final String SEPARATOR = File.separator;
	public static final String RAM = "ram";
	
	private List<DirectoryFile> roots;
	private List<NodeInfo> showedTree = new ArrayList<NodeInfo>();
	private List<File> deniedFolders;
	private List<DirectoryFile> showedChildrenFiles = new ArrayList<DirectoryFile>();
	private List<DirectoryFile> showedRoots;
	
	List<Runnable> guiTasks = new ArrayList<Runnable>();
	
	private HashMap<String, DirectoryFile> chainMap;
	
	
	public LogService logService = new LogService();
	
	
	public Logic() {
		
	}
	
	public void initLogic() {
		RepositoryDeliver repoDeliver = SpringContext.getBean(RepositoryDeliver.class);
		setRepository(repoDeliver.repository);
		refreshingFiles = new RefreshingFiles();
		deleteFile = new DeleteFile();
		showedTree = new ArrayList<NodeInfo>();
		showedChildrenFiles = new ArrayList<DirectoryFile>();
		deniedFolders = new ArrayList<File>();
		chainMap = new HashMap<String, DirectoryFile>();
		showedRoots = new ArrayList<DirectoryFile>();
		getRootsData();
		mySQL = new MySQL();
	}
	
	public void setMainController(MainController mc) {
		mainController = mc;
	}
	
	public MainController getMainController() {
		return mainController;
	}
	
	public void setExtractor(ChildrensExtractor extractor) {
		this.extractor = extractor;
	}
	
	public void setRepository (FileRepository r) {
		repository = r;
	}
	
	public List<File> getDeniedFolders(){
		return deniedFolders;
	}
	
	public void startUpdateFileTree() {
		updateFileTree = new UpdateFileTree();
		updateFileTree.updateFileTreeThread.start();
	}
	
	public void getRootsData() {		
		initRoots();
	}
	
	public void putInMap(DirectoryFile df) {
		synchronized (chainMap) {
			chainMap.put(df.toString(), df);
		}
	}
	
	private void initRoots() {
		roots = new ArrayList<DirectoryFile>();
		File [] rootsMassive = File.listRoots();
		for (File r : rootsMassive) {
			r = r.toPath().toFile();
			DirectoryFile df = new DirectoryFile(new MyFile(r), TypeFile.root);
//			df.setRoot(r);
			roots.add(df);
			repository.save(df);
			putInMap(df);
		}
	}
	
	public List<DirectoryFile> getRoots(){
		return roots;
	}
	
	public List<NodeInfo> getDirectories() {
		return showedTree;
	}
	
	public void addShowedFile (DirectoryFile df) {
		showedTree.add(new NodeInfo(df));
	}
	
	public boolean removeShowedFile( DirectoryFile df) {
		return showedTree.remove(new NodeInfo(df));
	}
	
	public List<NodeInfo> getShowedFiles(){
		return showedTree;
	}
	
	public ArrayList<NodeInfo> getCopyShowedTree() {
		synchronized(mainController) {
			return new ArrayList<NodeInfo>(showedTree);
		}
	}
	
	public void setShowedTree(Collection<? extends NodeInfo> showedFiles) {
		synchronized(mainController) {
			this.showedTree = new ArrayList<NodeInfo>(showedFiles);
		}
		
	}
	
	public int getShowedTreeSize() {
		synchronized(mainController) {
			return showedTree.size();
		}
	}
	
	public void setShowedChildrenFiles(List<DirectoryFile> files) {
		
		showedChildrenFiles = new ArrayList<DirectoryFile>(files);
	}
	
	public void updateChildrenFiles(DirectoryFile opened) {
		if(opened == null) {
			return;
		}
		List<DirectoryFile> childrens = repository.findByParentIdOrderByDirectoryDescNameIgnoreCaseAsc(opened.getId());
		setShowedChildrenFiles(childrens);
	}
	
	public List<DirectoryFile> getshowedChildrenFiles(){
		return showedChildrenFiles;
	}
	
	public int getshowedChildrenFilesSize() {
		synchronized(mainController) {
			return showedChildrenFiles.size();
		}
	}
	
	public List<DirectoryFile> getShowedRoots() {
		return showedRoots;
	}
	
	
	public List<DirectoryFile> getShowedTree() {

		if (MainController.FileTree.ScrollPanel.FIRST_FILE_INDEX == -1) {
			return new ArrayList<DirectoryFile>();
		}
		int showedFilesSize = 0;
		synchronized (mainController) {
			showedFilesSize = showedTree.size();
			
			if (MainController.FileTree.ScrollPanel.LAST_FILE_INDEX == -1) {
				return getAllShowedTree();
			}
			else if(showedFilesSize != 0){
				
				return getPartShoweTree();
			}
			else {
				return new ArrayList<DirectoryFile>();
			}
		}
	}

	private List<DirectoryFile>  getPartShoweTree() {
		
		int last = MainController.FileTree.ScrollPanel.LAST_FILE_INDEX >= showedTree.size() ? last = showedTree.size()
				: MainController.FileTree.ScrollPanel.LAST_FILE_INDEX;
		List<NodeInfo> showed;
		showed = showedTree.subList(MainController.FileTree.ScrollPanel.FIRST_FILE_INDEX, last);

		return mySQL.getShowedFromRepository(showed);
	}

	private List<DirectoryFile> getAllShowedTree() {
		
		List<NodeInfo> showed;
		synchronized (mainController) {
			showed = new ArrayList<NodeInfo>(showedTree.subList(MainController.FileTree.ScrollPanel.FIRST_FILE_INDEX, showedTree.size()));
		}
		return mySQL.getShowedFromRepository(showed);
	}
	
	
	public void addInShowedTree(DirectoryFile parent, List<DirectoryFile> allChildrens, List<MyFile> filesToAddInGUI) {
		if(filesToAddInGUI.size()==0) {
			return;
		}
		
		if(parent == null || allChildrens == null) {
			getParentAndChildrensFromSQL(filesToAddInGUI);
			
		}
		else {
			findParentIndexAndPaste(parent, allChildrens);
		}
	}

	private void getParentAndChildrensFromSQL(List<MyFile> filesToAddInGUI) {
		DirectoryFile parentFile = mySQL.getFile(filesToAddInGUI.getFirst().getParent().toString());
		if (parentFile == null) {
			return;
		}
		List<DirectoryFile> childrens = mySQL.getByParentOrderedByNameAsc(parentFile.getId());
		
		findParentIndexAndPaste(parentFile, childrens);
	}
	/**
	 * @param filesToAddInGUI, новые вложенные файлы первого уровня.
	 * добавляемые в родительскую папку, с учётом уже открытых 
	 * в ней папок.
	 */
	public void findParentIndexAndPaste(DirectoryFile parent, List<DirectoryFile> allChildrens) {
		synchronized (mainController) {
			int index = ChildrensExtractor.COPY_SHOWED_FILES.indexOf(new NodeInfo(parent));
			if (index == -1) {
				return;
			}
			index++;
			pasteChildrensByIndex(allChildrens, index);
		}
	}

	private void pasteChildrensByIndex(List<DirectoryFile> childrens, int index) {
		HashSet<NodeInfo> showedFilesSet = new HashSet<NodeInfo>(ChildrensExtractor.COPY_SHOWED_FILES);
		
		for (int i = 0; i < childrens.size(); i++) {
			DirectoryFile childDF = childrens.get(i);
			
			NodeInfo child = new NodeInfo(childDF);
			if (showedFilesSet.contains(child)) {
				index = findNextIndex(childDF, index);
				continue;
				
			} else {
				ChildrensExtractor.COPY_SHOWED_FILES.add(index++, child);
			}
		}
	}
	/**
	 * принимает дочерний элемент и его индекс
	 */
	private int findNextIndex(DirectoryFile childDF, int index) {
		if(childDF.isDirectory()&&childDF.isOpen()) {
			
			List<DirectoryFile> childrens = repository.findByParentIdOrderByDirectoryDescNameIgnoreCaseAsc(childDF.getId());
			
			if(childrens==null||childrens.size()==0) {
				return ++index;
			}
			else{
				return findNextIndex(childrens.getLast(), index + childrens.size());
			}
		}
		else {
			return ++index;
		}
	}
	
	public double getLeftMargin(String fullPath) {
		String[] separated = getSeparated(fullPath);
		double leftMargin = 0;
		if(separated.length>2) {
			leftMargin = (separated.length-2)*35;
		}
		return leftMargin;
	}
	
	public String[] getSeparated(String pathName) {
		String[] separated;
		if(SEPARATOR.equals("\\")){
			separated = pathName.split("\\\\");
		}
		else {
			separated = pathName.split(SEPARATOR);
		}
		return separated;
	}
	
	public void calculateRootMap(File fRoot, DirectoryFile dfRoot) {
		Path root = fRoot.toPath();
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				int i = 0;
				Stack<DirectoryFile> stack = new Stack<DirectoryFile>();
				@Transactional
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					System.out.println(++i);
					if(Thread.interrupted()||mainController.controllerData.appClosing()) {
						Thread.currentThread().interrupt();
						return FileVisitResult.TERMINATE;
					}
					
					File fDir = dir.toFile();
					
					if (dir == root) {
						stack.push(dfRoot);
						addFiles(dfRoot);
						return FileVisitResult.CONTINUE;
//						repository.save(dfRoot);
					}
					
					else if(fDir.isDirectory()) {
						DirectoryFile df = new DirectoryFile(new MyFile(fDir), TypeFile.dir);
						DirectoryFile parent = repository.findByName(fDir.getParentFile().toString());
						
						if(parent==null) {
							System.out.println("Parent is null " + fDir);
						}
						else {
							parent.recoveryFromSQL();
							df.setParent(parent);
							
							stack.push(df);
							
							df = repository.save(df);
							addFiles(df);
						}
						
						
					}
					
					return FileVisitResult.CONTINUE;
				}
				
				FileFilter filterFile = (file)->{
					if (file.isFile()) {
						return true;
					}
					else {
						return false;
					}
				};
				
				private void addFiles(DirectoryFile df) {
					File [] listFiles = df.getThisFile().listFiles(filterFile);
					List<DirectoryFile> childrens = new ArrayList<DirectoryFile>();
					for (File file : listFiles) {
						DirectoryFile children = new DirectoryFile(new MyFile(file), TypeFile.file);
						children.setParent(df);
						
						children.increaseSize(file.length());
						df.increaseSize(file.length());
						
						childrens.add(children);
						if(Thread.interrupted()||mainController.controllerData.appClosing()) {
							Thread.currentThread().interrupt();
							return;
						}
					}
					repository.saveAll(childrens);
				}

				
				@Override
			    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

					if(Thread.interrupted()||mainController.controllerData.appClosing()) {
						Thread.currentThread().interrupt();
						return FileVisitResult.TERMINATE;
					}
			        
					if (exc != null) {
						return FileVisitResult.CONTINUE;
					}
					
					DirectoryFile finishFolder = stack.pop();
					
			        if(!stack.empty()) {
			        	DirectoryFile parent = stack.peek();
			        	parent.increaseSize(finishFolder.getFileSize());
			        }
			        repository.save(finishFolder);
			        return FileVisitResult.CONTINUE;
			    }
				
				@Override
			    public FileVisitResult visitFileFailed(Path file, IOException exc) {
					
					if (exc instanceof AccessDeniedException) {
						String text = file.toString() + "\n";
						logService.writeLog(text);
						deniedFolders.add(file.toFile());
						return FileVisitResult.CONTINUE;
					}
					
					return FileVisitResult.CONTINUE;
				}
				
			});
		} catch (IOException e) {
			
		}
		if(Thread.interrupted()||mainController.controllerData.appClosing()) {
			Thread.currentThread().interrupt();
			return;
		}
	}
	
	public void clearAllRoots() {
		for(DirectoryFile root : roots) {
			repository.deleteAllChildrens(root.toString());
			root.setScanned(false);
		}
	}
	
	
	public class RefreshingFiles {
		
		public void tryAddToMap(File fRoot){	
			DirectoryFile parent = mySQL.getFile(fRoot.getParentFile().toString());
			List<DirectoryFile> saveList = new ArrayList<DirectoryFile>();
			if(deniedFolders.indexOf(fRoot.toPath().toFile())!=-1 || parent == null) {
				return;
			}
			if (fRoot.isDirectory()) {
				addFolder(fRoot, parent);
				return;
			}
			
			addFile(fRoot, parent, saveList);
			saveList.add(parent);
			mySQL.saveEntity(saveList);
//			
		}

		private void addFile(File fRoot, DirectoryFile parent, List<DirectoryFile> saveList) {
			DirectoryFile file = new DirectoryFile(new MyFile(fRoot), TypeFile.file);
//			
			file.setParent(parent);
			file.increaseSize((double)fRoot.length());
			parent.increaseSize(file.getFileSize());
			saveList.add(file);
			
//			parent.addChildrenFile(fRoot);
		}

		private void addFolder(File fRoot, DirectoryFile parent) {
			Path root = fRoot.toPath();
			try {
				Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
					Stack<DirectoryFile> stack = new Stack<DirectoryFile>();
					@Transactional
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {

						if (Thread.interrupted()||mainController.controllerData.appClosing()) {
							Thread.currentThread().interrupt();
							return FileVisitResult.TERMINATE;
						}

						File fDir = dir.toFile();

						if (dir == root) {
							var dfRoot = new DirectoryFile(new MyFile(fDir), TypeFile.dir);
							dfRoot.setParent(parent);
							dfRoot = repository.save(dfRoot);							
							stack.push(dfRoot);
							addFiles(dfRoot);
							return FileVisitResult.CONTINUE;
//							repository.save(dfRoot);
						}

						else if (fDir.isDirectory()) {
							DirectoryFile df = new DirectoryFile(new MyFile(fDir), TypeFile.dir);
//							df.setRoot(fRoot);
							DirectoryFile parent = repository.findByName(fDir.getParentFile().toString());

							if (parent == null) {
								System.out.println("Parent is null " + fDir);
							} else {
								parent.recoveryFromSQL();
								df.setParent(parent);
								df = repository.save(df);
								stack.push(df);
								addFiles(df);
							}
							
						}

						return FileVisitResult.CONTINUE;

					}
					
					private void addFiles(DirectoryFile parent) {
						File[] listFiles = parent.getThisFile().listFiles();
						List<DirectoryFile> saveFiles = new ArrayList<DirectoryFile>();
						for (File file : listFiles) {
							if(file.isFile()) {
								DirectoryFile children = new DirectoryFile(new MyFile(file), TypeFile.file);
								children.setParent(parent);
								children.increaseSize(file.length());
								parent.increaseSize(children.getFileSize());
								saveFiles.add(children);
							}
						}
						repository.saveAll(saveFiles);
					}
					
					@Override
				    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

						if(Thread.interrupted()||mainController.controllerData.appClosing()) {
							Thread.currentThread().interrupt();
							return FileVisitResult.TERMINATE;
						}
				        
						if (exc != null) {
							return FileVisitResult.CONTINUE;
						}
						
						DirectoryFile finishFolder = stack.pop();
						
						if (!stack.empty()) {
							DirectoryFile parent = stack.peek();
							parent.increaseSize(finishFolder.getFileSize());
						}
						else {
							parent.increaseSize(finishFolder.getFileSize());
						}
				        repository.save(finishFolder);
				        
				        return FileVisitResult.CONTINUE;
				    }
					
					@Override
				    public FileVisitResult visitFileFailed(Path file, IOException exc) {
						
						if (exc instanceof AccessDeniedException) {
							String text = file.toString() + "\n";
							logService.writeLog(text);
							deniedFolders.add(file.toFile());
							return FileVisitResult.CONTINUE;
						}
						
						return FileVisitResult.CONTINUE;
					}
					
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			repository.save(parent);
		}
		
		private void refreshRoots() {
			File[] roots = File.listRoots();
			
			if(roots==null) {
				return;
			}
			
			findNewRoot(roots);
		}

		
		private void findNewRoot(File[] roots) {
			List<File> rootList = null;
			List<NodeInfo> copyShowedTree = null;
			synchronized(mainController) {
				rootList = new ArrayList<File>(Arrays.asList(roots));
				copyShowedTree = getCopyShowedTree();
			}
			
			rootList.sort(Comparator.comparing(File::toString));
			getRootLists(rootList, copyShowedTree);
		}
		
		private void getRootLists(List<File> currentRoots, List<NodeInfo> copyShowedTree) {
			List<DirectoryFile> oldRoots = mySQL.getRoots();
			List<DirectoryFile> newRootsDF = getOnlyNewRoots(oldRoots, currentRoots);
			if(newRootsDF.size()==0) {
				return;
			}
			mySQL.saveEntity(newRootsDF);
			refreshGUI(copyShowedTree, oldRoots);
		}

		private void refreshGUI(List<NodeInfo> copyShowedTree, List<DirectoryFile> oldRoots) {
			addRootsInGUI(oldRoots, copyShowedTree);
			synchronized(mainController) {
				setShowedTree(copyShowedTree);
				mainController.controllerData.getFileTree().refreshFileTreeBox();
			}
		}
		
		@SuppressWarnings("unlikely-arg-type")
		private List<DirectoryFile> getOnlyNewRoots(List<DirectoryFile> oldRoots, List<File> currentRoots) {
			List<DirectoryFile> onlyNewRoots = new ArrayList<DirectoryFile>();
			for(File root : currentRoots) {
				MyFile r = new MyFile(root);
				if(oldRoots.contains(r)) {
					continue;
				}
				onlyNewRoots.add(new DirectoryFile(new MyFile(root), TypeFile.root));
			}
			return onlyNewRoots;
		}

		@SuppressWarnings("unlikely-arg-type")
		private void addRootsInGUI(List<DirectoryFile> oldRoots, List<NodeInfo> copyShowedTree) {
			List<DirectoryFile> newRootsDF = mySQL.getRoots();
			HashSet<NodeInfo> copyFileTreeSet = new HashSet<NodeInfo>(copyShowedTree);
			
			for (int i = 0; i<newRootsDF.size(); i++) {
				var root = newRootsDF.get(i);
				if(copyFileTreeSet.contains(root)) {
					continue;
				}
				
				int index = 0;
				if(i!=0) {
					var upperRoot = newRootsDF.get(i-1);
					index = copyShowedTree.indexOf(upperRoot)+1;
					
					if(upperRoot.isDirectory()&&upperRoot.isOpen()) {
						index += mySQL.getCountAllOpenedChildrensInTree(upperRoot);	
					}
					
				}
				
				copyShowedTree.add(index, new NodeInfo(root));
			}
		}
		
	}
	
	
	
	
	public class UpdateFileTree implements Runnable{
		private int countForGC = 1;
		
		public final static String THREAD_NAME = "Update file tree";
		Thread updateFileTreeThread;
		UpdateFileTree(){
			updateFileTreeThread = new Thread(this, THREAD_NAME);
			
			mainController.controllerData.getRunnedThreads().add(new WeakReference<Thread>(updateFileTreeThread));
		}
		@Override
		public void run() {
			
			while (!Thread.interrupted()||!mainController.controllerData.appClosing()) {

					executeTasks();
					mainController.controllerData.getRunnedThreads().removeIf(ref->ref.get()==null);
					if(Thread.interrupted()||mainController.controllerData.appClosing()) {
						Thread.currentThread().interrupt();
						break;
					}
					
					if (countForGC == 51) {
						refreshingFiles.refreshRoots();
						executeTasks();
						copyShowedFiles();
						countForGC = 1;
					}
					
					if(Thread.interrupted()||mainController.controllerData.appClosing()) {
						Thread.currentThread().interrupt();
						break;
					}
					
					executeTasks();
					countForGC++;

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		
		private void copyShowedFiles() {
			if (MainController.FileTree.ScrollPanel.FIRST_FILE_INDEX == -1) {
				return;
			}
			List<DirectoryFile> viewPortFiles = getShowedTree();
			
			if(hasNextTask()||Thread.interrupted()||mainController.controllerData.appClosing()) {
				return;
			}
			refershFileTreeShowedFiles(viewPortFiles);
			if(hasNextTask()||Thread.interrupted()||mainController.controllerData.appClosing()) {
				return;
			}
			refreshOpenedInChildrenFiles();
		}
		
		private void refershFileTreeShowedFiles(List<DirectoryFile> viewPortFiles) {
			for (DirectoryFile file: viewPortFiles) {
				if(Thread.interrupted()||mainController.controllerData.appClosing()) {
					Thread.currentThread().interrupt();
					break;
				}
				if(file == null) {
					continue;
				}
				
				if(file.isDirectory()) {
					
					if(file.isOpen()) {
						extractor.getNewChildrenDirectoryFiles(file, RefreshViewPort.FILE_TREE);
						
					}
					else {
						extractor.autoCheckExists(file);
					}
					
				}
				
				else if(file.isRegularFile()) {
					extractor.autoCheckExists(file);
				}
			}			
		}
		
		private void refreshOpenedInChildrenFiles() {

			if(mainController.controllerData.getOpenedInChildrenFiles()!=null) {
				DirectoryFile openedInChildrenFiles = mainController.controllerData.getOpenedInChildrenFiles();
				Optional<DirectoryFile> optional = repository.findById(openedInChildrenFiles.getId());
				optional.ifPresent(file->{
					extractor.getNewChildrenDirectoryFiles(file, RefreshViewPort.CHILDREN_FILES);
				});
			}
		}
		
		public void addTask(Runnable task) {
			synchronized(guiTasks) {
				if(guiTasks.size()>100) {
					return;
				}
				guiTasks.add(task);
			}
		}
		
		public boolean hasNextTask() {
			synchronized (guiTasks) {
				return guiTasks.size()>0 ? true : false;
			}
		}
		
		private Runnable getTask() {
			synchronized(guiTasks) {
				return hasNextTask() ? guiTasks.getFirst() : null;
			}
		}
		
		private void removeTask(Runnable task) {
			synchronized(guiTasks) {
				guiTasks.remove(task);
			}
		}
		
		private void executeTasks() {
			while(hasNextTask()&&(!Thread.interrupted()||!mainController.controllerData.appClosing())) {
				Runnable task = getTask();
				task.run();
				removeTask(task);
			}
		}
		
	}
	
	public class MySQL{
		public void saveEntity(DirectoryFile df) {
			repository.save(df);
		}
		
		public void saveEntity(List<DirectoryFile> files) {
			repository.saveAll(files);
		}
		
		public List<DirectoryFile> getRoots() {
			return repository.findByRootDirectoryTrueOrderByNameAsc();
		}
		
		public List<DirectoryFile> getAllById(List<Long> ids){
			return repository.findAllById(ids);
		}
		
		private List<DirectoryFile> getShowedFromRepository(List<NodeInfo> showed) {
			List<DirectoryFile> entities;
			List<Long> showedId = showed.stream().map(NodeInfo::getId).collect(Collectors.toList());
			entities = new ArrayList<DirectoryFile>(repository.findAllById(showedId));
			Map<Long, DirectoryFile> entitiesMap = entities.stream().collect(Collectors.toMap(DirectoryFile::getId, Function.identity()));
			
			return showedId.stream().map(entitiesMap::get).collect(Collectors.toList());
		}
		
		public DirectoryFile getFile(String file) {
			return repository.findByName(file.toString());
		}
		
		public List<DirectoryFile> getByParentOrderedByNameAsc(Long id) {
			return repository.findByParentIdOrderByDirectoryDescNameIgnoreCaseAsc(id);
		}
		
		public List<DirectoryFile> getByParentOrderedByNameDesc(Long id){
			return repository.findByParentOrderedByNameDesc(id);
		}
		
		public List<DirectoryFile> getByParentOrderedBySizeDesc(Long parentId){
			return repository.findByParentOrderedBySizeDesc(parentId);
		}
		
		public List<DirectoryFile> getByParentOrderedBySizeAsc(Long id){
			return repository.findByParentOrderedBySizeAsc(id);
		}
		
		public List<DirectoryFile> getByParentOrderedBySizeIgnoreDirectories(Long id){
			return repository.findByParentOrderedBySizeIgnoreDirectories(id);
		}
		
		public List<DirectoryFile> getByNameStartingWith(String prefix){
			return repository.findByNameStartingWith(prefix);
		}
		
		public void getAllOpenedChildrens(Long parentId, List<NodeInfo> remove, List<DirectoryFile> closed){
			List<DirectoryFile> childrens = repository.findByParentId(parentId);
			if(childrens == null || childrens.size() == 0) {
				return;
			}
			for(DirectoryFile children : childrens) {
				if(children.isDirectory()&&children.isOpen()) {
					getAllOpenedChildrens(children.getId(), remove, closed);
					children.setOpen(false);
					closed.add(children);
				}
				remove.add(new NodeInfo(children));
			}
		}
		
		public int getCountAllOpenedChildrensInTree(DirectoryFile file) {
			int count = 0;
			List<DirectoryFile> childrens = repository.findByParentId(file.getId());
			if (childrens == null || childrens.size() == 0) {
				return count;
			}
			count = childrens.size();
			for (DirectoryFile children : childrens) {
				if (children.isDirectory() && children.isOpen()) {
					count += getCountAllOpenedChildrensInTree(children);
				}
			}

			return count;
		}
		/**
		 * 
		 * childrens это вложенные папки одного уровня.
		 * Метод суммироет размер всех childrens, уменьшает на
		 * него размер всех родителей, удаляет все childrens из базы 
		 * и все вложенные в них файлы.
		 * 
		 * 
		 */
		
		
		public void removeFormSQLAndShowedFiles(List<DirectoryFile> childrens) {
			if(childrens.getFirst().isRoot()) {
				removeRootFromSQL(childrens.getFirst());
			}
			else {
				removeFileFromSQL(childrens);
			}
			
//			repository.deleteAllChildrens(parent.toString());
			
		}
		@SuppressWarnings("unlikely-arg-type")
		private void removeRootFromSQL(DirectoryFile root) {
			var opened = new ArrayList<NodeInfo>();
			getAllOpenedChildrens(root.getId(), opened, new ArrayList<DirectoryFile>());
		
			if (root.isOpen()) {
				LinkedHashSet<NodeInfo> showedSet = new LinkedHashSet<NodeInfo>(
						ChildrensExtractor.COPY_SHOWED_FILES);
				if (showedSet.contains(root)) {
					showedSet.removeAll(opened);
					showedSet.remove(root);
					ChildrensExtractor.COPY_SHOWED_FILES = new ArrayList<NodeInfo>(showedSet);
				}

			}
			repository.delete(root);
		}
		
		@SuppressWarnings("unlikely-arg-type")
		private void removeFileFromSQL(List<DirectoryFile> childrens) {
			decreaseSize(childrens);
			var opened = new ArrayList<NodeInfo>();
			var parent = repository.findByName(childrens.getFirst().getThisFile().getParent().toString());
			if(parent == null) {
				
				return;
			}
			for(DirectoryFile children : childrens) {
				getAllOpenedChildrens(children.getId(), opened, new ArrayList<DirectoryFile>());
			}
			
			
			if (parent.isOpen()) {
				LinkedHashSet<NodeInfo> showedSet = new LinkedHashSet<NodeInfo>(
						ChildrensExtractor.COPY_SHOWED_FILES);
				if (showedSet.contains(parent)) {
					showedSet.removeAll(opened);
					showedSet.removeAll(childrens);
					ChildrensExtractor.COPY_SHOWED_FILES = new ArrayList<NodeInfo>(showedSet);
					
				}

			}
			repository.deleteAll(childrens);
		}
		private void decreaseSize(List<DirectoryFile> childrens) {
			long size = 0l;
			for (DirectoryFile children : childrens) {
				size += children.getFileSize();
			}
			List<DirectoryFile> parents = repository.getAllParent(childrens.getFirst().getId());
			for(DirectoryFile parent : parents) {
				parent.recoveryFromSQL();
				parent.increaseSize(-size);
			}
			
			repository.saveAll(parents);
		}
		
		public void deleteAllChildrens(DirectoryFile df) {
			repository.deleteAllChildrens(df.toString());
		}
		
		public void setSizeFromSQL(DirectoryFile df) {
			DirectoryFile dfFromSQL = repository.findByName(df.getThisFile().toString());
			if(dfFromSQL!=null) {
				df.setFileSize(dfFromSQL.getFileSize());
			}
		}
		
		public void increaseAllParentAndThis(DirectoryFile file) {
			DirectoryFile fileFromSQL = repository.findByName(file.getThisFile().toString());
			List<DirectoryFile> allParent = repository.getAllParent(fileFromSQL.getId());
			double oldSize = file.getFileSize();
			double newSize = fileFromSQL.getFileSize();
			double deltaSize = newSize - oldSize;
			
			for(DirectoryFile parent : allParent) {
				parent.increaseSize(deltaSize);
			}
			repository.saveAll(allParent);
			file.setFileSize(newSize);
		}
	}
	
	public class DeleteFile {
		public void deleteFileFromDisk(DirectoryFile file) {
			
		}
		
		public void deleteDirectoryFromDisk(DirectoryFile directory) {
			
		}
	}
	
	class LogService{
		Path log;
		
		LogService(){
			log = Paths.get("log.txt");
			initLogFile();
		}
		
		protected void initLogFile() {
			try {
				Files.write(log, "".getBytes());
			} catch (IOException e) {
				
			}
		}
		
		protected void writeLog(String text) {
			try {
				Files.write(log, text.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (IOException e) {
			}
		}
	}
	
}
