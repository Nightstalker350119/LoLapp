package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class PassiveDTO {
    @SerializedName("image")
    private ImageDTO image;

    @SerializedName("sanitizedDescription")
    private String sanitizedDescription;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    public PassiveDTO(ImageDTO image, String sanitizedDescription,
                      String name, String description) {
        this.image = image;
        this.sanitizedDescription = sanitizedDescription;
        this.name = name;
        this.description = description;
    }

    public ImageDTO getImage() {
        return image;
    }

    public void setImage(ImageDTO image) {
        this.image = image;
    }

    public String getSanitizedDescription() {
        return sanitizedDescription;
    }

    public void setSanitizedDescription(String sanitizedDescription) {
        this.sanitizedDescription = sanitizedDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
