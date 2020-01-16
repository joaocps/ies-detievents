/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import entities.Sala;
import java.util.Iterator;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Miguel
 */
@FacesConverter("salaConverter")
public class SalaConverter implements Converter {

    private List<Sala> objects;

    public SalaConverter() {
        this.objects = RESTClient.getSalasFromDep(-1);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value.isEmpty()) {
            return null;
        }
        return this.getMyObject(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        return ((Sala) value).getNome();
    }

    public Sala getMyObject(String nome) {
        Iterator<Sala> iterator = this.objects.iterator();
        while (iterator.hasNext()) {
            Sala object = iterator.next();

            if (object.getNome().equals(nome)) {
                return object;
            }
        }
        return null;
    }

}
