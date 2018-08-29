package com.yongle.letuiweipad.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.yongle.letuiweipad.domain.createorder.GoodBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "GOOD_BEAN".
*/
public class GoodBeanDao extends AbstractDao<GoodBean, Long> {

    public static final String TABLENAME = "GOOD_BEAN";

    /**
     * Properties of entity GoodBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property GoodsId = new Property(1, String.class, "goodsId", false, "GOODS_ID");
        public final static Property GoodsName = new Property(2, String.class, "goodsName", false, "GOODS_NAME");
        public final static Property GoodsPrice = new Property(3, double.class, "goodsPrice", false, "GOODS_PRICE");
        public final static Property GoodsNum = new Property(4, double.class, "goodsNum", false, "GOODS_NUM");
        public final static Property Stock = new Property(5, double.class, "stock", false, "STOCK");
        public final static Property HasStock = new Property(6, int.class, "hasStock", false, "HAS_STOCK");
        public final static Property StockWarning = new Property(7, int.class, "stockWarning", false, "STOCK_WARNING");
        public final static Property GoodsUnit = new Property(8, String.class, "goodsUnit", false, "GOODS_UNIT");
        public final static Property Kinds = new Property(9, String.class, "kinds", false, "KINDS");
        public final static Property IsChecked = new Property(10, boolean.class, "isChecked", false, "IS_CHECKED");
        public final static Property SubNum = new Property(11, double.class, "subNum", false, "SUB_NUM");
        public final static Property Size_id = new Property(12, String.class, "size_id", false, "size_id");
        public final static Property SizeId = new Property(13, String.class, "sizeId", false, "sizeId");
        public final static Property GroupId = new Property(14, String.class, "groupId", false, "GROUP_ID");
        public final static Property GroupName = new Property(15, String.class, "groupName", false, "GROUP_NAME");
        public final static Property GoodsCode = new Property(16, String.class, "goodsCode", false, "GOODS_CODE");
        public final static Property Is_weigh = new Property(17, String.class, "is_weigh", false, "IS_WEIGH");
        public final static Property VipPrice = new Property(18, double.class, "vipPrice", false, "VIP_PRICE");
        public final static Property TypeId = new Property(19, int.class, "typeId", false, "TYPE_ID");
    }


    public GoodBeanDao(DaoConfig config) {
        super(config);
    }
    
    public GoodBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"GOOD_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"GOODS_ID\" TEXT," + // 1: goodsId
                "\"GOODS_NAME\" TEXT," + // 2: goodsName
                "\"GOODS_PRICE\" REAL NOT NULL ," + // 3: goodsPrice
                "\"GOODS_NUM\" REAL NOT NULL ," + // 4: goodsNum
                "\"STOCK\" REAL NOT NULL ," + // 5: stock
                "\"HAS_STOCK\" INTEGER NOT NULL ," + // 6: hasStock
                "\"STOCK_WARNING\" INTEGER NOT NULL ," + // 7: stockWarning
                "\"GOODS_UNIT\" TEXT," + // 8: goodsUnit
                "\"KINDS\" TEXT," + // 9: kinds
                "\"IS_CHECKED\" INTEGER NOT NULL ," + // 10: isChecked
                "\"SUB_NUM\" REAL NOT NULL ," + // 11: subNum
                "\"size_id\" TEXT," + // 12: size_id
                "\"sizeId\" TEXT," + // 13: sizeId
                "\"GROUP_ID\" TEXT," + // 14: groupId
                "\"GROUP_NAME\" TEXT," + // 15: groupName
                "\"GOODS_CODE\" TEXT," + // 16: goodsCode
                "\"IS_WEIGH\" TEXT," + // 17: is_weigh
                "\"VIP_PRICE\" REAL NOT NULL ," + // 18: vipPrice
                "\"TYPE_ID\" INTEGER NOT NULL );"); // 19: typeId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"GOOD_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, GoodBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String goodsId = entity.getGoodsId();
        if (goodsId != null) {
            stmt.bindString(2, goodsId);
        }
 
        String goodsName = entity.getGoodsName();
        if (goodsName != null) {
            stmt.bindString(3, goodsName);
        }
        stmt.bindDouble(4, entity.getGoodsPrice());
        stmt.bindDouble(5, entity.getGoodsNum());
        stmt.bindDouble(6, entity.getStock());
        stmt.bindLong(7, entity.getHasStock());
        stmt.bindLong(8, entity.getStockWarning());
 
        String goodsUnit = entity.getGoodsUnit();
        if (goodsUnit != null) {
            stmt.bindString(9, goodsUnit);
        }
 
        String kinds = entity.getKinds();
        if (kinds != null) {
            stmt.bindString(10, kinds);
        }
        stmt.bindLong(11, entity.getIsChecked() ? 1L: 0L);
        stmt.bindDouble(12, entity.getSubNum());
 
        String size_id = entity.getSize_id();
        if (size_id != null) {
            stmt.bindString(13, size_id);
        }
 
        String sizeId = entity.getSizeId();
        if (sizeId != null) {
            stmt.bindString(14, sizeId);
        }
 
        String groupId = entity.getGroupId();
        if (groupId != null) {
            stmt.bindString(15, groupId);
        }
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(16, groupName);
        }
 
        String goodsCode = entity.getGoodsCode();
        if (goodsCode != null) {
            stmt.bindString(17, goodsCode);
        }
 
        String is_weigh = entity.getIs_weigh();
        if (is_weigh != null) {
            stmt.bindString(18, is_weigh);
        }
        stmt.bindDouble(19, entity.getVipPrice());
        stmt.bindLong(20, entity.getTypeId());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, GoodBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String goodsId = entity.getGoodsId();
        if (goodsId != null) {
            stmt.bindString(2, goodsId);
        }
 
        String goodsName = entity.getGoodsName();
        if (goodsName != null) {
            stmt.bindString(3, goodsName);
        }
        stmt.bindDouble(4, entity.getGoodsPrice());
        stmt.bindDouble(5, entity.getGoodsNum());
        stmt.bindDouble(6, entity.getStock());
        stmt.bindLong(7, entity.getHasStock());
        stmt.bindLong(8, entity.getStockWarning());
 
        String goodsUnit = entity.getGoodsUnit();
        if (goodsUnit != null) {
            stmt.bindString(9, goodsUnit);
        }
 
        String kinds = entity.getKinds();
        if (kinds != null) {
            stmt.bindString(10, kinds);
        }
        stmt.bindLong(11, entity.getIsChecked() ? 1L: 0L);
        stmt.bindDouble(12, entity.getSubNum());
 
        String size_id = entity.getSize_id();
        if (size_id != null) {
            stmt.bindString(13, size_id);
        }
 
        String sizeId = entity.getSizeId();
        if (sizeId != null) {
            stmt.bindString(14, sizeId);
        }
 
        String groupId = entity.getGroupId();
        if (groupId != null) {
            stmt.bindString(15, groupId);
        }
 
        String groupName = entity.getGroupName();
        if (groupName != null) {
            stmt.bindString(16, groupName);
        }
 
        String goodsCode = entity.getGoodsCode();
        if (goodsCode != null) {
            stmt.bindString(17, goodsCode);
        }
 
        String is_weigh = entity.getIs_weigh();
        if (is_weigh != null) {
            stmt.bindString(18, is_weigh);
        }
        stmt.bindDouble(19, entity.getVipPrice());
        stmt.bindLong(20, entity.getTypeId());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public GoodBean readEntity(Cursor cursor, int offset) {
        GoodBean entity = new GoodBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // goodsId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // goodsName
            cursor.getDouble(offset + 3), // goodsPrice
            cursor.getDouble(offset + 4), // goodsNum
            cursor.getDouble(offset + 5), // stock
            cursor.getInt(offset + 6), // hasStock
            cursor.getInt(offset + 7), // stockWarning
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // goodsUnit
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // kinds
            cursor.getShort(offset + 10) != 0, // isChecked
            cursor.getDouble(offset + 11), // subNum
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // size_id
            cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13), // sizeId
            cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // groupId
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // groupName
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // goodsCode
            cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17), // is_weigh
            cursor.getDouble(offset + 18), // vipPrice
            cursor.getInt(offset + 19) // typeId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, GoodBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setGoodsId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setGoodsName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setGoodsPrice(cursor.getDouble(offset + 3));
        entity.setGoodsNum(cursor.getDouble(offset + 4));
        entity.setStock(cursor.getDouble(offset + 5));
        entity.setHasStock(cursor.getInt(offset + 6));
        entity.setStockWarning(cursor.getInt(offset + 7));
        entity.setGoodsUnit(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setKinds(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setIsChecked(cursor.getShort(offset + 10) != 0);
        entity.setSubNum(cursor.getDouble(offset + 11));
        entity.setSize_id(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setSizeId(cursor.isNull(offset + 13) ? null : cursor.getString(offset + 13));
        entity.setGroupId(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setGroupName(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setGoodsCode(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setIs_weigh(cursor.isNull(offset + 17) ? null : cursor.getString(offset + 17));
        entity.setVipPrice(cursor.getDouble(offset + 18));
        entity.setTypeId(cursor.getInt(offset + 19));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(GoodBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(GoodBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(GoodBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}