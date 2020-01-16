/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import util.RESTClient;
import entities.Departamento;
import entities.Evento;
import entities.Reserva;
import entities.Sala;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import util.Contract;

/**
 *
 * @author Miguel
 */
@Named(value = "eventCreateBean")
@ManagedBean
@ViewScoped
public class EventCreateBean implements Serializable {

    private Evento evento;
    private int currentUserId;
    private List<Departamento> departamentos;
    private Departamento departamento;
    private List<Sala> salas;

    
    public EventCreateBean() {
        evento = new Evento();
        departamentos = RESTClient.getAllDeps();
        String id = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId").toString();
        currentUserId = Integer.parseInt(id);
    }

    public String createEventBtnClick() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        String redirectPage;
        String response = RESTClient.createReserva(evento.getReserva());
        if (response != null) {
            System.out.println("CREATE RESERVA RESPONSE: " + response);
            try {
                Reserva r = new Reserva(response);
                evento.getReserva().setId(r.getId());
                evento.setIdAdmin(currentUserId);
                createEvento(evento);
                message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Event " + evento.getNome() + " created successfully!");
                redirectPage = "eventsList";
            } catch (JSONException | ParseException ex) {
                System.out.println(ex.getMessage());
                message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Book Error", "Classroom unavailable in that date.");
                redirectPage = "";
            }
        } else {
            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Book Error", "Classroom unavailable in that date.");
            redirectPage = "";
        }
        facesContext.addMessage(null, message);
        return redirectPage;
    }

    public void updateSalasList() {
        salas = RESTClient.getSalasFromDep(departamento.getId());
    }

    private void createEvento(Evento evento) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_EVENTOS;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(evento.toCreateJSON());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            System.out.println("CREATE REQUEST: " + evento.toCreateJSON());
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos = departamentos;
    }

    public Departamento getDepartamento() {
        return departamento;
    }

    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }

    public List<Sala> getSalas() {
        return salas;
    }

    public void setSalas(List<Sala> salas) {
        this.salas = salas;
    }
}
