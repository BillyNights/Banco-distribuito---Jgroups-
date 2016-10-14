package bancobeans;

import java.util.*;

import java.io.*;
import java.util.*;
import org.jgroups.*;
import org.jgroups.util.Util;

public class Persistencia extends ReceiverAdapter implements Serializable {
    private List<Conta> banco;
    JChannel channel;
    JChannel channelState;
    String user_name = System.getProperty("user.name", "n/a");

    public Persistencia() {
        banco=new LinkedList<Conta>();
    }

    public void salvaArquivo(List<Conta> banco) {
        try {
            FileOutputStream saveFile = new FileOutputStream("persistencia.ser");
            try (ObjectOutputStream stream = new ObjectOutputStream(saveFile)) {
                stream.writeObject(banco);
            }
        } catch (Exception exc) {
        }
    }

    @SuppressWarnings("unchecked")
    public void carregaArquivo(List<Conta> banco) {
        try {
            FileInputStream restFile = new FileInputStream("persistencia.ser");
            try (ObjectInputStream stream = new ObjectInputStream(restFile)) {
                banco = (List<Conta>) stream.readObject();
                for (Conta conta : banco) {
                    System.out.println(conta.getConta());
                    System.out.println(conta.getSaldo());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void receive(Message msg) {
        System.out.println("Atualizando...");
        banco = (List<Conta>) msg.getObject();
    }
    
            @Override
            public void getState(OutputStream output) throws Exception {                
                synchronized (banco) {
                    Util.objectToStream(banco, new DataOutputStream(output));
                }
                output.close();
            }

            @Override
            public void setState(InputStream input) throws Exception {
                List<Conta> list;
                list = (List<Conta>) Util.objectFromStream(new DataInputStream(input));
                synchronized (banco) {
                    banco.clear();
                    banco.addAll(list);
                }
                input.close();
                System.out.print("ESTADO RECEBIDO: ");
                for (Conta conta : list) {
                    System.out.print(conta.getConta()+", ");
                }
                System.out.println(".");
            }

    private void eventLoop() {
        while (true) {
            try {
                Thread.sleep(5000);
                System.out.println("Salvando dados em arquivo.");
                salvaArquivo(banco);
            } catch (InterruptedException e) {
            }
        }
    }

    public void atualizaPersistencia(Address dest, List<Conta> banco) throws Exception {
        Message msg = new Message(dest);
        msg.setObject(banco);
        channelState.send(msg);
    }

    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("Persistencia");
        channelState = new JChannel();
        channelState.setReceiver(
            new ReceiverAdapter() {
            @Override
            public void receive(Message msg) {
                
            }
            public void viewAccepted(View view){
                if (view.size()==1){
                    carregaArquivo(banco);
                }
            }
            
            @Override
            public void getState(OutputStream output) throws Exception {                
                synchronized (banco) {
                    Util.objectToStream(banco, new DataOutputStream(output));
                }
                output.close();
            }

            @Override
            public void setState(InputStream input) throws Exception {
                List<Conta> list;
                list = (List<Conta>) Util.objectFromStream(new DataInputStream(input));
                synchronized (banco) {
                    banco.clear();
                    banco.addAll(list);
                }
                input.close();
                System.out.print("ESTADO RECEBIDO: ");
                for (Conta conta : list) {
                    System.out.print(conta.getConta()+", ");
                }
                System.out.println(".");
            }
        }
        );
        channelState.connect("PersistenciaCluster");
        channelState.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    public static void main(String[] args) throws Exception {
        Persistencia persistencia = new Persistencia();
        persistencia.start();
    }
}
