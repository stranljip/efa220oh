/**
 * Title:        efa - elektronisches Fahrtenbuch für Ruderer
 * Copyright:    Copyright (c) 2001-2011 by Nicolas Michael
 * Website:      http://efa.nmichael.de/
 * License:      GNU General Public License v2
 *
 * @author Nicolas Michael
 * @version 2
 */

package de.nmichael.efa.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import de.nmichael.efa.Daten;
import de.nmichael.efa.core.CrontabThread;
import de.nmichael.efa.core.config.AdminRecord;
import de.nmichael.efa.core.config.Admins;
import de.nmichael.efa.core.items.IItemListener;
import de.nmichael.efa.core.items.IItemType;
import de.nmichael.efa.core.items.ItemTypeBoatstatusList;
import de.nmichael.efa.core.items.ItemTypeBoatstatusList.SortingBy;
import de.nmichael.efa.core.items.ItemTypeBoolean;
import de.nmichael.efa.core.items.ItemTypeConfigButton;
import de.nmichael.efa.core.items.ItemTypeList;
import de.nmichael.efa.data.BoatDamageRecord;
import de.nmichael.efa.data.BoatDamages;
import de.nmichael.efa.data.BoatRecord;
import de.nmichael.efa.data.BoatReservationRecord;
import de.nmichael.efa.data.BoatReservations;
import de.nmichael.efa.data.BoatStatus;
import de.nmichael.efa.data.BoatStatusRecord;
import de.nmichael.efa.data.Clubwork;
import de.nmichael.efa.data.Logbook;
import de.nmichael.efa.data.LogbookRecord;
import de.nmichael.efa.data.MessageRecord;
import de.nmichael.efa.data.Persons;
import de.nmichael.efa.data.Project;
import de.nmichael.efa.data.storage.DataRecord;
import de.nmichael.efa.data.storage.IDataAccess;
import de.nmichael.efa.data.types.DataTypeDate;
import de.nmichael.efa.data.types.DataTypeTime;
import de.nmichael.efa.gui.dataedit.BoatDamageEditDialog;
import de.nmichael.efa.gui.dataedit.BoatReservationListDialog;
import de.nmichael.efa.gui.dataedit.ClubworkListDialog;
import de.nmichael.efa.gui.dataedit.MessageEditDialog;
import de.nmichael.efa.gui.dataedit.StatisticsListDialog;
import de.nmichael.efa.gui.util.EfaBoathouseBackgroundTask;
import de.nmichael.efa.gui.util.EfaMenuButton;
import de.nmichael.efa.gui.util.EfaMouseListener;
import de.nmichael.efa.gui.widgets.ClockMiniWidget;
import de.nmichael.efa.gui.widgets.IWidget;
import de.nmichael.efa.gui.widgets.NewsMiniWidget;
import de.nmichael.efa.gui.widgets.Widget;
import de.nmichael.efa.util.Dialog;
import de.nmichael.efa.util.EfaUtil;
import de.nmichael.efa.util.Help;
import de.nmichael.efa.util.International;
import de.nmichael.efa.util.LogString;
import de.nmichael.efa.util.Logger;
import de.nmichael.efa.util.Mnemonics;

public class EfaBoathouseFrame extends BaseFrame implements IItemListener {

  private static final String NEWLINE = "\n";

  private static final long serialVersionUID = 1L;

  public static EfaBoathouseFrame efaBoathouseFrame;

  public static final int EFA_EXIT_REASON_USER = 0;
  public static final int EFA_EXIT_REASON_TIME = 1;
  public static final int EFA_EXIT_REASON_IDLE = 2;
  public static final int EFA_EXIT_REASON_OOME = 3;
  public static final int EFA_EXIT_REASON_AUTORESTART = 4;
  public static final int EFA_EXIT_REASON_ONLINEUPDATE = 5;

  public static final int ACTIONID_STARTSESSION = 1;
  public static final int ACTIONID_FINISHSESSION = 2;
  public static final int ACTIONID_LATEENTRY = 3;
  public static final int ACTIONID_STARTSESSIONCORRECT = 4;
  public static final int ACTIONID_ABORTSESSION = 5;
  public static final int ACTIONID_BOATRESERVATIONS = 6;
  public static final int ACTIONID_BOATDAMAGES = 7;
  public static final int ACTIONID_BOATINFOS = 8;
  public static final int ACTIONID_LASTBOATUSAGE = 9;

  String KEYACTION_shiftF1;
  String KEYACTION_F2;
  String KEYACTION_F3;
  String KEYACTION_F4;
  String KEYACTION_shiftF4;
  String KEYACTION_F5;
  String KEYACTION_F6;
  String KEYACTION_F7;
  String KEYACTION_F8;
  String KEYACTION_F9;
  String KEYACTION_F10;
  String KEYACTION_altF10;
  String KEYACTION_F11;
  String KEYACTION_altF11;
  String KEYACTION_F12;
  String KEYACTION_altX;

  // Boat List GUI Items
  JPanel boatsAvailablePanel;
  JPanel boatsNotAvailablePanel;
  ItemTypeBoatstatusList boatsAvailableList; // booteVerfuegbar
  ItemTypeBoatstatusList personsAvailableList; // booteVerfuegbar
  ItemTypeBoatstatusList boatsOnTheWaterList; // booteAufFahrt
  ItemTypeBoatstatusList boatsNotAvailableList; // booteNichtVerfuegbar
  ButtonGroup toggleAvailableBoats = new ButtonGroup();
  JRadioButton toggleAvailableBoatsToBoats = new JRadioButton();
  JRadioButton toggleAvailableBoatsToPersons = new JRadioButton();
  JRadioButton toggleAvailableBoatsToDescriptionOrt = new JRadioButton(); // Tor 1
  JRadioButton toggleAvailableBoatsToBoatType = new JRadioButton(); // Wildwasser
  JRadioButton toggleAvailableBoatsToBoatNameAffix = new JRadioButton(); // (PE, Rot)
  JRadioButton toggleAvailableBoatsToOwner = new JRadioButton(); // Eigentümer Overfreunde
  JRadioButton toggleAvailableBoatsToPaddelArt = new JRadioButton(); // Riggering
  JRadioButton toggleAvailableBoatsToSteuermann = new JRadioButton(); // Coxing

  // Center Panel GUI Items
  boolean toggleF12LangtextF12 = false;
  JLabel infoLabel = new JLabel();
  JLabel logoLabel = new JLabel();
  JButton startSessionButton = new JButton();
  JButton finishSessionButton = new JButton();
  JButton lateEntryButton = new JButton();
  JButton abortSessionButton = new JButton();
  JButton boatReservationButton = new JButton();
  JButton messageToAdminButton = new JButton();
  JButton adminButton = new JButton();
  JButton specialButton = new JButton();
  JButton efaButton = new JButton();
  JButton helpButton = new JButton();
  JButton showLogbookButton = new JButton();
  JButton statisticsButton = new JButton();
  JButton clubworkButton = new JButton();

  // Widgets
  ClockMiniWidget clock;
  NewsMiniWidget news;
  Vector<IWidget> widgets;
  JPanel widgetTopPanel = new JPanel();
  JPanel widgetBottomPanel = new JPanel();
  JPanel widgetLeftPanel = new JPanel();
  JPanel widgetRightPanel = new JPanel();
  JPanel widgetCenterPanel = new JPanel();

  // South Panel GUI Items
  JLabel statusLabel = new JLabel();

  // Base GUI Items
  JPanel westPanel = new JPanel();
  JPanel eastPanel = new JPanel();
  JPanel centerPanel = new JPanel();
  JPanel northPanel = new JPanel();
  JPanel southPanel = new JPanel();

  // Window GUI Items
  JLabel titleLabel = new JLabel();

  // Data
  EfaBoathouseBackgroundTask efaBoathouseBackgroundTask;
  CrontabThread crontabThread;
  EfaBaseFrame efaBaseFrame;
  Logbook logbook;
  Clubwork clubwork;
  BoatStatus boatStatus;
  volatile long lastUserInteraction = System.currentTimeMillis();
  volatile boolean inUpdateBoatList = false;
  byte[] largeChunkOfMemory = new byte[1024 * 1024];
  boolean isLocked = false;

  public EfaBoathouseFrame() {
    super(null, Daten.EFA_LONGNAME);
    EfaBoathouseFrame.efaBoathouseFrame = this;
  }

  @Override
  public void _keyAction(ActionEvent evt) {
    alive();
    if (evt == null || evt.getActionCommand() == null) {
      return;
    }

    if (evt.getActionCommand().equals(KEYACTION_ESCAPE)) {
      // do nothing (and don't invoke super._keyAction(evt)!)
      return;
    }
    if (evt.getActionCommand().equals(KEYACTION_F2)) {
      actionStartSession(null);
    }
    if (evt.getActionCommand().equals(KEYACTION_F3)) {
      actionFinishSession();
    }
    if (evt.getActionCommand().equals(KEYACTION_F4)) {
      actionAbortSession();
    }
    if (evt.getActionCommand().equals(KEYACTION_F5)) {
      actionLateEntry();
    }
    if (evt.getActionCommand().equals(KEYACTION_F6)) {
      actionBoatReservations();
    }
    if (evt.getActionCommand().equals(KEYACTION_F7)) {
      actionShowLogbook();
    }
    if (evt.getActionCommand().equals(KEYACTION_F8)) {
      actionStatistics();
    }
    if (evt.getActionCommand().equals(KEYACTION_F9)) {
      actionMessageToAdmin();
    }
    if (evt.getActionCommand().equals(KEYACTION_altF10)) {
      actionAdminMode();
    }
    if (evt.getActionCommand().equals(KEYACTION_altF11)) {
      actionSpecial();
    }
    if (evt.getActionCommand().equals(KEYACTION_F10)) {
      if (toggleAvailableBoatsToBoats.isSelected()) {
        boatsAvailableList.requestFocus();
      } else {
        personsAvailableList.requestFocus();
      }
    }
    if (evt.getActionCommand().equals(KEYACTION_F11)) {
      boatsOnTheWaterList.requestFocus();
    }
    if (evt.getActionCommand().equals(KEYACTION_F12)) {
      boatsNotAvailableList.requestFocus();
      // mit F12 Text ganz ausschalten. Schalter Daten.efaConfig.
      toggleF12LangtextF12=!toggleF12LangtextF12;
    }
    if (evt.getActionCommand().equals(KEYACTION_shiftF1)) {
      EfaUtil.gc(); // Garbage Collection
    }
    if (evt.getActionCommand().equals(KEYACTION_altX)) {
      cancel();
    }
    if (evt.getActionCommand().equals(KEYACTION_shiftF4)) {
      cancel();
    }

    super._keyAction(evt);
  }

  public boolean isToggleF12LangtextF12() {
    return toggleF12LangtextF12;
  }

  @Override
  public void keyAction(ActionEvent evt) {
    _keyAction(evt);
  }

  @Override
  protected void iniDialog() throws Exception {
    iniGuiBase();
    iniGuiMain();
    iniApplication();
    iniGuiRemaining();
    prepareEfaBaseFrame();
  }

  private void iniGuiBase() {
    URL iconResource = EfaBaseFrame.class.getResource("/de/nmichael/efa/img/efa_icon.png");
    Image iconImage = Toolkit.getDefaultToolkit().createImage(iconResource);
    setIconImage(iconImage);

    mainPanel.setLayout(new BorderLayout());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setResizable(false);

    this.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }

      @Override
      public void windowDeactivated(WindowEvent e) {
        this_windowDeactivated(e);
      }

      @Override
      public void windowActivated(WindowEvent e) {
        this_windowActivated(e);
      }

      @Override
      public void windowIconified(WindowEvent e) {
        this_windowIconified(e);
      }
    });

    // Key Actions
    KEYACTION_shiftF1 = addKeyAction("shift F1");
    KEYACTION_F2 = addKeyAction("F2");
    KEYACTION_F3 = addKeyAction("F3");
    KEYACTION_F4 = addKeyAction("F4");
    KEYACTION_shiftF4 = addKeyAction("shift F4");
    KEYACTION_F5 = addKeyAction("F5");
    KEYACTION_F6 = addKeyAction("F6");
    KEYACTION_F7 = addKeyAction("F7");
    KEYACTION_F8 = addKeyAction("F8");
    KEYACTION_F9 = addKeyAction("F9");
    KEYACTION_F10 = addKeyAction("F10");
    KEYACTION_altF10 = addKeyAction("alt F10");
    KEYACTION_F11 = addKeyAction("F11");
    KEYACTION_altF11 = addKeyAction("alt F11");
    KEYACTION_F12 = addKeyAction("F12");
    KEYACTION_altX = addKeyAction("alt X");

    // Mouse Actions
    this.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        alive();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        alive();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        alive();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        alive();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        alive();
      }
    });
  }

  private void iniGuiMain() {
    iniGuiBoatLists();
    iniGuiCenterPanel();
    iniGuiNorthPanel();
    iniGuiSouthPanel();
    iniGuiPanels();
    updateGuiWidgets();
  }

  public void updateGuiElements() {
    updateGuiWidgets();
    updateGuiClock();
    updateGuiNews();
    updateGuiButtonText();
    updateTopInfoText("Willkommen bei EFA!");
  }

  public boolean resetSorting() {
    if (!toggleAvailableBoatsToPersons.isSelected() &&
        !toggleAvailableBoatsToBoats.isSelected()) {
      toggleAvailableBoatsToBoats.setSelected(true);
      toggleAvailableBoats_actionPerformed(null); // incl. updateBoatLists() alive()
      return true; // means updateBoatLists() has already been called
    }
    return false;
  }

  private void iniGuiPanels() {
    widgetTopPanel.setLayout(new BorderLayout());
    widgetBottomPanel.setLayout(new BorderLayout());
    widgetLeftPanel.setLayout(new BorderLayout());
    widgetRightPanel.setLayout(new BorderLayout());
    widgetCenterPanel.setLayout(new BorderLayout());

    northPanel.add(widgetTopPanel, BorderLayout.CENTER);
    southPanel.add(widgetBottomPanel, BorderLayout.CENTER);
    westPanel.add(widgetLeftPanel, BorderLayout.WEST);
    eastPanel.add(widgetRightPanel, BorderLayout.EAST);
    GridBagConstraints gridBagConstraints = new GridBagConstraints(
        1, 19,
        1, 1,
        0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(5, 10, 10, 10),
        0, 0);
    centerPanel.add(widgetCenterPanel, gridBagConstraints);

    // Color bg = new Color(0, 170, 0);
    // northPanel.setBackground(bg);
    // southPanel.setBackground(bg);
    // westPanel.setBackground(bg);
    // eastPanel.setBackground(bg);
    // centerPanel.setBackground(bg);

    mainPanel.add(westPanel, BorderLayout.WEST);
    mainPanel.add(eastPanel, BorderLayout.EAST);
    mainPanel.add(northPanel, BorderLayout.NORTH);
    mainPanel.add(southPanel, BorderLayout.SOUTH);
    mainPanel.add(centerPanel, BorderLayout.CENTER); // muss Center
  }

  private void iniApplication() {
    openProject((AdminRecord) null);
    if (Daten.project == null) {
      Logger.log(Logger.ERROR, Logger.MSG_ERR_NOPROJECTOPENED,
          International.getString("Kein Projekt geöffnet."));
    } else {
      openLogbook((AdminRecord) null);
      if (logbook == null) {
        Logger.log(Logger.ERROR, Logger.MSG_ERR_NOLOGBOOKOPENED,
            International.getString("Kein Fahrtenbuch geöffnet."));
      }
      openClubwork((AdminRecord) null);
    }

    updateBoatLists(true);

    EfaExitFrame.initExitFrame(this);

    // Speicher-Überwachung
    try {
      de.nmichael.efa.java15.Java15.setMemUsageListener(this,
          Daten.MIN_FREEMEM_COLLECTION_THRESHOLD);
    } catch (UnsupportedClassVersionError e) {
      EfaUtil.foo();
    } catch (NoClassDefFoundError e) {
      EfaUtil.foo();
    }

    // Background Task
    efaBoathouseBackgroundTask = new EfaBoathouseBackgroundTask(this);
    efaBoathouseBackgroundTask.start();

    // Crontab Task
    crontabThread = new CrontabThread();
    crontabThread.start();

    alive();
    Logger.log(Logger.INFO, Logger.MSG_EVT_EFAREADY,
        International.getString("BEREIT") + " ------------------------------------");
  }

  private void iniGuiRemaining() {
    // Fenster nicht verschiebbar
    if (Daten.efaConfig.getValueEfaDirekt_fensterNichtVerschiebbar()) {
      try {
        // must be called before any packing of the frame,
        // since packing makes the frame displayable!
        this.setUndecorated(true);

        Color bgColor = new Color(0, 0, 170);
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(new EmptyBorder(2, 2, 2, 2));

        JLabel efaLabel = new JLabel();
        efaLabel.setIcon(getIcon("efa_icon_small.png"));

        titleLabel.setText(Daten.EFA_LONGNAME);
        titleLabel.setForeground(Color.white);
        titleLabel.setFont(titleLabel.getFont().deriveFont(12f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        JButton closeButton = new JButton();
        closeButton.setIcon(getIcon("frame_close.png"));
        closeButton.setBackground(bgColor);
        closeButton.setForeground(Color.white);
        closeButton.setFont(closeButton.getFont().deriveFont(10f));
        closeButton.setBorder(null);
        closeButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            cancel();
          }
        });

        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new BorderLayout());
        menuBar.setBorder(new EmptyBorder(4, 10, 2, 10));
        menuBar.setBackground(bgColor);
        menuBar.setForeground(Color.white);
        menuBar.add(efaLabel, BorderLayout.WEST);
        menuBar.add(titleLabel, BorderLayout.CENTER);
        menuBar.add(closeButton, BorderLayout.EAST);
        menuBar.validate();
        this.setJMenuBar(menuBar);
      } catch (NoSuchMethodError e) {
        Logger.log(Logger.WARNING, Logger.MSG_WARN_JAVA_VERSION,
            "Only supported as of Java 1.4: " + e.toString());
      }
    }

    // Fenster immer im Vordergrund
    try {
      if (Daten.efaConfig.getValueEfaDirekt_immerImVordergrund()) {
        if (!de.nmichael.efa.java15.Java15.setAlwaysOnTop(this, true)) {
          // Logger.log(Logger.WARNING,"Fenstereigenschaft 'immer im Vordergrund'
          // wird erst ab Java 1.5 unterstützt.");
          // Hier muß keine Warnung mehr ausgegeben werden, da ab v1.6.0 die Funktionalität auch für
          // Java < 1.5
          // durch einen Check alle 60 Sekunden nachgebildet wird.
        }
      }
    } catch (UnsupportedClassVersionError e) {
      // Java 1.3 kommt mit der Java 1.5 Klasse nicht klar
      Logger.log(Logger.WARNING, Logger.MSG_WARN_JAVA_VERSION,
          "Only supported as of Java 5: " + e.toString());
    } catch (NoClassDefFoundError e) {
      Logger.log(Logger.WARNING, Logger.MSG_WARN_JAVA_VERSION,
          "Only supported as of Java 5: " + e.toString());
    }

    // Fenster maximiert
    if (Daten.efaConfig.getValueEfaDirekt_startMaximized()) {
      try {
        // this.setSize(Dialog.screenSize);
        this.setMinimumSize(Dialog.screenSize);
        Dimension newsize = this.getSize();

        // breite für Scrollpanes ist (Fensterbreite - 20) / 2.
        // int width = (int) ((newsize.getWidth() - this.startSessionButton.getSize().getWidth() -
        // 20) / 2);
        int width = (int) (newsize.getWidth() / 3);
        // die Höhe der Scrollpanes ist, da sie CENTER sind, irrelevant; nur für jScrollPane3
        // ist die Höhe ausschlaggebend.
        boatsAvailableList.setFieldSize(width, 400);
        personsAvailableList.setFieldSize(width, 400);
        boatsOnTheWaterList.setFieldSize(width, 200);
        boatsNotAvailableList.setFieldSize(width, 300); // (int) (newsize.getHeight() / 4));
        int height = (int) (20.0f * (Dialog.getFontSize() < 10 ? 12 : Dialog.getFontSize()) / Dialog
            .getDefaultFontSize());
        if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()) {
          toggleAvailableBoatsToBoats.setPreferredSize(new Dimension(width / 2, height));
          toggleAvailableBoatsToPersons.setPreferredSize(new Dimension(width / 2, height));
        }
        validate();
      } catch (Exception e) {
        Logger.logwarn(e);
      }
    }

    // Lock efa?
    if (Daten.efaConfig.getValueEfaDirekt_locked()) {
      // lock efa NOW
      lockEfa();
    }

    // Update Project Info
    updateProjectLogbookInfo();

    // note: packing must happen at the very end, since it makes the frame "displayable", which then
    // does not allow to change any window settings like setUndecorated()
    packFrame("iniGuiRemaining()");
    boatsAvailableList.requestFocus();
  }

  private void iniGuiBoatLists() {
    // Toggle between Boats and Persons
    Mnemonics.setButton(this, toggleAvailableBoatsToBoats,
        International.getStringWithMnemonic("Boote"));
    Mnemonics.setButton(this, toggleAvailableBoatsToPersons,
        International.getStringWithMnemonic("Personen"));
    if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleSortByCategories()) {
      Mnemonics.setButton(this, toggleAvailableBoatsToBoats, "EFA");
    }
    Mnemonics.setButton(this, toggleAvailableBoatsToDescriptionOrt,
        International.getStringWithMnemonic("Ort"));
    Mnemonics.setButton(this, toggleAvailableBoatsToBoatType,
        International.getStringWithMnemonic("Art"));
    Mnemonics.setButton(this, toggleAvailableBoatsToBoatNameAffix,
        International.getStringWithMnemonic("OptikFarbeKurz"));
    Mnemonics.setButton(this, toggleAvailableBoatsToOwner,
        International.getStringWithMnemonic("EigentümerKurz"));
    Mnemonics.setButton(this, toggleAvailableBoatsToPaddelArt,
        International.getStringWithMnemonic("Paddel"));
    Mnemonics.setButton(this, toggleAvailableBoatsToSteuermann,
        International.getStringWithMnemonic("SteuermannKurz"));

    toggleAvailableBoatsToBoats.setHorizontalAlignment(SwingConstants.RIGHT);
    toggleAvailableBoatsToPersons.setHorizontalAlignment(SwingConstants.LEFT);
    toggleAvailableBoats.add(toggleAvailableBoatsToBoats);
    toggleAvailableBoats.add(toggleAvailableBoatsToPersons);
    toggleAvailableBoats.add(toggleAvailableBoatsToDescriptionOrt);
    toggleAvailableBoats.add(toggleAvailableBoatsToBoatType);
    toggleAvailableBoats.add(toggleAvailableBoatsToBoatNameAffix);
    toggleAvailableBoats.add(toggleAvailableBoatsToOwner);
    toggleAvailableBoats.add(toggleAvailableBoatsToPaddelArt);
    toggleAvailableBoats.add(toggleAvailableBoatsToSteuermann);

    Font smallerFont = toggleAvailableBoatsToBoats.getFont().deriveFont(12.0f);
    // toggleAvailableBoatsToBoats.setFont(smallerFont);
    // toggleAvailableBoatsToPersons.setFont(smallerFont);
    // toggleAvailableBoatsToDescriptionOrt.setFont(smallerFont);
    // toggleAvailableBoatsToBoatType.setFont(smallerFont);
    // toggleAvailableBoatsToBoatNameAffix.setFont(smallerFont);
    // toggleAvailableBoatsToOwner.setFont(smallerFont);
    // toggleAvailableBoatsToPaddelArt.setFont(smallerFont);
    // toggleAvailableBoatsToSteuermann.setFont(smallerFont);

    toggleAvailableBoatsToBoats.setSelected(true);
    toggleAvailableBoatsToBoats.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToPersons.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToDescriptionOrt.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToBoatType.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToBoatNameAffix.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToOwner.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToPaddelArt.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });
    toggleAvailableBoatsToSteuermann.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        toggleAvailableBoats_actionPerformed(e);
      }
    });

    // Update GUI Elements for Boat Lists
    toggleAvailableBoatsToBoats.setVisible(
        Daten.efaConfig.isValueEfaDirekt_listAllowToggleSortByCategories()
        || Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons());
    toggleAvailableBoatsToPersons.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleBoatsPersons());
    toggleAvailableBoatsToDescriptionOrt.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories());
    toggleAvailableBoatsToBoatType.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories());
    toggleAvailableBoatsToBoatNameAffix.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories() && false);
    toggleAvailableBoatsToOwner.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories() && false);
    toggleAvailableBoatsToPaddelArt.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories());
    toggleAvailableBoatsToSteuermann.setVisible(Daten.efaConfig
        .isValueEfaDirekt_listAllowToggleSortByCategories());

    // Boat Lists
    boatsAvailableList = new ItemTypeBoatstatusList("BOATSAVAILABLELIST", IItemType.TYPE_PUBLIC,
        "", International.getStringWithMnemonic("verfügbare Boote"), this);
    personsAvailableList = new ItemTypeBoatstatusList("PERSONSAVAILABLELIST",
        IItemType.TYPE_PUBLIC, "", International.getStringWithMnemonic("Personen"), this);
    boatsOnTheWaterList = new ItemTypeBoatstatusList("BOATSONTHEWATERLIST", IItemType.TYPE_PUBLIC,
        "", International.getStringWithMnemonic("Boote auf Fahrt"), this);
    boatsNotAvailableList = new ItemTypeBoatstatusList("BOATSNOTAVAILABLELIST",
        IItemType.TYPE_PUBLIC, "", International.getStringWithMnemonic("nicht verfügbare Boote"),
        this);
    boatsAvailableList.setFieldSize(200, 400);
    personsAvailableList.setFieldSize(200, 400);
    boatsOnTheWaterList.setFieldSize(200, 300);
    boatsNotAvailableList.setFieldSize(200, 100);

    // Color bg = new Color(0, 170, 0);
    // boatsAvailableList.setBackgroundColor(bg);
    // personsAvailableList.setBackgroundColor(bg);
    // boatsOnTheWaterList.setBackgroundColor(bg);
    // boatsNotAvailableList.setBackgroundColor(bg);

    boatsAvailableList.setPopupActions(getListActions(1, null));
    personsAvailableList.setPopupActions(getListActions(101, null));
    boatsOnTheWaterList.setPopupActions(getListActions(2, null));
    boatsNotAvailableList.setPopupActions(getListActions(3, null));
    boatsAvailableList.registerItemListener(this);
    personsAvailableList.registerItemListener(this);
    boatsOnTheWaterList.registerItemListener(this);
    boatsNotAvailableList.registerItemListener(this);
    iniGuiListNames();

    // add Panels to Gui
    boatsAvailablePanel = new JPanel();
    boatsAvailablePanel.setLayout(new BorderLayout());
    boatsAvailableList.displayOnGui(this, boatsAvailablePanel, BorderLayout.CENTER, SortingBy.EfaSorting);
    // personsAvailableList.displayOnGui(this, boatsAvailablePanel, BorderLayout.CENTER); // Cannot
    // be displayed here and now, only when toggled to!
    JPanel togglePanel = new JPanel();
    // Size preferredSize =
    // togglePanel.setPreferredSize(preferredSize);
    togglePanel.add(toggleAvailableBoatsToBoats);
    if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()) {
      togglePanel.add(toggleAvailableBoatsToPersons);
      togglePanel.setVisible(Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons());
    }
    if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleSortByCategories()) {
      togglePanel.add(toggleAvailableBoatsToDescriptionOrt);
      togglePanel.add(toggleAvailableBoatsToBoatType);
      togglePanel.add(toggleAvailableBoatsToBoatNameAffix);
      togglePanel.add(toggleAvailableBoatsToOwner);
      togglePanel.add(toggleAvailableBoatsToPaddelArt);
      togglePanel.add(toggleAvailableBoatsToSteuermann);
      togglePanel.setVisible(Daten.efaConfig.isValueEfaDirekt_listAllowToggleSortByCategories());
    }
    boatsAvailablePanel.add(togglePanel, BorderLayout.NORTH);
    westPanel.setLayout(new BorderLayout());
    westPanel.add(boatsAvailablePanel, BorderLayout.CENTER);

    boatsNotAvailablePanel = new JPanel();
    boatsNotAvailablePanel.setLayout(new BorderLayout());
    boatsOnTheWaterList.displayOnGui(this, boatsNotAvailablePanel, BorderLayout.CENTER, SortingBy.EfaSorting);
    boatsNotAvailableList.displayOnGui(this, boatsNotAvailablePanel, BorderLayout.SOUTH, SortingBy.EfaSorting);
    eastPanel.setLayout(new BorderLayout());
    eastPanel.add(boatsNotAvailablePanel, BorderLayout.CENTER);
  }

  private void iniGuiListNames() {
    boolean fkey = Daten.efaConfig.getValueEfaDirekt_showButtonHotkey();
    if (!Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()
        || toggleAvailableBoatsToBoats.isSelected()) {
      boatsAvailableList.setDescription(International.getString("verfügbare Boote")
          + (fkey ? " [F10]" : ""));
    } else {
      personsAvailableList.setDescription(International.getString("Personen")
          + (fkey ? " [F10]" : ""));
    }
    boatsOnTheWaterList.setDescription(International.getString("Boote auf Fahrt")
        + (fkey ? " [F11]" : ""));
    boatsNotAvailableList.setDescription(International.getString("nicht verfügbare Boote")
        + (fkey ? " [F12]" : ""));
  }

  private void iniGuiCenterPanel() {
    iniGuiLogo(); // Standard-Bild
    iniGuiButtons();
    updateGuiClock();
    centerPanel.setLayout(new GridBagLayout());

    Insets noInset = new Insets(0, 0, 0, 0);
    Insets top10Inset = new Insets(10, 0, 0, 0);
    centerPanel.add(infoLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, top10Inset, 0, 0));

    int yPlacement = 1;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    if (screenSize.getHeight() < 1234) {
      yPlacement = 22;
    }
    int logoTop = (int) (10.0f * (Dialog.getFontSize() < 10 ? 12 : Dialog.getFontSize()) / Dialog
        .getDefaultFontSize());
    int logoBottom = 5;
    if (Daten.efaConfig.getValueEfaDirekt_startMaximized()
        && Daten.efaConfig.getValueEfaDirekt_vereinsLogo().length() > 0) {
      logoBottom += (int) ((Dialog.screenSize.getHeight() - 825) / 5);
      if (logoBottom < 0) {
        logoBottom = 0;
      }
    }
    centerPanel.add(logoLabel, new GridBagConstraints(1, yPlacement, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(logoTop, 0, 0 + logoBottom, 0),
        0, 0));

    int fahrtbeginnTop = (int) (10.0f * (Dialog.getFontSize() < 10 ? 12 : Dialog.getFontSize())
        / Dialog.getDefaultFontSize());
    centerPanel.add(startSessionButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
        new Insets(fahrtbeginnTop, 0, 0, 0),
        0, 0));
    centerPanel.add(finishSessionButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, noInset, 0, 0));
    centerPanel.add(abortSessionButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(lateEntryButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(clubworkButton, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(boatReservationButton, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(showLogbookButton, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(statisticsButton, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, noInset, 0, 0));
    centerPanel.add(messageToAdminButton, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0,
        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(adminButton, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(specialButton, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0,
        GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, top10Inset, 0, 0));
    centerPanel.add(helpButton, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0,
        GridBagConstraints.SOUTH, GridBagConstraints.NONE, top10Inset, 0, 0));
    centerPanel.add(efaButton, new GridBagConstraints(1, 14, 1, 1, 0.0, 0.0,
        GridBagConstraints.SOUTH, GridBagConstraints.NONE, top10Inset, 0, 0));
    centerPanel.add(clock.getGuiComponent(), new GridBagConstraints(1, 15, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(10, 10, 10, 10), 0, 0));
  }

  private void updateTopInfoText(String text1, String text2) {
    if (infoLabel.getText().equals(text1)) {
      updateTopInfoText(text2);
    } else {
      updateTopInfoText(text1);
    }
  }

  private void updateTopInfoText(String infoText) {
    infoLabel.setText(infoText);
  }

  private void updateGuiZentralesLogo(String strZentralesBild) {
    if (strZentralesBild == null || strZentralesBild.length() == 0) {
      logoLabel.setIcon(null);
      return;
    }

    try {
      int xWidth = 352;
      int yHeight = 654; // 704;
      ImageIcon imageIcon = new ImageIcon(strZentralesBild);
      if (imageIcon.getIconHeight() < 0) {
        if (isToggleF12LangtextF12()) {
          strZentralesBild = Daten.efaImagesDirectory + "missing.photo.png"; // alternatives Bild
        } else {
          strZentralesBild = Daten.efaConfig.getValueEfaDirekt_vereinsLogo(); // alternatives Bild          
        }
        imageIcon = new ImageIcon(strZentralesBild);
      }
      Image newImage;
      if (imageIcon.getIconWidth() < xWidth) {
        newImage = imageIcon.getImage().getScaledInstance(-1, yHeight, Image.SCALE_SMOOTH);
      } else {
        newImage = imageIcon.getImage().getScaledInstance(xWidth, -1, Image.SCALE_SMOOTH);        
      }
      imageIcon = new ImageIcon(newImage, imageIcon.getDescription());
      logoLabel.setIcon(imageIcon);

      int lastIndexSlash = strZentralesBild.lastIndexOf(Daten.fileSep);
      String fileName = strZentralesBild.substring(lastIndexSlash, strZentralesBild.length());
      Pattern regexPattern = Pattern.compile("\\D*(\\d\\d*)x(\\d*).*");
      Matcher matcher = regexPattern.matcher(fileName);
      if (matcher.matches() && matcher.groupCount() == 2) {
        xWidth = Integer.parseInt(matcher.group(1));
        yHeight = Integer.parseInt(matcher.group(2));
      }
      if (xWidth > 352) {
        xWidth = 352; // max
        // yHeight = 704;
      }
      logoLabel.setPreferredSize(new Dimension(xWidth, yHeight));
    } catch (Exception e) {
      Logger.log(Logger.WARNING, Logger.MSG_ERROR_EXCEPTION, e);
    }
  }

  private void iniGuiLogo() {
    infoLabel.setPreferredSize(new Dimension(320, 170));
    infoLabel.setVerticalAlignment(SwingConstants.BOTTOM);
    infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    infoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    infoLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        actionBoatInfosOrEfaAbout();
      }
    });

    logoLabel.setPreferredSize(new Dimension(352, 654));
    logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    logoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    logoLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        actionBoatInfosOrEfaAbout();
      }
    });
  }

  private void iniGuiButtons() {
    Mnemonics.setButton(this, startSessionButton,
        International.getStringWithMnemonic("Fahrt beginnen"));
    startSessionButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionStartSession(null);
      }
    });

    Mnemonics.setButton(this, finishSessionButton,
        International.getStringWithMnemonic("Fahrt beenden"));
    finishSessionButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionFinishSession();
      }
    });

    Mnemonics.setButton(this, abortSessionButton,
        International.getStringWithMnemonic("Fahrt abbrechen"));
    abortSessionButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionAbortSession();
      }
    });

    Mnemonics.setButton(this, lateEntryButton, International.getStringWithMnemonic("Nachtrag"));
    lateEntryButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionLateEntry();
      }
    });

    Mnemonics.setButton(this, boatReservationButton,
        International.getStringWithMnemonic("Bootsreservierungen"));
    boatReservationButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionBoatReservations();
      }
    });

    Mnemonics.setButton(this, showLogbookButton,
        International.getStringWithMnemonic("Fahrtenbuch anzeigen"));
    showLogbookButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionShowLogbook();
      }
    });

    Mnemonics.setButton(this, statisticsButton,
        International.getStringWithMnemonic("Statistik erstellen"));
    statisticsButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionStatistics();
      }
    });

    Mnemonics.setButton(this, clubworkButton, International.getStringWithMnemonic("Vereinsarbeit"));
    clubworkButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionClubwork();
      }
    });

    Mnemonics.setButton(this, messageToAdminButton,
        International.getStringWithMnemonic("Nachricht an Admin"));
    messageToAdminButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionMessageToAdmin();
      }
    });

    Mnemonics.setButton(this, adminButton, International.getStringWithMnemonic("Admin-Modus"));
    adminButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionAdminMode();
      }
    });

    specialButton.setText(International.getString("Spezial-Button"));
    specialButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionSpecial();
      }
    });

    helpButton.setText(International.getMessage("Hilfe mit {key}", "[F1]"));
    helpButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        hilfeButton_actionPerformed(e);
      }
    });

    efaButton.setPreferredSize(new Dimension(90, 55));
    efaButton.setIcon(getIcon(Daten.getEfaImage(1)));
    efaButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        efaButton_actionPerformed(e);
      }
    });

    updateGuiButtonText();
  }

  private void setButtonLAF(JButton button, ItemTypeConfigButton config, String icon) {
    if (config != null) {
      button.setBackground(EfaUtil.getColorOrGray(config.getValueColor()));
    }
    if (icon != null && button.isVisible()) {
      button.setIcon(getIcon(icon));
      button.setIconTextGap(10);
      button.setHorizontalAlignment(SwingConstants.LEFT);
    }
  }

  private void updateGuiButtonLAF() {
    this.boatReservationButton.setVisible(Daten.efaConfig
        .getValueEfaDirekt_butBootsreservierungen().getValueShow());
    this.showLogbookButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butFahrtenbuchAnzeigen()
        .getValueShow());
    this.statisticsButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butStatistikErstellen()
        .getValueShow());
    this.clubworkButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butVereinsarbeit()
        .getValueShow() && clubwork != null && clubwork.isOpen());
    this.messageToAdminButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butNachrichtAnAdmin()
        .getValueShow());
    this.adminButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butAdminModus().getValueShow());
    this.specialButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butSpezial().getValueShow());
    this.helpButton.setVisible(Daten.efaConfig.getValueEfaDirekt_butHelp().getValueShow());

    setButtonLAF(startSessionButton, Daten.efaConfig.getValueEfaDirekt_butFahrtBeginnen(),
        "action_startSession.png");
    setButtonLAF(finishSessionButton, Daten.efaConfig.getValueEfaDirekt_butFahrtBeenden(),
        "action_finishSession.png");
    setButtonLAF(abortSessionButton, Daten.efaConfig.getValueEfaDirekt_butFahrtAbbrechen(),
        "action_abortSession.png");
    setButtonLAF(lateEntryButton, Daten.efaConfig.getValueEfaDirekt_butNachtrag(),
        "action_lateEntry.png");
    setButtonLAF(boatReservationButton, Daten.efaConfig.getValueEfaDirekt_butBootsreservierungen(),
        "menu_boatreservations.png");
    setButtonLAF(showLogbookButton, Daten.efaConfig.getValueEfaDirekt_butFahrtenbuchAnzeigen(),
        "action_logbook.png");
    setButtonLAF(statisticsButton, Daten.efaConfig.getValueEfaDirekt_butStatistikErstellen(),
        "action_statistics.png");
    setButtonLAF(clubworkButton, Daten.efaConfig.getValueEfaDirekt_butVereinsarbeit(),
        "action_clubwork.png");
    setButtonLAF(messageToAdminButton, Daten.efaConfig.getValueEfaDirekt_butNachrichtAnAdmin(),
        "action_message.png");
    setButtonLAF(adminButton, Daten.efaConfig.getValueEfaDirekt_butAdminModus(),
        "action_admin.png");
    setButtonLAF(specialButton, Daten.efaConfig.getValueEfaDirekt_butSpezial(),
        "action_special.png");
    setButtonLAF(helpButton, null, "action_help.png");

  }

  public void updateGuiButtonText() {
    boolean fkey = Daten.efaConfig.getValueEfaDirekt_showButtonHotkey();
    this.startSessionButton.setText(Daten.efaConfig.getValueEfaDirekt_butFahrtBeginnen()
        .getValueText() + (fkey ? " [F2]" : ""));
    this.finishSessionButton.setText(Daten.efaConfig.getValueEfaDirekt_butFahrtBeenden()
        .getValueText() + (fkey ? " [F3]" : ""));
    this.abortSessionButton.setText(International.getString("Fahrt abbrechen")
        + (fkey ? " [F4]" : ""));
    this.lateEntryButton.setText(International.getString("Nachtrag") + (fkey ? " [F5]" : ""));
    this.boatReservationButton.setText(International.getString("Reservierungen")
        + (fkey ? " [F6]" : ""));
    this.clubworkButton.setText(International.getString("Vereinsarbeit")
        + (fkey ? " [Alt-F6]" : ""));
    this.showLogbookButton.setText(International.getString("Fahrtenbuch anzeigen")
        + (fkey ? " [F7]" : ""));
    this.statisticsButton.setText(International.getString("Statistik erstellen")
        + (fkey ? " [F8]" : ""));
    this.messageToAdminButton.setText(International.getString("Nachricht an Admin")
        + (fkey ? " [F9]" : ""));
    this.adminButton.setText(International.getString("Admin-Modus") + (fkey ? " [Alt-F10]" : ""));
    this.specialButton.setText(Daten.efaConfig.getValueEfaDirekt_butSpezial().getValueText()
        + (fkey ? " [Alt-F11]" : ""));
    updateGuiButtonLAF();
    if (!Daten.efaConfig.getValueEfaDirekt_startMaximized() && isDisplayable()) {
      packFrame("iniButtonText()");
    }
  }

  private void updateGuiClock() {
    if (clock == null) {
      clock = new ClockMiniWidget();
    }
    clock.getGuiComponent().setVisible(Daten.efaConfig.getValueEfaDirekt_showUhr());
  }

  private void updateGuiNews() {
    if (news == null) {
      news = new NewsMiniWidget();
    }
    news.setText(Daten.efaConfig.getValueEfaDirekt_newsText());
    news.setScrollSpeed(Daten.efaConfig.getValueEfaDirekt_newsScrollSpeed());
    news.getGuiComponent().setVisible(Daten.efaConfig.getValueEfaDirekt_showNews());
    if (isDisplayable()) {
      packFrame("updateGuiNews()");
    }
  }

  private void updateGuiWidgets() {
    widgetTopPanel.removeAll();
    widgetBottomPanel.removeAll();
    widgetLeftPanel.removeAll();
    widgetRightPanel.removeAll();
    widgetCenterPanel.removeAll();

    if (Daten.efaConfig.getWidgets() == null) {
      return;
    }

    // stop all previously started widgets
    try {
      for (int i = 0; widgets != null && i < widgets.size(); i++) {
        IWidget w = widgets.get(i);
        w.stop();
      }
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    widgets = new Vector<IWidget>();

    // find all enabled widgets
    Vector<IWidget> allWidgets = Widget.getAllWidgets();
    for (int i = 0; allWidgets != null && i < allWidgets.size(); i++) {
      IWidget w = allWidgets.get(i);
      String parameterName = w.getParameterName(Widget.PARAM_ENABLED);
      IItemType enabled = Daten.efaConfig.getExternalGuiItem(parameterName);
      if (enabled != null
          && enabled instanceof ItemTypeBoolean
          && ((ItemTypeBoolean) enabled).getValue()) {
        // set parameters for this enabled widget according to configuration
        IItemType[] params = w.getParameters();
        for (IItemType param : params) {
          IItemType externalGuiItem = Daten.efaConfig.getExternalGuiItem(param.getName());
          param.parseValue(externalGuiItem.toString());
        }
        widgets.add(w);
      }
    }

    // show all enabled widgets
    for (int i = 0; i < widgets.size(); i++) {
      IWidget w = widgets.get(i);
      String position = w.getPosition();
      if (IWidget.POSITION_TOP.equals(position)) {
        w.show(widgetTopPanel, BorderLayout.CENTER);
      }
      if (IWidget.POSITION_BOTTOM.equals(position)) {
        w.show(widgetBottomPanel, BorderLayout.CENTER);
      }
      if (IWidget.POSITION_LEFT.equals(position)) {
        w.show(widgetLeftPanel, BorderLayout.CENTER);
      }
      if (IWidget.POSITION_RIGHT.equals(position)) {
        w.show(widgetRightPanel, BorderLayout.CENTER);
      }
      if (IWidget.POSITION_CENTER.equals(position)) {
        w.show(widgetCenterPanel, BorderLayout.CENTER);
      }
    }
  }

  private void iniGuiNorthPanel() {
    updateGuiNews();
    northPanel.setLayout(new BorderLayout());
  }

  private void iniGuiSouthPanel() {
    updateGuiNews();
    southPanel.setLayout(new BorderLayout());

    southPanel.add(statusLabel, BorderLayout.NORTH);
    southPanel.add(news.getGuiComponent(), BorderLayout.SOUTH);
    statusLabelSetText(International.getString("Status"));
  }

  private void statusLabelSetText(String s) {
    statusLabel.setText(s);
    // wenn Text zu lang, dann PreferredSize verringern, damit bei pack() die zu große Label-Breite
    // nicht zum Vergrößern des Fensters führt!
    if (statusLabel.getPreferredSize().getWidth() > this.getSize().getWidth()) {
      Dimension dimension = new Dimension(
          (int) this.getSize().getWidth() - 20,
          (int) statusLabel.getPreferredSize().getHeight());
      statusLabel.setPreferredSize(dimension);
    }
  }

  public void packFrame(String source) {
    this.pack();
  }

  public void bringFrameToFront() {
    this.toFront();
  }

  // i == 0 - automatically try to find correct list to focus
  // i == 1 - boats/persons available
  // i == 2 - boats on the water
  // i == 3 - boats not available
  public void boatListRequestFocus(int i) {
    if (i == 0) {
      if (boatsAvailableList != null && boatsAvailableList.getSelectedIndex() >= 0) {
        boatsAvailableList.requestFocus();
      } else if (personsAvailableList != null && personsAvailableList.getSelectedIndex() >= 0) {
        personsAvailableList.requestFocus();
      } else if (boatsOnTheWaterList != null && boatsOnTheWaterList.getSelectedIndex() >= 0) {
        boatsOnTheWaterList.requestFocus();
      } else if (boatsNotAvailableList != null && boatsNotAvailableList.getSelectedIndex() >= 0) {
        boatsNotAvailableList.requestFocus();
      } else if (boatsAvailableList != null) {
        boatsAvailableList.requestFocus();
      }
    }
    if (i == 1) {
      if (toggleAvailableBoatsToBoats.isSelected()) {
        boatsAvailableList.requestFocus();
      } else {
        personsAvailableList.requestFocus();
      }
    }
    if (i == 2) {
      boatsOnTheWaterList.requestFocus();
    }
    if (i == 3) {
      boatsNotAvailableList.requestFocus();
    }
  }

  void alive() {
    lastUserInteraction = System.currentTimeMillis();
  }

  public long getLastUserInteraction() {
    return lastUserInteraction;
  }

  private void this_windowClosing(WindowEvent e) {
    cancel(e, EFA_EXIT_REASON_USER, null, false);
  }

  private void this_windowDeactivated(WindowEvent e) {
    // nothing to do
  }

  private void this_windowActivated(WindowEvent e) {
    try {
      if (!isEnabled() && efaBaseFrame != null) {
        efaBaseFrame.toFront();
      }
    } catch (Exception ee) {
      Logger.logdebug(ee);
    }
  }

  private void this_windowIconified(WindowEvent e) {
    // super.processWindowEvent(e);
    this.setState(Frame.NORMAL);
  }

  @Override
  public boolean cancel() {
    return cancel(null, EFA_EXIT_REASON_USER, null, false);
  }

  public boolean cancel(WindowEvent e, int reason, AdminRecord admin, boolean restart) {
    Dialog.IGNORE_WINDOW_STACK_CHECKS = true;
    int exitCode = 0;
    String whoUser = "unknown";

    switch (reason) {
      case EFA_EXIT_REASON_USER: // manuelles Beenden von efa
        boolean byUser;
        if (admin != null || !Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenEfaBeenden()) {
          while (admin == null || !admin.isAllowedExitEfa()) {
            if (admin == null) {
              admin = AdminLoginDialog.login(this, International.getString("Beenden von efa"));
              if (admin == null) {
                Dialog.IGNORE_WINDOW_STACK_CHECKS = false;
                return false;
              }
            }
            if (admin != null && !admin.isAllowedExitEfa()) {
              EfaMenuButton.insufficientRights(admin, EfaMenuButton.BUTTON_EXIT);
              admin = null;
            }
          }
          whoUser = International.getString("Admin") + "=" + admin.getName();
          byUser = false;
        } else {
          whoUser = International.getString("Normaler Benutzer");
          byUser = true;
        }
        if (Daten.efaConfig.getValueEfaDirekt_execOnEfaExit().length() > 0 && byUser) {
          Logger.log(Logger.INFO, Logger.MSG_EVT_EFAEXITEXECCMD,
              International.getMessage(
                  "Programmende veranlaßt; versuche, Kommando '{cmd}' auszuführen...",
                  Daten.efaConfig.getValueEfaDirekt_execOnEfaExit()));
          try {
            Runtime.getRuntime().exec(Daten.efaConfig.getValueEfaDirekt_execOnEfaExit());
          } catch (Exception ee) {
            Logger.log(Logger.ERROR, Logger.MSG_ERR_EFAEXITEXECCMD_FAILED,
                LogString.cantExecCommand(Daten.efaConfig.getValueEfaDirekt_execOnEfaExit(),
                    International.getString("Kommando")));
          }
        }
        break;
      case EFA_EXIT_REASON_TIME:
      case EFA_EXIT_REASON_IDLE:
        whoUser = International.getString("Zeitsteuerung");
        whoUser += " (" + (reason == EFA_EXIT_REASON_TIME ? "time" : "idle") + ")";
        if (Daten.efaConfig.getValueEfaDirekt_execOnEfaAutoExit().length() > 0) {
          Logger.log(Logger.INFO, Logger.MSG_EVT_EFAEXITEXECCMD,
              International.getMessage(
                  "Programmende veranlaßt; versuche, Kommando '{cmd}' auszuführen...",
                  Daten.efaConfig.getValueEfaDirekt_execOnEfaAutoExit()));
          try {
            Runtime.getRuntime().exec(Daten.efaConfig.getValueEfaDirekt_execOnEfaAutoExit());
          } catch (Exception ee) {
            Logger.log(Logger.ERROR, Logger.MSG_ERR_EFAEXITEXECCMD_FAILED,
                LogString.cantExecCommand(Daten.efaConfig.getValueEfaDirekt_execOnEfaAutoExit(),
                    International.getString("Kommando")));
          }
        }
        break;
      case EFA_EXIT_REASON_OOME:
        whoUser = International.getString("Speicherüberwachung");
        break;
      case EFA_EXIT_REASON_AUTORESTART:
        whoUser = International.getString("Automatischer Neustart");
        break;
      case EFA_EXIT_REASON_ONLINEUPDATE:
        whoUser = International.getString("Online-Update");
        break;
    }
    if (restart) {
      exitCode = Daten.program.restart();
    }

    // if (e != null) {
    // super.processWindowEvent(e);
    // }
    Logger.log(Logger.INFO, Logger.MSG_EVT_EFAEXIT,
        International.getMessage("Programmende durch {originator}", whoUser));
    super.cancel();
    Daten.haltProgram(exitCode);
    return true;
  }

  public void cancelRunInThreadWithDelay(int reason, AdminRecord admin, boolean restart) {
    final int _reason = reason;
    final AdminRecord _admin = admin;
    final boolean _restart = restart;
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(1000);
        } catch (Exception e) {
        }
        cancel(null, _reason, _admin, _restart);
      }
    }.start();
  }

  /**
   * Returns all possible actions for a list. Those actions are prefixed with the following numbers
   * representing those actions, which may be processed by processListAction(String, int): 1 - start
   * session 2 - finish session 3 - late entry 4 - change session 5 - cancel session 6 - boat
   * reservations
   *
   * @param listnr
   * @param r
   * @return
   */
  private String[] getListActions(int listnr, DataRecord r) {
    if (r != null && r instanceof BoatStatusRecord) {
      // this boat may have been sorted into a "wrong" list... fix its status first
      String s = ((BoatStatusRecord) r).getCurrentStatus();
      if (s != null && s.equals(BoatStatusRecord.STATUS_AVAILABLE)) {
        listnr = 1;
      }
      if (s != null && s.equals(BoatStatusRecord.STATUS_ONTHEWATER)) {
        listnr = 2;
      }
      if (s != null && s.equals(BoatStatusRecord.STATUS_NOTAVAILABLE)) {
        listnr = 3;
      }
    }

    String startSession = EfaUtil.replace(
        Daten.efaConfig.getValueEfaDirekt_butFahrtBeginnen().getValueText(), ">>>", "").trim();
    String finishSession = EfaUtil.replace(
        Daten.efaConfig.getValueEfaDirekt_butFahrtBeenden().getValueText(), "<<<", "").trim();
    String boatReserve = (Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservieren()
        ? International.getString("Reservierungen")
        : International.getString("Bootsreservierungen"));
    if (listnr == 1 || listnr == 101) { // verfügbare Boote bzw. Personen
      if (listnr == 1) {
        ArrayList<String> listItems = new ArrayList<String>();
        listItems.add(ACTIONID_STARTSESSION + startSession);
        listItems.add(ACTIONID_LATEENTRY + International.getString("Nachtrag"));
        if (Daten.efaConfig.getValueEfaDirekt_butBootsreservierungen().getValueShow()) {
          listItems.add(ACTIONID_BOATRESERVATIONS + boatReserve);
        }
        if (Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton()) {
          listItems.add(ACTIONID_BOATDAMAGES + International.getString("Bootsschaden melden"));
        }
        listItems.add(ACTIONID_BOATINFOS + International.getString("Bootsinfos"));
        listItems.add(ACTIONID_LASTBOATUSAGE + International.getString("Letzte Benutzung"));
        return listItems.toArray(new String[0]);
      } else {
        return new String[] {
            ACTIONID_STARTSESSION + startSession,
            ACTIONID_LATEENTRY + International.getString("Nachtrag")
        };
      }
    }
    if (listnr == 2) { // Boote auf Fahrt
      ArrayList<String> listItems = new ArrayList<String>();
      listItems.add(ACTIONID_FINISHSESSION + finishSession);
      listItems.add(ACTIONID_STARTSESSIONCORRECT + International.getString("Eintrag ändern"));
      listItems.add(ACTIONID_ABORTSESSION + International.getString("Fahrt abbrechen"));
      if (Daten.efaConfig.getValueEfaDirekt_butBootsreservierungen().getValueShow()) {
        listItems.add(ACTIONID_BOATRESERVATIONS + boatReserve);
      }
      if (Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton()) {
        listItems.add(ACTIONID_BOATDAMAGES + International.getString("Bootsschaden melden"));
      }
      listItems.add(ACTIONID_BOATINFOS + International.getString("Bootsinfos"));
      listItems.add(ACTIONID_LASTBOATUSAGE + International.getString("Letzte Benutzung"));
      return listItems.toArray(new String[0]);
    }
    if (listnr == 3) { // nicht verfügbare Boote
      ArrayList<String> listItems = new ArrayList<String>();
      listItems.add(ACTIONID_STARTSESSION + startSession);
      if (Daten.efaConfig.getValueEfaDirekt_wafaRegattaBooteAufFahrtNichtVerfuegbar()) {
        listItems.add(ACTIONID_FINISHSESSION + finishSession);
        listItems.add(ACTIONID_ABORTSESSION + International.getString("Fahrt abbrechen"));
      }
      listItems.add(ACTIONID_LATEENTRY + International.getString("Nachtrag"));
      if (Daten.efaConfig.getValueEfaDirekt_butBootsreservierungen().getValueShow()) {
        listItems.add(ACTIONID_BOATRESERVATIONS + boatReserve);
      }
      if (Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton()) {
        listItems.add(ACTIONID_BOATDAMAGES + International.getString("Bootsschaden melden"));
      }
      listItems.add(ACTIONID_BOATINFOS + International.getString("Bootsinfos"));
      listItems.add(ACTIONID_LASTBOATUSAGE + International.getString("Letzte Benutzung"));
      return listItems.toArray(new String[0]);
    }
    return null;
  }

  // ========================================================================================================================================
  // Data-related methods
  // ========================================================================================================================================

  public static void haltProgram(String s, int exitCode) {
    if (s != null) {
      Dialog.error(s);
      Logger.log(Logger.ERROR, Logger.MSG_ERR_GENERIC,
          EfaUtil.replace(s, NEWLINE, " ", true));
    }
    Daten.haltProgram(exitCode);
  }

  private void updateProjectLogbookInfo() {
    if (Daten.project == null || !Daten.project.isOpen()) {
      titleLabel.setText(Daten.EFA_LONGNAME);
    } else {
      titleLabel.setText(Daten.EFA_LONGNAME + " [" + Daten.project.getProjectName() +
          (logbook != null && logbook.isOpen() ? ": " + logbook.getName() : "") + "]");
    }
  }

  public Project openProject(AdminRecord admin) {
    try {
      // project to open
      String projectName = null;
      if (admin == null && Daten.efaConfig.getValueLastProjectEfaBoathouse().length() > 0) {
        projectName = Daten.efaConfig.getValueLastProjectEfaBoathouse();
      }

      if (projectName == null || projectName.length() == 0) {
        if (admin != null && admin.isAllowedAdministerProjectLogbook()) {
          OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
              OpenProjectOrLogbookDialog.Type.project, admin);
          projectName = dlg.openDialog();
        }
      }

      if (projectName == null || projectName.length() == 0) {
        return null;
      }

      // close open project now
      if (Daten.project != null) {
        try {
          Daten.project.closeAllStorageObjects();
        } catch (Exception e) {
          String msg = LogString.fileCloseFailed(Daten.project.getProjectName(),
              International.getString("Projekt"), e.getMessage());
          Logger.log(Logger.ERROR, Logger.MSG_DATA_CLOSEFAILED, msg);
          Dialog.error(msg);
        }
      }
      Daten.project = null;
      logbook = null;

      if (!Project.openProject(projectName, true)) {
        Daten.project = null;
        return null;
      }

      if (Daten.project != null
          && Daten.project.getProjectStorageType() == IDataAccess.TYPE_EFA_REMOTE
          && !Daten.project.getProjectStorageUsername().equals(Admins.SUPERADMIN)) {
        Daten.project = null;
        String err = International.getString(
                "Nur der Super-Administrator darf im Bootshaus-Modus ein Remote-Projekt öffnen.");
        Logger.log(Logger.ERROR, Logger.MSG_ERR_NOPROJECTOPENED, err);
        Dialog.error(err);
        return null;
      }

      Daten.efaConfig.setValueLastProjectEfaBoathouse(projectName);
      boatStatus = Daten.project.getBoatStatus(false);
      Logger.log(Logger.INFO, Logger.MSG_EVT_PROJECTOPENED,
          LogString.fileOpened(projectName, International.getString("Projekt")));

      if (efaBoathouseBackgroundTask != null) {
        efaBoathouseBackgroundTask.interrupt();
      }
      return Daten.project;
    } finally {
      updateProjectLogbookInfo();
    }
  }

  public Logbook openLogbook(AdminRecord admin) {
    try {
      if (Daten.project == null) {
        return null;
      }

      // close any other logbook first
      String logbookNameBefore = (logbook != null ? logbook.getName() : null);
      if (logbook != null) {
        try {
          logbook.close();
        } catch (Exception e) {
          String msg = LogString.fileCloseFailed(Daten.project.getProjectName(),
              International.getString("Fahrtenbuch"), e.toString());
          Logger.log(Logger.ERROR, Logger.MSG_DATA_CLOSEFAILED, msg);
          Logger.logdebug(e);
          Dialog.error(msg);
          logbook = null;
        }
      }

      // logbook to open
      String logbookName = null;
      if (admin == null && Daten.project.getCurrentLogbookEfaBoathouse() != null) {
        logbookName = Daten.project.getCurrentLogbookEfaBoathouse();
      }

      if (logbookName == null || logbookName.length() == 0) {
        if (admin != null && admin.isAllowedAdministerProjectLogbook()) {
          OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
              OpenProjectOrLogbookDialog.Type.logbook, admin);
          logbookName = dlg.openDialog();
        }
      }
      if ((logbookName == null || logbookName.length() == 0) &&
          logbookNameBefore != null && logbookNameBefore.length() > 0 && admin != null) {
        // Admin-Mode: There was a logbook opened before, but admin aborted dialog and didn't
        // open a new one. So let's open the old one again!
        logbookName = logbookNameBefore;
      }
      if (logbookName == null || logbookName.length() == 0) {
        return null;
      }
      if (!openLogbook(logbookName)) {
        logbook = null;
        return null;
      }
      return logbook;
    } finally {
      updateProjectLogbookInfo();
    }
  }

  public Clubwork openClubwork(AdminRecord admin) {
    try {
      if (Daten.project == null) {
        return null;
      }

      // close any other clubworkBook first
      String clubworkNameBefore = (clubwork != null ? clubwork.getName() : null);
      if (clubwork != null) {
        try {
          clubwork.close();
        } catch (Exception e) {
          String msg = LogString.fileCloseFailed(Daten.project.getProjectName(),
              International.getString("Vereinsarbeit"), e.toString());
          Logger.log(Logger.ERROR, Logger.MSG_DATA_CLOSEFAILED, msg);
          Logger.logdebug(e);
          Dialog.error(msg);
          clubwork = null;
        }
      }

      // clubwork to open
      String clubworkName = null;
      if (admin == null && Daten.project.getCurrentClubworkEfaBoathouse() != null) {
        clubworkName = Daten.project.getCurrentClubworkEfaBoathouse();
      }

      // check whether clubwork exists in project (may have been deleted)
      if (clubworkName != null) {
        Hashtable<String, String> allClubwork = Daten.project.getClubworks();
        if (allClubwork == null || allClubwork.get(clubworkName) == null) {
          // clubwork was deleted
          Daten.project.setCurrentClubworkEfaBoathouse(null);
          clubworkName = null;
        }
      }

      if (clubworkName == null || clubworkName.length() == 0) {
        if (admin != null && admin.isAllowedAdministerProjectClubwork()) {
          OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
              OpenProjectOrLogbookDialog.Type.clubwork, admin);
          clubworkName = dlg.openDialog();
        }
      }
      if ((clubworkName == null || clubworkName.length() == 0)
          && clubworkNameBefore != null && clubworkNameBefore.length() > 0 && admin != null) {
        // Admin-Mode: There was a clubwork opened before, but admin aborted dialog and didn't
        // open a new one. So let's open the old one again!
        clubworkName = clubworkNameBefore;
      }
      if (clubworkName == null || clubworkName.length() == 0) {
        return null;
      }
      if (!openClubwork(clubworkName)) {
        clubwork = null;
        return null;
      }
      return clubwork;
    } finally {
      // current clubwork not shown yet updateProjectLogbookInfo();
    }
  }

  public boolean openLogbook(String logbookName) {
    try {
      if (Daten.project == null) {
        return false;
      }
      try {
        if (logbook != null && logbook.isOpen()) {
          logbook.close();
        }
      } catch (Exception e) {
        Logger.log(e);
        Dialog.error(e.toString());
      }
      if (logbookName != null && logbookName.length() > 0) {
        logbook = Daten.project.getLogbook(logbookName, false);
        if (logbook != null) {
          Daten.project.setCurrentLogbookEfaBoathouse(logbookName);
          Logger.log(Logger.INFO, Logger.MSG_EVT_LOGBOOKOPENED,
              LogString.fileOpened(logbookName, International.getString("Fahrtenbuch")));
          if (efaBoathouseBackgroundTask != null) {
            efaBoathouseBackgroundTask.interrupt();
          }
          return true;
        } else {
          Dialog.error(
              LogString.fileOpenFailed(logbookName, International.getString("Fahrtenbuch")));
        }
      }
      return false;
    } finally {
      updateProjectLogbookInfo();
    }
  }

  public boolean openClubwork(String clubworkName) {
    try {
      if (Daten.project == null) {
        return false;
      }
      try {
        if (clubwork != null && clubwork.isOpen()) {
          clubwork.close();
        }
      } catch (Exception e) {
        Logger.log(e);
        Dialog.error(e.toString());
      }
      if (clubworkName != null && clubworkName.length() > 0) {
        clubwork = Daten.project.getClubwork(clubworkName, false);
        if (clubwork != null) {
          clubwork.setEfaBoathouseFrame(this);
          Daten.project.setCurrentClubworkEfaBoathouse(clubworkName);
          Logger.log(Logger.INFO, Logger.MSG_EVT_CLUBWORKOPENED,
              LogString.fileOpened(clubworkName, International.getString("Vereinsarbeit")));
          if (efaBoathouseBackgroundTask != null) {
            efaBoathouseBackgroundTask.interrupt();
          }
          return true;
        } else {
          Dialog.error(LogString.fileOpenFailed(clubworkName,
              International.getString("Vereinarbeit")));
        }
      }
      return false;
    } finally {
      // clubwork not shown yet updateProjectLogbookInfo();
    }
  }

  public Logbook getLogbook() {
    return logbook;
  }

  public Clubwork getClubwork() {
    return clubwork;
  }

  // synchronizing this method can cause deadlock!!!!
  public void updateBoatLists(boolean listChanged) {
    if (!isEnabled()) {
      return;
    }
    if (inUpdateBoatList) {
      if (Logger.isTraceOn(Logger.TT_GUI, 8)) {
        Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
            + ") - concurrent call, aborting");
      }
      return;
    }
    inUpdateBoatList = true;
    try {
      if (Logger.isTraceOn(Logger.TT_GUI, 8)) {
        Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged + ")");
      }

      if (Daten.project == null || boatStatus == null) {
        boatsAvailableList.setItems(null);
        personsAvailableList.setItems(null);
        boatsOnTheWaterList.setItems(null);
        boatsNotAvailableList.setItems(null);
        if (Daten.project == null) {
          boatsAvailableList.addItem("*** " + International.getString("Kein Projekt geöffnet.")
              + " ***", null, false, '\0');
          personsAvailableList.addItem("*** " + International.getString("Kein Projekt geöffnet.")
              + " ***", null, false, '\0');
        }
        boatsAvailableList.showValue();
        personsAvailableList.showValue();
        boatsOnTheWaterList.showValue();
        boatsNotAvailableList.showValue();
      } else {
        if (boatsAvailableList.size() == 0) {
          listChanged = true;
        }

        if (listChanged) {
          if (!Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()
              || toggleAvailableBoatsToBoats.isSelected()) {
            if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
              Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                  + ") - setting boatsAvailableList ...");
            }
            boatsAvailableList.setBoatStatusData(
                boatStatus.getBoats(BoatStatusRecord.STATUS_AVAILABLE, true), logbook,
                "<" + International.getString("anderes Boot") + ">");
            if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
              Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                  + ") - setting boatsAvailableList - done");
            }
          } else {
            if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
              Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                  + ") - setting personsAvailableList ...");
            }
            Persons persons = boatStatus.getProject().getPersons(false);
            personsAvailableList.setPersonStatusData(
                persons.getAllPersons(System.currentTimeMillis(), false, false), "<"
                    + International.getString("andere Person") + ">");
            if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
              Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                  + ") - setting personsAvailableList - done");
            }
          }

          if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
            Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                + ") - setting boatsOnTheWaterList and boatsNotAvailableList ...");
          }
          boatsOnTheWaterList.setBoatStatusData(
              boatStatus.getBoats(BoatStatusRecord.STATUS_ONTHEWATER, true), logbook, null);
          boatsNotAvailableList.setBoatStatusData(
              boatStatus.getBoats(BoatStatusRecord.STATUS_NOTAVAILABLE, true), logbook, null);
          if (Logger.isTraceOn(Logger.TT_GUI, 9)) {
            Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
                + ") - setting boatsOnTheWaterList and boatsNotAvailableList - done");
          }
        }
      }

      /*
       * Dimension dim = boatsAvailableScrollPane.getSize();
       * boatsAvailableScrollPane.setPreferredSize(dim); // to make sure boatsAvailableScrollPane is
       * not resized when toggled between persons and boats boatsAvailableScrollPane.setSize(dim);
       * // to make sure boatsAvailableScrollPane is not resized when toggled between persons and
       * boats
       */

      if (!toggleAvailableBoatsToPersons.isSelected()) {
        statusLabelSetText(International.getString("Kein Boot ausgewählt."));
      } else {
        statusLabelSetText(International.getString("Keine Person ausgewählt."));
      }
      clearAllPopups();
      boatsAvailableList.clearSelection();
      personsAvailableList.clearSelection();
      boatsOnTheWaterList.clearSelection();
      boatsNotAvailableList.clearSelection();
      if (boatsAvailableList.isFocusOwner()) {
        boatsAvailableList.setSelectedIndex(0);
      } else if (personsAvailableList.isFocusOwner()) {
        personsAvailableList.setSelectedIndex(0);
      } else if (boatsOnTheWaterList.isFocusOwner()) {
        boatsOnTheWaterList.setSelectedIndex(0);
      } else if (boatsNotAvailableList.isFocusOwner()) {
        boatsNotAvailableList.setSelectedIndex(0);
      } else {
        if (!toggleAvailableBoatsToPersons.isSelected()) {
          boatsAvailableList.requestFocus();
          boatsAvailableList.setSelectedIndex(0);
          updateTopInfoText("Boote frisch sortiert!", "Brote frisch gestrichen!");
          updateGuiZentralesLogo(Daten.efaConfig.getValueEfaDirekt_vereinsLogo());
        } else {
          personsAvailableList.requestFocus();
          personsAvailableList.setSelectedIndex(0);
        }
      }
      if (Logger.isTraceOn(Logger.TT_GUI, 8)) {
        Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "updateBoatLists(" + listChanged
            + ") - done");
      }
    } catch (Exception e) {
      Logger.logwarn(e);
    } finally {
      inUpdateBoatList = false;
    }
  }

  // ========================================================================================================================================
  // Callbacks and Events
  // ========================================================================================================================================
  public void setUnreadMessages(boolean admin, boolean boatmaintenance) {
    String iconName = "action_admin.png";
    if (admin && boatmaintenance) {
      iconName = "action_admin_mailAdminBoat.png";
    } else if (admin) {
      iconName = "action_admin_mailAdmin.png";
    } else if (boatmaintenance) {
      iconName = "action_admin_mailBoat.png";
    }
    adminButton.setIcon(getIcon(iconName));
  }

  public synchronized void exitOnLowMemory(String detector, boolean immediate) {
    largeChunkOfMemory = null;
    Logger.log(Logger.ERROR, Logger.MSG_ERR_EXITLOWMEMORY,
        International.getMessage(
            "Der Arbeitsspeicher wird knapp [{detector}]: "
                + "efa versucht {jetzt} einen Neustart ...",
            detector,
            (immediate ? International.getString("jetzt").toUpperCase()
                : International.getString("jetzt"))));
    if (immediate) {
      this.cancel(null, EFA_EXIT_REASON_OOME, null, true);
    } else {
      EfaExitFrame.exitEfa(International.getString("Neustart wegen knappen Arbeitsspeichers"),
          true, EFA_EXIT_REASON_OOME);
    }
  }

  private int getListIdFromItem(IItemType item) {
    try {
      int listID = 0;
      if (item == boatsAvailableList) {
        listID = 1;
      }
      if (item == personsAvailableList) {
        listID = 1;
      }
      if (item == boatsOnTheWaterList) {
        listID = 2;
      }
      if (item == boatsNotAvailableList) {
        listID = 3;
      }
      return listID;
    } catch (Exception e) {
      return 0;
    }
  }

  @Override
  public void itemListenerAction(IItemType item, AWTEvent event) {
    int listID = 0;
    ItemTypeBoatstatusList aMainList = null;
    try {
      listID = getListIdFromItem(item);
      aMainList = (ItemTypeBoatstatusList) item;
    } catch (Exception eignore) {
    }
    if (listID == 0) {
      return;
    }
    
    ActionEvent actionEvent = null;
    try {
      actionEvent = (ActionEvent) event;
    } catch (Exception eignore) {
    }
    if (actionEvent != null) {
      String actionCommand = actionEvent.getActionCommand();
      if (actionCommand.equals(EfaMouseListener.EVENT_MOUSECLICKED_1x)) {
        showBoatStatus(listID, aMainList, 1);
      }
      if (actionCommand.equals(EfaMouseListener.EVENT_MOUSECLICKED_2x)) {
        showBoatStatus(listID, aMainList, 1);
        boatListDoubleClick(listID, aMainList);
      }
      if (actionCommand.equals(EfaMouseListener.EVENT_POPUP)) {
        showBoatStatus(listID, aMainList, 1);
      }
      // Popup clicked?
      if (actionCommand.startsWith(EfaMouseListener.EVENT_POPUP_CLICKED)) {
        int subCmd = EfaUtil.stringFindInt(actionCommand, -1);
        if (subCmd >= 0) {
          ItemTypeBoatstatusList.BoatListItem blitem = getSelectedListItem(aMainList);
          if (blitem != null) {
            processListAction(blitem, subCmd);
          }
        }
      }
    }

    KeyEvent keyEvent = null;
    try {
      keyEvent = (KeyEvent) event;
    } catch (Exception eignore) {
    }
    if (keyEvent != null) {
      clearAllPopups();
      int keyCode = keyEvent.getKeyCode();
      if (keyCode == KeyEvent.VK_ENTER || 
          keyCode == KeyEvent.VK_SPACE) {
        // don't react if space was pressed as part of an incremental search string
        if (keyCode == KeyEvent.VK_SPACE) {
          String s = ((ItemTypeList) aMainList).getIncrementalSearchString();
          if (s != null && s.length() > 0 && !s.startsWith(" ")) {
            return;
          }
        }
        boatListDoubleClick(listID, aMainList);
        return;
      }
      int direction = keyCode == 38 ? -1 : 1;
      showBoatStatus(listID, aMainList, direction);
      return;
    }

    FocusEvent focusEvent = null;
    try {
      focusEvent = (FocusEvent) event;
    } catch (Exception eignore) {
    }
    if (focusEvent != null) {
      if (focusEvent.getID() == FocusEvent.FOCUS_GAINED) {
        showBoatStatus(listID, aMainList, 1);
      }
    }
  }

  private void processListAction(ItemTypeBoatstatusList.BoatListItem blitem, int action) {
    switch (action) {
      case ACTIONID_STARTSESSION: // start session
        actionStartSession(blitem);
        break;
      case ACTIONID_FINISHSESSION: // finish session
        actionFinishSession();
        break;
      case ACTIONID_LATEENTRY: // late entry
        actionLateEntry();
        break;
      case ACTIONID_STARTSESSIONCORRECT: // change session
        actionStartSessionCorrect();
        break;
      case ACTIONID_ABORTSESSION: // cancel session
        actionAbortSession();
        break;
      case ACTIONID_BOATRESERVATIONS: // boat reservations
        actionBoatReservations();
        break;
      case ACTIONID_BOATDAMAGES: // boat damages
        actionBoatDamages();
        break;
      case ACTIONID_BOATINFOS: // boat infos
        actionBoatInfos();
        break;
      case ACTIONID_LASTBOATUSAGE: // boat damages
        actionLastBoatUsage();
        break;
    }
  }

  void boatListDoubleClick(int listnr, ItemTypeBoatstatusList list) {
    if (list == null || list.getSelectedIndex() < 0) {
      return;
    }
    clearAllPopups();

    ItemTypeBoatstatusList.BoatListItem blitem = getSelectedListItem(list);
    DataRecord r = null;
    String name = null;
    if (blitem != null) {
      if (blitem.boatStatus != null) {
        r = blitem.boatStatus;
        name = blitem.boatStatus.getBoatNameAsString(System.currentTimeMillis());
      } else if (blitem.person != null) {
        r = blitem.person;
        name = blitem.person.getQualifiedName();
      }
    }
    if (r == null) {
      return;
    }

    if (listnr == 1
        && Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()
        && toggleAvailableBoatsToPersons.isSelected()) {
      actionStartSession(blitem);
      return;
    }

    String[] actions = getListActions(listnr, r);
    if (actions == null || actions.length == 0) {
      return;
    }
    String[] myActions = new String[actions.length + 1];
    for (int i = 0; i < actions.length; i++) {
      myActions[i] = actions[i].substring(1);
    }
    myActions[myActions.length - 1] = International.getString("Nichts");
    int selection = Dialog.auswahlDialog(International.getString("Boot") + " " + name,
        International.getMessage("Was möchtest Du mit dem Boot {boat} machen?", name),
        myActions);
    if (selection >= 0 && selection < actions.length) {
      processListAction(blitem, EfaUtil.string2int(actions[selection].substring(0, 1), -1));
    }
  }

  public void clearAllPopups() {
    boatsAvailableList.clearPopup();
    personsAvailableList.clearPopup();
    boatsOnTheWaterList.clearPopup();
    boatsNotAvailableList.clearPopup();
  }

  void showBoatStatus(int listnr, ItemTypeBoatstatusList list, int direction) {
    if (Daten.project == null) {
      return;
    }
    try {
      String name = null;

      ItemTypeBoatstatusList.BoatListItem item = null;
      while (item == null) {
        try {
          item = getSelectedListItem(list);
          if (list != personsAvailableList) {
            name = item.text;
          } else {
            name = item.person.getQualifiedName();
          }
        } catch (Exception e) {
          // just to be sure
          Logger.logwarn(e);
        }
        if (name == null || name.startsWith("---")) {
          item = null;
          try {
            int i = list.getSelectedIndex() + direction;
            if (i < 0) {
              i = 1; 
              // i<0 kann nur erreicht werden,
              // wenn vorher i=0 und direction=-1; dann darf
              // nicht auf i=0 gesprungen werden,
              // da wir dort herkommen, sondern auf i=1
              direction = 1;
            }
            if (i >= list.size()) {
              return;
            }
            list.setSelectedIndex(i);
          } catch (Exception e) {
            Logger.logwarn(e);
          }
        }
      }

      if (list != personsAvailableList) {
        if (item.boatStatus != null) {
          BoatStatusRecord status = boatStatus.getBoatStatus(item.boatStatus.getBoatId());
          BoatRecord boat = Daten.project.getBoats(false).getBoat(item.boatStatus.getBoatId(),
              System.currentTimeMillis());
          String fileName = Daten.efaConfig.getValueEfaDirekt_vereinsLogo(); // alternatives Bild
          if (boat != null) {
            name = boat.getQualifiedName();
            fileName = Daten.efaImagesDirectory + boat.getName() + ".jpg"; // Bild vom Boot
          } else if (status != null) {
            name = status.getBoatText();
          } else {
            name = International.getString("anderes oder fremdes Boot");
          }

          String text = "";
          if (status != null) {
            String s = BoatStatusRecord.getStatusDescription(status.getCurrentStatus());
            if (s != null) {
              text = s;
            }
            s = status.getComment();
            if (s != null && s.length() > 0) {
              text = s;
              // if a comment is set, then *don't* display
              // the current status, but only the comment.
            }
          }
          String bootstyp = "";
          String rudererlaubnis = "";
          if (listnr == 1) {
            if (boat != null) {
              bootstyp = " (" + boat.getDetailedBoatType(boat.getVariantIndex(item.boatVariant))
                  + ")";
              String groups = boat.getAllowedGroupsAsNameString(System.currentTimeMillis());
              if (groups.length() > 0) {
                rudererlaubnis = (rudererlaubnis.length() > 0 ? rudererlaubnis + ", "
                    : "; " + International.getMessage("nur für {something}", groups));
              }
            }
          }
          statusLabelSetText(name + ": " + text + bootstyp + rudererlaubnis);
          
          StringBuilder s;
          // mit F12 Text ganz ausschalten. Schalter Daten.efaConfig.
          if (isToggleF12LangtextF12()) {
            // Text parametrisieren: ausgewählte Felder
            s = getInfoString(item, 
                true, // showBootName
                false, // showOwner
                true, // showPurchase
                false, // showSort
                false, // showType
                false, // showPaddle
                false, // showCox
                true, // showWeight
                true, // showGroups
                true, // showOrt
                false, // showStatus
                true, // showDamages
                true); // showLastUsage
          } else {
            // Text parametrisieren: nur Bootsname
            s = getInfoString(item, true, false, false, false, false,
                false, false, false, false, false, false, false, false);
          }

          // Textlänge reduzieren
          String infoStringForDisplay = s.substring(6).toString();
          double cutLength = 4 * Daten.efaConfig.getMinimumDauerFuerKulanz();
          cutLength += 24;
          // Bootshaus = 4*43-2 = 170 | 168 = 48 * 3 + 24 | 216
          if (s.length() > cutLength) {
            infoStringForDisplay = s.substring(6, (int)cutLength - 4) + "...";          
          }
          updateTopInfoText("<html>" + infoStringForDisplay + "</html>");
          updateGuiZentralesLogo(fileName); // Boot-Foto
        } else {
          // nach klick auf "<anderes Boot>"
          // gut // reset to Vereinslogo // Standard-Bild
          updateTopInfoText("Bitte Boot auswählen...");
          updateGuiZentralesLogo(Daten.efaConfig.getValueEfaDirekt_vereinsLogo()); 
          statusLabelSetText(International.getString("anderes oder fremdes Boot"));
        }
      } else {
        // nach klick auf Personenliste
        updateTopInfoText(name);
        statusLabelSetText(name);
      }

      if (listnr != 1) {
        boatsAvailableList.clearSelection();
        personsAvailableList.clearSelection();
      }
      if (listnr != 2) {
        boatsOnTheWaterList.clearSelection();
      }
      if (listnr != 3) {
        boatsNotAvailableList.clearSelection();
      }
    } catch (Exception e) {
      Logger.logwarn(e);
    }
  }

  private void toggleAvailableBoats_actionPerformed(ActionEvent e) {
    if (!Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons() &&
        !Daten.efaConfig.isValueEfaDirekt_listAllowToggleSortByCategories()) {
      return;
    }
    iniGuiListNames();
    if (Logger.isTraceOn(Logger.TT_GUI, 8)) {
      Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI, "toggleAvailableBoats_actionPerformed()");
    }
    try {
      SortingBy newSortingWunsch = getSelectedSorting();
      Logger.log(Logger.INFO, Logger.MSG_DEBUG_STATISTICS, "NewSortingBy: Wechsel auf " + newSortingWunsch);
      if (newSortingWunsch != null) {
        if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()) {
          boatsAvailablePanel.remove(personsAvailableList.getPanel());
        }
        Dimension size = boatsAvailablePanel.getPreferredSize();
        boatsAvailablePanel.remove(boatsAvailableList.getPanel()); // abf YES
        boatsAvailableList.displayOnGui(this, boatsAvailablePanel, BorderLayout.CENTER, newSortingWunsch);
        boatsAvailablePanel.setPreferredSize(size);

        size = boatsNotAvailablePanel.getPreferredSize();
        boatsNotAvailablePanel.remove(boatsNotAvailableList.getPanel());
        boatsNotAvailablePanel.remove(boatsOnTheWaterList.getPanel());
        boatsOnTheWaterList.displayOnGui(this, boatsNotAvailablePanel, BorderLayout.CENTER, newSortingWunsch);
        boatsNotAvailableList.displayOnGui(this, boatsNotAvailablePanel, BorderLayout.SOUTH, newSortingWunsch);
        boatsNotAvailablePanel.setPreferredSize(size);
      } else {
        Dimension size = boatsAvailablePanel.getPreferredSize();
        boatsAvailablePanel.remove(boatsAvailableList.getPanel());
        personsAvailableList.displayOnGui(this, boatsAvailablePanel, BorderLayout.CENTER);
        boatsAvailablePanel.setPreferredSize(size);
      }
      alive();
      validate();
      updateBoatLists(true);
    } catch (Exception ee) {
      Logger.logwarn(ee);
    }
    if (Logger.isTraceOn(Logger.TT_GUI, 8)) {
      Logger.log(Logger.DEBUG, Logger.MSG_GUI_DEBUGGUI,
          "toggleAvailableBoats_actionPerformed() - done");
    }
  }

  private SortingBy getSelectedSorting() {
    if (toggleAvailableBoatsToBoats.isSelected()) {
      return (SortingBy.EfaSorting);
    } else if (toggleAvailableBoatsToDescriptionOrt.isSelected()) {
      return (SortingBy.DescriptionOrt);
    } else if (toggleAvailableBoatsToBoatType.isSelected()) {
      return (SortingBy.BoatType);
    } else if (toggleAvailableBoatsToBoatNameAffix.isSelected()) {
      return (SortingBy.BoatNameAffix);
    } else if (toggleAvailableBoatsToOwner.isSelected()) {
      return (SortingBy.Owner);
    } else if (toggleAvailableBoatsToPaddelArt.isSelected()) {
      return (SortingBy.PaddelArt);
    } else if (toggleAvailableBoatsToSteuermann.isSelected()) {
      return (SortingBy.Steuermann);
    } else {
      return null;
    }
  }

  private ItemTypeBoatstatusList.BoatListItem getSelectedListItem(ItemTypeBoatstatusList list) {
    ItemTypeBoatstatusList.BoatListItem item = null;
    if (list != null) {
      item = list.getSelectedBoatListItem();
    }
    if (item != null && item.boatStatus != null) {
      // update saved boat status in GUI list with current boat status from Persistence
      item.boatStatus = boatStatus.getBoatStatus(item.boatStatus.getBoatId());
      if (item.boatStatus != null) {
        item.boat = item.boatStatus.getBoatRecord(System.currentTimeMillis());
      }
      if (item.boatStatus == null) {
        String s = International.getMessage("Boot {boat} nicht in der Statusliste gefunden!",
            item.text);
        Dialog.error(s);
        Logger.log(Logger.ERROR, Logger.MSG_ERR_BOATNOTFOUNDINSTATUS, s);
      }
    }
    return item;
  }

  private ItemTypeBoatstatusList.BoatListItem getSelectedListItem() {
    ItemTypeBoatstatusList.BoatListItem item = null;
    if (Daten.efaConfig.isValueEfaDirekt_listAllowToggleBoatsPersons()
        && toggleAvailableBoatsToPersons.isSelected()) {
      if (item == null && personsAvailableList != null) {
        item = getSelectedListItem(personsAvailableList);
      }
    } else {
      if (item == null && boatsAvailableList != null) {
        item = getSelectedListItem(boatsAvailableList);
      }
    }
    if (item == null && boatsOnTheWaterList != null) {
      item = getSelectedListItem(boatsOnTheWaterList);
    }
    if (item == null && boatsNotAvailableList != null) {
      item = getSelectedListItem(boatsNotAvailableList);
    }
    return item;
  }

  // mode bestimmt die Art der Checks
  // mode==1 - alle Checks durchführen
  // mode==2 - nur solche Checks durchführen, bei denen es egal ist, ob das Boot aus der Liste
  // direkt ausgewählt wurde
  // oder manuell über <anders Boot> eingegeben wurde. Der Aufruf von checkFahrtbeginnFuerBoot mit
  // mode==2
  // erfolgt aus EfaFrame.java.
  boolean checkStartSessionForBoat(ItemTypeBoatstatusList.BoatListItem item,
      String entryNo, int mode) {
    if (item == null || item.boatStatus == null || item.boatStatus.getCurrentStatus() == null) {
      return true;
    }
    BoatStatusRecord boatStatus = item.boatStatus;
    if (boatStatus.getCurrentStatus().equals(BoatStatusRecord.STATUS_ONTHEWATER)) {
      if (entryNo != null && boatStatus.getEntryNo() != null &&
          !entryNo.equals(boatStatus.getEntryNo().toString())) {
        // Dieses Boot ist bereits auf Fahrt in einem anderen Fahrtenbucheintrag!
        Dialog.error(International.getMessage("Das Boot {boat} ist bereits unterwegs.",
            boatStatus.getBoatText()));
        return false;
      }

      if (mode == 1) {
        actionStartSessionCorrect();
        return false;
      }
    }
    if (mode == 3) {
      return true;
    }
    if (boatStatus.getCurrentStatus().equals(BoatStatusRecord.STATUS_NOTAVAILABLE)) {
      if (Dialog.yesNoCancelDialog(
          International.getString("Boot gesperrt"),
          International.getMessage("Das Boot {boat} ist laut Liste nicht verfügbar.",
              boatStatus.getBoatText())
              + NEWLINE
              + (boatStatus.getComment() != null ? International.getString("Bemerkung") + ": "
                  + boatStatus.getComment() + NEWLINE : "")
              + NEWLINE
              + International.getString("Möchtest Du trotzdem das Boot benutzen?")) != Dialog.YES) {
        return false;
      }
    }

    if (frageBenutzerWennUeberschneidungReservierung(boatStatus)) {
      return false;
    }

    if (!checkBoatDamage(item,
        International.getString("Möchtest Du trotzdem das Boot benutzen?"))) {
      return false;
    }

    return true;
  }

  boolean frageBenutzerWennUeberschneidungReservierung(BoatStatusRecord boatStatus) {
    long now = System.currentTimeMillis();
    BoatReservations boatReservations = Daten.project.getBoatReservations(false);
    BoatReservationRecord[] reservations = null;
    // look for reservations for this boat within 3 hours
    if (boatStatus != null && boatStatus.getBoatId() != null) {
      int minuten = Daten.efaConfig.getValueEfaDirekt_resLookAheadTime();
      reservations = boatReservations.getBoatReservations(boatStatus.getBoatId(), now, minuten);
    }
    // look for more reservations ahead
    if (reservations == null || reservations.length == 0) {
      // plus die Reservierungen, die meine jetzige lange Fahrt überschneiden!
      // aber nur falls überhaupt der Dialog gemacht und schon Eingaben zur Endzeit
      DataTypeDate enddatum = efaBaseFrame.enddate.getDate();
      if (!enddatum.isSet()) {
        enddatum = DataTypeDate.today();
      }
      DataTypeTime endzeit = efaBaseFrame.endtime.getTime();
      if (!endzeit.isSet()) {
        endzeit = DataTypeTime.now();
      }
      long diffBisEndeMillis = enddatum.getTimestamp(endzeit) - now;
      if (diffBisEndeMillis > 0) {
        long minuten = diffBisEndeMillis / 1000 / 60;
        reservations = boatReservations.getBoatReservations(boatStatus.getBoatId(), now, minuten);
      }
    }
    // ask user how to proceed
    if (reservations != null && reservations.length > 0) {
      int minuten = Daten.efaConfig.getValueEfaDirekt_resLookAheadTime();
      long validInMinutes = reservations[0].getReservationValidInMinutes(now, minuten);
      int ergebnisYesNoCancelDialog = Dialog.yesNoCancelDialog(
          International.getString("Boot bereits reserviert"),
          getWarnmeldung(boatStatus.getBoatText(), reservations[0].getPersonAsName(),
              validInMinutes)
              + NEWLINE
              + getGrundMeldung(reservations[0].getReason())
              + getTelefonMeldung(reservations[0].getContact())
              + NEWLINE
              + International.getMessage("Die Reservierung liegt {from_time_to_time} vor.",
                  reservations[0].getReservationTimeDescription())
              + NEWLINE
              + International.getString("Möchtest Du jetzt trotzdem mit diesem Boot starten?"));
      if (ergebnisYesNoCancelDialog != Dialog.YES) {
        return true;
      }
    }
    return false;
  }

  private String getWarnmeldung(String boatStatusText, String reservationPersonName,
      long validInMinutes) {
    return International.getMessage(
        "Das Boot {boat} ist {currently_or_in_x_minutes} für {name} reserviert.",
        boatStatusText,
        (validInMinutes <= 0 ? International.getString("zur Zeit")
            : International.getMessage("in {x} Minuten", validInMinutes)),
        reservationPersonName);
  }

  private String getGrundMeldung(String grund) {
    return (grund != null && grund.length() > 0)
        ? International.getString("Grund") + ": " + grund + NEWLINE
        : "";
  }

  private String getTelefonMeldung(String telefonNr) {
    return (telefonNr != null && telefonNr.length() > 0)
        ? International.getString("Telefon für Rückfragen") + ": " + telefonNr + NEWLINE
        : "";
  }

  boolean checkBoatDamage(ItemTypeBoatstatusList.BoatListItem item, String questionText) {
    UUID boatId = null;
    if (item.boatStatus != null && item.boatStatus.getBoatId() != null) {
      boatId = item.boatStatus.getBoatId();
    }
    if (item.boat != null && item.boat.getId() != null) {
      boatId = item.boat.getId();
    }
    BoatDamageRecord[] damages = null;
    if (boatId != null) {
      BoatDamages boatDamages = Daten.project.getBoatDamages(false);
      damages = boatDamages.getBoatDamages(boatId, true, true);
    }
    if (damages != null && damages.length > 0) {
      String bodyText = "";
      if (damages.length == 1) {
        bodyText = International.getMessage
          ("Für das Boot {boat} wurde folgender Bootsschaden gemeldet:",
          item.boatStatus.getBoatText()) + NEWLINE;
      } else {
        bodyText = International.getMessage
            ("Für das Boot {boat} wurden folgende Bootsschäden gemeldet:",
            item.boatStatus.getBoatText()) + NEWLINE;
        }
      for (int i = 0; i < damages.length; i++) {
        bodyText +=  (i+1) + ") \"" + damages[i].getDescription() + "\"" + NEWLINE
          + International.getString("Schwere des Schadens") + ": "
          + damages[i].getSeverityDescription() + NEWLINE;
      }
      bodyText +=  NEWLINE + questionText;
      int yesNoAnswer = Dialog.yesNoDialog(International.getString("Bootsschaden gemeldet"), bodyText);
      if (yesNoAnswer != Dialog.YES) {
        return false;
      }
    }
    return true;
  }

  boolean checkBoatStatusOnTheWater(ItemTypeBoatstatusList.BoatListItem item) {
    if (item == null || item.boatStatus == null || item.boatStatus.getCurrentStatus() == null
        || !item.boatStatus.getCurrentStatus().equals(BoatStatusRecord.STATUS_ONTHEWATER)) {
      item = null;
    }

    if (item == null) {
      Dialog.error(International.getMessage(
          "Bitte wähle zuerst {from_the_right_list} ein Boot aus, welches unterwegs ist!",
          (Daten.efaConfig.getValueEfaDirekt_wafaRegattaBooteAufFahrtNichtVerfuegbar()
              ? International.getString("aus einer der rechten Listen")
              : International.getString("aus der rechten oberen Liste"))));
      boatsAvailableList.requestFocus();
      efaBoathouseBackgroundTask.interrupt(); // Falls requestFocus nicht funktioniert hat, setzt
      // der Thread ihn richtig!
      return false;
    }

    if (item.boatStatus.getEntryNo() == null || !item.boatStatus.getEntryNo().isSet()) {
      // keine LfdNr eingetragen: Das kann passieren, wenn der Admin den Status der Bootes manuell
      // geändert hat!
      String s = International.getMessage(
          "Es gibt keine offene Fahrt im Fahrtenbuch mit dem Boot {boat}.",
          item.boatStatus.getBoatText())
          + " " + International.getString("Die Fahrt kann nicht beendet werden.");
      Logger.log(
          Logger.ERROR,
          Logger.MSG_ERR_NOLOGENTRYFORBOAT,
          s + " "
              + International.getString("Bitte korrigiere den Status des Bootes im Admin-Modus."));
      Dialog.error(s);
      return false;
    }

    if (logbook.getLogbookRecord(item.boatStatus.getEntryNo()) == null) {
      String s = International.getMessage(
          "Es gibt keine offene Fahrt im Fahrtenbuch mit dem Boot {boat} und LfdNr {lfdnr}.",
          item.boatStatus.getBoatText(), (item.boatStatus.getEntryNo() != null ? item.boatStatus
              .getEntryNo().toString() : "null"))
          + " " + International.getString("Die Fahrt kann nicht beendet werden.");
      Logger.log(
          Logger.ERROR,
          Logger.MSG_ERR_NOLOGENTRYFORBOAT,
          s + " "
              + International.getString("Bitte korrigiere den Status des Bootes im Admin-Modus."));
      Dialog.error(s);
      return false;
    }

    return true;
  }

  void prepareEfaBaseFrame() {
    efaBaseFrame = new EfaBaseFrame(this, EfaBaseFrame.MODE_BOATHOUSE);
    efaBaseFrame.prepareDialog();
    efaBaseFrame.setFixedLocationAndSize();
  }

  void showEfaBaseFrame(int mode, ItemTypeBoatstatusList.BoatListItem action) {
    if (!Daten.isWriteModeMitSchluessel()) {
      Dialog.meldung("Nur für Vereinsmitglieder",
          "Bitte erst Bootshausschlüssel nach rechts drehen.");
      return;
    }
    for (IWidget w : widgets) {
      w.runWidgetWarnings(mode, true, null);
    }
    if (efaBaseFrame == null) {
      prepareEfaBaseFrame();
    }
    action.mode = mode;
    if (!efaBaseFrame.setDataForBoathouseAction(action, logbook)) {
      return;
    }
    if (mode != EfaBaseFrame.MODE_BOATHOUSE_ABORT) {
      efaBaseFrame.efaBoathouseShowEfaFrame();
    } else {
      efaBaseFrame.finishBoathouseAction(true);
    }
  }

  // Callback from EfaBaseFrame
  void showEfaBoathouseFrame(ItemTypeBoatstatusList.BoatListItem efaBoathouseAction,
      LogbookRecord r) {
    bringFrameToFront();
    updateBoatLists(true); // must be explicitly called here! only
    // efaBoathouseBackgroundTask.interrupt() is NOT sufficient.
    efaBoathouseBackgroundTask.interrupt();
    if (focusItem != null) {
      focusItem.requestFocus();
    }
    alive();
    if (efaBoathouseAction != null &&
        (efaBoathouseAction.mode == EfaBaseFrame.MODE_BOATHOUSE_FINISH
         || efaBoathouseAction.mode == EfaBaseFrame.MODE_BOATHOUSE_ABORT)) {
      if (!boatStatus.areBoatsOutOnTheWater() &&
          Daten.efaConfig.getValueEfaBoathouseShowLastFromWaterNotification() &&
          Daten.efaConfig.getValueNotificationWindowTimeout() > 0) {
        String txt = Daten.efaConfig.getValueEfaBoathouseShowLastFromWaterNotificationText();
        if (txt == null || txt.length() == 0) {
          txt = International.getString("Alle Boote sind zurück.") + "<br>" +
              International.getString("Bitte schließe die Hallentore.");
        }
        NotificationDialog dlg = new NotificationDialog(this,
            txt,
            BaseDialog.BIGIMAGE_CLOSEDOORS,
            "ffffff", "ff0000", Daten.efaConfig.getValueNotificationWindowTimeout());
        dlg.showDialog();
      }
    }
    if (efaBoathouseAction != null) {
      for (IWidget w : widgets) {
        w.runWidgetWarnings(efaBoathouseAction.mode, false, r);
      }
    }
  }

  void actionStartSession(ItemTypeBoatstatusList.BoatListItem item) {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    if (item == null) {
      item = getSelectedListItem();
    }
    if (item == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      efaBoathouseBackgroundTask.interrupt(); // Falls requestFocus nicht funktioniert hat, setzt
      // der Thread ihn richtig!
      return;
    }

    if (!checkStartSessionForBoat(item, null, 1)) {
      return;
    }

    showEfaBaseFrame(EfaBaseFrame.MODE_BOATHOUSE_START, item);
  }

  void actionStartSessionCorrect() {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (!checkBoatStatusOnTheWater(item)) {
      return;
    }

    showEfaBaseFrame(EfaBaseFrame.MODE_BOATHOUSE_START_CORRECT, item);
  }

  void actionFinishSession() {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (!checkBoatStatusOnTheWater(item)) {
      return;
    }

    showEfaBaseFrame(EfaBaseFrame.MODE_BOATHOUSE_FINISH, item);
  }

  void actionAbortSession() {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (!checkBoatStatusOnTheWater(item)) {
      return;
    }

    BoatRecord boat = item.boat;

    int auswahlDialogAnswer = Dialog.auswahlDialog(International.getString("Fahrt abbrechen"),
        International.getMessage("Die Fahrt des Bootes {boat} sollte nur abgebrochen werden, "
            + "wenn sie nie stattgefunden hat.", item.boatStatus.getBoatText())
            + NEWLINE
            + International.getString("Was möchtest Du tun?"),
        International.getString("Fahrt abbrechen"),
        International.getString("Fahrt abbrechen")
            + " (" + International.getString("Bootsschaden") + ")",
        International.getString("Nichts"));
    switch (auswahlDialogAnswer) {
      case 0:
        break;
      case 1:
        if (boat != null) {
          BoatDamageEditDialog.newBoatDamage(this, boat);
        }
        break;
      case 2:
        return;
    }

    showEfaBaseFrame(EfaBaseFrame.MODE_BOATHOUSE_ABORT, item);
  }

  void actionLateEntry() {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      efaBoathouseBackgroundTask.interrupt(); // Falls requestFocus nicht funktioniert hat, setzt
      // der Thread ihn richtig!
      return;
    }

    showEfaBaseFrame(EfaBaseFrame.MODE_BOATHOUSE_LATEENTRY, item);
  }

  void actionBoatReservations() {
    if (!Daten.efaConfig.getValueEfaDirekt_butBootsreservierungen().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      efaBoathouseBackgroundTask.interrupt(); // Falls requestFocus nicht funktioniert hat, setzt
      // der Thread ihn richtig!
      return;
    }
    if (item.boat != null && item.boat.getOwner() != null && item.boat.getOwner().length() > 0 &&
        !Daten.efaConfig.getValueMembersMayReservePrivateBoats()) {
      Dialog.error(International.getString("Privatboote dürfen nicht reserviert werden!"));
      return;
    }

    if (item.boat != null
        && !checkBoatDamage(item,
            International.getString("Möchtest Du trotzdem das Boot reservieren?"))) {
      return;
    }
    if (item.boat == null || item.boatStatus == null || item.boatStatus.getUnknownBoat()
        || item.boatStatus.getBoatId() == null) {
      BoatReservationListDialog dlg = new BoatReservationListDialog(this, null,
          Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservieren(),
          Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservierenZyklisch(),
          Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservierungenEditieren());
      dlg.showDialog();
      // Falls requestFocus nicht funktioniert hat, setzt der Thread ihn richtig!
      efaBoathouseBackgroundTask.interrupt();
      return;
    }

    BoatReservationListDialog dlg = new BoatReservationListDialog(this,
        item.boatStatus.getBoatId(),
        Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservieren(),
        Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservierenZyklisch(),
        Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenReservierungenEditieren());
    dlg.showDialog();
    efaBoathouseBackgroundTask.interrupt();
  }

  void actionClubwork() {
    if (!Daten.efaConfig.getValueEfaDirekt_butVereinsarbeit().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }
    ClubworkListDialog dlg = new ClubworkListDialog(this, null);
    dlg.showDialog();
  }

  void actionBoatDamages() {
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    if (!Daten.isWriteModeMitSchluessel()) {
      Dialog.meldung("Nur für Vereinsmitglieder",
          "Bitte erst Bootshausschlüssel nach rechts drehen.");
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null || item.boat == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      // Falls requestFocus nicht funktioniert hat, setzt der Thread ihn richtig!
      efaBoathouseBackgroundTask.interrupt();
      return;
    }
    BoatDamageEditDialog.newBoatDamage(this, item.boat, null, null);
    efaBoathouseBackgroundTask.interrupt();
  }

  void actionShowLogbook() {
    if (!Daten.efaConfig.getValueEfaDirekt_butFahrtenbuchAnzeigen().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }
    ShowLogbookDialog dlg = new ShowLogbookDialog(this, logbook);
    dlg.showDialog();
  }

  void actionStatistics() {
    if (!Daten.efaConfig.getValueEfaDirekt_butStatistikErstellen().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }
    StatisticsListDialog dlg = new StatisticsListDialog(this, null);
    dlg.showDialog();
  }

  void actionMessageToAdmin() {
    if (!Daten.efaConfig.getValueEfaDirekt_butNachrichtAnAdmin().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    if (Daten.project == null) {
      return;
    }

    MessageRecord msg = null;
    try {
      msg = Daten.project.getMessages(false).createMessageRecord();
      msg.setTo(Daten.efaConfig.getValueEfaDirekt_bnrMsgToAdminDefaultRecipient());
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    if (msg != null) {
      MessageEditDialog dlg = new MessageEditDialog(this, msg, true, null);
      dlg.showDialog();
      efaBoathouseBackgroundTask.interrupt();
    }
  }

  void actionAdminMode() {
    alive();
    clearAllPopups();

    // Prüfe, ob bereits ein Admin-Modus-Fenster offen ist
    Stack<?> s = Dialog.frameStack;
    boolean adminOnStack = false;
    try {
      for (int i = 0; i < s.size(); i++) {
        if (s.elementAt(i).getClass().getName().equals(AdminDialog.class.getCanonicalName())) {
          adminOnStack = true;
        }
      }
    } catch (Exception ee) {
    }
    if (adminOnStack) {
      Dialog.error(International.getString("Es ist bereits ein Admin-Fenster geöffnet."));
      return;
    }

    AdminRecord admin = AdminLoginDialog.login(this, International.getString("Admin-Modus"));
    if (admin == null) {
      return;
    }
    Daten.checkRegister();
    AdminDialog dlg = new AdminDialog(this, admin);
    try {
      Daten.applMode = Daten.APPL_MODE_ADMIN;
      dlg.showDialog();
      efaBoathouseBackgroundTask.interrupt();
      if (Daten.project != null) {
        logbook = Daten.project.getCurrentLogbook();
        boatStatus = Daten.project.getBoatStatus(false);
      }
      updateBoatLists(true);
      updateGuiElements();
    } finally {
      Daten.applMode = Daten.APPL_MODE_NORMAL;
    }
  }

  void actionSpecial() {
    if (!Daten.efaConfig.getValueEfaDirekt_butSpezial().getValueShow()) {
      return;
    }
    alive();
    clearAllPopups();
    String cmd = Daten.efaConfig.getValueEfaDirekt_butSpezialCmd().trim();
    if (cmd.length() > 0) {
      try {
        if (cmd.toLowerCase().startsWith("browser:")) {
          BrowserDialog.openInternalBrowser(_parent, cmd.substring(8));
        } else {
          Runtime.getRuntime().exec(cmd);
        }
      } catch (Exception ee) {
        LogString.logWarning_cantExecCommand(cmd, International.getString("Spezial-Button"),
            ee.toString());
      }
    } else {
      Dialog.error(International.getString("Kein Kommando für diesen Button konfiguriert!"));
    }
  }

  void actionBoatInfosOrEfaAbout() {
    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null || item.boat == null) {
      //efaButton_actionPerformed(null);
    } else {
      actionBoatInfos();
    }
  }
  
  void actionBoatInfos() {
    alive();
    clearAllPopups();
    if (Daten.project == null || logbook == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null || item.boat == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      // Falls requestFocus nicht funktioniert hat, setzt der Thread ihn richtig!
      efaBoathouseBackgroundTask.interrupt();
      return;
    }
    try {
      StringBuilder s = getInfoString(item, true, true, true, true, 
          true, true, true, true, true, true, true, true, true);
      Dialog.bootsinfoDialog(item.boat.getName(), s.toString());
    } catch (Exception e) {
      Logger.logdebug(e);
      Dialog.error(e.getMessage());
    }
  }

  private StringBuilder getInfoString(ItemTypeBoatstatusList.BoatListItem item,
      boolean showBootName,
      boolean showOwner,
      boolean showPurchase,
      boolean showSort,
      boolean showType,
      boolean showPaddle,
      boolean showCox,
      boolean showWeight,
      boolean showGroups,
      boolean showOrt,
      boolean showStatus,
      boolean showDamages,
      boolean showLastUsage) {
    StringBuilder s = new StringBuilder();
    if (item == null || item.boat == null) {
      return s;
    }
    String showString;
    if (showBootName) {
      s.append(International.getString("Boot") + ": " + item.boat.getQualifiedName() + NEWLINE);
    }
    if (showOwner) {
      showString = item.boat.getAsText(BoatRecord.OWNER);
      showString = showString == null ? "[todo]" : showString;      
      s.append("Eigentümer: " + showString + NEWLINE);
    }
    if (showPurchase) {
      showString = item.boat.getAsText(BoatRecord.PURCHASEDATE);
      showString = showString == null ? "[Jahr]" : showString;
      s.append("Anschaffung: " + showString + NEWLINE);
    }
    if (showSort) {
      showString = item.boat.getAsText(BoatRecord.TYPESEATS).split(";")[0];
      s.append("EFA-Sortierung: " + showString + NEWLINE);
    }
    if (showType) {
      showString = item.boat.getAsText(BoatRecord.TYPETYPE).split(";")[0];
      if (!showString.contentEquals("andere")) {
        s.append("Bootstyp: " + showString + NEWLINE);
      }
    }
    if (showPaddle) {
      showString = item.boat.getAsText(BoatRecord.TYPERIGGING).split(";")[0];
      if (!showString.contentEquals("andere")) {
        s.append("Paddel-Art: " + showString + NEWLINE);
      }
    }
    if (showCox) {
      showString = item.boat.getAsText(BoatRecord.TYPECOXING).split(";")[0];
      if (!showString.contentEquals("andere")) {
        s.append("Coxing: " + showString + NEWLINE);
      }
    }
    if (showWeight) {
      if (item.boat.getMaxCrewWeight() > 0) {
        s.append(International.getString("Maximales Mannschaftsgewicht") + ": " +
            item.boat.getMaxCrewWeight() + NEWLINE);
      }
    }
    if (showGroups) {
      String groups = item.boat.getAllowedGroupsAsNameString(System.currentTimeMillis());
      if (groups.length() > 0) {
        s.append(NEWLINE + International.getString("Gruppen, die dieses Boot benutzen dürfen")
            + ":" + NEWLINE + groups);
      }
    }

    s.append(NEWLINE);
    if (showOrt) {
      showString = item.boat.getAsText(BoatRecord.TYPEDESCRIPTION).split(";")[0];
      if (showString.length() > 1) {
        showString = "\"" + showString + "\"";
        s.append("Ort am Isekai: " + showString + NEWLINE);
      }
    }
    if (showStatus) {
      String currentStatus = item.boatStatus.getCurrentStatus();
      String statusDescription = BoatStatusRecord.getStatusDescription(currentStatus);
      s.append("Bootsstatus: " + statusDescription + NEWLINE);
    }
    if (showDamages) {
      BoatDamages boatDamages = Daten.project.getBoatDamages(false);
      BoatDamageRecord[] damages = boatDamages.getBoatDamages(item.boat.getId(), true, true);
      s.append((damages == null ? "Keine Schäden!" : "Anz.Schäden: " + damages.length) + NEWLINE);
      if (damages != null) {
        for (BoatDamageRecord damage : damages) {
          if (!damage.getFixed()) {
            s.append("Bootsschaden: " + damage.getDescription() + NEWLINE);
          }
        }
      }
    }
    s.append(NEWLINE);
    if (showLastUsage) {
      String lastUsage = International.getString("Letzte Benutzung") + ": " + NEWLINE;
      lastUsage += getLastBoatUsageString(item.boat.getId());
      lastUsage = EfaUtil.replace(lastUsage, ", null?", "", true);
      lastUsage = EfaUtil.replace(lastUsage, "null?, ", "", true);
      lastUsage = EfaUtil.replace(lastUsage, "null?", "", true);
      lastUsage = EfaUtil.replace(lastUsage, "; (efa:", NEWLINE + "(efa hat", true);
      lastUsage = EfaUtil.replace(lastUsage, ") (efa:", ")" + NEWLINE + "(efa hat", true);
      lastUsage = EfaUtil.replace(lastUsage, " (efa:", NEWLINE + "(efa hat", true);
      lastUsage = EfaUtil.replace(lastUsage, ": ", NEWLINE, true);
      if (lastUsage.contains("Keinen Eintrag gefunden")) {
        lastUsage = "Dies Jahr noch nie offiziell ausgeliehen.";
      }
      s.append(lastUsage + NEWLINE);
    }
    return s;
  }

  void actionLastBoatUsage() {
    alive();
    clearAllPopups();
    if (Daten.project == null || logbook == null) {
      return;
    }

    ItemTypeBoatstatusList.BoatListItem item = getSelectedListItem();
    if (item == null || item.boat == null || item.boat.getId() == null) {
      Dialog.error(International.getString("Bitte wähle zuerst ein Boot aus!"));
      boatListRequestFocus(1);
      // Falls requestFocus nicht funktioniert hat, setzt der Thread ihn richtig!
      efaBoathouseBackgroundTask.interrupt();
      return;
    }
    String lastBoatUsageString = getLastBoatUsageString(item.boat.getId());
    lastBoatUsageString = International.getString("Letzte Benutzung") + ": "
        + NEWLINE
        + lastBoatUsageString;
    Dialog.infoDialog(lastBoatUsageString);
  }

  private String getLastBoatUsageString(UUID id) {
    String latestString = "";
    LogbookRecord latest = logbook.getLastBoatUsage(id, null);
    if (latest != null) {
      latestString = latest.getLogbookRecordAsStringDescription();
    } else {
      latestString = International.getString("Keinen Eintrag gefunden!");
    }
    return latestString;
  }

  void efaButton_actionPerformed(ActionEvent e) {
    alive();
    clearAllPopups();
    EfaAboutDialog dlg = new EfaAboutDialog(this);
    dlg.showDialog();
  }

  void hilfeButton_actionPerformed(ActionEvent e) {
    clearAllPopups();
    Help.showHelp(getHelpTopics());
  }

  public void lockEfa() {
    if (Daten.efaConfig == null) {
      return;
    }
    if (isLocked) {
      return; // don't lock twice
    }
    isLocked = true;
    Daten.efaConfig.setValueEfaDirekt_lockEfaFromDatum(new DataTypeDate()); // damit nach Entsperren
    // nicht wiederholt
    // gelockt wird
    Daten.efaConfig.setValueEfaDirekt_lockEfaFromZeit(new DataTypeTime()); // damit nach Entsperren
    // nicht wiederholt
    // gelockt wird
    Daten.efaConfig.setValueEfaDirekt_locked(true);

    try {
      final EfaBoathouseFrame frame = this;
      new Thread() {

        @Override
        public void run() {
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
          }
          String endeDerSperrung = (Daten.efaConfig.getValueEfaDirekt_lockEfaUntilDatum().isSet()
              ? " "
                  + International.getString("Ende der Sperrung")
                  + ": "
                  + Daten.efaConfig.getValueEfaDirekt_lockEfaUntilDatum().toString()
                  + (Daten.efaConfig.getValueEfaDirekt_lockEfaUntilZeit().isSet() ? " "
                      + Daten.efaConfig.getValueEfaDirekt_lockEfaUntilZeit().toString() : "")
              : "");

          String html = Daten.efaConfig.getValueEfaDirekt_lockEfaShowHtml();
          if (html == null || !EfaUtil.canOpenFile(html)) {
            html = Daten.efaTmpDirectory + "locked.html";
            try {
              String img = EfaUtil.saveImage("efaLocked.png", "png",
                  Daten.efaTmpDirectory, true, false, false);
              BufferedWriter f = new BufferedWriter(new FileWriter(html));
              f.write("<html><body><h1 align=\"center\">"
                  + International.getString("efa ist für die Benutzung gesperrt")
                  + "</h1>" + NEWLINE);
              f.write("<p align=\"center\"><img src=\"" + img
                  + "\" align=\"center\" width=\"256\" height=\"256\"></p>" + NEWLINE);
              f.write("<p align=\"center\">"
                  + International.getString(
                          "efa wurde vom Administrator vorübergehend für die Benutzung gesperrt.")
                  + "</p>" + NEWLINE);
              if (endeDerSperrung.length() > 0) {
                f.write("<p align=\"center\">" + endeDerSperrung + "</p>" + NEWLINE);
              }
              f.write("</body></html>" + NEWLINE);
              f.close();
            } catch (Exception e) {
              EfaUtil.foo();
            }
          }
          de.nmichael.efa.gui.BrowserDialog browser = new BrowserDialog(frame, "file:" + html);
          browser.setLocked(frame, true);
          if (Daten.efaConfig.getValueEfaDirekt_lockEfaVollbild()) {
            browser.setFullScreenMode(true);
          } else {
            browser.setSize(650, 420);
            browser.setPreferredSize(new Dimension(650, 420));
          }
          browser.setModal(true);
          Dialog.setDlgLocation(browser, frame);
          browser.setClosingTimeout(10); // nur um Lock-Ende zu überwachen
          Logger.log(
              Logger.INFO,
              Logger.MSG_EVT_LOCKED,
              International.getString(
                      "efa wurde vom Administrator vorübergehend für die Benutzung gesperrt.")
                  + endeDerSperrung);
          browser.showDialog();
        }
      }.start();
    } catch (Exception ee) {
    }
  }

  public void setUnlocked() {
    isLocked = false;
  }

}
