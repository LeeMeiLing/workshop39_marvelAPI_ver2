package sg.edu.nus.iss.workshop39_v2_server.services;


import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sg.edu.nus.iss.workshop39_v2_server.repositories.CharacterRepository;
import sg.edu.nus.iss.workshop39_v2_server.models.Character;
import sg.edu.nus.iss.workshop39_v2_server.models.Comment;


@Service
public class CharacterService {
    
    @Autowired
    private CharacterRepository charRepo;

    public Optional<List<Character>> getCharacterList(String charName, Integer limit, Integer offset) throws NoSuchAlgorithmException{

        return charRepo.getCharacterList(charName,limit,offset);
    }


    public Optional<Character> getCharacterDetails(String characterId) throws NoSuchAlgorithmException {

        Character c;
        
        // first checks Redis to see if the character details is available
        Optional<Character> opt = charRepo.getCharacterDetailsFromRedis(characterId);
        if(opt.isPresent()){
            c = opt.get();
        }else{

            // If the requested character is not available, get from Marvel API endpoint /v1/public/characters/<characterId>
            // cache the result in Redis for 1 hour
            opt = charRepo.getCharacterDetailsFromMarvelApi(characterId);
            if(opt.isPresent()){
                c = opt.get();
            }else{
                return Optional.empty();
            }
        }

        // retrieved the 10 most recent comments from the Mongo database
        Optional<List<Comment>> optComment =  charRepo.getComments(characterId);
        if(optComment.isPresent()){
            c.setComments(optComment.get());
        }
        
        return Optional.of(c);
    }

    public Comment postComment(String characterId, String comment){

        Comment cm = new Comment(characterId, comment);
        return charRepo.postComment(cm);

    }

}
