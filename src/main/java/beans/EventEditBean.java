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
import java.util.List;
import java.util.Map;
import javax.annotation.ManagedBean;
import javax.inject.Named;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import util.Contract;

/**
 *
 * @author Miguel
 */
@Named(value = "eventEditBean")
@ManagedBean
@ViewScoped
public class EventEditBean implements Serializable {

    private Evento evento;
    private List<Departamento> departamentos;
    private List<Sala> salas;

    public EventEditBean() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = params.get("id");
        evento = RESTClient.getEventById(id);
        departamentos = RESTClient.getAllDeps();
        salas = RESTClient.getSalasFromDep(evento.getReserva().getSala().getDep().getId());
    }

    public String editBtnClick() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        String redirectPage;
        editReserva(evento.getReserva());
        // o grupo da base de dados nao esta a enviar resposta com a confirmaçao da disponibilidade da sala
        //String response = editReserva(evento.getReserva());
        editReserva(evento.getReserva());
//        if (response != null) {
//            System.out.println("EDIT RESERVA RESPONSE: " + response);

            editEvento(evento);
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Event " + evento.getNome() + " edited successfully!");
            redirectPage = "eventsList";
//        } else {
//            message = new FacesMessage(FacesMessage.SEVERITY_WARN, "Book Error", "Classroom unavailable in that date.");
//            redirectPage = "";
//        }
        facesContext.addMessage(null, message);
        return redirectPage;
    }

    private void editEvento(Evento evento) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_EVENTOS + "/" + evento.getId();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(url);
            StringEntity params = new StringEntity(evento.toEditJSON());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            httpClient.execute(request);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String editReserva(Reserva reserva) {
        String url = Contract.BASE_URL + "/" + Contract.PATH_TO_RESERVAS + "/" + reserva.getId();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPut request = new HttpPut(url);
            StringEntity params = new StringEntity(reserva.toEditJSON());
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

    public void updateSalasList() {
        salas = RESTClient.getSalasFromDep(evento.getReserva().getSala().getDep().getId());
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos = departamentos;
    }

    public List<Sala> getSalas() {
        return salas;
    }

    public void setSalas(List<Sala> salas) {
        this.salas = salas;
    }
}
