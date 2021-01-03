package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient extends Application implements Runnable {


    public static TextFlow messages = new TextFlow();
    public static ScrollPane scrollPane = new ScrollPane();

    Button button = new Button("Send");
    Button buttonFile = new Button("Send File");
    Button buttonImage = new Button("Send Image");

    TextField input = new TextField();

    static String myText = "";


    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;
    //private static DataInputStream nameReader = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;


    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("TextPhile");

        messages.setPrefHeight(420);

        input.setPrefHeight(40);
        input.setPrefWidth(350);

        button.setPrefHeight(40);
        button.setPrefWidth(100);

        buttonImage.setPrefWidth(100);
        buttonFile.setPrefWidth(100);


        button.setOnAction(e -> {

            myText = input.getText();
            input.clear();
            os.println(myText);
        });


        buttonFile.setOnAction(e ->{

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");

            File file = fileChooser.showOpenDialog(stage);

            String filePath = "file-"+file.getPath();
            os.println(filePath);

        });

        buttonImage.setOnAction(e ->{

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource Image");

            File file = fileChooser.showOpenDialog(stage);

            String imagePath = "image-"+file.getPath();
            os.println(imagePath);
        });

        input.setOnAction(e -> {
            myText = input.getText();
            input.clear();
            os.println(myText);
        });

        HBox hBox= new HBox(40,input,button);


        scrollPane.setId("presentationScrollPane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(messages);
        VBox layout = new VBox(20,scrollPane,hBox,buttonFile,buttonImage);
       layout.setPrefSize(600,640);




        Scene scene = new Scene(layout,600,640);


        input.setStyle("-fx-control-inner-background: #B8B8B8;" +
                "-fx-border-insets: -3 -3 -3 -3 ;" +
                "-fx-border-color: #FFD700;");

        scrollPane.setStyle("-fx-background: #181818;" +
                "-fx-border-color: #FFD700;");




        layout.setStyle("-fx-background-color: #181818");
        button.setStyle("-fx-background-color: #DC9656");
        buttonImage.setStyle("-fx-background-color: #DC9656");
        buttonFile.setStyle("-fx-background-color: #DC9656");


        VBox.setMargin(scrollPane, new Insets(50, 50, 0, 50));
        VBox.setMargin(hBox, new Insets(0, 50, 0, 50));
        VBox.setMargin(buttonImage, new Insets(0, 50, 0, 440));
        VBox.setMargin(buttonFile, new Insets(0, 50, 0, 440));

        stage.setScene(scene);
        stage.show();

    }


    public static void main(String[] args) {
        // The default port.
        int portNumber = 2222;
        // The default host.
        String host = "localhost";
        System.out.println("Usage: java ChatClient <host> <portNumber>\n"
                        + "Now using host=" + host + ", portNumber=" + portNumber);

        /*
         * Open socket
         */
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
        }
        /*
         * write data
         */

        (new Thread() {
            public void run() {
                Application.launch((ChatClient.class));
            }
        }).start();



        if (clientSocket != null && os != null && is != null) {
            try {

                /* write to server. */

                new Thread(new ChatClient()).start();

                while (!closed) {
                    myText = inputLine.readLine().trim();
                    os.println(myText);
                }
                /*
                 * Close stream
                 */
                os.close();
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }

    /*
     * read from the server
     */
    public void run() {

        /*
         * Keep on reading from the socket till we receive "Bye" from the
         * server. Once we received that then we want to break.
         */

        String responseLine;
        try {
            while ((responseLine = is.readLine()) != null) {

                if (responseLine.startsWith("image")){
                    String[] words = responseLine.split("-", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        if (!words[1].isEmpty()) {
                            FileInputStream inputStream = new FileInputStream(words[1]);
                            Image image = new Image(inputStream);

                            ImageView imageView = new ImageView(image);

                            //Setting the position of the image
                            imageView.setX(0);
                            imageView.setY(0);

                            //setting the fit height and width of the image view
                            imageView.setFitHeight(200);
                            imageView.setFitWidth(200);

                            //Setting the preserve ratio of the image view
                            imageView.setPreserveRatio(true);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    messages.getChildren().addAll(new Text("\n"),imageView,new Text("\n"));
                                    return;
                                }
                            });
                        }
                    }
                    continue;
                }

                else if (responseLine.startsWith("file")){
                    String[] words = responseLine.split("-", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        if (!words[1].isEmpty()) {

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        File myObj = new File(words[1]);
                                        Scanner myReader = new Scanner(myObj);
                                        while (myReader.hasNextLine()) {
                                            String data = myReader.nextLine();
                                            Text text = new Text(data);
                                            text.setFill(Color.AZURE.darker());
                                            text.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
                                            messages.getChildren().addAll(new Text("\n"),text);
                                            System.out.println(data);
                                        }
                                        myReader.close();
                                    } catch (FileNotFoundException e) {
                                        System.out.println("An error occurred.");
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                            });
                        }
                    }
                    continue;
                }

                Text text;
                if(messages.getChildren().size()==0){
                    text = new Text(responseLine);
                } else {
                    // Add new line if not the first child
                    text = new Text("\n" + responseLine );
                }
                text.setFill(Color.MEDIUMVIOLETRED);



                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        messages.getChildren().add(text);
                        scrollPane.setVvalue(1D);
                    }
                });


                if (!responseLine.contains("Bye")) {
                    continue;
                }
                break;
            }
            closed = true;
        } catch (IOException  e) {
            System.err.println("IOException:  " + e);
        }
    }
}


