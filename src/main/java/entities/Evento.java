/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
public class Evento implements Serializable {

    private int id;
    private String nome;
    private String descricao;
    private int idAdmin;
    private Reserva reserva;
    private String tipo;
    private String nomeAdmin;
    private boolean currentUserIsFollowing;
    private List<Integer> followers;

    public Evento(String jsonEntity) throws JSONException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.nome = obj.getString("nome");
        this.descricao = obj.getString("descricao");
        this.idAdmin = obj.getInt("idAdmin");
        this.reserva = RESTClient.getReservaById(obj.getInt("idReserva"));

        //Utilizador user = RESTClient.getUserById(idAdmin + "", false);
        Utilizador user = RESTClient.getUserById(idAdmin);
        this.nomeAdmin = user.getFirstname() + " " + user.getLastname();

        if (obj.has("tipo")) {
            this.tipo = obj.getString("tipo").toLowerCase();
        }
        currentUserIsFollowing = false;
        followers = new ArrayList<>();
    }

    public Evento(String jsonEntity, int currentUser) throws JSONException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.nome = obj.getString("nome");
        this.descricao = obj.getString("descricao");
        this.idAdmin = obj.getInt("idAdmin");
        this.reserva = RESTClient.getReservaById(obj.getInt("idReserva"));

        Utilizador user = RESTClient.getUserById(idAdmin);
        this.nomeAdmin = user.getFirstname() + " " + user.getLastname();

        if (obj.has("tipo")) {
            this.tipo = obj.getString("tipo").toLowerCase();
        }

        currentUserIsFollowing = false;
        followers = new ArrayList<>();
        if (obj.has("seguidores")) {
            JSONArray jsonArray = obj.getJSONArray("seguidores");
            for (int i = 0; i < jsonArray.length(); ++i) {
                if (jsonArray.optInt(i) == currentUser) {
                    currentUserIsFollowing = true;
                }
                followers.add(jsonArray.optInt(i));
            }
        }
    }

    public String toCreateJSON() {
        JSONObject obj = new JSONObject();
        obj.put("descricao", this.descricao);
        obj.put("idAdmin", this.idAdmin);
        obj.put("nome", this.nome);
        obj.put("tipo", this.tipo);
        obj.put("idReserva", this.reserva.getId());
        return obj.toString();
    }

    public String toEditJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", this.id);
        obj.put("descricao", this.descricao);
        obj.put("idAdmin", this.idAdmin);
        obj.put("nome", this.nome);
        obj.put("tipo", this.tipo);
        obj.put("idReserva", this.reserva.getId());
        return obj.toString();
    }

    public Evento() {
        reserva = new Reserva();
        followers = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getNomeAdmin() {
        return nomeAdmin;
    }

    public void setNomeAdmin(String nomeAdmin) {
        this.nomeAdmin = nomeAdmin;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public boolean isCurrentUserIsFollowing() {
        return currentUserIsFollowing;
    }

    public void setCurrentUserIsFollowing(boolean currentUserIsFollowing) {
        this.currentUserIsFollowing = currentUserIsFollowing;
    }

    public List<Integer> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Integer> followers) {
        this.followers = followers;
    }
}
