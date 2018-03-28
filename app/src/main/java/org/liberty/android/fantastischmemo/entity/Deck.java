package org.liberty.android.fantastischmemo.entity;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.liberty.android.fantastischmemo.dao.DeckDaoImpl;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable(tableName = "decks", daoClass = DeckDaoImpl.class)
public class Deck implements Serializable{
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(defaultValue = "", width = 8192)
    private String name;

    @DatabaseField(defaultValue = "", width = 8192)
    private String description;

    @DatabaseField
    private String dbPath;

    @DatabaseField(format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date creationDate;

    @DatabaseField(version = true, format="yyyy-MM-dd HH:mm:ss.SSSSSS", dataType=DataType.DATE_STRING)
    private Date updateDate;

    @DatabaseField(defaultValue = "0")
    private Integer rating;

    public Deck() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public Integer getRating() {return this.rating;}

    public void setRating(int rating) {this.rating = rating;}
}
