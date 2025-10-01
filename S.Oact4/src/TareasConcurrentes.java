// Importa todas las clases de Swing para la GUI (JFrame, JButton, etc.). 
import javax.swing.*; 
// Importa clases de AWT para gráficos, colores y layouts (Color, BorderLayout). 
import java.awt.*; 
// Importa la clase para manejar eventos de la GUI (clic de botón). 
import java.awt.event.ActionEvent; 
// Importa para obtener la hora actual del sistema. 
import java.time.LocalTime; 
// Importa para dar formato a la hora (ej: HH:mm:ss). 
import java.time.format.DateTimeFormatter; 
// Importa la clase para crear servicios de ejecución (ExecutorService). 
import java.util.concurrent.Executors; 
// Importa la interfaz para programar tareas periódicas. 
import java.util.concurrent.ScheduledExecutorService; 
// Importa la enumeración para especificar unidades de tiempo (segundos, minutos). 
import java.util.concurrent.TimeUnit; 

public class TareasConcurrentes extends JFrame { // Declara la clase principal que extiende JFrame (la ventana principal).

    // --- Componentes de la Interfaz ---
    public final JButton btnStart; // Botón para iniciar las tareas.
    public final JTextArea outputAreaTime; // Área de texto para la salida del Hilo 1 (Hora).
    public final JTextArea outputAreaWorking; // Área de texto para la salida del Hilo 2 (Trabajando).
    public final JLabel statusLabel; // Etiqueta para mostrar el estado del programa.

    // --- Lógica de Hilos ---
    public ScheduledExecutorService scheduler; // Objeto para gestionar y programar los hilos de trabajo.
   public final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // Formato para la hora.

    public TareasConcurrentes() { // Constructor de la clase.
        super("Concurrent Tasks Controller (1-Minute Limit)"); // Establece el título de la ventana.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura el cierre de la ventana para terminar la aplicación.
        setLayout(new BorderLayout(10, 10)); // Usa BorderLayout para organizar la ventana principal (espacio de 10px).
        getContentPane().setBackground(new Color(255, 192, 203)); // ⬅️ **Añade el color de fondo Rosa (Pink)** 🩷

        // 1. Inicializar Componentes
        btnStart = new JButton("▶️ Iniciar Hilos"); // Inicializa el botón de inicio.
        outputAreaTime = createOutputArea(); // Crea y asigna el área de texto para la hora.
        outputAreaWorking = createOutputArea(); // Crea y asigna el área de texto para "Trabajando".
        statusLabel = new JLabel("Estado: Detenido", SwingConstants.CENTER); // Inicializa la etiqueta de estado centrada.
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14)); // Establece una fuente más grande y negrita para el estado.

        // 2. Configurar Layout de Paneles

        // Panel de control (botones)
        JPanel controlPanel = new JPanel(new FlowLayout()); // Crea un panel para los botones con FlowLayout (centrado).
        controlPanel.add(btnStart); // Agrega el botón de inicio al panel de control.
        controlPanel.setBackground(new Color(255, 192, 203)); // Configura el fondo del panel de control a Rosa.


        // Panel de salidas (áreas de texto)
        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // Crea un panel con 1 fila, 2 columnas, y 10px de espacio.
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Añade un borde interior (padding).
        outputPanel.setBackground(new Color(255, 192, 203)); // Configura el fondo del panel de salida a Rosa.
        outputPanel.add(createLabeledPanel("Hilo 1 (Hora - 5s):", new JScrollPane(outputAreaTime))); // Agrega el área de hora con su etiqueta y scroll.
        outputPanel.add(createLabeledPanel("Hilo 2 (Texto - 1s):", new JScrollPane(outputAreaWorking))); // Agrega el área de texto con su etiqueta y scroll.

        // 3. Añadir Componentes a la Ventana Principal
        add(controlPanel, BorderLayout.NORTH); // Coloca el panel de control en la parte superior (NORTH).
        add(outputPanel, BorderLayout.CENTER); // Coloca el panel de salidas en el centro (CENTER).
        add(statusLabel, BorderLayout.SOUTH); // Coloca la etiqueta de estado en la parte inferior (SOUTH).

        // Configura el manejador de eventos para el botón de inicio.
        btnStart.addActionListener(this::startTasks);

        pack(); // Ajusta el tamaño de la ventana para que se adapte a los componentes.
        setLocationRelativeTo(null); // Centra la ventana en la pantalla.
    }

    // Método auxiliar para crear áreas de texto uniformes
    public JTextArea createOutputArea() {
        JTextArea area = new JTextArea(15, 20); // Crea un JTextArea con 15 filas y 20 columnas.
        area.setEditable(false); // Hace que el área de texto sea de solo lectura.
        area.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Usa una fuente de ancho fijo para mejor legibilidad.
        return area; // Devuelve el objeto JTextArea creado.
    }

    // Método auxiliar para crear paneles con etiquetas (para las áreas de texto)
    public JPanel createLabeledPanel(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout()); // Crea un panel con BorderLayout.
        panel.setBorder(BorderFactory.createTitledBorder(title)); // Le pone un título (etiqueta) al borde del panel.
        panel.add(component, BorderLayout.CENTER); // Agrega el componente (JScrollPane que contiene el JTextArea) al centro.
        panel.setBackground(new Color(255, 192, 203)); // Configura el fondo del panel con título a Rosa.
        return panel; // Devuelve el panel etiquetado.
    }

    // --- Lógica de Inicio de Hilos ---
    public void startTasks(ActionEvent e) { // Método llamado cuando se presiona el botón Iniciar.
        // Detener cualquier scheduler anterior por seguridad
        if (scheduler != null && !scheduler.isShutdown()) { // Verifica si ya hay un scheduler activo.
            scheduler.shutdownNow(); // Lo detiene forzosamente para empezar de nuevo.
        }

        // 1. Configurar el nuevo scheduler con 2 hilos
        scheduler = Executors.newScheduledThreadPool(2); // Crea un nuevo scheduler capaz de ejecutar 2 tareas concurrentemente.

        // 2. Programar el Hilo 1 (Hora cada 5s)
        Runnable taskTime = () -> { // Define la tarea (Runnable) para el Hilo 1.
            String time = LocalTime.now().format(timeFormatter); // Obtiene la hora y la formatea.
            // ¡ATENCIÓN! Usar SwingUtilities.invokeLater para actualizar la GUI
            SwingUtilities.invokeLater(() -> { // Envía la actualización de la GUI al Event Dispatch Thread (EDT).
                outputAreaTime.append("⏰ " + time + "\n"); // Añade la hora al área de texto.
                // Auto-scroll al final
                outputAreaTime.setCaretPosition(outputAreaTime.getDocument().getLength()); // Fuerza el scroll al final.
            });
        };
        // Programa la tarea de la hora: comienza de inmediato (0), repite cada 5 segundos.
        scheduler.scheduleAtFixedRate(taskTime, 0, 5, TimeUnit.SECONDS);

        // 3. Programar el Hilo 2 (Texto "Trabajando" cada 1s)
        Runnable taskWorking = () -> { // Define la tarea (Runnable) para el Hilo 2.
            // ¡ATENCIÓN! Usar SwingUtilities.invokeLater para actualizar la GUI
            SwingUtilities.invokeLater(() -> { // Envía la actualización de la GUI al EDT.
                outputAreaWorking.append("⚙️ Trabajando...\n"); // Añade el texto al área de trabajo.
                // Auto-scroll al final
                outputAreaWorking.setCaretPosition(outputAreaWorking.getDocument().getLength()); // Fuerza el scroll al final.
            });
        };
        // Programa la tarea de "Trabajando": comienza de inmediato (0), repite cada 1 segundo.
        scheduler.scheduleAtFixedRate(taskWorking, 0, 1, TimeUnit.SECONDS);


        // 4. Programar la Tarea de Auto-Detención (límite de 1 minuto)
        // Programa una única ejecución para detener las tareas después de 60 segundos.
        scheduler.schedule(this::stopTasks, 60, TimeUnit.SECONDS);


        // 5. Actualizar la Interfaz
        btnStart.setEnabled(false); // Deshabilita el botón de inicio para evitar doble clic.
        outputAreaTime.setText(""); // Limpia el área de hora.
        outputAreaWorking.setText(""); // Limpia el área de trabajo.
        statusLabel.setText("Estado: Ejecutando... (Auto-detención en 60s)"); // Actualiza el estado.
    }

    // --- Lógica de Detención de Hilos ---
    public void stopTasks() { // Método para detener la ejecución de las tareas.
        if (scheduler != null) { // Verifica si el scheduler existe.
            scheduler.shutdownNow(); // Detiene todos los hilos en ejecución.
        }

        // Se usa invokeLater para garantizar que las actualizaciones de la GUI sean seguras,
        // ya que este método es llamado por un hilo del scheduler después de 60s.
        SwingUtilities.invokeLater(() -> {
            btnStart.setEnabled(true); // Vuelve a habilitar el botón de inicio.
            statusLabel.setText("Estado: Detenido. Ejecución completada."); // Actualiza el estado.
        });
    }


    public static void main(String[] args) { // Método principal de la aplicación.
        // Ejecutar la aplicación en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(() -> { // Asegura que la construcción de la GUI se haga en el hilo seguro (EDT).
            new TareasConcurrentes().setVisible(true); // Crea y muestra la ventana.
        });
    }
}