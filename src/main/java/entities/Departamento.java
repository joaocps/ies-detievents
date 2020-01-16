/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Miguel
 */
public class Departamento implements Serializable {

    private int id;
    private String nome;

    public Departamento(String jsonEntity) throws JSONException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.nome = obj.getString("nomeDepartamento");
    }

    public Departamento() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof Departamento) {
            Departamento dep = (Departamento) other;
            result = (this.id == dep.getId()
                    && this.nome.equals(dep.getNome())
                    && this.getClass().equals(dep.getClass()));
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + this.id;
        hash = 79 * hash + Objects.hashCode(this.nome);
        return hash;
    }
}
