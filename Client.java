import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.*;

class Client {
	
	static boolean finished = false;
	
	public static class ChatBox extends JFrame implements Runnable {
	    
	    JPanel[] row = new JPanel[2];
	    JButton sendButton = null;
	    
	    Dimension chatDimension = new Dimension(350, 160);
	    Dimension textBoxDimension = new Dimension(200, 40);
	    Dimension sendDimension = new Dimension(90, 40);

	    JTextArea chat = new JTextArea(10,20);
	    JTextField textBox = new JTextField(20);
	    Font font = new Font("Times New Roman", Font.BOLD, 16);

	    JScrollPane scroll = new JScrollPane(chat);
	    
	    PrintWriter pout = null;
	    BufferedReader bin = null;
	    
	    public ChatBox(Socket server) throws Exception {
	        super("Chat Box Client");
	        
	        OutputStream out = server.getOutputStream();
			pout = new PrintWriter(out, true);
			InputStream in = server.getInputStream();
			bin = new BufferedReader(new InputStreamReader(in));
			
	        //setSize(400, 400);
	        setResizable(true);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        GridLayout grid = new GridLayout(2,2);
	        setLayout(grid);
	        
	        FlowLayout f1 = new FlowLayout(FlowLayout.CENTER,10,10);
	        FlowLayout f2 = new FlowLayout(FlowLayout.CENTER,10,10);
	        for(int i = 0; i < 2; i++)
	            row[i] = new JPanel();
	        row[0].setLayout(f1);
	        row[1].setLayout(f2);
	        
	        sendButton = new JButton();
	        sendButton.setText("Send");
	        sendButton.setFont(font);
	        sendButton.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent ae) {
	        		send();
	        	}
	        });
	        
	        textBox.addKeyListener(new KeyListener() {
	        	public void keyPressed(KeyEvent e) {
	        		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
	        			send();
	        		}
	        	}
	        	public void keyReleased(KeyEvent e) { }
	        	public void keyTyped(KeyEvent e) { }
	        });
	        
	        chat.setFont(font);
	        chat.setEditable(false);
	        chat.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	        //chat.setPreferredSize(chatDimension);

			scroll = new JScrollPane(chat);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroll.setBounds(0, 0, 350, 1000);
	        
	        textBox.setFont(font);
	        textBox.setEditable(true);
	        textBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	        textBox.setPreferredSize(textBoxDimension);
	        
	        sendButton.setPreferredSize(sendDimension);
	        
	        row[0].add(scroll);
	        add(row[0]);
	        
	        row[1].add(textBox);
	        row[1].add(sendButton);
	        add(row[1]);
	        
	        pack();
	        setLocationRelativeTo(null);
	        setVisible(true);
	    }
	    
	    public void send() {
	    	String request = textBox.getText();
	    	if(request.equals("/quit")) {
				Client.finished = true;
				//chat.append("You are leaving the server.\n");
			}
			pout.println(request);
	    	textBox.setText("");
	    }
	    
	    public void send(String text) {
	    	chat.append(text + "\n");
	    }
	    
	    public void run() {
	    	while(!Client.finished) {
				try {
					String response = bin.readLine();
					send(response);
				} catch(IOException e) {
					if(!Client.finished) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
  
	public static void main(String[] args) throws Exception {
		Socket server = null;
		Scanner input = new Scanner(System.in);
		try {
			//ChatBox c = new ChatBox();
			//c.send("What IP do you want to connect to?");
			System.out.println("What IP do you want to connect to?");
			String ip = input.nextLine();
			server = new Socket(ip, 1337);
			ChatBox c = new ChatBox(server);
			Thread cThread = new Thread(c);
			cThread.start();
			cThread.join();
    	} catch (IOException e ) {
    		System.out.println(e.getMessage());
    	} finally {
    		if (server != null) {
    			try {
    				server.close();
    			} catch (IOException e) {
    				System.out.println(e.getMessage());
    			}
    		}
    		input.close();
    	}
	}
}