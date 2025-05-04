package ChatRoomRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat
{
    private Map<String, IUserChat> userList;
    private String name;

    protected RoomChat(String name) throws RemoteException
    {
        super();
        this.name = name;
    }

    @Override
    public void sendMsg(String usrName, String msg) throws RemoteException
    {
        for (IUserChat user : userList.values())
        {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public void joinRoom(String userName, IUserChat user) throws RemoteException
    {
        userList.put(userName, user);
        sendMsg("SERVER", userName + " entrou na sala.");
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException
    {
        userList.remove(usrName);
        sendMsg("SERVER", usrName + " saiu da sala.");
    }

    @Override
    public String getRoomName() throws RemoteException
    {
        return name;
    }

    @Override
    public void closeRoom() throws RemoteException
    {
        throw new UnsupportedOperationException("Unimplemented method 'closeRoom'");
    }    
}
