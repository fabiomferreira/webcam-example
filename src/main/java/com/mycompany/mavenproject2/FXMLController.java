package com.mycompany.mavenproject2;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javax.imageio.ImageIO;
import com.github.sarxos.webcam.Webcam;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXMLController implements Initializable {
    
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private BufferedImage grabbedImage, originalImage, resizedImage;
    private Webcam webcam;
    private boolean stopCamera = false;
    Dimension[] nonStandardResolutions = new Dimension[] {
        WebcamResolution.PAL.getSize(),
        WebcamResolution.HD720.getSize(),
        WebcamResolution.XGA.getSize(),
        new Dimension(540, 720),
        new Dimension(1000, 500),
    };
    
//    @FXML
//    private Label label;
    
    @FXML
    private ImageView imgWebCamCapturedImage;
    
    @FXML
    private ComboBox cameraOptions;
    
    @FXML
    private ImageView webcam_picture;
    
    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        originalImage = webcam.getImage();
        resizedImage = originalImage.getSubimage(360, 0, 540, 720);
        Image image = SwingFXUtils.toFXImage(resizedImage, null);
        webcam_picture.setImage(image);
        ImageIO.write(resizedImage, "PNG", new File("rg.png"));
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createTopPanel();
//        webcam = Webcam.getWebcamByName("Microsoft® LifeCam Cinema(TM) 0");
//        webcam.open();
    }    

    private void startWebCamStream() {
        stopCamera  = false;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while(!stopCamera){
                    try {
                        grabbedImage = webcam.getImage();
                        if(grabbedImage != null) {

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    final Image mainiamge = SwingFXUtils
                                        .toFXImage(grabbedImage, null);
                                    imageProperty.set(mainiamge);
                                }
                            });
                            grabbedImage.flush();
                        }
                    } catch (Exception e) {
                    } finally{

                    }
                    Thread.sleep(100);
                }
                return null;
            }
            
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);
    }

    private void createTopPanel() {
        int webCamCounter = 0;
	ObservableList<WebCamInfo> options = FXCollections.observableArrayList();
        for(Webcam webcam:Webcam.getWebcams())
        {
                WebCamInfo webCamInfo = new WebCamInfo();
                webCamInfo.setWebCamIndex(webCamCounter);
                webCamInfo.setWebCamName(webcam.getName());
                options.add(webCamInfo);
                webCamCounter ++;
        }
        cameraOptions.setItems(options);
        cameraOptions.setPromptText("Escolha a camera");
        cameraOptions.getSelectionModel().selectedItemProperty().addListener(new  ChangeListener<WebCamInfo>() {

	        @Override
	        public void changed(ObservableValue<? extends WebCamInfo> arg0, WebCamInfo arg1, WebCamInfo arg2) {
	            if (arg2 != null) {
	               
	            	//System.out.println("WebCam Index: " + arg2.getWebCamIndex()+": WebCam Name:"+ arg2.getWebCamName());
	            	initializeWebCam(arg2.getWebCamIndex());
                        
	            }
	        }

            
	    });
    }
    private void initializeWebCam(final int webCamIndex) {
        Task<Void> webCamTask = new Task<Void>() {
			
            @Override
            protected Void call() throws Exception {

                    if(webcam != null)
                    {
                        System.out.println("2");
                        disposeWebCamCamera();
                        webcam = Webcam.getWebcams().get(webCamIndex);         
                        webcam.setCustomViewSizes(nonStandardResolutions);
                        webcam.setViewSize(WebcamResolution.HD720.getSize());

                        webcam.open();
                    }else
                    {
                        System.out.println("1");
                        webcam = Webcam.getWebcams().get(webCamIndex);
                        webcam.setCustomViewSizes(nonStandardResolutions);

                        
                        webcam.setViewSize(WebcamResolution.HD720.getSize());
                       
                        webcam.open();
                    }
                    startWebCamStream();
                    return null;
            }

            
        };
		
        Thread webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();
    }
    private void disposeWebCamCamera() {
        stopCamera = true;
         webcam.close();
//         Webcam.shutdown();
//         Webcam.resetDriver();
                
    }
    class WebCamInfo
    {
            private String webCamName ;
            private int webCamIndex ;

            public String getWebCamName() {
                    return webCamName;
            }
            public void setWebCamName(String webCamName) {
                    this.webCamName = webCamName;
            }
            public int getWebCamIndex() {
                    return webCamIndex;
            }
            public void setWebCamIndex(int webCamIndex) {
                    this.webCamIndex = webCamIndex;
            }

            @Override
            public String toString() {
                    return webCamName;
         }

    }
}

