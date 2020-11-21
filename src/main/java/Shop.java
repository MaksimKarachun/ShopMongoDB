
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Shop {

    static MongoCollection<Document> shopCollection;

    //не выставленные товары
    private static MongoCollection<Document> storage;

    private static HashMap<String, StringBuilder> statisticMap = new HashMap<>();

    private String name;

    public static void createShopCollection(MongoDatabase database, String collectionName){
        shopCollection = database.getCollection(collectionName);
        storage = database.getCollection("storage");
        shopCollection.drop();
        storage.drop();
    }

    public Shop(String name){
        this.name = name;
        Document shopDocument = new Document()
                .append("name", name);
        shopDocument.append("products", new ArrayList<>());
        shopCollection.insertOne(shopDocument);
    }

    public static void addProductToStorage(String name, int price){
        Document productDocument = new Document()
                .append("product_name", name)
                .append("price", price);

        storage.insertOne(productDocument);
    }

    public static void addProductFromStorage(String productName, String shopName){
        Document query = new Document("product_name", productName);
        Document query1 = new Document("name", shopName);
        if (storage.find(query).first() == null)
            System.out.println("product is not find in storage");
        else {
            if (shopCollection.find(query1).first() == null)
                System.out.println("shop is not find");
            else
                shopCollection.findOneAndUpdate(new Document("name", shopName), new Document("$push", new Document("products", productName)));
        }
    }

    public static void statistic(){

        BsonDocument query1 = BsonDocument.parse("{$lookup: {from: \"storage\"," +
                "localField: \"products\"," +
                "foreignField: \"product_name\"," +
                " as: \"product_list\"}}");
        BsonDocument query2 = BsonDocument.parse("{ $unwind: \"$product_list\"}");
        BsonDocument query3 = BsonDocument.parse("{ $group: {_id: \"$name\"," +
                "products_count: {$sum :1}," +
                "avg_products_price: {$avg:\"$product_list.price\"}," +
                "min_price: {$min:\"$product_list.price\"}," +
                "max_price: {$max:\"$product_list.price\"}," +
                "}}");

        BsonDocument query5 = BsonDocument.parse("{$match:{\"product_list.price\":{$lt : 100}}}");
        BsonDocument query6 = BsonDocument.parse("{ $group: { _id: \"$name\", prod_less_then_100: { $sum: 1 } } }");



        shopCollection.aggregate(Arrays.asList(query1, query2, query3)).forEach((Consumer<Document>) doc -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("average  product price: " + doc.get("avg_products_price") + "\n" +
                    "min product price: " + doc.get("min_price") + "\n" +
                    "max product price: " + doc.get("max_price")
            );

            statisticMap.put(doc.getString("_id"), stringBuilder);

        });

        shopCollection.aggregate(Arrays.asList(query1, query2, query5, query6)).forEach((Consumer<Document>) doc -> {
            statisticMap.get(doc.get("_id")).
                    append("\n").
                    append("count products cheaper then 100: ").
                    append(doc.get("prod_less_then_100")).
                    append("\n");

        });

        for(Map.Entry<String, StringBuilder> iter : statisticMap.entrySet()){
            System.out.println(iter.getKey() + "\n" + iter.getValue());
        }
    }


}
