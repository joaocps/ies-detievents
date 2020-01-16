/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.login;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 *
 * @author Miguel
 */
public class ChangePassRequest {

    private String oldpassword;
    private String newpassword;

    public String getOldpassword() {
        return oldpassword;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }

    public String toJSON() {

        String oldhashPass = Hashing.sha256().hashString(oldpassword, StandardCharsets.UTF_8).toString();
        //String newhashPass = Hashing.sha256().hashString(newpassword, StandardCharsets.UTF_8).toString(); 

        JSONObject obj = new JSONObject();
        obj.put("oldPassword", oldhashPass);
        obj.put("password", newpassword);
        return obj.toString();
    }
}
