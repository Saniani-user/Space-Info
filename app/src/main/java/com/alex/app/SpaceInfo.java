package com.alex.app;

import java.lang.ref.WeakReference;
import java.net.URL;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.alex.controller.MainController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication()
public class SpaceInfo extends Application {
	
	MainController mainController;
	FXMLLoader loader;
	static ConfigurableApplicationContext context;
	static String[] args;
	public static void main(String[] args) {
		
		SpaceInfo.args = args;
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		
		URL fxmlUrl = getClass().getResource("/fxml/MainWindow.fxml");
		loader = new FXMLLoader();
		Parent root = null;
		try {
			loader.setLocation(fxmlUrl);
			root = loader.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (root==null) {
			System.out.println("FXML не загрузился");
			Platform.exit();
			return;
		}
		
		buildAndShowStage(primaryStage, root);
		startSpring();
		
	}

	private void buildAndShowStage(Stage primaryStage, Parent root) {
		mainController = loader.getController();
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		primaryStage.setScene(scene);
		mainController.initAfterSetScene();
		primaryStage.setOnCloseRequest(_->{
			closeApp();
		});
		
		primaryStage.show();
	}
	
	private void startSpring() {
		Thread springThread = new Thread(()->{
			SpringApplication app = new SpringApplication(SpaceInfo.class);
			app.setWebApplicationType(WebApplicationType.SERVLET);
			context = app.run(args);
			if(!mainController.controllerData.appClosing()){
				mainController.initAfterStartSpring();
			}
			else {
				context.close();
			}
		}, "Start spring thread");
		springThread.setDaemon(true);
		springThread.start();
	}
	
	public void stop() {
		
	}
	
	private void closeApp() {
		mainController.controllerData.setAppClosing(true);
		stopAllThreads();
		if(context != null) {
			context.close();
		}
		else {
//			System.exit(0);
		}
		
	}
	
	private void stopAllThreads() {
		for(WeakReference<Thread> ref : mainController.controllerData.getRunnedThreads()) {
			Thread thread = ref.get();
			if(thread==null) {
				continue;
			}
			thread.interrupt();
			int n = 0;
			while (thread.isAlive()) {
				try {
					thread.join(5);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				n++;
				if (n>1000) {
					System.exit(n);
				}
			}
			
		}
	}
		
}
