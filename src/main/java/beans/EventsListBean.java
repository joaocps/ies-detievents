/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import entities.Evento;
import entities.Utilizador;
import java.io.IOException;
import javax.inject.Named;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.primefaces.context.RequestContext;
import util.Contract;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
@Named(value = "eventsListBean")
@ManagedBean
@ViewScoped
public class EventsListBean implements Serializable {

    private final int currentUserId;
    private final Utilizador currentUser;

    private boolean selectedEventEditable;
    private Evento selectedEvent;
    private List<Utilizador> selectedEventFollowers;
    private List<Evento> events;

    private String searchName;
    private boolean searchOnlyMine;

    public EventsListBean() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String id = facesContext.getExternalContext().getSessionMap().get("userId").toString();
        currentUserId = Integer.parseInt(id);
        String type = facesContext.getExternalContext().getSessionMap().get("userType").toString();
        currentUser = RESTClient.getUserById(currentUserId);
        searchName = "";
        searchOnlyMine = false;
        events = new ArrayList<>();
        selectedEventFollowers = new ArrayList<>();
        orderedEvents();
    }

    public void orderedEvents() {
        List<Evento> aux = searchEvents();
        events.clear();
        Date currentDate = new Date();

        // first appear the future events, followed by the current user, that have the same departement
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getReserva().getDataFim().after(currentDate)
                    && aux.get(i).isCurrentUserIsFollowing()
                    && aux.get(i).getReserva().getSala().getDep().getId() == currentUser.getDep().getId()) {
                System.out.println("1. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear the future events, followed by the current user
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getReserva().getDataFim().after(currentDate)
                    && aux.get(i).isCurrentUserIsFollowing()) {
                System.out.println("2. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear the future events, that have the same departement
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getReserva().getDataFim().after(currentDate)
                    && aux.get(i).getReserva().getSala().getDep().getId() == currentUser.getDep().getId()) {
                System.out.println("3. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear the future events
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getReserva().getDataFim().after(currentDate)) {
                System.out.println("4. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear events followed by the current user, that have the same departement
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).isCurrentUserIsFollowing()
                    && aux.get(i).getReserva().getSala().getDep().getId() == currentUser.getDep().getId()) {
                System.out.println("5. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear events followed by the current user
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).isCurrentUserIsFollowing()) {
                System.out.println("6. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        // then appear events that have the same departement
        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getReserva().getSala().getDep().getId() == currentUser.getDep().getId()) {
                System.out.println("7. " + aux.get(i).getNome());
                events.add(aux.get(i));
                aux.remove(i);
                i--;
            }
        }

        for (int i = 0; i < aux.size(); i++) {
            System.out.println("8. " + aux.get(i).getNome());
            events.add(aux.get(i));
        }
    }

    private List<Evento> searchEvents() {
        List<Evento> allEvents = findAllEvents();
        List<Evento> aux = new ArrayList<>();
        if (searchName.isEmpty() && !searchOnlyMine) {
            aux = new ArrayList<>(allEvents);
        } else if (!searchName.isEmpty() && !searchOnlyMine) {
            for (int i = 0; i < allEvents.size(); i++) {
                if (allEvents.get(i).getNome().toLowerCase().contains(searchName.toLowerCase())) {
                    aux.add(allEvents.get(i));
                }
            }
        } else if (searchName.isEmpty() && searchOnlyMine) {
            for (int i = 0; i < allEvents.size(); i++) {
                if (allEvents.get(i).getIdAdmin() == currentUserId) {
                    aux.add(allEvents.get(i));
                }
            }
        } else {
            for (int i = 0; i < allEvents.size(); i++) {
                if (allEvents.get(i).getIdAdmin() == currentUserId && allEvents.get(i).getNome().toLowerCase().contains(searchName.toLowerCase())) {
                    aux.add(allEvents.get(i));
                }
            }
        }
        return aux;
    }

    private List<Evento> convertToList(String jsonListString) throws JSONException, ParseException {
        JSONArray jsonList = new JSONArray(jsonListString);
        List<Evento> list = new ArrayList<>();
        for (int i = 0; i < jsonList.length(); i++) {
            Evento entity = new Evento(jsonList.get(i).toString(), currentUserId);
            list.add(entity);
        }
        return list;
    }

    private List<Evento> findAllEvents() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_EVENTOS);
            String jsonListString = webTarget.request(MediaType.APPLICATION_JSON).get(String.class);
            return convertToList(jsonListString);
        } catch (JSONException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Evento não encontrado."));
            System.out.println(ex.getMessage());
        } catch (ParseException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Data Inválida."));
            System.out.println(ex.getMessage());
        }
        return null;
    }

    public void deleteBtnClick() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message;

        deleteReserva(selectedEvent.getReserva().getId());
        deleteEvento(selectedEvent.getId());
        message = new FacesMessage("Delete Successfuly");
        facesContext.addMessage(null, message);
        try {
            facesContext.getExternalContext().redirect("/IES_EventsUI/faces/eventsList.xhtml?i=2");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void deleteEvento(int id) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_EVENTOS)
                .path(MessageFormat.format("{0}", new Object[]{id}));
        webTarget.request().delete();
    }

    private void deleteReserva(int id) {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(Contract.BASE_URL).path(Contract.PATH_TO_RESERVAS)
                .path(MessageFormat.format("{0}", new Object[]{id}));
        webTarget.request().delete();
    }

    public Evento getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Evento selectedEvent) {
        this.selectedEvent = selectedEvent;
        setSelectedEventEditable();
        RequestContext.getCurrentInstance().update("form:btns");

        selectedEventFollowers.clear();
        for (int i = 0; i < selectedEvent.getFollowers().size(); i++) {
            selectedEventFollowers.add(RESTClient.getUserById(selectedEvent.getFollowers().get(i)));
        }
    }

    public boolean isSelectedEventEditable() {
        return selectedEventEditable;
    }

    public void setSelectedEventEditable() {
        this.selectedEventEditable = selectedEvent.getIdAdmin() == currentUserId;
    }

    public void setFollower(Evento evento) {

        if (evento.isCurrentUserIsFollowing()) {
            RESTClient.addFollower(evento, currentUserId);
        } else {
            RESTClient.removeFollower(evento, currentUserId);
        }

        String summary = evento.isCurrentUserIsFollowing() ? "Following" : "Not Following";
        summary += " " + evento.getNome();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(summary));
    }

    public int getNumberOfEvents() {
        return events.size();
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public boolean isSearchOnlyMine() {
        return searchOnlyMine;
    }

    public void setSearchOnlyMine(boolean searchOnlyMine) {
        this.searchOnlyMine = searchOnlyMine;
    }

    public List<Evento> getEvents() {
        return events;
    }

    public void setEvents(List<Evento> events) {
        this.events = events;
    }

    public List<Utilizador> getSelectedEventFollowers() {
        return selectedEventFollowers;
    }

    public void setSelectedEventFollowers(List<Utilizador> selectedEventFollowers) {
        this.selectedEventFollowers = selectedEventFollowers;
    }

}
