package ChatRoomRMI;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserChat extends UnicastRemoteObject implements IUserChat
{
    protected UserChat(String name) throws RemoteException
    {
        super();
        this.name = name;
        currentRoom = null;
    }

    private static String name;
    private static IServerChat server;
    private static UserChat userChat;
    private static String serverIP;

    private IRoomChat currentRoom;

    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JComboBox<String> roomComboBox;
    private JButton joinButton, leaveButton, sendButton, createRoomButton, refreshRoomListButton;

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException
    {
        if(messageArea != null)
        {
            SwingUtilities.invokeLater(() -> {
                messageArea.append(senderName + ": " + msg + "\n");

                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            });
        }

        if (msg.equals("Sala fechada pelo servidor."))
        {
            currentRoom = null;
            updateRoomList("", false);
        }
    }

    private void updateRoomList(String selectedRoom, boolean shouldSetSelected)
    {
        try
        {
            ArrayList<String> rooms = server.getRooms();
        
            SwingUtilities.invokeLater(() -> {
                roomComboBox.removeAllItems();

                for (String room : rooms)
                {
                    roomComboBox.addItem(room);
                }
                
                if (shouldSetSelected)
                {
                    roomComboBox.setSelectedItem(selectedRoom);
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void initGUI(IServerChat server)
    {
        frame = new JFrame("Chat - " + name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new BorderLayout());
        messageArea = new JTextArea();
        messageArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout());

        roomComboBox = new JComboBox<>();

        try
        {
            ArrayList<String> rooms = server.getRooms();
            for (String room : rooms) {
                roomComboBox.addItem(room);
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }

        bottomPanel.add(roomComboBox, BorderLayout.NORTH);
        inputField = new JTextField();
        bottomPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        joinButton = new JButton("Entrar");
        leaveButton = new JButton("Sair");
        sendButton = new JButton("Enviar");
        createRoomButton = new JButton("Criar Sala");
        refreshRoomListButton = new JButton("Recarregar Salas");

        createRoomButton.addActionListener(e -> {
            String newRoomName = JOptionPane.showInputDialog(
                    null,
                    "Digite o nome da sala:",
                    "Crie uma sala",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (newRoomName == null || newRoomName.trim().isEmpty()) {
                return;
            }

            try
            {
                server.createRoom(newRoomName);
                updateRoomList(newRoomName, true);
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog(frame,
                        "Erro ao criar sala: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        joinButton.addActionListener(e -> {
            try
            {
                String roomName = (String) roomComboBox.getSelectedItem();

                if (roomName != null)
                {
                    messageArea.setText("");

                    if (currentRoom != null)
                    {
                        currentRoom.leaveRoom(name);
                    }

                    currentRoom = (IRoomChat) Naming.lookup("rmi://" + serverIP + ":2020/" + roomName);
                    currentRoom.joinRoom(name, userChat);

                    updateRoomList(roomName, true);
                    messageArea.append("Você entrou na sala: " + roomName + "\n");
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame,
                        "Erro ao entrar na sala: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        sendButton.addActionListener(e -> {
            try {
                if (currentRoom != null) {
                    String msg = inputField.getText();
                    currentRoom.sendMsg(name, msg);
                    inputField.setText("");
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        leaveButton.addActionListener(e -> {
            try {
                if (currentRoom != null) {
                    currentRoom.leaveRoom(name);
                    messageArea.append("Você saiu da sala: " + currentRoom.getRoomName() + "\n");
                    currentRoom = null;
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        refreshRoomListButton.addActionListener(e -> {
            try
            {
                if (currentRoom != null)
                {
                    updateRoomList(currentRoom.getRoomName(), true);
                }
                else
                {
                    updateRoomList("", false);
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            
        });

        buttonPanel.add(joinButton);
        buttonPanel.add(leaveButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(createRoomButton);
        buttonPanel.add(refreshRoomListButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        String userName = JOptionPane.showInputDialog(
                null,
                "Digite seu nome:",
                "Entrada no Chat",
                JOptionPane.PLAIN_MESSAGE
        );

        serverIP = JOptionPane.showInputDialog(
                null,
                "Digite o IP do Servidor:",
                "Entrada no Chat",
                JOptionPane.PLAIN_MESSAGE
        );

        if (userName == null || userName.trim().isEmpty())
        {
            System.exit(0);
        }

        if (serverIP == null || serverIP.trim().isEmpty())
        {
            System.exit(0);
        }

        try
        {
            server = (IServerChat) Naming.lookup("rmi://" + serverIP + ":2020/Servidor");
            userChat = new UserChat(userName);

            SwingUtilities.invokeLater(() -> {
                userChat.initGUI(server);
            });
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao conectar ao servidor: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
}