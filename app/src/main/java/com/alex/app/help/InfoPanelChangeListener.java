package com.alex.app.help;

import java.lang.ref.WeakReference;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;

public class InfoPanelChangeListener {
	Scene scene;
	WeakReference<HBox> ref;
	
	
	public InfoPanelChangeListener(Scene s, WeakReference<HBox> r, double delta){
		scene = s;
		ref = r;
		scene.widthProperty().addListener(new WidthListener());
		scene.heightProperty().addListener(new HeightListener(delta));
		
	}
	
	class WidthListener implements ChangeListener<Number> {
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			var infoBox = ref.get();
			if(infoBox == null) {
				scene.widthProperty().removeListener(this);
				return;
			}
			
			double windowWidth = newValue.doubleValue();
			double width = infoBox.getMaxWidth();
			double translateX = (windowWidth - width)/2.0;
			infoBox.setTranslateX(translateX);

		}
	}
	/*
	 * так как infoBox устанавливается в mainWorkflow а не в root,
	 * надо учесть высоту элементов над ним для центровки
	 * delta = infoPanel.getMaxHeight() + mainMenu.getHeight()
	 */
	class HeightListener implements ChangeListener<Number> {
		double delta = 0.0;
		HeightListener(double d){
			delta = d;
		}
		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			var infoBox = ref.get();
			if(infoBox == null) {
				scene.heightProperty().removeListener(this);
				return;
			}
			
			double windowHeight = newValue.doubleValue() - delta;
			double height = infoBox.getMaxHeight();
			double translateY = (windowHeight - height - delta)/2.0;
			infoBox.setTranslateY(translateY);

		}
	}
	
	
}
