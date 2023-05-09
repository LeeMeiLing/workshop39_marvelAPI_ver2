package sg.edu.nus.iss.workshop39_v2_server.repositories;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import sg.edu.nus.iss.workshop39_v2_server.models.Character;
import sg.edu.nus.iss.workshop39_v2_server.models.Comment;

@Repository
public class CharacterRepository {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${MARVELAPI_PUBLICKEY}")
    private String publicKey;

    @Value("${MARVELAPI_PRIVATEKEY}")
    private String privateKey;

    @Value("${MARVELAPI_URL}")
    private String marvelApiUrl;

    private String generateHash(Instant ts) throws NoSuchAlgorithmException{

        // generate hash: md5(ts+privateKey+publicKey)
        String toHash = ts.toString() + privateKey + publicKey;

        // Static getInstance method is called with hashing MD5
        MessageDigest md = MessageDigest.getInstance("MD5");

        // digest() method is called to calculate message digest of an input, digest() return array of byte
        byte[] digest = md.digest(toHash.getBytes());

        // Convert byte array into signum representation
        BigInteger no = new BigInteger(1, digest);

        // convert to hash value
        // Convert message digest into hex value
        String hash = no.toString(16);
        while (hash.length() < 32) {
            hash = "0" + hash;
        }

        return hash;
    }


    // GET /v1/public/characters
    // nameStartsWith=charName , limit=20 , offset=0
    // save the individual character details into the Redis database, cache for 1 hour
    public Optional<List<Character>> getCharacterList(String charName, Integer limit, Integer offset) throws NoSuchAlgorithmException{

        // generate ts
        Instant ts = Instant.now();

        String hash = generateHash(ts);

        // construct http request to call Marvel API
        String url = UriComponentsBuilder.fromUriString(marvelApiUrl + "v1/public/characters")
                        .queryParam("apikey", publicKey)
                        .queryParam("ts", ts)
                        .queryParam("hash", hash)
                        .queryParam("nameStartsWith", charName)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .toUriString();
        
        RequestEntity<Void> req = RequestEntity.get(url).build();

        RestTemplate restTemplate = new RestTemplate();

        try{
            ResponseEntity<String> resp = restTemplate.exchange(req,String.class);
            String payload = resp.getBody();

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray resultsArr = reader.readObject().getJsonObject("data").getJsonArray("results");
            List<Character> characterList = resultsArr.stream()
                                                .map(e -> Character.create(e.asJsonObject()))
                                                .toList();

            // save the individual character details into the Redis database, cache for 1 hour
            // if characterList is empty, wont throw error
            characterList.stream().forEach(c -> redisTemplate.opsForValue().set(String.valueOf(c.getId()),c.toJson().toString(),3600, TimeUnit.SECONDS));
          

            if(characterList.size() > 0){
                System.out.println(">>> characterList: " + characterList); // debug
                return Optional.of(characterList);
            }else{
                System.out.println(">>> empty characterList"); // debug
                return Optional.empty();
            }
    
        }catch(HttpClientErrorException ex){
            String payload = ex.getResponseBodyAsString();
            System.out.println(">>> httpclient error: " + payload); // debug

            return Optional.empty();
            // change to throw custom error
        }

    }

    public Optional<Character> getCharacterDetailsFromRedis (String characterId){

        System.out.println(">>> in repo, getcharDetails");

        String data = (String) redisTemplate.opsForValue().get(characterId); // this return null if key not found in redis

        if( data != null){

            JsonReader reader = Json.createReader(new StringReader(data));
            JsonObject jo = reader.readObject();
            Character c = Character.createFromRedis(jo);
            System.out.println(">>> character details from Redis: " + c); // debug
            return Optional.of(c);

        }else{
            return Optional.empty();
        }
        

        // === use this if value saved to redis is object ===
        // Character data = (Character) redisTemplate.opsForValue().get(characterId);
        // System.out.println(">>> data: " + data);

    }

    // GET /v1/public/characters/<characterId>
    public  Optional<Character> getCharacterDetailsFromMarvelApi (String characterId) throws NoSuchAlgorithmException{

        // generate ts
        Instant ts = Instant.now();
        String hash = generateHash(ts);

        // construct http request to call Marvel API
        String url = UriComponentsBuilder.fromUriString(marvelApiUrl + "v1/public/characters")
                        .pathSegment(characterId) // .path() does not auto insert "/"   pathSegment() auto insert "/"
                        .queryParam("apikey", publicKey)
                        .queryParam("ts", ts)
                        .queryParam("hash", hash)
                        .toUriString();
        
        RequestEntity<Void> req = RequestEntity.get(url).build();

        RestTemplate restTemplate = new RestTemplate();

        try{
            ResponseEntity<String> resp = restTemplate.exchange(req,String.class);
            String payload = resp.getBody();

            JsonReader reader = Json.createReader(new StringReader(payload));
            JsonArray resultsArr = reader.readObject().getJsonObject("data").getJsonArray("results");
            Character c = Character.create(resultsArr.getJsonObject(0));

            // save the individual character details into the Redis database, cache for 1 hour
            // if character not found, http response 404, go to catch block
            redisTemplate.opsForValue().set(String.valueOf(c.getId()),c.toJson().toString(),3600, TimeUnit.SECONDS);
            System.out.println(">>> character details from Marvel endpoint: " + c); // debug

            return Optional.of(c);
    
        }catch(HttpClientErrorException ex){

            String payload = ex.getResponseBodyAsString();
            System.out.println(">>>>> httpclient error: " + payload); // debug

            return Optional.empty();
            // change to throw custom error
        }

    }

    /*
     * db.comments.find(
           { charId: "1011247" }
        ).sort(
            { timestamp: -1 }
        ).limit(10)
     */
    public Optional<List<Comment>> getComments(String characterId){ // if mongo charId is int, can read?
        
        Criteria criteria = Criteria.where("charId").is(characterId);
        Query query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "timestamp")).limit(10);
        List<Document> docs = mongoTemplate.find(query, Document.class, "comments"); // mongo.find() return empty list if result not found

        // docs returned, "timestamp" is type Date, need cast to date then convert to instant
        // System.out.println(" timestamp in getComment " + docs.get(0).get("timestamp").getClass().getSimpleName()); // debug

        if(docs.size() > 0){

            // List<Comment> cList =docs.stream().map(d -> Comment.createFromDocument(d)).toList();
            List<Comment> cList =docs.stream().map(Comment::createFromDocument).toList();

            return Optional.of(cList);

        }else{
            return Optional.empty();
        }
    }

    public Comment postComment(Comment comment){

        Document toInsert = comment.toDocument();

        
        Document insertedDoc = mongoTemplate.insert(toInsert, "comments");

        // insertedDoc returned, "timestamp" is type Instant, no need cast to date then convert
        // System.out.println(" timestamp in postcomment " + insertedDoc.get("timestamp").getClass().getSimpleName()); //debug

        return Comment.createFromDocument(insertedDoc);
    }

    
}
