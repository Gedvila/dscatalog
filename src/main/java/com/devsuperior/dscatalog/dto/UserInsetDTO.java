package com.devsuperior.dscatalog.dto;

public class UserInsetDTO extends UserDTO{

    private String password;

    public UserInsetDTO(){
        super();
    }



    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
