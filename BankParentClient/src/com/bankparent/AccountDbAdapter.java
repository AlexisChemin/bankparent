/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.bankparent;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bankparent.model.Account;
import com.bankparent.model.AccountExtended;
import com.bankparent.model.Operation;

public class AccountDbAdapter {

    public static final String ACCOUNT_ROWID = "_id";
    public static final String ACCOUNT_ID = "id";
    public static final String ACCOUNT_NAME = "name";
    public static final String ACCOUNT_IMAGE= "image";
    public static final String ACCOUNT_BALANCE = "balance";
    public static final String ACCOUNT_VERSION= "version";
    

    public static final String OPERATION_ROWID = "_id";
    public static final String OPERATION_UID = "uid";
    public static final String OPERATION_ACCOUNT = "account_rowid";
    public static final String OPERATION_LABEL = "label";
    public static final String OPERATION_CREATION_TIMESTAMP = "creationTimestamp";
    public static final String OPERATION_AMOUNT = "amount";
    public static final String OPERATION_VERSION= "version";
    public static final String OPERATION_NATURE= "nature";

    private static final String TAG = "AccountDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    
    private static final String DATABASE_CREATE_ACCOUNT_TABLE =
            "create table account (_id integer primary key autoincrement, "
    		+ "id text not null, "
            + "version INTEGER DEFAULT 1,"
            + "name text not null, balance real not null, image text null);";
    
    
    private static final String DATABASE_CREATE_OPERATION_TABLE =
            "create table operation (_id integer primary key autoincrement, "
            + "account_rowid not null,"
            + "amount real,"    
            + "creationTimestamp integer not null,"        
            + "label text null,"      
            + "nature text null,"
            + "version INTEGER DEFAULT 0,"
            + "uid text not null, "
            + "FOREIGN KEY(account_rowid) REFERENCES account(_id) );";

//    private static final String DATABASE_CREATE_ACCOUNT_TABLE_v7 =
//        "create table account (_id integer primary key autoincrement, "
//        + "name text not null, balance real not null, image text null);";    
//
//    private static final String DATABASE_CREATE_OPERATION_TABLE_v7 =
//            "create table operation (_id integer primary key autoincrement, "
//            + "account_id not null,"
//            + "amount real not null,"
//            + "date text not null,"            
//            + "note text null,"
//            + "FOREIGN KEY(account_id) REFERENCES account(_id) );";

    private static final String DATABASE_NAME = "data";
    private static final String ACCOUNT_TABLE = "account";
    private static final String OPERATION_TABLE = "operation";
    private static final int DATABASE_VERSION = 7;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_ACCOUNT_TABLE);
            db.execSQL(DATABASE_CREATE_OPERATION_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion );
            if (oldVersion==7) {
//            	updateToEight(db);
//            	oldVersion =8;
            }
        }
//        
//        protected void updateToEight(SQLiteDatabase db) {
//            Log.w(TAG, "modifying database to version 8.");
//        	db.execSQL("ALTER TABLE " + OPERATION_TABLE + " ADD COLUMN uid String;");
//        	db.execSQL("ALTER TABLE " + OPERATION_TABLE + " ADD COLUMN version INTEGER DEFAULT 0;");
//        	db.execSQL("ALTER TABLE " + ACCOUNT_TABLE + " ADD COLUMN version INTEGER DEFAULT 1;");
//        	
//        	// add a uid for each operation
//    		db.beginTransaction();
//        	Cursor mCursor =
//                    db.query(true, OPERATION_TABLE, new String[] {
//                    		OPERATION_ROWID}, null, null,null, null, null, null);
//        	if (mCursor != null) {
//        		mCursor.moveToFirst();
//
//            	int rowidColumnIndex = mCursor.getColumnIndex(OPERATION_ROWID);
//        		do {
//        			Long rowId = mCursor.getLong(rowidColumnIndex);
//            		ContentValues args = new ContentValues();
//           			String newUID = newUID();
//					args.put(OPERATION_UID, newUID);
//
//		            Log.w(TAG, "op-" + rowId +" -- uid:"+newUID); 
//           			db.update(OPERATION_TABLE, args, OPERATION_ROWID + "=" + rowId, null);
//            	}while(mCursor.moveToNext());
//        		mCursor.close();
//        	}
//        	db.endTransaction();
//        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public AccountDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AccountDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        Cursor cursor = fetchAllAccounts();
        if (cursor.getCount() == 0) {
        	//populateAccounts();
        } 
        return this;
    }

    private void populateAccounts() {
    	Account test = new Account();
    	test.setName("Aglaé");
    	test.setId("aglae");
    	test.setVersion(1);
    	test.setSum(0.0);
    	createAccount(test);
//		long acc1 = createAccount("Aglaé", 0.0f, "head1");
//		long acc2 = createAccount("Malorie", 0.00f, "head2");
//		long acc3 = createAccount("Violette", 0.0f, "head3");
//		
//		createOperation(acc1, "2011-09-11", 10.00f, "cadeau");
//		createOperation(acc1, "2011-01-01", 10.00f, "recompense");
//		createOperation(acc2, "2001-02-01", 10.00f, "recompense");
//		createOperation(acc2, "2002-02-01", -10.00f, "depense inutile");
	}

	public void close() {
        mDbHelper.close();
        mDb.close();
    }

	/*
	 * ACCOUNT
	 */
	

	
    public long createAccount(Account account) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ACCOUNT_ID, account.getId());
        initialValues.put(ACCOUNT_NAME, account.getName());
        initialValues.put(ACCOUNT_BALANCE, account.getSum());
        initialValues.put(ACCOUNT_VERSION, account.getVersion());
        initialValues.put(ACCOUNT_IMAGE, account.getId());

        return mDb.insert(ACCOUNT_TABLE, null, initialValues);
    }
	
//    public long createAccount(String name, Float balance, String image) {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(ACCOUNT_NAME, name);
//        initialValues.put(ACCOUNT_BALANCE, balance);
//        initialValues.put(ACCOUNT_IMAGE, image);
//
//        return mDb.insert(ACCOUNT_TABLE, null, initialValues);
//    }
    
    
    public boolean deleteAccounts() {
        return mDb.delete(ACCOUNT_TABLE, null, null) > 0;
    }
    
    public boolean deleteAccount(long accountRowid) {
        return mDb.delete(ACCOUNT_TABLE, ACCOUNT_ROWID + "=" + accountRowid, null) > 0;
    }

    public Cursor fetchAllAccounts() {

        return mDb.query(ACCOUNT_TABLE, new String[] {ACCOUNT_ROWID,
        		ACCOUNT_ID,
        		ACCOUNT_NAME, ACCOUNT_BALANCE, ACCOUNT_IMAGE, ACCOUNT_VERSION}, null, null, null, null, null);
    }

    
    
    public AccountExtended fetchAccount(long accountRowid) throws SQLException {

        Cursor cursor =

            mDb.query(true, ACCOUNT_TABLE, new String[] {ACCOUNT_ROWID,
            		ACCOUNT_ID,
            		ACCOUNT_NAME, ACCOUNT_BALANCE, ACCOUNT_IMAGE, ACCOUNT_VERSION}, ACCOUNT_ROWID + "=" + accountRowid, null,
                    null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        
        AccountExtended result = toAccount(cursor);

        cursor.close();
        
        
        return result;
    }
    
    public AccountExtended fetchAccount(String accountId) throws SQLException {
    	AccountExtended result = null;
    	try { 
	        Cursor cursor =
	
	            mDb.query(true, ACCOUNT_TABLE, new String[] {ACCOUNT_ROWID,
	            		ACCOUNT_ID,
	            		ACCOUNT_NAME, ACCOUNT_BALANCE, ACCOUNT_IMAGE, ACCOUNT_VERSION}, ACCOUNT_ID + "='" + accountId+"'", null,
	                    null, null, null, null);
	        if (cursor != null) {
	            cursor.moveToFirst();
		        if (!cursor.isAfterLast()) {
		        	result = toAccount(cursor);
		        }
		        cursor.close();
	        }
    	} catch(SQLiteException e) {
    		return null;
    	}
        
        
        return result;
    }

    
    
	public AccountExtended toAccount(Cursor cursor) {
		String id = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_NAME));
        Float balance = cursor.getFloat(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_BALANCE));
        String image = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_IMAGE));
        Long version = cursor.getLong(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_VERSION));
        Long rowid = cursor.getLong(cursor.getColumnIndexOrThrow(AccountDbAdapter.ACCOUNT_ROWID));

        AccountExtended result = new AccountExtended();
        result.setRowid(rowid);
        result.setId(id);
        result.setName(name);
        result.setSum(balance==null?null:balance.doubleValue());
        result.setVersion(version==null?null:version.intValue());
        result.setImage(image);
		return result;
	}
    
    

    public boolean updateAccount(long accountRowid, Account account) {
        ContentValues args = new ContentValues();
        String name = account.getName();
        Integer version = account.getVersion();
        Double balance = account.getSum();
        args.put(ACCOUNT_ID, account.getId());
		if (name !=null) {
        	args.put(ACCOUNT_NAME, name);
        }
        if (version!=null) {
        	args.put(ACCOUNT_VERSION, version);
        }
        if (balance!=null) {
            args.put(ACCOUNT_BALANCE, balance);
        }
        if (account instanceof AccountExtended) {
        	String image = ((AccountExtended)account).getImage();
            args.put(ACCOUNT_IMAGE, image);
        }

        return mDb.update(ACCOUNT_TABLE, args, ACCOUNT_ROWID + "=" + accountRowid, null) > 0;
    }

    
    
    
	/*
	 * OPERATION
	 */

	
    public long createOperation(long accountRowid, Operation operation) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(OPERATION_ACCOUNT, accountRowid);
        initialValues.put(OPERATION_UID, operation.getId());
        initialValues.put(OPERATION_CREATION_TIMESTAMP, operation.getCreationTimestamp());
        initialValues.put(OPERATION_AMOUNT, operation.getAmount());
        initialValues.put(OPERATION_LABEL, operation.getLabel());
        initialValues.put(OPERATION_NATURE, operation.getNature());
        initialValues.put(OPERATION_VERSION, operation.getVersion());

        return mDb.insert(OPERATION_TABLE, null, initialValues);
    }
    
    public boolean updateOperation(long accountRowid, Operation operation) {
        ContentValues args = new ContentValues();
        String uid = operation.getId();
        Integer version = operation.getVersion();
        Double amount = operation.getAmount();
        Long creationTimestamp = operation.getCreationTimestamp();
        String label = operation.getLabel();
        String nature = operation.getNature();
        args.put(OPERATION_UID, uid);
		if (version !=null) {
        	args.put(OPERATION_VERSION, version);
        }
        if (creationTimestamp!=null) {
        	args.put(OPERATION_CREATION_TIMESTAMP, creationTimestamp);
        }
        if (amount!=null) {
            args.put(OPERATION_AMOUNT, amount);
        }
        if (label!=null) {
            args.put(OPERATION_LABEL, label);
        }
        if (nature!=null) {
            args.put(OPERATION_NATURE, nature);
        }
        
        return mDb.update(OPERATION_TABLE, args, 
        		OPERATION_ACCOUNT + "=" + accountRowid+ " AND " + OPERATION_UID + "='" + uid+"'"
        				, null) > 0;
    }
    
    
	public Operation toOperation(Cursor cursor) {
		String uid = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_UID));
        Long creationTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_CREATION_TIMESTAMP));
        String label = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_LABEL));
        Float amount = cursor.getFloat(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_AMOUNT));
        String nature = cursor.getString(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_NATURE));
        Long version = cursor.getLong(cursor.getColumnIndexOrThrow(AccountDbAdapter.OPERATION_VERSION));

        Operation result = new Operation();
        result.setId(uid);
        result.setCreationTimestamp(creationTimestamp);
        result.setLabel(label);
        result.setAmount(amount==null?null:amount.doubleValue());
        result.setNature(nature);
        result.setVersion(version==null?null:version.intValue());
		return result;
	}
    
    public Cursor fetchAccountOperations(long accountRowid) throws SQLException {

        Cursor mCursor =

            mDb.query(true, OPERATION_TABLE, new String[] {
            		OPERATION_ROWID,
            		OPERATION_UID,
            		OPERATION_CREATION_TIMESTAMP,
            		OPERATION_AMOUNT,
            		OPERATION_LABEL,
            		OPERATION_NATURE,
            		OPERATION_VERSION
            		}, OPERATION_ACCOUNT + "=" + accountRowid, null,
                    null, null, OPERATION_CREATION_TIMESTAMP + " DESC", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public List<Operation> fetchAccountNewOperations(long accountRowId) throws SQLException {
    	List<Operation> result = new ArrayList<Operation>();

        Cursor mCursor =

            mDb.query(true, OPERATION_TABLE, new String[] {
            		OPERATION_UID,
            		OPERATION_CREATION_TIMESTAMP,
            		OPERATION_AMOUNT,
            		OPERATION_LABEL,
            		OPERATION_NATURE,
            		OPERATION_VERSION
            		}, OPERATION_ACCOUNT + "=" + accountRowId + " AND " + OPERATION_VERSION + "<= 0" ,
            		null, null, null, OPERATION_CREATION_TIMESTAMP + " DESC", null);
        if (mCursor != null) {
            mCursor.moveToFirst();while(!mCursor.isAfterLast()) {
            	Operation op = toOperation(mCursor);
            	result.add(op);
            	mCursor.moveToNext();
           	}
        }
        return result;
    } 
    


    public long createOperationAndUpdateAccount(long accountRowid, Operation operation) {
    	mDb.beginTransaction();
    	long operationId = createOperation(accountRowid, operation);
    	Cursor cursor = fetchAccountOperations(accountRowid);
    	if (cursor ==null) {
    		mDb.endTransaction();
    		return -1;
    	}
    	int amountColumnIndex = cursor.getColumnIndex(OPERATION_AMOUNT);
    	cursor.moveToFirst();
    	
    	Double balance = 0.0;
    	while(!cursor.isAfterLast()){
			Double a = cursor.getDouble(amountColumnIndex);
			if (a!=null) {
				balance += a;
			}
			cursor.moveToNext();
    	}
    	cursor.close();
    	AccountExtended account = fetchAccount(accountRowid);
    	account.setSum(balance);
        updateAccount(accountRowid, account);
        mDb.setTransactionSuccessful();
		mDb.endTransaction();
		return operationId;
    }

    
    public boolean deleteOperation(String uid) {
        return mDb.delete(OPERATION_TABLE, OPERATION_UID + "='" + uid+"'", null) > 0;
    }
    
    public boolean deleteOperations() {
        return mDb.delete(OPERATION_TABLE, null, null) > 0;
    }
    
}
