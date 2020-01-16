/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import util.RESTClient;
import entities.Departamento;
import entities.Reserva;
import entities.Sala;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

/**
 *
 * @author Miguel
 */
@Named(value = "bookBean")
@ManagedBean
@ViewScoped
public class BookBean implements Serializable {

    private int currentUserId;
    private List<Departamento> departamentos;
    private Departamento departamento;
    private List<Sala> salas;
    private Reserva reserva;

    @PostConstruct
    public void init() {
        reserva = new Reserva();
        departamentos = RESTClient.getAllDeps();
        String id = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userId").toString();
        currentUserId = Integer.parseInt(id);
    }

    public String bookBtnClick() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;
        String redirectPage;
        String response = RESTClient.createReserva(reserva);
        if (response != null) {
            message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Classroom " + reserva.getSala().getNome() + " booked successfully");
            System.out.println("CREATE RESERVA RESPONSE: " + response);
            redirectPage = "eventsList";
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
    
    public List<Sala> getSalas() {
        return salas;
    }

    public void setSalas(List<Sala> salas) {
        this.salas = salas;
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

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}
