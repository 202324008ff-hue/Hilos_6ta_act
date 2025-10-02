// Importa todas las clases de Swing para la GUI (JFrame, JButton, etc.).
import javax.swing.*;
// Importa clases de AWT para gráficos y layouts (BorderLayout, Font, Color).
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

public class TareasConcurrentes extends JFrame {

    // --- Componentes de la Interfaz ---
    private final JButton btnStart;
    private final JTextArea outputAreaTime;
    private final JTextArea outputAreaWorking;
    private final JLabel statusLabel;

    // --- Lógica de Hilos ---
    private ScheduledExecutorService scheduler;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Color rosa claro. Lo definí para mantener el tema visual solicitado.
    private static final Color PINK_LIGHT = new Color(255, 223, 230);

    public TareasConcurrentes() {
        // Establecí el título de la ventana y configuré el cierre de la aplicación.
        super("Concurrent Tasks Controller (Ultra Simple)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Elegí el BorderLayout para una disposición clara de Norte, Centro y Sur.
        setLayout(new BorderLayout(10, 10)); // Usé 10px de espaciado.
        getContentPane().setBackground(PINK_LIGHT); // Apliqué el color de fondo rosa claro.

        // 1. Inicializar Componentes
        // Creé el botón de inicio con la indicación del límite de tiempo.
        btnStart = new JButton("Iniciar Hilos (60s)");
        // Inicialicé la etiqueta de estado, la centré y le di un formato de fuente destacado.
        statusLabel = new JLabel("Estado: Detenido", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        // 2. Crear las áreas de salida
        outputAreaTime = createOutputArea();
        outputAreaWorking = createOutputArea();

        // Panel Central: Organicé las dos salidas una al lado de la otra (1 fila, 2 columnas).
        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Añadí padding.
        outputPanel.setBackground(PINK_LIGHT);

        // Agregué las áreas de texto al panel central. Usé createLabeledScrollPane para el etiquetado en el borde.
        outputPanel.add(createLabeledScrollPane("Hilo 1 (Hora - 5s)", outputAreaTime));
        outputPanel.add(createLabeledScrollPane("Hilo 2 (Texto - 1s)", outputAreaWorking));


        // 3. Añadir Componentes a la Ventana Principal usando BorderLayout
        // Zona NORTE: Contuve el botón en un JPanel con FlowLayout para centrarlo.
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        northPanel.setBackground(PINK_LIGHT);
        northPanel.add(btnStart);
        add(northPanel, BorderLayout.NORTH);

        // Zona CENTRO: Coloqué el panel de las áreas de texto.
        add(outputPanel, BorderLayout.CENTER);

        // Zona SUR: Asigné la etiqueta de estado.
        add(statusLabel, BorderLayout.SOUTH);

        // Configuré el manejador de eventos del botón para que llamara al método startTasks.
        btnStart.addActionListener(this::startTasks);

        // Ajusté el tamaño de la ventana a sus contenidos y la centré en la pantalla.
        pack();
        setLocationRelativeTo(null);
    }

    // Método auxiliar para crear áreas de texto uniformes.
    private JTextArea createOutputArea() {
        // Las definí como 10 filas y 25 columnas para un diseño compacto.
        JTextArea area = new JTextArea(10, 25);
        area.setEditable(false); // Las hice de solo lectura.
        area.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Fuente de ancho fijo para mejor legibilidad.
        return area;
    }

    // Método auxiliar clave: Creé un JScrollPane con un borde etiquetado (TitledBorder).
    private JComponent createLabeledScrollPane(String title, JTextArea area) {
        JScrollPane scrollPane = new JScrollPane(area);
        // Usé un JPanel auxiliar para aplicar el borde con título al JScrollPane.
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(PINK_LIGHT);
        return panel;
    }


    // --- Lógica de Hilos ---
    // Método llamado al presionar el botón de inicio.
    private void startTasks(ActionEvent e) {
        // Me aseguré de detener y limpiar cualquier scheduler anterior antes de empezar.
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        // Creé un nuevo pool de hilos programados, capaz de manejar al menos 2 tareas.
        scheduler = Executors.newScheduledThreadPool(2);

        // Hilo 1 (Hora cada 5s)
        Runnable taskTime = () -> {
            String time = LocalTime.now().format(timeFormatter);
            // USÉ SwingUtilities.invokeLater() para garantizar que la actualización de la GUI fuera segura (EDT).
            SwingUtilities.invokeLater(() -> {
                outputAreaTime.append( time + "\n");
                // Forcé el scroll al final de la lista.
                outputAreaTime.setCaretPosition(outputAreaTime.getDocument().getLength());
            });
        };
        // Programé la tarea: inicia inmediatamente (0) y se repite cada 5 segundos.
        scheduler.scheduleAtFixedRate(taskTime, 0, 5, TimeUnit.SECONDS);

        // Hilo 2 (Texto "Trabajando" cada 1s)
        Runnable taskWorking = () -> {
            // USÉ SwingUtilities.invokeLater() de nuevo por seguridad en la actualización de la GUI.
            SwingUtilities.invokeLater(() -> {
                outputAreaWorking.append("️ Trabajando...\n");
                outputAreaWorking.setCaretPosition(outputAreaWorking.getDocument().getLength());
            });
        };
        // Programé esta tarea para que se repitiera cada 1 segundo (más rápido que la hora).
        scheduler.scheduleAtFixedRate(taskWorking, 0, 1, TimeUnit.SECONDS);

        // Programé la Tarea de Auto-Detención (límite de 60 segundos)
        // Programé una única llamada al método stopTasks después de 60 segundos.
        scheduler.schedule(this::stopTasks, 60, TimeUnit.SECONDS);

        // Actualicé la Interfaz: deshabilité el botón, limpié las áreas y cambié el estado.
        btnStart.setEnabled(false);
        outputAreaTime.setText("");
        outputAreaWorking.setText("");
        statusLabel.setText("Estado: En proceso... ");
    }

    // Método para detener la ejecución de las tareas.
    private void stopTasks() {
        // Verifiqué si el scheduler existía y lo detuve de forma inmediata.
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        // Como este método es llamado por un hilo de trabajo, usé invokeLater para actualizar la GUI.
        SwingUtilities.invokeLater(() -> {
            btnStart.setEnabled(true); // Re-habilité el botón de inicio.
            statusLabel.setText("Estado: Detenido. Ejecución completada."); // Actualicé el estado final.
        });
    }


    // Método principal: Aseguré que la construcción de la GUI se hiciera en el Event Dispatch Thread (EDT).
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TareasConcurrentes().setVisible(true); // Creé y mostré la ventana.
        });
    }
}