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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

import javax.swing.DefaultFocusManager;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import de.nmichael.efa.Daten;
import de.nmichael.efa.core.AdminTask;
import de.nmichael.efa.core.config.AdminRecord;
import de.nmichael.efa.core.config.EfaConfig;
import de.nmichael.efa.core.config.EfaTypes;
import de.nmichael.efa.core.items.IItemListener;
import de.nmichael.efa.core.items.IItemType;
import de.nmichael.efa.core.items.ItemTypeBoatstatusList;
import de.nmichael.efa.core.items.ItemTypeButton;
import de.nmichael.efa.core.items.ItemTypeDate;
import de.nmichael.efa.core.items.ItemTypeDistance;
import de.nmichael.efa.core.items.ItemTypeHashtable;
import de.nmichael.efa.core.items.ItemTypeLabel;
import de.nmichael.efa.core.items.ItemTypeLabelValue;
import de.nmichael.efa.core.items.ItemTypeString;
import de.nmichael.efa.core.items.ItemTypeStringAutoComplete;
import de.nmichael.efa.core.items.ItemTypeStringList;
import de.nmichael.efa.core.items.ItemTypeStringPhone;
import de.nmichael.efa.core.items.ItemTypeTime;
import de.nmichael.efa.data.BoatRecord;
import de.nmichael.efa.data.BoatStatus;
import de.nmichael.efa.data.BoatStatusRecord;
import de.nmichael.efa.data.Clubwork;
import de.nmichael.efa.data.CrewRecord;
import de.nmichael.efa.data.Crews;
import de.nmichael.efa.data.DestinationRecord;
import de.nmichael.efa.data.GroupRecord;
import de.nmichael.efa.data.Groups;
import de.nmichael.efa.data.Logbook;
import de.nmichael.efa.data.LogbookRecord;
import de.nmichael.efa.data.MessageRecord;
import de.nmichael.efa.data.PersonRecord;
import de.nmichael.efa.data.Persons;
import de.nmichael.efa.data.Project;
import de.nmichael.efa.data.ProjectRecord;
import de.nmichael.efa.data.SessionGroupRecord;
import de.nmichael.efa.data.WatersRecord;
import de.nmichael.efa.data.storage.DataKeyIterator;
import de.nmichael.efa.data.storage.IDataAccess;
import de.nmichael.efa.data.types.DataTypeDate;
import de.nmichael.efa.data.types.DataTypeDistance;
import de.nmichael.efa.data.types.DataTypeIntString;
import de.nmichael.efa.data.types.DataTypeList;
import de.nmichael.efa.data.types.DataTypeTime;
import de.nmichael.efa.ex.EfaException;
import de.nmichael.efa.gui.dataedit.BoatDamageEditDialog;
import de.nmichael.efa.gui.dataedit.BoatEditDialog;
import de.nmichael.efa.gui.dataedit.DestinationEditDialog;
import de.nmichael.efa.gui.dataedit.PersonEditDialog;
import de.nmichael.efa.gui.dataedit.SessionGroupListDialog;
import de.nmichael.efa.gui.util.AutoCompleteList;
import de.nmichael.efa.gui.util.EfaMenuButton;
import de.nmichael.efa.util.Dialog;
import de.nmichael.efa.util.EfaUtil;
import de.nmichael.efa.util.International;
import de.nmichael.efa.util.LogString;
import de.nmichael.efa.util.Logger;
import de.nmichael.efa.util.Mnemonics;

public class EfaBaseFrame extends BaseDialog implements IItemListener {

  private static final long serialVersionUID = 1L;
  private static final String NEWLINE = "\n";
  public static final int MODE_BASE = 0;
  public static final int MODE_BOATHOUSE = 1;
  public static final int MODE_BOATHOUSE_START = 2;
  public static final int MODE_BOATHOUSE_START_CORRECT = 3;
  public static final int MODE_BOATHOUSE_FINISH = 4;
  public static final int MODE_BOATHOUSE_LATEENTRY = 5;
  public static final int MODE_BOATHOUSE_ABORT = 6;
  public static final int MODE_ADMIN = 7;
  public static final int MODE_ADMIN_SESSIONS = 8;
  public static final String GUIITEM_ADDITIONALWATERS = "GUIITEM_ADDITIONALWATERS";
  private int mode;

  // =========================================================================
  // GUI Elements
  // =========================================================================

  // Menu Bar
  JMenuBar menuBar = new JMenuBar();

  // Toolbar
  JToolBar toolBar = new JToolBar();
  JButton toolBar_firstButton = new JButton();
  JButton toolBar_prevButton = new JButton();
  JButton toolBar_nextButton = new JButton();
  JButton toolBar_lastButton = new JButton();
  JButton toolBar_newButton = new JButton();
  // @remove JButton toolBar_insertButton = new JButton();
  JButton toolBar_deleteButton = new JButton();
  JButton toolBar_searchButton = new JButton();
  JTextField toolBar_goToEntry = new JTextField();
  JButton toolBar_goToEntryNext = new JButton();

  // Data Fields
  ItemTypeString entryno;
  ItemTypeLabel opensession;
  ItemTypeButton closesessionButton;
  ItemTypeDate date;
  ItemTypeDate enddate;
  ItemTypeStringAutoComplete boat;
  ItemTypeStringList boatvariant;
  ItemTypeStringAutoComplete cox;
  ItemTypeStringAutoComplete[] crew;
  ItemTypeStringList boatcaptain;
  ItemTypeTime starttime;
  ItemTypeTime endtime;
  ItemTypeLabel starttimeInfoLabel;
  ItemTypeLabel endtimeInfoLabel;
  ItemTypeStringAutoComplete destination;
  ItemTypeString destinationInfo;
  ItemTypeStringAutoComplete waters;
  ItemTypeDistance distance;
  ItemTypeStringPhone phoneNr;
  ItemTypeString comments;
  ItemTypeStringAutoComplete sessiongroup;

  // Supplementary Elements
  ItemTypeButton remainingCrewUpButton;
  ItemTypeButton remainingCrewDownButton;
  ItemTypeButton boatDamageButton;
  ItemTypeButton boatNotCleanedButton;
  ItemTypeButton saveButton;
  JLabel infoLabel = new JLabel();
  String KEYACTION_F3;
  String KEYACTION_F4;

  // Internal Data Structures
  Logbook logbook; // this logbook
  AdminRecord admin;
  AdminRecord remoteAdmin;
  DataKeyIterator iterator; // iterator for this logbook
  LogbookRecord currentRecord; // aktDatensatz = aktuell angezeigter Datensatz
  LogbookRecord referenceRecord; // refDatensatz = Referenz-Datensatz
  // (zuletzt angezeigter Datensatz, wenn neuer erstellt wird)
  long logbookValidFrom = 0;
  long logbookInvalidFrom = 0;

  boolean isNewRecord; // neuerDatensatz
  // = ob akt. Datensatz ein neuer Datensatz, oder ein bearbeiteter ist
  boolean isInsertedRecord; // neuerDatensatz_einf = ob der neue Datensatz eingefügt wird
  // (dann beim Hinzufügen keine Warnung wegen kleiner Lfd. Nr.!)
  int entryNoForNewEntry = -1; // lfdNrForNewEntry = LfdNr (zzgl. 1),
  // die für den nächsten per "Neu" erzeugten Datensatz verwendet werden soll;
  // wenn <0, dann wird "last+1" verwendet

  BoatRecord currentBoat; // aktBoot = aktuelle Bootsdaten
  // (um nächstes Eingabefeld zu ermitteln)
  String currentBoatTypeSeats; // boat type for currentBoat
  String currentBoatTypeCoxing; // boat type for currentBoat
  int currentBoatNumberOfSeats; // boat type for currentBoa
  String lastDestination = ""; // zum Vergleichen, ob Ziel geändert wurde
  int crewRangeSelection = 0; // mannschAuswahl = 0: 1-8 sichtbar; 1: 9-16 sichtbar; 2: 17-24
  // sichtbar
  String crew1defaultText = null; // mannsch1_label_defaultText = der Standardtext, den das Label
  // "Mannschaft 1: " normalerweise haben soll (wenn es nicht für
  // Einer auf "Name: " gesetzt wird)
  IItemType lastFocusedItem;
  private volatile boolean _inUpdateBoatVariant = false;
  AutoCompleteList autoCompleteListBoats = new AutoCompleteList();
  AutoCompleteList autoCompleteListPersons = new AutoCompleteList();
  AutoCompleteList autoCompleteListDestinations = new AutoCompleteList();
  AutoCompleteList autoCompleteListWaters = new AutoCompleteList();
  EfaBaseFrameFocusManager efaBaseFrameFocusManager;
  String _jumpToEntryNo;
  boolean showEditWaters = false; // allow to edit waters per session even if disabled in EfaConfig

  // Internal Data Structures for EfaBoathouse
  EfaBoathouseFrame efaBoathouseFrame;
  AdminDialog adminDialog;
  ItemTypeBoatstatusList.BoatListItem efaBoathouseAction;
  BoatStatusRecord correctSessionLastBoatStatus;
  int positionX, positionY; // Position des Frames, wenn aus efaDirekt aufgerufen

  public EfaBaseFrame(int mode) {
    super((JFrame) null, Daten.EFA_LONGNAME);
    this.mode = mode;
  }

  public EfaBaseFrame(JDialog parent, int mode) {
    super(parent, Daten.EFA_LONGNAME, null);
    this.mode = mode;
  }

  public EfaBaseFrame(JDialog parent, int mode,
      AdminRecord admin, Logbook logbook, String entryNo) {
    super(parent, Daten.EFA_LONGNAME, null);
    this.mode = mode;
    this.logbook = logbook;
    this.admin = admin;
    this._jumpToEntryNo = entryNo;
    this.showEditWaters = true;
  }

  public EfaBaseFrame(EfaBoathouseFrame efaBoathouseFrame, int mode) {
    // unsichtbar Motte:x=546 y=160 Isekai:x=94 y=665
    super(efaBoathouseFrame, "base " + Daten.EFA_LONGNAME, null);
    this.efaBoathouseFrame = efaBoathouseFrame;
    this.mode = mode;
  }

  private int getMode() {
    return mode;
  }

  private void setMode(int mode) {
    this.mode = mode;
  }

  private boolean isModeBase() {
    return mode == MODE_BASE;
  }

  private boolean isModeBaseOrAdmin() {
    return mode == MODE_BASE
        || mode == MODE_ADMIN;
  }

  private boolean isModeBoathouse() {
    return mode == MODE_BOATHOUSE
        || mode == MODE_BOATHOUSE_START
        || mode == MODE_BOATHOUSE_START_CORRECT
        || mode == MODE_BOATHOUSE_FINISH
        || mode == MODE_BOATHOUSE_LATEENTRY
        || mode == MODE_BOATHOUSE_ABORT;
  }

  private boolean isModeStartOrStartCorrect() {
    return mode == MODE_BOATHOUSE_START
        || mode == MODE_BOATHOUSE_START_CORRECT;
  }

  private boolean isModeFinishOrLateEntry() {
    return mode == MODE_BOATHOUSE_FINISH
        || mode == MODE_BOATHOUSE_LATEENTRY;
  }

  private boolean isModeAdmin() {
    return mode == MODE_ADMIN
        || mode == MODE_ADMIN_SESSIONS;
  }

  private AdminRecord getAdmin() {
    return (remoteAdmin != null ? remoteAdmin : admin);
  }

  private void checkRemoteAdmin() {
    boolean error = false;
    if (remoteAdmin == null && Daten.project != null &&
        Daten.project.getProjectStorageType() == IDataAccess.TYPE_EFA_REMOTE) {
      error = true;
      Dialog.error(International.getString("Login fehlgeschlagen") + ".\n"
          + International.getString(
          "Bitte überprüfe Remote-Adminnamen und Paßwort in den Projekteinstellungen."));
    }
    if (remoteAdmin != null && !remoteAdmin.isAllowedRemoteAccess()) {
      error = true;
      EfaMenuButton.insufficientRights(remoteAdmin,
          International.getString("Remote-Zugriff über efaRemote"));
    }
    if (error) {
      Daten.project = null;
      remoteAdmin = null;
    } else {
      // enable some local functions for the admin
      if (remoteAdmin != null) {
        remoteAdmin.setAllowedShowLogfile((admin != null && admin.isAllowedShowLogfile()));
      }
    }
  }

  @Override
  public void _keyAction(ActionEvent evt) {
    if (evt.getActionCommand().equals(KEYACTION_F3)) {
      SearchLogbookDialog.search();
    }
    super._keyAction(evt);
  }

  @Override
  public void keyAction(ActionEvent evt) {
    _keyAction(evt);
  }

  public void packFrame(String method) {
    pack();
  }

  public void setFixedLocationAndSize() {
    Dialog.setDlgLocation(this, null);
    Dimension dlgSize = getSize();
    setMinimumSize(dlgSize);
    setMaximumSize(dlgSize);
  }

  @Override
  protected void iniDialog() {
    if (isModeBase() && admin == null) {
      iniAdmin();
    }
    iniGuiBase();
    if (isModeBase()) {
      iniGuiMenu();
    }
    if (isModeBaseOrAdmin()) {
      iniGuiToolbar();
    }
    iniGuiMain();
    iniGuiRemaining();
    iniApplication();
    if (isModeBase()) {
      Daten.iniSplashScreen(false);
    }
  }

  public void setAdmin(AdminRecord admin) {
    this.admin = admin;
  }

  private void iniAdmin() {
    if (admin == null) {
      admin = AdminLoginDialog.login(null, Daten.APPLNAME_EFA,
          true, Daten.efaConfig.getValueLastProjectEfaBase());
      if (admin == null || !admin.isAllowedAdministerProjectLogbook()) {
        if (admin != null) {
          EfaMenuButton.insufficientRights(admin,
              International.getString("Projekte und Fahrtenbücher administrieren"));
        }
        super.cancel();
        Daten.haltProgram(Daten.HALT_ADMINLOGIN);
      }
      String p = AdminLoginDialog.getLastSelectedProject();
      if (p != null && p.length() > 0) {
        Daten.efaConfig.setValueLastProjectEfaBase(p);
      }
      AdminTask.startAdminTask(admin, this);
    }
    Daten.checkRegister();
  }

  private void iniGuiBase() {
    setIconImage(Toolkit.getDefaultToolkit().createImage(
        EfaBaseFrame.class.getResource("/de/nmichael/efa/img/efa_icon.png")));
    mainPanel.setLayout(new BorderLayout());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }

      @Override
      public void windowIconified(WindowEvent e) {
        this_windowIconified(e);
      }
    });
    KEYACTION_F3 = addKeyAction("F3");
    KEYACTION_F4 = addKeyAction("F4");
  }

  private void iniGuiMenu() {
    if (!isModeBase()) {
      return;
    }
    Vector<EfaMenuButton> menuButtons = EfaMenuButton.getAllMenuButtons(
        getAdmin(), false);
    String lastMenuName = null;
    menuBar.removeAll();
    JMenu menu = null;
    for (EfaMenuButton menuButton : menuButtons) {
      if (!menuButton.getMenuName().equals(lastMenuName)) {
        if (menu != null) {
          menuBar.add(menu);
        }
        // New Menu
        menu = new JMenu();
        Mnemonics.setButton(this, menu, menuButton.getMenuText());
        lastMenuName = menuButton.getMenuName();
      }
      if (menuButton.getButtonName().equals(EfaMenuButton.SEPARATOR)) {
        menu.addSeparator();
      } else {
        JMenuItem menuItem = new JMenuItem();
        Mnemonics.setMenuButton(this, menuItem, menuButton.getButtonText());
        if (menuButton.getIcon() != null) {
          setIcon(menuItem, menuButton.getIcon());
        }
        menuItem.setActionCommand(menuButton.getButtonName());
        menuItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            menuActionPerformed(e);
          }
        });
        menu.add(menuItem);
      }
    }
    menuBar.add(menu);
    setJMenuBar(menuBar);
  }

  private void iniGuiToolbar() {
    boolean useText = false;

    toolBar_firstButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_firstButton,
        (useText ? International.getStringWithMnemonic("Erster") : null),
        BaseDialog.IMAGE_FIRST);
    toolBar_firstButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        navigateInLogbook(Integer.MIN_VALUE);
      }
    });

    toolBar_prevButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_prevButton,
        (useText ? International.getStringWithMnemonic("Vorheriger") : null),
        BaseDialog.IMAGE_PREV);
    toolBar_prevButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        navigateInLogbook(-1);
      }
    });

    toolBar_nextButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_nextButton,
        (useText ? International.getStringWithMnemonic("Nächster") : null),
        BaseDialog.IMAGE_NEXT);
    toolBar_nextButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        navigateInLogbook(1);
      }
    });

    toolBar_lastButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_lastButton,
        (useText ? International.getStringWithMnemonic("Letzter") : null),
        BaseDialog.IMAGE_LAST);
    toolBar_lastButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        navigateInLogbook(Integer.MAX_VALUE);
      }
    });

    toolBar_newButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_newButton,
        (useText ? International.getStringWithMnemonic("Neu") : null),
        BaseDialog.IMAGE_ADD);
    toolBar_newButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        createNewRecord(false);
      }
    });

    /*
     * @remove toolBar_insertButton.setMargin(new Insets(2, 3, 2, 3)); Mnemonics.setButton(this,
     * toolBar_insertButton, International.getStringWithMnemonic("Einfügen"));
     * toolBar_insertButton.addActionListener(new java.awt.event.ActionListener() { public void
     * actionPerformed(ActionEvent e) { createNewRecord(true); } });
     */

    toolBar_deleteButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_deleteButton,
        (useText ? International.getStringWithMnemonic("Löschen") : null),
        BaseDialog.IMAGE_DELETE);
    toolBar_deleteButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        deleteRecord();
      }
    });

    toolBar_searchButton.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_searchButton,
        (useText ? International.getStringWithMnemonic("Suchen") : null),
        BaseDialog.IMAGE_SEARCH);
    toolBar_searchButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchLogbook();
      }
    });

    Dialog.setPreferredSize(toolBar_goToEntry, 30, 19);
    toolBar_goToEntry.addKeyListener(new java.awt.event.KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        goToEntry(toolBar_goToEntry.getText().trim(), false);
      }
    });
    toolBar_goToEntry.addFocusListener(new java.awt.event.FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        String s = toolBar_goToEntry.getText().trim();
        if (s.length() > 0 && Character.isDigit(s.charAt(0))) {
          toolBar_goToEntry.setText("");
        }
      }
    });

    toolBar_goToEntryNext.setMargin(new Insets(2, 3, 2, 3));
    Mnemonics.setButton(this, toolBar_goToEntryNext,
        null,
        BaseDialog.IMAGE_SEARCHNEXT);
    toolBar_goToEntryNext.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        goToEntry(toolBar_goToEntry.getText().trim(), true);
      }
    });

    toolBar.add(toolBar_firstButton, null);
    toolBar.add(toolBar_prevButton, null);
    toolBar.add(toolBar_nextButton, null);
    toolBar.add(toolBar_lastButton, null);
    JLabel toolBar_spaceLabel1 = new JLabel();
    toolBar_spaceLabel1.setText("  ");
    toolBar.add(toolBar_spaceLabel1, null);
    toolBar.add(toolBar_newButton, null);
    // @remove toolBar.add(toolBar_insertButton, null);
    toolBar.add(toolBar_deleteButton, null);
    JLabel toolBar_spaceLabel2 = new JLabel();
    toolBar_spaceLabel2.setText("  ");
    toolBar.add(toolBar_spaceLabel2, null);
    toolBar.add(toolBar_searchButton, null);
    JLabel toolBar_goToEntryLabel = new JLabel();
    toolBar_goToEntryLabel.setText("  \u21B7 "); // \u00BB \u23E9
    toolBar.add(toolBar_goToEntryLabel, null);
    toolBar.add(toolBar_goToEntry, null);
    toolBar.add(toolBar_goToEntryNext, null);

    mainPanel.add(toolBar, BorderLayout.NORTH);
  }

  private void iniGuiMain() {
    JPanel mainInputPanel = new JPanel();
    mainInputPanel.setLayout(new GridBagLayout());
    mainPanel.add(mainInputPanel, BorderLayout.CENTER);

    // EntryNo
    entryno = new ItemTypeString(LogbookRecord.ENTRYID, "", IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Lfd. Nr."));
    entryno.setAllowedRegex("[0-9]+[A-Z]?");
    entryno.setToUpperCase(true);
    entryno.setNotNull(true);
    entryno.setFieldSize(200, 19);
    entryno.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    entryno.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    entryno.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    entryno.displayOnGui(this, mainInputPanel, 0, 0);
    entryno.registerItemListener(this);
    entryno.setVisible(isModeBaseOrAdmin());

    // Open Session
    opensession = new ItemTypeLabel(LogbookRecord.OPEN, IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Fahrt offen"));
    opensession.setColor(Color.red);
    opensession.setFieldGrid(4, 1, -1, -1);
    opensession.displayOnGui(this, mainInputPanel, 5, 0);
    opensession.setVisible(false);

    closesessionButton = new ItemTypeButton("CloseSessionButton", IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Fahrt offen") + " - " +
            International.getStringWithMnemonic("jetzt beenden"));
    closesessionButton.setColor(Color.red);
    closesessionButton.setFieldSize(50, 17);
    closesessionButton.setFieldGrid(4, 1, -1, -1);
    closesessionButton.displayOnGui(this, mainInputPanel, 5, 0);
    closesessionButton.registerItemListener(this);
    closesessionButton.setVisible(false);

    // Date
    date = new ItemTypeDate(LogbookRecord.DATE, new DataTypeDate(), IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Datum"));
    date.showWeekday(true);
    date.setFieldSize(100, 19);
    date.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    date.setFieldGrid(1, GridBagConstraints.WEST, GridBagConstraints.NONE);
    date.setWeekdayGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    date.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    date.displayOnGui(this, mainInputPanel, 0, 3);
    date.registerItemListener(this);

    // End Date
    enddate = new ItemTypeDate(LogbookRecord.ENDDATE, new DataTypeDate(), IItemType.TYPE_PUBLIC,
        null, International.getStringWithMnemonic("bis"));
    enddate.setMustBeAfter(date, false);
    enddate.showWeekday(true);
    enddate.setFieldSize(100, 19);
    enddate.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    enddate.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    enddate.setWeekdayGrid(1, GridBagConstraints.WEST, GridBagConstraints.NONE);
    enddate.showOptional(true);
    if (isModeBoathouse()) {
      enddate.setOptionalButtonText("+ " + International.getString("Enddatum"));
    }
    enddate.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    enddate.displayOnGui(this, mainInputPanel, 4, 3);
    enddate.registerItemListener(this);
    if (isModeBoathouse() && !Daten.efaConfig.getValueAllowEnterEndDate()) {
      enddate.setVisible(false);
    }

    // Boat
    boat = new ItemTypeStringAutoComplete(LogbookRecord.BOATNAME, "", IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Boot"), true);
    boat.setFieldSize(200, 19);
    boat.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    boat.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    boat.setAutoCompleteData(autoCompleteListBoats);
    boat.setChecks(true, true);
    boat.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    boat.displayOnGui(this, mainInputPanel, 0, 1);
    boat.registerItemListener(this);

    // Boat Variant
    boatvariant = new ItemTypeStringList(LogbookRecord.BOATVARIANT, "", null, null,
        IItemType.TYPE_PUBLIC, null, International.getString("Variante"));
    boatvariant.setFieldSize(80, 17);
    boatvariant.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    boatvariant.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    boatvariant.setBackgroundColorWhenFocused(Daten.efaConfig
        .getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    boatvariant.displayOnGui(this, mainInputPanel, 0, 2);
    boatvariant.registerItemListener(this);

    // Cox = Steuermann
    cox = new ItemTypeStringAutoComplete(LogbookRecord.COXNAME, "", IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Paddel-Kapitänin"), true);
    cox.setFieldSize(200, 19);
    cox.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    cox.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    cox.setAutoCompleteData(autoCompleteListPersons);
    cox.setChecks(true, true);
    cox.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    cox.displayOnGui(this, mainInputPanel, 0, 9);
    cox.registerItemListener(this);

    // Crew
    crew = new ItemTypeStringAutoComplete[LogbookRecord.CREW_MAX];
    for (int i = 1; i <= crew.length; i++) {
      int j = i - 1;
      boolean left = ((j / 4) % 2) == 0;
      crew[j] = new ItemTypeStringAutoComplete(LogbookRecord.getCrewFieldNameName(i), "",
          IItemType.TYPE_PUBLIC, null,
          (i == 1 ? International.getString("Mannschaft") + " " : (i < 10 ? "  " : ""))
              + Integer.toString(i),
          true);
      crew[j].setPadding((left ? 0 : 10), 0, 0, 0);
      crew[j].setFieldSize(200, 19);
      crew[j].setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
      crew[j].setFieldGrid((left ? 2 : 3), GridBagConstraints.WEST, GridBagConstraints.NONE);
      crew[j].setAutoCompleteData(autoCompleteListPersons);
      crew[j].setChecks(true, true);
      crew[j].setBackgroundColorWhenFocused(
          Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
      crew[j].displayOnGui(this, mainInputPanel, (left ? 0 : 4), 6 + j % 4);
      crew[j].setVisible(j < 8);
      crew[j].registerItemListener(this);
    }
    crew1defaultText = crew[0].getDescription();

    // Boat Captain = Obmann
    boatcaptain = new ItemTypeStringList(LogbookRecord.BOATCAPTAIN, "",
        LogbookRecord.getBoatCaptainValues(), LogbookRecord.getBoatCaptainDisplay(),
        IItemType.TYPE_PUBLIC, null, International.getString("Obmann"));
    boatcaptain.setFieldSize(80, 17);
    boatcaptain.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    boatcaptain.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    boatcaptain.setBackgroundColorWhenFocused(Daten.efaConfig
        .getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    boatcaptain.displayOnGui(this, mainInputPanel, 5, 9);
    boatcaptain.registerItemListener(this);
    if (isModeBoathouse()) {
      boatcaptain.setVisible(Daten.efaConfig.getValueShowObmann());
      // TODO 2020-03-27 abf brauchen wir boatcaptain?
      boatcaptain.setVisible(false);
    }

    // Phone Number
    phoneNr = new ItemTypeStringPhone(LogbookRecord.CONTACT, null, IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Telefon/Handy"));
    phoneNr.setSehrStreng(true);
    phoneNr.setFieldSize(200, 19);
    phoneNr.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    phoneNr.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    phoneNr.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    phoneNr.displayOnGui(this, mainInputPanel, 0, 10);
    phoneNr.registerItemListener(this);

    // StartTime
    starttime = new ItemTypeTime(LogbookRecord.STARTTIME, new DataTypeTime(),
        IItemType.TYPE_PUBLIC, null, International.getStringWithMnemonic("Abfahrt"));
    starttime.setFieldSize(200, 19);
    starttime.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    starttime.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    starttime.enableSeconds(false);
    starttime.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    starttime.displayOnGui(this, mainInputPanel, 0, 4);
    starttime.registerItemListener(this);

    // EndTime
    endtime = new ItemTypeTime(LogbookRecord.ENDTIME, new DataTypeTime(), IItemType.TYPE_PUBLIC,
        null, International.getStringWithMnemonic("Ankunft"));
    endtime.setFieldSize(200, 19);
    endtime.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    endtime.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    endtime.enableSeconds(false);
    endtime.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    endtime.displayOnGui(this, mainInputPanel, 0, 5);
    endtime.registerItemListener(this);

    starttimeInfoLabel = new ItemTypeLabel("GUIITEM_STARTTIME_INFOLABEL",
        IItemType.TYPE_PUBLIC, null, "");
    starttimeInfoLabel.setFieldGrid(5, GridBagConstraints.WEST, GridBagConstraints.NONE);
    starttimeInfoLabel.setVisible(false);
    starttimeInfoLabel.displayOnGui(this, mainInputPanel, 3, 4);
    endtimeInfoLabel = new ItemTypeLabel("GUIITEM_ENDTIME_INFOLABEL",
        IItemType.TYPE_PUBLIC, null, "");
    endtimeInfoLabel.setFieldGrid(5, GridBagConstraints.WEST, GridBagConstraints.NONE);
    endtimeInfoLabel.setVisible(false);
    endtimeInfoLabel.displayOnGui(this, mainInputPanel, 3, 5);

    // Destination
    destination = new ItemTypeStringAutoComplete(LogbookRecord.DESTINATIONNAME, "",
        IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Ziel") + "/" +
            International.getStringWithMnemonic("Strecke"),
        true);
    destination.setFieldSize(400, 19);
    destination.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    destination.setFieldGrid(7, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    destination.setAutoCompleteData(autoCompleteListDestinations);
    destination.setChecks(true, false);
    destination.setIgnoreEverythingAfter(DestinationRecord.DESTINATION_VARIANT_SEPARATOR);
    destination.setBackgroundColorWhenFocused(Daten.efaConfig
        .getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    destination.displayOnGui(this, mainInputPanel, 0, 11);
    destination.registerItemListener(this);
    destination.setVisible(isModeBaseOrAdmin());

    destinationInfo = new ItemTypeString("GUIITEM_DESTINATIONINFO", "",
        IItemType.TYPE_PUBLIC, null, International.getString("Gewässer"));
    destinationInfo.setFieldSize(400, 19);
    destinationInfo.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    destinationInfo.setFieldGrid(7, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    destinationInfo.displayOnGui(this, mainInputPanel, 0, 12);
    destinationInfo.setEditable(false);
    destinationInfo.setVisible(false);

    // Waters
    waters = new ItemTypeStringAutoComplete(GUIITEM_ADDITIONALWATERS, "", IItemType.TYPE_PUBLIC,
        null, International.getStringWithMnemonic("Gewässer"), true);
    waters.setFieldSize(400, 19);
    waters.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    waters.setFieldGrid(7, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    waters.setAutoCompleteData(autoCompleteListWaters);
    waters.setChecks(true, false);
    waters.setIgnoreEverythingAfter(LogbookRecord.WATERS_SEPARATORS);
    waters.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    waters.displayOnGui(this, mainInputPanel, 0, 13);
    waters.registerItemListener(this);
    waters.setVisible(isModeBaseOrAdmin());

    // Distance
    distance = new ItemTypeDistance(LogbookRecord.DISTANCE, null, IItemType.TYPE_PUBLIC, null,
        DataTypeDistance.getDefaultUnitName());
    distance.setFieldSize(200, 19);
    distance.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    distance.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    distance.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    distance.displayOnGui(this, mainInputPanel, 0, 14);
    distance.registerItemListener(this);
    distance.setVisible(false);

    // Comments
    comments = new ItemTypeString(LogbookRecord.COMMENTS, null, IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Bemerkungen"));
    comments.setFieldSize(400, 19);
    comments.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    comments.setFieldGrid(7, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    comments.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    comments.displayOnGui(this, mainInputPanel, 0, 15);
    comments.registerItemListener(this);

    // Session Group
    sessiongroup = new ItemTypeStringAutoComplete(LogbookRecord.SESSIONGROUPID,
        "", IItemType.TYPE_PUBLIC, null,
        International.getStringWithMnemonic("Fahrtgruppe"), true);
    sessiongroup.setFieldSize(200, 19);
    sessiongroup.setLabelGrid(1, GridBagConstraints.EAST, GridBagConstraints.NONE);
    sessiongroup.setFieldGrid(2, GridBagConstraints.WEST, GridBagConstraints.NONE);
    sessiongroup.setEditable(false);
    sessiongroup.displayOnGui(this, mainInputPanel, 0, 17);
    sessiongroup.registerItemListener(this);
    sessiongroup.setVisible(isModeBaseOrAdmin());
    sessiongroup.setVisible(false);

    // Further Fields which are not part of Data Input

    // Remaining Crew Button
    remainingCrewUpButton = new ItemTypeButton("REMAININGCREWUP", IItemType.TYPE_PUBLIC, null,
        "\u2191");
    remainingCrewUpButton.setFieldSize(18, 30);
    remainingCrewUpButton.setPadding(5, 0, 3, 3);
    remainingCrewUpButton.setFieldGrid(1, 2, GridBagConstraints.WEST, GridBagConstraints.VERTICAL);
    remainingCrewUpButton.displayOnGui(this, mainInputPanel, 9, 5);
    remainingCrewUpButton.registerItemListener(this);
    remainingCrewUpButton.setVisible(false);

    remainingCrewDownButton = new ItemTypeButton("REMAININGCREWDOWN", IItemType.TYPE_PUBLIC, null,
        "\u2193");
    remainingCrewDownButton.setFieldSize(18, 30);
    remainingCrewDownButton.setPadding(5, 0, 3, 3);
    remainingCrewDownButton.setFieldGrid(1, 2,
        GridBagConstraints.WEST, GridBagConstraints.VERTICAL);
    remainingCrewDownButton.displayOnGui(this, mainInputPanel, 9, 7);
    remainingCrewDownButton.registerItemListener(this);
    remainingCrewDownButton.setVisible(false);

    // Info Label
    infoLabel.setForeground(Color.blue);
    infoLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    infoLabel.setText(" ");
    mainInputPanel.add(infoLabel,
        new GridBagConstraints(0, 18, 8, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(10, 20, 10, 20), 0, 0));

    // Boat Damage Button
    boatDamageButton = new ItemTypeButton("BOATDAMAGE", IItemType.TYPE_PUBLIC, null,
        International.getString("Bootsschaden melden"));
    boatDamageButton.setFieldSize(200, 19);
    boatDamageButton.setFieldGrid(4, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    boatDamageButton.setBackgroundColorWhenFocused(Daten.efaConfig
        .getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    boatDamageButton.setIcon(getIcon(BaseDialog.IMAGE_DAMAGE));
    boatDamageButton.displayOnGui(this, mainInputPanel, 4, 19);
    boatDamageButton.registerItemListener(this);
    boatDamageButton.setVisible(isModeBoathouse()
        && Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton());

    // Boat Not Cleaned Button
    boatNotCleanedButton = new ItemTypeButton("BOATNOTCLEANED", IItemType.TYPE_PUBLIC, null,
        International.getString("Boot war nicht geputzt"));
    boatNotCleanedButton.setFieldSize(200, 19);
    boatNotCleanedButton.setFieldGrid(4, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
    boatNotCleanedButton.setBackgroundColorWhenFocused(Daten.efaConfig
        .getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    boatNotCleanedButton.setIcon(getIcon(BaseDialog.IMAGE_SOAP));
    boatNotCleanedButton.displayOnGui(this, mainInputPanel, 4, 20);
    boatNotCleanedButton.registerItemListener(this);
    boatNotCleanedButton.setVisible(isModeBoathouse()
        && Daten.efaConfig.getShowBoatNotCleanedButton());

    // Save Button
    saveButton = new ItemTypeButton("SAVE", IItemType.TYPE_PUBLIC, null,
        International.getString("Eintrag speichern"));
    saveButton.setBackgroundColorWhenFocused(
        Daten.efaConfig.getValueEfaDirekt_colorizeInputField() ? Color.yellow : null);
    saveButton.setIcon(getIcon(BaseDialog.IMAGE_ACCEPT));
    saveButton.displayOnGui(this, mainPanel, BorderLayout.SOUTH);
    saveButton.registerItemListener(this);

    // Set Valid Date and Time Fields for Autocomplete Lists
    boat.setValidAt(date, starttime);
    cox.setValidAt(date, starttime);
    for (ItemTypeStringAutoComplete element : crew) {
      element.setValidAt(date, starttime);
    }
    destination.setValidAt(date, starttime);
  }

  void iniGuiRemaining() {
    efaBaseFrameFocusManager = new EfaBaseFrameFocusManager(this, FocusManager.getCurrentManager());
    FocusManager.setCurrentManager(efaBaseFrameFocusManager);
    if (isModeBoathouse()) {
      setResizable(false);
    }
    if (isModeAdmin()) {
      addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowDeactivated(WindowEvent e) {
          this_windowDeactivated(e);
        }
      });
    }

  }

  void iniApplication() {
    if (Daten.project == null && isModeBase()) {
      if (Daten.efaConfig.getValueLastProjectEfaBase().length() > 0) {
        Project.openProject(Daten.efaConfig.getValueLastProjectEfaBase(), true);
        remoteAdmin = (Daten.project != null ? Daten.project.getRemoteAdmin() : null);
        checkRemoteAdmin();
        iniGuiMenu();
      }
    }
    if (Daten.project != null && isModeBase()
        && Daten.project.getCurrentLogbookEfaBase() != null) {
      openLogbook(Daten.project.getCurrentLogbookEfaBase());
    }
    if (Daten.project != null && isModeAdmin() && logbook != null) {
      // What a hack... ;-) openLogbook() will only open a new logbook if it is not identical with
      // the current one.
      // Actually, there isn't really a *current* logbook. It's only the variable which has been set
      // in the constructor.
      // So we just tweak it a bit so that openLogbook() will accept our logbook as a new one...
      Logbook newLogbook = logbook;
      logbook = null;
      openLogbook(newLogbook);
      if (_jumpToEntryNo != null && _jumpToEntryNo.length() > 0) {
        goToEntry(_jumpToEntryNo, false);
      }
    }
  }

  void setTitle() {
    String adminName = (getAdmin() != null ? getAdmin().getName() : null);
    String adminNameString = (adminName != null && adminName.length() > 0 ? " [" + adminName + "]"
        : "");
    if (isModeBoathouse()) {
      setTitle(Daten.EFA_LONGNAME);
    } else {
      if (Daten.project == null) {
        setTitle(Daten.EFA_LONGNAME + adminNameString);
      } else {
        if (!isLogbookReady()) {
          setTitle(Daten.project.getProjectName() + " - " + Daten.EFA_LONGNAME + adminNameString);
        } else {
          setTitle(Daten.project.getProjectName() + ": " + logbook.getName() + " - "
              + Daten.EFA_LONGNAME + adminNameString);
        }
      }
    }
  }

  private void clearAllBackgroundColors() {
    entryno.restoreBackgroundColor();
    date.restoreBackgroundColor();
    enddate.restoreBackgroundColor();
    boat.restoreBackgroundColor();
    boatvariant.restoreBackgroundColor();
    cox.restoreBackgroundColor();
    for (ItemTypeStringAutoComplete element : crew) {
      element.restoreBackgroundColor();
    }
    boatcaptain.restoreBackgroundColor();
    phoneNr.restoreBackgroundColor();
    starttime.restoreBackgroundColor();
    endtime.restoreBackgroundColor();
    destination.restoreBackgroundColor();
    waters.restoreBackgroundColor();
    distance.restoreBackgroundColor();
    comments.restoreBackgroundColor();
    boatDamageButton.restoreBackgroundColor();
    boatNotCleanedButton.restoreBackgroundColor();
    saveButton.restoreBackgroundColor();
  }

  // =========================================================================
  // Data-related methods
  // =========================================================================

  long getValidAtTimestamp(LogbookRecord r) {
    long t = 0;
    if (r != null) {
      t = r.getValidAtTimestamp();
    } else {
      t = LogbookRecord.getValidAtTimestamp(date.getDate(), starttime.getTime());
    }
    if (t == 0) {
      t = System.currentTimeMillis();
    }
    return t;
  }

  PersonRecord findPerson(ItemTypeString item, long validAt) {
    try {
      String s = item.getValueFromField().trim();
      if (Daten.efaConfig.getValuePostfixPersonsWithClubName()) {
        s = PersonRecord.trimAssociationPostfix(s);
      }
      if (s.length() > 0) {
        Persons persons = Daten.project.getPersons(false);
        PersonRecord r = persons.getPerson(s, validAt);

        // If we have not found a valid record, we next try whether we can find
        // any (currently invalid) record for that name (within the validiy range
        // of this lookbook). If we find such a record, we use its ID to find
        // yet another record of the same ID, which might be valid now.
        // Since we search by name, it could be that the user entered name "A", which is
        // not currently valid, but appears in the AutoComplete list because it is valid
        // some other time for this logbook. It might be that "A" is just another name for
        // "B" of the same record, which is valid. If there is a "B", we will use that.
        // That means, even though the user entered "A", we will save the ID, and display "B".
        if (validAt > 0 && r == null) {
          PersonRecord r2 = persons.getPerson(s, logbookValidFrom, logbookInvalidFrom - 1, validAt);
          if (r2 != null) {
            r = persons.getPerson(r2.getId(), validAt);
          }
        }
        return r;
      }
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    return null;
  }

  BoatRecord findBoat(long validAt) {
    try {
      String s = boat.getValueFromField().trim();
      if (s.length() > 0) {
        BoatRecord r = Daten.project.getBoats(false).getBoat(s, validAt);

        // If we have not found a valid record, we next try whether we can find
        // any (currently invalid) record for that name (within the validiy range
        // of this lookbook). If we find such a record, we use its ID to find
        // yet another record of the same ID, which might be valid now.
        // Since we search by name, it could be that the user entered name "A", which is
        // not currently valid, but appears in the AutoComplete list because it is valid
        // some other time for this logbook. It might be that "A" is just another name for
        // "B" of the same record, which is valid. If there is a "B", we will use that.
        // That means, even though the user entered "A", we will save the ID, and display "B".
        if (validAt > 0 && r == null) {
          BoatRecord r2 = Daten.project.getBoats(false).getBoat(s, logbookValidFrom,
              logbookInvalidFrom - 1, validAt);
          if (r2 != null) {
            r = Daten.project.getBoats(false).getBoat(r2.getId(), validAt);
          }
        }

        return r;
      }
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    return null;
  }

  PersonRecord findPerson(int pos, long validAt) {
    return findPerson(getCrewItem(pos), validAt);
  }

  DestinationRecord findDestinationFromString(String s, long validAt) {
    return findDestinationFromString(s, null, validAt);
  }

  private DestinationRecord findDestinationFromString(String s,
      String boathouseName, long validAt) {
    DestinationRecord r = null;
    try {
      String[] dest = LogbookRecord.getDestinationNameAndVariantFromString(s);
      if (dest[0].length() > 0 && dest[1].length() > 0) {
        // this is a destination of the form "base + variant".
        // however, it could be that we have an explicit destination "base & variant" in our
        // database.
        // check for "base & variant" first
        r = Daten.project.getDestinations(false).getDestination(dest[0] + " & " + dest[1],
            boathouseName, validAt);
        if (r != null) {
          return r;
        }
      }
      if (dest[0].length() > 0) {
        r = Daten.project.getDestinations(false).getDestination(dest[0],
            boathouseName, validAt);

        // If we have not found a valid record, we next try whether we can find
        // any (currently invalid) record for that name (within the validiy range
        // of this lookbook). If we find such a record, we use its ID to find
        // yet another record of the same ID, which might be valid now.
        // Since we search by name, it could be that the user entered name "A", which is
        // not currently valid, but appears in the AutoComplete list because it is valid
        // some other time for this logbook. It might be that "A" is just another name for
        // "B" of the same record, which is valid. If there is a "B", we will use that.
        // That means, even though the user entered "A", we will save the ID, and display "B".
        if (validAt > 0 && r == null) {
          DestinationRecord r2 = Daten.project.getDestinations(false).getDestination(dest[0],
              logbookValidFrom, logbookInvalidFrom - 1, validAt);
          if (r2 != null) {
            r = Daten.project.getDestinations(false).getDestination(r2.getId(), validAt);
          }
        }

        return r;
      }
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    return null;
  }

  DestinationRecord findDestination(long validAt) {
    String s = destination.getValueFromField();
    String bths = null;
    if (isModeBoathouse() && Daten.project.getNumberOfBoathouses() > 1) {
      bths = Daten.project.getMyBoathouseName();
    }
    DestinationRecord r = findDestinationFromString(s, bths, validAt);
    if (r == null && s != null && s.length() > 0) {
      // not found; try to find as prefixed with water
      int pos = s.indexOf(DestinationRecord.WATERS_DESTINATION_DELIMITER);
      if (pos > 0 && pos + 1 < s.length()) {
        s = s.substring(pos + 1);
        r = findDestinationFromString(s, bths, validAt);
      }
    }
    if (r == null && s != null && s.length() > 0) {
      // not found; try to find as postfixed with boathouse name
      String dest = DestinationRecord.getDestinationNameFromPostfixedDestinationBoathouseString(s);
      bths = DestinationRecord.getBoathouseNameFromPostfixedDestinationBoathouseString(s);
      if (dest != null && bths != null && dest.length() > 0 && bths.length() > 0) {
        r = findDestinationFromString(dest, bths, validAt);
      }
    }
    return r;
  }

  DataTypeList<?>[] findWaters(ItemTypeString item) {
    try {
      String s = item.toString().trim();
      if (s.length() == 0) {
        return null;
      }
      s = EfaUtil.replace(s, "+", ",", true);
      s = EfaUtil.replace(s, ";", ",", true);
      Vector<String> wlist = EfaUtil.split(s, ',');
      if (wlist.size() == 0) {
        return null;
      }
      DataTypeList<UUID> watersIdList = new DataTypeList<UUID>();
      DataTypeList<String> watersNameList = new DataTypeList<String>();
      for (int i = 0; i < wlist.size(); i++) {
        String ws = wlist.get(i).trim();
        if (ws.length() == 0) {
          continue;
        }
        WatersRecord w = Daten.project.getWaters(false).findWatersByName(ws);
        if (w != null && w.getId() != null) {
          watersIdList.add(w.getId());
        } else {
          watersNameList.add(ws);
        }
      }
      return new DataTypeList[] {
          watersIdList, watersNameList
      };
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    return null;
  }

  String updateBoatVariant(BoatRecord b, int variant) {
    if (_inUpdateBoatVariant) {
      return null;
    }
    if (Logger.isTraceOn(Logger.TT_GUI, 7)) {
      Logger.log(Logger.DEBUG, Logger.MSG_DEBUG_GUI_EFABASEFRAME,
          "updateBoatVariant(" +
              (b != null ? b.getQualifiedName() : "") + ", " + variant + ")");
    }
    _inUpdateBoatVariant = true;
    try {
      if (b != null) {
        int numberOfVariants = b.getNumberOfVariants();
        if (numberOfVariants < 0) {
          boatvariant.setVisible(false);
          return null;
        }

        if (variant == -1 && numberOfVariants > 1 && boatvariant.isVisible() &&
            currentBoat != null && currentBoat.getId() != null &&
            b.getId() != null && currentBoat.getId().equals(b.getId())) {
          variant = EfaUtil.string2int(boatvariant.getValueFromField(), -1);
        }

        String[] bt = new String[numberOfVariants];
        String[] bv = new String[numberOfVariants];
        for (int i = 0; i < numberOfVariants; i++) {
          bt[i] = Integer.toString(b.getTypeVariant(i));
          bv[i] = b.getQualifiedBoatTypeShortName(i);
        }
        boatvariant.setListData(bt, bv);
        if (variant < 0 && b.getDefaultVariant() > 0) {
          variant = b.getDefaultVariant();
        }
        if (variant > 0) {
          boatvariant.parseAndShowValue(Integer.toString(variant));
        } else {
          if (numberOfVariants > 0 && bt != null && bt.length > 0 && bt[0] != null) {
            boatvariant.parseAndShowValue(bt[0]);
          }
        }
        boatvariant.setVisible(numberOfVariants > 1);
        return boatvariant.getValue();
      }
      boatvariant.setListData(null, null);
      boatvariant.setVisible(false);
      return null;
    } finally {
      _inUpdateBoatVariant = false;
    }
  }

  void openLogbook(String logbookName) {
    if (!getAdmin().isAllowedEditLogbook()) {
      logbookName = null;
    }
    if (logbookName == null || logbookName.length() == 0) {
      setFields(null);
    } else {
      Logbook newLogbook = Daten.project.getLogbook(logbookName, false);
      if (newLogbook != null) {
        openLogbook(newLogbook);
      } else {
        Dialog.error(LogString.fileOpenFailed(logbookName, International.getString("Fahrtenbuch")));
        setFields(null);
      }
    }
  }

  void openLogbook(Logbook newLogbook) {
    if (Daten.project == null) {
      return;
    }
    if (newLogbook == null) {
      return;
    }
    try {
      if (logbook != null && logbook.isOpen()) {
        if (logbook.getName().equals(newLogbook.getName()) &&
            logbook.getProject().getProjectName().equals(Daten.project.getProjectName())) {
          return;
        }
        logbook.close();
      }
    } catch (Exception e) {
      Logger.log(e);
      Dialog.error(e.toString());
    }
    logbook = newLogbook;
    if (!isModeBoathouse()) {
      Daten.project.setCurrentLogbookEfaBase(logbook.getName());
    }
    ProjectRecord pr = Daten.project.getLoogbookRecord(logbook.getName());
    if (pr != null) {
      logbookValidFrom = logbook.getValidFrom();
      logbookInvalidFrom = logbook.getInvalidFrom();
    }
    try {
      iterator = logbook.data().getDynamicIterator();
      autoCompleteListBoats.setDataAccess(Daten.project.getBoats(false).data(), logbookValidFrom,
          logbookInvalidFrom - 1);
      autoCompleteListPersons.setDataAccess(Daten.project.getPersons(false).data(),
          logbookValidFrom, logbookInvalidFrom - 1);
      autoCompleteListDestinations.setDataAccess(Daten.project.getDestinations(false).data(),
          logbookValidFrom, logbookInvalidFrom - 1);
      autoCompleteListWaters.setDataAccess(Daten.project.getWaters(false).data(), logbookValidFrom,
          logbookInvalidFrom - 1);
    } catch (Exception e) {
      Logger.logdebug(e);
      iterator = null;
    }
    if (isModeBoathouse()) {
      autoCompleteListDestinations.setFilterDataOnlyForThisBoathouse(true);
    }
    if (isModeBoathouse()) {
      autoCompleteListDestinations.setPostfixNamesWithBoathouseName(false);
    }
    if (isModeBaseOrAdmin()) {
      try {
        LogbookRecord r = (LogbookRecord) logbook.data().getLast();
        if (r != null) {
          setFields(r);
        } else {
          createNewRecord(false);
        }
        entryno.requestFocus();
      } catch (Exception e) {
        Logger.logdebug(e);
        setFields(null);
      }
    }
    setTitle();
  }

  void openClubwork(String clubworkName) {
    if (!getAdmin().isAllowedEditLogbook()) {
      clubworkName = null;
    }
    if (clubworkName != null && clubworkName.length() > 0) {
      Clubwork newClubwork = Daten.project.getClubwork(clubworkName, false);
      if (newClubwork != null) {
        if (!isModeBoathouse()) {
          Daten.project.setCurrentClubworkBookEfaBase(newClubwork.getName());
          Dialog.infoDialog(LogString.fileOpened(clubworkName,
              International.getString("Vereinsarbeit")));
        }
      } else {
        Dialog.error(LogString.fileOpenFailed(clubworkName,
            International.getString("Vereinsarbeit")));
      }
    }
  }

  boolean isLogbookReady() {
    return Daten.project != null && Daten.project.isOpen() && logbook != null && logbook.isOpen();
  }

  String getFieldValue(ItemTypeLabelValue field, LogbookRecord r) {
    try {
      if (field == entryno) {
        return (r != null && r.getEntryId() != null ? r.getEntryId().toString() : "");
      }
      if (field == date) {
        return (r != null ? r.getDate().toString() : "");
      }
      if (field == enddate) {
        return (r != null ? r.getEndDate().toString() : "");
      }
      if (field == boat) {
        return (r != null ? r.getBoatAsName(getValidAtTimestamp(r)) : "");
      }
      if (field == boatvariant) {
        return updateBoatVariant((r != null ? r.getBoatRecord(getValidAtTimestamp(r)) : null),
            (r != null ? r.getBoatVariant() : 0));
      }
      if (field == cox) {
        return (r != null ? r.getCoxAsName(getValidAtTimestamp(r)) : "");
      }
      for (int i = 0; i < crew.length; i++) {
        if (field == crew[i]) {
          return (r != null ? r.getCrewAsName(i + 1, getValidAtTimestamp(r)) : "");
        }
      }
      if (field == phoneNr) {
        return (r != null ? r.getContact() : "");
      }
      if (field == starttime) {
        return (r != null ? r.getStartTime().toString() : "");
      }
      if (field == endtime) {
        return (r != null ? r.getEndTime().toString() : "");
      }
      if (field == destination) {
        return (r != null ? r.getDestinationAndVariantName(getValidAtTimestamp(r),
            Daten.efaConfig.getValuePrefixDestinationWithWaters(),
            !isModeBoathouse()) : "");
      }
      if (field == waters) {
        return (r != null ? r.getWatersNamesStringList() : "");
      }
      if (field == distance) {
        return (r != null ? r.getDistance().getAsFormattedString() : "");
      }
      if (field == comments) {
        return (r != null ? r.getComments() : "");
      }
      if (field == sessiongroup) {
        UUID id = (r != null ? r.getSessionGroupId() : null);
        sessiongroup.setRememberedId(id);
        return Daten.project.getSessionGroups(false).getSessionGroupName(id);
      }
      if (field == boatcaptain) {
        int pos = (r != null ? (r.getBoatCaptainPosition() >= 0 ? r.getBoatCaptainPosition() : -1)
            : -1);
        if (pos >= 0) {
          setBoatCaptain(pos, false);
        }
        return (pos != -1 ? Integer.toString(pos) : "");
      }
    } catch (NullPointerException enull) {
      // this happens when field in r has not been set, no need to log this!
    } catch (Exception e) {
      Logger.logdebug(e);
    }
    return "";
  }

  void setField(ItemTypeLabelValue field, LogbookRecord r) {
    field.parseAndShowValue(getFieldValue(field, r));
  }

  void setFields(LogbookRecord r) {
    if (!isLogbookReady() && r != null) {
      return;
    }
    referenceRecord = currentRecord;
    currentRecord = r;
    if (iterator != null && r != null) {
      iterator.goTo(r.getKey());
    }
    isNewRecord = r == null;
    isInsertedRecord = false;

    setField(entryno, r);
    setField(date, r);
    setField(enddate, r);
    setField(boat, r);
    setField(boatvariant, r);
    setField(cox, r);
    for (ItemTypeStringAutoComplete element : crew) {
      setField(element, r);
    }
    setField(boatcaptain, r);
    setField(phoneNr, r);
    setField(starttime, r);
    setField(endtime, r);
    setField(destination, r);
    setDestinationInfo((r != null ? r.getDestinationRecord(getValidAtTimestamp(r)) : null));
    setField(waters, r);
    setField(distance, r);
    setField(comments, r);
    setField(sessiongroup, r);
    // replaced by closesessionButton // opensession.setVisible(isModeFull() && r != null &&
    // r.getSessionIsOpen());
    closesessionButton.setVisible(isModeBaseOrAdmin() && r != null && r.getSessionIsOpen());
    currentBoatUpdateGui((r != null && r.getBoatVariant() > 0 ? r.getBoatVariant() : -1));
    setCrewRangeSelection(0);
    setEntryUnchanged();
    entryNoForNewEntry = -1; // -1 bedeutet, daß beim nächsten neuen Datensatz die LfdNr "last+1"
    // vorgegeben wird
    if (r == null) {
      date.requestFocus();
      date.setSelection(0, Integer.MAX_VALUE);
    }
    updateTimeInfoFields();
  }

  LogbookRecord getFields() {
    String s;
    if (!isLogbookReady()) {
      return null;
    }

    // EntryNo
    LogbookRecord r = (isNewRecord || currentRecord == null
        ? logbook.createLogbookRecord(DataTypeIntString.parseString(entryno.getValue()))
        : currentRecord);
    r.setEntryId(DataTypeIntString.parseString(entryno.getValue()));

    // Date
    if (date.isSet()) {
      r.setDate(date.getDate());
    } else {
      r.setDate(null);
    }

    // End Date
    if (enddate.isSet()) {
      r.setEndDate(enddate.getDate());
    } else {
      r.setEndDate(null);
    }

    // Start & End Time
    if (starttime.isSet()) {
      r.setStartTime(starttime.getTime());
    } else {
      r.setStartTime(null);
    }
    if (endtime.isSet()) {
      r.setEndTime(endtime.getTime());
    } else {
      r.setEndTime(null);
    }

    // Boat & Boat Variant
    BoatRecord b = findBoat(getValidAtTimestamp(r));
    if (b != null) {
      r.setBoatId(b.getId());
      r.setBoatVariant(EfaUtil.stringFindInt(boatvariant.getValue(), b.getTypeVariant(0)));
      r.setBoatName(null);
    } else {
      s = boat.toString().trim();
      r.setBoatName((s.length() == 0 ? null : s));
      r.setBoatId(null);
      r.setBoatVariant(IDataAccess.UNDEFINED_INT);
    }

    // Cox and Crew
    for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
      PersonRecord p = findPerson(i, getValidAtTimestamp(r));
      if (p != null) {
        if (i == 0) {
          r.setCoxId(p.getId());
          r.setCoxName(null);
        } else {
          r.setCrewId(i, p.getId());
          r.setCrewName(i, null);
        }
      } else {
        s = getCrewItem(i).toString().trim();
        if (i == 0) {
          r.setCoxName((s.length() == 0 ? null : s));
          r.setCoxId(null);
        } else {
          r.setCrewName(i, (s.length() == 0 ? null : s));
          r.setCrewId(i, null);
        }
      }
    }

    // Boat Captain
    if (boatcaptain.getValue().length() > 0) {
      r.setBoatCaptainPosition(EfaUtil.stringFindInt(boatcaptain.getValue(), 0));
    } else {
      r.setBoatCaptainPosition(IDataAccess.UNDEFINED_INT);
    }

    // Contact = phoneNr
    s = phoneNr.getValue().trim();
    if (s.length() > 0) {
      r.setContact(s);
    } else {
      r.setContact(null);
    }

    // Destination
    DestinationRecord d = findDestination(getValidAtTimestamp(r));
    if (d != null) {
      r.setDestinationId(d.getId());
      r.setDestinationVariantName(LogbookRecord.getDestinationNameAndVariantFromString(destination
          .toString())[1]);
      r.setDestinationName(null);
    } else {
      s = destination.toString().trim();
      r.setDestinationName((s.length() == 0 ? null : s));
      r.setDestinationId(null);
      r.setDestinationVariantName(null);
    }

    // Waters
    if (waters.isVisible()) {
      DataTypeList[] wlists = findWaters(waters);
      if (wlists == null || wlists.length == 0 ||
          (wlists[0].length() == 0 && wlists[1].length() == 0)) {
        r.setWatersIdList(null);
        r.setWatersNameList(null);
      } else {
        r.setWatersIdList((wlists[0].length() > 0 ? wlists[0] : null));
        r.setWatersNameList((wlists[1].length() > 0 ? wlists[1] : null));
      }
    }

    // Distance
    if (distance.isSet()) {
      r.setDistance(distance.getValue());
    } else {
      r.setDistance(null);
    }

    // Comments
    s = comments.toString().trim();
    if (s.length() > 0) {
      r.setComments(s);
    } else {
      r.setComments(null);
    }

    // Session Group
    r.setSessionGroupId((UUID) sessiongroup.getRememberedId());

    return r;
  }

  private void autocompleteAllFields() {
    try {
      if (boat.isVisible()) {
        boat.acpwCallback(null);
      }
      if (cox.isVisible()) {
        cox.acpwCallback(null);
      }
      for (ItemTypeStringAutoComplete element : crew) {
        if (element.isVisible()) {
          element.acpwCallback(null);
        }
      }
      if (destination.isVisible()) {
        destination.acpwCallback(null);
      }
    } catch (Exception e) {}
  }

  // Datensatz speichern
  // liefert "true", wenn erfolgreich
  boolean saveEntry() {
    if (!isLogbookReady()) {
      return false;
    }

    // Da das Hinzufügen eines Eintrags in der Bootshausversion wegen des damit verbundenen
    // Speicherns lange dauern kann, könnte ein ungeduldiger Nutzer mehrfach auf den "Hinzufügen"-
    // Button klicken. "synchronized" hilft hier nicht, da sowieso erst nach Ausführung des
    // Threads der Klick ein zweites Mal registriert wird. Da aber nach Abarbeitung dieser
    // Methode der Frame "EfaFrame" vom Stack genommen wurde und bei der zweiten Methode damit
    // schon nicht mehr auf dem Stack ist, kann eine Überprüfung, ob der aktuelle Frame
    // "EfaFrame" ist, benutzt werden, um eine doppelte Ausführung dieser Methode zu verhindern.
    if (Dialog.frameCurrent() != this) {
      return false;
    }

    // make sure to autocomplete all texts once more in the input fields.
    // users have found strange ways of working around completion...
    autocompleteAllFields();

    // run all checks before saving this entry
    if (!checkMisspelledInput() ||
        !checkDuplicatePersons() ||
        !checkPersonsForBoatType() ||
        !checkDuplicateEntry() ||
        !checkEntryNo() ||
        !checkBoatCaptain() ||
        !checkBoatStatus() ||
        !checkMultiDayTours() ||
        !checkDate() ||
        !checkTime() ||
        !checkDateTooLong() ||
        !checkAllowedDateForLogbook() ||
        !checkAllDataEntered() ||
        !checkNamesValid() ||
        !checkUnknownNames() ||
        !checkAllowedPersons() ||
        !checkUndAktualisiereHandyNrInPersonProfil()) {
      return false;
    }
    if (efaBoathouseAction != null) {
      BoatStatusRecord boatStatus = efaBoathouseAction.boatStatus;
      if (getMode() != MODE_BOATHOUSE_FINISH) {
        if (efaBoathouseFrame.frageBenutzerWennUeberschneidungReservierung(boatStatus)) {
          return false; // abortNow
        }
      }
    }

    boolean success = saveEntryInLogbook();

    if (isModeBaseOrAdmin()) {
      if (success) {
        setEntryUnchanged();
        boolean createNewRecord = false;
        if (isModeBaseOrAdmin()) {
          try {
            LogbookRecord rlast = (LogbookRecord) logbook.data().getLast();
            if (currentRecord.getEntryId().equals(rlast.getEntryId())) {
              createNewRecord = true;
            }
          } catch (Exception eignore) {}
        }
        if (createNewRecord) {
          createNewRecord(false);
        } else {
          entryno.requestFocus();
        }
      }
      return success;
    } else {
      finishBoathouseAction(success);
      return success;
    }
  }

  // den Datensatz nun wirklich speichern;
  boolean saveEntryInLogbook() {
    if (!isLogbookReady()) {
      return false;
    }

    long lock = 0;
    Exception myE = null;
    try {
      boolean changeEntryNo = false;
      if (!isNewRecord && currentRecord != null
          && !currentRecord.getEntryId().toString().equals(entryno.toString())) {
        // Datensatz mit geänderter LfdNr: Der alte Datensatz muß gelöscht werden!
        lock = logbook.data().acquireGlobalLock();
        logbook.data().delete(currentRecord.getKey(), lock);
        changeEntryNo = true;
      }
      currentRecord = getFields();

      if (isModeStartOrStartCorrect()) {
        currentRecord.setSessionIsOpen(true);
      } else {
        currentRecord.setSessionIsOpen(false);
        // all other updates to an open entry (incl. Admin
        // Mode) will mark it as finished
      }

      if (isNewRecord || changeEntryNo) {
        logbook.data().add(currentRecord, lock);
      } else {
        logbook.data().update(currentRecord, lock);
      }
      isNewRecord = false;
    } catch (Exception e) {
      Logger.log(e);
      myE = e;
    } finally {
      if (lock != 0) {
        logbook.data().releaseGlobalLock(lock);
      }
    }
    if (myE != null) {
      Dialog.error(International.getString("Fahrtenbucheintrag konnte nicht gespeichert werden.")
          + "\n" + myE.toString());
      return false;
    }

    if (isModeBaseOrAdmin()) {
      logAdminEvent(
          Logger.INFO,
          (isNewRecord ? Logger.MSG_ADMIN_LOGBOOK_ENTRYADDED
              : Logger.MSG_ADMIN_LOGBOOK_ENTRYMODIFIED),
          (isNewRecord ? International.getString("Eintrag hinzugefügt")
              : International
                  .getString("Eintrag geändert")),
          currentRecord);
    }
    return true;
  }

  void setEntryUnchanged() {
    entryno.setUnchanged();
    date.setUnchanged();
    enddate.setUnchanged();
    boat.setUnchanged();
    boatvariant.setUnchanged();
    cox.setUnchanged();
    for (ItemTypeStringAutoComplete element : crew) {
      element.setUnchanged();
    }
    boatcaptain.setUnchanged();
    phoneNr.setUnchanged();
    starttime.setUnchanged();
    endtime.setUnchanged();
    destination.setUnchanged();
    waters.setUnchanged();
    distance.setUnchanged();
    comments.setUnchanged();
    sessiongroup.setUnchanged();
  }

  boolean isEntryChanged() {
    boolean changed = entryno.isChanged() ||
        date.isChanged() ||
        enddate.isChanged() ||
        boat.isChanged() ||
        cox.isChanged() ||
        boatcaptain.isChanged() ||
        phoneNr.isChanged() ||
        starttime.isChanged() ||
        endtime.isChanged() ||
        destination.isChanged() ||
        waters.isChanged() ||
        distance.isChanged() ||
        comments.isChanged() ||
        sessiongroup.isChanged();
    for (int i = 0; !changed && i < crew.length; i++) {
      changed = crew[i].isChanged();
    }
    return changed;
  }

  boolean promptSaveChangesOk() {
    if (!isLogbookReady() || isModeBoathouse()) {
      return true;
    }
    if (isEntryChanged()) {
      String txt;
      if (isNewRecord) {
        txt = International
            .getString(
                "Der aktuelle Eintrag wurde verändert und noch nicht zum Fahrtenbuch hinzugefügt.")
            + "\n" +
            International.getString("Eintrag hinzufügen?");
      } else {
        txt = International
            .getString("Änderungen an dem aktuellen Eintrag wurden noch nicht gespeichert.")
            + "\n"
            +
            International.getString("Änderungen speichern?");
      }
      switch (Dialog.yesNoCancelDialog(International.getString("Eintrag nicht gespeichert"), txt)) {
        case Dialog.YES:
          return saveEntry();
        case Dialog.NO:
          break;
        default:
          return false;
      }
    }
    return true;
  }

  ItemTypeStringAutoComplete getCrewItem(int pos) {
    if (pos == 0) {
      return cox;
    }
    if (pos >= 1 && pos <= LogbookRecord.CREW_MAX) {
      return crew[pos - 1];
    }
    return null;
  }

  boolean isCoxOrCrewItem(IItemType item) {
    if (item == cox) {
      return true;
    }
    for (ItemTypeStringAutoComplete element : crew) {
      if (item == element) {
        return true;
      }
    }
    return false;
  }

  int getNumberOfPersonsInBoat() {
    int c = 0;
    if (cox.getValueFromField().trim().length() > 0) {
      c++;
    }
    for (int i = 0; i < LogbookRecord.CREW_MAX; i++) {
      if (crew[i].getValueFromField().trim().length() > 0) {
        c++;
      }
    }
    return c;
  }

  int getNumberOfPersonsMitVorNachname() {
    String myMatch = Daten.efaConfig.getRegexForVorUndNachname();

    int c = 0;
    String trimmedName = cox.getValueFromField().trim();
    if (trimmedName.length() > 0 &&
        trimmedName.matches(myMatch)) {
      c++;
    }
    for (int i = 0; i < LogbookRecord.CREW_MAX; i++) {
      trimmedName = crew[i].getValueFromField().trim();
      if (trimmedName.length() > 0 &&
          trimmedName.matches(myMatch)) {
        c++;
      }
    }
    return c;
  }

  void setTime(ItemTypeTime field, int addMinutes, DataTypeTime notBefore) {
    DataTypeTime now = DataTypeTime.now();

    if (getMode() == MODE_BOATHOUSE_LATEENTRY
        && field == starttime) {
      // for Late Entries, we default to a start time about 100 minutes ago
      now.delete((100 + addMinutes) * 60);
    }

    now.setSecond(0);
    now.add(addMinutes * 60);
    int m = now.getMinute();
    if (m % 5 != 0) {
      if (m % 5 < 3) {
        now.delete((m % 5) * 60);
      } else {
        now.add((5 - m % 5) * 60);
      }
    }

    if (notBefore != null) {
      // Test: EndTime < StartTime (where EndTime is at most the configured (add+substract)*2 times
      // smaller)
      if (now.isBefore(notBefore) &&
          now.getTimeAsSeconds() + (Daten.efaConfig.getValueEfaDirekt_plusMinutenAbfahrt() +
              Daten.efaConfig.getValueEfaDirekt_minusMinutenAnkunft()) * 60 * 2 > notBefore
              .getTimeAsSeconds()) {
        // use StartTime as EndTime instead (avoid overlapping times)
        now.setHour(notBefore.getHour());
        now.setMinute(notBefore.getMinute());
      }
    }

    field.parseAndShowValue(now.toString());
    field.setSelection(0, Integer.MAX_VALUE);
  }

  void updateTimeInfoFields() {
    JComponent endDateField = enddate.getComponent();
    starttimeInfoLabel.setVisible(endDateField != null && endDateField.isVisible() &&
        starttime.isVisible());
    endtimeInfoLabel.setVisible(endDateField != null && endDateField.isVisible() &&
        endtime.isVisible());

    String date1 = date.getValueFromField();
    String date2 = enddate.getValueFromField();
    starttimeInfoLabel.setDescription((date1 != null && date1.length() > 0
        ? " (" + International.getMessage("am {date}", date1) + ")"
        : ""));
    endtimeInfoLabel.setDescription((date2 != null && date2.length() > 0
        ? " (" + International.getMessage("am {date}", date2) + ")"
        : ""));
  }

  void setDestinationInfo(DestinationRecord r) {
    boolean showDestinationInfo = false;
    boolean showWatersInput = false;

    if (Daten.efaConfig.getValueShowDestinationInfoForInput()) {
      String[] info = (r != null ? r.getWatersAndDestinationAreasAsLabelAndString() : null);
      String infoLabel = (info != null ? info[0] : International.getString("Gewässer"));
      String infoText = (info != null ? info[1] : "");
      destinationInfo.setDescription(infoLabel);
      destinationInfo.parseAndShowValue((infoText.length() > 0 ? infoText : " ")); // intentionally
      // a space and
      // not empty!
      showDestinationInfo = infoText.length() > 0;
    }

    if (!Daten.efaConfig.getValueAdditionalWatersInput()
        && !Daten.efaConfig.getValueEfaDirekt_gewaesserBeiUnbekanntenZielenPflicht()
        && !showEditWaters) {
      waters.setVisible(false);
    } else {
      String variant = LogbookRecord.getDestinationNameAndVariantFromString(destination
          .getValueFromField())[1];
      boolean isDestinationUnknownOrVariant = (r == null
          || (variant != null && variant.length() > 0));
      boolean watersWasVisible = waters.isVisible();
      showWatersInput = (Daten.efaConfig.getValueAdditionalWatersInput()
          || Daten.efaConfig.getValueEfaDirekt_gewaesserBeiUnbekanntenZielenPflicht()
          || showEditWaters)
          && isDestinationUnknownOrVariant && destination.getValueFromField().length() > 0;
      if (showWatersInput) {
        if (r == null) {
          waters.setDescription(International.getString("Gewässer"));
        } else {
          waters.setDescription(International.getString("Weitere Gewässer"));
        }
      }
      waters.setVisible(showWatersInput);
      if (showWatersInput && !watersWasVisible && isModeBoathouse()) {
        waters.requestFocus();
      }
    }

    if (showDestinationInfo ||
        (Daten.efaConfig.getValueShowDestinationInfoForInput() && !showWatersInput)) {
      destinationInfo.setVisible(true);
    } else {
      destinationInfo.setVisible(false);
    }
  }

  void truncateWatersFromEnteredDestination() {
    String dest = destination.getValueFromField().trim();
    int pos = dest.indexOf(DestinationRecord.WATERS_DESTINATION_DELIMITER.trim());
    if (pos > 0 && pos + 1 < dest.length()) {
      String wtext = dest.substring(0, pos).trim();
      String dtext = dest.substring(pos + 1).trim();
      destination.parseAndShowValue(dtext);
      waters.parseAndShowValue(wtext);
    }
  }

  void setDesinationDistance() {
    String newDestination = DestinationRecord.tryGetNameAndVariant(destination.getValueFromField()
        .trim())[0];
    if (isModeBoathouse() && newDestination.length() > 0
        && distance.getValueFromField().trim().length() == 0) {
      lastDestination = "";
    }
    setFieldEnabledDistance();
    if (!destination.isKnown()) {
      if (!newDestination.equals(lastDestination)) {
        // Das "Leeren" des Kilometerfeldes darf nur im DirectMode erfolgen. Im normalen Modus hätte
        // das
        // den unschönen Nebeneffekt, daß beim korrigieren von unbekannten Zielen die eingegeben
        // Kilometer
        // aus dem Feld verschwinden (ebenso nach der Suche nach unvollständigen Einträgen mit
        // unbekannten
        // Zielen).
        if (isModeBoathouse()
            && (isModeStartOrStartCorrect()
            || isModeFinishOrLateEntry())) {
          distance.parseAndShowValue("");
        }
        lastDestination = "";
        if (Daten.efaConfig.getValuePrefixDestinationWithWaters()) {
          truncateWatersFromEnteredDestination();
        }
      }
      setDestinationInfo(null);
      return;
    }

    DestinationRecord r = findDestination(getValidAtTimestamp(null));
    if (!newDestination.equals(lastDestination) && newDestination.length() != 0
        && isLogbookReady()) {
      // die folgende Zeile ist korrekt, da diese Methode nur nach "vervollstaendige" und bei
      // "zielButton.getBackground()!=Color.red" aus "ziel_keyReleased" oder "zielButton_focusLost"
      // aufgerufen wird und somit ein gültiger Datensatz bereits gefunden wurde!
      if (r != null && r.getDistance() != null && r.getDistance().isSet()) {
        distance.parseAndShowValue(r.getDistance().getAsFormattedString());
      } else {
        distance.parseAndShowValue("1");
      }
    }

    String currentDistance = distance.getValueFromField();
    if (currentDistance == null || EfaUtil.stringFindInt(currentDistance, 0) == 0) {
      // always enable when no distance entered
      setFieldEnabled(true, true, distance);
    }

    setDestinationInfo(r);
  }

  void editBoat(ItemTypeStringAutoComplete item) {
    if (!isLogbookReady()) {
      return;
    }
    String s = item.getValueFromField().trim();
    if (s.length() == 0) {
      return;
    }
    BoatRecord r = findBoat(getValidAtTimestamp(null));
    if (r == null) {
      r = findBoat(-1);
    }
    if (isModeBoathouse() || getMode() == MODE_ADMIN_SESSIONS) {
      if (!Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenNamenHinzufuegen() || r != null) {
        return; // only add new boats (if allowed), but don't edit existing ones
      }
    }
    boolean newRecord = (r == null);
    if (r == null) {
      r = Daten.project.getBoats(false).createBoatRecord(UUID.randomUUID());
      r.addTypeVariant("", EfaTypes.TYPE_BOAT_OTHER, EfaTypes.TYPE_NUMSEATS_OTHER,
          EfaTypes.TYPE_RIGGING_OTHER, EfaTypes.TYPE_COXING_OTHER, Boolean.toString(true));
      String[] name = BoatRecord.tryGetNameAndAffix(s);
      if (name != null && name[0] != null) {
        r.setName(name[0]);
      }
      if (name != null && name[1] != null) {
        r.setNameAffix(name[1]);
      }
    }
    BoatEditDialog dlg = new BoatEditDialog(this, r, newRecord, getAdmin());
    dlg.showDialog();
    if (dlg.getDialogResult()) {
      item.parseAndShowValue(r.getQualifiedName());
      item.setChanged();
      currentBoatUpdateGui();
    }
    efaBaseFrameFocusManager.focusNextItem(item, item.getComponent());
  }

  void editPerson(ItemTypeStringAutoComplete item) {
    if (!isLogbookReady()) {
      return;
    }
    String s = item.getValueFromField().trim();
    if (s.length() == 0) {
      return;
    }
    PersonRecord r = findPerson(item, getValidAtTimestamp(null));
    if (r == null) {
      r = findPerson(item, -1);
    }
    if (isModeBoathouse() || getMode() == MODE_ADMIN_SESSIONS) {
      if (!Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenNamenHinzufuegen() || r != null) {
        return; // only add new persons (if allowed), but don't edit existing ones
      }
    }
    boolean notifyAdmin = false;
    boolean newRecord = (r == null);
    if (r == null) {
      Persons persons = Daten.project.getPersons(false);
      r = persons.createPersonRecord(UUID.randomUUID());
      String[] name = PersonRecord.tryGetFirstLastName(s);
      boolean anyNameSet = false;
      if (name != null && name[0] != null) {
        r.setFirstName(name[0]);
        anyNameSet = true;
      }
      if (name != null && name[1] != null) {
        r.setLastName(name[1]);
        anyNameSet = true;
      }
      if (!anyNameSet && s != null) {
        r.setFirstName(s);
      }
      if (getAdmin() == null || !getAdmin().isAllowedEditPersons()) {
        r.setStatusId(Daten.project.getStatus(false).getStatusOther().getId());
        notifyAdmin = true;
      }
    }
    PersonEditDialog dlg = (new PersonEditDialog(this, r, newRecord, getAdmin()));
    dlg.showDialog();
    if (dlg.getDialogResult()) {
      if (notifyAdmin) {
        String msgTitle = International
            .getString("Eine neue Person wurde der Personenliste hinzugefügt.");
        String msg = msgTitle + "\n" +
            International.getString("Person") + " " + r.getQualifiedName() + ": " + r.toString();
        Logger.log(Logger.INFO, Logger.MSG_EVT_PERSONADDED, msg);
        Daten.project.getMessages(false).createAndSaveMessageRecord(MessageRecord.TO_ADMIN,
            msgTitle, msg);
      }
      item.parseAndShowValue(r.getQualifiedName());
      item.setChanged();
    }
    efaBaseFrameFocusManager.focusNextItem(item, item.getComponent());
  }

  void editDestination(ItemTypeStringAutoComplete item) {
    if (!isLogbookReady()) {
      return;
    }
    String s = item.getValueFromField().trim();
    if (s.length() == 0) {
      return;
    }
    DestinationRecord r = findDestination(getValidAtTimestamp(null));
    if (r == null) {
      r = findDestination(-1);
    }
    if (isModeBoathouse() || getMode() == MODE_ADMIN_SESSIONS) {
      if (!Daten.efaConfig.getValueEfaDirekt_mitgliederDuerfenNamenHinzufuegen() || r != null) {
        return; // only add new destinations (if allowed), but don't edit existing ones
      }
    }
    boolean newRecord = (r == null);
    if (r == null) {
      r = Daten.project.getDestinations(false).createDestinationRecord(UUID.randomUUID());
      String[] name = DestinationRecord.tryGetNameAndVariant(s);
      if (name != null && name[0] != null) {
        r.setName(name[0]);
      }
    }
    DestinationEditDialog dlg = new DestinationEditDialog(this, r, newRecord, getAdmin());
    dlg.showDialog();
    if (dlg.getDialogResult()) {
      item.parseAndShowValue(r.getQualifiedName());
      item.setChanged();
      String distBefore = distance.getValueFromField();
      setDesinationDistance();
      if (distance.getValueFromField().length() == 0 && distBefore.length() > 0) {
        // if there was a distance set, but the new/changed destination record does not
        // specify a distance, then keep the distance that was previously set!
        distance.parseAndShowValue(distBefore);
      }
    }
    efaBaseFrameFocusManager.focusNextItem(item, item.getComponent());
  }

  void selectSessionGroup() {
    if (!isLogbookReady()) {
      return;
    }
    UUID id = null;
    if (currentRecord != null) {
      id = currentRecord.getSessionGroupId();
    }
    SessionGroupListDialog dlg = new SessionGroupListDialog(this, logbook.getName(), id,
        getAdmin());
    dlg.showDialog();
    if (dlg.getDialogResult()) {
      SessionGroupRecord r = dlg.getSelectedSessionGroupRecord();
      if (r == null) {
        sessiongroup.parseAndShowValue("");
        sessiongroup.setRememberedId(null);
      } else {
        sessiongroup.parseAndShowValue(r.getName());
        sessiongroup.setRememberedId(r.getId());
      }
    }
  }

  // =========================================================================
  // Save Entry Checks
  // =========================================================================

  private String getLogbookRecordStringWithEntryNo() {
    return International.getMessage("Fahrtenbucheintrag #{entryno}",
        entryno.getValueFromField());
  }

  private boolean checkMisspelledInput() {
    PersonRecord r;
    for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
      ItemTypeStringAutoComplete field = getCrewItem(i);
      String s = field.getValueFromField().trim();
      if (s.length() == 0) {
        continue;
      }
      r = findPerson(i, getValidAtTimestamp(null));
      if (r == null) {
        // check for comma without blank
        int pos = s.indexOf(",");
        if (pos > 0 && pos + 1 < s.length() && s.charAt(pos + 1) != ' ') {
          field.parseAndShowValue(s.substring(0, pos) + ", " + s.substring(pos + 1));
        }
      }
    }
    return true;
  }

  private boolean checkDuplicatePersons() {
    // Ruderer auf doppelte prüfen
    Hashtable<UUID, String> h = new Hashtable<UUID, String>();
    String doppelt = null; // Ergebnis doppelt==null heißt ok, doppelt!=null heißt Fehler! ;-)
    while (true) { // Unsauber; aber die Alternative wäre ein goto; dies ist keine Schleife!!
      PersonRecord r;
      for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
        r = findPerson(i, getValidAtTimestamp(null));
        if (r != null) {
          UUID id = r.getId();
          if (h.get(id) == null) {
            h.put(id, "");
          } else {
            doppelt = r.getQualifiedName();
            break;
          }
        }
      }
      break; // alles ok, keine doppelten --> Pseudoschleife abbrechen
    }
    if (doppelt != null) {
      Dialog.error(International.getMessage("Die Person '{name}' wurde mehrfach eingegeben!",
          doppelt));
      return false;
    }
    return true;
  }

  private boolean checkPersonsForBoatType() {
    // bei steuermannslosen Booten keinen Steuermann eingeben = cox
    if (cox.getValueFromField().trim().length() > 0
        && currentBoatTypeCoxing != null) {
      if (currentBoatTypeCoxing.equals(EfaTypes.TYPE_COXING_COXLESS)) {
        int ret = Dialog.yesNoDialog(
            International.getString("Steuermann"),
            International
                .getString("Du hast für ein steuermannsloses Boot einen Steuermann eingetragen.")
                + "\n" + International.getString("Trotzdem speichern?"));
        if (ret != Dialog.YES) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkDuplicateEntry() {
    // Prüfen, ob ein Doppeleintrag vorliegt
    if (isModeBoathouse()) {
      LogbookRecord duplicate = logbook.findDuplicateEntry(getFields(), 25); // search last 25
      // logbook entries for
      // potential duplicates
      if (duplicate != null) {
        Vector<String> v = duplicate.getAllCoxAndCrewAsNames();
        String m = "";
        for (int i = 0; i < v.size(); i++) {
          m += (m.length() > 0 ? "; " : "") + v.get(i);
        }
        switch (Dialog.auswahlDialog(
            International.getString("Doppeleintrag") + "?",
            International.getString("efa hat einen ähnlichen Eintrag im Fahrtenbuch gefunden.")
                + "\n"
                + International.getString(
                    "Eventuell hast Du oder jemand anderes die Fahrt bereits eingetragen.")
                + "\n\n"
                + International.getString("Vorhandener Eintrag")
                + ":\n"
                + International.getMessage("#{entry} vom {date} mit {boat}",
                duplicate.getEntryId().toString(), duplicate.getDate().toString(),
                duplicate.getBoatAsName())
                + ":\n"
                + International.getString("Mannschaft")
                + ": "
                + m
                + "\n"
                + International.getString("Abfahrt")
                + ": "
                + (duplicate.getStartTime() != null ? duplicate.getStartTime().toString() : "")
                + "; "
                + International.getString("Ankunft")
                + ": "
                + (duplicate.getEndTime() != null ? duplicate.getEndTime().toString() : "")
                + "; "
                + International.getString("Ziel")
                + " / "
                + International.getString("Strecke")
                + ": "
                + duplicate.getDestinationAndVariantName()
                + " ("
                + (duplicate.getDistance() != null ? duplicate.getDistance().getAsFormattedString() : "")
                + " Km)"
                + "\n\n"
                + International.getString(
                    "Bitte füge den aktuellen Eintrag nur hinzu, falls es sich NICHT um einen Doppeleintrag handelt.")
                + "\n"
                + International.getString("Was möchtest Du tun?"),
            International.getString("Eintrag hinzufügen")
                + " (" + International.getString("kein Doppeleintrag") + ")",
            International.getString("Eintrag nicht hinzufügen")
                + " (" + International.getString("Doppeleintrag") + ")",
            International.getString("Zurück zum Eintrag"))) {
          case 0: // kein Doppeleintrag: Hinzufügen
            break;
          case 1: // Doppeleintrag: NICHT hinzufügen
            cancel();
            return false;
          default: // Zurück zum Eintrag
            return false;
        }
      }
    }
    return true;
  }

  private boolean checkEntryNo() {
    DataTypeIntString newEntryNo = DataTypeIntString.parseString(entryno.getValue());
    while ((logbook.getLogbookRecord(newEntryNo) != null && (isNewRecord || !newEntryNo
        .equals(currentRecord.getEntryId())))
        || newEntryNo.length() == 0) {
      if (isNewRecord && isModeBoathouse()) {
        // duplicate EntryNo's for new sessions in efa-Boathouse can happen in case
        // of simultaneous remote access.
        // if this happens, just increase number by one
        entryno.parseAndShowValue(Integer.toString(newEntryNo.intValue() + 1));
        newEntryNo = DataTypeIntString.parseString(entryno.getValue());
        continue;
      }
      Dialog.error(International.getString("Diese Laufende Nummer ist bereits vergeben!") + " "
          + International.getString("Bitte korrigiere die laufende Nummer des Eintrags!") + "\n\n"
          + International.getString("Hinweis") + ": "
          + International.getString("Um mehrere Einträge unter 'derselben' Nummer hinzuzufügen, "
          + "füge einen Buchstaben von A bis Z direkt an die Nummer an!"));
      entryno.requestFocus();
      return false;
    }

    if (isNewRecord || currentRecord == null) {
      // erstmal prüfen, ob die Laufende Nummer korrekt ist
      DataTypeIntString highestEntryNo = new DataTypeIntString(" ");
      try {
        LogbookRecord r = (LogbookRecord) (logbook.data().getLast());
        if (r != null) {
          highestEntryNo = r.getEntryId();
        }
      } catch (Exception e) {
        Logger.logdebug(e);
      }
      if (newEntryNo.compareTo(highestEntryNo) <= 0 && !isInsertedRecord) {
        boolean printWarnung = true;
        if (entryNoForNewEntry > 0 && newEntryNo.intValue() == entryNoForNewEntry + 1) {
          printWarnung = false;
        }
        if (printWarnung && // nur warnen, wenn das erste Mal eine zu kleine LfdNr eingegeben wurde!
            Dialog.yesNoDialog(International.getString("Warnung"),
                International
                    .getString("Die Laufende Nummer dieses Eintrags ist kleiner als die des "
                        + "letzten Eintrags.")
                    + " " + International.getString("Trotzdem speichern?")) == Dialog.NO) {
          entryno.requestFocus();
          return false;
        }
      }
      entryNoForNewEntry = EfaUtil.string2date(entryno.getValue(), 1, 1, 1).tag; // lfdNr merken,
      // nächster Eintrag erhält dann per default diese Nummer + 1
    } else { // geänderter Fahrtenbucheintrag
      if (!currentRecord.getEntryId().toString().equals(entryno.toString())) {
        if (Dialog.yesNoDialog(International.getString("Warnung"),
            International.getString("Du hast die Laufende Nummer dieses Eintrags verändert!")
                + " " + International.getString("Trotzdem speichern?")) == Dialog.NO) {
          entryno.requestFocus();
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkBoatCaptain() {
    // falls noch nicht geschehen, ggf. automatisch Obmann auswählen
    if (Daten.efaConfig.getValueAutoObmann() && getBoatCaptain() < 0) {
      autoSelectBoatCaptain();
    }

    // Obmann-Auswahl (Autokorrektur, neu in 1.7.1)
    int boatCaptain = getBoatCaptain();
    if (boatCaptain == 0 && cox.getValue().length() == 0 && crew[0].getValue().length() > 0) {
      setBoatCaptain(1, true);
    }
    if (boatCaptain > 0 && crew[boatCaptain - 1].getValue().length() == 0
        && cox.getValue().length() > 0) {
      setBoatCaptain(0, true);
    }
    if (boatCaptain > 0 && crew[boatCaptain - 1].getValue().length() == 0
        && crew[0].getValue().length() > 0) {
      setBoatCaptain(1, true);
    }
    boatCaptain = getBoatCaptain();

    // just to be really sure... if we hide boatcaptain field, but the wrong one is selected, fall
    // back to autoselect
    if ((boatCaptain == 0 && cox.getValue().length() == 0)
        || (boatCaptain > 0 && crew[boatCaptain - 1].getValue().length() == 0)) {
      if (!boatcaptain.isVisible()) {
        autoSelectBoatCaptain(true);
      }
    }

    // Obmann-Check
    if ((boatCaptain == 0 && cox.getValue().length() == 0)
        || (boatCaptain > 0 && crew[boatCaptain - 1].getValue().length() == 0)) {
      Dialog.error(International
          .getString("Bitte wähle als Obmann eine Person aus, die tatsächlich im Boot sitzt!"));
      boatcaptain.setVisible(true);
      boatcaptain.requestFocus();
      return false;
    }

    if (Daten.efaConfig.getValueEfaDirekt_eintragErzwingeObmann() && boatCaptain < 0) {
      Dialog.error(International.getString("Bitte wähle einen Obmann aus!"));
      boatcaptain.setVisible(true);
      boatcaptain.requestFocus();
      return false;
    }

    return true;
  }

  private boolean checkBoatStatus() {
    if (isModeStartOrStartCorrect()) {
      int checkMode = 3;
      // checkFahrtbeginnFuerBoot nur bei direkt_boot==null machen, da ansonsten der Check schon in
      // EfaDirektFrame gemacht wurde
      if (efaBoathouseAction != null) {
        if (efaBoathouseAction.boat == null || // when called from EfaBoathouseFrame before boat is
            // entered
            (currentBoat != null && currentBoat.getId() != null && // boat changed as part of
                // START_CORRECT
                efaBoathouseAction.boat.getId() != currentBoat.getId())) {
          checkMode = 2;
          efaBoathouseAction.boat = currentBoat;
        }
        if (efaBoathouseAction.boat != null) {
          // update boat status (may have changed since we opened the dialog)
          efaBoathouseAction.boatStatus = efaBoathouseAction.boat.getBoatStatus();
        }
        boolean success = efaBoathouseFrame.checkStartSessionForBoat(efaBoathouseAction,
            entryno.getValueFromField(), checkMode);
        if (!success) {
          efaBoathouseAction.boat = null; // otherwise next check would fail
        }
        return success;
      }
    }
    return true;
  }

  private boolean checkMultiDayTours() {
    // Prüfen, ob Eintrag einer Mehrtagesfahrt vorliegt und das Datum in den Zeitraum der
    // Mehrtagesfahrt fällt
    if (isModeBoathouse()) {
      return true;
    }
    UUID sgId = (UUID) sessiongroup.getRememberedId();
    SessionGroupRecord g = (sgId != null ? Daten.project.getSessionGroups(false)
        .findSessionGroupRecord(sgId) : null);
    if (!date.getDate().isSet()) {
      return true; // shouldn't happen
    }
    if (g != null) {
      DataTypeDate entryStartDate = date.getDate();
      DataTypeDate entryEndDate = enddate.getDate();
      DataTypeDate groupStartDate = g.getStartDate();
      DataTypeDate groupEndDate = g.getEndDate();
      if (entryStartDate.isBefore(groupStartDate)
          || entryStartDate.isAfter(groupEndDate)
          ||
          (entryEndDate.isSet() && (entryEndDate.isBefore(groupStartDate) || entryEndDate
              .isAfter(groupEndDate)))) {
        Dialog.error(International.getMessage(
            "Das Datum des Fahrtenbucheintrags {entry} liegt außerhalb des Zeitraums, "
                + "der für die ausgewählte Fahrtgruppe '{name}' angegeben wurde.",
            entryno.getValue(), g.getName()));
        return false;
      }
    }
    return true;
  }

  private boolean checkDate() {
    if (date.isSet() && enddate.isSet() && !date.getDate().isBefore(enddate.getDate())) {
      String msg = International.getString("Das Enddatum muß nach dem Startdatum liegen.");
      Dialog.error(msg);
      enddate.requestFocus();
      return false;
    }
    return true;
  }

  private boolean checkDateTooLong() {
    if (isModeStartOrStartCorrect() && !isModeAdmin()
        && date.isSet() && enddate.isSet()) {
      long anzahlTageBeantragt = enddate.getDate().getDifferenceDays(date.getDate());
      double minimumStundenFuerKulanz = Daten.efaConfig.getMinimumDauerFuerKulanz();
      if (anzahlTageBeantragt * 24 >= minimumStundenFuerKulanz) {
        String msg = ""; // So lange??
        msg += International.getString("Ist die Ausleihe mit dem Fachwart abgestimmt?");
        msg += NEWLINE;
        msg += International.getString("Bitte trage für lange Fahrten eine Reservierung ein.");
        Dialog.error(msg); // Sorry!
        enddate.requestFocus();
        return false;
      }
    }
    return true;
  }

  private boolean checkTime() {
    if (isModeBoathouse()) {
      if (starttime.isVisible() && !starttime.isSet()) {
        setTime(starttime, Daten.efaConfig.getValueEfaDirekt_plusMinutenAbfahrt(), null);
      }
      if (endtime.isVisible() && !endtime.isSet()
          && isModeFinishOrLateEntry()) {
        setTime(endtime, -Daten.efaConfig.getValueEfaDirekt_minusMinutenAnkunft(),
            starttime.getTime());
      }

      // check whether end time is after start time (or multi day tour)
      if (starttime.isVisible() && endtime.isVisible() &&
          starttime.isSet() && endtime.isSet() &&
          starttime.getTime().isAfter(endtime.getTime()) &&
          endtime.isEditable() &&
          !enddate.isSet()) {
        if (Dialog.yesNoDialog(
            International.getMessage("Ungültige Eingabe im Feld '{field}'",
                International.getString("Zeit")),
            International.getString("Bitte überprüfe die eingetragenen Uhrzeiten.") + "\n" +
                International.getString("Ist dieser Eintrag eine Mehrtagsfahrt?")) == Dialog.YES) {
          enddate.expandToField();
          enddate.requestFocus();
          return false;
        }
        endtime.requestFocus();
        return false;
      }

      // check whether the elapsed time is long enough
      if (starttime.isVisible() && endtime.isVisible() && distance.isVisible() &&
          starttime.isSet() && endtime.isSet() && endtime.isEditable() && distance.isSet()) {
        long timediff = Math.abs(endtime.getTime().getTimeAsSeconds()
            - starttime.getTime().getTimeAsSeconds());
        long dist = distance.getValue().getValueInMeters();
        if (timediff < 15 * 60 &&
            timediff < dist / 10 &&
            dist < 100 * 1000) {
          // if a short elapsed time (anything less than 15 minutes) has been entered,
          // then check whether it is plausible; plausible times need to be at least
          // above 1 s per 10 meters; everything else is unplausible
          // we skip the check if the distance is >= 100 Km (that's probably a late entry
          // then).
          String msg = International.getString("Bitte überprüfe die eingetragenen Uhrzeiten.");
          Dialog.error(msg);
          endtime.requestFocus();
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkAllowedDateForLogbook() {
    long tRec = getValidAtTimestamp(null);
    if (tRec < logbookValidFrom || tRec >= logbookInvalidFrom) {
      if (getMode() != MODE_BOATHOUSE_LATEENTRY) {
        String msg = getLogbookRecordStringWithEntryNo()
            + ": "
            + International
            .getMessage(
                "Der Eintrag kann nicht gespeichert werden, da er außerhalb des gültigen Zeitraums ({startdate} - {enddate}) "
                    + "für dieses Fahrtenbuch liegt.",
                logbook.getStartDate().toString(),
                logbook.getEndDate().toString());
        Logger.log(Logger.WARNING, Logger.MSG_EVT_ERRORADDRECORDOUTOFRANGE, msg + " ("
            + getFields().toString() + ")");
        Dialog.error(msg);
        date.requestFocus();
      } else {
        currentRecord = getFields();
        String msg = International
            .getMessage(
                "Das Datum {date} liegt außerhalb des Zeitraums "
                    +
                    "für dieses Fahrtenbuch ({dateFrom} - {dateTo}) und kann daher nicht gespeichert werden. "
                    +
                    "Du kannst diesen Eintrag aber zum Nachtrag an den Administrator senden.",
                (currentRecord.getDate() != null ? currentRecord.getDate().toString() : "?"),
                logbook.getStartDate().toString(), logbook.getEndDate().toString())
            + "\n" +
            International.getString("Was möchtest Du tun?");
        switch (Dialog.auswahlDialog(getLogbookRecordStringWithEntryNo(), msg,
            International.getString("Datum korrigieren"),
            International.getString("Nachtrag an Admin senden"), true)) {
          case 0:
            date.requestFocus();
            break;
          case 1:
            String fname = Daten.efaTmpDirectory + "entry_" +
                EfaUtil.getCurrentTimeStampYYYYMMDD_HHMMSS() + ".xml";
            String entryNo = getLogbookRecordStringWithEntryNo();
            currentRecord.setEntryId(null);
            if (currentRecord.saveRecordToXmlFile(fname)) {
              Daten.project
                  .getMessages(false)
                  .createAndSaveMessageRecord(
                      MessageRecord.TO_ADMIN,
                      International.getString("Nachtrag"),
                      International
                          .getMessage(
                              "Ein Nachtrag für {datum} konnte im Fahrtenbuch {logbook} nicht gespeichert werden, "
                                  +
                                  "da sein Datum außerhalb des Zeitraums für dieses Fahrtenbuch liegt ({dateFrom} - {dateTo}).",
                              currentRecord.getDate().toString(), logbook.getName(),
                              logbook.getStartDate().toString(), logbook.getEndDate().toString())
                          + "\n\n"
                          +
                          currentRecord.getLogbookRecordAsStringDescription()
                          + "\n\n"
                          +
                          International
                              .getMessage(
                                  "Der Eintrag wurde als Importdatei {name} abgespeichert "
                                      +
                                      "und kann durch Import dieser Datei zum entsprechenden Fahrtenbuch hinzugefügt werden.",
                                  fname));
              Dialog
                  .infoDialog(International
                      .getMessage(
                          "{entry} wurde zum Nachtrag an den Admin geschickt und wird erst nach der Bestätigung durch den Admin sichtbar sein.",
                          entryNo));
              cancel();
            } else {
              Dialog.error(LogString.operationFailed(International
                  .getString("Nachtrag an Admin senden")));
            }
            break;
          default:
            break;
        }
      }
      return false;
    }
    return true;
  }

  private boolean checkAllDataEntered() {
    if (isModeBoathouse()) {
      if (boat.getValue().length() == 0) {
        Dialog.error(International.getString("Bitte gib einen Bootsnamen ein!"));
        boat.requestFocus();
        return false;
      }

      if (getNumberOfPersonsInBoat() == 0) {
        Dialog.error(International.getString("Bitte trage mindestens eine Person ein!"));
        if (cox.isEditable()) {
          cox.requestFocus();
        } else {
          crew[0].requestFocus();
        }
        return false;
      }

      // Aufruf andere Prüfung aus Reservierung
      // if (config-Erlaubt) --> nicht nötig, einfach Regexp mit .* füllen
      if (getNumberOfPersonsMitVorNachname() == 0) {
        Dialog.error(International.getString("Bitte trage Vor- und Nachnamen ein"));
        if (cox.isEditable()) {
          cox.requestFocus();
        } else {
          crew[0].requestFocus();
        }
        return false;
      }

      if (!phoneNr.isValidInput()) {
        Dialog.error(phoneNr.getInvalidErrorText() + "\n" +
            International.getString("Bitte gib eine Telefon- oder Handynummer ein"));
        phoneNr.requestFocus();
        return false;
      }

      // Ziel vor Fahrtbeginn eintragen
      if (isModeStartOrStartCorrect()
          && Daten.efaConfig.getValueEfaDirekt_zielBeiFahrtbeginnPflicht()
          && destination.getValue().length() == 0) {
        Dialog.error(International
            .getString("Bitte trage ein voraussichtliches Fahrtziel/Strecke ein!"));
        destination.requestFocus();
        return false;
      }

      if (isModeFinishOrLateEntry()
          && Daten.efaConfig.getValueEfaDirekt_zielBeiFahrtbeginnPflicht()
          && destination.getValue().length() == 0) {
        Dialog.error(International.getString("Bitte trage ein Fahrtziel/Strecke ein!"));
        destination.requestFocus();
        return false;
      }

      // Waters
      if (isModeFinishOrLateEntry() &&
          Daten.efaConfig.getValueEfaDirekt_gewaesserBeiUnbekanntenZielenPflicht() &&
          waters.isVisible() && waters.getValue().length() == 0) {
        Dialog.error(International.getString("Bitte trage ein Gewässer ein!"));
        waters.requestFocus();
        return false;
      }

      // Distance
      if ((!distance.isSet() || distance.getValue().getValueInDefaultUnit() == 0)) {
        if (isModeFinishOrLateEntry()) {
          if (!Daten.efaConfig.getValueAllowSessionsWithoutDistance()) {
            Dialog.error(International.getString("Bitte trage die gefahrenen Entfernung ein!"));
            distance.requestFocus();
            return false;
          }
        }
        if (isModeBaseOrAdmin()) {
          if (Dialog.yesNoDialog(International.getString("Warnung"),
              International.getString("Keine Kilometer eingetragen.") + "\n"
                  + International.getString("Trotzdem speichern?")) == Dialog.NO) {
            distance.requestFocus();
            return false;
          }
        }
      } else {
        if (distance.getValue().getRoundedValueInKilometers() > 100) {
          Dialog.meldung(distance.getValue().getRoundedValueInKilometers() + "km???",
              International.getString("Bitte prüfe die gefahrene Distanz!"));
          distance.requestFocus();
          return false;
        }
      }

    }
    return true;
  }

  private boolean ingoreNameInvalid(String name, long validAt, String type, IItemType field) {
    String msg = International.getMessage(
        "{type} '{name}' ist zum Zeitpunkt {dateandtime} ungültig.",
        type, name, EfaUtil.getTimeStamp(validAt));
    if (isModeBoathouse()) {
      // don't prompt, warn only
      LogbookRecord r = getFields();
      Logger.log(Logger.WARNING, Logger.MSG_EVT_ERRORRECORDINVALIDATTIME,
          getLogbookRecordStringWithEntryNo() + ": " + msg +
              (r != null ? " (" + r.toString() + ")" : ""));
      return true;

    }
    if (Dialog.auswahlDialog(International.getString("Warnung"),
        msg,
        International.getString("als unbekannten Namen speichern"),
        International.getString("Abbruch"), false) != 0) {
      field.requestFocus();
      return false;
    } else {
      return true;
    }
  }

  private boolean checkNamesValid() {
    // Prüfen, ocb ein eingetragener Datensatz zum angegebenen Zeitpunkt ungültig ist
    long preferredValidAt = getValidAtTimestamp(null);

    String name = boat.getValueFromField();
    if (name != null && name.length() > 0) {
      BoatRecord r = findBoat(preferredValidAt);
      if (r == null) {
        r = findBoat(-1);
      }
      if (preferredValidAt > 0 && r != null && !r.isValidAt(preferredValidAt)) {
        if (!ingoreNameInvalid(r.getQualifiedName(), preferredValidAt,
            International.getString("Boot"), boat)) {
          return false;
        }
      }
    }

    for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
      name = (i == 0 ? cox : crew[i - 1]).getValueFromField();
      if (name != null && name.length() > 0) {
        PersonRecord r = findPerson(i, preferredValidAt);
        if (r == null) {
          r = findPerson(i, -1);
        }
        if (preferredValidAt > 0 && r != null && !r.isValidAt(preferredValidAt)) {
          if (!ingoreNameInvalid(r.getQualifiedName(), preferredValidAt,
              International.getString("Person"), (i == 0 ? cox : crew[i - 1]))) {
            return false;
          }
        }
      }
    }

    name = destination.getValueFromField();
    if (name != null && name.length() > 0) {
      DestinationRecord r = findDestination(preferredValidAt);
      if (r == null) {
        r = findDestination(-1);
      }
      if (preferredValidAt > 0 && r != null && !r.isValidAt(preferredValidAt)) {
        if (!ingoreNameInvalid(r.getQualifiedName(), preferredValidAt,
            International.getString("Ziel"), destination)) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean checkUnknownNames() {
    // Prüfen, ob ggf. nur bekannte Boote/Ruderer/Ziele eingetragen wurden
    if (isModeBoathouse()) {
      if (Daten.efaConfig.getValueEfaDirekt_eintragNurBekannteBoote()) {
        String name = boat.getValueFromField();
        if (name != null && name.length() > 0 && findBoat(getValidAtTimestamp(null)) == null) {
          Dialog.error(LogString.itemIsUnknown(name, International.getString("Boot")));
          boat.requestFocus();
          return false;
        }
      }
      if (Daten.efaConfig.getValueEfaDirekt_eintragNurBekannteRuderer()) {
        for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
          String name = (i == 0 ? cox : crew[i - 1]).getValueFromField();
          if (name != null && name.length() > 0
              && findPerson(i, getValidAtTimestamp(null)) == null) {
            Dialog.error(LogString.itemIsUnknown(name, International.getString("Person")));
            if (i == 0) {
              cox.requestFocus();
            } else {
              crew[i - 1].requestFocus();
            }
            return false;
          }
        }
      }
      if (Daten.efaConfig.getValueEfaDirekt_eintragNurBekannteZiele()) {
        String name = destination.getValueFromField();
        if (name != null && name.length() > 0
            && findDestination(getValidAtTimestamp(null)) == null) {
          Dialog.error(LogString.itemIsUnknown(name, International.getString("Ziel/Strecke")));
          destination.requestFocus();
          return false;
        }
      }

      if (Daten.efaConfig.getValueEfaDirekt_eintragNurBekannteGewaesser() && waters.isVisible()) {
        DataTypeList<?>[] wlists = findWaters(waters);
        if (wlists != null && wlists.length != 0 && wlists[1].length() > 0) {
          Dialog.error(LogString.itemIsUnknown(wlists[1].toString(),
              International.getString("Gewässer")));
          waters.requestFocus();
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkAllowedPersons() {
    if (isModeStartOrStartCorrect()) {
      if (currentBoat == null) {
        return true;
      }

      LogbookRecord myRecord = getFields();
      if (myRecord == null) {
        return true;
      }

      Groups groups = Daten.project.getGroups(false);
      long tstmp = getValidAtTimestamp(myRecord);

      DataTypeList<UUID> groupIdList = currentBoat.getAllowedGroupIdList();
      if (groupIdList != null && groupIdList.length() > 0) {
        String nichtErlaubt = null;
        int nichtErlaubtAnz = 0;
        // Vector g = Boote.getGruppen(b);
        for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
          PersonRecord p = myRecord.getCrewRecord(i, tstmp);
          String ptext = myRecord.getCrewName(i);
          if (p == null && ptext == null) {
            continue;
          }

          boolean inAnyGroup = false;
          if (p != null) {
            for (int j = 0; j < groupIdList.length(); j++) {
              GroupRecord g = groups.findGroupRecord(groupIdList.get(j), tstmp);
              if (g != null && g.getMemberIdList() != null
                  && g.getMemberIdList().contains(p.getId())) {
                inAnyGroup = true;
                break;
              }
            }
          }
          if (!inAnyGroup) {
            String name = (p != null ? p.getQualifiedName() : ptext);
            nichtErlaubt = (nichtErlaubt == null ? name : nichtErlaubt + "\n" + name);
            nichtErlaubtAnz++;
          }
        }
        if (Daten.efaConfig.getValueCheckAllowedPersonsInBoat() &&
            nichtErlaubtAnz > 0) {
          String erlaubteGruppen = null;
          for (int j = 0; j < groupIdList.length(); j++) {
            GroupRecord g = groups.findGroupRecord(groupIdList.get(j), tstmp);
            String name = (g != null ? g.getName() : null);
            if (name == null) {
              continue;
            }
            erlaubteGruppen = (erlaubteGruppen == null ? name
                : erlaubteGruppen
                    + (j + 1 < groupIdList.length() ? ", " + name
                    : " "
                        + International.getString("und") + " " + name));
          }
          int ergebnisAuswahlDialog = Dialog
              .auswahlDialog(
                  International.getString("Boot nur für bestimmte Gruppen freigegeben"),
                  International.getMessage("Dieses Boot dürfen nur {list_of_valid_groups} nutzen.",
                      erlaubteGruppen)
                      + "\n"
                      + International
                      .getString(
                          "Folgende Personen gehören keiner der Gruppen an und dürfen das Boot nicht benutzen:")
                      + " \n"
                      + nichtErlaubt + "\n"
                      + International.getString("Was möchtest Du tun?"),
                  International.getString("Anderes Boot wählen"),
                  International.getString("Mannschaft ändern"),
                  International.getString("Trotzdem benutzen"),
                  International.getString("Eintrag abbrechen"));
          switch (ergebnisAuswahlDialog) {
            case 0: // "Anderes Boot wählen"
              setFieldEnabled(true, true, boat);
              boat.parseAndShowValue("");
              boat.requestFocus();
              return false;
            case 1: // "Mannschaft ändern"
              crew[0].requestFocus();
              return false;
            case 2: // "Trotzdem benutzen"
              logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_UNALLOWEDBOATUSAGE,
                  International.getString("Unerlaubte Benutzung eines Bootes"),
                  myRecord);
              break;
            case 3: // "Eintrag abbrechen"
              cancel();
              return false;
            default: // "Eintrag abbrechen"
              cancel();
              return false;
          }
        }
      }
    }
    return true;
  }

  // =========================================================================
  // Menu Actions
  // =========================================================================

  void menuActionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if (cmd == null) {
      return;
    }

    // check and prompt to save changes (except for Help and About)
    if (!cmd.equals(EfaMenuButton.BUTTON_HELP) &&
        !cmd.equals(EfaMenuButton.BUTTON_ABOUT)) {
      if (!isModeBaseOrAdmin() || !promptSaveChangesOk()) {
        return;
      }
    }

    // now check permissions and perform the menu action
    boolean permission = EfaMenuButton.menuAction(this, cmd, getAdmin(), logbook);

    // handle exit
    if (cmd.equals(EfaMenuButton.BUTTON_EXIT) && permission) {
      cancel();
    }

    // Projects and Logbooks are *not* handled within EfaMenuButton
    if (cmd.equals(EfaMenuButton.BUTTON_PROJECTS) && permission) {
      menuFileProjects(e);
    }
    if (cmd.equals(EfaMenuButton.BUTTON_LOGBOOKS) && permission) {
      menuFileLogbooks(e);
    }
    if (cmd.equals(EfaMenuButton.BUTTON_CLUBWORKBOOK) && permission) {
      menuFileClubwork(e);
    }

  }

  void menuFileProjects(ActionEvent e) {
    // for projects, we always use the permissions of the local admin!!
    OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
        OpenProjectOrLogbookDialog.Type.project, admin); // local admin!!
    String projectName = dlg.openDialog();
    if (projectName == null) {
      return;
    }
    if (Daten.project != null && Daten.project.isOpen()) {
      try {
        Daten.project.closeAllStorageObjects();
      } catch (Exception ee) {
        Logger.log(ee);
        Dialog.error(ee.toString());
        return;
      }
    }
    Daten.project = null;
    Project.openProject(projectName, true);
    remoteAdmin = (Daten.project != null ? Daten.project.getRemoteAdmin() : null);
    checkRemoteAdmin();
    iniGuiMenu();
    if (Daten.project != null && !isModeBoathouse()) {
      Daten.efaConfig.setValueLastProjectEfaBase(Daten.project.getProjectName());
    }
    if (Daten.project != null) {
      if (Daten.project.getCurrentLogbookEfaBase() != null) {
        openLogbook(Daten.project.getCurrentLogbookEfaBase());
      } else {
        menuFileLogbooks(null);
      }
    }
    setTitle();
  }

  void menuFileLogbooks(ActionEvent e) {
    if (Daten.project == null) {
      menuFileProjects(e);
      if (Daten.project == null) {
        return;
      }
    }
    OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
        OpenProjectOrLogbookDialog.Type.logbook, getAdmin());
    String logbookName = dlg.openDialog();
    if (logbookName != null) {
      openLogbook(logbookName);
    }
    setTitle();
  }

  void menuFileClubwork(ActionEvent e) {
    if (Daten.project == null) {
      menuFileProjects(e);
      if (Daten.project == null) {
        return;
      }
    }
    OpenProjectOrLogbookDialog dlg = new OpenProjectOrLogbookDialog(this,
        OpenProjectOrLogbookDialog.Type.clubwork, getAdmin());
    String clubworkName = dlg.openDialog();
    if (clubworkName != null) {
      openClubwork(clubworkName);
    }
    setTitle();
  }

  // =========================================================================
  // Toolbar Button Actions
  // =========================================================================

  void navigateInLogbook(int relative) {
    if (!isLogbookReady() || iterator == null) {
      return;
    }
    if (!promptSaveChangesOk()) {
      return;
    }
    LogbookRecord r = null;
    switch (relative) {
      case Integer.MIN_VALUE:
        r = logbook.getLogbookRecord(iterator.getFirst());
        break;
      case Integer.MAX_VALUE:
        r = logbook.getLogbookRecord(iterator.getLast());
        break;
      case -1:
        r = logbook.getLogbookRecord(iterator.getPrev());
        if (r == null) {
          r = logbook.getLogbookRecord(iterator.getFirst());
        }
        break;
      case 1:
        r = logbook.getLogbookRecord(iterator.getNext());
        if (r == null) {
          r = logbook.getLogbookRecord(iterator.getLast());
        }
        break;
      case 0:
        r = logbook.getLogbookRecord(iterator.getCurrent());
        if (r == null) {
          r = logbook.getLogbookRecord(iterator.getLast());
        }
        break;
      default:
        Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR,
            "navigateInLogbook(): unreachable switch: " + "relative = " + relative);
        break;
    }
    if (r != null) {
      setFields(r);
    }
  }

  void goToEntry(String entryNo, boolean next) {
    if (!isLogbookReady() || iterator == null) {
      return;
    }
    if (!promptSaveChangesOk()) {
      return;
    }
    if (entryNo == null || entryNo.length() == 0) {
      return;
    }
    if (Character.isDigit(entryNo.charAt(0))) {
      LogbookRecord r = logbook.getLogbookRecord(DataTypeIntString.parseString(entryNo));
      if (r != null) {
        setFields(r);
      }
    } else {
      SearchLogbookDialog.initialize(this, logbook, iterator);
      SearchLogbookDialog.search(entryNo.toLowerCase(), false,
          SearchLogbookDialog.SearchMode.normal,
          next, false);
    }
  }

  void createNewRecord(boolean insertAtCurrentPosition) {
    if (!isLogbookReady()) {
      return;
    }
    if (isModeBaseOrAdmin() && !promptSaveChangesOk()) {
      return;
    }

    String currentEntryNo = null;
    /*
     * @remove if (insertAtCurrentPosition && currentRecord != null && currentRecord.getEntryId() !=
     * null) { currentEntryNo = currentRecord.getEntryId().toString(); if (!isModeBase() &&
     * Daten.project.getBoatStatus(false).areBoatsOutOnTheWater()) {
     * Dialog.error(InternationalXXX.getString("Es sind noch Boote unterwegs. " +
     * "Das Einfügen von Einträgen ist nur möglich, wenn alle laufenden Fahrten beendet sind."));
     * return; } int ret = Dialog.yesNoDialog(InternationalXXX.getString("Eintrag einfügen"),
     * InternationalXXX.getMessage(
     * "Soll vor dem aktuellen Eintrag (Lfd. Nr. {lfdnr}) wirklich ein neuer Eintrag eingefügt werden?\n"
     * + "Alle nachfolgenden laufenden Nummern werden dann um eins erhöht!", currentEntryNo)); if
     * (ret != Dialog.YES) { return; } long lock = -1; try { lock =
     * logbook.data().acquireGlobalLock(); DataKeyIterator it = logbook.data().getStaticIterator();
     * DataKey k = it.getLast(); while (k != null) { LogbookRecord r = logbook.getLogbookRecord(k);
     * // calculate new entryNo String entryNo = r.getEntryId().toString(); int entryNoi =
     * EfaUtil.stringFindInt(entryNo, 0); String entryNoc =
     * entryNo.substring(Integer.toString(entryNoi).length());
     * r.setEntryId(DataTypeIntString.parseString(Integer.toString(++entryNoi) + entryNoc)); //
     * change entry logbook.data().delete(k, lock); logbook.data().add(r, lock); if
     * (currentEntryNo.equals(r.getEntryId().toString())) { break; } k = it.getPrev(); } }
     * catch(Exception e) { Logger.logdebug(e); Dialog.error(e.toString()); } finally { if (lock !=
     * -1) { logbook.data().releaseGlobalLock(lock); } } }
     */

    setFields(null);

    // calculate new EntryID for new record
    if (insertAtCurrentPosition) {
      entryno.parseAndShowValue(currentEntryNo);
      entryno.setUnchanged();
    } else {
      String n;
      if (isModeBaseOrAdmin() && entryNoForNewEntry > 0) {
        n = Integer.toString(entryNoForNewEntry + 1);
      } else {
        n = logbook.getNextEntryNo().toString();
      }
      entryno.parseAndShowValue(n);
      entryno.setUnchanged();
    }

    // set Date
    String d;
    if (referenceRecord != null && referenceRecord.getDate() != null) {
      d = referenceRecord.getDate().toString();
    } else {
      d = EfaUtil.getCurrentTimeStampDD_MM_YYYY();
    }
    date.parseAndShowValue(d);
    updateTimeInfoFields();
    date.setUnchanged();
    if (isModeBaseOrAdmin()) {
      date.setSelection(0, Integer.MAX_VALUE);
    }
  }

  void deleteRecord() {
    if (!isModeBaseOrAdmin() || !isLogbookReady()) {
      return;
    }
    String entryNo = null;
    if (currentRecord != null && currentRecord.getEntryId() != null
        && currentRecord.getEntryId().toString().length() > 0) {
      entryNo = currentRecord.getEntryId().toString();
    }
    if (entryNo == null) {
      return;
    }
    if (Dialog.yesNoDialog(International.getString("Wirklich löschen?"),
        International
            .getString("Möchtest Du den aktuellen Eintrag wirklich löschen?")) == Dialog.YES) {
      try {
        logbook.data().delete(currentRecord.getKey());
        if (isModeBaseOrAdmin()) {
          logAdminEvent(Logger.INFO, Logger.MSG_ADMIN_LOGBOOK_ENTRYDELETED,
              International.getString("Eintrag gelöscht"), currentRecord);
        }
      } catch (Exception e) {
        Logger.logdebug(e);
        Dialog.error(e.toString());
      }

      LogbookRecord r = logbook.getLogbookRecord(iterator.getCurrent());
      if (r == null) {
        r = logbook.getLogbookRecord(iterator.getLast());
      }
      setFields(r);
    }
  }

  void searchLogbook() {
    String s = toolBar_goToEntry.getText().trim();
    SearchLogbookDialog.showSearchDialog(this, logbook, iterator, (s.length() > 0 ? s : null));
  }

  // =========================================================================
  // Callback-related methods
  // =========================================================================

  private void this_windowDeactivated(WindowEvent e) {
    try {
      if (isEnabled() && Dialog.frameCurrent() == this) {
        toFront();
      }
    } catch (Exception ee) {
      Logger.logdebug(ee);
    }
  }

  @Override
  public void itemListenerAction(IItemType item, AWTEvent event) {
    int id = event.getID();
    if (id == ActionEvent.ACTION_PERFORMED) {
      if (item == boat) {
        editBoat((ItemTypeStringAutoComplete) item);
      }
      if (item == cox) {
        editPerson((ItemTypeStringAutoComplete) item);
      }
      for (int i = 0; i < LogbookRecord.CREW_MAX; i++) {
        if (item == crew[i]) {
          editPerson((ItemTypeStringAutoComplete) item);
        }
      }
      if (item == destination) {
        editDestination((ItemTypeStringAutoComplete) item);
      }
      if (item == sessiongroup) {
        selectSessionGroup();
      }
      if (item == remainingCrewUpButton) {
        setCrewRangeSelection(crewRangeSelection - 1);
      }
      if (item == remainingCrewDownButton) {
        setCrewRangeSelection(crewRangeSelection + 1);
      }
      if (item == boatDamageButton) {
        if (currentBoat != null && currentBoat.getId() != null) {
          UUID personID = null;
          LogbookRecord myRecord = currentRecord;
          if (myRecord == null) {
            myRecord = getFields();
          }
          if (myRecord != null) {
            personID = myRecord.getCoxId();
            if (personID == null) {
              personID = myRecord.getCrewId(1);
            }
          }
          String logbookRecordText = null;
          if (myRecord != null) {
            logbookRecordText = myRecord.getLogbookRecordAsStringDescription();
          }
          BoatDamageEditDialog.newBoatDamage(this, currentBoat, personID, logbookRecordText);
        }
      }
      if (item == boatNotCleanedButton) {
        if (currentBoat != null && currentBoat.getId() != null) {
          String boatName = currentBoat.getQualifiedName();
          String personName = "";
          LogbookRecord myRecord = currentRecord;
          if (myRecord == null) {
            myRecord = getFields();
          }
          if (myRecord != null) {
            UUID personID = myRecord.getCoxId();
            if (personID == null) {
              personID = myRecord.getCrewId(1);
            }
            if (personID != null) {
              Persons persons = Daten.project.getPersons(false);
              PersonRecord r = persons.getPerson(personID, System.currentTimeMillis());
              if (r != null) {
                personName = r.getQualifiedName();
              }
            }
          }
          String logbookRecordText = null;
          if (myRecord != null) {
            logbookRecordText = myRecord.getLogbookRecordAsStringDescription();
          }
          IItemType[] items = new IItemType[4];
          items[0] = new ItemTypeLabel("INFO", IItemType.TYPE_PUBLIC, "",
              International
                  .getString("Bitte nur ausfüllen, wenn du das Boot ungeputzt vorgefunden hast!"));
          ((ItemTypeLabel) items[0]).setPadding(0, 0, 0, 20);
          items[1] = new ItemTypeLabel("BOAT", IItemType.TYPE_PUBLIC, "",
              International.getString("Boot") + ": " + boatName);
          items[2] = new ItemTypeString("DESCRIPTION", "", IItemType.TYPE_PUBLIC, "",
              International.getString("Beschreibung"));
          ((ItemTypeString) items[2]).setNotNull(true);
          items[3] = new ItemTypeStringAutoComplete("NAME", personName,
              IItemType.TYPE_PUBLIC, "",
              International.getString("Dein Name"), false);
          ((ItemTypeStringAutoComplete) items[3]).setNotNull(true);
          ((ItemTypeStringAutoComplete) items[3]).setAutoCompleteData(autoCompleteListPersons);
          if (SimpleInputDialog.showInputDialog(this,
              International.getString("ein ungeputztes Boot melden"),
              items)) {
            items[2].getValueFromField();
            personName = items[3].getValueFromField();
            UUID personId = (UUID) (((ItemTypeStringAutoComplete) items[3]).getId(personName));
            LogbookRecord latest = (logbook != null
                ? logbook.getLastBoatUsage(currentBoat.getId(), myRecord)
                : null);
            String lastUsage = (latest != null ? latest.getLogbookRecordAsStringDescription()
                : International.getString("Keinen Eintrag gefunden!"));
            StringBuilder message = new StringBuilder();
            message.append(International.getMessage(
                "{person} hat gemeldet, dass das Boot '{boat}' nicht geputzt war.",
                personName, boatName) + "\n\n");
            message.append(International.getString("gemeldet am") + ": "
                + EfaUtil.getCurrentTimeStampYYYY_MM_DD_HH_MM_SS() + "\n");
            message.append(International.getString("gemeldet von") + ": " + personName +
                " (" + logbookRecordText + ")\n\n");
            message.append(International.getString("Letzte Benutzung") + ":\n" + lastUsage);

            Daten.project.getMessages(false).createAndSaveMessageRecord(personName,
                MessageRecord.TO_BOATMAINTENANCE, personId,
                International.getString("Boot war nicht geputzt") + " - " + boatName,
                message.toString());
            Dialog.infoDialog(International.getString("Danke") + "!");
          }
        }
      }
      if (item == closesessionButton) {
        if (currentRecord != null) {
          int yesNoBeendenDialog = Dialog.yesNoDialog(
              International.getString("Fahrt beenden"),
              International.getString(
                  "Möchtest du die Fahrt jetzt beenden und den Status des Boots auf verfügbar setzen?"));
          if (yesNoBeendenDialog == Dialog.YES) {
            currentRecord.setSessionIsOpen(false);
            updateBoatStatus(true, MODE_BOATHOUSE_FINISH);
            saveEntry();
            navigateInLogbook(0);
          }
        }
      }
      if (item == saveButton) {
        saveEntry();
      }
    }
    if (id == FocusEvent.FOCUS_GAINED) {
      showHint(item.getName());
      if (Daten.efaConfig.getValueTouchScreenSupport()
          && item instanceof ItemTypeStringAutoComplete) {
        ((ItemTypeStringAutoComplete) item).showOrRemoveAutoCompletePopupWindow();
      }
      if (lastFocusedItem != null
          && isCoxOrCrewItem(lastFocusedItem)
          && !isCoxOrCrewItem(item)) {
        autoSelectBoatCaptain();
      }
      if (item == date) {
        if (isNewRecord
            && (isModeBaseOrAdmin() || getMode() == MODE_BOATHOUSE_LATEENTRY)) {
          date.setSelection(0, Integer.MAX_VALUE);
        }
      }
      if (item == phoneNr) {
        if (cox.isKnown() && phoneNr.getValueFromField().isEmpty()) {
          fillPhoneNr(cox);
        }
      }
      if (item == destination) {
        lastDestination = DestinationRecord.tryGetNameAndVariant(
            destination.getValueFromField().trim())[0];
      }
      if (item == distance) {
        if (isModeBoathouse() || (isModeBaseOrAdmin() && isNewRecord)) {
          distance.setSelection(0, Integer.MAX_VALUE);
        }
        if (!distance.isEditable() && distance.hasFocus()) {
          efaBaseFrameFocusManager.focusNextItem(distance, distance.getComponent());
        }
      }
    }
    if (id == FocusEvent.FOCUS_LOST) {
      showHint(null);
      lastFocusedItem = item;
      if (item == date) {
        checkStartOnlyToday(date);
        updateTimeInfoFields();
      }
      if (item == enddate) {
        updateTimeInfoFields();
      }
      if (item == boat || item == boatvariant) {
        currentBoatUpdateGui();
      }
      if (item == cox) {
        if (Daten.efaConfig.getValueAutoObmann() && isNewRecord
            && cox.getValueFromField().trim().length() > 0 && getBoatCaptain() == -1) {
          setBoatCaptain(0, true);
        }

      }
      if (item == crew[0]) {
        if (Daten.efaConfig.getValueAutoObmann() && isNewRecord && getBoatCaptain() == -1) {
          if (Daten.efaConfig.getValueDefaultObmann().equals(EfaConfig.OBMANN_BOW)
              && crew[0].getValueFromField().trim().length() > 0) {
            setBoatCaptain(1, true);
          }
        }
      }

      if (isModeBoathouse() && (item == starttime || item == endtime)) {
        if (item == starttime && !starttime.isSet()) {
          setTime(starttime, Daten.efaConfig.getValueEfaDirekt_plusMinutenAbfahrt(), null);
        }
        if (item == endtime && !endtime.isSet()) {
          setTime(endtime, -Daten.efaConfig.getValueEfaDirekt_minusMinutenAnkunft(),
              starttime.getTime());
        }
      }
      /*
       * if (isModeBoathouse() && (item == starttime || item == endtime)) {
       * setTime((ItemTypeTime)item, 0, null); }
       */
      if (item == destination) {
        boolean wasEditable = distance.isEditable();
        setDesinationDistance();
        if (distance.isEditable() && !wasEditable) {
          distance.requestFocus();
        }
        if (!distance.isEditable() && distance.hasFocus()) {
          efaBaseFrameFocusManager.focusNextItem(distance, distance.getComponent());
        }
      }
    }
    if (id == KeyEvent.KEY_PRESSED && event instanceof KeyEvent) {
      KeyEvent e = (KeyEvent) event;
      if ((e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) || // Ctrl-F
          (e.getKeyCode() == KeyEvent.VK_F5)) { // F5
        if (item instanceof ItemTypeLabelValue) {
          insertLastValue(e, (ItemTypeLabelValue) item);
        }
      }
      if ((e.isControlDown() && e.getKeyCode() == KeyEvent.VK_O)) { // Ctrl-O
        if (item instanceof ItemTypeLabelValue) {
          selectBoatCaptain(item.getName());
        }
      }
      if (item == comments) {
        ItemTypeHashtable<String> hash = Daten.efaConfig.getValueKeys();
        String[] k = hash.getKeysArray();
        if (k != null && k.length > 0) {
          for (String element : k) {
            if ((element.equals("F6") && e.getKeyCode() == KeyEvent.VK_F6
                && hash.get(element) != null)
                ||
                (element.equals("F7") && e.getKeyCode() == KeyEvent.VK_F7
                    && hash.get(element) != null)
                ||
                (element.equals("F8") && e.getKeyCode() == KeyEvent.VK_F8
                    && hash.get(element) != null)
                ||
                (element.equals("F9") && e.getKeyCode() == KeyEvent.VK_F9
                    && hash.get(element) != null)
                ||
                (element.equals("F10") && e.getKeyCode() == KeyEvent.VK_F10 && hash
                    .get(element) != null)
                ||
                (element.equals("F11")
                    && (e.getKeyCode() == KeyEvent.VK_F11 || e.getKeyCode() == KeyEvent.VK_STOP)
                    && hash
                    .get(element) != null)
                ||
                (element.equals("F12")
                    && (e.getKeyCode() == KeyEvent.VK_F12 || e.getKeyCode() == KeyEvent.VK_AGAIN)
                    && hash
                    .get(element) != null)) {
              comments.parseAndShowValue(comments.getValueFromField() + hash.get(element));
            }
          }
        }
      }
    }
    if (id == MouseEvent.MOUSE_CLICKED) {
      if (item instanceof ItemTypeLabelValue) {
        selectBoatCaptain(item.getName());
      }
    }
    if (id == ItemEvent.ITEM_STATE_CHANGED) {
      if (item == boatcaptain) {
        setBoatCaptain(getBoatCaptain(), false);
      }
      if (item == boatvariant) {
        int variant = EfaUtil.stringFindInt(boatvariant.getValueFromField(), -1);
        currentBoatUpdateGui(variant);
      }
    }
    if (id == ItemTypeLabelValue.ACTIONID_FIELD_EXPANDED && item == enddate) {
      starttimeInfoLabel.setVisible(true);
      endtimeInfoLabel.setVisible(true);
    }
    if (id == ItemTypeLabelValue.ACTIONID_FIELD_COLLAPSED && item == enddate) {
      starttimeInfoLabel.setVisible(false);
      endtimeInfoLabel.setVisible(false);
    }
  }

  private boolean checkUndAktualisiereHandyNrInPersonProfil() {
    // Nutzer fragen, ob Handy-Nummer gespeichert werden soll
    if (!isNewRecord) {
      return true;
    }
    if (phoneNr == null || phoneNr.getValue().isBlank()) {
      return true;
    }
    String action = International.getStringWithMnemonic("Fahrt beginnen");
    if (saveButton != null) {
      String buttonText = saveButton.getDescription();
      if (!action.contentEquals(buttonText)) {
        return true;
      }
    }
    boolean booleanAlleMenschenZumVormerkenDerHandyNummerAuffordern = Daten.efaConfig
        .getValueEfaDirekt_AlleMenschenZumVormerkenDerHandyNummerAuffordern();
    if (cox == null || !cox.isKnown()) {
      if (booleanAlleMenschenZumVormerkenDerHandyNummerAuffordern) {
        fragenUndLoggen(action);
      }
      return true;
    }
    PersonRecord person = findPerson(cox, getValidAtTimestamp(null));
    if (person == null) {
      if (booleanAlleMenschenZumVormerkenDerHandyNummerAuffordern) {
        fragenUndLoggen(action);
      }
      return true;
    }
    String antwort = person.checkUndAktualisiereHandyNr(phoneNr.getValue(),
        booleanAlleMenschenZumVormerkenDerHandyNummerAuffordern);
    if (antwort.contentEquals("noQuestion")) {
      return true; // Frage nicht möglich, also weiter
    }
    if (antwort.contentEquals("keinHinweisAufKürzelErwünscht")) {
      try {
        Logger.log(Logger.INFO, Logger.MSG_ABF_INFO,
            person.getFirstLastName() + ": kein Hinweis auf Kürzel erwünscht");
        Persons persons = Daten.project.getPersons(false);
        persons.data().update(person);
        return true; // Frage nicht möglich, also weiter
      } catch (EfaException e4) {
        String error = action + ": e4 " + e4.getLocalizedMessage();
        Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR, error);
        return true; // Frage nicht möglich, also weiter
      }
    }
    if (antwort.contentEquals("abbrechen")) {
      return false; // User wollte abbrechen, also STOP: stop saving loobook
    }
    if (!antwort.contains("saved")) {
      return false; // wrong answer!?! and change Phone in Dialog
    }

    // save entry
    String info = action + ": " + person.getFirstLastName();
    if (antwort.contentEquals("savedNew")) {
      info += " hat nun als TelefonNr '" + person.getHandy2() + "'";
    }
    if (antwort.contentEquals("savedOld")) {
      info += " hat weiterhin die TelefonNr '" + person.getHandy2() + "'";
    }
    if (antwort.contentEquals("savedEmpty")) {
      info += " hat nun kein Telefon " + person.getHandy2() + " " + person.getFestnetz1() + ",";
    }
    info += " und die Erlaubnis '" + person.isErlaubtTelefon() + "'";
    try {
      Logger.log(Logger.INFO, Logger.MSG_ABF_INFO, info);
      person.sendEmailConfirmation(person.getEmail(), "CONFIRM_SETPHONENR", info);
      Persons persons = Daten.project.getPersons(false);
      persons.data().update(person);
      return true; // TelefonNr wurde aktualisiert, weiter mit Fahrt beginnen
    } catch (EfaException e3) {
      String error = action + ": e3 " + e3.getLocalizedMessage();
      Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR, error);
      return true; // TelefonNr wurde aktualisiert, weiter mit Fahrt beginnen
    }
  }

  private void fragenUndLoggen(String action) {
    String info = checkUndAktualisiereHandyNr(action, phoneNr.getValue());
    String coxName = (cox != null) ? cox.getValue() + " (unbekannt)" : "Ein unbekanntes Mitglied";
    info = coxName + " hätte vielleicht gerne " + phoneNr + " gespeichert: " + info;
    Logger.log(Logger.INFO, Logger.MSG_ABF_INFO, info);
  }

  public String checkUndAktualisiereHandyNr(String action, String newPhone) {
    // true = nur zugesagte Leute werden korrigiert.
    // false = alle Leute werden gefragt, Ausnahme zugesagte Nummer stimmt noch
    String telnumAusProfil = International.getString("keine Nummer bzw nix"); // keine bzw. nix

    // weder noch
    String frage = International.getMessage(
        "Vorbelegung neu {newPhone} anstelle von {telnumAusProfil}", newPhone, telnumAusProfil);
    int antwort = Dialog.auswahlDialog(International.getString("Vorbelegung der Telefonnummer"),
        frage, newPhone + " vorschlagen", // 0 ja neue Nummer übernehmen
        "nix mehr vorschlagen", // 1 Erlaubnis entziehen
        telnumAusProfil + " vorschlagen"); // 2 = alte bisherige Nummer
    switch (antwort) {
      case 0: // neue Nummer zukünftig merken (rechts, default, selektiert)
        return "savedNew"; // muss noch gespeichert werden / persistiert
      case 1: // gar nix mehr vorschlagen
        return "savedEmpty"; // muss noch gespeichert werden / persistiert
      case 2: // alten Vorschlag beibehalten (links)
        return "savedEmpty"; // muss noch gespeichert werden / persistiert
      case 3: // hier könnte ein Button "abbrechen" rein...
        return "abbrechen"; // = nix tun
      case -1: // abbrechen = cancel = ESC = x // zurück, nochmal die Nummer ändern
        return "abbrechen"; // = nix tun
      default: // unbekannt
        return "abbrechen"; // = nix tun
    }
  }

  private void fillPhoneNr(ItemTypeStringAutoComplete nameItemAutoComplete) {
    ItemTypeString nameItemString = (ItemTypeString) nameItemAutoComplete;
    PersonRecord person = findPerson(nameItemString, getValidAtTimestamp(null));
    if (person == null) {
      return;
    }
    if (!person.isErlaubtTelefon()) {
      return;
    }
    String telnum = person.getHandy2();
    if (telnum == null || telnum.length() == 0) {
      telnum = person.getFestnetz1();
    }
    if (telnum == null || telnum.length() == 0) {
      return;
    }
    Logger.log(Logger.DEBUG, Logger.MSG_DEBUG_AUTOCOMPLETE,
        "Formular: TelNum für " + nameItemString + " automatisch eingetragen. "
            + person.isErlaubtTelefon());
    phoneNr.setValue(telnum);
  }

  private void checkStartOnlyToday(ItemTypeDate startTag) {
    // startTag.getValueFromGui();
    DataTypeDate today = DataTypeDate.today();
    if (!today.equals(startTag.getDate())) {
      if (_title.equals(International.getString("Neue Fahrt beginnen"))) {
        String msg = ""
            + International.getString("Modus") + ": " + _title + "\n"
            + International.getMessage("aber erst am {tag}", "" + startTag) + "\n"
            + International.getString("Fahrtbeginn muss heute sein") + ".\n"
            + International.getString("Andere Tage bitte reservieren");
        Dialog.meldung("Kleiner Produkthinweis", msg);
        date.setValueDate(today);
        date.showValue();
        date.requestFocus();
      }
    }
  }

  void showHint(String s) {
    if (s == null) {
      infoLabel.setText(" ");
      return;
    }
    if (s.equals(LogbookRecord.ENTRYID)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Laufende Nummer") + ">");
      return;
    }
    if (s.equals(LogbookRecord.DATE) || s.equals(LogbookRecord.ENDDATE)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Tag") + ">.<"
          + International.getString("Monat") + ">.<"
          + International.getString("Jahr") + ">");
      return;
    }
    if (s.equals(LogbookRecord.BOATNAME)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Bootsname") + ">");
      return;
    }
    if (s.equals(LogbookRecord.BOATVARIANT)) {
      infoLabel.setText(International.getString("Bitte auswählen")
          + ": " + International.getString("Bootsvariante"));
      return;
    }
    if (LogbookRecord.getCrewNoFromFieldName(s) >= 0) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + (Daten.efaConfig.getValueNameFormat().equals(EfaConfig.NAMEFORMAT_FIRSTLAST)
          ? "<" + International.getString("Vorname") + "> <"
          + International.getString("Nachname") + ">"
          : "<" + International.getString("Nachname") + ">,  <"
              + International.getString("Vorname") + ">"));
      return;
    }
    if (s.equals(LogbookRecord.BOATCAPTAIN)) {
      infoLabel.setText(International.getString("Bitte auswählen")
          + ": " + International.getString("verantwortlichen Obmann"));
      return;
    }
    if (s.equals(LogbookRecord.CONTACT)) {
      infoLabel.setText(International.getString("Bitte eingeben")
          + ": " + International.getString("Telefon für Rückfragen"));
      return;
    }
    if (s.equals(LogbookRecord.STARTTIME) || s.equals(LogbookRecord.ENDTIME)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Stunde") + ">:<"
          + International.getString("Minute") + ">");
      return;
    }
    if (s.equals(LogbookRecord.DESTINATIONNAME)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Fahrtziel oder Strecke") + ">");
      return;
    }
    if (s.equals(GUIITEM_ADDITIONALWATERS)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Weitere Gewässer") + ">");
      return;
    }
    if (s.equals(LogbookRecord.DISTANCE)) {
      infoLabel.setText(International.getString("Bitte eingeben") + ": "
          + "<" + International.getString("Länge der Fahrt") + ">"
          + " (" + DataTypeDistance.getAllUnitAbbrevationsAsString(true) + ")");
      return;
    }
    if (s.equals(LogbookRecord.COMMENTS)) {
      infoLabel.setText(International.getString("Bemerkungen eingeben oder frei lassen"));
      return;
    }
    if (s.equals("REMAININGCREWUP") || s.equals("REMAININGCREWDOWN")) {
      infoLabel.setText(International.getString("weitere Mannschaftsfelder anzeigen"));
      return;
    }
    if (s.equals("BOATDAMAGE")) {
      infoLabel.setText(International.getString("einen Schaden am Boot melden"));
      return;
    }
    if (s.equals("BOATNOTCLEANED")) {
      infoLabel.setText(International.getString("ein ungeputztes Boot melden"));
      return;
    }
    if (s.equals("SAVE")) {
      infoLabel.setText(International
          .getString("<Leertaste> drücken, um den Eintrag abzuschließen"));
      return;
    }
    infoLabel.setText(" ");
  }

  void insertLastValue(KeyEvent e, ItemTypeLabelValue item) {
    if (e == null || isModeBoathouse() || referenceRecord == null) {
      return;
    }
    if ((e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) || // Ctrl-F
        (e.getKeyCode() == KeyEvent.VK_F5)) { // F5
      setField(item, referenceRecord);
    }
  }

  private void setFieldEnabled(boolean enabled, boolean visible, IItemType item) {
    if (Daten.efaConfig.getValueEfaDirekt_eintragHideUnnecessaryInputFields()) {
      if (item instanceof ItemTypeStringAutoComplete) {
        ((ItemTypeStringAutoComplete) item).setVisibleSticky(visible);
      } else {
        item.setVisible(visible);
      }
    }
    item.setEditable(enabled);
    item.saveBackgroundColor(true);
  }

  private void setFieldEnabledDistance() {
    if (!isModeFinishOrLateEntry()) {
      return; // Zielabhängiges Enabled der BootsKm nur bei "Fahrt beenden" und "Nachtrag"
    }
    boolean enabled = !destination.isKnown()
        || !Daten.efaConfig.getValueEfaDirekt_eintragNichtAenderbarKmBeiBekanntenZielen();
    setFieldEnabled(enabled, true, distance);
  }

  private void currentBoatUpdateGuiBoathouse(boolean isCoxed, int numCrew) {
    setFieldEnabled(true, true, cox); // Steuermann immer an.
    if (!isCoxed) {
      cox.parseAndShowValue("");
      if (getBoatCaptain() == 0) {
        setBoatCaptain(-1, true);
      }
    }
    if (Daten.efaConfig.getValueEfaDirekt_eintragErlaubeNurMaxRudererzahl()) {
      for (int i = 1; i <= LogbookRecord.CREW_MAX; i++) {
        setFieldEnabled(false, false, crew[i - 1]);
        if (i > numCrew) {
          crew[i - 1].parseAndShowValue("");
          if (getBoatCaptain() == i) {
            setBoatCaptain(-1, true);
          }
        }
      }
    }
    setCrewRangeSelection(0);

    // "Weiterere Mannschaft"-Button ggf. ausblenden
    setFieldEnabled(true,
        numCrew > 8 || !Daten.efaConfig.getValueEfaDirekt_eintragErlaubeNurMaxRudererzahl(),
        remainingCrewUpButton);
    setFieldEnabled(true,
        numCrew > 8 || !Daten.efaConfig.getValueEfaDirekt_eintragErlaubeNurMaxRudererzahl(),
        remainingCrewDownButton);

    // "Obmann" ggf. ausblenden
    boolean makeVisible = isCoxed || numCrew > 1;
    setFieldEnabled(true, makeVisible, boatcaptain);

    // Bezeichnung für Mannschaftsfelder anpassen
    if (numCrew != 1 || isCoxed) {
      crew[0].setDescription(crew1defaultText);
    } else {
      crew[0].setDescription(International.getString("Name"));
    }
  }

  // wird von boot_focusLost aufgerufen, sowie vom FocusManager! (irgendwie unsauber, da bei <Tab>
  // doppelt...
  void currentBoatUpdateGui() {
    currentBoatUpdateGui(-1);
  }

  void currentBoatUpdateGui(int newvariant) {
    boat.getValueFromGui();

    if (Logger.isTraceOn(Logger.TT_GUI, 7)) {
      Logger.log(Logger.DEBUG, Logger.MSG_DEBUG_GUI_EFABASEFRAME,
          "currentBoatUpdateGui(" + newvariant + ") for boat: " + boat.getValue());
    }

    currentBoat = null;
    currentBoatTypeSeats = null;
    currentBoatTypeCoxing = null;
    currentBoatNumberOfSeats = 0;
    if (!isLogbookReady()) {
      return;
    }

    try {
      BoatRecord b = findBoat(getValidAtTimestamp(null));
      if (b != null) {
        currentBoat = b;
        if (Logger.isTraceOn(Logger.TT_GUI, 7)) {
          Logger.log(
              Logger.DEBUG,
              Logger.MSG_DEBUG_GUI_EFABASEFRAME,
              "currentBoatUpdateGui(" + newvariant + "): b = " +
                  b.getId() + "(" + b.getValidFrom() + "-" + b.getInvalidFrom() + "): "
                  + b.getQualifiedName());
        }
        // Update Boat Type selection
        updateBoatVariant(currentBoat, newvariant);
        int variant = EfaUtil.stringFindInt(boatvariant.toString(), -1);
        int idx = b.getVariantIndex(variant);
        currentBoatTypeSeats = b.getTypeSeats(idx);
        currentBoatTypeCoxing = b.getTypeCoxing(idx);
        currentBoatTypeCoxing = EfaTypes.TYPE_COXING_COXED; // Steuermann
        currentBoatNumberOfSeats = b.getNumberOfSeats(idx);
        if (Logger.isTraceOn(Logger.TT_GUI, 7)) {
          Logger
              .log(Logger.DEBUG, Logger.MSG_DEBUG_GUI_EFABASEFRAME,
                  "currentBoatUpdateGui(" + newvariant + "): "
                      + "variant=" + variant + ", idx=" + idx + ", seats=" + currentBoatTypeSeats
                      + ", coxing=" + currentBoatTypeCoxing + ", noofseats="
                      + currentBoatNumberOfSeats);
        }
      } else {
        if (Logger.isTraceOn(Logger.TT_GUI, 7)) {
          Logger.log(Logger.DEBUG, Logger.MSG_DEBUG_GUI_EFABASEFRAME,
              "currentBoatUpdateGui(" + newvariant + "): No boat found for boat " + boat.getValue()
                  +
                  " at " + getValidAtTimestamp(null) + ".");
        }
      }
    } catch (Exception e) {
      Logger.logdebug(e);
    }

    if (isModeBoathouse()) {
      boolean isCoxed = (currentBoatTypeCoxing == null || currentBoatTypeCoxing
          .equals(EfaTypes.TYPE_COXING_COXED));
      int numCrew = (currentBoatNumberOfSeats <= 0 ? LogbookRecord.CREW_MAX
          : currentBoatNumberOfSeats);
      currentBoatUpdateGuiBoathouse(isCoxed, numCrew);
      packFrame("currentBoatUpdateGui()");
    }
  }

  void selectBoatCaptain(String field) {
    int pos = LogbookRecord.getCrewNoFromFieldName(field);
    if (pos >= 0) {
      selectBoatCaptain(pos);
    }
  }

  void selectBoatCaptain(int pos) {
    if (getBoatCaptain() == pos) {
      setBoatCaptain(-1, true);
    } else {
      setBoatCaptain(pos, true);
    }
  }

  void setBoatCaptain(int pos, boolean updateListSelection) {
    ItemTypeStringAutoComplete field;
    for (int i = 0; i <= LogbookRecord.CREW_MAX; i++) {
      field = getCrewItem(i);
      if (i == pos) {
        field.setFieldFont(field.getLabelFont().deriveFont(Font.BOLD));
      } else {
        field.restoreFieldFont();
      }
    }
    if (updateListSelection) {
      if (pos >= 0 && pos <= LogbookRecord.CREW_MAX) {
        boatcaptain.parseAndShowValue(Integer.toString(pos));
      } else {
        boatcaptain.parseAndShowValue("");
      }
    }
  }

  int getBoatCaptain() {
    String val = boatcaptain.getValueFromField();
    if (val.length() == 0) {
      return -1;
    }
    try {
      return Integer.parseInt(val);
    } catch (Exception e) {
      Logger.logdebug(e);
      return -1;
    }
  }

  void autoSelectBoatCaptain() {
    autoSelectBoatCaptain(false);
  }

  void autoSelectBoatCaptain(boolean force) {
    if ((force || (Daten.efaConfig.getValueAutoObmann() && isNewRecord))
        && getBoatCaptain() == -1) {
      if (Daten.efaConfig.getValueDefaultObmann().equals(EfaConfig.OBMANN_STROKE)) {
        try {
          int anzRud = getNumberOfPersonsInBoat();
          if (anzRud > 0) {
            setBoatCaptain(anzRud, true);
          }
        } catch (Exception ee) {
          Logger.logdebug(ee);
        }
      }
    }

    // Wenn Angabe eines Obmanns Pflicht ist, soll auch im Einer immer der Obmann automatisch
    // selektiert werden,
    // unabhängig davon, ob Daten.efaConfig.autoObmann aktiviert ist oder nicht
    if ((force || (Daten.efaConfig.getValueEfaDirekt_eintragErzwingeObmann()
        && isNewRecord))
        && getBoatCaptain() == -1
        && cox.getValueFromField().trim().length() == 0
        && getNumberOfPersonsInBoat() == 1) {
      try {
        setBoatCaptain(1, true);
      } catch (Exception ee) {
        Logger.logdebug(ee);
      }
    }
  }

  void setCrewRangeSelection(int nr) {
    if (nr < 0) {
      nr = (LogbookRecord.CREW_MAX / 8) - 1;
    }
    if (nr >= LogbookRecord.CREW_MAX / 8) {
      nr = 0;
    }
    for (int i = 0; i < LogbookRecord.CREW_MAX; i++) {
      crew[i].setVisible(isModeBaseOrAdmin());
    }
    crewRangeSelection = nr;
    setCrewRangeSelectionColoring();
    packFrame("setCrewRangeSelection(nr)");
  }

  void setCrewRangeSelectionColoring() {
    boolean hiddenCrewFieldsSet = false;
    for (int i = 0; !hiddenCrewFieldsSet && i < LogbookRecord.CREW_MAX; i++) {
      if (i / 8 != crewRangeSelection) {
        if (crew[i].getValueFromField().trim().length() > 0) {
          hiddenCrewFieldsSet = true;
        }
      }
    }
    if (hiddenCrewFieldsSet) {
      remainingCrewUpButton.setBackgroundColor(Color.orange);
      remainingCrewDownButton.setBackgroundColor(Color.orange);
    } else {
      remainingCrewUpButton.restoreBackgroundColor();
      remainingCrewDownButton.restoreBackgroundColor();
    }
  }

  void setDefaultCrew(UUID crewId) {
    Crews crews = Daten.project.getCrews(false);
    CrewRecord r = crews.getCrew(crewId);
    if (r != null) {
      Persons persons = Daten.project.getPersons(false);
      if (currentBoatTypeCoxing != null
          && !currentBoatTypeCoxing.equals(EfaTypes.TYPE_COXING_COXLESS) &&
          r.getCoxId() != null) {
        PersonRecord p = persons.getPerson(r.getCoxId(), getValidAtTimestamp(null));
        if (p != null) {
          cox.parseAndShowValue(p.getQualifiedName());
        }
      }
      for (int i = 1; i <= currentBoatNumberOfSeats && i <= LogbookRecord.CREW_MAX; i++) {
        UUID id = r.getCrewId(i);
        if (id != null) {
          PersonRecord p = persons.getPerson(id, getValidAtTimestamp(null));
          if (p != null) {
            crew[i - 1].parseAndShowValue(p.getQualifiedName());
          }
        }
      }
      if ((r.getBoatCaptainPosition() == 0 && currentBoatTypeCoxing != null
          && !currentBoatTypeCoxing.equals(EfaTypes.TYPE_COXING_COXLESS))
          || (r.getBoatCaptainPosition() > 0
          && r.getBoatCaptainPosition() <= currentBoatNumberOfSeats)) {
        boatcaptain.parseAndShowValue(Integer.toString(r.getBoatCaptainPosition()));
      }
    }
  }

  // =========================================================================
  // Window-related methods
  // =========================================================================

  private void this_windowClosing(WindowEvent e) {
    cancel();
  }

  private void this_windowIconified(WindowEvent e) {
    if (isModeBoathouse()) {
      // startBringToFront(true); not needed any more
    }
  }

  @Override
  public boolean cancel() {
    if (isModeBoathouse()) {
      efaBoathouseHideEfaFrame();
      return true;
    }

    if (!promptSaveChangesOk()) {
      return false;
    }

    if (isModeAdmin()) {
      super.cancel();
      return true;
    }

    // @efaconfig if (!Daten.efaConfig.writeFile()) {
    // @efaconfig LogString.logError_fileWritingFailed(Daten.efaConfig.getFileName(),
    // International.getString("Konfigurationsdatei"));
    // @efaconfig }
    super.cancel();
    Daten.haltProgram(0);
    return true;
  }

  // =========================================================================
  // FocusManager
  // =========================================================================

  class EfaBaseFrameFocusManager extends DefaultFocusManager {

    private EfaBaseFrame efaBaseFrame;
    private FocusManager fm;

    public EfaBaseFrameFocusManager(EfaBaseFrame efaBaseFrame, FocusManager fm) {
      this.efaBaseFrame = efaBaseFrame;
      this.fm = fm;
    }

    private IItemType getItem(Component c) {
      if (c == null) {
        return null;
      }
      if (c == efaBaseFrame.entryno.getComponent()) {
        return efaBaseFrame.entryno;
      }
      if (c == efaBaseFrame.date.getComponent()) {
        return efaBaseFrame.date;
      }
      if (c == efaBaseFrame.enddate.getComponent()) {
        return efaBaseFrame.enddate;
      }
      if (c == efaBaseFrame.boat.getComponent() ||
          c == efaBaseFrame.boat.getButton()) {
        return efaBaseFrame.boat;
      }
      if (c == efaBaseFrame.boatvariant.getComponent()) {
        return efaBaseFrame.boatvariant;
      }
      if (c == efaBaseFrame.cox.getComponent() ||
          c == efaBaseFrame.cox.getButton()) {
        return efaBaseFrame.cox;
      }
      for (ItemTypeStringAutoComplete element : efaBaseFrame.crew) {
        if (c == element.getComponent() ||
            c == element.getButton()) {
          return element;
        }
      }
      if (c == efaBaseFrame.boatcaptain.getComponent()) {
        return efaBaseFrame.boatcaptain;
      }
      if (c == efaBaseFrame.phoneNr.getComponent()) {
        return efaBaseFrame.phoneNr;
      }
      if (c == efaBaseFrame.starttime.getComponent()) {
        return efaBaseFrame.starttime;
      }
      if (c == efaBaseFrame.endtime.getComponent()) {
        return efaBaseFrame.endtime;
      }
      if (c == efaBaseFrame.destination.getComponent() ||
          c == efaBaseFrame.destination.getButton()) {
        return efaBaseFrame.destination;
      }
      if (c == efaBaseFrame.waters.getComponent() ||
          c == efaBaseFrame.waters.getButton()) {
        return efaBaseFrame.waters;
      }
      if (c == efaBaseFrame.distance.getComponent()) {
        return efaBaseFrame.distance;
      }
      if (c == efaBaseFrame.comments.getComponent()) {
        return efaBaseFrame.comments;
      }
      if (c == efaBaseFrame.remainingCrewUpButton.getComponent()) {
        return efaBaseFrame.remainingCrewUpButton;
      }
      if (c == efaBaseFrame.remainingCrewDownButton.getComponent()) {
        return efaBaseFrame.remainingCrewDownButton;
      }
      if (c == efaBaseFrame.boatDamageButton.getComponent()) {
        return efaBaseFrame.boatDamageButton;
      }
      if (c == efaBaseFrame.boatNotCleanedButton.getComponent()) {
        return efaBaseFrame.boatNotCleanedButton;
      }
      if (c == efaBaseFrame.saveButton.getComponent()) {
        return efaBaseFrame.saveButton;
      }
      return null;
    }

    void focusItem(IItemType item, Component cur, int direction) {
      // fSystem.out.println("focusItem(" + item.getName() + ")");
      if (item == efaBaseFrame.starttime && Daten.efaConfig.getValueSkipUhrzeit()) {
        focusItem(efaBaseFrame.destination, cur, direction);
      } else if (item == efaBaseFrame.endtime && Daten.efaConfig.getValueSkipUhrzeit()) {
        focusItem(efaBaseFrame.destination, cur, direction);
      } else if (item == efaBaseFrame.destination && Daten.efaConfig.getValueSkipZiel()) {
        focusItem(efaBaseFrame.distance, cur, direction);
      } else if (item == efaBaseFrame.comments && Daten.efaConfig.getValueSkipBemerk()) {
        focusItem(efaBaseFrame.saveButton, cur, direction);
      } else if (item.isEnabled() && item.isVisible() && item.isEditable()) {
        item.requestFocus();
      } else {
        if (direction > 0) {
          focusNextItem(item, cur);
        } else {
          focusPreviousItem(item, cur);
        }
      }
    }

    public void focusNextItem(IItemType item, Component cur) {
      // System.out.println("focusNextItem(" + item.getName() + ")");

      // LFDNR --> boat
      if (item == efaBaseFrame.entryno) {
        focusItem(efaBaseFrame.boat, cur, 1);
        return;
      }

      // BOOT --> date
      if (item == efaBaseFrame.boat) {
        efaBaseFrame.boat.getValueFromGui();
        efaBaseFrame.currentBoatUpdateGui();
        if (!(cur instanceof JButton) && efaBaseFrame.boat.getValue().length() > 0
            && !efaBaseFrame.boat.isKnown() && !efaBaseFrame.isModeBoathouse()) {
          efaBaseFrame.boat.requestButtonFocus();
        } else if (efaBaseFrame.boatvariant.isVisible()) {
          focusItem(efaBaseFrame.boatvariant, cur, 1);
        } else {
          if (efaBaseFrame.currentBoatTypeCoxing != null
              && efaBaseFrame.currentBoatTypeCoxing.equals(EfaTypes.TYPE_COXING_COXLESS)) {
            focusItem(efaBaseFrame.crew[0], cur, 1);
          } else {
            focusItem(efaBaseFrame.cox, cur, 1);
          }
        }
        return;
      }

      // BOOTVARIANT --> date
      if (item == efaBaseFrame.boatvariant) {
        efaBaseFrame.boatvariant.getValueFromGui();
        efaBaseFrame.currentBoatUpdateGui();
        if (efaBaseFrame.currentBoatTypeCoxing != null
            && efaBaseFrame.currentBoatTypeCoxing.equals(EfaTypes.TYPE_COXING_COXLESS)) {
          focusItem(efaBaseFrame.crew[0], cur, 1);
        } else {
          focusItem(efaBaseFrame.cox, cur, 1);
        }
        return;
      }

      // DATUM --> cox
      if (item == efaBaseFrame.date) {
        focusItem(efaBaseFrame.starttime, cur, 1);
        return;
      }

      // ABFAHRT --> endtime
      if (item == efaBaseFrame.starttime) {
        focusItem(efaBaseFrame.endtime, cur, 1);
        return;
      }

      // ANKUNFT --> cox
      if (item == efaBaseFrame.endtime) {
        focusItem(efaBaseFrame.cox, cur, 1);
        return;
      }

      // STEUERMANN = COX --> phone
      if (item == efaBaseFrame.cox) {
        efaBaseFrame.cox.getValueFromGui();
        if (!(cur instanceof JButton) && efaBaseFrame.cox.getValue().length() > 0
            && !efaBaseFrame.cox.isKnown() && !efaBaseFrame.isModeBoathouse()) {
          efaBaseFrame.cox.requestButtonFocus();
        } else {
          focusItem(efaBaseFrame.phoneNr, cur, 1);
        }
        return;
      }

      // CONTACT = phoneNr --> save
      if (item == efaBaseFrame.phoneNr) {
        efaBaseFrame.phoneNr.getValueFromGui();
        if (efaBaseFrame.phoneNr.getValue().length() == 0) {
          efaBaseFrame.phoneNr.requestFocus();
        } else {
          focusItem(efaBaseFrame.saveButton, cur, 1);
        }
        return;
      }

      // MANNSCHAFT
      for (int i = 0; i < efaBaseFrame.crew.length; i++) {
        if (item == efaBaseFrame.crew[i]) {
          efaBaseFrame.crew[i].getValueFromGui();
          if (!(cur instanceof JButton) && efaBaseFrame.crew[i].getValue().length() > 0
              && !efaBaseFrame.crew[i].isKnown() && !efaBaseFrame.isModeBoathouse()) {
            efaBaseFrame.crew[i].requestButtonFocus();
          } else if (efaBaseFrame.crew[i].getValueFromField().trim().length() == 0) {
            focusItem(efaBaseFrame.starttime, cur, 1);
          } else if (efaBaseFrame.currentBoatTypeSeats != null && i + 1 < efaBaseFrame.crew.length
              &&
              i + 1 == EfaTypes.getNumberOfRowers(efaBaseFrame.currentBoatTypeSeats) &&
              efaBaseFrame.crew[i + 1].getValueFromField().trim().length() == 0) {
            focusItem(efaBaseFrame.starttime, cur, 1);
          } else if (i + 1 < efaBaseFrame.crew.length) {
            focusItem(efaBaseFrame.crew[i + 1], cur, 1);
          } else {
            focusItem(efaBaseFrame.starttime, cur, 1);
          }
          return;
        }
      }

      // ZIEL
      if (item == efaBaseFrame.destination) {
        if (!(cur instanceof JButton) && efaBaseFrame.destination.getValue().length() > 0
            && !efaBaseFrame.destination.isKnown() && !efaBaseFrame.isModeBoathouse()) {
          efaBaseFrame.destination.requestButtonFocus();
        } else {
          focusItem(efaBaseFrame.waters, cur, 1);
        }
        return;
      }

      // WATERS
      if (item == efaBaseFrame.waters) {
        if (!(cur instanceof JButton) && efaBaseFrame.waters.getValue().length() > 0
            && !efaBaseFrame.waters.isKnown() && !efaBaseFrame.isModeBoathouse()) {
          efaBaseFrame.waters.requestButtonFocus();
        } else {
          focusItem(efaBaseFrame.distance, cur, 1);
        }
        return;
      }

      // BOOTS-KM
      if (item == efaBaseFrame.distance) {
        focusItem(efaBaseFrame.saveButton, cur, 1);
        return;
      }

      // COMMENTS
      if (item == efaBaseFrame.comments) {
        focusItem(efaBaseFrame.saveButton, cur, 1);
        return;
      }

      // ADD-BUTTON
      if (item == efaBaseFrame.saveButton) {
        focusItem(efaBaseFrame.entryno, cur, 1);
        return;
      }

      // other
      fm.focusNextComponent(cur);
    }

    public void focusPreviousItem(IItemType item, Component cur) {
      if (item == efaBaseFrame.entryno) {
        focusItem(efaBaseFrame.saveButton, cur, -1);
        return;
      }
      if (item == efaBaseFrame.date) {
        focusItem(efaBaseFrame.boat, cur, -1);
        return;
      }
      if (item == efaBaseFrame.cox) {
        focusItem(efaBaseFrame.crew[efaBaseFrame.crew.length - 1], cur, -1);
        return;
      }
      if (item == efaBaseFrame.phoneNr) {
        focusItem(efaBaseFrame.cox, cur, -1);
        return;
      }
      for (int i = 0; i < efaBaseFrame.crew.length; i++) {
        if (item == efaBaseFrame.crew[i]) {
          focusItem((i == 0 ? efaBaseFrame.endtime : efaBaseFrame.crew[i - 1]), cur, -1);
          return;
        }
      }
      if (item == efaBaseFrame.cox) {
        for (int i = 0; i < 8; i++) {
          if (efaBaseFrame.crew[i + efaBaseFrame.crewRangeSelection * 8]
              .getValueFromField().trim().length() == 0 || i == 7) {
            focusItem(efaBaseFrame.crew[i + efaBaseFrame.crewRangeSelection * 8], cur, -1);
            return;
          }
        }
      }
      if (item == efaBaseFrame.waters) {
        focusItem(efaBaseFrame.destination, cur, -1);
        return;
      }
      if (item == efaBaseFrame.distance) {
        focusItem(efaBaseFrame.waters, cur, -1);
        return;
      }
      if (item == efaBaseFrame.comments) {
        focusItem(efaBaseFrame.distance, cur, -1);
        return;
      }
      if (item == efaBaseFrame.saveButton) {
        focusItem(efaBaseFrame.phoneNr, cur, -1);
        return;
      }

      // other
      fm.focusPreviousComponent(cur);
    }

    @Override
    public void focusNextComponent(Component cur) {
      // System.out.println("focusNextComponent("+cur+")");
      IItemType item = getItem(cur);
      if (item != null) {
        focusNextItem(item, cur);
      } else {
        fm.focusNextComponent(cur);
      }
    }

    @Override
    public void focusPreviousComponent(Component cur) {
      // System.out.println("focusPreviousComponent("+cur+")");
      IItemType item = getItem(cur);
      if (item != null) {
        focusPreviousItem(item, cur);
      } else {
        fm.focusPreviousComponent(cur);
      }
    }

  }

  // =========================================================================
  // efaBoathouse methods
  // =========================================================================

  public void setDataForAdminAction(Logbook logbook, AdminRecord admin, AdminDialog adminDialog) {
    setMode(MODE_ADMIN);
    this.logbook = logbook;
    this.admin = admin;
    this.adminDialog = adminDialog;
  }

  boolean setDataForBoathouseAction(ItemTypeBoatstatusList.BoatListItem action, Logbook logbook) {
    setMode(action.mode);
    openLogbook(logbook);
    if (getMode() == MODE_BOATHOUSE_START_CORRECT) {
      correctSessionLastBoatStatus = action.boatStatus;
    } else {
      correctSessionLastBoatStatus = null;
    }
    efaBoathouseAction = action;
    clearAllBackgroundColors();
    switch (getMode()) {
      case MODE_BOATHOUSE_START:
        return efaBoathouseStartSession(action);
      case MODE_BOATHOUSE_START_CORRECT:
        return efaBoathouseCorrectSession(action);
      case MODE_BOATHOUSE_FINISH:
        return efaBoathouseFinishSession(action);
      case MODE_BOATHOUSE_LATEENTRY:
        return efaBoathouseLateEntry(action);
      case MODE_BOATHOUSE_ABORT:
        return efaBoathouseAbortSession(action);
      default:
        Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR,
            "setDataForBoathouseAction(): unreachable switch: "
                + "getMode() action = " + action);
        break;
    }
    return false;
  }

  private void efaBoathouseSetPersonAndBoat(ItemTypeBoatstatusList.BoatListItem item) {
    if (item.person != null) {
      crew[0].parseAndShowValue(item.person.getQualifiedName());
    }
    if (item.boat != null && item.boat.getPopupfrage() != null) {
      //Titel_Einweisung_Profi_Boote
      int yesNoDialog = Dialog.yesNoDialog(
              International.getString("Titel Einweisung Boote"),
              "  " + item.boat.getQualifiedName() + "\n\r"
                      + item.boat.getPopupfrage() + "\n\r  "
                      + Daten.efaTypes.getValue(EfaTypes.CATEGORY_NUMSEATS,
                      item.boat.getTypeSeats(0)));
      if (yesNoDialog != Dialog.YES) {
        Dialog.infoDialog(Daten.efaTypes.getValue(EfaTypes.CATEGORY_NUMSEATS,
                        item.boat.getTypeSeats(0)),
                International.getMessage("Ansprache der {ansprechperson}",
                        item.boat.getAnsprechperson()));
        item.boat = null;
      }
    }
    else if (item.boat != null && item.boat.getTypeSeats(0).equals(
            International.getString("Profi Boote Kontrollnummer"))) {
      int yesNoDialog = Dialog.yesNoDialog(
              International.getString("Titel Einweisung Profi Boote"),
              "  " + item.boat.getQualifiedName() + "\n\r"
                      + International.getString("Frage Einweisung Profi Boote") + "\n\r  "
                      + Daten.efaTypes.getValue(EfaTypes.CATEGORY_NUMSEATS,
                      item.boat.getTypeSeats(0)));
      if (yesNoDialog != Dialog.YES) {
        Dialog.infoDialog(Daten.efaTypes.getValue(EfaTypes.CATEGORY_NUMSEATS,
                item.boat.getTypeSeats(0)),
                International.getString("Termin Einweisung Profi Boote"));
        item.boat = null;
      }
    }
    if (item.boat != null) {
      boat.parseAndShowValue(item.boat.getQualifiedName());
      if (item.boatVariant >= 0) {
        updateBoatVariant(item.boat, item.boatVariant);
      }
      currentBoatUpdateGui((item.boatVariant >= 0 ? item.boatVariant : -1));
      if (cox.isEditable()) {
        setRequestFocus(cox);
      } else {
        setRequestFocus(crew[0]);
      }
    }
    if (item.boat == null) {
      currentBoatUpdateGui();
      setRequestFocus(boat);
    }
  }

  boolean efaBoathouseStartSession(ItemTypeBoatstatusList.BoatListItem item) {
    setTitle(International.getString("Neue Fahrt beginnen"));
    saveButton.setDescription(International.getStringWithMnemonic("Fahrt beginnen"));
    createNewRecord(false);
    date.parseAndShowValue(EfaUtil.getCurrentTimeStampDD_MM_YYYY());
    setTime(starttime, Daten.efaConfig.getValueEfaDirekt_plusMinutenAbfahrt(), null);
    setTime(endtime, Daten.efaConfig.getValueEfaDirekt_plusMinutenAbfahrt() + 119, starttime.getTime());

    setFieldEnabled(false, false, entryno);
    setFieldEnabled(true, true, date);
    setFieldEnabled(item.boat == null, true, boat);
    setFieldEnabled(true, true, phoneNr);
    if (Daten.efaConfig.getValueEfaDirekt_eintragNichtAenderbarUhrzeit()) {
      setFieldEnabled(false, true, starttime);
      setFieldEnabled(false, false, endtime);
    } else {
      setFieldEnabled(true, true, starttime);
      setFieldEnabled(true, true, endtime);
    }
    if (isBootshausOH()) {
      setFieldEnabled(true, true, destination);
    } else {
      setFieldEnabled(false, false, destination);
    }
    setFieldEnabled(false, false, distance);
    setFieldEnabled(true, true, comments);
    setFieldEnabled(true, Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton(), boatDamageButton);
    setFieldEnabled(true, Daten.efaConfig.getShowBoatNotCleanedButton(), boatNotCleanedButton);

    efaBoathouseSetPersonAndBoat(item);
    updateTimeInfoFields();
    return true;
  }

  boolean efaBoathouseCorrectSession(ItemTypeBoatstatusList.BoatListItem item) {
    setTitle(International.getString("Fahrt korrigieren"));
    saveButton.setDescription(International.getStringWithMnemonic("Fahrt korrigieren"));
    currentRecord = null;
    try {
      currentRecord = logbook.getLogbookRecord(item.boatStatus.getEntryNo());
    } catch (Exception e) {
      Logger.log(e);
    }
    if (currentRecord == null) {
      String msg = International.getString("Fahrt korrigieren")
          + ": "
          + International.getMessage(
            "Die gewählte Fahrt #{lfdnr} ({boot}) konnte nicht gefunden werden!",
            (item != null && item.boatStatus != null && item.boatStatus.getEntryNo() != null
                ? item.boatStatus
                .getEntryNo().toString()
                : "null"),
            (item != null && item.boat != null ? item.boat.getQualifiedName()
                : (item != null ? item.text : "null")));
      logBoathouseEvent(Logger.ERROR, Logger.MSG_ERR_NOLOGENTRYFORBOAT, msg, null);
      return false;
    }
    setFields(currentRecord);

    setFieldEnabled(false, false, entryno);
    setFieldEnabled(true, true, date);
    setFieldEnabled(true, true, boat);
    setFieldEnabled(true, true, phoneNr);
    if (Daten.efaConfig.getValueEfaDirekt_eintragNichtAenderbarUhrzeit()) {
      setFieldEnabled(false, true, starttime);
      setFieldEnabled(false, false, endtime);
    } else {
      setFieldEnabled(true, true, starttime);
      setFieldEnabled(true, true, endtime);
    }
    if (isBootshausOH()) {
      setFieldEnabled(true, true, destination);
    } else {
      setFieldEnabled(false, false, destination);
    }
    setFieldEnabled(false, false, distance);
    setFieldEnabled(true, true, comments);
    setFieldEnabled(true, Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton(),
        boatDamageButton);
    setFieldEnabled(true, Daten.efaConfig.getShowBoatNotCleanedButton(), boatNotCleanedButton);

    currentBoatUpdateGui(
        (currentRecord.getBoatVariant() >= 0 ? currentRecord.getBoatVariant() : -1));
    updateTimeInfoFields();
    setRequestFocus(boat);

    return true;
  }

  private boolean isBootshausOH() {
    if (boat == null) {
      return false;
    }
    String myData = boat.getValueFromField();
    boolean istBootshausOH = myData.equals(BoatRecord.BOOTSHAUS_NAME);
    return istBootshausOH;
  }

  boolean efaBoathouseFinishSession(ItemTypeBoatstatusList.BoatListItem item) {
    setTitle(International.getString("Fahrt beenden"));
    saveButton.setDescription(International.getStringWithMnemonic("Fahrt beenden"));
    currentRecord = null;
    try {
      currentRecord = logbook.getLogbookRecord(item.boatStatus.getEntryNo());
    } catch (Exception e) {
      Logger.log(e);
    }
    if (currentRecord == null) {
      String msg = International.getString("Fahrtende") + ": "
          + International.getMessage(
          "Die gewählte Fahrt #{lfdnr} ({boot}) konnte nicht gefunden werden!",
          (item != null && item.boatStatus != null
              && item.boatStatus.getEntryNo() != null
              ? item.boatStatus.getEntryNo().toString()
              : "null"),
          (item != null && item.boat != null
              ? item.boat.getQualifiedName()
              : (item != null ? item.text : "null")));
      logBoathouseEvent(Logger.ERROR, Logger.MSG_ERR_NOLOGENTRYFORBOAT, msg, null);
      return false;
    }
    setFields(currentRecord);
    setTime(endtime, -Daten.efaConfig.getValueEfaDirekt_minusMinutenAnkunft(),
        currentRecord.getStartTime());
    setDesinationDistance();

    setFieldEnabled(false, false, entryno);
    setFieldEnabled(true, true, date);
    setFieldEnabled(false, true, boat);
    setFieldEnabled(false, true, phoneNr);
    if (Daten.efaConfig.getValueEfaDirekt_eintragNichtAenderbarUhrzeit()) {
      setFieldEnabled(false, true, starttime);
      setFieldEnabled(false, true, endtime);
    } else {
      setFieldEnabled(true, true, starttime);
      setFieldEnabled(true, true, endtime);
    }
    if (isBootshausOH()) {
      setFieldEnabled(true, true, destination);
    } else {
      setFieldEnabled(false, false, destination);
    }
    setFieldEnabled(false, false, distance);
    setFieldEnabled(true, true, comments);
    setFieldEnabled(true, Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton(),
        boatDamageButton);
    setFieldEnabled(true, Daten.efaConfig.getShowBoatNotCleanedButton(), boatNotCleanedButton);

    currentBoatUpdateGui(
        (currentRecord.getBoatVariant() >= 0 ? currentRecord.getBoatVariant() : -1));
    updateTimeInfoFields();
    setRequestFocus(endtime);

    return true;
  }

  boolean efaBoathouseLateEntry(ItemTypeBoatstatusList.BoatListItem item) {
    setTitle(International.getString("Nachtrag"));
    saveButton.setDescription(International.getStringWithMnemonic("Nachtrag"));
    createNewRecord(false);

    setFieldEnabled(false, false, entryno);
    setFieldEnabled(true, true, date);
    setFieldEnabled(true, true, boat);
    setFieldEnabled(false, true, phoneNr);
    setFieldEnabled(true, true, starttime);
    setFieldEnabled(true, true, endtime);
    if (isBootshausOH()) {
      setFieldEnabled(true, true, destination);
    } else {
      setFieldEnabled(false, false, destination);
    }
    setFieldEnabled(false, false, distance);
    setFieldEnabled(true, true, comments);
    setFieldEnabled(true, Daten.efaConfig.getValueEfaDirekt_showBootsschadenButton(),
        boatDamageButton);
    setFieldEnabled(true, Daten.efaConfig.getShowBoatNotCleanedButton(), boatNotCleanedButton);

    efaBoathouseSetPersonAndBoat(item);
    updateTimeInfoFields();
    setRequestFocus(date);
    return true;
  }

  boolean efaBoathouseAbortSession(ItemTypeBoatstatusList.BoatListItem item) {
    currentRecord = null;
    try {
      currentRecord = logbook.getLogbookRecord(item.boatStatus.getEntryNo());
    } catch (Exception e) {
      Logger.log(e);
    }
    if (currentRecord == null) {
      String msg = International.getString("Fahrtende")
          + ": "
          +
          International
              .getMessage(
                  "Die gewählte Fahrt #{lfdnr} ({boot}) konnte nicht gefunden werden!",
                  (item != null && item.boatStatus != null && item.boatStatus.getEntryNo() != null
                      ? item.boatStatus
                      .getEntryNo().toString()
                      : "null"),
                  (item != null && item.boat != null ? item.boat.getQualifiedName()
                      : (item != null ? item.text : "null")));
      logBoathouseEvent(Logger.ERROR, Logger.MSG_ERR_NOLOGENTRYFORBOAT, msg, null);
      return false;
    }
    /*
     * ** the following code has been moved to in 2.0.5_01 to prevent errors in remote access; **
     * logbook record will now be deleted after status has been updated boolean checks =
     * logbook.data().isPreModifyRecordCallbackEnabled(); try {
     * logbook.data().setPreModifyRecordCallbackEnabled(false); // otherwise we couldn't delete the
     * record before we change the status logbook.data().delete(currentRecord.getKey()); }
     * catch(Exception e) { Dialog.error(e.toString()); return false; }
     * logbook.data().setPreModifyRecordCallbackEnabled(checks);
     */
    return true;
  }

  private void updateBoatStatus(boolean success, int mode) {
    // log this action
    if (success) {
      switch (mode) {
        case MODE_BOATHOUSE_START:
          logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_TRIPSTART,
              International.getString("Fahrtbeginn") + " (" + currentRecord.getStartTime() + ")",
              currentRecord);
          break;
        case MODE_BOATHOUSE_START_CORRECT:
          logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_TRIPSTART_CORR,
              International.getString("Fahrtbeginn korrigiert"),
              currentRecord);
          break;
        case MODE_BOATHOUSE_FINISH:
          logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_TRIPEND,
              International.getString("Fahrtende") + " (" + currentRecord.getEndTime() + ")",
              currentRecord);
          break;
        case MODE_BOATHOUSE_LATEENTRY:
          logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_TRIPLATEREC,
              International.getString("Nachtrag"),
              currentRecord);
          break;
        case MODE_BOATHOUSE_ABORT:
          logBoathouseEvent(Logger.INFO, Logger.MSG_EVT_TRIPABORT,
              International.getString("Fahrtabbruch"),
              currentRecord);
          break;
        default:
          Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR,
              "updateBoatStatus(): unreachable switch: " + "mode = " + mode);
          break;
      }
    } else {
      logBoathouseEvent(Logger.ERROR, Logger.MSG_EVT_ERRORSAVELOGBOOKENTRY,
          International.getString("Fahrtenbucheintrag konnte nicht gespeichert werden."),
          currentRecord);
    }

    // Update boat status
    if (success && currentRecord != null &&
        mode != MODE_BOATHOUSE_LATEENTRY &&
        (efaBoathouseAction != null || isModeBaseOrAdmin())) {
      long tstmp = currentRecord.getValidAtTimestamp();
      BoatStatus boatStatus = Daten.project.getBoatStatus(false);
      BoatRecord boatRecord = currentRecord.getBoatRecord(tstmp);
      BoatStatusRecord boatStatusRecord = (boatRecord != null
          ? boatStatus.getBoatStatus(boatRecord.getId())
          : null);

      // figure out new status information
      String newStatus = null;
      String newShowInList = null; // if not explicitly set, this boat will appear in the list
      // determined by its status
      DataTypeIntString newEntryNo = null;
      String newComment = null;
      if (efaBoathouseAction != null) {
        mode = efaBoathouseAction.mode;
      }
      switch (mode) {
        case MODE_BOATHOUSE_START:
        case MODE_BOATHOUSE_START_CORRECT:
          newStatus = BoatStatusRecord.STATUS_ONTHEWATER;
          newEntryNo = currentRecord.getEntryId();
          newComment = BoatStatusRecord.createStatusString(
              currentRecord.getDestinationAndVariantName(tstmp),
              currentRecord.getDate(),
              currentRecord.getStartTime(),
              currentRecord.getAllCoxAndCrewAsNameString(),
              currentRecord.getEndDate(),
              currentRecord.getEndTime());
          if (BoatStatusRecord.isOnTheWaterShowNotAvailable(currentRecord.getEndDate())) {
            newShowInList = BoatStatusRecord.STATUS_NOTAVAILABLE;
          }
          break;
        case MODE_BOATHOUSE_FINISH:
        case MODE_BOATHOUSE_ABORT:
          newStatus = BoatStatusRecord.STATUS_AVAILABLE;
          newComment = "";
          break;
        case MODE_BOATHOUSE_LATEENTRY:
          break;
        default:
          Logger.log(Logger.ERROR, Logger.MSG_ABF_ERROR,
              "updateBoatStatus(): unreachable switch: " + "mode = " + mode);
          break;
      }

      boolean newBoatStatusRecord = false;
      if (boatRecord != null && boatStatusRecord == null) {
        // oops, this shouldn't happen!
        String msg = International.getMessage("Kein Bootsstatus für Boot {boat} gefunden.",
            boatRecord.getQualifiedName());
        logBoathouseEvent(Logger.ERROR, Logger.MSG_EVT_ERRORNOBOATSTATUSFORBOAT,
            msg, currentRecord);
        Dialog.error(msg);
      } else {
        if (boatStatusRecord == null) {
          // unknown boat
          boatStatusRecord = (efaBoathouseAction != null ? efaBoathouseAction.boatStatus : null);

          // it could be that a session has been corrected and efaBoathouseAction.boatStatus
          // is actually the status of a real boat; if that's the case, then create a new
          // boat status for the unknown boat
          if (boatStatusRecord != null && !boatStatusRecord.getUnknownBoat()) {
            boatStatusRecord = null;
          }

          if (boatStatusRecord == null
              && (isModeStartOrStartCorrect())) {
            // create new status record for unknown boat
            boatStatusRecord = boatStatus.createBoatStatusRecord(UUID.randomUUID(),
                currentRecord.getBoatAsName());
            newBoatStatusRecord = true;
          }
          if (boatStatusRecord != null) {
            boatStatusRecord.setUnknownBoat(true);
          }
        }
      }

      if (boatStatusRecord != null) {
        if (newStatus != null) {
          boatStatusRecord.setCurrentStatus(newStatus);
        }
        if (newShowInList != null) {
          boatStatusRecord.setShowInList(newShowInList);
        } else {
          boatStatusRecord.setShowInList(null);
        }
        if (newEntryNo != null) {
          boatStatusRecord.setEntryNo(newEntryNo);
          boatStatusRecord.setLogbook(logbook.getName());
        } else {
          boatStatusRecord.setEntryNo(null);
          boatStatusRecord.setLogbook(null);
        }
        if (newComment != null) {
          boatStatusRecord.setComment(newComment);
        }
        boatStatusRecord.setBoatText(currentRecord.getBoatAsName());
        try {
          if (boatStatusRecord.getUnknownBoat() && newStatus != null &&
              !newStatus.equals(BoatStatusRecord.STATUS_ONTHEWATER)) {
            boatStatus.data().delete(boatStatusRecord.getKey());
          } else {
            if (newBoatStatusRecord) {
              boatStatus.data().add(boatStatusRecord);
            } else {
              boatStatus.data().update(boatStatusRecord);
            }

            // check whether we have changed the boat during this dialog (e.g. StartCorrect)
            if (correctSessionLastBoatStatus != null
                && correctSessionLastBoatStatus.getBoatId() != null
                && !boatStatusRecord.getBoatId().equals(correctSessionLastBoatStatus.getBoatId())) {
              correctSessionLastBoatStatus.setCurrentStatus(BoatStatusRecord.STATUS_AVAILABLE);
              correctSessionLastBoatStatus.setEntryNo(null);
              correctSessionLastBoatStatus.setLogbook(null);
              correctSessionLastBoatStatus.setComment("");
              boatStatus.data().update(correctSessionLastBoatStatus);
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }

    }

    if (mode == MODE_BOATHOUSE_ABORT) {
      try {
        logbook.data().delete(currentRecord.getKey());
      } catch (Exception e) {
        Dialog.error(e.toString());
      }
    }
  }

  void finishBoathouseAction(boolean success) {
    updateBoatStatus(success, getMode());
    efaBoathouseHideEfaFrame();
  }

  static String logEventInfoText(String logType, String logKey, String msg, LogbookRecord r) {
    String infoText = null;
    if (r != null) {
      long tstmp = r.getValidAtTimestamp();
      infoText = "#" + r.getEntryId().toString() + " - " + r.getBoatAsName(tstmp) + " ";
      infoText += International.getMessage("mit {crew}", r.getAllCoxAndCrewAsNameString(tstmp));
    }
    return msg + (infoText != null ? ": " + infoText : "");
  }

  void logAdminEvent(String logType, String logKey, String msg, LogbookRecord r) {
    Logger.log(logType, logKey,
        International.getString("Admin") + " " + (admin != null ? admin.getName() : "<none>")
            + ": " +
            logEventInfoText(logType, logKey, msg, r));
  }

  public static void logBoathouseEvent(String logType, String logKey, String msg, LogbookRecord r) {
    Logger.log(logType, logKey, logEventInfoText(logType, logKey, msg, r));
  }

  void efaBoathouseSetFixedLocation(int x, int y) {
    if (x >= 0 && y >= 0) {
      positionX = x;
      positionY = y;
    }
    setLocation(positionX, positionY);
  }

  public void efaBoathouseShowEfaFrame() {
    if (infoLabel.isVisible() != Daten.efaConfig.getValueEfaDirekt_showEingabeInfos()) {
      infoLabel.setVisible(Daten.efaConfig.getValueEfaDirekt_showEingabeInfos());
    }
    packFrame("efaBoathouseShowEfaFrame(Component)");
    efaBoathouseSetFixedLocation(-1, -1);
    showMe();
    toFront();
    if (focusItem != null) {
      focusItem.requestFocus();
    }
  }

  private void efaBoathouseHideEfaFrame() {
    if (getMode() != MODE_BOATHOUSE_ABORT) {
      setVisible(false);
      Dialog.frameClosed(this);
    }
    efaBoathouseFrame.showEfaBoathouseFrame(efaBoathouseAction, currentRecord);
  }

}
