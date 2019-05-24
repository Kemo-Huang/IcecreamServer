package com.icecream.server.service.rssfeed;

import com.icecream.server.dao.ArticleRepository;
import com.icecream.server.dao.RssFeedRepository;
import com.icecream.server.dao.UserRepository;
import com.icecream.server.entity.Article;
import com.icecream.server.entity.RssFeed;
import com.icecream.server.entity.User;
import com.icecream.server.exception.RssException;
import com.icecream.server.rss.ObjectFactory;
import com.icecream.server.rss.TRss;
import com.icecream.server.rss.TRssChannel;
import com.icecream.server.rss.TRssItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the Service implementation for {@link RssFeed}.
 *
 * @author NicoleMayer
 */
@Service
public class RssFeedServiceImpl implements RssFeedService {

  @Autowired
  private transient RssFeedRepository rssFeedRepository;

  @Autowired
  private transient UserRepository userRepository;

  @Autowired
  private transient ArticleRepository articleRepository;

  private static final Logger logger = LoggerFactory.getLogger(RssFeedServiceImpl.class);

  /**
   * User add a new channel.
   *
   * @param url  the channel address
   * @param user wants to add channel
   * @return succeed or not
   */
  @Override
  public boolean addChannel(String url, User user) {
    RssFeed existRssFeed = rssFeedRepository.findByUrl(url);
    if (existRssFeed == null) {
      return false;
    }

    for (RssFeed rssFeed : user.getRssFeedEntities()) {
      if (rssFeed.getId().equals(existRssFeed.getId())) {
        return false;
      }
    }
    user.getRssFeedEntities().add(existRssFeed);
    userRepository.save(user);
    return true;
  }

  /**
   * Add new articles for a given rss feed.
   *
   * @param rssFeedEntity the channel object
   */
  @Override
  public void addArticles(RssFeed rssFeedEntity) {
    try {
      List<Article> articles = crawlArticles(rssFeedEntity.getUrl());
      articles.forEach(entry -> {
        Article savedArticle =
                articleRepository.findByRssFeedEntityAndLink(
                        rssFeedEntity, entry.getLink()); //no repeat articles
        if (savedArticle == null) {
          entry.setRssFeedEntity(rssFeedEntity);
          articleRepository.save(entry);
        }
      });
    } catch (RssException e) {
      logger.info("Could not save the channel");
    }
  }

//  /**
//   * Add new articles for all rss feeds in a fixed time.
//   */
//  @Scheduled(cron = "0 0 0,2,4,6,8,10,12,14,16,18,20,22 * * ?")
//  private void reloadChannels() {
//    rssFeedRepository.findAll().stream().forEach(this::addArticles);
//    logger.info("finish fixed time task!");
//  }

  /**
   * Delete a channel.
   *
   * @param url  the channel address
   * @param user wants to add channel
   * @return succeed or not
   */
  @Override
  public boolean deleteChannel(String url, User user) {
    RssFeed existRssFeed = rssFeedRepository.findByUrl(url);
    if (existRssFeed == null) {
      return false;
    }
    for (RssFeed rssFeed : user.getRssFeedEntities()) {
      if (rssFeed.getId().equals(existRssFeed.getId())) {
        user.getRssFeedEntities().remove(rssFeed);
        userRepository.save(user);
        return true;
      }
    }
    return false;
  }

  /**
   * Crawl articles for a given source.
   *
   * @param source the channel address
   * @return a list of articles
   */
  private List<Article> crawlArticles(Source source) throws RssException {
    List<Article> list = new ArrayList<>();
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      JAXBElement<TRss> jaxbElement = unmarshaller.unmarshal(source, TRss.class);
      TRss rss = jaxbElement.getValue();

      List<TRssChannel> channel = rss.getChannel();
      for (TRssChannel chanel : channel) {
        List<TRssItem> items = chanel.getItem();
        for (TRssItem rssItem : items) {
          Article article = new Article();
          article.setTitle(rssItem.getTitle());
          article.setDescription(parseHtml(rssItem.getDescription()));
          article.setLink(rssItem.getLink());
          Date pubDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
                  .parse(rssItem.getPubDate());
          article.setPublishedTime(pubDate);
          list.add(article);
        }
      }

    } catch (JAXBException | ParseException e) {
      throw new RssException(e);
    }
    return list;
  }

  /**
   * Crawl articles for a given url.
   *
   * @param url the channel address
   * @return a list of articles
   */
  @Override
  public List<Article> crawlArticles(String url) throws RssException {
    return this.crawlArticles(new StreamSource(url));
  }

  /**
   * Find a rss feed by id.
   *
   * @param id rss feed identification
   * @return rss feed object
   */
  @Override
  public Optional<RssFeed> findById(long id) {
    return rssFeedRepository.findById(id);
  }

  /**
   * Remove html element in the content
   * @param htmlStr HTML content
   * @return non-html content
   */
  private String parseHtml(String htmlStr){
    String regEx_script="<script[^>]*?>[\\s\\S]*?<\\/script>";
    String regEx_style="<style[^>]*?>[\\s\\S]*?<\\/style>";
    String regEx_html="<[^>]+>";

    Pattern p_script=Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
    Matcher m_script=p_script.matcher(htmlStr);
    htmlStr=m_script.replaceAll("");

    Pattern p_style=Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
    Matcher m_style=p_style.matcher(htmlStr);
    htmlStr=m_style.replaceAll("");

    Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
    Matcher m_html=p_html.matcher(htmlStr);
    htmlStr=m_html.replaceAll("");

    return htmlStr.trim();
  }
}

