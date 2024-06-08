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
import java.util.ArrayList;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
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
  byte[] run(String path, String parameters);
}

/**
 * Handles the web server.
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
   * debugging purposes.
   */
  private static boolean m_debug = false;

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

  // Processes web requests.
  @Override
  protected void
  doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    String path, parameters;
    byte[] resp = null;

    // Get the path and parameters from the request.
    path = request.getRequestURI();
    parameters = request.getQueryString();

    // If in debug mode, print out the path and parameters.
    if(m_debug)
    {
      System.out.println(path + ", " + parameters);
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

    // Loop through the dynamic paths.
    for(int i = 0; i < m_dynamicPaths.size(); i++)
    {
      // See if the requested path matches this dynamic path.
      if(path.equals(m_dynamicPaths.get(i)))
      {
        // Call the dynamic handler for this path.
        resp = m_dynamicHandlers.get(i).run(path.substring(3), parameters);

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

  /*
  @WebSocket
  public class TimeSocket implements Runnable
  {
    private TimeZone m_timezone;
    private Session m_session;

    @OnWebSocketOpen
    public void onOpen(Session session)
    {
      m_session = session;
      m_timezone = TimeZone.getTimeZone("UTC");
      new Thread(this).start();
    }

    @OnWebSocketClose
    public void onClose(int closeCode, String closeReasonPhrase)
    {
      m_session = null;
    }

    @Override
    public void run()
    {
      while (m_session != null)
      {
        try
        {
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
          dateFormat.setTimeZone(m_timezone);

          String timestamp = dateFormat.format(new Date());
          m_session.sendText(timestamp, Callback.NOOP);
          TimeUnit.SECONDS.sleep(1);
        }
        catch(InterruptedException e)
        {
          System.out.println("Send of TEXT message interrupted: " + e);
        }
      }
    }
  }
  */

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
   * Starts the web server.
   *
   * @param port The port on which to serve content; should be 80 unless
   *             special circumstances exist.
   *
   * @param ports The secure port on which to serve content; should be 443
   *              unless special circumstances exist.
   */
  private void
  run(int port, int ports)
  {
    // Create a web server.
    Server server = new Server();

    // Configure the HttpConfiguration for the clear-text connector.
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSecurePort(ports);

    // Add the clear-text connector to the server.
    ServerConnector connector =
      new ServerConnector(server, new HttpConnectionFactory(httpConfig));
    connector.setPort(port);
    server.addConnector(connector);

    // Get a resource factory for finding the keystore.
    ResourceFactory resourceFactory = ResourceFactory.of(server);

    // Setup the SSL/TLS context.
    SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
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
    ServerConnector httpsConnector = new ServerConnector(server,
        new SslConnectionFactory(sslContextFactory, "http/1.1"),
        new HttpConnectionFactory(httpsConf));
    httpsConnector.setPort(8443);
    server.addConnector(httpsConnector);

    // Add the redirect from the clear-text to the secured connection.
    SecuredRedirectHandler securedHandler = new SecuredRedirectHandler();
    server.setHandler(securedHandler);

    // Create a context handler and associate it with the secure handler.
    ServletContextHandler handler = new ServletContextHandler();
    securedHandler.setHandler(handler);

    // Add a servlet to the server for serving up the content.
    handler.addServlet(this, "/");

    // Start the server.
    try
    {
      server.start();
    }
    catch(Exception e)
    {
      System.out.println("Jetty error: " + e);
    }
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
   * Performs initial setup for the web server.
   */
  public void
  setup()
  {
    // Get a reference to the configuration manager.
    m_config = Config.getInstance();

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

    // Load the en_US strings, then overlay them with the strings for the
    // selected locale.  This means that new strings, which will likely
    // appear in en_US first, will at least be present.
    loadStrings("en_US");
    if(m_config.localeGet() != "en_US")
    {
      loadStrings(m_config.localeGet());
    }

    // Start the web server.
    run(8080, 8443);
  }
}