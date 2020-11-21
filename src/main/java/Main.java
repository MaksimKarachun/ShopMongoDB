import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws IOException {

        MongoClient mongoClient = new MongoClient( "127.0.0.1" , 27017 );

        MongoDatabase database = mongoClient.getDatabase("shopDB");

        Shop.createShopCollection(database, "shops");

        new Shop("pyatorochka");
        new Shop("perecrestok");

        Shop.addProductToStorage("moloko", 10);
        Shop.addProductToStorage("smetana", 15);
        Shop.addProductToStorage("kolbasa", 20);
        Shop.addProductToStorage("chipsi", 20);

        Shop.addProductFromStorage("chipsi", "pyatorochka");
        Shop.addProductFromStorage("moloko","pyatorochka");

        Shop.addProductFromStorage("chipsi", "perecrestok");
        Shop.addProductFromStorage("moloko","perecrestok");

        Shop.statistic();

        while (true) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String[] inputString = bufferedReader.readLine().split(" ");

            switch (inputString[0]) {
                case ("ДОБАВИТЬ_МАГАЗИН"):
                    new Shop(inputString[1]);
                    break;
                case ("ДОБАВИТЬ_ТОВАР"):
                    Shop.addProductToStorage(inputString[1], Integer.parseInt(inputString[2]));
                    break;
                case ("ВЫСТАВИТЬ_ТОВАР"):
                    Shop.addProductFromStorage(inputString[1], inputString[2]);
                    break;
                case ("СТАТИСТИКА_ТОВАРОВ"):
                    Shop.statistic();
                    break;
                default:
                    System.out.println("command not found");
                    break;
            }
        }

    }
}
