import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import com.mashape.unirest.http.Headers;
import org.apache.poi.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.sound.sampled.Line;


/**
 *
 * @author alexisMoreno
 */
public class Main {
    private static final String usuarioCen = "CET00D1B0";
    private static final String passwordCen = "centel04";
    //private static final String usuario = "Sup34d1b0";
    private static final String usuario = "Sup00d1b2";
    //private static final String password = "Supercel";
    private static final String password = "DICEL888";
    private static final String tipo = "DISTRIBUIDOR";
    private static final String fechaActual = "A%2022%20de%20Marzo%20del%202018";
    private static final String lugar = "CULIACAN,%20SINALOA";
    private static final String lugaryfecha = lugar+"%20"+fechaActual;
    private static final Integer tamanoNombres = 100;
    private static final Integer tamanoDirecciones = 719;
    private static final Integer tamanoCalles = 201;
    private static final String mensaje = "Telcel Regi&oacute;n 2";
    private static final String msjExitosoSava = "ha sido enviado con exito";
    private static final String linkUsuario = "https://region2.telcel.com/validamefv.asp";
    private static final String linkLogIN = "https://region2.telcel.com/contenido2.asp";
    private static final String linkNumero = "https://region2.telcel.com/distribuidor/regdol/regdol_checa_v2.asp?w=2";
    private static final String linkRegistro = "https://region2.telcel.com/distribuidor/regdol/regdol_procesa_v2.asp";
    private static final String linkLogOUT = "https://region2.telcel.com/default.asp";
    private static final String linkClaveRegistro = "https://region2.telcel.com/aplicaciones/activaciones/distribuidores/valida.asp?y=b";
    private static final String linkResuelve = "https://region2.telcel.com/aplicaciones/activaciones/distribuidores/resuelve_gsm_nodol_tarifa.asp";
    private static final String linkProcesa = "https://region2.telcel.com/aplicaciones/activaciones/distribuidores/procesa_gsm_nodol_tarifa.asp";
    private static final String [] meses = {"Diciembre"};
    private static final String rutaCorrectosDOL = "archivos/registradosCET0322.txt";
    private static final String rutaIncorrectosDOL = "archivos/tronadosCET0322.txt";
    private static final String rutaCorrectosCHIP = "archivos/chipgve031401.txt";
    private static final String rutaIncorrectosCHIP = "archivos/chipgve031401mal.txt";
    static String responseBody = null;
    static Random mr = new Random();
    private static final String ladaGve = "687";
    private static final String ladaCln = "667";
    private static final String ladaNvto = "672";
    private static final String ladaGml = "673";
    static String iccidComun = "8952020018122";
    static Integer inicio = 30001;
    static Integer fin = 30500;



    public static void main(String[] args) throws Exception {
        //pruebacreararchivo();
        procesarArchivo();
        //getLogOut();
        ///generarInformacionChip();
        //ordenarLineasRegistradas(rutaCorrectosCHIP);
        //crearExcel(rutaCorrectosCHIP.replaceAll(".txt",".xls"),rutaCorrectosCHIP.replaceAll(".txt","Ordenado.txt"));
        System.exit(0);
    }

    public static void generarInformacionChip () {
        Chip chip;
        Usuario usuarioLog;
        int c = 0;
        int t = 0;
        try {
            ArrayList<String> lineasRegistradas = getLinesOfFile(rutaCorrectosCHIP);
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
            postClaveRegistro(linkClaveRegistro);
            while ( inicio <= fin ) {
                if (!yaFueRegistradoChip(iccidComun+completarConCeros(inicio),lineasRegistradas)) {
                    chip = new Chip();
                    Direccion direccion = getDireccion(direcciones.get(mr.nextInt(tamanoDirecciones)));
                    chip.setIccid(iccidComun+completarConCeros(inicio));
                    chip.setCiudad_plaza("GUASAVE");
                    chip.setPlaza("15");
                    String nombre = (nombres.get(mr.nextInt(tamanoNombres))).replaceAll(" ", "%20");
                    String app = apellidos.get(mr.nextInt(tamanoNombres));
                    String apm = apellidos.get(mr.nextInt(tamanoNombres));
                    chip.setNombre(nombre + "%20" + app + "%20" + apm);
                    chip.setCalle(calles.get(mr.nextInt(tamanoCalles)).replaceAll(" ", "%20"));
                    chip.setColonia(direccion.getColonia().replaceAll(" ", "%20"));
                    chip.setCp(direccion.getCp());
                    chip.setTel(getNumeroTelefonicoAleatorio());
                    chip.setCiudad("CULIACAN");
                    chip.setEstado("SINALOA");
                    chip.setLugaryfecha(lugaryfecha);
                    String resultado = postResuelve(linkResuelve, chip);
                    postProcesa(linkProcesa, chip);
                    if (resultado.equals("BIEN")) {
                        escribirLineaRegistradaChip(chip, rutaCorrectosCHIP);
                        c++;
                    } else {
                        escribirLineaRegistradaChip(chip, rutaIncorrectosCHIP);
                        t++;
                    }

                    System.out.println("Total bien: " + c);
                    System.out.println("Total mal: " + t);
                    Thread.sleep(3000);
                }
                inicio++;
            }

        } catch ( Exception e ) {
            System.out.println("Error causado por : "+ e.getMessage());
        } finally {
            getLogOut();
        }
    }

    public static void procesarArchivo (){
        Linea linea;
        Usuario usuarioLog;
        int c = 0;
        int t = 0;
        try {
            ArrayList<String> lineas = getLinesOfFile("archivos/CETLINEAS2203.csv");
            ArrayList<String> lineasRegistradas = getLinesOfFile(rutaCorrectosDOL);
            ArrayList<String> nombres = getLinesOfFile("archivos/NOMBRES.txt");
            ArrayList<String> apellidos = getLinesOfFile("archivos/APELLIDOS.txt");
            ArrayList<String> direcciones = getLinesOfFile("archivos/COLONIASCPCLN.txt");
            ArrayList<String> calles = getLinesOfFile("archivos/CALLES.txt");
            ArrayList<String> lineasFiltradas = getLineasFiltradas(lineas);
            usuarioLog = new Usuario();
            usuarioLog.setUser(usuario);
            usuarioLog.setPass(password);
            usuarioLog.setTipo(tipo);
            postUsuario(usuarioLog);
            getLogIn();
            for ( String telefono : lineasFiltradas ){
                if (!yaFueRegistrado(telefono,lineasRegistradas)) {
                    linea = new Linea();
                    linea.setDistribuidor("CET");
                    linea.setTelefono(telefono);
                    postNumeroDOL(linea);
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
                    //String resultado = postSave(linea);
                    /*if ( resultado.equals("BIEN")) {
                        escribirLineaRegistrada(linea,rutaCorrectosDOL);
                        c ++;
                    } else {
                        escribirLineaRegistrada(linea,rutaIncorrectosDOL);
                        t++;
                    }*/
                    System.out.println(linea.toString());

                    System.out.println("Total bien: "+c);
                    System.out.println("Total mal: "+t);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }/* catch (InterruptedException e) {
            e.printStackTrace();
        } */catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            getLogOut();
        }
    }

    public static void pruebacreararchivo ( ) throws Exception {

        Linea linea;
        Usuario usuarioLog;
        try {
            ArrayList<String> lineas = getLinesOfFile("archivos/CETLINEAS0115.csv");
            ArrayList<String> lineasRegistradas = getLinesOfFile("archivos/registradosCET0115.txt");
            ArrayList<String> nombres = getLinesOfFile("archivos/NOMBRES.txt");
            ArrayList<String> apellidos = getLinesOfFile("archivos/APELLIDOS.txt");
            ArrayList<String> direcciones = getLinesOfFile("archivos/COLONIASCPCLN.txt");
            ArrayList<String> calles = getLinesOfFile("archivos/CALLES.txt");
            ArrayList<String> lineasFiltradas = getLineasFiltradas(lineas);
            for ( String telefono : lineasFiltradas ){
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
                   // String resultado = postSave(linea);
                    escribirLineaRegistrada(linea,rutaCorrectosDOL);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }
    }

   public static ArrayList<String> getLineasFiltradas (ArrayList<String> lineasOriginales ) {
       ArrayList<String> lineasFiltradas = new ArrayList<>();
       for ( String linea : lineasOriginales){
           String [] valores = linea.split(",");
           if (valores[18].contains("DOL")) {
               lineasFiltradas.add(valores[10]);
           }

       }
       return lineasFiltradas;
   }


    public static boolean yaFueRegistrado ( String numero , ArrayList <String> lineasRegistradas ) {
        for ( String l : lineasRegistradas) {
            String[] contenido = l.split("\\|");
            if ( contenido[0].trim() .equals(numero.trim())  ) {
                return true;
            }
        }
        return false;
    }

    public static String completarConCeros ( Integer n ) {
        String numeroCompleto = n.toString();
        int largo = numeroCompleto.length();
        if ( largo == 1 ){
            numeroCompleto = "0000" + numeroCompleto;
        } else if ( largo == 2 ) {
            numeroCompleto = "000" + numeroCompleto;
        } else if ( largo == 3 ) {
            numeroCompleto = "00" + numeroCompleto;
        } else if ( largo == 4 ) {
            numeroCompleto = "0" + numeroCompleto;
        }
        return numeroCompleto;
    }

    public static boolean yaFueRegistradoChip ( String iccid , ArrayList <String> lineasRegistradas ) {
        for ( String l : lineasRegistradas) {
            String[] contenido = l.split("\\|");
            if ( contenido[0] .equals(iccid)  ) {
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

    public static void escribirLineaRegistradaChip ( Chip chip, String ruta ) throws IOException{
        try {
            File file = new File(ruta);
            if (!file.exists()) {
                file.createNewFile();
            }
            String content = chip.getIccid() + "|" + chip.getNombre().replaceAll("%20"," ") +
                    "|" + chip.getTelefono() + "|" + chip.getCalle().replaceAll("%20"," ")  + "|" +
                    chip.getColonia().replaceAll("%20"," ")
                    + "|" + chip.getCp() + "|" + chip.getTel() ;
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.newLine();
            bw.close();
        } catch ( IOException ioe) {

        }
    }

    public static void ordenarLineasRegistradas ( String ruta ) throws IOException {
        ArrayList<String> lineasRegistradas = getLinesOfFile(ruta);
        ArrayList<Chip> lineas = new ArrayList<>();
        File archivoOrigen = new File(ruta);
        String nombre = archivoOrigen.getName();
        String path = archivoOrigen.getPath();
        String nombreNuevoArchivo = path.replace(".txt","Ordenado.txt");
        Chip chip;
        try {
            for ( String linea : lineasRegistradas ){
                String [] contenido = linea.split("\\|");
                chip = new Chip();
                chip.setIccid(contenido[0]);
                chip.setNombre(contenido[1]);
                chip.setTelefono(contenido[2]);
                chip.setCalle(contenido[3]);
                chip.setColonia(contenido[4]);
                chip.setCp(contenido[5]);
                chip.setTel(contenido[6]);
                lineas.add(chip);
            }
            Collections.sort(lineas);
            for ( Chip c : lineas) {
                escribirLineaRegistradaChip(c,nombreNuevoArchivo);
            }
        } catch ( Exception ioe ){
            System.out.println("Error al ordenar el archivo causado por : " + ioe.getMessage());
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
            File file = new File(archivo);
            if (!file.exists()) {
                file.createNewFile();
            }
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
        String mes = meses[mr.nextInt(meses.length)];
        if ( diaNumero < 10) {
            dia = "0" + diaNumero;
        } else {
            dia += diaNumero;
        }
        return (dia + " de " + mes + " del 2018").replaceAll(" ","%20");
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
            numero += mr.nextInt(8)+1;
        }
        return numero;
    }

    public static String getNumeroCasaAleatorio () {
        return mr.nextInt(3900) + 100 + "";
    }

    public static void crearExcel ( String nombreExcel, String rutaTxt ) throws IOException {
        File archivo = new File(nombreExcel);

        Workbook workbook = new HSSFWorkbook();

        Sheet pagina = workbook.createSheet("Reporte de chips");

        CellStyle style = workbook.createCellStyle();

        ArrayList<String> lineasRegistradas = getLinesOfFile(rutaTxt);

        int c = 0;

        for ( String linea : lineasRegistradas ) {
            String valores [] = linea.split("\\|");
            String datos [] = { valores[0],valores[2] } ;
            Row fila = pagina.createRow(c);

            int n = 0;

            for ( String v : datos) {
                Cell celda = fila.createCell(n);
                celda.setCellStyle(style);
                celda.setCellValue(v);
                n ++;
            }

            c++;
        }
        FileOutputStream salida = new FileOutputStream(archivo);
        workbook.write(salida);
        workbook.close();
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
            responseBody = response.getBody();
            System.out.println(responseBody);
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

    public static void postNumeroDOL(Linea linea) throws Exception {
        int codigo ;
        try {
            HttpResponse<String> response = Unirest.post(linkNumero)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("telefeno="+linea.getTelefono())
                    .asString();
            codigo = response.getStatus();
            responseBody = response.getBody();
            org.jsoup.nodes.Document doc = Jsoup.parse(responseBody);
            Element body = doc.body();
            Element modalidad = body.selectFirst("input[name=modalidad]");
            Element fechaActivacion = body.selectFirst("input[name=fecha_activacion]");
            System.out.println("Modaliad = " + modalidad.toString());
            System.out.println("Fecha Activacion = " + fechaActivacion.toString());
            //System.out.println(responseBody);
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
                            "&distribuidor=" + linea.getDistribuidor()+
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

    public static void postClaveRegistro(String tURL) throws Exception {
        Integer codigo = null;
        try {
            HttpResponse<String> response = Unirest.post(linkClaveRegistro)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("pass=01004")
                    .asString();
            codigo = response.getStatus();
            System.out.println(codigo);
            if ( codigo == 200 ) {
                System.out.println("TODO BIEN POST CLAVE REGISTRO");
            } else {
                System.out.println("TRONO POST CLAVE REGISTRO");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex);
            throw new Exception(ex.getMessage());
        }

    }

    public static String postResuelve(String tURL, Chip chip) throws IOException, JSONException, Exception {
        URL url;
        Integer codigo = null;
        SAXBuilder builder = new SAXBuilder();
        String resultado = "";
        try {

            HttpResponse<String> response = Unirest.post(tURL)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("tipo="+chip.getTipo() +
                            "&iccid="+chip.getIccid() +
                            "&imei="+chip.getImei() +
                            "&marca="+chip.getMarca() +
                            "&modelo="+chip.getModelo() +
                            "&plaza=" +chip.getPlaza()+
                            "&nombre="+chip.getNombre() +
                            "&rfc=" +
                            "&calle="+chip.getCalle() +
                            "&colonia="+chip.getColonia() +
                            "&tel="+chip.getTel()+
                            "&fax="+chip.getFax()+
                            "&ciudad="+chip.getCiudad() +
                            "&estado="+chip.getEstado()+
                            "&cp="+chip.getCp() +
                            "&lugar="+lugar +
                            "&fecha="+fechaActual)
                    .asString();
            codigo = response.getStatus();
            responseBody = response.getBody();
            System.out.println(codigo);
           // System.out.println(responseBody);
            System.out.println("----------------------------");
            boolean vieneNumero = responseBody.contains(ladaGve);
            if ( codigo == 200 && vieneNumero){
                System.out.println("TODO BIEN POST RESULVE");
                Integer indice = responseBody.replaceAll(chip.getTel(),"").
                        replaceAll(chip.getIccid(),"").lastIndexOf(ladaGve);
                String numeroRespuesta = responseBody.substring(indice,indice+10);
                System.out.println("TELEFONO = "+numeroRespuesta + "ICCID = "+chip.getIccid());
                chip.setTelefono(numeroRespuesta);
                resultado = "BIEN";
            } else {
                System.out.println("TRONO POST RESUELVE");
                resultado = "TRONO";
            }
            System.out.println("---------------------------------------------------");
        } catch (Exception ex) {
            resultado = "TRONO";
            System.out.println(ex.getMessage());
        }
        return resultado;
    }

    public static void postProcesa(String tURL, Chip chip) throws IOException, JSONException, Exception {
        URL url;
        Integer codigo = null;
        SAXBuilder builder = new SAXBuilder();
        String resultado = "";
        try {

            HttpResponse<String> response = Unirest.post(tURL)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("cache-control", "no-cache")
                    .body("telefono="+chip.getTelefono()+
                            "&tipo="+chip.getTipo() +
                            "&iccid="+chip.getIccid() +
                            "&imei="+chip.getImei() +
                            "&marca="+chip.getMarca() +
                            "&modelo="+chip.getModelo() +
                            "&ciudad_plaza="+chip.getCiudad_plaza()+
                            "&plaza=" +chip.getPlaza()+
                            "&monto="+chip.getMonto()+
                            "&tarifa="+chip.getTarifa()+
                            "&nombre="+chip.getNombre() +
                            "&rfc=" +
                            "&calle="+chip.getCalle() +
                            "&colonia="+chip.getColonia() +
                            "&tel="+chip.getTel()+
                            "&fax="+chip.getFax()+
                            "&ciudad="+chip.getCiudad() +
                            "&estado="+chip.getEstado()+
                            "&cp="+chip.getCp() +
                            "&lugaryfecha="+lugaryfecha )
                    .asString();
            codigo = response.getStatus();
            responseBody = response.getBody();
            //System.out.println(responseBody);
            System.out.println(codigo);
            if ( codigo == 200 ){
                System.out.println("TODO BIEN POST PROCESA");
            } else {
                System.out.println("TRONO POST PROCESA");
            }
            System.out.println("---------------------------------------------------");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
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
    private String distribuidor = "";
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

class Chip implements Comparable<Chip>{
    private String telefono;
    private String tipo = "CHIP";
    private String iccid;
    private String imei = "909368900000118";
    private String marca = "";
    private String modelo = "";
    private String ciudad_plaza;
    private String plaza;
    private String monto = "$0.00";
    private String tarifa = "0";
    private String nombre;
    private String rfc = "";
    private String calle;
    private String colonia;
    private String tel;
    private String fax = "";
    private String ciudad;
    private String estado;
    private String cp;
    private String lugaryfecha;

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCiudad_plaza() {
        return ciudad_plaza;
    }

    public void setCiudad_plaza(String ciudad_plaza) {
        this.ciudad_plaza = ciudad_plaza;
    }

    public String getPlaza() {
        return plaza;
    }

    public void setPlaza(String plaza) {
        this.plaza = plaza;
    }

    public String getMonto() {
        return monto;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }

    public String getTarifa() {
        return tarifa;
    }

    public void setTarifa(String tarifa) {
        this.tarifa = tarifa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
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

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getLugaryfecha() {
        return lugaryfecha;
    }

    public void setLugaryfecha(String lugaryfecha) {
        this.lugaryfecha = lugaryfecha;
    }

    @Override
    public int compareTo(Chip o) {
        return Long.valueOf(this.getIccid()).compareTo(Long.valueOf(o.getIccid()));
    }
}
