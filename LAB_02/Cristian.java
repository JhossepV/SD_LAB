package LAB_02;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Algoritmo de Cristian - Sincronización de Relojes en Sistemas Distribuidos
 * 
 * El algoritmo funciona así:
 * 1. Cliente solicita la hora al servidor
 * 2. Servidor responde con su hora actual
 * 3. Cliente calcula el offset considerando el tiempo de viaje (RTT)
 * 4. Cliente ajusta su reloj local
 */

// Clase Servidor - Proporciona la hora actual
class CristianServer {
  private static final int PUERTO = 9999;
  // Simula un reloj adelantado respecto al sistema
  private static long DESPLAZAMIENTO_SERVIDOR = 5000; // 5 segundos adelantado

  public void iniciar() {
    try {
      ServerSocket servidor = new ServerSocket(PUERTO);
      System.out.println("[SERVIDOR] Escuchando en puerto " + PUERTO);
      System.out.println("[SERVIDOR] Reloj del servidor adelantado " + DESPLAZAMIENTO_SERVIDOR + " ms\n");

      while (true) {
        Socket cliente = servidor.accept();
        new Thread(new ManejadorCliente(cliente)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class ManejadorCliente implements Runnable {
    private Socket socket;

    ManejadorCliente(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        DataInputStream entrada = new DataInputStream(socket.getInputStream());
        DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

        // Leer solicitud del cliente
        String solicitud = entrada.readUTF();

        // Enviar hora del servidor (simulada con desplazamiento)
        long horaServidor = System.currentTimeMillis() + DESPLAZAMIENTO_SERVIDOR;
        salida.writeLong(horaServidor);
        salida.flush();

        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

// Clase Cliente - Sincroniza su reloj con el servidor
class CristianClient {
  private static final String HOST = "localhost";
  private static final int PUERTO = 9999;
  private long offsetReloj = 0; // Diferencia entre reloj local y servidor
  private long[] historicoDesviaciones = new long[2]; // Almacenar deviaciones
  private int numSincronizaciones = 0;

  /**
   * Realiza la sincronización usando el algoritmo de Cristian
   */
  public void sincronizar(int numero) {
    try {
      // 1. Registrar tiempo local ANTES de enviar solicitud
      long T0 = System.currentTimeMillis();

      // 2. Conectar con el servidor
      Socket socket = new Socket(HOST, PUERTO);
      DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
      DataInputStream entrada = new DataInputStream(socket.getInputStream());

      // 3. Enviar solicitud
      salida.writeUTF("SINCRONIZAR");
      salida.flush();

      // 4. Recibir hora del servidor
      long T_servidor = entrada.readLong();

      // 5. Registrar tiempo local DESPUÉS de recibir respuesta
      long T1 = System.currentTimeMillis();

      socket.close();

      // 6. Calcular el tiempo de viaje (Round Trip Time)
      long RTT = T1 - T0;

      // Offset anterior (antes de sincronizar)
      long offsetAnterior = offsetReloj;

      // 7. Calcular offset considerando el delay de viaje
      offsetReloj = T_servidor + (RTT / 2) - T1;

      // Guardar desviación
      historicoDesviaciones[numSincronizaciones] = offsetReloj;
      numSincronizaciones++;

      // Mostrar detalles de la sincronización
      mostrarSincronizacion(numero, T0, T1, T_servidor, RTT, offsetAnterior, offsetReloj);

    } catch (IOException e) {
      System.err.println("[CLIENTE] Error de conexión: " + e.getMessage());
    }
  }

  /**
   * Obtiene la hora sincronizada
   */
  public long obtenerHoraSincronizada() {
    return System.currentTimeMillis() + offsetReloj;
  }

  /**
   * Obtiene la hora sin sincronizar (desviada)
   */
  public long obtenerHoraDesviada() {
    return System.currentTimeMillis();
  }

  /**
   * Muestra los detalles del proceso de sincronización
   */
  private void mostrarSincronizacion(int numero, long T0, long T1, long T_servidor,
      long RTT, long offsetAnterior, long offsetNuevo) {
    System.out.println("\n╔════════════════════════════════════════════════╗");
    System.out.println("║ SINCRONIZACIÓN #" + numero + " - ALGORITMO DE CRISTIAN       ║");
    System.out.println("╚════════════════════════════════════════════════╝");
    System.out.println("  T0 (envío):              " + String.format("%6d", T0) + " ms");
    System.out.println("  T1 (recepción):          " + String.format("%6d", T1) + " ms");
    System.out.println("  RTT:                     " + String.format("%6d", RTT) + " ms");
    System.out.println("  ────────────────────────────────────────");
    System.out.println("  Hora servidor:           " + String.format("%6d", T_servidor) + " ms");
    System.out.println("  ────────────────────────────────────────");
    System.out.println("  Offset anterior:         " + String.format("%+6d", offsetAnterior) + " ms");
    System.out.println("  Offset actual:           " + String.format("%+6d", offsetNuevo) + " ms");
    System.out.println("  Corrección aplicada:   " + String.format("%+6d", offsetNuevo - offsetAnterior) + " ms");
  }
}

// Clase Principal - Demostración del algoritmo
public class Cristian {
  public static void main(String[] args) throws InterruptedException {
    // Modo servidor
    if (args.length > 0 && args[0].equals("servidor")) {
      CristianServer servidor = new CristianServer();
      servidor.iniciar();
    }
    // Modo cliente
    else {
      // Iniciar servidor en un thread separado
      Thread servidorThread = new Thread(() -> {
        CristianServer servidor = new CristianServer();
        servidor.iniciar();
      });
      servidorThread.setDaemon(true);
      servidorThread.start();

      // Dar tiempo al servidor para iniciar
      Thread.sleep(1000);

      // Crear cliente y sincronizar
      CristianClient cliente = new CristianClient();

      System.out.println("\n" + "═".repeat(60));
      System.out.println("    DEMOSTRACION: ALGORITMO DE CRISTIAN");
      System.out.println("    Sincronización de Relojes Distribuidos");
      System.out.println("═".repeat(60));
      System.out.println("\nESCENARIO: El servidor tiene el reloj adelantado 5 segundos");
      System.out.println("OBJETIVO: Cliente sincroniza su reloj con el servidor\n");

      System.out.println("RELOJ DESINCRONIZADO (sin algoritmo de Cristian):");
      long horaDesincronizada = cliente.obtenerHoraDesviada();
      System.out.println("  Hora local: " + horaDesincronizada + " ms");

      // Realizar 2 sincronizaciones en intervalos de 5 segundos
      for (int i = 1; i <= 2; i++) {
        cliente.sincronizar(i);

        // Mostrar comparativa
        long horaSincronizada = cliente.obtenerHoraSincronizada();
        long horaDesviada = cliente.obtenerHoraDesviada();
        long diferencia = horaSincronizada - horaDesviada;

        System.out.println("\n COMPARATIVA:");
        System.out.println("     Hora sin sincronizar: " + String.format("%12d", horaDesviada) + " ms");
        System.out.println("     Hora sincronizada:    " + String.format("%12d", horaSincronizada) + " ms");
        System.out.println("     Diferencia:         " + String.format("%+12d", diferencia) + " ms");

        // Esperar antes de siguiente sincronización
        if (i < 2) {
          System.out.println("\n Esperando 5 segundos para siguiente sincronización...\n");
          Thread.sleep(5000);
        }
      }
    }
  }
}
