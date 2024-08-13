// Copyright (c) 2024 Brian Kircher
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package FLLScorer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The interface for accepting requests for dynamically generated pages.
 */
interface DynamicHandler
{
  byte[] run(String path, HashMap<String, String> paramMap);
}

/**
 * Handles the web server.
 * <p>
 * This is a singleton that is acquired via the getInstance() method.
 */
public class WebServer extends HttpServlet
{
  /**
   * The object for the WebServer singleton.
   */
  private static WebServer m_instance = null;

  /**
   * The Config object.
   */
  private Config m_config = null;

  /**
   * The Jetty web server object.
   */
  private Server m_server = null;

  /**
   * The Jetty servlet context handler object.
   */
  private ServletContextHandler m_handler = null;

  /**
   * The Jetty authenticator user store.
   */
  private UserStore m_userStore = null;

  /**
   * The list of file name extensions that have mime types.
   */
  private ArrayList<String> m_extensions;

  /**
   * The list of mime types for the file name extensions in
   * <i>m_extensions</i>, stored in the same order.
   */
  private ArrayList<String> m_mimeTypes;

  /**
   * The list of booleans that are <b>true</b> if files with the corresponding
   * extension should have Server Side Includes processed; stored in the same
   * order as <i>m_extensions</i>.
   */
  private ArrayList<Boolean> m_useSSI;

  /**
   * The list of names for the Server Side Includes.
   */
  private ArrayList<String> m_SSINames;

  /**
   * The list of values for the Server Side Includes, stored in the same order
   * as <i>m_SSINames</i>.
   */
  private ArrayList<String> m_SSIValues;

  /**
   * The list of paths that are dynamically generated.
   */
  private ArrayList<String> m_dynamicPaths;

  /**
   * The list of handlers for dynmically generated content, stored in the same
   * order as <i>m_dynamicPaths</i>.
   */
  private ArrayList<DynamicHandler> m_dynamicHandlers;

  /**
   * The list of source paths that are mapped (allowing internal resources to
   * be exposed into the server tree, and possibly renamed).
   */
  private ArrayList<String> m_pathMappingSrc;

  /**
   * The list of destination paths for the entries of <i>m_pathMappingSrc</i>,
   * store din the same order.
   */
  private ArrayList<String> m_pathMappingDest;

  /**
   * When set to <b>true</b>, every request is printed to the terminal for
   * debugging purposes.  This is configurable via the "httpDebug" value in
   * the configuration database table.
   */
  private static boolean m_debug = false;

  /**
   * When set to <b>true</b>, the security/login is bypassed (making it easier
   * to use during development).  This is configurable via the "securityBypass"
   * value in the configuration database table.
   */
  private static boolean m_securityBypass = false;

  /**
   * Gets the WebServer singleton object, creating it if necessary.
   *
   * @return Returns the WebServer singleton.
   */
  public static WebServer
  getInstance()
  {
    // Create the WebServer object if required.
    if(m_instance == null)
    {
      m_instance = new WebServer();
    }

    // Return the WebServer object.
    return(m_instance);
  }

  /**
   * The constructor.  This is private so that the object can only be created
   * via the getIntance() method.
   */
  private
  WebServer()
  {
  }

  /**
   * Associates a file name extension with a mime type.
   *
   * @param extension The file name extension.
   *
   * @param mimeType The mime type associated with this file name extension.
   *
   * @param useSSI <b>true</b> if files with this extension should have
   *               Server Side Inclusion processing performed.
   */
  public void
  registerMimeType(String extension, String mimeType, boolean useSSI)
  {
    // Add this mime type to the lists.
    m_extensions.add(extension);
    m_mimeTypes.add(mimeType);
    m_useSSI.add(useSSI);
  }

  /**
   * Associates a string with a corresponding value as a Server Side Include.
   *
   * @param name The name.
   *
   * @param value The value to be put in place of the name when performing
   *              Server Side Inclusions.
   */
  public void
  registerSSI(String name, String value)
  {
    // Loop through the existing SSIs.
    for(int i = 0; i < m_SSINames.size(); i++)
    {
      // See if these names match.
      if(name.equalsIgnoreCase(m_SSINames.get(i)))
      {
        // The names match, so update the value.
        m_SSIValues.set(i, value);

        // Done.
        return;
      }
    }

    // This is a new name, so add it and the value to the SSI lists.
    m_SSINames.add(name);
    m_SSIValues.add(value);
  }

  /**
   * Associated a handler for dynamic content with a web path.
   *
   * @param path The web path to server dynamically.
   *
   * @param handler The method that generates the dynamic content when it is
   *                requested.
   */
  public void
  registerDynamicFile(String path, DynamicHandler handler)
  {
    // Add the values to the dynamic handler lists.
    m_dynamicPaths.add("www" + path);
    m_dynamicHandlers.add(handler);
  }

  /**
   * Provides a path mapping to the web server.
   * <p>
   * This can be used for two things.  The first is to make specific content
   * outside the www directory accessible to a broswer (without making the
   * entire set of content in that path accessible), and the other is to map
   * content to two different URLs.
   *
   * @param src The web path to the content.
   *
   * @param dest The resource path to the content.
   */
  public void
  registerPathMapping(String src, String dest)
  {
    // Add the values to the path mapping lists.
    m_pathMappingSrc.add("www" + src);
    m_pathMappingDest.add(dest);
  }

  /**
   * Sets the mime type on the HTTP response.
   *
   * @param response The HTTP response that is being constructed.
   *
   * @param path The URL that was requested.
   *
   * @return <b>true</b> if Server Side Include processing should be performed
   *         on the content that is returned.
   */
  private boolean
  setMimeType(HttpServletResponse response, String path)
  {
    // Get the file name extension from the request.
    String extension = path.substring(path.lastIndexOf("."));

    // Set the default mime type, and do not perform SSI processing on files of
    // unknown provenance.
    String mimeType = "application/octet-stream";
    boolean useSSI = false;

    // Loop through the list of known extensions.
    for(int i = 0; i < m_extensions.size(); i++)
    {
      // See if this extension exists.
      if(extension.equals(m_extensions.get(i)))
      {
        // Update the mime type and use of SSI based on this extension.
        mimeType = m_mimeTypes.get(i);
        useSSI = m_useSSI.get(i);

        // No need to look through the rest of the list.
        break;
      }
    }

    // Set the mime type on the response.
    response.setContentType(mimeType);

    // Return the indicator of SSI processing.
    return(useSSI);
  }

  /**
   * Performs Server Side Inclusion processing on the given response.
   *
   * @param response The response text.
   *
   * @return The processed response text.
   */
  private byte[]
  processSSI(byte[] response)
  {
    String resp, name, value;
    int start, end;

    // Get the response into a String, making it easier to manipulate.
    resp = new String(response, StandardCharsets.UTF_8);

    // Loop while there are more SSI indicators in the response.
    while((start = resp.indexOf("<!--#")) != -1)
    {
      // Find the end marker for the SSI.
      end = resp.indexOf("-->", start);
      if(end == -1)
      {
        // The end marker could not be found, so stop SSI processing.
        break;
      }

      // Get the name for this SSI.
      name = resp.substring(start + 5, end);

      // The default value is to remove SSI tag from the response.
      value = "";

      // Loop through the known SSI substitutions.
      for(int idx = 0; idx < m_SSINames.size(); idx++)
      {
        // See if this substitution name matches.
        if(name.equalsIgnoreCase(m_SSINames.get(idx)))
        {
          // Update the value to the substitution value.
          value = m_SSIValues.get(idx);

          // The remainder of the list does not need to be searched.
          break;
        }
      }

      // Construct a new response, substituting the value for the SSI tag.
      resp = resp.substring(0, start) + value + resp.substring(end + 3);
    }

    // Convert the response back to a byte array and return it.
    return(resp.getBytes(StandardCharsets.UTF_8));
  }

  // Processes HTTP POST requests.
  @Override
  protected void
  doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    HashMap<String, String> paramMap = new HashMap<String, String>();
    byte[] resp = null;
    String path;

    // Get the path and parameters from the request.
    path = request.getRequestURI();

    // If in debug mode, print out the path and parameters.
    if(m_debug)
    {
      System.out.println("POST: " + path);
    }

    // Strip any leading "/" or "../" path elements from the request.
    while(true)
    {
      if((path.length() > 0) && (path.substring(0, 1).equals("/")))
      {
        path = path.substring(1);
        continue;
      }
      if((path.length() > 2) && (path.substring(0, 3).equals("../")))
      {
        path = path.substring(3);
        continue;
      }
      break;
    }

    // Prepend the path with "www/" (so that only those resources are served)
    // and append "/index.html" if there is not a file name extension in the
    // request.
    path = "www/" + path;
    if(path.lastIndexOf("/") > path.lastIndexOf("."))
    {
      path += "/index.html";
    }

    // Replace any double path separators with single path separators.
    path = path.replaceAll("//", "/");

    // If this path has a path mapping, change it to its mapped value.
    for(int i = 0; i < m_pathMappingSrc.size(); i++)
    {
      if(path.equals(m_pathMappingSrc.get(i)))
      {
        path = m_pathMappingDest.get(i);
      }
    }

    // Get the parameters from the request.
    Enumeration<String> keys = request.getParameterNames();
    while(keys.hasMoreElements())
    {
      // Get the next parameter key.
      String key = keys.nextElement();

      // Get this parameter's value and add it to the parameter map.
      if(!key.equals("authenticated_user"))
      {
        paramMap.put(key, request.getParameter(key));
      }
    }

    // Add the authenticated user, if one exists, to the parameter map.
    if(request.getRemoteUser() != null)
    {
      paramMap.put("authenticated_user", request.getRemoteUser());
    }

    // Loop through the dynamic paths.
    for(int i = 0; i < m_dynamicPaths.size(); i++)
    {
      // See if the requested path matches this dynamic path.
      if(path.equals(m_dynamicPaths.get(i)))
      {
        // Call the dynamic handler for this path.
        resp = m_dynamicHandlers.get(i).run(path.substring(3), paramMap);

        // The remainder of the list does not need to be checked.
        break;
      }
    }

    // If there is not a response, return an empty response.  A POST request
    // must be handled by a dynamic handler.
    if(resp == null)
    {
      resp = "".getBytes();
    }

    // Set the mime type for this request.
    if(setMimeType(response, path))
    {
      // Perform SSI processing on the response.
      resp = processSSI(resp);
    }

    // Set the content length and write the response.
    response.setContentLength(resp.length);
    response.getOutputStream().write(resp, 0, resp.length);
  }

  // Processes HTTP GET requests.
  @Override
  protected void
  doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    HashMap<String, String> paramMap = new HashMap<String, String>();
    String path, parameters;
    byte[] resp = null;

    // Get the path and parameters from the request.
    path = request.getRequestURI();
    parameters = request.getQueryString();

    // If in debug mode, print out the path and parameters.
    if(m_debug)
    {
      System.out.println("GET: " + path + ", " + parameters);
    }

    // Strip any leading "/" or "../" path elements from the request.
    while(true)
    {
      if((path.length() > 0) && (path.substring(0, 1).equals("/")))
      {
        path = path.substring(1);
        continue;
      }
      if((path.length() > 2) && (path.substring(0, 3).equals("../")))
      {
        path = path.substring(3);
        continue;
      }
      break;
    }

    // See if this is a logout request.
    if(path.equals("logout"))
    {
      // Log out the current user.
      if(!m_securityBypass)
      {
        request.logout();
      }

      // Redirect the browser to the root page.
      response.sendRedirect("/");

      // There is nothing further to be done.
      return;
    }

    // Prepend the path with "www/" (so that only those resources are served)
    // and append "/index.html" if there is not a file name extension in the
    // request.
    path = "www/" + path;
    if(path.lastIndexOf("/") > path.lastIndexOf("."))
    {
      path += "/index.html";
    }

    // Replace any double path separators with single path separators.
    path = path.replaceAll("//", "/");

    // If this path has a path mapping, change it to its mapped value.
    for(int i = 0; i < m_pathMappingSrc.size(); i++)
    {
      if(path.equals(m_pathMappingSrc.get(i)))
      {
        path = m_pathMappingDest.get(i);
      }
    }

    // See if there are parameters.
    if(parameters != null)
    {
      // Split the parameter string into its individual parameters.
      String[] params = parameters.split("&");

      // Loop through the parameters.
      for(int i = 0; i < params.length; i++)
      {
        // Split this parameter into its key/value.
        String[] items = params[i].split("=");

        // Add this key/value to the parameter map.
        if(!items[0].equals("authenticated_user"))
        {
          paramMap.put(items[0], items[1]);
        }
      }
    }

    // Add the authenticated user, if one exists, to the parameter map.
    if(request.getRemoteUser() != null)
    {
      paramMap.put("authenticated_user", request.getRemoteUser());
    }

    // Loop through the dynamic paths.
    for(int i = 0; i < m_dynamicPaths.size(); i++)
    {
      // See if the requested path matches this dynamic path.
      if(path.equals(m_dynamicPaths.get(i)))
      {
        // Call the dynamic handler for this path.
        resp = m_dynamicHandlers.get(i).run(path.substring(3), paramMap);

        // The remainder of the list does not need to be checked.
        break;
      }
    }

    // See if there is a response (in other words, there is not a dynamic
    // handler, or the dynamic handler chose to not return a response).
    if(resp == null)
    {
      // Get the class loader, so that resources can be read.
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      // Create an input stream for the given resource.
      InputStream in = classLoader.getResourceAsStream(path);
      if(in == null)
      {
        // The resource doesn't exist, so return the root HTML page instead.
        path = "www/index.html";
        in = classLoader.getResourceAsStream(path);
      }

      // Read the bytes from the resource.
      resp = in.readAllBytes();
    }

    // Set the mime type for this request.
    if(setMimeType(response, path))
    {
      // Perform SSI processing on the response.
      resp = processSSI(resp);
    }

    // Set the content length and write the response.
    response.setContentLength(resp.length);
    response.getOutputStream().write(resp, 0, resp.length);
  }

  /**
   * Finds the resource that contains the SSL key store.
   *
   * @param resourceFactory The resource factory to use.
   *
   * @return The resource that contains the SSL key store.
   */
  private static Resource
  findKeyStore(ResourceFactory resourceFactory)
  {
    // The name of the resource file that contains the key store.
    String resourceName = "ssl/keystore";

    // Get a resource for the key store.
    Resource resource = resourceFactory.newClassLoaderResource(resourceName);
    if (!Resources.isReadableFile(resource))
    {
      throw(new RuntimeException("Unable to read " + resourceName));
    }

    // Return the resource.
    return(resource);
  }

  /**
   * Adds a WebSocket handler to the web server.
   *
   * @param path The path where the WebSocket will be accessed.
   *
   * @param creator The creator for the WebSocket handler.
   *
   * @param idleTimeout The idle timeout for the WebSocket, in milliseconds.
   */
  public void
  addWebSocket(String path, JettyWebSocketCreator creator, long idleTimeout)
  {
    // Configure this WebSocket.
    JettyWebSocketServletContainerInitializer.configure(m_handler,
                                                        (context,
                                                         configurator) ->
      {
        // Set the idle timeout.
        configurator.setIdleTimeout(Duration.ofMillis(idleTimeout));

        // Set the mapping for this WebSocket.
        configurator.addMapping(path, creator);
      });
  }

  /**
   * Gets the value of a Server Side Include.
   * <p>
   * This is useful for leveraging the list of Server Side Includes (for
   * example, the locale-specific strings) when generating dynamic content.
   *
   * @param name The name of the Server Side Include.
   *
   * @return The value of the Server Side Include, or <b>null</b> if the name
   *         does not exist.
   */
  public String
  getSSI(String name)
  {
    // Loop through the SSI names.
    for(int i = 0; i < m_SSINames.size(); i++)
    {
      // See if this name matches.
      if(name.equalsIgnoreCase(m_SSINames.get(i)))
      {
        // Return the corresponding value.
        return(m_SSIValues.get(i));
      }
    }

    // The SSI name could not be found.
    return(null);
  }

  /**
   * Loads a fragment (a Server Side Include stored in its own file).
   *
   * @param name The name of the Server Side Include.
   *
   * @param file The name of the file containing the fragment to load.
   */
  private void
  loadFragment(String name, String file)
  {
    String fragment, line;
    boolean skip;

    // Use the class loader to open this resource file.
    InputStream in = Thread.currentThread().getContextClassLoader().
                       getResourceAsStream("fragments/" + file);
    if(in == null)
    {
      return;
    }

    // Create a buffered reader so that the file can be a line at a time.
    InputStreamReader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
    BufferedReader br = new BufferedReader(inr);

    // A try/catch to handle exceptions.
    fragment = "";
    skip = false;
    try
    {
      // Loop through all the lines in the file.
      while((line = br.readLine()) != null)
      {
        // Check for a start of comment line.
        if(line.equals("<!--"))
        {
          // Start skipping lines until the end of comment line is found.
          skip = true;

          // Go to the next line.
          continue;
        }

        // Check for an end of comment line.
        if(line.equals("-->"))
        {
          // Stop skipping lines until the next start of comment line is found.
          skip = false;

          // Go to the next line.
          continue;
        }

        // Go to the next line if lines are being skipped.
        if(skip)
        {
          continue;
        }

        // Add this line to the accumulated fragment.  Since readline() strips
        // the end of line characters, add a newline prior to all lines except
        // the first.
        if(fragment.length() == 0)
        {
          fragment += line;
        }
        else
        {
          fragment += "\n" + line;
        }
      }

      // Register the contents of this fragment as a Server Side Include.
      registerSSI(name, fragment);
    }
    catch (IOException e)
    {
      System.out.println("Fragment error: " + e);
    }
  }

  /**
   * Loads strings into Server Side Includes.
   *
   * @param locale The name of the locale for loading strings.
   */
  private void
  loadStrings(String locale)
  {
    String line;
    int pos;

    // Use the class loader to open this resource file.
    InputStream in = Thread.currentThread().getContextClassLoader().
                       getResourceAsStream("strings/" + locale + ".txt");
    if(in == null)
    {
      // Do nothing since the resource file could not be loaded.
      return;
    }

    // Create a buffered reader so that the file can be a line at a time.
    InputStreamReader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
    BufferedReader br = new BufferedReader(inr);

    // A try/catch to handle exceptions.
    try
    {
      // Loop through all the lines in the file.
      while((line = br.readLine()) != null)
      {
        // Ignore lines that are too short, or start with a C++ comment
        // sequence.
        if((line.length() < 3) || (line.substring(0, 2).equals("//")))
        {
          continue;
        }

        // Get the position of the colon that separates the name and value of
        // the string.
        pos = line.indexOf(":");
        if(pos != -1)
        {
          // Set the name and value as a Server Side Include.
          registerSSI(line.substring(0, pos), line.substring(pos + 1));
        }
      }
    }
    catch(IOException e)
    {
    }
  }

  /**
   * Adds a security constraint to the web server.
   *
   * @param securityHandler The {@link ConstraintSecurityHandler} to which to
   *                        add the constraint.
   *
   * @param constraint The {@link Constraint} to add.
   *
   * @param pathSpec The web path to which to apply the constraint.
   */
  private void
  addConstraint(ConstraintSecurityHandler securityHandler,
                Constraint constraint, String pathSpec)
  {
    // Create a new constraint mapping.
    ConstraintMapping constraintMapping = new ConstraintMapping();

    // Provide the contraint and path to the constraint mapping.
    constraintMapping.setConstraint(constraint);
    constraintMapping.setPathSpec(pathSpec);

    // Add the constraint mapping to the security handler.
    securityHandler.addConstraintMapping(constraintMapping);
  }

  /**
   * Adds a user to the user store.
   *
   * @param name The name of the user.
   *
   * @param password The user's password.
   *
   * @param admin <b>1</b> if the user has the <i>admin</i> role, and <b>0</b>
   *              otherwise.
   *
   * @param host <b>1</b> if the user has the <i>host</i> role, and <b>0</b>
   *             otherwise.
   *
   * @param judge <b>1</b> if the user has the <i>judge</i> role, and <b>0</b>
   *              otherwise.
   *
   * @param referee <b>1</b> if the user has the <i>referee</i> role, and
   *                <b>0</b> otherwise.
   *
   * @param timekeeper <b>1</b> if the user has the <i>timekeeper</i> role, and
   *                   <b>0</b> otherwise.
   */
  public void
  addUser(String name, String password, int admin, int host, int judge,
          int referee, int timekeeper)
  {
    // An array to hold the user's roles.
    ArrayList<String> roles = new ArrayList<String>();

    // Add the appropriate roles to the array.
    if(admin != 0)
    {
      roles.add("admin");
    }
    if(host != 0)
    {
      roles.add("host");
    }
    if(judge != 0)
    {
      roles.add("judge");
    }
    if(referee != 0)
    {
      roles.add("referee");
    }
    if(timekeeper != 0)
    {
      roles.add("timekeeper");
    }

    // Convert the ArrayList of rules into an array of roles.
    String[] roleArray = new String[roles.size()];
    roleArray = roles.toArray(roleArray);

    // Add this user to the user store.
    m_userStore.addUser(name, Credential.getCredential(password),
                        roleArray);
  }

  /**
   * Removes a user from the user store.
   *
   * @param name The name of the user.
   */
  public void
  removeUser(String name)
  {
    // Remove this user from the user store.
    m_userStore.removeUser(name);
  }

  /**
   * Performs initial setup for the web server.
   */
  public void
  setup()
  {
    // Get a reference to the configuration manager.
    m_config = Config.getInstance();

    // Get the HTTP debug and security bypass configuration from the database.
    m_debug = m_config.httpDebugGet();
    m_securityBypass = m_config.securityBypassGet();

    // Create the arrays used by the web server.
    m_extensions = new ArrayList<String>();
    m_mimeTypes = new ArrayList<String>();
    m_useSSI = new ArrayList<Boolean>();
    m_SSINames = new ArrayList<String>();
    m_SSIValues = new ArrayList<String>();
    m_dynamicPaths = new ArrayList<String>();
    m_dynamicHandlers = new ArrayList<DynamicHandler>();
    m_pathMappingSrc = new ArrayList<String>();
    m_pathMappingDest = new ArrayList<String>();

    // Register the MIME types for the various content that will be served.
    registerMimeType(".css", "text/css", true);
    registerMimeType(".html", "text/html", true);
    registerMimeType(".ico", "image/png", false);
    registerMimeType(".jpg", "image/jpeg", false);
    registerMimeType(".js", "text/javascript", true);
    registerMimeType(".json", "application/json", false);
    registerMimeType(".mp3", "audio/mpeg", false);
    registerMimeType(".png", "image/png", false);
    registerMimeType(".wav", "audio/wav", false);

    // Set the application version as a Server Side Include.
    String version = Main.class.getPackage().getImplementationVersion();
    if(version == null)
    {
      version = "development";
    }
    registerSSI("version", version);

    // Set the locale as a Server Side Include.
    registerSSI("locale", m_config.localeGet());

    // Set the match length as a Server Side Include.
    registerSSI("match_len", "150");

    // Load the HTML fragments into Server Side Inclueds.
    loadFragment("html_body_end", "body_end.html");
    loadFragment("html_body_start", "body_start.html");
    loadFragment("html_head", "head.html");
    loadFragment("html_popup_menu", "popup_menu.html");

    // Load the en_US strings, then overlay them with the strings for the
    // selected locale.  This means that new strings, which will likely
    // appear in en_US first, will at least be present.
    loadStrings("en_US");
    if(m_config.localeGet() != "en_US")
    {
      loadStrings(m_config.localeGet());
    }

    // The ports to use.
    int port = 8080;
    int ports = 8443;

    // Create a web server.
    m_server = new Server();

    // Configure the HttpConfiguration for the clear-text connector.
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSecurePort(ports);

    // Add the clear-text connector to the server.
    ServerConnector connector =
      new ServerConnector(m_server, new HttpConnectionFactory(httpConfig));
    connector.setPort(port);
    m_server.addConnector(connector);

    // Get a resource factory for finding the keystore.
    ResourceFactory resourceFactory = ResourceFactory.of(m_server);

    // Setup the SSL/TLS context.
    SslContextFactory.Server sslContextFactory =
      new SslContextFactory.Server();
    sslContextFactory.setKeyStoreResource(findKeyStore(resourceFactory));
    sslContextFactory.setKeyStorePassword("12345678");
    sslContextFactory.setKeyManagerPassword("12345678");

    // Configure the HttpConfiguration for the secured connection. The SNI host
    // check is disabled (which will cause a browser warning) since a canned,
    // self-signed certificate is used.
    HttpConfiguration httpsConf = new HttpConfiguration();
    httpsConf.setSecurePort(ports);
    httpsConf.setSecureScheme("https");
    SecureRequestCustomizer src = new SecureRequestCustomizer();
    src.setSniHostCheck(false);
    httpsConf.addCustomizer(src);

    // Add the secured connector to the server.
    ServerConnector httpsConnector =
      new ServerConnector(m_server,
                          new SslConnectionFactory(sslContextFactory,
                                                   "http/1.1"),
                          new HttpConnectionFactory(httpsConf));
    httpsConnector.setPort(8443);
    m_server.addConnector(httpsConnector);

    // Add the redirect from the clear-text to the secured connection.
    SecuredRedirectHandler securedHandler = new SecuredRedirectHandler();
    m_server.setHandler(securedHandler);

    // Create a context handler and associate it with the secure handler.
    m_handler = new ServletContextHandler("/", ServletContextHandler.SESSIONS |
                                               ServletContextHandler.SECURITY);
    securedHandler.setHandler(m_handler);

    // Add a servlet to the server for serving up the content.
    m_handler.addServlet(this, "/");

    // Create the security handler that contains the role restrictions.
    ConstraintSecurityHandler securityHandler =
      new ConstraintSecurityHandler();

    // Add a constraint for the administration page.
    addConstraint(securityHandler, Constraint.from("admin", "host"),
                  "/admin/*");

    // Add a constraint for the judge page.
    addConstraint(securityHandler, Constraint.from("admin", "host", "judge"),
                  "/judge/*");

    // Add a constraint for the referee page.
    addConstraint(securityHandler, Constraint.from("admin", "host", "referee"),
                  "/referee/*");

    // Add a constraint for the timekeeper page.
    addConstraint(securityHandler,
                  Constraint.from("admin", "host", "timekeeper"),
                  "/timekeeper/*");

    // Allow anyone to access everything else.
    addConstraint(securityHandler, Constraint.ALLOWED, "/*");

    // Create a login service.
    HashLoginService loginService = new HashLoginService();

    // Create a user credential storage.
    m_userStore = new UserStore();

    // Add the user store to the login service, and the login service to the
    // security handler
    loginService.setUserStore(m_userStore);
    securityHandler.setLoginService(loginService);

    // Create a form authenticator to present the actual login to the user.
    FormAuthenticator authenticator =
      new FormAuthenticator("/login/login.html", "/login/login.html?error=yes",
                            false);
    authenticator.setAlwaysSaveUri(true);
    securityHandler.setAuthenticator(authenticator);

    // Add the security handler to the servelet.
    if(!m_securityBypass)
    {
      m_handler.setSecurityHandler(securityHandler);
    }
  }

  /**
   * Starts the web server.
   */
  public void
  run()
  {
    // Start the server.
    try
    {
      m_server.start();
    }
    catch(Exception e)
    {
      System.out.println("Jetty error: " + e);
    }
  }
}