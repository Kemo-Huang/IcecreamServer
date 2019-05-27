package com.icecream.server.client;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a data class for record response.
 *
 * @author NicoleMayer
 */
public class RecordResponse {
  private String message;
  private int msgCode;
  private transient List<ArticleAndRecord> articleAndRecords;

  /**
   * This class is a inner class for record response.
   *
   * @author NicoleMayer
   */
  static class ArticleAndRecord {
    String content;
    String recordPath;

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getRecordPath() {
      return recordPath;
    }

    public void setRecordPath(String recordPath) {
      this.recordPath = recordPath;
    }

    /**
     * This is a constructor for ArticleAndRecord class.
     *
     * @param content     article's content
     * @param recordPath path to store the record info
     */
    public ArticleAndRecord(String content, String recordPath) {
      this.content = content;
      this.recordPath = recordPath;
    }
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getMsgCode() {
    return msgCode;
  }

  public void setMsgCode(int msgCode) {
    this.msgCode = msgCode;
  }

  public List<ArticleAndRecord> getArticleAndRecord() {
    return articleAndRecords;
  }

  public void setArticleAndRecords(List<ArticleAndRecord> articleAndRecords) {
    this.articleAndRecords = articleAndRecords;
  }

  /**
   * Add new articleAndRecords to the list variable.
   *
   * @param content     article's content
   * @param recordPath path to store the record info
   */
  public void addArticleAndRecord(String content, String recordPath) {
    this.articleAndRecords.add(new ArticleAndRecord(content, recordPath));
  }

  /**
   * This is a constructor for RecordResponse class.
   *
   * @param message response info
   * @param msgCode response number
   */
  public RecordResponse(String message, int msgCode) {
    this.message = message;
    this.msgCode = msgCode;
    this.articleAndRecords = new ArrayList<>();
  }
}
