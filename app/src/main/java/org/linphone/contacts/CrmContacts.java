package org.linphone.contacts;

public class CrmContacts {
    private String id, name, phone, email, property, create_date, tenant;

    public CrmContacts(
            String id,
            String name,
            String phone,
            String email,
            String property,
            String create_date,
            String tenant) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.property = property;
        this.create_date = create_date;
        this.tenant = tenant;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
}
