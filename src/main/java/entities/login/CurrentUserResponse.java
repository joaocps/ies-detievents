/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.login;

import java.io.Serializable;
import org.json.JSONObject;

/**
 *
 * @author Miguel
 */
public class CurrentUserResponse implements Serializable {

    private int userId;
    private String username;
    private String firstname;
    private String lastname;
    private String email;

    public CurrentUserResponse(String jsonEntity) {
        JSONObject obj = new JSONObject(jsonEntity);
        this.userId = obj.getInt("userId");
        this.username = obj.getString("username");
        this.firstname = obj.getString("firstname");
        this.lastname = obj.getString("lastname");
        this.email = obj.getString("email");
    }

    public String toEditUserJSONRequset() {
        JSONObject obj = new JSONObject();
        obj.put("firstname", firstname);
        obj.put("lastname", lastname);
        obj.put("email", email);
        return obj.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
