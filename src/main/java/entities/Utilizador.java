/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import org.json.JSONException;
import org.json.JSONObject;
import util.RESTClient;

/**
 *
 * @author Miguel
 */
public class Utilizador {

    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private Departamento dep;
    private int nmec;
    private String curso;

    public Utilizador(String jsonEntity, boolean isAluno) throws JSONException {
        JSONObject obj = new JSONObject(jsonEntity);
        this.id = obj.getInt("id");
        this.firstname = obj.getString("firstName");
        this.lastname = obj.getString("lastName");
        this.dep = RESTClient.getDepById(obj.getInt("departamento"));
        this.nmec = obj.getInt("nmec");
        if (isAluno) {
            this.curso = obj.getString("curso");
        }
    }

    public String toAlunoJSON() {
        JSONObject obj = new JSONObject();
        obj.put("departamento", dep.getId());
        obj.put("email", email);
        obj.put("firstName", firstname);
        obj.put("lastName", lastname);
        obj.put("id", id);
        obj.put("curso", curso);
        obj.put("nmec", nmec);
        return obj.toString();
    }

    public String toProfessorJSON() {
        JSONObject obj = new JSONObject();
        obj.put("departamento", dep.getId());
        obj.put("email", email);
        obj.put("firstName", firstname);
        obj.put("lastName", lastname);
        obj.put("id", id);
        obj.put("nmec", nmec);
        return obj.toString();
    }

    public Utilizador() {
        Departamento dep = new Departamento();
    }

    public int getNmec() {
        return nmec;
    }

    public void setNmec(int nmec) {
        this.nmec = nmec;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Departamento getDep() {
        return dep;
    }

    public void setDep(Departamento dep) {
        this.dep = dep;
    }
}
