/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entities.Departamento;
import entities.Utilizador;
import entities.login.LoginRequest;
import entities.login.RegisterRequest;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.primefaces.context.RequestContext;
import util.Contract;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
@Named(value = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private final WebTarget webTarget;
    private final Client client;
    private Utilizador user;
    private List<Departamento> departamentos;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    private String userType;
    private boolean isAluno;

    public LoginBean() {
        client = ClientBuilder.newClient();
        webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_PROFESSORES);
        isAluno = true;
        loginRequest = new LoginRequest();
        registerRequest = new RegisterRequest();
        user = new Utilizador();
        departamentos = RESTClient.getAllDeps();
    }

    public void loginBtnClick(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        boolean loggedIn;

        String url = Contract.BASE_URL_LOGIN + "/login";

        String jsonResponse = sendPOST(url, loginRequest.toJSON());

        System.out.println("Login: " + jsonResponse);

        JSONObject obj = new JSONObject(jsonResponse);
        int code = obj.getInt("code");
        String requestMessage = obj.getString("message");

        if (code == 200) {
            loggedIn = true;

            JSONObject jsonUser = (JSONObject) obj.get("response");
            String token = jsonUser.getString("token");
            int id = jsonUser.getInt("userId");
            String firstName = jsonUser.getString("firstname");
            String lastName = jsonUser.getString("lastname");

            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", firstName + " " + lastName);

            if (facesContext.getExternalContext().getSessionMap().get("token") != null) {
                String oldToken = facesContext.getExternalContext().getSessionMap().get("token").toString();

                String logoutResponse = logout(oldToken);

                System.out.println("Logout: " + logoutResponse);

                facesContext.getExternalContext().invalidateSession();
            }

            Utilizador professor = getProfessorById(id);

            if (professor != null) {
                facesContext.getExternalContext().getSessionMap().put("userType", "professor");
                System.out.println("OLA PROFESSOR");
                isAluno = false;
            } else {
                facesContext.getExternalContext().getSessionMap().put("userType", "aluno");
                System.out.println("OLA ALUNO");
                isAluno = true;
            }

            facesContext.getExternalContext().getSessionMap().put("token", token);
            facesContext.getExternalContext().getSessionMap().put("userId", id);

        } else {
            loggedIn = false;
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", requestMessage);
        }

        facesContext.addMessage(null, message);
        context.addCallbackParam("loggedIn", loggedIn);
    }

    public void logoutBtnClick() {
        FacesMessage message;
        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (facesContext.getExternalContext().getSessionMap().get("token") != null) {
            String oldToken = facesContext.getExternalContext().getSessionMap().get("token").toString();

            String logoutResponse = logout(oldToken);

            System.out.println("Logout: " + logoutResponse + " Token: " + oldToken);

            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Goodbye", "See ya");

            facesContext.getExternalContext().invalidateSession();
        } else {
            System.out.println("Already logged out");
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Already logged out", "No active user session");
        }

        facesContext.addMessage(null, message);

        try {
            facesContext.getExternalContext().redirect("/IES_EventsUI/");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void registerBtnClick(ActionEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        boolean regSuccess = false;

        if (!registerRequest.getPassword().equals(registerRequest.getRePassword())) {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Register Error", "Passwords dont match.");

            facesContext.addMessage(null, message);
            context.addCallbackParam("regSuccess", regSuccess);
            return;
        }

        String url = Contract.BASE_URL_LOGIN + "/user/register";
        String jsonResponse = sendPOST(url, registerRequest.toJSON());

        System.out.println("Register: " + jsonResponse);

        JSONObject obj = new JSONObject(jsonResponse);
        int code = obj.getInt("code");
        String requestMessage = obj.getString("message");

        if (code == 200) {
            regSuccess = true;

            JSONObject jsonUser = (JSONObject) obj.get("user");
            int id = jsonUser.getInt("userId");
            String firstName = jsonUser.getString("firstname");
            String lastName = jsonUser.getString("lastname");

            user.setId(id);
            user.setEmail(registerRequest.getEmail());
            user.setFirstname(registerRequest.getFirstname());
            user.setLastname(registerRequest.getLastname());
            if (isAluno) {
                createUser(user, Contract.PATH_TO_ALUNOS);
            } else {
                createUser(user, Contract.PATH_TO_PROFESSORES);
            }

            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Welcome", firstName + " " + lastName);
            facesContext.addMessage(null, new FacesMessage("You can Log In now"));
        } else {
            regSuccess = false;
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Register Error", requestMessage);
        }

        facesContext.addMessage(null, message);
        context.addCallbackParam("regSuccess", regSuccess);
    }

    public Utilizador getProfessorById(int id) {
        try {
            WebTarget resource = webTarget.path(MessageFormat.format("{0}", new Object[]{id}));
            String json = resource.request(MediaType.APPLICATION_JSON).get(String.class);
            return new Utilizador(json, false);
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    private void createUser(Utilizador user, String type) {
        String url = Contract.BASE_URL + "/" + type;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            StringEntity params;
            if (type.equals(Contract.PATH_TO_PROFESSORES)) {
                params = new StringEntity(user.toProfessorJSON());
            } else {
                params = new StringEntity(user.toAlunoJSON());
            }

            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String logout(String token) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            String url = Contract.BASE_URL_LOGIN + "/logout";
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

    public String sendPOST(String url, String body) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.addHeader("Auth-token", "52b5f98e-761e-42e7-9311-c5fc257f4f64");
            request.setEntity(params);

            System.out.println("Post Requset: " + body);
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            return json;

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public void updateForm() {
        if (userType.equals("professor")) {
            isAluno = false;
        } else if (userType.equals("aluno") || userType.equals("")) {
            isAluno = true;
        }
    }

    public Utilizador getUser() {
        return user;
    }

    public void setUser(Utilizador user) {
        this.user = user;
    }

    public LoginRequest getLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(LoginRequest loginRequest) {
        this.loginRequest = loginRequest;
    }

    public RegisterRequest getRegisterRequest() {
        return registerRequest;
    }

    public void setRegisterRequest(RegisterRequest registerRequest) {
        this.registerRequest = registerRequest;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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
