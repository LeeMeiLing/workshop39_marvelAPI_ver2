package sg.edu.nus.iss.workshop39_v2_server.models;

import java.io.Serializable;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

public class Character implements Serializable{
    
    // id (int, optional): The unique ID of the character resource.,
    // name (string, optional): The name of the character.,
    // description (string, optional): A short bio or description of the character.,
    // modified (Date, optional): The date the resource was most recently modified.,
    // resourceURI (string, optional): The canonical URL identifier for this resource.,
    // urls (Array[Url], optional): A set of public web site URLs for the resource.,
    // thumbnail (Image, optional): The representative image for this character.,
    // comics (ComicList, optional): A resource list containing comics which feature this character.,
    // stories (StoryList, optional): A resource list of stories in which this character appears.,
    // events (EventList, optional): A resource list of events in which this character appears.,
    // series (SeriesList, optional): A resource list of series in which this character appears.

    private Integer id;
    private String name;
    private String description;
    private String resourceURI;
    private String image;
    private List<Comment> comments;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getResourceURI() {
        return resourceURI;
    }
    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    @Override
    public String toString() {
        return "Character [id=" + id + ", name=" + name + ", description=" + description + ", resourceURI="
                + resourceURI + ", image=" + image + ", comments=" + comments + "]";
    }

    public static Character create(JsonObject j){

        Character c = new Character();
        c.setId(j.getInt("id"));
        c.setName(j.getString("name"));
        c.setDescription(j.getString("description"));
        c.setResourceURI(j.getString("resourceURI"));
        c.setImage(j.getJsonObject("thumbnail").getString("path") + "." +j.getJsonObject("thumbnail").getString("extension"));

        return c;
    }

    public JsonObject toJson(){

        return Json.createObjectBuilder()
                    .add("id", this.getId())
                    .add("name", this.getName())
                    .add("description", this.getDescription())
                    .add("resourceURI", this.getResourceURI())
                    .add("image", this.getImage())
                    .build();
    }

    public JsonObject toJsonResponse(){

        JsonObjectBuilder joB = Json.createObjectBuilder()
                    .add("id", this.getId())
                    .add("name", this.getName())
                    .add("description", this.getDescription())
                    .add("resourceURI", this.getResourceURI())
                    .add("image", this.getImage());

        // check if List<Comment> is null or empty
        if(this.getComments() != null && this.getComments().size() > 0){

            JsonArrayBuilder arrB = Json.createArrayBuilder();
            this.getComments().stream().map(c -> c.toJson()).forEach(j -> arrB.add(j));
            JsonValue commentArr = arrB.build();
            joB.add("comments", commentArr);

        }
    
        return joB.build();
                          
    }


    public static Character createFromRedis(JsonObject j){

        Character c = new Character();
        c.setId(j.getInt("id"));
        c.setName(j.getString("name"));
        c.setDescription(j.getString("description"));
        c.setResourceURI(j.getString("resourceURI"));
        c.setImage(j.getString("image"));
        
        return c;
    }
    
}
