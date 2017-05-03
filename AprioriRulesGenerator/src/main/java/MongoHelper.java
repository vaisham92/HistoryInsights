import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import java.net.UnknownHostException;

/**
 * Created by maverick on 5/1/17.
 */

public class MongoHelper {
    MongoClient mongoClient;
    MongoClientURI mongoUri;
    MongoDatabase database;

    public void connect() throws UnknownHostException{
        this.mongoUri = new MongoClientURI("mongodb://bharath:bharath@ds127731.mlab.com:27731/browsing_history_db");
        this.mongoClient = new MongoClient(this.mongoUri);
        this.database = this.mongoClient.getDatabase(this.mongoUri.getDatabase());
        System.out.println("Connected to DB");
    }

    public void disconnect(){
        this.mongoClient = null;
        System.out.println("Disconnected successfully");
    }
}
