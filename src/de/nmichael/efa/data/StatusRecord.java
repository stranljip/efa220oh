/**
 * Title:        efa - elektronisches Fahrtenbuch für Ruderer
 * Copyright:    Copyright (c) 2001-2011 by Nicolas Michael
 * Website:      http://efa.nmichael.de/
 * License:      GNU General Public License v2
 *
 * @author Nicolas Michael
 * @version 2
 */

package de.nmichael.efa.data;

import java.util.UUID;
import java.util.Vector;

import de.nmichael.efa.core.config.AdminRecord;
import de.nmichael.efa.core.items.IItemType;
import de.nmichael.efa.core.items.ItemTypeBoolean;
import de.nmichael.efa.core.items.ItemTypeLabel;
import de.nmichael.efa.core.items.ItemTypeString;
import de.nmichael.efa.data.storage.DataKey;
import de.nmichael.efa.data.storage.DataRecord;
import de.nmichael.efa.data.storage.IDataAccess;
import de.nmichael.efa.data.storage.MetaData;
import de.nmichael.efa.gui.util.TableItem;
import de.nmichael.efa.gui.util.TableItemHeader;
import de.nmichael.efa.util.International;

// @i18n complete

public class StatusRecord extends DataRecord {

  // =========================================================================
  // Field Names
  // =========================================================================

  public static final String TYPE_GUEST = "GUEST";
  public static final String TYPE_OTHER = "OTHER";
  public static final String TYPE_USER = "USER";

  public static final String ID = "Id";
  public static final String NAME = "Name";
  public static final String TYPE = "Type";
  public static final String MEMBERSHIP = "Membership";

  public static final String[] IDX_NAME = new String[] { NAME };

  public static final int MEMBERSHIP_NOMEMBER = 0;
  public static final int MEMBERSHIP_MEMBER = 1;
  private static final String GUIITEM_MEMBERSHIP = "GUIITEM_MEMBERSHIP";

  public static void initialize() {
    Vector<String> f = new Vector<String>();
    Vector<Integer> t = new Vector<Integer>();

    f.add(ID);
    t.add(IDataAccess.DATA_UUID);
    f.add(NAME);
    t.add(IDataAccess.DATA_STRING);
    f.add(TYPE);
    t.add(IDataAccess.DATA_STRING);
    f.add(MEMBERSHIP);
    t.add(IDataAccess.DATA_INTEGER);
    MetaData metaData = constructMetaData(Status.DATATYPE, f, t, false);
    metaData.setKey(new String[] { ID });
    metaData.addIndex(IDX_NAME);
  }

  public StatusRecord(Status status, MetaData metaData) {
    super(status, metaData);
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

  @Override
  public int compareTo(Object o) {
    if (o == null) {
      return -1;
    }
    StatusRecord or = (StatusRecord) o;
    if (getType().equals(or.getType())) {
      return getStatusName().compareTo(or.getStatusName());
    } else {
      if (getType().equals(TYPE_OTHER)) { // OTHER type is always last
        return 1;
      }
      if (getType().equals(TYPE_GUEST)) { // GUEST type is always between USER and OTHER types
        if (or.getType().equals(TYPE_OTHER)) {
          return -1;
        }
        if (or.getType().equals(TYPE_USER)) {
          return 1;
        }
      }
      if (getType().equals(TYPE_USER)) { // USER types are always first
        return -1;
      }
    }
    return 0;
  }

  public void setId(UUID id) {
    setUUID(ID, id);
  }

  public UUID getId() {
    return getUUID(ID);
  }

  public void setStatusName(String name) {
    setString(NAME, name);
  }

  public String getStatusName() {
    return getString(NAME);
  }

  public void setType(String type) {
    if (type.equals(TYPE_GUEST) ||
        type.equals(TYPE_OTHER) ||
        type.equals(TYPE_USER)) {
      setString(TYPE, type);
    }
  }

  public String getType() {
    String type = getString(TYPE);
    if (type == null ||
        (!type.equals(TYPE_GUEST) && !type.equals(TYPE_OTHER))) {
      return TYPE_USER;
    }
    return type;
  }

  public String getTypeDescription() {
    String type = getType();
    if (type.equals(TYPE_GUEST)) {
      return International.getString("Gast");
    }
    if (type.equals(TYPE_OTHER)) {
      return International.getString("andere");
    }
    return International.getString("benutzerdefiniert");
  }

  public boolean isTypeUser() {
    return TYPE_USER.equals(getType());
  }

  public boolean isTypeGuest() {
    return TYPE_GUEST.equals(getType());
  }

  public boolean isTypeOther() {
    return TYPE_OTHER.equals(getType());
  }

  public void setMembership(int membership) {
    setInt(MEMBERSHIP, membership);
  }

  public int getMembership() {
    return getInt(MEMBERSHIP);
  }

  public boolean isMember() {
    int m = getMembership();
    // m < 0 (and especially m == UNDEFINED_INT) are considered unset, and default to "MEMBER"
    return m < 0 || m == MEMBERSHIP_MEMBER;
  }

  @Override
  public String[] getQualifiedNameFields() {
    return IDX_NAME;
  }

  @Override
  public Object getUniqueIdForRecord() {
    return getId();
  }

  @Override
  public String getQualifiedName() {
    return getStatusName();
  }

  @Override
  public String getAsText(String fieldName) {
    if (fieldName.equals(MEMBERSHIP)) {
      return (isMember() ?
          International.getString("ja") :
            International.getString("nein"));
    }
    return super.getAsText(fieldName);
  }

  @Override
  public boolean setFromText(String fieldName, String value) {
    if (fieldName.equals(MEMBERSHIP)) {
      setMembership((value.equals(International.getString("ja")) ?
          MEMBERSHIP_MEMBER : MEMBERSHIP_NOMEMBER));
    } else {
      return super.setFromText(fieldName, value);
    }
    return (value.equals(getAsText(fieldName)));
  }

  @Override
  public Vector<IItemType> getGuiItems(AdminRecord admin) {
    String CAT_BASEDATA = "%01%" + International.getString("Status");
    Vector<IItemType> v = new Vector<IItemType>();
    v.add(new ItemTypeLabel("LABEL",
        IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Typ") + ": "
            + getTypeDescription()));
    v.add(new ItemTypeString(StatusRecord.NAME, getStatusName(),
        IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Status")));
    if (isTypeUser()) {
      v.add(new ItemTypeBoolean(StatusRecord.GUIITEM_MEMBERSHIP, isMember(),
          IItemType.TYPE_PUBLIC, CAT_BASEDATA, International.getString("Mitglied")));
    }
    return v;
  }

  @Override
  public TableItemHeader[] getGuiTableHeader() {
    TableItemHeader[] header = new TableItemHeader[3];
    header[0] = new TableItemHeader(International.getString("Status"));
    header[1] = new TableItemHeader(International.getString("Typ"));
    header[2] = new TableItemHeader(International.getString("Mitglied"));
    return header;
  }

  @Override
  public TableItem[] getGuiTableItems() {
    TableItem[] items = new TableItem[3];
    items[0] = new TableItem(getStatusName());
    items[1] = new TableItem(getTypeDescription());
    items[2] = new TableItem(isMember() ?
        International.getString("ja") :
          International.getString("nein"));
    return items;
  }

  @Override
  public void saveGuiItems(Vector<IItemType> items) {
    for (IItemType item : items) {
      if (item.getName().equals(GUIITEM_MEMBERSHIP)) {
        setMembership(((ItemTypeBoolean) item).getValue() ? MEMBERSHIP_MEMBER : MEMBERSHIP_NOMEMBER);
      }
    }
    super.saveGuiItems(items);
  }

}
