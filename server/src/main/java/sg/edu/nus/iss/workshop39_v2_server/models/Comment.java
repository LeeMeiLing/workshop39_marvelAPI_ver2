package sg.edu.nus.iss.workshop39_v2_server.models;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import org.bson.Document;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class Comment implements Serializable{

    private String id;
    private String charId;
    private String comment;
    // private Date timestamp;
    private Instant timestamp;


    public Comment() {
    }

    public Comment(String charId, String comment) {
        this.charId = charId;
        this.comment = comment;
        // this.timestamp = new Date();
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCharId() {
        return charId;
    }
    public void setCharId(String charId) {
        this.charId = charId;
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    // public Date getTimestamp() {
    //     return timestamp;
    // }
    // public void setTimestamp(Date timestamp) {
    //     this.timestamp = timestamp;
    // }
   
    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    

    @Override
    public String toString() {
        return "Comment [id=" + id + ", charId=" + charId + ", comment=" + comment + ", timestamp=" + timestamp + "]";
    }

    public JsonObject toJson(){

        return Json.createObjectBuilder()
                    .add("id", this.getId())
                    .add("charId", this.getCharId())
                    .add("comment", this.getComment())
                    .add("timestamp", this.getTimestamp().toString())
                    .build();
    }

    public Document toDocument() {

        Document doc = new Document();
        doc.put("charId", this.getCharId());
        doc.put("comment", this.getComment());
        doc.put("timestamp", this.getTimestamp());

        return doc;
    }

    public static Comment createFromDocument(Document doc){

        Comment cm = new Comment();
        cm.setId(doc.getObjectId("_id").toString());
        cm.setCharId(doc.getString("charId"));
        cm.setComment(doc.getString("comment"));

        // === using util.Date ===
        // cm.setTimestamp((Date) doc.get("timestamp")); 
        
        // === using instant ===
        if(Instant.class.isInstance(doc.get("timestamp"))){

            // System.out.println(">> isInstance Instant is true "); // debug
            cm.setTimestamp(doc.get("timestamp", Instant.class)); 

        }else if (doc.get("timestamp") instanceof Date){

            // System.out.println(">> isInstance Date is true "); // debug
            Date date = doc.getDate("timestamp"); // get the timestamp field as a Date object
            Instant instant = date.toInstant(); // convert the Date object to an Instant
            cm.setTimestamp(instant);

        }

        return cm;

        // === using LocalDateTime ===:
        // // 1)
        // cm.setTimestamp(LocalDateTime.ofInstant(((Date) doc.get("timestamp")).toInstant(), ZoneId.systemDefault())); // works but lost info of timezone
        
                // // === converting util.Date to LocalDateTime ===
                // LocalDateTime.ofInstant(doc.get("timestamp").toInstant(), ZoneId.systemDefault()) 

        // // 2) 
        // // !!! ClassCastException: class java.util.Date cannot be cast to class java.time.LocalDateTime
        // cm.setTimestamp((LocalDateTime) doc.get("timestamp")); 

    }


    
}
