import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Main server class that will accept new clients and assign every client to a thread that will serve them.
 * */
class Server{
    static final int HTTP_SERVER_PORT = 80; //default http server port
    static final int CHAT_SERVER_PORT = 8080; //default chat server port
	static myVector clientSokets = new myVector(), clientNames = new myVector();	//every clients name and socket stores here.
	static myVector ClientHandlers = new myVector();								//every clients Threadhandler store here.
	static String msgbuffer = new String();											//Server keep a copy of activity of client chat.
	HttpServer httpServer;
    ServerSocket chatServerSocket;		
	Socket client;
	int portNum;
	JFrame frame;
	private JPanel contentPane;
	
	public Server(){
		/*--------------------GUI Components------------------------------*/
		frame = new JFrame("Server");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.setBounds(100, 100, 421, 89);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(216, 191, 216));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		
		JButton closwSeverBtn = new JButton("Close Server");
		closwSeverBtn.setBounds(10, 11, 385, 23);
		closwSeverBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeIt();
			}
		});
		contentPane.setLayout(null);
		contentPane.add(closwSeverBtn);
		frame.setVisible(true);
		/*-------------------GUI Components--------------------------------*/
		
		/** confirm socket port no before starting server
		 * */
		try{
			this.portNum = confirmSocketNo();
			chatServerSocket = new ServerSocket(this.portNum);
			httpServer = new HttpServer();
            httpServer.setPriority(Thread.MIN_PRIORITY);
            httpServer.start();
            while(true){
				client = chatServerSocket.accept();							//accept new client
				ClientHandler newClient = new ClientHandler(client);	//assign them new Thread
				ClientHandlers.addElement(newClient);					//add this thread to vector so we can later access it.				
				newClient.start();										//start the thread
			}
		}catch(Exception e){
            
        }
	}
	public static void main(String []a) throws IOException{
		new Server();
	}
	/** For closing a Server.
	 * */
	public void closeIt() {
		for(int i=0; i< ClientHandlers.size(); i++)
			((ClientHandler)  ClientHandlers.elementAt(i)).logOut();	//logout/close client sockets
		try {
			this.chatServerSocket.close();									//close server socket
		} catch (IOException e) {}
		frame.dispose();												//close system
		System.exit(0);
	}
	/** ask that if user want to start server at differnt port.
	 * */
	int confirmSocketNo(){
		boolean ans = (JOptionPane.showConfirmDialog(null, 
                                                    "Default Socket No : " + CHAT_SERVER_PORT + "\n Do you want to change?", 
                                                    "Confirm Socket Number", JOptionPane.YES_NO_OPTION)
                                                    == JOptionPane.YES_OPTION);
		if(ans){
			String No = JOptionPane.showInputDialog(null, "Socket Port No:", "Confirm Socket", JOptionPane.QUESTION_MESSAGE);
			return Integer.parseInt(No);
		}
		return CHAT_SERVER_PORT;
	}
}

/** A Threading class will handle a client every need, i.e receiving sending etc.
 * */
class ClientHandler extends Thread{
	Socket clientSocket;
	String clientName;
    DataInputStream din;
    DataOutputStream dout;
    boolean clientAvailable;
    
    ClientHandler(Socket clientSocket)throws IOException{
    	String temp;
    	this.clientSocket = clientSocket;
    	this.din = new DataInputStream(clientSocket.getInputStream());
    	this.dout = new DataOutputStream(clientSocket.getOutputStream());
    	this.clientAvailable = true;
        temp = din.readUTF();
    	StringTokenizer st = new StringTokenizer(temp, "~");
		temp = st.nextToken();
		//wait for client login msg 
		if(temp.equals("LOGIN")){
			this.clientName = st.nextToken();	//determine client name
			Server.clientNames.addElement(this.clientName);
			Server.clientSokets.addElement(this.clientSocket);
			Server.msgbuffer+=("\n" + this.clientName + " has logged IN!");
			loginUser(this.clientName);
		}
        else{
            System.out.println(temp);
            while(true);
        }
    	dout.writeUTF("PREVIOUS_CHAT" + "~" + Server.msgbuffer);	//send new user previous chat
    	dout.flush();
    	dout.writeUTF("USERS_LIST" + "~" + Server.clientNames.toString());	//send new user available user list.
    	dout.flush();
    	
    }
    /** for stoping thread's loop
     * */
    void finish(){
    	this.clientAvailable = false;
    }
    
    /** analyze user msg and do according action (for understanding review first client side code)
     * */
    public void run(){
    	String recivedText, msgType, from, tempStr;
    	StringTokenizer st;
    	while(this.clientAvailable){
    		try{
    			recivedText = din.readUTF();
    			st = new StringTokenizer(recivedText, "~");
				msgType = st.nextToken();
				if(msgType.equals("MSG")){
					from = st.nextToken();
					tempStr = st.nextToken();
					Server.msgbuffer += ("\n" + from + " : " + tempStr);
					forwardMsgToAll(tempStr, from);
				}
				else if(msgType.equals("FILE")){
					from = st.nextToken();
					tempStr = st.nextToken();
					int fileSize = Integer.parseInt(st.nextToken());
					Server.msgbuffer+=("\n" + from + " has sent " + tempStr);
					forwardFileToAll( from, tempStr, receiveFile(fileSize) );
				}
				else if(msgType.equals("LOGOUT")){
					from = st.nextToken();
					Server.msgbuffer += ("\n" + from + " has logged OUT!");
					logOut();
				}
				else if(msgType.equals("REMOVE")){
					from = st.nextToken();
					removeUser(from);
				}
    		}catch(IOException e){}
    	}
    }
    /** Forward a file as byte array to all clients sokets
     * **/
    static void forwardFileToAll(String senderName, String fileName, byte[] fileInBytes){
    	Iterator i = Server.ClientHandlers.iterator();
		ClientHandler c;
		while(i.hasNext()){
			try{
				c = (ClientHandler) i.next(); 
				c.dout.writeUTF("FILE" + "~" + senderName + "~" + fileName + "~" + fileInBytes.length);	//first tell client about file is coming
				c.dout.flush();
				c.dout.write(fileInBytes, 0, fileInBytes.length);										//then send file
				c.dout.flush();
			}catch(IOException e){}
		}
		fileInBytes = null;
	}
    /** Forward text msg to all available clients.
     * */
	static void forwardMsgToAll(String msg, String sender){
		Iterator i = Server.ClientHandlers.iterator();
		ClientHandler c;
		while(i.hasNext()){
			try{
				c = (ClientHandler) i.next(); 
				c.dout.writeUTF("MSG" + "~" + sender + "~" + msg);					//write msg as our pattern.
				c.dout.flush();
			}catch(IOException e){}
		}
	}
	/** forward a some user's login msg to all client
	 * */
	static void loginUser(String user){
		Iterator i = Server.ClientHandlers.iterator();
    	ClientHandler c;
		while(i.hasNext()){
			try{
				c = (ClientHandler) i.next(); 
				c.dout.writeUTF("LOGIN" + "~" + user);				
				c.dout.flush();
			}catch(IOException e){}
		}
	}
	/** forward this user's logout msg to every user and close sockets, streams and user's thread.
	 * */
	void logOut(){
		Iterator i = Server.ClientHandlers.iterator();
    	ClientHandler c;
		while(i.hasNext()){
			try{
				c = (ClientHandler) i.next(); 
				c.dout.writeUTF("LOGOUT" + "~" + this.clientName);
				dout.flush();
			}catch(IOException e){}
		}
		try{
			this.din.close();
			this.dout.close();
			this.clientSocket.close();
		}catch(IOException e){}
		Server.clientSokets.remove(this.clientSocket);
		Server.clientNames.remove(this.clientName);
		Server.ClientHandlers.remove(this);
		this.finish();
	}
	/** forward some users removal msg to all clients and close his/her socket, thread and stream.
	 * */
	static void removeUser(String userToRemove){
		Iterator i = Server.ClientHandlers.iterator();
    	ClientHandler c, cToRemove = null;
		while(i.hasNext()){
			try{
				c = (ClientHandler) i.next();  
				c.dout.writeUTF("REMOVED" + "~" + userToRemove);
				c.dout.flush();
				if(c.clientName.equals(userToRemove))
					cToRemove = c;
			}catch(IOException e){}
		}
		try{
			cToRemove.din.close();
			cToRemove.dout.close();
			cToRemove.clientSocket.close();
		}catch(IOException e){}
		Server.ClientHandlers.remove(cToRemove);
		Server.clientSokets.remove(cToRemove.clientSocket);
		Server.clientNames.remove(cToRemove.clientName);
		cToRemove.finish();
	}
	/** receive a file as byte array from client's stream and return as byte array so we can easily forward it.
	 * */
	byte[] receiveFile(int fileSize){
		byte [] byteArray  = new byte[fileSize];
		int bytesRead;
		try {
			bytesRead = din.read(byteArray,0,byteArray.length);
			int current = bytesRead;
			while(current < fileSize) {
				bytesRead = din.read(byteArray, current, (byteArray.length-current));
				if(bytesRead >= 0) current += bytesRead;
			} 
		} catch (IOException e) {}
		return byteArray;
	}
}

class HttpClientHandler extends Thread{
    static final File WEB_ROOT = new File("."); //HttpServer root is the current directory
    static final String DEFAULT_FILE = "index.htm";
    Socket connect;

    //constructor
    public HttpClientHandler(Socket connect){
        this.connect = connect;
    }
    
    /**
    * run method services each request in a separate thread.
    */
    public void run(){
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = "";

        try{
            in = new BufferedReader(new InputStreamReader(connect.getInputStream())); //get character output stream to client (for headers)
            out = new PrintWriter(connect.getOutputStream());                           //get binary output stream to client (for requested data)
            dataOut = new BufferedOutputStream(connect.getOutputStream());              //get first line of request from client
            String input = in.readLine();

            StringTokenizer parse = new StringTokenizer(input); //create StringTokenizer to parse request
            String method = parse.nextToken().toUpperCase(); //parse out method
            fileRequested = parse.nextToken().toLowerCase(); //parse out method

            if (!method.equals("GET")){
                //send Not Implemented message to client
                out.println("HTTP/1.0 501 Not Implemented");
                out.println("Server: Aim Chat Server Support 1.0");
                out.println("Date: " + new Date());
                out.println("Content-Type: text/html");
                out.println(); 
                out.println("<HTML>");
                out.println("<HEAD><TITLE>Not Implemented</TITLE>" + "</HEAD>");
                out.println("<BODY>");
                out.println("<H2>501 Not Implemented: " + method + " method.</H2>");
                out.println("</BODY></HTML>");
                out.flush();
                return;
            }

            if (fileRequested.endsWith("/")){
                fileRequested += DEFAULT_FILE;  //append default file name to request
            }

            File file = new File(getClass().getClassLoader().getResource(fileRequested).getFile());  //create file object
            int fileLength = (int)file.length();    //get length of file

            //get the file's MIME content type
            String content = getContentType(fileRequested);

            //if request is a GET, send the file content
            if (method.equals("GET")){
                FileInputStream fileIn = null;
                byte[] fileData = new byte[fileLength]; //create byte array to store file data

                try{
                    fileIn = new FileInputStream(file); //open input stream from file
                    fileIn.read(fileData);              //read file into byte array
                }
                finally{
                    close(fileIn);                      //close file input stream
                }

                //send HTTP headers
                out.println("HTTP/1.0 200 OK");
                out.println("Server: Aim Chat Server Support 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + content);
                out.println("Content-length: " + file.length());
                out.println();                          //blank line between headers and content
                out.flush();                            //flush character output stream buffer

                dataOut.write(fileData,0,fileLength);    //write file
                dataOut.flush();                         //flush binary output stream buffer
            }
        }
        catch (FileNotFoundException fnfe){
            fileNotFound(out, fileRequested);            //inform client file doesn't exist
        }
        catch (Exception e){
            System.err.println("Server Error: " + e);
        }
        finally{
            close(in); //close character input stream
            close(out); //close character output stream
            close(dataOut); //close binary output stream
            close(connect); //close socket connection
        }
    }


    /**
    * fileNotFound informs client that requested file does not
    * exist.
    *
    * @param out Client output stream
    * @param file File requested by client
    */
    private void fileNotFound(PrintWriter out, String file){
        //send file not found HTTP headers
        out.println("HTTP/1.0 404 File Not Found");
        out.println("Server: Aim Chat Server Support 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: text/html");
        out.println();
        out.println("<HTML>");
        out.println("<HEAD><TITLE>File Not Found</TITLE>" + "</HEAD>");
        out.println("<BODY>");
        out.println("<H2>404 File Not Found: " + file + "</H2>");
        out.println("</BODY>");
        out.println("</HTML>");
        out.flush();
    }


    /**
    * getContentType returns the proper MIME content type
    * according to the requested file's extension.
    *
    * @param fileRequested File requested by client
    */
    private String getContentType(String fileRequested){
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else if (fileRequested.endsWith(".gif"))
            return "image/gif";
        else if (fileRequested.endsWith(".jpg") || fileRequested.endsWith(".jpeg"))
            return "image/jpeg";
        else if (fileRequested.endsWith(".png"))
            return "image/png";
        else if (fileRequested.endsWith(".class") || fileRequested.endsWith(".jar"))
            return "applicaton/octet-stream";
        else if (fileRequested.endsWith(".css"))
            return "text/css";
        else
            return "text/plain";
    }


    /**
    * close method closes the given stream.
    *
    * @param stream
    */
    public void close(Object stream){
        if (stream == null)
            return;

        try{
            if (stream instanceof Reader){
                ((Reader)stream).close();
            }
            else if (stream instanceof Writer){
                ((Writer)stream).close();
            }
            else if (stream instanceof InputStream){
                ((InputStream)stream).close();
            }
            else if (stream instanceof OutputStream){
                ((OutputStream)stream).close();
            }
            else if (stream instanceof Socket){
                ((Socket)stream).close();
            }
            else{
                System.err.println("Unable to close object: " + stream);
            }
        }
        catch (Exception e){
            System.err.println("Error closing stream: " + e);
        }
    }
}

class HttpServer extends Thread{
    ServerSocket httpServer;
    HttpServer(){
        try{
            httpServer = new ServerSocket(Server.HTTP_SERVER_PORT);
        }
        catch(Exception e){
            System.err.println("HTTP Server Exception: " + e);
        }
    }
    public void run(){
        try{
            while(true){
                HttpClientHandler HttpClient = new HttpClientHandler(httpServer.accept()); //instantiate HttpServer
                HttpClient.start(); //start thread
            }
        }
        catch (Exception e){
            System.err.println("Server error: " + e);
        }
    }
}

/** we overided vector class toString method as our need.
 * */
class myVector extends Vector{
	private static final long serialVersionUID = 1L;

	public String toString(){
		String str = "";
		for(int i=0; i<this.size(); i++)
			str += elementAt(i) + "~";
		return str;
	}
}