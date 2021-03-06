/**
 * Title: efa - elektronisches Fahrtenbuch für Ruderer Copyright: Copyright (c)
 * 2001-2011 by Nicolas Michael Website: http://efa.nmichael.de/ License: GNU
 * General Public License v2
 *
 * @author Velten Heyn
 * @version 2
 */

package de.nmichael.efa.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Pattern;

import de.nmichael.efa.Daten;
import de.nmichael.efa.core.config.AdminRecord;
import de.nmichael.efa.core.items.IItemFactory;
import de.nmichael.efa.core.items.IItemType;
import de.nmichael.efa.core.items.ItemTypeDate;
import de.nmichael.efa.core.items.ItemTypeDouble;
import de.nmichael.efa.core.items.ItemTypeItemList;
import de.nmichael.efa.core.items.ItemTypeString;
import de.nmichael.efa.core.items.ItemTypeStringAutoComplete;
import de.nmichael.efa.data.storage.DataKey;
import de.nmichael.efa.data.storage.DataRecord;
import de.nmichael.efa.data.storage.IDataAccess;
import de.nmichael.efa.data.storage.MetaData;
import de.nmichael.efa.data.types.DataTypeDate;
import de.nmichael.efa.ex.EfaException;
import de.nmichael.efa.gui.util.TableItem;
import de.nmichael.efa.gui.util.TableItemHeader;
import de.nmichael.efa.util.International;
import de.nmichael.efa.util.Logger;

// @i18n complete
public class ClubworkRecord extends DataRecord implements IItemFactory {

  // =========================================================================
  // Field Names
  // =========================================================================
  public static final String ID = "Id";
  public static final String PERSONID = "PersonId";
  public static final String FIRSTNAME = "FirstName";
  public static final String LASTNAME = "LastName";
  public static final String FIRSTLASTNAME = "FirstLastName";
  public static final String NAMEAFFIX = "NameAffix";
  public static final String WORKDATE = "Date";
  public static final String DESCRIPTION = "Description";
  public static final String HOURS = "Hours";
  public static final String GUIITEM_PERSONIDLIST = "PersonList";
  public static final String FLAG = "Flag";

  public enum Flags {
    UNDEFINED, Normal, CarryOver, Credit
  }

  private static String CAT_BASEDATA = "%01%" + International.getString("Basisdaten");
  public static final String INPUTSHORTCUT = "InputShortcut";
  public static final String[] IDX_DATE_NAME_NAMEAFFIX = new String[] { FIRSTLASTNAME, NAMEAFFIX,
      WORKDATE };
  private static Pattern qnamePattern = Pattern.compile("(.+) \\(([^\\(\\)]+)\\)");

  public static void initialize() {
    Vector<String> f = new Vector<String>();
    Vector<Integer> t = new Vector<Integer>();

    f.add(ID);
    t.add(IDataAccess.DATA_UUID);
    f.add(PERSONID);
    t.add(IDataAccess.DATA_UUID);
    f.add(FIRSTNAME);
    t.add(IDataAccess.DATA_VIRTUAL);
    f.add(LASTNAME);
    t.add(IDataAccess.DATA_VIRTUAL);
    f.add(FIRSTLASTNAME);
    t.add(IDataAccess.DATA_VIRTUAL);
    f.add(NAMEAFFIX);
    t.add(IDataAccess.DATA_VIRTUAL);
    f.add(WORKDATE);
    t.add(IDataAccess.DATA_DATE);
    f.add(DESCRIPTION);
    t.add(IDataAccess.DATA_STRING);
    f.add(HOURS);
    t.add(IDataAccess.DATA_DOUBLE);
    f.add(FLAG);
    t.add(IDataAccess.DATA_INTEGER);

    MetaData metaData = constructMetaData(Clubwork.DATATYPE, f, t, false);
    metaData.setKey(new String[] { ID });
    metaData.addIndex(IDX_DATE_NAME_NAMEAFFIX);
  }

  public ClubworkRecord(Clubwork clubwork, MetaData metaData) {
    super(clubwork, metaData);
  }

  @Override
  public DataRecord createDataRecord() { // used for cloning
    return getPersistence().createNewRecord();
  }

  @Override
  public DataKey<UUID, ?, ?> getKey() {
    return new DataKey<UUID, String, String>(getId(), null, null);
  }

  public static DataKey<UUID, ?, ?> getKey(UUID id) {
    return new DataKey<UUID, String, String>(id, null, null);
  }

  public void setId(UUID id) {
    setUUID(ID, id);
  }

  public UUID getId() {
    return getUUID(ID);
  }

  public void setPersonId(UUID id) {
    setUUID(PERSONID, id);
  }

  public UUID getPersonId() {
    return getUUID(PERSONID);
  }

  public void setFirstName(String name) {
    // nothing to do (this column in virtual)
  }

  public String getFirstName() {
    PersonRecord pr = tryGetPerson(PERSONID, System.currentTimeMillis());
    return pr != null ? pr.getFirstName() : null;
  }

  public void setLastName(String name) {
    // nothing to do (this column in virtual)
  }

  public String getLastName() {
    PersonRecord pr = tryGetPerson(PERSONID, System.currentTimeMillis());
    return pr != null ? pr.getLastName() : null;
  }

  public void setFirstLastName(String name) {
    // nothing to do (this column in virtual)
  }

  public String getFirstLastName() {
    PersonRecord pr = tryGetPerson(PERSONID, System.currentTimeMillis());
    return pr != null ? pr.getFirstLastName() : null;
  }

  public void setWorkDate(DataTypeDate date) {
    setDate(WORKDATE, date);
  }

  public DataTypeDate getWorkDate() {
    return getDate(WORKDATE);
  }

  public void setDescription(String description) {
    setString(DESCRIPTION, description);
  }

  public String getDescription() {
    return getString(DESCRIPTION);
  }

  public void setHours(double hours) {
    setDouble(HOURS, hours);
  }

  public double getHours() {
    Double h = getDouble(HOURS);
    if (h == IDataAccess.UNDEFINED_DOUBLE) {
      return 0;
    }
    return h;
  }

  public void setFlag(Flags flag) {
    setInt(FLAG, flag.ordinal());
  }

  public Flags getFlag() {
    int flag = getInt(FLAG);
    Flags[] flags = Flags.values();
    if (flag >= 0 && flag < flags.length) {
      return Flags.values()[flag];
    } else {
      return Flags.UNDEFINED;
    }
  }

  public String getFlagAsText() {
    switch (getFlag()) {
      case Normal:
        return International.getString("normal");
      case CarryOver:
        return International.getString("Übertrag");
      case Credit:
        return International.getString("Kredit");
      default:
        return International.getString("unbekannt");
    }
  }

  public String getEfaId() {
    return Daten.EFA_SHORTNAME + getFirstLastName().substring(0, 1).toUpperCase();
  }

  public String getQualifiedName(boolean firstFirst) {
    return /* getFullName( */getFirstName()/* , getLastName(), getNameAffix(), firstFirst) */;
  }

  @Override
  public String getQualifiedName() {
    return getQualifiedName(Daten.efaConfig.getValueNameFormatIsFirstNameFirst());
  }

  @Override
  public String[] getQualifiedNameFields() {
    return IDX_DATE_NAME_NAMEAFFIX;
  }

  @Override
  public String[] getQualifiedNameFieldsTranslateVirtualToReal() {
    return new String[] { FIRSTNAME, LASTNAME, NAMEAFFIX };
  }

  @Override
  protected Object getVirtualColumn(int fieldIdx) {
    if (getFieldName(fieldIdx).equals(FIRSTLASTNAME)) {
      return getFirstLastName();
    }
    if (getFieldName(fieldIdx).equals(FIRSTNAME)) {
      return getFirstName();
    }
    if (getFieldName(fieldIdx).equals(LASTNAME)) {
      return getLastName();
    }
    if (getFieldName(fieldIdx).equals(NAMEAFFIX)) {
      return getLastName();
    }
    return null;
  }

  @Override
  public Object getUniqueIdForRecord() {
    return getId();
  }

  public PersonRecord tryGetPerson(String field, long validAt) {
    UUID id = getUUID(field);
    if (id != null) {
      Persons persons = getPersistence().getProject().getPersons(false);
      if (persons != null) {
        PersonRecord r = persons.getPerson(id, validAt);
        if (r != null) {
          return r;
        }
      }
    }
    return null;
  }

  public String getPersonAsName(String field, long validAt) {
    UUID id = getUUID(field);
    if (id != null) {
      Persons persons = getPersistence().getProject().getPersons(false);
      if (persons != null) {
        PersonRecord r = persons.getPerson(id, validAt);
        if (r != null) {
          return r.getQualifiedName();
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("[");
    for (int i = 0; i < getFieldCount(); i++) {
      Object v = get(i);
      if (v == null && !isKeyField(i)) {
        continue;
      }
      // if (getFieldType(i) == IDataAccess.DATA_VIRTUAL) {
      // continue;
      // }
      if (b.length() > 1) {
        b.append(";");
      }
      if (isKeyField(i)) { // Output for Key Field
        b.append("#" + getFieldName(i) + "#" + "="
            + (v != null ? v.toString() : "<UNSET>"));
      } else { // Output for normal Field
        b.append(getFieldName(i) + "=" + v.toString());
      }
    }
    b.append("]");
    return b.toString();
  }

  @Override
  public String getAsText(String fieldName) {
    if (fieldName.equals(PERSONID)) {
      return getPersonAsName(PERSONID, System.currentTimeMillis());
    }
    return super.getAsText(fieldName);
  }

  @Override
  public boolean setFromText(String fieldName, String value) {
    if (fieldName.equals(PERSONID)) {
      Persons persons = getPersistence().getProject().getPersons(false);
      PersonRecord pr = persons.getPerson(value, -1);
      if (pr != null) {
        set(fieldName, pr.getId());
      }
    } else {
      return super.setFromText(fieldName, value);
    }
    return (value.equals(getAsText(fieldName)));
  }

  @Override
  public IItemType[] getDefaultItems(String itemName) {
    // simply create an empty personid field
    Persons persons = getPersistence().getProject().getPersons(false);
    IItemType[] items = new IItemType[1];
    items[0] = getGuiItemTypeStringAutoComplete(PERSONID, getPersonId(),
        IItemType.TYPE_PUBLIC, CAT_BASEDATA,
        persons, getValidFrom(), getInvalidFrom() - 1,
        International.getString("Person"));
    items[0].setFieldSize(300, -1);
    return items;
  }

  @Override
  public Vector<IItemType> getGuiItems(AdminRecord admin) {
    Persons persons = getPersistence().getProject().getPersons(false);

    IItemType item;
    Vector<IItemType> v = new Vector<IItemType>();

    v.add(item = getGuiItemTypeStringAutoComplete(ClubworkRecord.PERSONID, getPersonId(),
        IItemType.TYPE_PUBLIC, CAT_BASEDATA,
        persons, System.currentTimeMillis(), System.currentTimeMillis(),
        International.getString("Person")));
    item.setFieldSize(300, 19);

    if (getPersonId() == null) {
      Vector<IItemType[]> itemList = new Vector<IItemType[]>();
      v.add(item = new ItemTypeItemList(GUIITEM_PERSONIDLIST, itemList, this,
          IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("weitere Personen")));
      ((ItemTypeItemList) item).setAppendPositionToEachElement(true);
      ((ItemTypeItemList) item).setRepeatTitle(false);
      ((ItemTypeItemList) item).setXForAddDelButtons(3);
      ((ItemTypeItemList) item).setPadYbetween(0);
    }

    v.add(item = new ItemTypeDate(WORKDATE, getWorkDate(),
        IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Datum")));

    v.add(item = new ItemTypeString(DESCRIPTION, getDescription(),
        IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Beschreibung")));

    v.add(item = new ItemTypeDouble(HOURS, getHours(), ItemTypeDouble.MIN, ItemTypeDouble.MAX,
        IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Stunden")));

    return v;
  }

  @Override
  public void saveGuiItems(Vector<IItemType> items) {
    super.saveGuiItems(items);

    // saveGuiItems(items) has to be before this.get... otherwise null returned
    for (IItemType item : items) {
      String name = item.getName();
      if (name.equals(GUIITEM_PERSONIDLIST) && item.isChanged()) {
        ItemTypeItemList list = (ItemTypeItemList) item;
        for (int i = 0; i < list.size(); i++) {
          IItemType[] typeItems = list.getItems(i);
          UUID uuid = (UUID) ((ItemTypeStringAutoComplete) typeItems[0]).getId(typeItems[0]
              .toString());
          if (uuid != null && uuid.toString().length() > 0) {
            Clubwork clubwork = Daten.project.getCurrentClubwork();

            ClubworkRecord record = clubwork.createClubworkRecord(UUID.randomUUID());
            record.setPersonId(uuid);
            record.setWorkDate(getWorkDate());
            record.setDescription(getDescription());
            record.setHours(getHours());
            record.setFlag(Flags.Normal);
            try {
              clubwork.data().add(record);
            } catch (Exception eignore) {
              Logger.logdebug(eignore);
            }
          }
        }
      }
    }
  }

  @Override
  public TableItemHeader[] getGuiTableHeader() {
    TableItemHeader[] header = new TableItemHeader[6];
    if (Daten.efaConfig.getValueNameFormatIsFirstNameFirst()) {
      header[0] = new TableItemHeader(International.getString("Vorname"));
      header[1] = new TableItemHeader(International.getString("Nachname"));
    } else {
      header[0] = new TableItemHeader(International.getString("Nachname"));
      header[1] = new TableItemHeader(International.getString("Vorname"));
    }
    header[2] = new TableItemHeader(International.getString("Datum"));
    header[3] = new TableItemHeader(International.getString("Beschreibung"));
    header[4] = new TableItemHeader(International.getString("Stunden"));
    header[5] = new TableItemHeader(International.getString("Typ"));
    return header;
  }

  @Override
  public TableItem[] getGuiTableItems() {
    TableItem[] items = new TableItem[6];
    if (Daten.efaConfig.getValueNameFormatIsFirstNameFirst()) {
      if (Daten.isAdminMode()) {
        items[0] = new TableItem(getFirstName());
        items[1] = new TableItem(getLastName());
      } else {
        items[0] = new TableItem(getFirstName().substring(0, 1) + ".");
        items[1] = new TableItem(getLastName().substring(0, 1) + ".");
      }
    } else {
      items[0] = new TableItem(getLastName());
      items[1] = new TableItem(getFirstName());
    }
    items[2] = new TableItem(getWorkDate());
    items[3] = new TableItem(getDescription());
    items[4] = new TableItem(getHours());
    items[5] = new TableItem(getFlagAsText());
    return items;
  }

  /**
   * @param aggregations instantiated with empty strings, size == header.size
   * @param index
   * @param size
   * @return
   */
  @Override
  public String[] getGuiTableAggregations(String[] aggregations, int index, int size,
      HashMap<String, Object> sideInfo) {
    if (index == 0) {
      sideInfo.put("uniquePeople", new HashSet<UUID>() {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
          add(getPersonId());
        }
      });
      sideInfo.put("earliestDate", getWorkDate());
      sideInfo.put("latestDate", getWorkDate());
      aggregations[3] = String.valueOf(getHours());
    } else {
      DataTypeDate earliestDate = (DataTypeDate) sideInfo.get("earliestDate");
      if (getWorkDate().isBefore(earliestDate)) {
        sideInfo.put("earliestDate", getWorkDate());
      }

      DataTypeDate latestDate = (DataTypeDate) sideInfo.get("latestDate");
      if (getWorkDate().isAfter(latestDate)) {
        sideInfo.put("latestDate", getWorkDate());
      }

      aggregations[3] = String.valueOf(Double.valueOf(aggregations[3]) + getHours());
    }

    HashSet<UUID> uniquePeople = (HashSet<UUID>) sideInfo.get("uniquePeople");
    uniquePeople.add(getPersonId());

    if (index == size - 1) {
      int uniqueSize = uniquePeople.size();
      aggregations[0] = uniqueSize + International.getString("Person(s)");
      aggregations[1] = (sideInfo.get("earliestDate")).toString() + "-"
          + (sideInfo.get("latestDate")).toString();
      Clubwork clubwork = Daten.project.getCurrentClubwork();
      if (clubwork != null) {
        ProjectRecord clubworkBook = Daten.project.getClubworkBookRecord(clubwork.getName());
        Persons personContainer = Daten.project.getPersons(false);
        int groupMonth = 0;
        for (UUID id : uniquePeople) {
          try {
            DataRecord[] personRecords = personContainer.data().getValidAny(
                new DataKey<UUID, Long, String>(id, null, null));
            for (DataRecord personRecord : personRecords) {
              // vh if (((PersonRecord) personRecord).isStatusMember()) {
              groupMonth += ((PersonRecord) personRecord).getPersonMemberMonth(
                  clubwork.getStartDate(), clubwork.getEndDate());
              // }
            }
          } catch (EfaException e) {
            e.printStackTrace();
          }
        }

        aggregations[3] += "/"
            + Math.round(clubworkBook.getDefaultMonthlyClubworkTargetHours() * groupMonth * 100)
                / 100d;
      }
    }

    return aggregations;
  }
}
