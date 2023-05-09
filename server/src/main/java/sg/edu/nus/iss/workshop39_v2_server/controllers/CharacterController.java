package sg.edu.nus.iss.workshop39_v2_server.controllers;

import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import sg.edu.nus.iss.workshop39_v2_server.services.CharacterService;
import sg.edu.nus.iss.workshop39_v2_server.models.Character;
import sg.edu.nus.iss.workshop39_v2_server.models.Comment;


@RestController
@RequestMapping("/api")
public class CharacterController {

    @Autowired
    private CharacterService charSvc;
    
    // GET /api/characters
    // Accept: application/json
    @GetMapping(path = "/characters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getCharacterList(@RequestParam(required = true) String charName, 
        @RequestParam(defaultValue = "20") Integer limit, @RequestParam(defaultValue = "0") Integer offset) throws NoSuchAlgorithmException{

        System.out.println(" >>> in controller: " + charName); // debug

        Optional<List<Character>> opt = charSvc.getCharacterList(charName,limit, offset);


        if(opt.isPresent()){
            
            JsonArrayBuilder arrB = Json.createArrayBuilder();
            opt.get().stream().forEach(e -> arrB.add(e.toJson()));
            JsonArray payload = arrB.build();

            // return ResponseEntity.ok(payload.toString()); // ** w/o content-type, his works as well
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
            .body(payload.toString());
        }

        return ResponseEntity.status(HttpStatus.OK).body("not found");
    }


    // GET /api/character/<characterId>
    // Accept: application/json
    @GetMapping(path = "/character/{characterId}", produces = MediaType.APPLICATION_JSON_VALUE)
    // public ResponseEntity<String> getCheracterDetails(@PathVariable Integer characterId){ // try Integer, this works!
    public ResponseEntity<String> getCheracterDetails(@PathVariable String characterId) throws NoSuchAlgorithmException{ // try String

        Optional<Character> opt = charSvc.getCharacterDetails(characterId);
        JsonObject jsonCharacter = opt.get().toJsonResponse();

        if(opt.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
            .body(jsonCharacter.toString());
        }else{
            return ResponseEntity.status(HttpStatus.OK).body("not found");
        }

    }

    // POST /api/character/<characterId>
    // Content-Type: application/json
    // Accept: application/json
    @PostMapping(path = "/character/{characterId}", consumes = MediaType.APPLICATION_JSON_VALUE, 
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postComment(@PathVariable String characterId, @RequestBody String payload){

        JsonReader rd = Json.createReader(new StringReader(payload));
        JsonObject jo = rd.readObject();
        String comment = jo.getString("comment");

        Comment inserted = charSvc.postComment(characterId, comment);

        return ResponseEntity.ok().body(inserted.toJson().toString());

    }


}
