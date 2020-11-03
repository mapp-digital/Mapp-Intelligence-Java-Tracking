package demo.models;

import demo.entities.User;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class UserModel {
    private static final String JSON_USERS = Objects.requireNonNull(ProductModel.class.getClassLoader().getResource("users.json")).getFile();
    private JSONObject jsonUsers = null;

    public UserModel() {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSON_USERS)) {
            this.jsonUsers = (JSONObject) jsonParser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private User save(User user) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("FirstName", user.getFirstName());
        jsonObject.put("LastName", user.getLastName());
        jsonObject.put("Password", user.getPassword());
        jsonObject.put("Email", user.getEmail());
        jsonObject.put("Telephone", user.getTelephone());

        this.jsonUsers.put(user.getEmail(), jsonObject);

        try (FileWriter file = new FileWriter(JSON_USERS)) {
            file.write(this.jsonUsers.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;
    }

    public User register(String mail, String password, String firstName, String lastName, String telephone) {
        User user = new User(mail, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setTelephone(telephone);

        if (this.find(mail) == null) {
            return this.save(user);
        }

        return null;
    }

    public User login(String mail, String password) {
        User user = this.find(mail);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    public User find(String mail) {
        User user = null;
        JSONObject jsonUser = (JSONObject) this.jsonUsers.get(mail);
        if (jsonUser != null) {
            user = new User();
            user.setFirstName((String) jsonUser.get("FirstName"));
            user.setLastName((String) jsonUser.get("LastName"));
            user.setPassword((String) jsonUser.get("Password"));
            user.setEmail((String) jsonUser.get("Email"));
            user.setTelephone((String) jsonUser.get("Telephone"));
        }

        return user;
    }
}
