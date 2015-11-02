package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Main extends Application {


    Rectangle FieldRect[][] = new Rectangle[10][10];

    int x, y;
    int X = 0, Y = 0;

    int x3, y3;

    private static int[][] Field;
    private final static int LINE = 10;
    private final static int COLUMN = 10;

    private static int Port = 6727; //порт
    private static String host;
    private static Socket clientSocket;
    private static DataInputStream in; //для получения данных с сервера
    Pane layout = new Pane(); //
    BorderPane pane = new BorderPane();
    static TextField text;
    static int  Number; //чтобы клиент зал, какой игрок играет - player1 или player2

    static {
        Field = new int[LINE][COLUMN];
        for (int i = 0; i < LINE; i++, System.out.println()) {
            for (int j = 0; j < COLUMN; j++) {
                Field[i][j] = 0;
            }
        }
        try {
            BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Input IP");
            host = sc.readLine();
            System.out.println("Input the port!");
            Port = Integer.parseInt(sc.readLine());
            clientSocket = new Socket(host, Port);//}

            in = new DataInputStream(clientSocket.getInputStream()); // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            Number = in.readInt(); //получаем номер игрока от сервера. сделать это нужно ДО запуска метода start, поэтому помещаем в static блок
            //  text = new TextField();
            // text.setText("Text");
        }catch(IOException e){}
    }



    private static void getFromServer(DataInputStream in) throws IOException{ //получаем данные с сервера
        int PointsPlayer1 = in.readInt();
        System.out.println("player1 - " + PointsPlayer1);



        int PointsPlayer2 = in.readInt();
        System.out.println("Points for second player - " + PointsPlayer2);
        text.setText("player1 - " + PointsPlayer1+ "   player2 - " + PointsPlayer2);

        for (int i = 0; i < Main.LINE; i++) { //получаем поле
            for (int j = 0; j < Main.COLUMN; j++) {
                Main.Field[i][j] = in.readInt();

            }
        }

    }

    @Override
    public void start(Stage primaryStage)throws IOException {  //тут прорисовка поля
        primaryStage.setTitle("Dots game"); //заголовок
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml")); //это для Scene Builder! но пользоваться им для этой игры было не удобно. так что здесь ничего

        for (int i = 0; i < 10; i++) {   //тут создается поле

            for (int j = 0; j < 10; j++) {
                PaintGrid(i, j); //а здесь прорисовывается
                layout.getChildren().add(FieldRect[i][j]); //добавляем на экран
            }
            X += 1; //это для создания линий между rectangle
            Y = 0;

        }
        Button button = new Button("GO!"); //кнопка, при помощи которой игрок подтверждает ход

        pane.setTop(layout); //в верхнюю часть экрана помещаем поле

        HBox box = new HBox(); //layout для расположений внижу экрана
        box.setSpacing(30); //устранавливаем промежуток между элементами
        box.getChildren().add(button); // добавляем кнопку Go, она нужна двоим игрокам

        text = new TextField();
        box.getChildren().add(text);

        button.setOnAction(new EventHandler<ActionEvent>(){
                               @Override
                               public void handle(ActionEvent event) {           //добавляем слушателя кнопки
                                   try {
                                       DataOutputStream output1 = new DataOutputStream(clientSocket.getOutputStream());
                                       output1.writeInt(y3);    //отправляем на сервер
                                       // System.out.println("Input column!"); //вводим колонку

                                       output1.writeInt(x3);//отправляем на сервер
                                       output1.flush(); // заставляем поток закончить передачу данных.
                                       getFromServer(in);


                                       PaintCircle1();
                                   }
                                   catch(IOException e){
                                       System.out.println("IOException in sending data to server!");
                                   }

                               }
                           }
        );




        if (Number==2) { //если играет игрок 2, то создаем дополнительную кнопку - Get field! В начале игры после 1го ода игрока надо нажать ее, чтобы получать ходы игрока1
            Button button1 = new Button("Get Field!");
            button1.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        getFromServer(in);
                        PaintCircle1();
                    } catch (IOException e) {
                    }
                }
            });
            box.getChildren().add(button1);
        }



        Label label = new Label("Dots Game"); //сщздаем label
        label.setFont(new Font("Algerian", 18)); //устанавливаем шрифт

        box.getChildren().add(label); // добавляем label на экран
        pane.setBottom(box); //+ в нижнюю часть экрана
        Scene scene = new Scene(pane, 500, 500, Color.YELLOW); //устанавливаем параметры scene
        primaryStage.setScene(scene); //устанавливаем эту сцену как текущую

        primaryStage.show(); //показываем окно

    }

    private void PaintGrid(final int i, final int j)//,final Socket s)throws IOException {
    { x = X + i * 40;
        y = Y + j * 40;
        Rectangle r = new Rectangle(x, y, 40, 40);   //создаем rectangle
        r.setOnMouseClicked(new EventHandler<MouseEvent>() { //добавляем слушателя прямоугольников, слушатели для мышки
                                @Override
                                public void handle(MouseEvent event) {
                                    x3 = (int) event.getX() / 41; //здесь получаем координаты от мышки и определяем ,в каком квадраде на поле произошло событие
                                    y3 = (int) event.getY() / 41;
                                    System.out.println("Event in  X " + y3 + " and  Y " + x3);
                                    Field[y3][x3] = Number; //это для того, чтобы 1й игрок ходим синими точками, а 2й - красными

                                    PaintCircle1(); //прорисовка точек


                                }
                            }
        );

        r.setFill(Color.WHITE);
        FieldRect[i][j] = r;
        Y += 1;

    }



    private void PaintCircle1() { //здесь прорисовка точек
        int X1 =20; int Y1=20;
        for (int i = 0; i < Main.LINE; i++){
            for (int j = 0; j < Main.COLUMN; j++) {
                int x1 = X1 + i * 40;
                int y1 = Y1 + j * 40;

                if (Field[j][i] == 1) {
                    Circle c1 = new Circle(x1, y1, 20, Color.BLUE); //+ на экран

                    layout.getChildren().add(c1);
                }
                if (Field[j][i] == 2) {
                    Circle c2 = new Circle(x1, y1, 20, Color.RED); //+ на экран

                    layout.getChildren().add(c2);
                }
                if (Field[j][i] == 6) {
                    Circle c3 = new Circle(x1, y1, 20, Color.BLACK); //+ на экран

                    layout.getChildren().add(c3);
                }
                if (Field[j][i]==7) {
                    Circle c4 = new Circle(x1, y1, 20, Color.BLACK); //+ на экран

                    layout.getChildren().add(c4);
                }
                Y1 += 1;
            }
            X1 += 1;
            Y1 = 20;
        }


    }


    public static void main(String[] args)throws IOException {
        launch(args);
        // System.out.println("Input IP and the port!");
    }
}