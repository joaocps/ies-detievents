/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import entities.Departamento;
import entities.Evento;
import entities.Reserva;
import entities.Sala;
import entities.Utilizador;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author Miguel
 */
public class RESTClient {

    // como o modulo da base de dados nao nos permite saber atraves do id, se o utilizador é um aluno ou um professor
    // temos de exprimentar fazer o pedido no URL dos alunos, e caso nao seja bem sucedido fazemos o pedido no URL dos professores
    public static Utilizador getUserById(int id) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget resource = client.target(Contract.BASE_URL).path(Contract.PATH_TO_ALUNOS)
                    .path(MessageFormat.format("{0}", new Object[]{id}));
            String json = resource.request(MediaType.APPLICATION_JSON).get(String.class);
            return new Utilizador(json, true);
        } catch (JSONException ex) {
            try {
                Client client = ClientBuilder.newClient();
                WebTarget resource = client.target(Contract.BASE_URL).path(Contract.PATH_TO_PROFESSORES)
                        .path(MessageFormat.format("{0}", new Object[]{id}));
                String json = resource.request(MediaType.APPLICATION_JSON).get(String.class);
                return new Utilizador(json, false);
            } catch (JSONException exe) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("User not found."));
                System.out.println("Aluno try: " + ex.getMessage());
                System.out.println("Prof try: " + exe.getMessage());
                return null;
            }
        }
    }

    public static void removeFollower(Evento evento, int userId) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_EVENTOS + "/" + evento.getId() + "/removePessoa/" + userId;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void addFollower(Evento evento, int userId) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_EVENTOS + "/" + evento.getId() + "/addPessoa/" + userId;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static Evento getEventById(String id) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_EVENTOS)
                    .path(MessageFormat.format("{0}", new Object[]{id}));
            String jsonEvento = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            System.out.println("getEventById: " + jsonEvento);
            return new Evento(jsonEvento);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Event not found."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static String createReserva(Reserva reserva) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_RESERVAS;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(reserva.toCreateJSON());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse result = httpClient.execute(request);
            if (result.getEntity() != null) {
                return EntityUtils.toString(result.getEntity(), "UTF-8");
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static List<Sala> convertSalasToList(String jsonListString, int depId) throws JSONException {
        JSONArray jsonList = new JSONArray(jsonListString);
        List<Sala> list = new ArrayList<>();
        for (int i = 0; i < jsonList.length(); i++) {
            Sala entity = new Sala(jsonList.get(i).toString());
            if (entity.getDep().getId() == depId || depId < 0) {
                list.add(entity);
            }
        }
        return list;
    }

    public static List<Sala> getSalasFromDep(int depId) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_SALAS);
            String jsonListString = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return convertSalasToList(jsonListString, depId);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Classrooms not found."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private static List<Departamento> convertDepsToList(String jsonListString) throws JSONException {
        JSONArray jsonList = new JSONArray(jsonListString);
        List<Departamento> list = new ArrayList<>();
        for (int i = 0; i < jsonList.length(); i++) {
            Departamento entity = new Departamento(jsonList.get(i).toString());
            list.add(entity);
        }
        return list;
    }

    public static List<Departamento> getAllDeps() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_DEPARTAMENTOS);
            String jsonListString = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return convertDepsToList(jsonListString);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Departments not found."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static Reserva getReservaById(int id) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_RESERVAS)
                    .path(MessageFormat.format("{0}", new Object[]{id}));
            String json = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return new Reserva(json);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Event not found."));
            System.out.println(ex.getMessage());
        } catch (ParseException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Invalid date."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static Sala getSalaById(int id) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_SALAS)
                    .path(MessageFormat.format("{0}", new Object[]{id}));
            String json = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return new Sala(json);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Classroom not found."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public static Departamento getDepById(int id) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_DEPARTAMENTOS)
                    .path(MessageFormat.format("{0}", new Object[]{id}));
            String json = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return new Departamento(json);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Evento não encontrado."));
            System.out.println(ex.getMessage());
        }
        return null;
    }
}
