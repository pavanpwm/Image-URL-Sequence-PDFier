package application;
	
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;


public class Main extends Application implements Initializable{
	
	
	@FXML
    private Label status;
    @FXML
    private TextField prefix;
    @FXML
    private TextField suffix;
    @FXML
    private TextField firstPage;
    @FXML
    private TextField lastPage;
    @FXML
    private Button outputFolder;
    
    static String dest = "D:\\Users\\pavan BTD\\Desktop\\8th sem\\FON\\pdf\\";
    static ArrayList<String> imagePaths = new ArrayList<String>();
    static ArrayList<Thread> activeThreadList = new ArrayList<Thread>();
	
	
    @Override
	public void start(Stage primaryStage) throws Exception{
		Stage newStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("Form.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		newStage.setScene(scene);
		newStage.setResizable(false);
		newStage.sizeToScene();			// to solve the extra margins caused by setResizable(false) method
		newStage.setTitle("Image Sequence URL PDFier");
		newStage.show();
		newStage.setOnCloseRequest(w->{
			Platform.exit();
			System.exit(0);
		});
	}
	public static void main(String[] args) {
		launch(args);
	}
	public void stop(Stage primaryStage) throws Exception {
		Platform.exit();
	}
	
	public void selectDest() {
		DirectoryChooser dir_chooser = new DirectoryChooser();
		dir_chooser.setTitle("Select the folder with HTML files");
        File loc = dir_chooser.showDialog((Stage) status.getScene().getWindow());
        dest = loc.getAbsolutePath();
        outputFolder.setText(loc.getName());
        status.setText("Output folder selected :" + loc.getAbsolutePath());
	}
	
	public void onSubmit() throws Exception {
		imagePaths.clear();
		String pre = prefix.getText();
		String suf = suffix.getText();
		int first = Integer.parseInt(firstPage.getText());
		int last = Integer.parseInt(lastPage.getText());
		
		if (!pre.isEmpty() && !firstPage.getText().isEmpty() && !lastPage.getText().isEmpty() && !dest.isEmpty()) {
			System.out.println("downloading images");
			status.setText("Please wait while images are being downloaded...");
			for (int i = first; i <= last; i++) {
				status.setText("Downloading " + i);
				String imageUrl = pre + i + suf;
				String outputImagePath = dest + "/page"+ i +".jpg";
				imagePaths.add(outputImagePath); 
				Thread t = new Thread(() -> {
					try {
				        InputStream inputStream = null;
				        OutputStream outputStream = null;
				        URL url = new URL(imageUrl);
			            inputStream = url.openStream();
			            outputStream = new FileOutputStream(outputImagePath);
			            byte[] buffer = new byte[2048];
			            int length;
			            while ((length = inputStream.read(buffer)) != -1) {
			                outputStream.write(buffer, 0, length);
			            }
			            inputStream.close();
			            outputStream.close();
					} catch (Exception e) {
						status.setText("Error!");
					}
				});
				activeThreadList.add(t);
				t.start();
		    }
			
			activeThreadList.forEach(tt ->{
				try {
					tt.join();
				}catch (Exception e) {
				}
			});
			
			try {
				buildPDF();
			} catch (Exception eee) {
				eee.printStackTrace();
				status.setText("Error!");
			}
			
			
		}else {
			status.setText("Please fill all required fields!");
		}
		
		
		
		
	}
	
	
	// add a single itextpdf-5.5.9.jar
	public void buildPDF() throws Exception {
		Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(new File(dest + "/newPDF.pdf")));
        document.open();
        for (String f : imagePaths) {
            document.newPage();
            Image image = Image.getInstance(new File(f).getAbsolutePath());
            image.setAbsolutePosition(0, 0);
            image.setBorderWidth(0);
            image.scaleAbsolute(PageSize.A4);
            document.add(image);
        }
        document.close();
        for (String f : imagePaths) {
        	new File(f).delete();
        }
        status.setText("Done.");
		Desktop.getDesktop().open(new File(dest));
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		prefix.setText("https://image.slidesharecdn.com/networkcybersecuritymodule1-5-190703103227/85/vtu-network-cyber-security-15-module-full-notes-");
		suffix.setText("-638.jpg?cb=1562150264");
		firstPage.setText("1");
		lastPage.setText("176");
	}
}
