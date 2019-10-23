
package io.hyscale.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageRegistry implements Serializable {

    private String name;
    private String url;
    private String userName;
    private String password;
    private FileMeta sslCertificate;

    public FileMeta getSslCertificate() {
        return sslCertificate;
    }

    public void setSslCertificate(FileMeta sslCertificate) {
        this.sslCertificate = sslCertificate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
