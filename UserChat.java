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
    private JButton joinButton, leaveButton, sendButton, createRoomButton;

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException
    {
        if(messageArea != null) {
            System.out.println(" - " + senderName + ": " + msg);
            SwingUtilities.invokeLater(() -> {
                messageArea.append(senderName + ": " + msg + "\n");

                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            });
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

        createRoomButton.addActionListener(e -> {
            String newRoomName = JOptionPane.showInputDialog(
                null,
                "Digite o nome da sala:",
                "Crie uma sala",
                JOptionPane.PLAIN_MESSAGE
            );

            if (newRoomName == null)
            {
                return;
            }

            try
            {
                server.createRoom(newRoomName);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
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
                    currentRoom.joinRoom(name, this);
                    try {
                        ArrayList<String> rooms = server.getRooms();
                        SwingUtilities.invokeLater(() -> {
                            roomComboBox.removeAllItems();
                            for (String r : rooms) {
                                roomComboBox.addItem(r);
                            }
                            roomComboBox.setSelectedItem(roomName);
                        });
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }

                    messageArea.append("[Sistema] Você entrou na sala: " + roomName + "\n");
                }
            } catch (Exception ex) {
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
                    messageArea.append("[Sistema] Você saiu da sala: " + currentRoom.getRoomName() + "\n");
                    currentRoom = null;
                }
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });

        buttonPanel.add(joinButton);
        buttonPanel.add(leaveButton);
        buttonPanel.add(sendButton);
        buttonPanel.add(createRoomButton);
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
            //server.createRoom("SALA-TESTE");
            UserChat userChat = new UserChat(userName);

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
