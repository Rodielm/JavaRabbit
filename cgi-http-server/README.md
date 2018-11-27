CgiHTTPServer
================

This is a lightweight HTTP server that was created for teaching 
purposes. It can handle static and dynamic (CGI-like) responses.

You can clone the project with the following shell command:

git clone https://github.com/jgutie2r/CgiHTTPServer.git

or you can do it from Eclipse if you have a git plugin installed.

This is an HTTP server that can handle static or dynamic (CGI-like)
requests.

The first thing is to determine which handler (static or dynamic) 
should be created to create the response. In this implementation
it is used a file that has the mapping between the URL and 
the process that should be executed. If the URL requested
is in this mapping then a dynamic CGI-like handler is created,
otherwise, a static handler is created.

The directory where resources are located, the maximum number of
concurrent requests and the port number are specified in the
config.ini file. Also you can configure the prefix of resources
that require user/password and the name of the file in that
path that stores that user/password information.

This is the config.ini file included in the project:

PATH=/var/web/resources/
CGI_MAPPINGS_FILE=/var/web/config/cgi_mappings.txt
NUM_THREADS=500
SERVER_PORT=8080
AUTH_PREFIX=private
CREDENTIALS=.creds

This means that:

1) The static resources are located in the path /var/web/resources  <br />
   If the URI is http://server:8080/path/resource  <br />
   then there should exists /var/web/resources/path/resource  <br />
   in order to have a 200 OK response with the resource
   
2) The cgi mappings are located in that file. 
   That file contains lines with this format:
   
   url1;process1 argName1 ... argNameN 
   
   url2;process2 argName1 ... argNameM
   
   So that if the URL requested matches any of the URLs 
   in this file then the specified process will be executed
   to generate the response. 
   
   The response can be a full HTML page, XML, JSON or a 
   part of a page if the request is made from JavaScript (Ajax).
   
3) The server can deal with 500 concurrent requests.

4) The server listen in port 8080

5) If the path starts with private then the server
   sends to the client a 401 message asking for 
   user and password.  <br />
   Sample URL that triggers this behaviour:  <br />
   http://server:8080/privateSite1/index.html
   
6) This is the file name that stores the user
   and password. This should be a text file
   that has a single line with the format:  <br />
   user;password
   
   So in the previous URL example it is assumed
   that there exists the following file:  <br />
   /var/web/resources/privateSite1/.creds  <br />
   
   Each path can have its own .creds file storing
   different user/password.
      
---------------------------------------------

This is part of a teaching subject in Server Side Web Applications 
Programming.

This code is the improvement of the static server located at
https://github.com/jgutie2r/StaticHTTPServer

----------------------------------------------

Juan Gutierrez Aguado (PhD) <br />
Departamento de Informatica - Computer Science Department <br />
Universidad de Valencia     - Valencia University <br />
Spain



