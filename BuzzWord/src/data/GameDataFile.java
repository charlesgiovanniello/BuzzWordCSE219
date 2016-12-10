package data;

import com.fasterxml.jackson.core.*;
import components.AppDataComponent;
import components.AppFileComponent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import java.security.MessageDigest;

/**
 * @author Ritwik Banerjee
 */
public class GameDataFile implements AppFileComponent {

    public static final String TARGET_WORD  = "TARGET_WORD";
    public static final String GOOD_GUESSES = "GOOD_GUESSES";
    public static final String BAD_GUESSES  = "BAD_GUESSES";
    public static final String USER_NAME    = "USER_NAME";
    public static final String PASSWORD    = "PASSWORD";
    public static final String GAME_ONE_PROG    = "GAME_ONE_PROG";
    public static final String GAME_TWO_PROG    = "GAME_TWO_PROG";
    public static final String GAME_THREE_PROG    = "GAME_THREE_PROG";

    @Override
    public void saveData(AppDataComponent data, Path to) {
        GameData       gamedata    = (GameData) data;
        Set<Character> goodguesses = gamedata.getGoodGuesses();
        Set<Character> badguesses  = gamedata.getBadGuesses();

        JsonFactory jsonFactory = new JsonFactory();

        try (OutputStream out = Files.newOutputStream(to)) {

            JsonGenerator generator = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

            generator.writeStartObject();

            generator.writeStringField(TARGET_WORD, gamedata.getTargetWord());

            generator.writeFieldName(GOOD_GUESSES);
            generator.writeStartArray(goodguesses.size());
            for (Character c : goodguesses)
                generator.writeString(c.toString());
            generator.writeEndArray();

            generator.writeFieldName(BAD_GUESSES);
            generator.writeStartArray(badguesses.size());
            for (Character c : badguesses)
                generator.writeString(c.toString());
            generator.writeEndArray();

            generator.writeEndObject();

            generator.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void createProfile(AppDataComponent data, Path to, String userName, String passWord) {
        GameData       gamedata    = (GameData) data;
        JsonFactory jsonFactory = new JsonFactory();
        try (OutputStream out = Files.newOutputStream(to,WRITE,APPEND)) {

            JsonGenerator generator = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

            generator.writeStartObject();

            generator.writeStringField(USER_NAME, userName);

            try {
                passWord = hashPassword(passWord);
            }catch(NoSuchAlgorithmException e){}
            generator.writeStringField(PASSWORD, passWord);

            generator.writeStringField(GAME_ONE_PROG, String.valueOf(gamedata.getGameOneProg()));
            generator.writeStringField(GAME_TWO_PROG, String.valueOf(gamedata.getGameTwoProg()));
            generator.writeStringField(GAME_THREE_PROG, String.valueOf(gamedata.getGameThreeProg()));

            generator.writeEndObject();

            generator.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean login(AppDataComponent data, Path from, String userName, String passWord) throws IOException {
        GameData gamedata = (GameData) data;
        gamedata.reset();
        try {
            passWord = hashPassword(passWord);
        }catch(NoSuchAlgorithmException e){}

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser  jsonParser  = jsonFactory.createParser(Files.newInputStream(from));
        boolean objectFound = false;

            JsonToken token;
            do {
                    token = jsonParser.nextToken();
                if (token == null) //if end of JSON file is reached and no matching credentials
                    break;
                if (token.equals(VALUE_STRING)) {
                    String fieldname = jsonParser.getValueAsString();
                    if (fieldname.equals(userName)) {
                        //if we reached the object in the linear search

                        jsonParser.nextToken();
                        jsonParser.nextToken(); // value of password
                        if( !jsonParser.getValueAsString().equals(passWord))
                            break;
                        else
                            objectFound = true;
                    }
                }
            }while(objectFound == false);

            if (objectFound) {
                gamedata.setUser(jsonParser.getValueAsString());

                jsonParser.nextToken();
                jsonParser.nextToken(); //Game one prog
                gamedata.setGameOneProg(jsonParser.getValueAsInt());

                jsonParser.nextToken();
                jsonParser.nextToken();
                gamedata.setGameTwoProg(jsonParser.getValueAsInt());

                jsonParser.nextToken();
                jsonParser.nextToken();
                gamedata.setGameThreeProg(jsonParser.getValueAsInt());

                return true;
            }
        return false;
    }
    public void updateUserProg(AppDataComponent data, Path to, String mode, String userName, int difficulty) throws IOException{
        GameData       gamedata    = (GameData) data;
        JsonFactory jsonFactory = new JsonFactory();
        JsonParser  jsonParser  = jsonFactory.createParser(Files.newInputStream(to));
        JsonToken token;
        boolean objectFound = false;

        try (OutputStream out = Files.newOutputStream(to,WRITE)) {

            JsonGenerator generator = jsonFactory.createGenerator(out, JsonEncoding.UTF8);

            do {
                token = jsonParser.nextToken();
                if (token == null) //if end of JSON file is reached and no matching credentials
                    break;
                if (token.equals(VALUE_STRING)) {
                    String fieldname = jsonParser.getValueAsString();
                    if (fieldname.equals(userName)) {
                        objectFound = true;
                    }
                }
            }while(!objectFound);
            jsonParser.nextToken();jsonParser.nextToken();

            if (mode.equals("Names")) {
                jsonParser.nextToken();
                jsonParser.nextToken();
            }
            if (mode.equals("Foods")) {
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
            }
            if (mode.equals("Animals")) {
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
                jsonParser.nextToken();
            }
            //generator.setCurrentValue(jsonParser.getEmbeddedObject());
            generator.setCodec(jsonParser.getCodec());
            generator.writeStringField("GAME_ONE_PROG", (String.valueOf(difficulty)));
            generator.writeString(String.valueOf(difficulty));
            //generator.writeEndObject();
            generator.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void loadData(AppDataComponent data, Path from) throws IOException {
        GameData gamedata = (GameData) data;
        gamedata.reset();

        JsonFactory jsonFactory = new JsonFactory();
        JsonParser  jsonParser  = jsonFactory.createParser(Files.newInputStream(from));

        while (!jsonParser.isClosed()) {
            JsonToken token = jsonParser.nextToken();
            if (FIELD_NAME.equals(token)) {
                String fieldname = jsonParser.getCurrentName();
                switch (fieldname) {
                    case TARGET_WORD:
                        jsonParser.nextToken();
                        gamedata.setTargetWord(jsonParser.getValueAsString());
                        break;
                    case GOOD_GUESSES:
                        jsonParser.nextToken();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
                            gamedata.addGoodGuess(jsonParser.getText().charAt(0));
                        break;
                    case BAD_GUESSES:
                        jsonParser.nextToken();
                        while (jsonParser.nextToken() != JsonToken.END_ARRAY)
                            gamedata.addBadGuess(jsonParser.getText().charAt(0));
                        break;
                    default:
                        throw new JsonParseException(jsonParser, "Unable to load JSON data");
                }
            }
        }
    }
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] b = md.digest();
        StringBuffer sb = new StringBuffer();
        for(byte b1 : b){
            sb.append(Integer.toHexString(b1 & 0xff).toString());
        }
        return sb.toString();
    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
