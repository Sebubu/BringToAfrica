package models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
public class Address extends Model {
    @Id
    @GeneratedValue
    private Long id;
    private String country;
    private String city;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public static Finder<Long, Address> find = new Finder<>(
            Long.class, Address.class
    );
}
