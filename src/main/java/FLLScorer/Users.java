// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.SimpleJSONObject;
import org.bspfsystems.simplejson.parser.JSONParser;

/**
 * Handles the users tab.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class Users
{
  /**
   * The object for the Users singleton.
   */
  private static Users m_instance = null;

  /**
   * The Webserver object.
   */
  private WebServer m_webserver = null;

  /**
   * The database object.
   */
  private Database m_database = null;

  /**
   * Gets the Users singleton object, creating it if necessary.
   *
   * @return Returns the Users singleton.
   */
  public static Users
  getInstance()
  {
    // Create the Users object if required.
    if(m_instance == null)
    {
      m_instance = new Users();
    }

    // Return the Users object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  Users()
  {
  }

  /**
   * Encrypts a password provided by the user.
   *
   * @param user The name of the user.
   *
   * @param password The password.
   *
   * @return The encrypted password.
   */
  private String
  passwordEncrypt(String user, String password)
  {
    // Encrypt the password with the Unix crypt algorithm.
    return(MD5SHA.hash(user, password));
  }

  /**
   * Updates a user in the web server's user store.
   *
   * @param id The ID of the user to update.
   */
  private void
  userUpdate(int id)
  {
    // Get the details for this user.
    String name = m_database.userNameGet(id);
    String password = m_database.userPasswordGet(id);
    int admin = m_database.userRoleGet(id, "admin");
    int host = m_database.userRoleGet(id, "host");
    int judge = m_database.userRoleGet(id, "judge");
    int referee = m_database.userRoleGet(id, "referee");
    int timekeeper = m_database.userRoleGet(id, "timekeeper");

    // Add/update this user in the web server's user store.
    m_webserver.addUser(name, password, admin, host, judge, referee,
                        timekeeper);
  }

  /**
   * Adds a user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param name The name of the user.
   *
   * @param password The password for the user.
   */
  private void
  add(JSONObject result, String name, String password)
  {
    // Encrypt the password.
    password = passwordEncrypt(name, password);

    // Add the user and get the resulting ID.
    int id = m_database.userAdd(name, password, 0, 0, 0, 0, 0);

    // Return a failure if the user add failed.
    if(id == -1)
    {
      result.set("result", m_webserver.getSSI("str_users_already_exists"));
      return;
    }

    // Add this user to the web server.
    userUpdate(id);

    // Success.
    result.set("result", "ok");
    result.set("id", id);
  }

  /**
   * Adds a role to a user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the user.
   *
   * @param role The name of the role to add.
   */
  private void
  addRole(JSONObject result, int id, String role)
  {
    // The admin user can not have roles added.
    if("admin".equals(m_database.userNameGet(id)))
    {
      result.set("result", m_webserver.getSSI("str_users_role_add_error"));
      return;
    }

    // Return an error if an invalid role was provided.
    if(!role.equals("admin") && !role.equals("host") &&
       !role.equals("judge") && !role.equals("referee") &&
       !role.equals("timekeeper"))
    {
      result.set("result", m_webserver.getSSI("str_users_invalid_role"));
      return;
    }

    // Add this role to the user, returning an error if it fails.
    if(m_database.userRoleAdd(id, role) != true)
    {
      result.set("result", m_webserver.getSSI("str_users_role_add_fail"));
      return;
    }

    // Update this user in the web server.
    userUpdate(id);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Change the user name for the user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the user.
   *
   * @param name The new name for the user.
   */
  private void
  changeName(JSONObject result, int id, String name)
  {
    // The admin user and can not be renamed.
    if("admin".equals(m_database.userNameGet(id)))
    {
      result.set("result", m_webserver.getSSI("str_users_name_change_error"));
      return;
    }

    // Set the name for the user.
    if(m_database.userNameSet(id, name) == false)
    {
      result.set("result", m_webserver.getSSI("str_users_name_change_fail"));
      return;
    }

    // Update this user in the web server.
    userUpdate(id);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Change the password for the user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the user.
   *
   * @param password The new password for the user.
   */
  private void
  changePassword(JSONObject result, int id, String password)
  {
    // Encrypt the password.
    password = passwordEncrypt(m_database.userNameGet(id), password);

    // Set the new password.
    if(!m_database.userPasswordSet(id, password))
    {
      // Return an error since the new password could not be set.
      result.set("result", m_webserver.getSSI("str_users_password_fail"));
      return;
    }

    // Update this user in the web server.
    userUpdate(id);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Deletes a user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the user.
   */
  private void
  delete(JSONObject result, int id)
  {
    // Get the name of this user.
    String name = m_database.userNameGet(id);

    // The admin user can not be deleted.
    if("admin".equals(name))
    {
      result.set("result", m_webserver.getSSI("str_users_remove_error"));
      return;
    }

    // Remove the user.
    if(m_database.userRemove(id) == false)
    {
      result.set("result", m_webserver.getSSI("str_users_remove_fail"));
      return;
    }

    // Remove this user from the web server.
    m_webserver.removeUser(name);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Lists the users.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   */
  private void
  list(JSONObject result)
  {
    // Arrays to hold information about the users.
    ArrayList<Integer> id = new ArrayList<Integer>();
    ArrayList<String> name = new ArrayList<String>();
    ArrayList<Integer> admin = new ArrayList<Integer>();
    ArrayList<Integer> host = new ArrayList<Integer>();
    ArrayList<Integer> judge = new ArrayList<Integer>();
    ArrayList<Integer> referee = new ArrayList<Integer>();
    ArrayList<Integer> timekeeper = new ArrayList<Integer>();

    // Enumerate the users.
    if(m_database.userEnumerate(id, name, null, admin, host, judge,
                                referee, timekeeper) == false)
    {
      result.set("result", m_webserver.getSSI("str_users_list_fail"));
      return;
    }

    // Create a JSON array for returning the users.
    JSONArray users = new SimpleJSONArray();

    // Loop through the users.
    for(int idx = 0; idx < id.size(); idx++)
    {
      // Create a JSON object for this user.
      JSONObject user = new SimpleJSONObject();
      user.set("id", id.get(idx));
      user.set("name", name.get(idx));
      user.set("admin", admin.get(idx));
      user.set("host", host.get(idx));
      user.set("judge", judge.get(idx));
      user.set("referee", referee.get(idx));
      user.set("timekeeper", timekeeper.get(idx));

      // Add the JSON object for this user to the JSON array.
      users.addEntry(user);
    }

    // Add the JSON array to the response.
    result.set("users", users);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Removes a role from a user.
   *
   * @param result The JSON object that is used to communicate the result back
   *               to the client.
   *
   * @param id The ID of the user.
   *
   * @param role The name of the role to remove.
   */
  private void
  removeRole(JSONObject result, int id, String role)
  {
    // The admin user can not have roles removed.
    if("admin".equals(m_database.userNameGet(id)))
    {
      result.set("result", m_webserver.getSSI("str_users_role_remove_error"));
      return;
    }

    // Return an error if an invalid role was provided.
    if(!role.equals("admin") && !role.equals("host") &&
       !role.equals("judge") && !role.equals("referee") &&
       !role.equals("timekeeper"))
    {
      result.set("result", m_webserver.getSSI("str_users_invalid_role"));
      return;
    }

    // Remove this role from the user, returning an error if it fails.
    if(m_database.userRoleRemove(id, role) != true)
    {
      result.set("result", m_webserver.getSSI("str_users_role_remove_fail"));
      return;
    }

    // Update this user in the web server.
    userUpdate(id);

    // Success.
    result.set("result", "ok");
  }

  /**
   * Handles requests for /admin/users/users.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  serveUsers(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "add", for adding a user.
      if(paramMap.get("action").equals("add") &&
         paramMap.containsKey("name") && paramMap.containsKey("password"))
      {
        // Add the user.
        add(result,
            URLDecoder.decode(paramMap.get("name"), StandardCharsets.UTF_8),
            URLDecoder.decode(paramMap.get("password"),
                              StandardCharsets.UTF_8));
      }

      // See if the action is "addRole", for adding a role to a user.
      else if(paramMap.get("action").equals("addRole") &&
              paramMap.containsKey("id") && paramMap.containsKey("role"))
      {
        // Add the role to the user.
        addRole(result, Integer.parseInt(paramMap.get("id")),
                paramMap.get("role"));
      }

      // See if the action is "changeName", for changing the name of a user.
      else if(paramMap.get("action").equals("changeName") &&
              paramMap.containsKey("id") && paramMap.containsKey("name"))
      {
        // Change the name of this user.
        changeName(result, Integer.parseInt(paramMap.get("id")),
                   URLDecoder.decode(paramMap.get("name"),
                                     StandardCharsets.UTF_8));
      }

      // See if the action is "changePassword", for changing the password of a
      // user.
      else if(paramMap.get("action").equals("changePassword") &&
              paramMap.containsKey("id") && paramMap.containsKey("password"))
      {
        // Change the password of this user.
        changePassword(result, Integer.parseInt(paramMap.get("id")),
                       URLDecoder.decode(paramMap.get("password"),
                                         StandardCharsets.UTF_8));
      }

      // See if the action is "delete", for deleting a user.
      else if(paramMap.get("action").equals("delete") &&
              paramMap.containsKey("id"))
      {
        // Delete this user.
        delete(result, Integer.parseInt(paramMap.get("id")));
      }

      // See if the action is "removeRole", for removing a role from a user.
      else if(paramMap.get("action").equals("removeRole") &&
              paramMap.containsKey("id") && paramMap.containsKey("role"))
      {
        // Remove the role from the user.
        removeRole(result, Integer.parseInt(paramMap.get("id")),
                   paramMap.get("role"));
      }

      // See if the action is "list", for listing the users.
      else if(paramMap.get("action").equals("list"))
      {
        // List the users.
        list(result);
      }

      // Otherwise, return an error.
      else
      {
        result.set("result", "error");
      }
    }

    // Otherwise, return a list of the users.
    else
    {
      // List the users.
      list(result);
    }

    // Convert the response into a byte array and return it.
    try
    {
      String json = JSONParser.format(JSONParser.serialize(result));
      return(json.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Handles requests for /password.json.
   *
   * @param path The path from the request.
   *
   * @param paramMap The parameters from the request.
   *
   * @return An array of bytes to return to the client.
   */
  private byte[]
  servePassword(String path, HashMap<String, String> paramMap)
  {
    JSONObject result = new SimpleJSONObject();

    // See if there is an action request.
    if(paramMap.containsKey("action"))
    {
      // See if the action is "change", for changing a user's password.
      if(paramMap.get("action").equals("change") &&
         paramMap.containsKey("authenticated_user") &&
         paramMap.containsKey("old") && paramMap.containsKey("new") &&
         paramMap.containsKey("verify"))
      {
        // Get the parameters.
        String user = paramMap.get("authenticated_user");
        String old = URLDecoder.decode(paramMap.get("old"),
                                       StandardCharsets.UTF_8);
        String new_pw = URLDecoder.decode(paramMap.get("new"),
                                          StandardCharsets.UTF_8);
        String verify = URLDecoder.decode(paramMap.get("verify"),
                                          StandardCharsets.UTF_8);

        // Get the user's ID.
        int id = m_database.userIDGet(user);

        // See if the current password is correct.
        if(m_database.userPasswordValidate(id, MD5SHA.hash(user, old)) ==
           false)
        {
          result.set("result", m_webserver.getSSI("str_password_incorrect"));
        }

        // See if the new and verify passwords match.
        else if(!new_pw.equals(verify))
        {
          result.set("result", m_webserver.getSSI("str_password_mismatch"));
        }

        // Update the user's password.
        else
        {
          changePassword(result, id, new_pw);
        }
      }

      // An unknown request was provided.
      else
      {
        result.set("result", "error");
      }
    }

    // No request was provided.
    else
    {
      result.set("result", "error");
    }

    // Convert the response into a byte array and return it.
    try
    {
      String json = JSONParser.format(JSONParser.serialize(result));
      return(json.getBytes(StandardCharsets.UTF_8));
    }
    catch(Exception e)
    {
      return("{}".getBytes(StandardCharsets.UTF_8));
    }
  }

  /**
   * Performs initial setup for the users handler.
   */
  public void
  setup()
  {
    // Get references to the web server, database, season, and the event
    // objects.
    m_webserver = WebServer.getInstance();
    m_database = Database.getInstance();

    // Arrays to hold information about the users.
    ArrayList<String> name = new ArrayList<String>();
    ArrayList<String> password = new ArrayList<String>();
    ArrayList<Integer> admin = new ArrayList<Integer>();
    ArrayList<Integer> host = new ArrayList<Integer>();
    ArrayList<Integer> judge = new ArrayList<Integer>();
    ArrayList<Integer> referee = new ArrayList<Integer>();
    ArrayList<Integer> timekeeper = new ArrayList<Integer>();

    // Enumerate the users.
    if(m_database.userEnumerate(null, name, password, admin, host, judge,
                                referee, timekeeper) == true)
    {
      // Loop through all the users.
      for(int idx = 0; idx < name.size(); idx++)
      {
        // Add this uesr to the webserver for authentication.
        m_webserver.addUser(name.get(idx), password.get(idx), admin.get(idx),
                            host.get(idx), judge.get(idx), referee.get(idx),
                            timekeeper.get(idx));
      }
    }

    // Register the dynamic handler for the users.json file.
    m_webserver.registerDynamicFile("/admin/users/users.json",
                                    this::serveUsers);

    // Register the dynamic handler for the password.json file.
    m_webserver.registerDynamicFile("/password.json", this::servePassword);
  }
}