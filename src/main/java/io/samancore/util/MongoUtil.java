package io.samancore.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import io.samancore.ConstantUtil;
import lombok.extern.jbosslog.JBossLog;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Date;


@JBossLog
public class MongoUtil {

    private final UpdateOptions updateOptions = new UpdateOptions().upsert(true);

    public void execute(JsonNode json) {
        log.debug("MongoUtil.execute");
        try (MongoClient mongoClient = getConection()) {

            String templateId;
            var jsonDocument = Document.parse(json.toPrettyString());
            if (json.get("_id") != null) {
                templateId = json.get("_id").textValue();
                jsonDocument.append("updatedAt", new Date());
            } else {
                var objectId = new ObjectId();
                templateId = objectId.toString();
                jsonDocument.append("createdAt", new Date());
            }
            Bson filter = Filters.eq("_id", templateId);
            var documentData = new Document("$set", jsonDocument);
            mongoClient.getDatabase(ConstantUtil.DATABASE_NAME)
                    .getCollection(ConstantUtil.COLLECTION_DBA_NAME)
                    .updateOne(filter, documentData, updateOptions);
        } catch (Exception error) {
            log.error(error);
            throw error;
        }

    }

    private MongoClient getConection() {
        var userDb = System.getProperty(ConstantUtil.DBA_USER_KEY);
        var userPw = System.getProperty(ConstantUtil.DB_PW_KEY);
        var host = System.getProperty(ConstantUtil.DB_HOST_KEY);
        return MongoClients.create("mongodb://" + userDb + ":" + userPw + "@" + host);
    }

}
