package com.icecream.server.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.util.Set;
import javax.persistence.*;


/**
 * This class is a data class for article.
 *
 * @author NicoleMayer
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
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date publishedTime;

  private String record;

  @ManyToOne
  private RssFeed rssFeedEntity;


  @JsonIgnore
  @ManyToMany(mappedBy = "collectedArticles", fetch = FetchType.EAGER)
  private Set<User> userEntities;

  public String getRecord() {
    return record;
  }

  public void setRecord(String record) {
    this.record = record;
  }

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

  public Date getPublishedTime() {
    return publishedTime;
  }

  public void setPublishedTime(Date publishedTime) {
    this.publishedTime = publishedTime;
  }

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

  public Set<User> getUserEntities() {
    return userEntities;
  }

  public void setUserEntities(Set<User> userEntities) {
    this.userEntities = userEntities;
  }

  /**
   * String representation of the article.
   *
   * @return String stands for the article.
   */
  @Override
  public String toString() {
    return "Article{"
            + "id=" + id
            + ", title='" + title + '\''
            + ", link='" + link + '\''
            + ", description='" + description + '\''
            + ", publishedTime=" + publishedTime
            + ", rssFeedUrl=" + rssFeedEntity.getUrl()
            + '}';
  }
}
