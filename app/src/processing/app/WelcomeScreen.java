package processing.app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

public class WelcomeScreen {
  
  private static final EmptyBorder QUICK_ACTION_PANEL_PADDING = new EmptyBorder(0, 0, 0, 20);
  private static final String LABEL_FONT_NAME = "SansSerif";
  private static final Dimension BUTTON_MAXIMUM_SIZE = new Dimension(180, 38);
  private static final Color COLOR_TEAL_3 = new Color(0,129,132);
  private static final Color COLOR_TRANSPARENT = new Color(0,0,0,0);
  private static final Dimension WINDOW_MINIMUM_SIZE = new Dimension(800, 600);
  private static final EmptyBorder WINDOW_BORDER = new EmptyBorder(20, 72, 36, 72);
  private static final Dimension WINDOW_SIZE = new Dimension(950, 740);
  private static final Color BACKGROUND_COLOR = Color.WHITE;
  private static final int SPACE_BETWEEN_IMAGES = 10;
  private static final int SKETCH_TABLE_ROW_HEIGHT = 55;
  private static final Color COLOR_BASE_GRAY = new Color(244,244,244);
  private static final int MAX_RECENT_SKETCHES = 8;
  private static final boolean RESIZABLE = true;

  private int hoveredRow = -1;
  private static GraphicsConfiguration graphicsConfig;
  private SketchManager sketchManager;
  private JFrame welcomeScreenFrame;

  private ArrayList<File> recentSketchFiles = new ArrayList<File>();

  public WelcomeScreen(SketchManager sketchManager) {
    this.sketchManager = sketchManager;
    
    for(File file : sketchManager.getRecentSketchFiles(MAX_RECENT_SKETCHES)) {
      recentSketchFiles.add(file);
    }
  }

  public void show() {
    if(welcomeScreenFrame != null) {
      welcomeScreenFrame.toFront();
      return;
    }
    
    String version = BaseNoGui.VERSION_NAME_LONG;
    welcomeScreenFrame = new JFrame(graphicsConfig);
    welcomeScreenFrame.setTitle("Arduino" + version);
    welcomeScreenFrame.setSize(WINDOW_SIZE);
    welcomeScreenFrame.setMinimumSize(WINDOW_MINIMUM_SIZE);
    welcomeScreenFrame.setLocationRelativeTo(null);
    welcomeScreenFrame.setResizable(RESIZABLE);
    welcomeScreenFrame.toFront();
    
    JPanel contentPanel = new JPanel();    
    contentPanel.setLayout(new GridBagLayout());
    contentPanel.setBorder(WINDOW_BORDER);
    contentPanel.setBackground(BACKGROUND_COLOR);
    welcomeScreenFrame.setContentPane(contentPanel);
    
    addRecentSketchTable(contentPanel);
    addQuickActions(contentPanel);
    addNewsSection(contentPanel);
    
    welcomeScreenFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowEvent) {
         welcomeScreenFrame = null;
      }
  });
    
    welcomeScreenFrame.setVisible(true);
  }

  void close() {
    this.welcomeScreenFrame.dispose();
    this.welcomeScreenFrame = null;
  }

  void addRecentSketchTable(JComponent frame) {

    String[] columns = new String[] { "Name", "Last modified" };
    Object[][] data = new Object[MAX_RECENT_SKETCHES][columns.length];

    int i = 0;

    for (File recentSketch : sketchManager.getRecentSketchFiles(MAX_RECENT_SKETCHES)) {
      String sketchName = recentSketch.getParentFile().getName();
      SimpleDateFormat dateFormat = new SimpleDateFormat(
          "MMM dd yyyy HH:mm:ss");
      String modifiedTimestamp = dateFormat.format(recentSketch.lastModified());

      data[i][0] = sketchName;
      data[i][1] = modifiedTimestamp;
      ++i;
    }

    JTable table = new JTable(data, columns);
    table.setDefaultEditor(Object.class, null);
    table.setBorder(new EmptyBorder(0,0,0,0));
    table.setRowHeight(SKETCH_TABLE_ROW_HEIGHT);
    table.setSelectionBackground(COLOR_BASE_GRAY);
    table.setSelectionForeground(Color.black);
    table.setShowGrid(false);
    addTableListeners(table);
    
    JPanel recentSketchesContainer = new JPanel();
    recentSketchesContainer.setLayout(new BoxLayout(recentSketchesContainer, BoxLayout.Y_AXIS));
    recentSketchesContainer.setBackground(COLOR_TRANSPARENT);
    recentSketchesContainer.add(createSpacer(0, 36));
    
    JLabel label = createTitleLabel("Recent Sketches");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    recentSketchesContainer.add(label);
    
    recentSketchesContainer.add(createSpacer(0, 20));
    JScrollPane tableScrollPane = new JScrollPane(table);
    tableScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    recentSketchesContainer.add(tableScrollPane);
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridwidth = 2;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridx = 0;
    constraints.gridy = 1;
    
    frame.add(recentSketchesContainer, constraints);
  }

  void addTableListeners(JTable table) {
    table.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseMoved(MouseEvent e) {
        hoveredRow = table.rowAtPoint(e.getPoint());        
        table.setRowSelectionInterval(hoveredRow, hoveredRow);
        table.repaint();
        
        final int x = e.getX();
        final int y = e.getY();

        final Rectangle cellBounds = table.getBounds();
        if (cellBounds != null && cellBounds.contains(x, y)) {
          table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
          table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        
        if(hoveredRow == -1) table.clearSelection();
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        hoveredRow = -1;
        table.repaint();
      }
    });
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
          int row = table.rowAtPoint(evt.getPoint());
          int col = table.columnAtPoint(evt.getPoint());
          if (row >= 0 && col >= 0) {
            File selectedSketch = recentSketchFiles.get(row);
            try {
              close();
              Base.INSTANCE.handleOpen(selectedSketch);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
      }
  });
  }
  
  void addQuickActions(JComponent frame) {
    JPanel quickActionsContainer = new JPanel();
    quickActionsContainer.setLayout(new BoxLayout(quickActionsContainer, BoxLayout.Y_AXIS));
    quickActionsContainer.setBackground(BACKGROUND_COLOR);
    quickActionsContainer.setBorder(QUICK_ACTION_PANEL_PADDING);
    
    RoundedButton newSketchButton = createButton("NEW SKETCH");
    newSketchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    newSketchButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          close();
          Base.INSTANCE.handleNew();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    
    RoundedButton openSketchButton = createButton("OPEN SKETCH...");
    openSketchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    openSketchButton.addActionListener(new ActionListener() {
      
      @Override
      public void actionPerformed(ActionEvent event) {
        close();
        try {
          Base.INSTANCE.handleOpenPrompt();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    constraints.weighty = 0;
    constraints.gridx = 0;
    constraints.gridy = 0;
    
    JLabel label = createTitleLabel("Quick Actions");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    quickActionsContainer.add(label);
    quickActionsContainer.add(createSpacer(0, 20));
    quickActionsContainer.add(newSketchButton);
    quickActionsContainer.add(createSpacer(0, 20));
    quickActionsContainer.add(openSketchButton);
    frame.add(quickActionsContainer, constraints);
  }

  void addNewsSection(JComponent frame) {
    JPanel newsContainer = new JPanel();
    newsContainer.setLayout(new BoxLayout(newsContainer, BoxLayout.Y_AXIS));
    newsContainer.setBackground(COLOR_TRANSPARENT);
    
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.FIRST_LINE_END;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.gridx = 1;
    constraints.gridy = 0;
    
    JPanel imageContainer = new JPanel();
    imageContainer.setBackground(COLOR_TRANSPARENT);
    imageContainer.setLayout(new BoxLayout(imageContainer, BoxLayout.X_AXIS));
    imageContainer.add(imageFromURL("https://content.arduino.cc/assets/Arduino-IDE-Marketing-1.png", "https://arduino.cc"));
    imageContainer.add(createSpacer(SPACE_BETWEEN_IMAGES, 0));
    imageContainer.add(imageFromURL("https://content.arduino.cc/assets/Arduino-IDE-Marketing-2.png", "https://arduino.cc"));
    imageContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    JLabel label = createTitleLabel("What’s new at Arduino?");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    
    newsContainer.add(label);
    newsContainer.add(Box.createRigidArea(new Dimension(0, 20)));
    newsContainer.add(imageContainer);    
    frame.add(newsContainer, constraints);
  }
  

  /**
   * Helper Methods
   */
  
  private RoundedButton createButton(String title) {
    RoundedButton button = new RoundedButton(title);
    button.setMaximumSize(BUTTON_MAXIMUM_SIZE);
    button.setForeground(Color.WHITE);
    button.setBackground(COLOR_TEAL_3);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  private Component createSpacer(int spaceX, int spaceY ) {
    return Box.createRigidArea(new Dimension(spaceX, spaceY));
  }
  
  
  private JLabel createTitleLabel(String title) {
    JLabel label = new JLabel(title);
    Font labelFont = new Font(LABEL_FONT_NAME, Font.PLAIN, 20);  
    label.setFont(labelFont);
    return label;
  }

  JComponent imageFromURL(String path, String link) {
    BufferedImage image = null;
    try {
      URL url = new URL(path);
      image = ImageIO.read(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    JLabel label = new JLabel(new ImageIcon(image));
    label.addMouseListener(new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent event){
          if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
              Desktop.getDesktop().browse(new URI(link));
            } catch (IOException | URISyntaxException e) {
              e.printStackTrace();
            }
        }
        }
    });
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return label;
  }

}


