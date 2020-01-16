/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.Objects;
import util.RESTClient;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Miguel
 */
public class Sala implements Serializable {

    private int id;
    private String nome;
    private Departamento dep;

    public Sala(String jsonEntity) throws JSONException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.nome = obj.getString("nome");
        this.dep = RESTClient.getDepById(obj.getInt("idDepartamento"));
    }

    public Sala() {
        dep = new Departamento();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.id;
        hash = 29 * hash + Objects.hashCode(this.nome);
        hash = 29 * hash + Objects.hashCode(this.dep);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Sala other = (Sala) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.nome, other.nome)) {
            return false;
        }
        if (!Objects.equals(this.dep, other.dep)) {
            return false;
        }
        return true;
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
    public String toString() {
        return "Sala{" + "id=" + id + ", nome=" + nome + ", depNome=" + dep.getNome() + '}';
    }

    public Departamento getDep() {
        return dep;
    }

    public void setDep(Departamento dep) {
        this.dep = dep;
    }
}
