package com.android.packageinstaller;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SetApexPermissions extends Activity  {
	ArrayList<String> permList;
	private ArrayList<String> deniedPermList; 
	
	private static Map<String, PermissionDetails> pMap = new HashMap<String, PermissionDetails>(); 
	
	private ListView mainListView ; 
	private Permission[] permissions ;
	private ArrayAdapter<Permission> listAdapter ;
	
	private static String TAG = "APEX:SetApexPermissions";
	// private String permDirectory = "/system/etc/apex/perms/";
	private String permDirectory = "/sdcard/apex-";
	// private String permDirectory = "/data/secure/";

	private String packageName = "";
    private int targetSdkVersion;
    
    // Permission levels 
    private final static int PERM_CRITICAL = 2;
    private final static int PERM_SENSITIVE = 1; 
    private final static int PERM_NORMAL = 0;

	  
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("Hello, Android");
		// setContentView(tv);
		setContentView(R.layout.setperms);
		
		pMap = PermissionDetails.getPermissionDetailsDefalut(); 
		
		permList = (ArrayList<String>) getIntent().getExtras().get("permsArrayList");
		deniedPermList = (ArrayList<String>) getIntent().getExtras().get("deniedPermsArrayList");
		packageName  = (String)getIntent().getExtras().get("packageName");
		// targetSdkVersion = (Integer)getIntent().getExtras().get("targetSdkVersion");
		
		for(String _str : permList){
			Log.d(TAG, "Found in set permissions: " + _str);
		}
		for(String _str : deniedPermList){
            Log.d(TAG, "Found in denied permissions: " + _str);
        }
		
		// set the list view for selecting permissions 
		// Find the ListView resource. 
	    mainListView = (ListView) findViewById( R.id.mainListView );
	    
	    // When item is tapped, toggle checked properties of CheckBox and Permission.
	    mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	      @Override
	      public void onItemClick( AdapterView<?> parent, View item, 
	                               int position, long id) {
	        Permission permission = listAdapter.getItem( position );
	        permission.toggleChecked();
	        PermissionViewHolder viewHolder = (PermissionViewHolder) item.getTag();
	        viewHolder.getCheckBox().setChecked( permission.isChecked() );
	      }
	    });

	    final Context thisContext = this; 
	    // Set Buttons 
	    final Button buttonInstall = (Button) findViewById(R.id.btnInstall);
	    buttonInstall.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	String policiesText  = ""; 
	        	ArrayList<Permission> ps = new ArrayList<Permission>();
	        	for(int i = 0; i < listAdapter.getCount(); i++) {
	        		if(!listAdapter.getItem(i).checked){ 
	        			ps.add(listAdapter.getItem(i));
	        		} 
	        	}
	            // Toast.makeText(thisContext, "Denying permissions: " + ps.size(), Toast.LENGTH_SHORT).show();
	            if(ps.size() > 0){ 
	            	policiesText = "<Policies>\n"; 
	            	for (int i = 0; i < ps.size(); i++) {
	            		String permName = ps.get(i).getName(); 
	            		policiesText += PolicyHelper.getSimpleDenyPermission(permName);
	            		
	            	} 
	            	policiesText += "</Policies>"; 
	            	// Log.d(TAG, "Got policy for package name ["+packageName+"] : " + policiesText);
	            	// if (writeFile(permDirectory + packageName, policiesText))
	            	//	Toast.makeText(thisContext, "Policy written successfully.", Toast.LENGTH_SHORT).show();
	            	// else 
	            	//	Toast.makeText(thisContext, "Error writing policy!", Toast.LENGTH_SHORT).show();
	            	
	            } else { 
	            	Toast.makeText(thisContext, "No permissions denied.", Toast.LENGTH_SHORT).show();
	            }
	            
	            // let's go back 
	            Intent resultIntent = new Intent(); 
	            resultIntent.putExtra("policyText", policiesText);
	            setResult(Activity.RESULT_OK, resultIntent);
	            finish(); 
	        }
	    });
	    // Set Buttons 
	    final Button buttonCancel = (Button) findViewById(R.id.btnCancel);
	    buttonCancel.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	            Toast.makeText(thisContext, "No permissions were set.", Toast.LENGTH_SHORT).show();
	            Intent resultIntent = new Intent(); 
	            setResult(Activity.RESULT_CANCELED, resultIntent);
	            finish();
	        }
	    });
	    
	    // Create and populate permissions.
	    // permissions = (Permission[]) getLastNonConfigurationInstance() ;
	    if ( permissions == null ) {
	      permissions = new Permission[permList.size()];
	      for (int i = 0; i < permList.size() ; i++){
	          String curPerm = permList.get(i); 
	          boolean isPermitted = true; 
	          isPermitted = !deniedPermList.contains(curPerm); 
	    	  permissions[i] = new Permission(curPerm, isPermitted); 
	      }
	    }
	    ArrayList<Permission> permissionList = new ArrayList<Permission>();
	    permissionList.addAll( Arrays.asList(permissions) );
	    
	    // Set our custom array adapter as the ListView's adapter.
	    listAdapter = new PermissionArrayAdapter(this, permissionList);
	    mainListView.setAdapter( listAdapter );
	}
	
	/*
	private boolean writeFile(String policyFile, String policyText){ 
		PrintWriter out;
		boolean retVal = true;
		Log.d(TAG, "Trying to write to policy file: " + policyFile);
		try {
			out = new PrintWriter(new FileWriter(policyFile));
			out.print(policyText);
			out.close();
			File file = new File(policyFile); 
			if (file.exists()) {
	            boolean bval = file.setWritable(true, true);
	            Log.d(TAG, "Set the owner's write permission: "+ bval);
	            bval = file.setReadable(true, true);
	            Log.d(TAG, "Set the owner's read permission: "+ bval);
	        } else {
	             System.out.println("File does not exists.");
	        }
		} catch (IOException e) {
			Log.d(TAG, "Exception writing policy file."); 
			e.printStackTrace();
			retVal = false; 
		} 
		return retVal; 
	  }
	*/

	
	/** Holds permission data. */
	  private static class Permission {
	    private String name = "" ;
	    private boolean checked = true ;
	    public Permission() {}
	    public Permission( String name ) {
	      this.name = name ;
	    }
	    public Permission( String name, boolean checked ) {
	      this.name = name ;
	      this.checked = checked ;
	    }
	    public String getName() {
	      return name;
	    }
	    public void setName(String name) {
	      this.name = name;
	    }
	    public boolean isChecked() {
	      return checked;
	    }
	    public void setChecked(boolean checked) {
	      this.checked = checked;
	    }
	    public String toString() {
	      return name ; 
	    }
	    public void toggleChecked() {
	      checked = !checked ;
	    }
		public String getFriendlyName() {
			String friendlyName = name.substring(name.lastIndexOf('.')+1);
			friendlyName = friendlyName.toLowerCase().replace('_', ' ');
			friendlyName = friendlyName.substring(0,1).toUpperCase() + friendlyName.substring(1);  
			return friendlyName;
		}
	  }
	  
	  /** Holds child views for one row. */
	  private static class PermissionViewHolder {
	    private CheckBox checkBox ;
	    private TextView textView ;
        private ImageView icon;
        private TextView descBox;
        
	    public PermissionViewHolder() {}
	    public PermissionViewHolder( TextView textView, CheckBox checkBox, ImageView icon, TextView descBox) {
	      this.checkBox = checkBox ;
	      this.textView = textView ;
	      this.icon = icon;
	      this.descBox = descBox; 
	    }
	    public CheckBox getCheckBox() {
	      return checkBox;
	    }
	    public void setCheckBox(CheckBox checkBox) {
	      this.checkBox = checkBox;
	    }
	    public TextView getTextView() {
	      return textView;
	    }
	    public void setTextView(TextView textView) {
	      this.textView = textView;
	    }
	    public ImageView getIcon() {
	        return icon;
	    }
	    public void setIcon(ImageView icon) {
	        this.icon = icon;
	    }
	    public void setDescBox(TextView descBox) { 
	        this.descBox = descBox;
	    }
        public TextView getDescBox() {
            return descBox; 
        }
	  }
	  
	  /** Custom adapter for displaying an array of Permission objects. */
	  private static class PermissionArrayAdapter extends ArrayAdapter<Permission> {
	    
	    private LayoutInflater inflater;
	    
	    public PermissionArrayAdapter( Context context, List<Permission> permissionList ) {
	      super( context, R.layout.simplerow, R.id.rowTextView, permissionList );
	      // Cache the LayoutInflate to avoid asking for a new one each time.
	      inflater = LayoutInflater.from(context) ;
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	      // Permission to display
	      Permission permission = (Permission) this.getItem( position ); 

	      // The child views in each row.
	      CheckBox checkBox; 
	      TextView textView; 
	      ImageView icon;
	      TextView descBox; 
	      
	      // Create a new row view
	      if ( convertView == null ) {
	        convertView = inflater.inflate(R.layout.simplerow, null);
	        
	        // Find the child views.
	        textView = (TextView) convertView.findViewById( R.id.rowTextView );
	        checkBox = (CheckBox) convertView.findViewById( R.id.CheckBox01 );
	        icon = (ImageView) convertView.findViewById(R.id.perm_sensitivity); 
	        descBox = (TextView) convertView.findViewById(R.id.rowPermDesc);
	        
	        // Optimization: Tag the row with it's child views, so we don't have to 
	        // call findViewById() later when we reuse the row.
	        convertView.setTag( new PermissionViewHolder(textView,checkBox, icon, descBox) );

	        // If CheckBox is toggled, update the permission it is tagged with.
	        checkBox.setOnClickListener( new View.OnClickListener() {
	          public void onClick(View v) {
	            CheckBox cb = (CheckBox) v ;
	            Permission permission = (Permission) cb.getTag();
	            permission.setChecked( cb.isChecked() );
	          }
	        });        
	      }
	      // Reuse existing row view
	      else {
	        // Because we use a ViewHolder, we avoid having to call findViewById().
	        PermissionViewHolder viewHolder = (PermissionViewHolder) convertView.getTag();
	        checkBox = viewHolder.getCheckBox() ;
	        textView = viewHolder.getTextView() ;
	        icon = viewHolder.getIcon();
	        descBox = viewHolder.getDescBox(); 
	      }

	      // Tag the CheckBox with the Permission it is displaying, so that we can
	      // access the permission in onClick() when the CheckBox is toggled.
	      checkBox.setTag( permission ); 
	      
	      // Display permission data
	      checkBox.setChecked( permission.isChecked() );
	      textView.setText( permission.getFriendlyName() );      
	      
	      // Set the permission icon 
	       
	      if (PolicyHelper.getPermissionSensitivity(permission.getName()) == PERM_CRITICAL) 
	          icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.exclamation));
	      else if (PolicyHelper.getPermissionSensitivity(permission.getName()) == PERM_SENSITIVE) 
              icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.error));
	      else 
	          icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.button_indicator_finish));
	      
	      descBox.setText(PolicyHelper.getPermissionDesc(permission.getName())); 
	      return convertView;
	    }
	    
	  }
	  private static class PolicyHelper { 
		  private static String getSimpleDenyPermission(String permName){
			String policy = ""
					+ "  <Policy Effect=\"Deny\">  " + "\n"  
					+ "    <Permission Name=\"" + permName + "\" /> "  + "\n"
					+ "    <Constraint CombiningAlgorithm=\"org.csrdu.apex.combiningalgorithms.All\"> " + "\n"
					+ "      <Expression FunctionId=\"org.csrdu.apex.functions.StringToBoolean\"> " + "\n"
					+ "        <Constant Value='true' />" + "\n"
					+ "      </Expression> " + "\n"
					+ "    </Constraint>"  + "\n"
					+ "  </Policy> " + "\n";
			return policy; 
		  }
		  private static int getPermissionSensitivity(String permName) {
		      if(pMap.get(permName) != null) 
		          return pMap.get(permName).sensitivity;
		      
		      else return PERM_NORMAL;
		  }
		  private static String getPermissionDesc(String permName) { 
		      if(pMap.get(permName) != null)
		          return pMap.get(permName).desc; 
		      else 
		          return "";
		  }
	  }
}

class PermissionDetails { 
    public int sensitivity; 
    public String desc;
    
    public PermissionDetails(int sensitivity, String desc) { 
        this.sensitivity = sensitivity; 
        this.desc = desc; 
    }
    
    public static HashMap<String, PermissionDetails> getPermissionDetailsDefalut() { 
        HashMap<String, PermissionDetails> pMap = new HashMap<String, PermissionDetails>();
        
        pMap.put("android.permission.ACCESS_COARSE_LOCATION", 
                new PermissionDetails(1, "Allows the app to access approximate location through wireless network"));
        pMap.put("android.permission.CALL_PHONE", 
                new PermissionDetails(2, "Allows the app to initiate a phone call without going through the Dialer"));
        
        return pMap;
    }
}
