/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bankparent;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.bankparent.model.AccountExtended;
import com.bankparent.model.Operation;

public class BankParentOperations extends Activity {
	
	public static final NumberFormat amountNumberFormat = new DecimalFormat("'+'#0.00;'-'#0.00");

	public SimpleDateFormat niceDateFormat = new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.FRENCH);
	
    private TextView mBalanceText;
    private ListView mOperationList;
    private EditText mBodyText;
    private Long mAccountId;
    private AccountDbAdapter mAccountDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountDbHelper = new AccountDbAdapter(this);
        mAccountDbHelper.open();

        setContentView(R.layout.operation_list);

        mBalanceText = (TextView) findViewById(R.id.balance);
        mOperationList = (ListView) findViewById(R.id.operations);

        Button addAmountButton = (Button) findViewById(R.id.add_amount);
        Button subAmountButton = (Button) findViewById(R.id.sub_amount);
        
//        addAmountButton.setVisibility(View.GONE) ;
//        subAmountButton.setVisibility(View.GONE) ;

        mAccountId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(AccountDbAdapter.ACCOUNT_ROWID);
		if (mAccountId == null) {
			Bundle extras = getIntent().getExtras();
			mAccountId = extras != null ? extras.getLong(AccountDbAdapter.ACCOUNT_ROWID)
									: null;
		}

        
		loadAccount();

        addAmountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	newAmountDialog(true);
            }
        });

        subAmountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	newAmountDialog(false);
            }
        });
    }

    private void loadAccount() {
        if (mAccountId != null) {
            AccountExtended account = mAccountDbHelper.fetchAccount(mAccountId);
//            startManagingCursor(account);
            setTitle(account.getName());
            
            Double balance = account.getSum();
            mBalanceText.setText(BankParentActivity.balanceNumberFormat.format(balance) + " €");

            // fetch operations
            loadAccountOperations();
        }
    }

    

    private void loadAccountOperations() {
    	    	
        Cursor operationCursor = mAccountDbHelper.fetchAccountOperations(mAccountId);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{
        		AccountDbAdapter.OPERATION_CREATION_TIMESTAMP, 
        		AccountDbAdapter.OPERATION_AMOUNT, 
        		AccountDbAdapter.OPERATION_LABEL  };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.date, R.id.amount, R.id.note};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter adapter =        		
            new SimpleCursorAdapter(this, R.layout.operation_row, operationCursor, from, to);
        
        adapter.setViewBinder(new ViewBinder() {
        	

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

//            	Operation operation = mAccountDbHelper.toOperation(aCursor);
            	
                if (aColumnIndex == 2) { // creation timestamp
                    Long creationTimestamp = aCursor.getLong(aColumnIndex);
                    
                    String niceDate = "?? ?? ????";
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTimeInMillis(creationTimestamp);
					niceDate = niceDateFormat.format(calendar.getTime());
					
                    TextView textView = (TextView) aView;
					textView.setText(niceDate);
                    return true;
             }            	
            	
                if (aColumnIndex == 3) { // amount
                        Double amount = aCursor.getDouble(aColumnIndex);
                        TextView textView = (TextView) aView;
                        textView.setText(amountNumberFormat.format(amount) + " €");
                        return true;
                 }
                

                 return false;
            }
        });

        

        mOperationList.setAdapter(adapter);
    }    
    
    
    protected void newAmountDialog(final boolean addAmount) {
    	
    	//Context context = getApplicationContext();
    	final Dialog dialog = new Dialog(this);

    	dialog.setContentView(R.layout.new_amount_dlg);

    	// large width
    	LayoutParams params = dialog.getWindow().getAttributes(); 
        params.width = LayoutParams.FILL_PARENT; 
        dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
    	
    	
    	//builder.setIcon(R.drawable.dialog_question);
    	if(addAmount) {
    		dialog.setTitle(R.string.add_amount_title);
    	} else {
    		dialog.setTitle(R.string.sub_amount_title);
    	}
    	final EditText amountEdit = (EditText) dialog.findViewById(R.id.amountEdit);
    	final EditText amountNoteEdit = (EditText) dialog.findViewById(R.id.amountNote);

    	Button okButton = (Button) dialog.findViewById(R.id.confirm);
    	Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
    	
		cancelButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
	    	    dialog.dismiss();				
			}			
			}
		);
		
		okButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String newAmountValue = amountEdit.getText().toString();
				String note = amountNoteEdit.getText().toString();
				newAmount(addAmount, newAmountValue, note);
	    	    dialog.dismiss();				
			}			
			}
		);
    	dialog.show();
    }
    

	private void newAmount(boolean addAmount, String newAmountValue, String label) {
		// convert amount value
		double newValue;
		if (newAmountValue == null || newAmountValue.trim().length()==0) {
			return;
		}
		try {
			newValue = Double.parseDouble(newAmountValue.replace(',', '.'));
			if (addAmount ^ (newValue>=0) ) {
				newValue = -newValue; // change le signe si besoin
			}
			BigDecimal db = new BigDecimal(newValue).setScale(2, BigDecimal.ROUND_HALF_UP); // rounding
			newValue = db.doubleValue();
		} catch(NumberFormatException e) {
			return;
		}
		
		// fixe note length
		if (label!=null) {
			label = label.trim();
			if (label.length() > 30) {
				label = label.substring(0, 27) + "...";
			}
		}
		Operation operation = new Operation();
		operation.setCreationTimestamp(new Date().getTime());
		operation.setAmount(newValue);
		operation.setLabel(label);
		operation.setNature(Operation.NATURE_ADDITION);
		operation.setVersion(-1);
		
		mAccountDbHelper.createOperationAndUpdateAccount(mAccountId, operation);
		loadAccount();
	}    
	
	

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(AccountDbAdapter.ACCOUNT_ROWID, mAccountId);
    }
    
    

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccount();
    }

    @Override
    protected void onDestroy() {
    	mAccountDbHelper.close();
    	super.onDestroy();
    }    

    private void saveState() {
//        String title = mBalanceText.getText().toString();
//        String body = mBodyText.getText().toString();
//
//        if (mAccountId == null) {
//            long id = mDbHelper.createNote(title, body);
//            if (id > 0) {
//                mAccountId = id;
//            }
//        } else {
//            mDbHelper.updateNote(mAccountId, title, body);
//        }
    }

}
