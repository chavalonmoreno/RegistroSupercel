import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.squareup.okhttp.*;
import com.squareup.okhttp.MediaType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;


/**
 *
 * @author alexisMoreno
 */
public class Main {
    private static final String usuario = "CEt00d1b0";
    private static final String password = "centel01";
    private static final String tipo = "DISTRIBUIDOR";
    private static final String fechaActual = "24%20de%20Octubre%20del%202017";
    private static final Integer tamanoNombres = 100;
    private static final Integer tamanoDirecciones = 719;
    private static final Integer tamanoCalles = 201;
    public static  String cookie = "";
    private static final String mensaje = "Telcel Regi&oacute;n 2";
    private static final String msjExitosoSava = "ha sido enviado con exito";
    private static final String linkUsuario = "https://region2.telcel.com/validamefv.asp";
    private static final String linkLogIN = "https://region2.telcel.com/contenido2.asp";
    private static final String linkNumero = "https://region2.telcel.com/distribuidor/regdol/regdol_checa_v2.asp?w=2";
    private static final String linkRegistro = "https://region2.telcel.com/distribuidor/regdol/regdol_procesa_v2.asp";
    private static final String linkLogOUT = "https://region2.telcel.com/default.asp";
    private static final String [] meses = {"Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre"};
    private static final String rutaCorrectos = "archivos/registradosbien.txt";
    private static final String rutaIncorrectos = "archivos/tronados.txt";
    static HttpURLConnection connection = null;
    static String responseBody = null;
    public static String targetUrl = "";
    static Random mr = new Random();


    public static void main(String[] args) throws IOException {
        procesarArchivo();
        //getLogOut();

        System.exit(0);
    }

    public static void procesarArchivo (){
        Linea linea;
        Usuario usuarioLog;
        int c = 0;
        try {
            ArrayList<String> lineas = getLinesOfFile("archivos/tronados.txt");
            ArrayList<String> lineasRegistradas = getLinesOfFile("archivos/registradosbien.txt");
            ArrayList<String> nombres = getLinesOfFile("archivos/NOMBRES.txt");
            ArrayList<String> apellidos = getLinesOfFile("archivos/APELLIDOS.txt");
            ArrayList<String> direcciones = getLinesOfFile("archivos/COLONIASCPCLN.txt");
            ArrayList<String> calles = getLinesOfFile("archivos/CALLES.txt");
            usuarioLog = new Usuario();
            usuarioLog.setUser(usuario);
            usuarioLog.setPass(password);
            usuarioLog.setTipo(tipo);
            postUsuario(usuarioLog);
            getLogIn();
           /*for ( String renglon : lineas ){
                String[] contenido = renglon.split("\\|");
                String telefono = contenido[0];
                if (!yaFueRegistrado(telefono,lineasRegistradas)) {
                    linea = new Linea();
                    linea.setTelefono(telefono);
                    Direccion direccion = getDireccion(direcciones.get(mr.nextInt(tamanoDirecciones)));
                    linea.setNombre((nombres.get(mr.nextInt(tamanoNombres))).replaceAll(" ", "%20"));
                    linea.setAp_paterno(apellidos.get(mr.nextInt(tamanoNombres)));
                    linea.setAp_materno(apellidos.get(mr.nextInt(tamanoNombres)));
                    linea.setColonia(direccion.getColonia().replaceAll(" ", "%20"));
                    linea.setCp(direccion.getCp());
                    linea.setNumero(getNumeroCasaAleatorio());
                    linea.setTel_casa(getNumeroTelefonicoAleatorio());
                    linea.setCalle(calles.get(mr.nextInt(tamanoCalles)).replaceAll(" ", "%20"));
                    linea.setEstado("SIN");
                    linea.setCiudad("CULIACAN");
                    linea.setFecha(fechaActual);
                    linea.setFecha_activacion(getFechaActivacionAleatoria());
                    linea.setUsuario(usuario);
                    Thread.sleep(2000);
                    String resultado = postSave(linea);
                    if ( resultado.equals("BIEN")) {
                        escribirLineaRegistrada(linea,rutaCorrectos);
                    } else {
                        escribirLineaRegistrada(linea,rutaIncorrectos);
                    }
                    System.out.println(linea.toString());

                    System.out.println("Total : "+c++);
                }
            }*/
           getLogOut();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (InterruptedException e) {
            e.printStackTrace();
        } */catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean yaFueRegistrado ( String numero , ArrayList <String> lineasRegistradas ) {
        for ( String l : lineasRegistradas) {
            String[] contenido = l.split("\\|");
            if ( contenido[0] .equals(numero)  ) {
                return true;
            }
        }
        return false;
    }

    public static void escribirLineaRegistrada ( Linea linea, String ruta ) throws IOException{
        try {
            File file = new File(ruta);
            if (!file.exists()) {
                file.createNewFile();
            }
            String content = linea.getTelefono() + "|" + linea.getNombre().replaceAll("%20"," ") +
                    "|" + linea.getAp_paterno() + "|" +
                    linea.getAp_materno() + "|" + linea.getCalle().replaceAll("%20"," ")  + "|" +
                    linea.getNumero() + "|" + linea.getColonia().replaceAll("%20"," ")
                    + "|" + linea.getCp() + "|" + linea.getTel_casa();
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.newLine();
            bw.close();
        } catch ( IOException ioe) {

        }
    }

    public static void getLogIn () {
        try {
            readJsonFromUrlGetLogIn(linkLogIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getLogOut () {
        try {
            readJsonFromUrlGetLogOut(linkLogOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postUsuario (Usuario usuarioLog) throws Exception {
        try {
            readJsonFromUrlPostLogIn(linkUsuario);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String postSave (Linea linea) throws Exception {
        return readJsonFromUrlPost(linkRegistro,linea);

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
        return (dia + " de " + mes + " del 2017").replaceAll(" ","%20");
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

    public static void readJsonFromUrlPostLogIn(String tURL) throws Exception {
        Integer codigo = null;
        try {
            HttpResponse<String> response = Unirest.post("https://region2.telcel.com/validamefv.asp")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("tipo="+tipo+"&user="+usuario+"&pass="+password)
                    .asString();
            codigo = response.getStatus();
            System.out.println(codigo);
            if ( codigo == 200 ) {
                System.out.println("TODO BIEN GET");
            } else {
                System.out.println("TRONO GET");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        }

    }

    public static void readJsonFromUrlGetLogIn(String tURL) throws Exception {
        Integer codigo = null;
        SAXBuilder builder = new SAXBuilder();

        try {
            HttpResponse<String> response = Unirest.get("https://region2.telcel.com/contenido2.asp")
                    .header("cache-control", "no-cache")
                    .asString();
            codigo = response.getStatus();
            responseBody = response.getBody();
            Document document = (Document) builder.build(responseBody);
            Element raiz = document.getRootElement();
            System.out.println(raiz.getText());
            System.out.println(codigo);

            if ( codigo == 200 && responseBody.contains(mensaje)) {
                System.out.println("TODO BIEN GET");
            } else {
                System.out.println("TRONO GET");
            }
            } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        }

    }

    public static String readJsonFromUrlPost(String tURL, Linea linea) throws IOException, JSONException, Exception {
        URL url;
        Integer codigo = null;
        SAXBuilder builder = new SAXBuilder();
        String resultado = "";
        try {

            HttpResponse<String> response = Unirest.post("https://region2.telcel.com/distribuidor/regdol/regdol_procesa_v2.asp")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("existe=1" +
                            "&telefono="+linea.getTelefono() +
                            "&plataforma=GSM" +
                            "&plan=CEL" +
                            "&modalidad=CPP" +
                            "&distribuidor=SUP" +
                            "&fecha_activacion="+linea.getFecha_activacion() +
                            "&titulo=" +
                            "&nombre="+linea.getNombre() +
                            "&ap_paterno="+linea.getAp_paterno() +
                            "&ap_materno="+linea.getAp_materno() +
                            "&calle="+linea.getCalle() +
                            "&numero="+linea.getNumero() +
                            "&colonia="+linea.getColonia() +
                            "&cp="+linea.getCp() +
                            "&ciudad=culiacan" +
                            "&estado=SIN" +
                            "&ocupacion=" +
                            "&rfc=" +
                            "&tel_casa="+linea.getTel_casa() +
                            "&tel_oficina=" +
                            "&edad=" +
                            "&email=telcel%40telcel.com" +
                            "&usuario="+linea.getUsuario() +
                            "&fecha="+fechaActual)
                    .asString();
                codigo = response.getStatus();
                responseBody = response.getBody();
            System.out.println(codigo);

            if ( codigo == 200 && responseBody.contains(msjExitosoSava)){
                System.out.println("TODO BIEN POST");
                resultado = "BIEN";
            } else {
                System.out.println("TRONO POST");
                resultado = "TRONO";
            }
            System.out.println("---------------------------------------------------");
         } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        }
        return resultado;
    }


    public static void readJsonFromUrlGetLogOut(String tURL) throws IOException,  Exception {
        URL url;
        Integer codigo = null;
        SAXBuilder builder = new SAXBuilder();

        try {
            HttpResponse<String> response = Unirest.get("https://region2.telcel.com/default.asp")
                    .header("cache-control", "no-cache")
                    .asString();
            responseBody = response.getBody();
            //System.out.println(responseBody);
            codigo = response.getStatus();
            if ( codigo == 200 && responseBody.contains(mensaje)){
                System.out.println("TODO BIEN POST");
            } else {
                System.out.println("TRONO POST");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        }
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
    private String distribuidor = "CET" +
            "";
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

    private String tipo;
    private String user;
    private String pass;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

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


}
