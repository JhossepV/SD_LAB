package LAB_02;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Algoritmo de Berkeley - Sincronización de Relojes Distribuidos
 * 
 * El algoritmo funciona así:
 * 1. Coordinador solicita la hora a todos los nodos
 * 2. Cada nodo responde con su hora actual (potencialmente desviada)
 * 3. Coordinador calcula la hora promedio y el offset para cada nodo
 * 4. Coordinador envía la corrección a cada nodo
 * 5. Cada nodo ajusta su reloj local
 */

// Clase que representa un Nodo (Cliente) con reloj propio
class Nodo {
  private int id;
  private long desplazamientoReloj; // Desviación del reloj del nodo
  private long offsetAplicado = 0; // Offset que se aplicará al sincronizar

  public Nodo(int id, long desplazamiento) {
    this.id = id;
    this.desplazamientoReloj = desplazamiento;
  }

  public int getId() {
    return id;
  }

  // Obtiene la hora actual del nodo (desviada)
  public long obtenerHoraLocal() {
    return System.currentTimeMillis() + desplazamientoReloj;
  }

  // Obtiene la hora sincronizada
  public long obtenerHoraSincronizada() {
    return System.currentTimeMillis() + desplazamientoReloj + offsetAplicado;
  }

  // El coordinador envía el offset para sincronizar
  public void aplicarOffset(long offset) {
    this.offsetAplicado = offset;
  }

  public long getDesplazamiento() {
    return desplazamientoReloj;
  }
}

// Clase Coordinador - Responsable de sincronizar todos los nodos
class Coordinador {
  private List<Nodo> nodos = new ArrayList<>();
  private long ultimaSincronizacion = 0;

  public Coordinador(List<Nodo> nodos) {
    this.nodos = nodos;
  }

  /**
   * Realiza la sincronización de Berkeley
   */
  public void sincronizarTodos() {
    System.out.println("\n========== ALGORITMO DE BERKELEY ==========");

    // PASO 1: Recopilar horas de todos los nodos
    System.out.println("\n[COORDINADOR] PASO 1: Solicita horas a todos los nodos");
    System.out.println("==========================================");

    List<Long> horasRecolectadas = new ArrayList<>();
    for (Nodo nodo : nodos) {
      long horaLocal = nodo.obtenerHoraLocal();
      horasRecolectadas.add(horaLocal);
      System.out.println("  Nodo " + nodo.getId() + ": " + String.format("%12d", horaLocal) + " ms");
    }

    // PASO 2: Calcular la hora promedio
    System.out.println("\n[COORDINADOR] PASO 2: Calcular hora promedio");
    System.out.println("==========================================");

    long sumaHoras = 0;
    for (long hora : horasRecolectadas) {
      sumaHoras += hora;
    }
    long horaPromedio = sumaHoras / nodos.size();

    System.out.println("  Suma de todas las horas:    " + sumaHoras + " ms");
    System.out.println("  Cantidad de nodos:          " + nodos.size());
    System.out.println("  Hora promedio:              " + horaPromedio + " ms");

    // PASO 3: Calcular offsets de cada nodo
    System.out.println("\n[COORDINADOR] PASO 3: Calcular offset para cada nodo");
    System.out.println("==========================================");

    List<Long> offsets = new ArrayList<>();
    for (int i = 0; i < nodos.size(); i++) {
      long offset = horaPromedio - horasRecolectadas.get(i);
      offsets.add(offset);

      String accion = offset >= 0 ? "Adelantar" : "Atrasar";
      System.out.println("  Nodo " + nodos.get(i).getId() + ": offset = " +
          String.format("%+6d", offset) + " ms  (" + accion + ")");
    }

    // PASO 4: Enviar correcciones a cada nodo
    System.out.println("\n[COORDINADOR] PASO 4: Enviar correcciones a cada nodo");
    System.out.println("==========================================");

    for (int i = 0; i < nodos.size(); i++) {
      nodos.get(i).aplicarOffset(offsets.get(i));
      long horaCorregida = nodos.get(i).obtenerHoraSincronizada();
      System.out.println("  Nodo " + nodos.get(i).getId() + " aplica corrección: " +
          String.format("%+6d", offsets.get(i)) + " ms");
    }

    // PASO 5: Mostrar resultado
    System.out.println("\n[COORDINADOR] PASO 5: Verificar sincronización");
    System.out.println("==========================================");
    System.out.println("  Horas sincronizadas:");

    long minHora = Long.MAX_VALUE;
    long maxHora = Long.MIN_VALUE;

    for (Nodo nodo : nodos) {
      long horaSincronizada = nodo.obtenerHoraSincronizada();
      minHora = Math.min(minHora, horaSincronizada);
      maxHora = Math.max(maxHora, horaSincronizada);
      System.out.println("    Nodo " + nodo.getId() + ": " + String.format("%12d", horaSincronizada) + " ms");
    }

    System.out.println("==========================================");
    System.out.println("  Rango de variacion: " + (maxHora - minHora) + " ms");
    System.out.println("  Todos los nodos sincronizados correctamente\n");

    ultimaSincronizacion = System.currentTimeMillis();
  }
}

// Clase Principal - Demostración del algoritmo
public class Berkeley {
  public static void main(String[] args) throws InterruptedException {
    System.out.println("\n" + "═".repeat(60));
    System.out.println("    DEMOSTRACION: ALGORITMO DE BERKELEY");
    System.out.println("    Sincronización de Relojes Distribuidos");
    System.out.println("═".repeat(60));
    System.out.println("\nESCENARIO: 4 nodos con relojes desincronizados");
    System.out.println("  Nodo 0: Reloj adelantado 8000 ms");
    System.out.println("  Nodo 1: Reloj atrasado 3000 ms");
    System.out.println("  Nodo 2: Reloj adelantado 2000 ms");
    System.out.println("  Nodo 3: Reloj atrasado 7000 ms\n");

    // Crear nodos con diferentes desplazamientos
    List<Nodo> nodos = new ArrayList<>();
    nodos.add(new Nodo(0, 8000)); // Adelantado 8 segundos
    nodos.add(new Nodo(1, -3000)); // Atrasado 3 segundos
    nodos.add(new Nodo(2, 2000)); // Adelantado 2 segundos
    nodos.add(new Nodo(3, -7000)); // Atrasado 7 segundos

    // Mostrar estado antes de sincronización
    System.out.println("\nANTES DE SINCRONIZACIÓN (relojes desviados):");
    System.out.println("──────────────────────────────────────────────────────────");
    for (Nodo nodo : nodos) {
      long horaDesviada = nodo.obtenerHoraLocal();
      System.out.println("  Nodo " + nodo.getId() + ": " + String.format("%12d", horaDesviada) +
          " ms (desplazamiento: " + String.format("%+6d", nodo.getDesplazamiento()) + " ms)");
    }

    // Crear coordinador y realizar sincronización
    Coordinador coordinador = new Coordinador(nodos);

    // Realizar 2 sincronizaciones
    for (int sync = 1; sync <= 2; sync++) {
      System.out.println("\n\n" + "═".repeat(60));
      System.out.println("                 SINCRONIZACIÓN #" + sync);
      System.out.println("═".repeat(60));

      coordinador.sincronizarTodos();

      if (sync == 1) {
        System.out.println("Esperando 8 segundos para siguiente sincronización...\n");
        Thread.sleep(8000);
      }
    }
  }
}
