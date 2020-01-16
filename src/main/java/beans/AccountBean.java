/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entities.Departamento;
import entities.Utilizador;
import entities.login.ChangePassRequest;
import entities.login.CurrentUserResponse;
import java.io.IOException;
import javax.inject.Named;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.primefaces.context.RequestContext;
import util.Contract;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
@Named(value = "accountBean")
@ManagedBean
@ViewScoped
public class AccountBean implements Serializable {

    private final WebTarget webTarget;
    private final Client client;
    private Utilizador user;
    private List<Departamento> departamentos;

    private String token;
    private String id;

    private CurrentUserResponse currentUser;
    private ChangePassRequest passRequest;
    private final String PATH_TO_USER;
    private boolean isAluno;

    public AccountBean() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        if (facesContext.getExternalContext().getSessionMap().get("token") != null) {
            token = facesContext.getExternalContext().getSessionMap().get("token").toString();
            String responseJSON = getCurrentUser(token);
            JSONObject responseObj = new JSONObject(responseJSON);
            JSONObject currentUserJSON = (JSONObject) responseObj.get("response");
            System.out.println("Current User: " + currentUserJSON.toString());
            currentUser = new CurrentUserResponse(currentUserJSON.toString());

            id = facesContext.getExternalContext().getSessionMap().get("userId").toString();
        } else {
            try {
                facesContext.getExternalContext().redirect("/IES_EventsUI/");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        
        String type = facesContext.getExternalContext().getSessionMap().get("userType").toString();
        isAluno = type.equals("aluno");

        if (isAluno) {
            PATH_TO_USER = Contract.PATH_TO_ALUNOS;
        } else {
            PATH_TO_USER = Contract.PATH_TO_PROFESSORES;
        }

        client = ClientBuilder.newClient();
        webTarget = client.target(Contract.BASE_URL).path(PATH_TO_USER);
        user = RESTClient.getUserById(Integer.parseInt(id));
        departamentos = RESTClient.getAllDeps();
        passRequest = new ChangePassRequest();
    }

    private void deleteUser(String id) {
        WebTarget resource = webTarget.path(MessageFormat.format("{0}", new Object[]{id}));
        resource.request().delete();
    }

    private void editUser(Utilizador user) {
        String url = Contract.BASE_URL + "/" + PATH_TO_USER + "/" + id;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(url);
            StringEntity params;
            if (isAluno) {
                params = new StringEntity(user.toAlunoJSON());
            } else {
                params = new StringEntity(user.toProfessorJSON());
            }
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    // User from login module
    public String getCurrentUser(String token) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String url = Contract.BASE_URL_LOGIN + "/info";
            HttpGet request = new HttpGet(url);
            request.addHeader("Auth-token", "52b5f98e-761e-42e7-9311-c5fc257f4f64");
            request.addHeader("token", token);
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return json;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public void saveBtnClick(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        boolean saveSuccess;

        String url = Contract.BASE_URL_LOGIN + "/user/edit/" + currentUser.getUserId();
        String jsonResponse = sendPUT(url, currentUser.toEditUserJSONRequset(), token);

        System.out.println("Save: " + jsonResponse);

        JSONObject obj = new JSONObject(jsonResponse);
        int code = obj.getInt("code");
        String requestMessage = obj.getString("message");

        if (code == 200) {
            saveSuccess = true;

            user.setId(Integer.parseInt(id));
            user.setEmail(currentUser.getEmail());
            user.setFirstname(currentUser.getFirstname());
            user.setLastname(currentUser.getLastname());
            editUser(user);

            message = new FacesMessage("Edit Successfuly");
        } else {
            saveSuccess = false;
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Edit Error", requestMessage);
        }

        facesContext.addMessage(null, message);
        context.addCallbackParam("saveSuccess", saveSuccess);
    }

    public void deleteBtnClick(ActionEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;

        String url = Contract.BASE_URL_LOGIN + "/user/delete/" + currentUser.getUserId();
        String jsonResponse = sendDELETE(url, token);

        System.out.println("Delete Response: " + jsonResponse);

        JSONObject obj = new JSONObject(jsonResponse);
        int code = obj.getInt("code");
        String requestMessage = obj.getString("message");

        if (code == 200) {
            deleteUser(id);
            message = new FacesMessage("Delete Successfuly");
            facesContext.addMessage(null, message);

            try {
                facesContext.getExternalContext().redirect("/IES_EventsUI/");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Delete Error", requestMessage);
            facesContext.addMessage(null, message);
        }
    }

    public void passBtnClick(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        boolean success;

        String url = Contract.BASE_URL_LOGIN + "/user/edit/" + currentUser.getUserId();
        String jsonResponse = sendPUT(url, passRequest.toJSON(), token);

        System.out.println("Change Pass Response: " + jsonResponse);

        JSONObject obj = new JSONObject(jsonResponse);
        int code = obj.getInt("code");
        String requestMessage = obj.getString("message");

        if (code == 200) {
            success = true;
            message = new FacesMessage("Changed Password Successfuly");
        } else {
            success = false;
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Change Password Error", requestMessage);
        }

        facesContext.addMessage(null, message);
        context.addCallbackParam("success", success);
    }

    public String sendPUT(String url, String body, String token) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(url);
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.addHeader("Auth-token", "52b5f98e-761e-42e7-9311-c5fc257f4f64");
            request.addHeader("token", token);
            request.setEntity(params);
            System.out.println("PUT Request: " + body);
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return json;

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public String sendDELETE(String url, String token) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpDelete request = new HttpDelete(url);
            request.addHeader("Auth-token", "52b5f98e-761e-42e7-9311-c5fc257f4f64");
            request.addHeader("token", token);
            HttpResponse result = httpClient.execute(request);
            System.out.println("DELETE Request URL: " + url);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return json;

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return "Exception found";
        }
    }

    public CurrentUserResponse getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUserResponse currentUser) {
        this.currentUser = currentUser;
    }

    public ChangePassRequest getPassRequest() {
        return passRequest;
    }

    public void setPassRequest(ChangePassRequest passRequest) {
        this.passRequest = passRequest;
    }

    public Utilizador getUser() {
        return user;
    }

    public void setUser(Utilizador user) {
        this.user = user;
    }

    public boolean isIsAluno() {
        return isAluno;
    }

    public void setIsAluno(boolean isAluno) {
        this.isAluno = isAluno;
    }

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos = departamentos;
    }
}
