package Logica.cliente;

import Logica.DTOAlfabeto;
import Logica.DTOAlgoritmos;
import Logica.cliente.generacion.ConseqDupliBuilder;
import Logica.cliente.generacion.GeneradorStrings;
import Logica.cliente.generacion.NonConseqDupliBuilder;
import Logica.cliente.generacion.NonConseqNonDupliBuilder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class ControladorCliente {

    private DTOAlfabeto dtoAlfabeto;
    private DTOAlgoritmos dtoAlgoritmos;
    private DTOString dtoString;
    private Socket cliente;

    public void initialize(){

        dtoString = new DTOString();

        try{
            // Se conecta a local host en puerto 4444
            cliente = new Socket(InetAddress.getLocalHost(), 4444);

            if(cliente.isConnected()){
                cliente.setKeepAlive(true);
                recibirDTOs();
            }

        }catch (Exception e){
            System.out.println("No se pudo inicializar el controlador.");
        }
    }

    public DTOAlfabeto getDtoAlfabeto() {
        return dtoAlfabeto;
    }

    public void setDtoAlfabeto(DTOAlfabeto dtoAlfabeto) {
        this.dtoAlfabeto = dtoAlfabeto;
    }

    public DTOAlgoritmos getDtoAlgoritmos() {
        return dtoAlgoritmos;
    }

    public void setDtoAlgoritmos(DTOAlgoritmos dtoAlgoritmos) {
        this.dtoAlgoritmos = dtoAlgoritmos;
    }

    public DTOString getDtoString() {
        return dtoString;
    }

    public void setDtoString(DTOString dtoString) {
        this.dtoString = dtoString;
    }

    public void generarString(DTOString dto){

        GeneradorStrings generador = new GeneradorStrings();

        // Hace algo con esto
        switch (dto.getTipo()){
            case "Sin restricciones": {
                generador.setCreadorStrings(new ConseqDupliBuilder());
                generador.generarString(dto.getLargo(), dto.getAlfabeto());
                String resultado = generador.getString();
                dto.setResultado(resultado);
                break;
            }
            case "Sin consecutivos": {
                generador.setCreadorStrings(new NonConseqDupliBuilder());
                generador.generarString(dto.getLargo(), dto.getAlfabeto());
                String resultado = generador.getString();
                dto.setResultado(resultado);
                break;
            }
            case "Sin consecutivos ni duplicados": {
                generador.setCreadorStrings(new NonConseqNonDupliBuilder());
                generador.generarString(dto.getLargo(), dto.getAlfabeto());
                String resultado = generador.getString();
                dto.setResultado(resultado);
                break;
            }
        }
    }

    public void recibirDTOs() throws Exception{
        while(!cliente.isClosed() && cliente.isConnected()){
            if(cliente.getInputStream().available() > 0){
                // Recibe el primer objeto
                ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
                dtoAlgoritmos = (DTOAlgoritmos) in.readObject();
                // Recibe el segundo objeto
                dtoAlfabeto = (DTOAlfabeto) in.readObject();
                break;
            }
        }
    }

    public void enviarDTOs() throws Exception{
        if(!cliente.isClosed() && cliente.isConnected()){
            // Genera los streams de salida
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bs);

            // Escribe los dos objetos serializados
            out.writeObject(dtoAlgoritmos);
            out.writeObject(dtoAlfabeto);

            // Cierra el stream de objetos
            out.close();

            // Genera el buffer con los objetos
            byte[] bytes = bs.toByteArray();

            // Escribe el buffer
            cliente.getOutputStream().write(bytes);
        }
    }

    public void finalizar(){
        try {
            cliente.close();
        }catch (Exception e){
            System.out.println("El socket no se pudo cerrar");
        }
    }
}
