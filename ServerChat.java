package ChatRoomRMI;

import javax.swing.*;
import java.awt.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
    private Map<String, IRoomChat> roomList;
    private JFrame frame;
    private JTextArea logArea;
    private JList<String> roomListUI;

    public ServerChat() throws RemoteException {
        super();
        roomList = new HashMap<>();
        initGUI();
    }

    private void initGUI() {
        frame = new JFrame("Servidor de Chat - Porta 2020");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        JPanel panel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(logArea);
        panel.add(scrollLog, BorderLayout.CENTER);
        JPanel sidePanel = new JPanel(new BorderLayout());
        roomListUI = new JList<>(new DefaultListModel<>());
        JScrollPane scrollRooms = new JScrollPane(roomListUI);
        sidePanel.add(new JLabel("Salas Ativas:"), BorderLayout.NORTH);
        sidePanel.add(scrollRooms, BorderLayout.CENTER);
        JButton closeRoomButton = new JButton("Fechar Sala");
        closeRoomButton.addActionListener(e -> closeSelectedRoom());
        sidePanel.add(closeRoomButton, BorderLayout.SOUTH);
        panel.add(sidePanel, BorderLayout.EAST);
        frame.add(panel);
        frame.setVisible(true);
    }

    private void closeSelectedRoom() {
        String selectedRoom = roomListUI.getSelectedValue();
        if (selectedRoom != null) {
            try {
                IRoomChat room = roomList.get(selectedRoom);
                room.closeRoom();
                roomList.remove(selectedRoom);
                updateRoomList();
                log("Sala fechada: " + selectedRoom);
            } catch (RemoteException ex) {
                log("Erro ao fechar sala: " + ex.getMessage());
            }
        }
    }

    private void updateRoomList() {
        DefaultListModel<String> model = new DefaultListModel<>();
        roomList.keySet().forEach(model::addElement);
        roomListUI.setModel(model);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException {
        return new ArrayList<>(roomList.keySet());
    }

    @Override
    public void createRoom(String roomName) throws RemoteException {
        if (roomList.containsKey(roomName)) {
            log("Tentativa de criar sala existente: " + roomName);
            return;
        }

        try {
            RoomChat room = new RoomChat(roomName);
            roomList.put(roomName, room);
            Naming.rebind("rmi://localhost:2020/" + roomName, room);
            updateRoomList();
            log("Sala criada: " + roomName);
        } catch (Exception e) {
            log("Erro ao criar sala: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat();
            LocateRegistry.createRegistry(2020);
            Naming.rebind("rmi://localhost:2020/Servidor", server);
            server.log("Servidor iniciado na porta 2020");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao iniciar servidor: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}