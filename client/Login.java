import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.MatteBorder;

// Login class which takes a user name and passed it to client class
public class Login implements ActionListener{
	JFrame frame1;
	JTextField userName;
	JButton button;
	JLabel heading;
	JLabel lblEnterYouLogin;
	private JLabel lblNewLabel;
	private JCheckBox EditAdvanceSettings;
	private JLabel lblHostIp;
	private JLabel lblHostSocketNumber;
	private JTextField hostIp;
	private JTextField hostSocket;
	public static void main(String[] args){
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		new Login();
	} 
	public Login(){
		frame1 = new JFrame("Login Page");
		frame1.setIconImage(Toolkit.getDefaultToolkit().getImage(Login.class.getResource("/images/foto_slide3.ico")));
		frame1.setResizable(false);
		userName=new JTextField();
		button=new JButton("Login");
		button.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		button.setForeground(new Color(153, 50, 204));
		heading=new JLabel("Multi Party Sharing App ");
		heading.setForeground(new Color(139, 0, 139));
		heading.setFont(new Font("Script MT Bold", Font.PLAIN, 42));
		lblEnterYouLogin=new JLabel("Enter you Login Name:");
		lblEnterYouLogin.setForeground(new Color(138, 43, 226));
		lblEnterYouLogin.setFont(new Font("Segoe Print", Font.PLAIN, 22));
		JPanel panel = new JPanel();
		panel.setBackground(new Color(216, 191, 216));
		button.addActionListener(this);
		panel.add(heading);panel.add(userName);panel.add(lblEnterYouLogin);
		panel.add(button);
		heading.setBounds(36,11,511,69);
		lblEnterYouLogin.setBounds(25,306,282,60);
		userName.setBounds(345,326,150,30);
		button.setBounds(251,471,90,30);
		frame1.getContentPane().add(panel);
		panel.setLayout(null);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Login.class.getResource("/images/rsz_lc21.jpg")));
		lblNewLabel.setBounds(113, 70, 294, 225);
		panel.add(lblNewLabel);
		
		EditAdvanceSettings = new JCheckBox("Edit Advance Settings");
		EditAdvanceSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox chkbox = (JCheckBox) e.getSource();
				if(chkbox.isSelected() == true){
					hostIp.setEnabled(true);
					hostSocket.setEnabled(true);
				}else{
					hostIp.setEnabled(false);
					hostSocket.setEnabled(false);
				}
			}
		});
		EditAdvanceSettings.setBounds(25, 362, 179, 23);
		EditAdvanceSettings.setSelected(false);
		panel.add(EditAdvanceSettings);
		
		lblHostIp = new JLabel("Host IP:");
		lblHostIp.setBounds(25, 397, 46, 14);
		panel.add(lblHostIp);
		
		lblHostSocketNumber = new JLabel("Host Socket Number:");
		lblHostSocketNumber.setBounds(25, 431, 125, 14);
		panel.add(lblHostSocketNumber);
		
		hostIp = new JTextField();
		hostIp.setEnabled(false);
		hostIp.setText("localhost");
		hostIp.setBounds(160, 389, 101, 30);
		panel.add(hostIp);
		hostIp.setColumns(10);
		
		hostSocket = new JTextField();
		hostSocket.setEnabled(false);
		hostSocket.setText("8080");
		hostSocket.setBounds(160, 423, 101, 30);
		panel.add(hostSocket);
		hostSocket.setColumns(10);
		frame1.setSize(560, 540);
	    frame1.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	// pass the user name to MyClient class
	public void actionPerformed(ActionEvent e){
		String userNAme = userName.getText();
		String hostIP = hostIp.getText();
		int hostSOcket = Integer.parseInt(hostSocket.getText());
		frame1.dispose();
		try{ 
	        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"); 
			ChatWindow frame = new ChatWindow(userNAme, hostIP, hostSOcket);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
}