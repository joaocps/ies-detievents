/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
public class Reserva implements Serializable {

    private int id;
    private Date dataInicio;
    private Date dataFim;
    private Sala sala;

    public Reserva(String jsonEntity) throws JSONException, ParseException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.sala = RESTClient.getSalaById(obj.getInt("sala"));
        this.dataInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(obj.getString("dataI"));
        this.dataFim = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(obj.getString("dataF"));
    }

    public Reserva() {
        sala = new Sala();
    }

    public String toCreateJSON() {
        JSONObject obj = new JSONObject();
        obj.put("sala", sala.getId());
        obj.put("dataI", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataInicio));
        obj.put("dataF", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataFim));
        return obj.toString();
    }
    
     public String toEditJSON() {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("sala", sala.getId());
        obj.put("dataI", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataInicio));
        obj.put("dataF", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataFim));
        return obj.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(Date dataFim) {
        this.dataFim = dataFim;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }
}
