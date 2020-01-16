/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.login;

import com.google.common.hash.Hashing;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 *
 * @author Miguel
 */
public class LoginRequest implements Serializable {

    private String username;
    private String password;

    public LoginRequest() {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJSON() {
        String hashPass = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
        JSONObject obj = new JSONObject();
        obj.put("username", username);
        obj.put("password", hashPass);
        return obj.toString();
    }
}
