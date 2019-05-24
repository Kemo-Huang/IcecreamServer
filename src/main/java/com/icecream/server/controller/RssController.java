package com.icecream.server.controller;

import com.icecream.server.client.*;
import com.icecream.server.dao.ArticleRepository;
import com.icecream.server.dao.RssFeedRepository;
import com.icecream.server.entity.Article;
import com.icecream.server.entity.RssFeed;
import com.icecream.server.entity.User;
import com.icecream.server.service.rssfeed.ArticleService;
import com.icecream.server.service.rssfeed.RssFeedService;
import com.icecream.server.service.user.UserService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * This class is a rest controller for rss subscribe.
 *
 * @author NicoleMayer
 */
@RestController
public class RssController {

  private final transient UserService userService;

  private final transient RssFeedService rssFeedService;

  private final transient ArticleService articleService;

  private final transient ArticleRepository articleRepository;

  private final transient RssFeedRepository rssFeedRepository;

  private static final Logger logger = LoggerFactory.getLogger(RssController.class);

  private static final transient String WRONG_TOKEN = "wrong token";
  private static final transient String USER_NOT_FOUND = "user not found";
  private static final transient String SUCCEED = "succeed";

  /**
   * Constructor of RssController.
   *
   * @param userService       UserService class
   * @param rssFeedService    RssFeedService class
   * @param articleService    ArticleService class
   * @param articleRepository ArticleRepository class
   */
  public RssController(UserService userService, RssFeedService rssFeedService,
                       ArticleService articleService, ArticleRepository articleRepository,
                       RssFeedRepository rssFeedRepository) {
    this.userService = userService;
    this.rssFeedService = rssFeedService;
    this.articleService = articleService;
    this.articleRepository = articleRepository;
    this.rssFeedRepository = rssFeedRepository;
  }

  /**
   * Welcome page.
   *
   * @return Welcome message
   */
  @RequestMapping(value = {"/"}, method = RequestMethod.GET)
  public String hello() {
    return "welcome to icecream server";
  }

  /**
   * A user's subscribed channels.
   *
   * @param token verification for user
   * @return FeedsResponse
   */
  @RequestMapping(value = {"/list/feeds"}, method = RequestMethod.GET)
  public FeedsResponse getChannelListForCurrentUser(String token) {
    Long id = userService.verifyToken(token);
    if (id == null) {
      return new FeedsResponse(WRONG_TOKEN, 0, new HashSet<>());
    }
    User user = userService.findById(id).orElse(null);
    if (user == null) {
      return new FeedsResponse(USER_NOT_FOUND, 1, new HashSet<>());
    }
    return new FeedsResponse(SUCCEED, 2, user.getRssFeedEntities());
  }

  /**
   * Get all channels.
   *
   * @return a list of rss feed
   */
  @RequestMapping(value = {"/list/all/feeds"}, method = RequestMethod.GET)
  public List<RssFeed> getAllChannelList() {
    return rssFeedRepository.findAll();
  }

  /**
   * Articles for a subscribed channel.
   *
   * @param token verification for user
   * @return ArticlesResponse
   */
  @RequestMapping(value = {"/list/feed/{id}/articles"}, method = RequestMethod.GET)
  public ArticlesResponse getNewestArticlesFromOneFeed(@PathVariable("id") Long id, String token) {
    Long userId = userService.verifyToken(token);
    if (userId == null) {
      return new ArticlesResponse(WRONG_TOKEN, 0, new ArrayList<>());
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      return new ArticlesResponse(USER_NOT_FOUND, 1, new ArrayList<>());
    }
    RssFeed rssFeed = rssFeedService.findById(id).orElse(null);
    if (rssFeed == null) {
      return new ArticlesResponse("feed not find", 2, new ArrayList<>());
    }
    List<Article> articles = articleService.find30NewestArticlesFromOneFeed(rssFeed);

    return new ArticlesResponse(SUCCEED, 3, articles);
  }

  /**
   * Articles for all subscribed channels for one user.
   *
   * @param token verification for user
   * @return ArticlesResponse
   */
  @RequestMapping(value = {"/list/user/all/articles"}, method = RequestMethod.GET)
  public ArticlesResponse getNewestArticles(String token) {
    Long userId = userService.verifyToken(token);
    if (userId == null) {
      return new ArticlesResponse(WRONG_TOKEN, 0, new ArrayList<>());
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      return new ArticlesResponse(USER_NOT_FOUND, 1, new ArrayList<>());
    }
    List<Article> articles = articleService.find30NewestArticlesFromManyFeeds(
        user.getRssFeedEntities());

    return new ArticlesResponse(SUCCEED, 2, articles);

  }


  /**
   * Articles for all channels.
   *
   * @return ArticlesResponse
   */
  @RequestMapping(value = {"/list/feed/all/articles"}, method = RequestMethod.GET)
  public ArticlesResponse getNewestArticles() {
    List<Article> articles = articleService.find30NewestArticlesFromManyFeeds(
        rssFeedRepository.findAll());
    return new ArticlesResponse(SUCCEED, 2, articles);

  }

  /**
   * A article for a given article id.
   *
   * @param token verification for user
   * @return ArticleResponse
   */
  @RequestMapping(value = {"/list/article/{id}"}, method = RequestMethod.GET)
  public ArticleResponse getOneArticle(@PathVariable("id") Long id, String token) {
    Long userId = userService.verifyToken(token);
    if (userId == null) {
      return new ArticleResponse(WRONG_TOKEN, 0);
    }
    Article article = articleRepository.findById(id).isPresent()
        ? articleRepository.findById(id).get() : null;
    if (article == null) {
      return new ArticleResponse("article not find", 1);
    }
    ArticleResponse articleResponse = new ArticleResponse("find article successfully", 2);
    articleResponse.setTitle(article.getTitle());
    articleResponse.setChannelUrl(article.getRssFeedEntity().getUrl());
    articleResponse.setLink(article.getLink());
    articleResponse.setPublishedTime(article.getPublishedTime());
    articleResponse.setContent(article.getDescription());
    return articleResponse;
  }


  /**
   * Collected or liked articles for a user.
   *
   * @param token verification for user
   * @return ArticleResponse
   */
  @RequestMapping(value = {"/list/like/articles"}, method = RequestMethod.GET)
  public ArticlesResponse getCollectedArticles(String token) {
    Long userId = userService.verifyToken(token);
    if (userId == null) {
      return new ArticlesResponse(WRONG_TOKEN, 0, new ArrayList<>());
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      return new ArticlesResponse(USER_NOT_FOUND, 1, new ArrayList<>());
    }
    List<Article> articles = new ArrayList<>(user.getCollectedArticles());
    return new ArticlesResponse(SUCCEED, 2, articles);
  }

  /**
   * Subscribe a new channel.
   *
   * @param token verification for user
   * @param url   RSS feed url
   * @return NormalResponse
   */
  @RequestMapping(value = {"/addChannel"}, method = RequestMethod.GET)
  public NormalResponse subscribeChannel(String token, String url) {
    Long id = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("add a new channel");
    if (id == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
      return normalResponse;
    }
    User user = userService.findById(id).orElse(null);
    if (user == null) {
      normalResponse.setMsgCode(1);
      normalResponse.setMessage(USER_NOT_FOUND);
      return normalResponse;
    }

    if (!rssFeedService.addChannel(url, user)) {
      normalResponse.setMsgCode(2);
      normalResponse.setMessage("add failed");
    } else {
      normalResponse.setMsgCode(3);
      normalResponse.setMessage("add succeed");
    }
    return normalResponse;
  }

  /**
   * Unsubscribe a channel.
   *
   * @param token verification for user
   * @param url   RSS feed url
   * @return NormalResponse
   */
  @RequestMapping(value = {"/deleteChannel"}, method = RequestMethod.GET)
  public NormalResponse deleteChannel(String token, String url) {
    Long userId = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("delete a channel");
    if (userId == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
      return normalResponse;
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      normalResponse.setMsgCode(1);
      normalResponse.setMessage(USER_NOT_FOUND);
      return normalResponse;
    }
    if (!rssFeedService.deleteChannel(url, user)) {
      normalResponse.setMsgCode(2);
      normalResponse.setMessage("delete failed");
    } else {
      normalResponse.setMsgCode(3);
      normalResponse.setMessage("delete succeed");
    }
    return normalResponse;
  }

  /**
   * Like a new article.
   *
   * @param token verification for user
   * @return NormalResponse
   */
  @RequestMapping(value = {"/like/article/{id}"}, method = RequestMethod.GET)
  public NormalResponse likeArticle(@PathVariable("id") Long id, String token) {
    Long userId = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("collect a article");
    if (userId == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
      return normalResponse;
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      normalResponse.setMsgCode(1);
      normalResponse.setMessage(USER_NOT_FOUND);
      return normalResponse;
    }

    if (!articleService.likeArticle(user, id)) {
      normalResponse.setMsgCode(2);
      normalResponse.setMessage("like failed");
    } else {
      normalResponse.setMsgCode(3);
      normalResponse.setMessage("like succeed");
    }
    return normalResponse;
  }

  /**
   * Unlike a new article.
   *
   * @param token verification for user
   * @return NormalResponse
   */
  @RequestMapping(value = {"/unlike/article/{id}"}, method = RequestMethod.GET)
  public NormalResponse unlikeArticle(@PathVariable("id") Long id, String token) {
    Long userId = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("uncollect a article");
    if (userId == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
      return normalResponse;
    }
    User user = userService.findById(userId).orElse(null);
    if (user == null) {
      normalResponse.setMsgCode(1);
      normalResponse.setMessage(USER_NOT_FOUND);
      return normalResponse;
    }

    if (!articleService.unlikeArticle(user, id)) {
      normalResponse.setMsgCode(2);
      normalResponse.setMessage("unlike failed");
    } else {
      normalResponse.setMsgCode(3);
      normalResponse.setMessage("unlike succeed");
    }
    return normalResponse;
  }


  /**
   * Get article's record.
   *
   * @return RecordResponse
   */
  @RequestMapping(value = {"/freshRecords"}, method = RequestMethod.GET)
  public RecordResponse freshRecords() {
    RecordResponse recordResponse = new RecordResponse(SUCCEED, 0);
    List<Article> articles = articleRepository.findAll();
    for (Article article : articles) {
      if (article.getRecord() == null) {
        String path = "/media/icecream_record/" + article.getId() + "/";
        article.setRecord(path);
        File file = new File(path);
        if (!file.exists()) {
          recordResponse.addArticleAndRecord(article.getDescription(), article.getRecord());
          articleRepository.save(article);
          // do not need to create file here.
        }
      }
    }
    return recordResponse;
  }

  /**
   * Return mp3 record.
   *
   * @param id       article id
   * @throws Exception exception
   */
  @RequestMapping(value = {"/list/record_mp3/{id}"}, method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Resource> getOneRecordMp3(@PathVariable("id") Long id) throws Exception {
    String path = "/media/icecream_record/" + id + "/record.mp3";
    File file = new File(path);
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

    return ResponseEntity.ok()
        .contentLength(file.length())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(resource);
//    FileInputStream in = new FileInputStream(new File(path));
//    ServletOutputStream out = response.getOutputStream();
//    response.setContentType("audio/mpeg3");
//    byte[] b = null;
//    while (in.available() > 0) {
//      if (in.available() > 10240) {
//        b = new byte[10240];
//      } else {
//        b = new byte[in.available()];
//      }
//      out.write(b, 0, in.read(b, 0, b.length));
//    }
//    in.close();
//    out.flush();
//    out.close();
  }

  /**
   * Return record info.
   *
   * @param id article id
   * @throws Exception exception
   */
  @RequestMapping(value = {"/list/record_info/{id}"}, method = RequestMethod.GET)
  @ResponseBody
  public String getOneRecordInfo(@PathVariable("id") Long id) throws Exception {
    String path = "/media/icecream_record/" + id + "/record.json";
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }

  /**
   * Checks the token.
   *
   * @param token user token.
   * @return Normal response.
   */
  @RequestMapping(value = {"/checkToken"}, method = RequestMethod.GET)
  public NormalResponse checkToken(String token) {
    Long userId = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("check token");
    if (userId == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
    } else if (!userService.findById(userId).isPresent()) {
      normalResponse.setMsgCode(1);
      normalResponse.setMessage(USER_NOT_FOUND);
    } else {
      normalResponse.setMsgCode(2);
      normalResponse.setMessage("valid");
    }
    return normalResponse;
  }

  /**
   * Gets the user settings.
   *
   * @param token token.
   * @return normal response.
   */
  @RequestMapping(value = {"/userSetting"}, method = RequestMethod.GET)
  public NormalResponse getUserSetting(String token) {
    Long userId = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("get user settings");
    if (userId == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage(WRONG_TOKEN);
    } else {
      Optional<User> optional = userService.findById(userId);
      if (!optional.isPresent()) {
        normalResponse.setMsgCode(1);
        normalResponse.setMessage(USER_NOT_FOUND);
      } else {
        normalResponse.setMsgCode(2);
        JSONObject jsonObject = new JSONObject();
        User user = optional.get();
        try {
          jsonObject.put("username", user.getUsername());
          // other settings
        } catch (JSONException e) {
          e.printStackTrace();
        }
        normalResponse.setMessage(jsonObject.toString());
      }
    }
    return normalResponse;
  }

  /**
   * Fresh all subscribed channels.
   *
   * @param token token.
   * @return normal response.
   */
  @RequestMapping(value = {"/freshChannels"}, method = RequestMethod.GET)
  public NormalResponse freshChannels(String token) {
    Long user_id = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("fresh channels");
    if (user_id == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage("wrong token");
      return normalResponse;
    }
    User user = userService.findById(user_id).orElse(null);
    Set<RssFeed> rssFeedSet = user.getRssFeedEntities();
    for (RssFeed rssFeed : rssFeedSet) {
      rssFeedService.addArticles(rssFeed);
    }
    normalResponse.setMsgCode(1);
    normalResponse.setMessage("update succeed");
    return normalResponse;
  }

  /**
   * Fresh only one channel.
   *
   * @param token token.
   * @return normal response.
   */
  @RequestMapping(value = {"/freshChannel"}, method = RequestMethod.POST)
  public NormalResponse freshOneChannel(String token, RssFeed rssFeed) {
    Long user_id = userService.verifyToken(token);
    NormalResponse normalResponse = new NormalResponse("fresh channels");
    if (user_id == null) {
      normalResponse.setMsgCode(0);
      normalResponse.setMessage("wrong token");
      return normalResponse;
    }
    rssFeedService.addArticles(rssFeed);
    normalResponse.setMsgCode(1);
    normalResponse.setMessage("update succeed");
    return normalResponse;
  }


}
