package com.hardcopy.arduinocontroller;

import com.hardcopy.arduinocontroller.R;
import com.hardcopy.arduinocontroller.Constants;
import com.hardcopy.arduinocontroller.SerialConnector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ArduinoControllerActivity extends TravelActivity implements View.OnClickListener {

	private Context mContext = null;
	private ActivityHandler mHandler = null;

	private SerialListener mListener = null;
	private SerialConnector mSerialConn = null;

	private TextView mTextLog = null;
	private TextView mTextInfo = null;
	private Button mButton1;
	private Button mButton2;
	private Button mButton3;
	private Button mButton4;
	private Button mAlbumList;

	String Toast_msg = null;

	int album_id;
	String album_name;
	String path, name;
	int PRODUCT_ID;
	ProductDatabaseHandler dbHandler = new ProductDatabaseHandler(this);


	private int tmp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// System
		mContext = getApplicationContext();

		// Layouts
		setContentView(R.layout.activity_arduino_controller);

		mTextLog = (TextView) findViewById(R.id.text_serial);
		mTextLog.setMovementMethod(new ScrollingMovementMethod());
		mTextInfo = (TextView) findViewById(R.id.text_info);
		mTextInfo.setMovementMethod(new ScrollingMovementMethod());
		mButton1 = (Button) findViewById(R.id.button_send1);
		mButton1.setOnClickListener(this);
		mButton2 = (Button) findViewById(R.id.button_send2);
		mButton2.setOnClickListener(this);
		mButton3 = (Button) findViewById(R.id.button_send3);
		mButton3.setOnClickListener(this);
		mButton4 = (Button) findViewById(R.id.button_send4);
		mButton4.setOnClickListener(this);

		mAlbumList = (Button) findViewById(R.id.album_btn);
		mAlbumList.setOnClickListener(this);

		album_id = Integer.parseInt(getIntent().getStringExtra("ALBUM_ID"));
		album_name = getIntent().getStringExtra("ALBUM_NAME");

		// Initialize
		mListener = new SerialListener();
		mHandler = new ActivityHandler();

		// Initialize Serial connector and starts Serial monitoring thread.
		mSerialConn = new SerialConnector(mContext, mListener, mHandler);
		mSerialConn.initialize();



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mSerialConn.finalize();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_send1:
			mSerialConn.sendCommand("ab1z");
			//Log.i("onClick", "CallSubActivity");

			/*Toast toast =  Toast.makeText(this, "사진촬영 시작",
					Toast.LENGTH_SHORT);
			toast.show();
			Intent intentSubActivity =
					new Intent(ArduinoControllerActivity.this, CameraActivity.class);
			startActivity(intentSubActivity);*/
			/*Handler mHandler = new Handler();
			mHandler.postDelayed(new Runnable() {
				//Do Something
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Intent intentSubActivity =
							new Intent(ArduinoControllerActivity.this, CameraActivity.class);
					startActivity(intentSubActivity);
				}
			}, 1000); // 1000ms*/
			break;
		case R.id.button_send2:
			mSerialConn.sendCommand("b2");
			break;
		case R.id.button_send3:
			saveProduct("3.5");
			//mSerialConn.sendCommand("b3");
			break;
		case R.id.button_send4:
			mSerialConn.sendCommand("b4");
			break;
		case R.id.album_btn:
			Intent i = new Intent(ArduinoControllerActivity.this, AlbumList.class);
			startActivity(i);
		default:
			break;
		}
	}


	public class SerialListener {
		public void onReceive(int msg, int arg0, int arg1, String arg2, Object arg3) {
			switch(msg) {
			case Constants.MSG_DEVICD_INFO:
				mTextLog.append(arg2+"1");
				break;
			case Constants.MSG_DEVICE_COUNT:
				mTextLog.append(Integer.toString(arg0) + " device(s) found \n");
				break;
			case Constants.MSG_READ_DATA_COUNT:
				mTextLog.append(Integer.toString(arg0) + " buffer received \n");
				break;
			case Constants.MSG_READ_DATA:
				if(arg3 != null) {
					mTextInfo.setText((String)arg3);
					mTextLog.append((String)arg3+"read");
					mTextLog.append("\n");
				}
				break;
			case Constants.MSG_SERIAL_ERROR:
				mTextLog.append(arg2+"2");
				break;
			case Constants.MSG_FATAL_ERROR_FINISH_APP:
				finish();
				break;
			}
		}
	}

	public class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case Constants.MSG_DEVICD_INFO:
				mTextLog.append((String)msg.obj+"a1");
				break;
			case Constants.MSG_DEVICE_COUNT:
				mTextLog.append(Integer.toString(msg.arg1) + " device(s) found \n");
				break;
			case Constants.MSG_READ_DATA_COUNT://사진촬영
				String con = (String)msg.obj;
				mTextLog.append(con + "\n");
				if(con.charAt(0)!='a') {
					Intent intentSubActivity =
							new Intent(ArduinoControllerActivity.this, CameraActivity.class);
					intentSubActivity.putExtra("ALBUM_ID", Integer.toString(album_id));
					intentSubActivity.putExtra("ALBUM_NAME", album_name);
					startActivity(intentSubActivity);
				}
				else
					mTextInfo.setText(con);
				break;
			case Constants.MSG_READ_DATA://무게와따
				if(msg.obj != null) {
					mTextInfo.setText((String)msg.obj);
					mTextLog.append((String)msg.obj+"a3");
					mTextLog.append("\n");
					saveProduct((String)msg.obj);
				}
				break;
			case Constants.MSG_SERIAL_ERROR:
				mTextLog.append((String)msg.obj+"a4");
				break;
			}
		}
	}

	public void saveProduct(String weight) {
		if (weight != null && weight.length() != 0
				&& path != null && path.length() != 0
				&& name != null && name.length() != 0
				) {

			dbHandler.Add_Product(new Product(album_id, name,
					path, weight));
			Toast_msg = "Data inserted successfully";
			Show_Toast(Toast_msg);
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if( getIntent().hasExtra("PATH") ) {
			path = getIntent().getStringExtra("PATH");
			name = getIntent().getStringExtra("NAME");

			Log.e("path and name in Resume", "path : "+path+" name : "+name);
		}

	}

	public void Show_Toast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}


}
