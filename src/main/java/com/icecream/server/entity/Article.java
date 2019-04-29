package com.icecream.server.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: Daniel
 */
@Entity
@Table(name = "article")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String title;

    private String link;

    @Lob
    @Column(length = 10000)
    private String description;

    @Column(name = "published_time")
    private Date publishedTime;

    @ManyToOne
    private RssFeed rssFeedEntity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getPublishedTime() { return publishedTime; }

    public void setPublishedTime(Date publishedTime) { this.publishedTime = publishedTime; }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public RssFeed getRssFeedEntity() {
        return rssFeedEntity;
    }

    public void setRssFeedEntity(RssFeed rssFeedEntity) {
        this.rssFeedEntity = rssFeedEntity;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", publishedTime=" + publishedTime +
                ", link='" + link + '\'' +
                ", rssFeedEntity=" + rssFeedEntity +
                '}';
    }
}