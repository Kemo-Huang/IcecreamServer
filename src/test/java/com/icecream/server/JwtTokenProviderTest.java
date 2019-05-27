package com.icecream.server;

import com.icecream.server.entity.User;
import com.icecream.server.service.user.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/** 
 * JwtTokenProvider Tester.
 *
 * @author nicolemayer
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IceCreamServerApplication.class)
public class JwtTokenProviderTest {

  @Autowired
  private transient UserService userService;

  @Autowired
  private transient JwtTokenProvider jwtTokenProvider;



  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws Exception {
  }


  /**
   * Test to generate a token.
   * @throws Exception
   */
  @Test
  public void testGenerateToken() throws Exception {
    User user = userService.findById(1).isPresent()?userService.findById(1).get():null;
    String token = null;
    try{
      token = jwtTokenProvider.generateToken(user);
    }
    catch (Exception e){
      fail("generate token shouldn't fail");
    }

    if (!jwtTokenProvider.validateToken(token)){
      fail("generate token shouldn't fail");
    }
  }

  /**
   * Test to get user id from token.
   * @throws Exception
   */
  @Test
  public void testGetUserIdFromJwt() throws Exception {
    User user = userService.findById(1).isPresent()?userService.findById(1).get():null;
    String token = jwtTokenProvider.generateToken(user);
    assertEquals("wrong parse id", (long)1, (long)jwtTokenProvider.getUserIdFromJwt(token));
    user = userService.findById(2).isPresent()?userService.findById(2).get():null;
    token = jwtTokenProvider.generateToken(user);
    assertEquals("wrong parse id", (long)2, (long)jwtTokenProvider.getUserIdFromJwt(token));
  }


  /**
   * Test to validate the token.
   * @throws Exception
   */
  @Test
  public void testValidateToken() throws Exception {
    User user = userService.findById(1).isPresent()?userService.findById(1).get():null;
    String token = jwtTokenProvider.generateToken(user);
    assertEquals("token is invalid", true, jwtTokenProvider.validateToken(
            token));
    assertEquals("token is invalid", false, jwtTokenProvider.validateToken(
            "4354657687ghyjynjnj"));

  }


} 
