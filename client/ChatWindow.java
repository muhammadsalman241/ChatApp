import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

/** A chatwindow class perform UI tasks for client.
 * */

public class ChatWindow implements ActionListener{
	JFrame frame;
	JPanel contentPane;
	JTextField msgToSend, userToRemove;
	JButton removeUserBtn, receiveFileBtn, sendMsgBtn, sendFileBtn, logOutBtn;
	JTextArea msgArea;
	DefaultListModel<String> userListModel;				/* a list where online user name will shown */
	MsgReceivingSendingThreadClass chatHandler;	/* a Thread class that will perform network tasks like sending and receivng */

	public ChatWindow(String uName, String Machine, int portNum ) {
		
		/* give chat handler this chatwindows's instance so it will intreact with it, and user name and ipNo/portNo */
		chatHandler = new MsgReceivingSendingThreadClass(this, uName, Machine, portNum); 
		
		/*----------------- GUI Components Starts -------------------- */
		frame = new JFrame(uName);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(ChatWindow.class.getResource("/images/foto_slide3.ico")));
		frame.setResizable(false);
		frame.setBackground(new Color(216, 191, 216));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setBounds(100, 100, 496, 557);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(216, 191, 216));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		contentPane.setLayout(null);
		
		userListModel = new DefaultListModel<String>();
		JList<String> list = new JList<String>(userListModel);
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Available Users", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane.setBounds(364, 11, 111, 278);
		contentPane.add(scrollPane);
		
		msgToSend = new JTextField();
		msgToSend.setBounds(92, 295, 251, 50);
		contentPane.add(msgToSend);
		msgToSend.setColumns(10);
		
		JLabel lblTypeA = new JLabel("Message:");
		lblTypeA.setHorizontalAlignment(SwingConstants.CENTER);
		lblTypeA.setBounds(10, 296, 72, 49);
		contentPane.add(lblTypeA);
		
		sendMsgBtn = new JButton("Send");
		sendMsgBtn.addActionListener(this);
		sendMsgBtn.setBounds(364, 295, 111, 50);
		contentPane.add(sendMsgBtn);
		
		sendFileBtn = new JButton("Send File");
		sendFileBtn.addActionListener(this);
		sendFileBtn.setBounds(20, 351, 215, 44);
		contentPane.add(sendFileBtn);
		
		JLabel lblRemove = new JLabel("Remove");
		lblRemove.setHorizontalTextPosition(SwingConstants.CENTER);
		lblRemove.setHorizontalAlignment(SwingConstants.CENTER);
		lblRemove.setBounds(20, 406, 53, 40);
		contentPane.add(lblRemove);
		
		userToRemove = new JTextField();
		userToRemove.setBounds(92, 401, 199, 45);
		contentPane.add(userToRemove);
		userToRemove.setColumns(10);
		
		removeUserBtn = new JButton("Remove User");
		removeUserBtn.addActionListener(this);
		removeUserBtn.setBounds(301, 401, 169, 45);
		removeUserBtn.setEnabled(false);
		contentPane.add(removeUserBtn);
		
		logOutBtn = new JButton("Log Out");
		logOutBtn.addActionListener(this);
		logOutBtn.setBounds(10, 457, 465, 50);
		contentPane.add(logOutBtn);
		
		receiveFileBtn = new JButton("Save Files To");
		receiveFileBtn.setToolTipText("edit Location where to save files.");
		receiveFileBtn.addActionListener(this);
		receiveFileBtn.setBounds(237, 351, 238, 44);
		contentPane.add(receiveFileBtn);
		
		msgArea = new JTextArea();
		msgArea.setTabSize(4);
		msgArea.setEditable(false);
		msgArea.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Messages", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		msgArea.setBounds(9, 10, 343, 277);
		
		JScrollPane msgScrollPane = new JScrollPane(msgArea);
		msgScrollPane.setAutoscrolls(true);
		msgScrollPane.setBounds(10, 11, 344, 278);
		contentPane.add(msgScrollPane);
		
		frame.setVisible(true);
		/*----------------- GUI Components Ends -------------------- */
		chatHandler.start();
	}
	
	/** put a new msg in client display
	 * */
	void putMsg(String str){
		this.msgArea.append(str);
	}
	
	/** put a new user in user list.
	 * */
	void insertUserInList(String user){
		this.userListModel.addElement(user);
	}
	
	/** remove offline user from list.
	 * */
	void removeUserFromList(String user){
		this.userListModel.removeElement(user);
	}
	
	/** perform action on button click
	 * */
	public void actionPerformed(ActionEvent e) {
		/** when user press send msg button.
		 * */
		if(e.getSource() == sendMsgBtn){
			String msg = msgToSend.getText();
			msgToSend.setText("");
			if(!msg.isEmpty())
				chatHandler.sendMsg(msg);
		}
		/** when user press send file button.
		 * */
		else if(e.getSource() == sendFileBtn){
			JFileChooser jfc = new JFileChooser();
			int returnValue = jfc.showOpenDialog(null);	//open a file choser

			if (returnValue == JFileChooser.APPROVE_OPTION) {	//if something is selected
				File selectedFile = jfc.getSelectedFile();
				jfc.setDialogTitle("Choose a File to send: ");
				chatHandler.sendFile(selectedFile);				//send selected file to network handler to send.
			}
		}
		/** if logout button pressed.
		 * */
		else if(e.getSource() == logOutBtn){
			chatHandler.logout();
		}
		/** if user remove button pressed
		 * */
		else if(e.getSource() == removeUserBtn){
			String toRemove = userToRemove.getText();
			userToRemove.setText("");
			if(userListModel.contains(toRemove))
				chatHandler.removeUser(toRemove);		//check if its a valid user name
			else
				JOptionPane.showMessageDialog(null, "Wrong Input", "Invalid User Name!", JOptionPane.ERROR_MESSAGE);
		}
		/** if save file directory change button pressed.
		 * */
		else if(e.getSource() == receiveFileBtn){
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogTitle("Choose a directory to save your file: ");
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);	//select a directory

			int returnValue = jfc.showSaveDialog(null);				//if file is selected?
			if (returnValue == JFileChooser.APPROVE_OPTION) {			
				if (jfc.getSelectedFile().isDirectory())
					chatHandler.fileReceivingLocation = jfc.getSelectedFile().getAbsolutePath() + "\\";	//send directory to send/receive handler. 
			}
		}
	}
}

/** a class used to intereact with server using socket.
 * */
class MsgReceivingSendingThreadClass extends Thread{
	
	ChatWindow currentClient;	//UI windows
	String UserName, Machine;
	int portNumber;
	Socket clientSocket;		
	DataInputStream din;
	DataOutputStream dout;
	String fileReceivingLocation;	//a directory to download file.

	public MsgReceivingSendingThreadClass(ChatWindow currentClient, String uName, String hostIP, int portNo){
		this.currentClient = currentClient;
		this.UserName = uName;
		this.fileReceivingLocation = System.getenv("userprofile") + "\\Desktop\\" + this.UserName + "_"; //initialize the default directory which is desktop.  
		/*----------------- Socket & Stream Components Starts -------------------*/
		try{
			clientSocket = new Socket(hostIP, portNo);
			din = new DataInputStream(clientSocket.getInputStream());
			dout = new DataOutputStream(clientSocket.getOutputStream());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		/*----------------- Socket & Stream Components Ends -------------------- */
	}
	
	/** a method continuously received msg from server and analyze it type and call according methods.
	 * */
	public void run(){
		String recivedText /* whole msg received from server*/, 
				from /* sender name */, 
				tempStr/* msg content */, 
				msgType = ""; /* msg type */
		StringTokenizer st;
		
		login();	//perform login operation.
		
		/** before starting receive previous chat from server.
		 * */
		do{
			try{
				recivedText = din.readUTF();
				st = new StringTokenizer(recivedText, "~");
				msgType = st.nextToken();
				if(msgType.equals("PREVIOUS_CHAT")){	//confirm msg type 
					currentClient.putMsg(st.nextToken());	//put it in UI.
					break;
				}
			}catch(Exception e){}
		}while(!msgType.equals("PREVIOUS_CHAT"));
		
		/** before starting receive previously avaialable userNames from server.
		 * */
		do{
			try{
				recivedText = din.readUTF();
				st = new StringTokenizer(recivedText, "~");
				msgType = st.nextToken();
				if(msgType.equals("USERS_LIST")){	//confirm its type
					while(st.hasMoreTokens())		//put every user in list.
						currentClient.insertUserInList(st.nextToken());
					
					if(currentClient.userListModel.firstElement().equals(this.UserName))
						currentClient.removeUserBtn.setEnabled(true);	//check if i m first user so enable removing button.
					else
						currentClient.removeUserBtn.setEnabled(false);
					break;
				}
			}catch(Exception e){}
		}while(!msgType.equals("USERS_LIST"));
		
		/** continue msg receiving cycle.
		 * */
		while(true){
			try{
				recivedText = din.readUTF(); 									// receive msg.
				st = new StringTokenizer(recivedText, "~");
				msgType = st.nextToken();										//break msg.
				
				//analyze its type and perform particular action.
				if(msgType.equals("MSG")){										//if its a simple text msg updat it in UI.
					from = st.nextToken();
					tempStr = st.nextToken();
					currentClient.putMsg("\n" + from + " : " + tempStr);
				}
				else if(msgType.equals("FILE")){								//if its a file
					from = st.nextToken();										//find sender name.
					tempStr = st.nextToken();									//find file name.
					int size = Integer.parseInt(st.nextToken());				//find file size.
					currentClient.putMsg("\n" + from + " has sent " + tempStr);	//update UI that we received a file.
					receiveFile(tempStr, size);									//download file
				}
				else if(msgType.equals("LOGIN")){								//if someone is loggen in.
					from = st.nextToken();										//find its name.
					currentClient.putMsg("\n" + from + " has logged IN!");		//update UI.
					currentClient.insertUserInList(from);						//insert its name in user lists.
				}
				else if(msgType.equals("LOGOUT")){								//if someone is logged out.
					from = st.nextToken();										//find its name.
					currentClient.putMsg("\n" + from + " has logged OUT!");		//updat UI.
					currentClient.removeUserFromList(from);						//remove its name from list.
					if(currentClient.userListModel.firstElement().equals(this.UserName))
						currentClient.removeUserBtn.setEnabled(true);			//if someone is removed so check if we became a first user now, so active remove button.
				}
				else if(msgType.equals("REMOVED")){								//if received a removal msg.
					from = st.nextToken();										//find its name.
					if(from.equals(this.UserName)){								//if its my name?
						currentClient.putMsg("\n" + "You are Removed!");		
						removeOwnSelf();										//remove own self
					}
					currentClient.putMsg("\n" + from + " has removed!");		//if its not for me, update UI.
					currentClient.removeUserFromList(from);						//remove form list its name.
					if(currentClient.userListModel.firstElement().equals(this.UserName))
						currentClient.removeUserBtn.setEnabled(true);			//if someone is removed so check if we became a first user now, so active remove button.
				}
			}catch(IOException e){}
		}
	}
	/** forward login msg to server.
	 * */
	void login(){
		try{ //pattern: LOGIN separator Login's name.
			dout.writeUTF("LOGIN" + "~" + UserName);
			dout.flush();
		}catch(IOException e){}
	}
	/** forward logout msg to server & close socket and data stream.
	 * */
	void logout(){
		try{ //pattern: LOGOUT separator LogOut's name.
			dout.writeUTF("LOGOUT" + "~" + UserName);
			dout.flush();
			dout.close();
			din.close();
			clientSocket.close();
			System.exit(0);
		}catch(IOException e){}
	}
	/** for text msg to server.
	 * */
	void sendMsg(String msg){
		try{ //pattern: MSG separator SENDER NAME separator  TEXT MSG.
			dout.writeUTF("MSG" + "~" + UserName + "~" + msg);
			dout.flush();
		}catch(IOException e){}
	}
	/** forward File to server.
	 * */
	void sendFile(File file){
		try{ //pattern: FILE separator SENDER NAME separator FILE NAME separator FILE SIZE. 
			//THEN File as byte per byte.
			System.out.println("start S");
			dout.writeUTF("FILE" + "~" + UserName + "~" + file.getName() + "~" + file.length());
			dout.flush();
			
			byte [] byteArray  = new byte [(int)file.length()];
			FileInputStream fileInputStream = new FileInputStream(file);
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			bufferedInputStream.read(byteArray,0,byteArray.length); // copied file into byteArray
			
			dout.write(byteArray,0,byteArray.length);			//copying byteArray to socket
			dout.flush();										//flushing socket
			System.out.println("done S");
			fileInputStream.close();
			bufferedInputStream.close();
			byteArray = null;
		}catch(IOException e){}
	}
	/** Receive file/download file from server.
	 * */
	void receiveFile(String fileName, int fileSize){
		byte [] byteArray  = new byte [fileSize];
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(fileReceivingLocation + fileName);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			int bytesRead = din.read(byteArray,0,byteArray.length);					//copying file from socket to byteArray
			int current = bytesRead;
			while(current < fileSize) {
				System.out.println(bytesRead);
				bytesRead = din.read(byteArray, current, (byteArray.length-current));
				if(bytesRead >= 0) current += bytesRead;
			} 
			bufferedOutputStream.write(byteArray, 0 , current);							//writing byteArray to file
			bufferedOutputStream.flush();												//flushing buffers
			bufferedOutputStream.close();
			fileOutputStream.close();
			byteArray = null;   
		}catch(IOException e){}
		currentClient.putMsg("\n*Saved As: " + fileReceivingLocation + fileName);		//tel user that where is file stored.
	}
	/** send any user removal msg to server
	 * */
	void removeUser(String userToRemove){
		try{ //pattern: REMOVE separator user name to remove.
			dout.writeUTF("REMOVE" + "~" + userToRemove);
			dout.flush();
		}catch(IOException e){}
	}
	/** if server send a removal msg for me.
	 * */
	void removeOwnSelf(){
		try{ 
			din.close();				//close stream & sockets
			dout.close();
			clientSocket.close();
			currentClient.userListModel.clear();	//clear user name.
		}catch(IOException e){}
		
		//ask user that if he/she want to close chat window or not! (now user can only read previos chat).
		int choice = JOptionPane.showConfirmDialog(null,
							"You Are Removed!\n Are you like to Close window? ", 
							"Opps Sorry " + UserName, 
							JOptionPane.WARNING_MESSAGE
							);
		if ( choice == JOptionPane.OK_OPTION ){
			System.exit(0);
		} 
	}
}