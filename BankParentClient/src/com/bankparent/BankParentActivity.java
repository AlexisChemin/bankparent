package com.bankparent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.ResourceAccessException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.bankparent.model.Account;
import com.bankparent.model.AccountExtended;
import com.bankparent.model.AccountSynchro;
import com.bankparent.model.Operation;
import com.bankparent.rest.BankParentAPIRestTemplate;

public class BankParentActivity extends ListActivity {
	
    public static final NumberFormat balanceNumberFormat = new DecimalFormat("#####0.00");// NumberFormat.getCurrencyInstance(Locale.FRENCH);

    private static final int ACTIVITY_OPERATIONS=1;

    private String bankparentEndpoint =
    		"http://bankparent.appspot.com";
    		//"http://192.168.2.35:8889";

	BankParentAPIRestTemplate bankparentRemote = new BankParentAPIRestTemplate(bankparentEndpoint);
	
    private AccountDbAdapter mAccountDbHelper;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_list);
        mAccountDbHelper = new AccountDbAdapter(this);
        mAccountDbHelper.open();
        loadAccounts();
    }
    

    private void loadAccounts() {
        Cursor accountCursor = mAccountDbHelper.fetchAllAccounts();
        startManagingCursor(accountCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{AccountDbAdapter.ACCOUNT_NAME, AccountDbAdapter.ACCOUNT_BALANCE, AccountDbAdapter.ACCOUNT_IMAGE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.name, R.id.balance, R.id.image};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter adapter =        		
            new SimpleCursorAdapter(this, R.layout.account_row, accountCursor, from, to);
        
        adapter.setViewBinder(new ViewBinder() {

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                if (aColumnIndex == 3) { // Balance
                        Double balance = aCursor.getDouble(aColumnIndex);
                        TextView textView = (TextView) aView;
                        textView.setText("" + balanceNumberFormat.format(balance) + " €");
                        return true;
                 }
                if (aColumnIndex == 4) { // Image
                    String image = aCursor.getString(aColumnIndex);
                    ImageView imageView = (ImageView) aView;
                    
                    int resource = getResources().getIdentifier(image, "drawable","com.bankparent");
                    if (resource<=0) {
                    	resource = R.drawable.icon;
                    }
                    
					imageView.setImageResource(resource);
                    return true;
             }

                 return false;
            }
        });
        
        setListAdapter(adapter);
    }
    

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, BankParentOperations.class);
        i.putExtra(AccountDbAdapter.ACCOUNT_ROWID, id);
        startActivityForResult(i, ACTIVITY_OPERATIONS);
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        loadAccounts();
    }
    

    protected void synchAccounts() {
    	feedback("Synchronisation");
    	try {
	    	List<Account> accounts = bankparentRemote.getAccounts();
	    	for(Account account : accounts) {
	    		synchAccount(account);
	    	}
	    	finish();
	    	startActivity(getIntent());
    	} catch(ResourceAccessException e) {
    		feedback("Echec de la synchronisation");
    	}
	}

    
    private void synchAccount(Account account) {
    	AccountExtended local = mAccountDbHelper.fetchAccount(account.getId());
    	if (local==null) {
    		synchNewAccount(account);
    	} else {
			syncExistingAccount(account, local);
    	}
		
	}


	protected void synchNewAccount(Account account) {
		// account does not exist at all !!
		// get all data from remote
		AccountSynchro synchro = new AccountSynchro();
		account.setVersion(0); // we need full update !
		synchro.setAccount(account);
		synchro = bankparentRemote.synchronize(synchro);
		long accountRowid = mAccountDbHelper.createAccount(synchro.getAccount());
		syncOperations(accountRowid, synchro.getOperations());
		feedback("Nouveau : " + account.getName());
	}
	
	protected void syncExistingAccount(Account account, AccountExtended local) {
		// send local operations if any
		long accountRowid = local.getRowid();
		List<Operation> operations = mAccountDbHelper.fetchAccountNewOperations(accountRowid);
		if ( (operations.size() <=0)
			&& (local.getVersion() >= account.getVersion())
			) {
			// we are up to date
			feedback(account.getName() + " : à jour");
			return; 
		}
		// we need to update this account
		AccountSynchro synchro = new AccountSynchro();
		synchro.setAccount(local);
		synchro.setOperations(operations);
		synchro = bankparentRemote.synchronize(synchro);
		mAccountDbHelper.updateAccount(accountRowid, synchro.getAccount());  
		List<Operation> remoteOperations = synchro.getOperations();
		syncOperations(accountRowid, remoteOperations);
		if (remoteOperations.size() > 0 ) {
			feedback(account.getName() + " : +" + remoteOperations.size() + " operation(s)");
		}
	}


	protected void syncOperations(long accountRowid, List<Operation> operations) {
		Map<String, Operation> localOperationsByUID = new HashMap<String, Operation>();
		// fetch local operations
		Cursor operationCursor = mAccountDbHelper.fetchAccountOperations(accountRowid);
		while(!operationCursor.isAfterLast()) {
			Operation op = mAccountDbHelper.toOperation(operationCursor);
			localOperationsByUID.put(op.getId(), op);
			operationCursor.moveToNext();
		}
		// loops though remote operations
		for(Operation remoteOperation : operations) {
			// find the local version of the operation
			Operation localOperation = localOperationsByUID.get(remoteOperation.getId());
			if (localOperation != null) {
				String uid = localOperation.getId();
				if (remoteOperation.getCanceled()) {
					// remove the operation
					mAccountDbHelper.deleteOperation(uid);
				} else {
					mAccountDbHelper.updateOperation(accountRowid, remoteOperation);
				}
			} else if (!remoteOperation.getCanceled()) {
				// new operation
				mAccountDbHelper.createOperation(accountRowid, remoteOperation);
			}
		}
	}


    
    // MENU


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sync_accounts:
                new SyncTask().execute(null);
                return true;
            case R.id.settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    // FEEDBACK
    protected void feedback(final String message) {
    	this.runOnUiThread(new Runnable() {
    		public void run() {

            	Toast toast = Toast.makeText(getApplicationContext(), message,  Toast.LENGTH_SHORT );

            	toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

            	toast.show();
        		}
        	})
        ;
    		
    }
    
    // DESTROY
    
    


	@Override
    protected void onDestroy() {
    	mAccountDbHelper.close();
    	super.onDestroy();
    }
    
	
	
	private class SyncTask extends AsyncTask<String, String, String> {
		
		 
		@Override
		protected String doInBackground(String... params) {
		 
			synchAccounts();

			return null;
		}
		 
		protected void onPostExecute(String result) {
		 
		}
	} // end SyncTask 
	
	
}