package ChatRoomRMI;

import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IRoomChat extends java.rmi.Remote
{
    public void sendMsg(String usrName, String msg) throws RemoteException;
    public void joinRoom(String userName, IUserChat user) throws RemoteException;
    public void leaveRoom(String usrName) throws RemoteException;
    public String getRoomName() throws RemoteException;
    public void closeRoom() throws RemoteException;
    public ArrayList<String> getUserList() throws RemoteException;

}
