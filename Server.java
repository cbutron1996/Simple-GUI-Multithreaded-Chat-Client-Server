import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class Server {
	
  private static ServerSocket serverSocket = null;
  private static Socket clientSocket = null;

  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  public static void main(String args[]) throws Exception {
	  
    int portNumber = 1337;
    
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }
    
    ChatBox c = new ChatBox();
    
    //System.out.println("Server port number: " + portNumber);
    c.send("Server port number: " + portNumber);
    
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads, c)).start();
            break;
          }
        }
        if (i == maxClientsCount) {
          OutputStream out = clientSocket.getOutputStream();
          PrintWriter pout = new PrintWriter(out, true);
          pout.println("Server too busy. Try later.");
          pout.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

class clientThread extends Thread {

  private String name = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  
  private BufferedReader bin = null;
  private PrintWriter pout = null;
  
  private ChatBox c = null;

  public clientThread(Socket clientSocket, clientThread[] threads, ChatBox x) throws Exception {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    
    InputStream in = clientSocket.getInputStream();
	bin = new BufferedReader(new InputStreamReader(in));
	  
	OutputStream out = clientSocket.getOutputStream();
	pout = new PrintWriter(out, true);
	
	c = x;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      while (true) {
        pout.println("Enter your name.");
        name = bin.readLine();
        break;
      }
      
      pout.println("Welcome to the chatroom, " + name + ".");
      pout.println("To leave enter /quit in a new line.");
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this) {
            threads[i].pout.println("*** " + name + " entered the chat room! ***");
          }
        }
        //System.out.println("*** " + name + " entered the chat room! ***");
        c.send("*** " + name + " entered the chat room! ***");
      }
      
      while (true) {
        String line = bin.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        synchronized (this) {
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i].name != null) {
              threads[i].pout.println("<" + name + "> " + line);
            }
          }
          //System.out.println("<" + name + "> " + line);
          c.send("<" + name + "> " + line);
        }
      }
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] != this && threads[i].name != null) {
            threads[i].pout.println("*** The user " + name + " is leaving the chat room. ***");
          }
        }
        //System.out.println("*** The user " + name + " is leaving the chat room. ***");
        c.send("*** The user " + name + " is leaving the chat room. ***");
      }
      pout.println("*** Bye " + name + " ***");
      
      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      
      bin.close();
      pout.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}

class ChatBox extends JFrame {
    
    JPanel[] row = new JPanel[2];
    JButton sendButton = null;
    
    Dimension chatDimension = new Dimension(350, 160);
    Dimension textBoxDimension = new Dimension(200, 40);
    Dimension sendDimension = new Dimension(90, 40);

    JTextArea chat;
    JTextField textBox = new JTextField(20);
    JScrollPane scroll;
    Font font = new Font("Times New Roman", Font.BOLD, 16);
    
    String name = null;
    
    ChatBox() {
        super("Chat Box Server");
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

        chat = new JTextArea(10,20);
        chat.setFont(font);
        chat.setEditable(false);
        chat.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        chat.setPreferredSize(chatDimension);

        scroll = new JScrollPane(chat);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setBounds(0, 0, 350, 160);
        
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
    	chat.append(textBox.getText());
    	textBox.setText("");
    }
    
    public void send(String text) {
    	chat.append(text + "\n");
    }
}
