

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 *
 * @author alexisMoreno
 */
public class Main {
    private static final String usuario = "SUP00T1B0";
    private static final String password = "supercel";
    private static final String tipo = "DISTRIBUIDOR";
    private static final String fechaActual = "09 de Septiembre del 2017";
    private static final Integer tamanoNombres = 100;
    private static final Integer tamanoDirecciones = 719;
    private static final Integer tamanoCalles = 201;
    private static final String linkUsuario = "https://region2.telcel.com/validamefv.asp";
    private static final String linkLogIN = "https://region2.telcel.com/contenido2.asp";
    private static final String linkNumero = "https://region2.telcel.com/distribuidor/regdol/regdol_checa_v2.asp?w=2";
    private static final String linkRegistro = "https://region2.telcel.com/distribuidor/regdol/regdol_procesa_v2.asp";
    private static final String linkLogOUT = "https://region2.telcel.com/default.asp";
    private static final String [] meses = {"Marzo","Abril","Mayo","Junio","Julio"};
    static HttpURLConnection connection = null;
    static Response response = null;
    static Request requestApi = null;
    static String responseBody = null;
    public static OkHttpClient client = new OkHttpClient();
    public static String targetUrl = "";
    static Random mr = new Random();

    public static void main(String[] args) throws IOException, JSONException {
        procesarArchivo();

    }

    public static void procesarArchivo () throws JSONException {
        Linea linea;
        Usuario usuarioLog;
        Timer timer = new Timer();
        try {
            ArrayList<String> lineas = getLinesOfFile("C:\\users\\alexi\\Documents\\CSVSUPERCEL\\PAGOCHIPEXPRESS.csv");
            ArrayList<String> nombres = getLinesOfFile("C:\\users\\alexi\\Documents\\CSVSUPERCEL\\NOMBRES.txt");
            ArrayList<String> apellidos = getLinesOfFile("C:\\users\\alexi\\Documents\\CSVSUPERCEL\\APELLIDOS.txt");
            ArrayList<String> direcciones = getLinesOfFile("C:\\users\\alexi\\Documents\\CSVSUPERCEL\\COLONIASCPCLN.txt");
            ArrayList<String> calles = getLinesOfFile("C:\\users\\alexi\\Documents\\CSVSUPERCEL\\CALLES.txt");
            usuarioLog = new Usuario();
            usuarioLog.setUser(usuario);
            usuarioLog.setPass(password);
            usuarioLog.setTipo(tipo);
            postUsuario(usuarioLog);
            getLogIn();
            for ( String renglon : lineas ){
                String[] contenido = renglon.split(",");
                linea = new Linea();
                linea.setTelefono(contenido[10]);
                Direccion direccion = getDireccion(direcciones.get(mr.nextInt(tamanoDirecciones)));
                linea.setNombre(nombres.get(mr.nextInt(tamanoNombres)));
                linea.setAp_paterno(apellidos.get(mr.nextInt(tamanoNombres)));
                linea.setAp_materno(apellidos.get(mr.nextInt(tamanoNombres)));
                linea.setColonia(direccion.getColonia());
                linea.setCp(direccion.getCp());
                linea.setNumero(getNumeroCasaAleatorio());
                linea.setTel_casa(getNumeroTelefonicoAleatorio());
                linea.setCalle(calles.get(mr.nextInt(tamanoCalles)));
                linea.setEstado("SIN");
                linea.setCiudad("CULIACAN");
                linea.setFecha(fechaActual);
                linea.setFecha_activacion(getFechaActivacionAleatoria());
                linea.setUsuario(usuario);
                System.out.println(linea.toString());
                Thread.sleep(1000);
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void getLogIn () {
        byte [] bs = {};
        try {
            readJsonFromUrlGet(linkLogIN,bs,"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postUsuario (Usuario usuarioLog) throws JSONException {
        try {
            byte[] token = {};
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
            com.squareup.okhttp.MediaType mediaType = com.squareup.okhttp.MediaType.parse("application/json; charset=utf-8");
            String json = gson.toJson(usuarioLog);
            RequestBody jsonLog = RequestBody.create(mediaType, json);
            readJsonFromUrlPost(linkUsuario, jsonLog, token, "");
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static ArrayList<String> getLinesOfFile ( String archivo ) throws IOException{
        ArrayList<String> lineas = new ArrayList<>();
        try {
            String cadena = "";
            BufferedReader bf = new BufferedReader(new FileReader( archivo ));
            while ( (cadena = bf.readLine()) != null ) {
                lineas.add(cadena);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lineas;
    }

    public static String getFechaActivacionAleatoria () {
        int diaNumero = mr.nextInt(27) + 1 ;
        String dia = "";
        String mes = meses[mr.nextInt(5)];
        if ( diaNumero < 10) {
            dia = "0" + diaNumero;
        } else {
            dia += diaNumero;
        }
        return dia + " de " + mes + " del 2017";
    }

    public static Direccion getDireccion (String linea ){
        Direccion direccion = new Direccion();
        String [] datos = linea.split(",");
        direccion.setColonia(datos[0]);
        direccion.setCp(datos[1].trim());
        if ( direccion.getCp().equals("0") ){
            direccion.setCp("80010");
        }
        return direccion;
    }

    public static String getNumeroTelefonicoAleatorio () {
        String numero = "667";
        for ( int i = 0; i < 7; i ++ ) {
            numero += mr.nextInt(9);
        }
        return numero;
    }

    public static String getNumeroCasaAleatorio () {
        return mr.nextInt(3900) + 100 + "";
    }

    public static void readJsonFromUrlGet(String tURL, byte[] token, String prefijo) throws IOException, JSONException, Exception {
        URL url;

        Integer codigo = null;


        client.setWriteTimeout(10, TimeUnit.SECONDS);
        try {
            String tokenStr = new String(token);

            requestApi = new Request.Builder()
                    .addHeader("Authorization", prefijo + " " + tokenStr)
                    .addHeader("Accept", "*/*")
                    .url(targetUrl + tURL)
                    .get()
                    .build();

            response = client.newCall(requestApi).execute();
            //responseBody = response.body().toString();
            //System.out.println(responseBody);

            codigo = response.code();
            if ( codigo == 200 ) {
                System.out.println("TODO BIEN GET");
            } else {
                System.out.println("TRONO GET");
            }
            /*try {
                if (codigo > 199 && codigo < 300) {
                } else if (codigo > 399 && codigo < 500) {
                    responseBody = response.body().string();
                    //String mensaje = new JSONObject(responseBody).get("message").toString();
                    System.out.println(responseBody);
                    System.out.println(mensaje);
                    throw new Exception(mensaje);
                } else if (codigo > 499 && codigo < 600) {
                    throw new Exception("Ocurrio un error en la api. Comuniquese con el administrador del sistema.");
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }

            if (!response.isSuccessful()) {
                System.out.println(response.body().string());
                throw new IOException("Unexpected code " + response.body().string());
            }*/

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
            throw new Exception(ex.getMessage());
        } finally {

        }
        //responseBody = response.body().string();
        //return new JSONObject(responseBody);
    }

    public static void readJsonFromUrlPost(String tURL, RequestBody formBody, byte[] token, String prefijo) throws IOException, JSONException, Exception {
        URL url;
        Integer codigo = null;

        client.setWriteTimeout(10, TimeUnit.SECONDS);
        try {
            String tokenStr = new String(token);

            requestApi = new Request.Builder()
                    .addHeader("Authorization", prefijo + " " + tokenStr)
                    .addHeader("Accept", "*/*")
                    .url(tURL)
                    .post(formBody)
                    .build();

            response = client.newCall(requestApi).execute();
            codigo = response.code();
            if ( codigo == 200 ){
                System.out.println("TODO BIEN POST");
            } else {
                System.out.println("TRONO POST");
            }
            /*try {
                if (codigo > 199 && codigo < 300) {
                } else if (codigo > 399 && codigo < 500) {
                    responseBody = response.body().string();
                    String mensaje = new JSONObject(responseBody).get("message").toString();
                    throw new Exception(mensaje);
                } else if (codigo > 499 && codigo < 600) {
                    throw new Exception("Ocurrio un error en la api. Comuniquese con el administrador del sistema.");
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }

            if (!response.isSuccessful()) {
                System.out.println(response.body().string());
                throw new IOException("Unexpected code " + response.body().string());
            }*/

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        } catch (JSONException ex) {
            System.out.println(ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        //responseBody = response.body().string();
        //return new JSONObject(responseBody);
    }
}

class Direccion {
    private String colonia;
    private String cp;

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }
}

class Linea {
    private String existe = "1";
    private String telefono;
    private String plataforma = "GSM";
    private String plan = "CEL";
    private String modalidad = "CPP";
    private String distribuidor = "SUP";
    private String fecha_activacion ;
    private String titulo = "";
    private String nombre ;
    private String ap_paterno;
    private String ap_materno;
    private String calle;
    private String numero;
    private String colonia;
    private String cp;
    private String ciudad;
    private String estado;
    private String ocupacion = "";
    private String rfc = "";
    private String tel_casa;
    private String tel_oficina = "";
    private String edad = "";
    private String email = "telcel@telcel.com";
    private String usuario;
    private String fecha;

    @Override
    public String toString() {
        return "Linea{" + "existe=" + existe + ", telefono=" + telefono + ", plataforma=" + plataforma + ", plan=" + plan + ", modalidad=" + modalidad + ", distribuidor=" + distribuidor + ", fecha_activacion=" + fecha_activacion + ", titulo=" + titulo + ", nombre=" + nombre + ", ap_paterno=" + ap_paterno + ", ap_materno=" + ap_materno + ", calle=" + calle + ", numero=" + numero + ", colonia=" + colonia + ", cp=" + cp + ", ciudad=" + ciudad + ", estado=" + estado + ", ocupacion=" + ocupacion + ", rfc=" + rfc + ", tel_casa=" + tel_casa + ", tel_oficina=" + tel_oficina + ", edad=" + edad + ", email=" + email + ", usuario=" + usuario + ", fecha=" + fecha + '}';
    }

    public String getExiste() {
        return existe;
    }

    public void setExiste(String existe) {
        this.existe = existe;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getDistribuidor() {
        return distribuidor;
    }

    public void setDistribuidor(String distribuidor) {
        this.distribuidor = distribuidor;
    }

    public String getFecha_activacion() {
        return fecha_activacion;
    }

    public void setFecha_activacion(String fecha_activacion) {
        this.fecha_activacion = fecha_activacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAp_paterno() {
        return ap_paterno;
    }

    public void setAp_paterno(String ap_paterno) {
        this.ap_paterno = ap_paterno;
    }

    public String getAp_materno() {
        return ap_materno;
    }

    public void setAp_materno(String ap_materno) {
        this.ap_materno = ap_materno;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getTel_casa() {
        return tel_casa;
    }

    public void setTel_casa(String tel_casa) {
        this.tel_casa = tel_casa;
    }

    public String getTel_oficina() {
        return tel_oficina;
    }

    public void setTel_oficina(String tel_oficina) {
        this.tel_oficina = tel_oficina;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

}

class Usuario {
    private String user;
    private String pass;
    private String tipo;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
