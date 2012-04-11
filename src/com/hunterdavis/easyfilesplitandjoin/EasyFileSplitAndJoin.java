package com.hunterdavis.easyfilesplitandjoin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyFileSplitAndJoin extends Activity {

	int SELECT_FILE = 22;
	int SELECT_MULTI_FILE = 223;
	String filePath = null;
	static long fileSizeInBytes = 0;
	private Context magicContext;
	public Vector magicNamesVector;
	Button multiButton;
	String newFileName;
	int lasterror;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		magicNamesVector = new Vector();
		lasterror = 0;

		// Create an anonymous implementation of OnClickListener
		OnClickListener loadButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to
				Intent intent = new Intent(v.getContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				startActivityForResult(intent, SELECT_FILE);

			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener splitButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				Float goodSplitVal = testSplitVal();
				if (goodSplitVal > 0) {
					splitFile(goodSplitVal);
				}
			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener joinButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				boolean goodJoinName = testJoinName();
				if (goodJoinName == true) {
					joinFile();
				}
			}
		};

		// Create an anonymous implementation of OnClickListener
		OnClickListener multiButtonListner = new OnClickListener() {
			public void onClick(View v) {
				magicNamesVector.clear();
				magicContext = v.getContext();
				// in onCreate or any event where your want the user to
				Intent intent = new Intent(v.getContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				startActivityForResult(intent, SELECT_MULTI_FILE);
			}
		};

		Button loadButton = (Button) findViewById(R.id.loadButton);
		loadButton.setOnClickListener(loadButtonListner);

		Button splitButton = (Button) findViewById(R.id.splitbutton);
		splitButton.setOnClickListener(splitButtonListner);

		Button joinButton = (Button) findViewById(R.id.joinbutton);
		joinButton.setOnClickListener(joinButtonListner);

		multiButton = (Button) findViewById(R.id.multijoinbutton);
		multiButton.setOnClickListener(multiButtonListner);

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
	}// end of oncreate

	public void joinFile() {
		InputStream is = null;
		OutputStream os = null;
		lasterror = 0;

		// first try to open the file
		try {
			is = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lasterror = 1;

		}

		// now try to open the first output file
		try {
			os = new FileOutputStream(getBasePathOfString(filePath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lasterror = 1;
		}

		if (lasterror == 1) {
			Toast.makeText(getBaseContext(),
					"I/O Error " + getBasePathOfString(filePath),
					Toast.LENGTH_SHORT).show();
			return;
		}

		String nextFilePath = filePath;
		byte[] kilobuffer = new byte[1024];
		int filefound = 1;

		while (filefound == 1) {
			try {
				while (is.read(kilobuffer) > 0) {
					try {
						os.write(kilobuffer);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						lasterror = 1;
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				lasterror = 1;
			}

			nextFilePath = nextFilePathIncrementSuffix(nextFilePath);
			try {
				is = new FileInputStream(nextFilePath);
			} catch (FileNotFoundException e) {
				filefound = 0;
			}

		}
		if (lasterror == 1) {
			Toast.makeText(getBaseContext(),
					"I/O Error " + getBasePathOfString(filePath),
					Toast.LENGTH_SHORT).show();
			return;
		} else {

			// after finish reading the first one, do the second one
			Toast.makeText(
					getBaseContext(),
					"Successfuly Joined Into File "
							+ getBasePathOfString(filePath), Toast.LENGTH_SHORT)
					.show();
		}
	}

	public String getBasePathOfString(String oldString) {
		int dotposition = oldString.lastIndexOf(".");
		return oldString.substring(0, dotposition);
	}

	public String nextFilePathIncrementSuffix(String oldFilePath) {

		int dotposition = oldFilePath.lastIndexOf(".");
		String numbers = oldFilePath.substring(dotposition + 1);
		int realnumbers = Integer.valueOf(numbers);
		int numbersLength = numbers.length();

		// first increment the real number
		realnumbers++;

		String suffixStart = String.valueOf(realnumbers);
		int suffixlength = suffixStart.length();

		int diff = numbersLength - suffixlength;
		String ourFullSuffix = getBasePathOfString(oldFilePath) + ".";

		for (int i = 0; i < diff; i++) {
			ourFullSuffix += "0";
		}
		ourFullSuffix += suffixStart;
		return ourFullSuffix;
	}

	public void splitFile(float splitval) {
		lasterror = 0;
		int splitValInKiloBytes = (int) (splitval * 1024);
		int kiloByteCounter = 0;
		int suffixCounter = 1;
		InputStream is = null;
		OutputStream os = null;
		String newfilePath = filePath
				+ getStringSuffixFromInt(suffixCounter, splitValInKiloBytes);

		// first try to open the file
		try {
			is = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			lasterror = 1;
		}

		// now try to open the first output file
		try {
			os = new FileOutputStream(newfilePath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			lasterror = 1;
		}

		if (lasterror == 1) {
			Toast.makeText(getBaseContext(),
					"I/O Error " + getBasePathOfString(filePath),
					Toast.LENGTH_SHORT).show();
			return;
		}

		byte[] kilobuffer = new byte[1024];
		try {
			while (is.read(kilobuffer) > 0) {
				kiloByteCounter++;

				if (kiloByteCounter > splitValInKiloBytes) {
					kiloByteCounter = 1;
					suffixCounter++;
					newfilePath = filePath
							+ getStringSuffixFromInt(suffixCounter,
									splitValInKiloBytes);
					os.close();
					// now try to open the first output file
					try {
						os = new FileOutputStream(newfilePath);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						lasterror = 1;
					}
				}

				try {
					os.write(kilobuffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					lasterror = 1;
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (lasterror == 1) {
			Toast.makeText(getBaseContext(),
					"I/O Error " + getBasePathOfString(filePath),
					Toast.LENGTH_SHORT).show();
			return;
		} else {
			Toast.makeText(
					getBaseContext(),
					"Successfuly Split Into " + suffixCounter
							+ " files of type " + newfilePath,
					Toast.LENGTH_SHORT).show();
		}
	}

	public String getStringSuffixFromInt(int suffix, int splitSize) {
		int fileSizeInKilos = (int) (fileSizeInBytes / 1024);
		int numberOfFiles = (int) Math.floor(fileSizeInKilos / splitSize);

		float remainder = fileSizeInKilos % splitSize;
		if (remainder > 0) {
			numberOfFiles++;
		}

		String numberStart = String.valueOf(numberOfFiles);
		int places = numberStart.length();

		String suffixStart = String.valueOf(suffix);
		int suffixlength = suffixStart.length();

		int diff = places - suffixlength;
		String ourFullSuffix = ".";

		for (int i = 0; i < diff; i++) {
			ourFullSuffix += "0";
		}
		ourFullSuffix += suffixStart;
		return ourFullSuffix;

	}

	public float testSplitVal() {
		EditText splitVal = (EditText) findViewById(R.id.splitsize);
		float splitExactly = Float.valueOf(splitVal.getText().toString());
		if (splitExactly < .009) {
			Toast.makeText(getBaseContext(),
					"Minimum Split Value is .01 megs (10k)", Toast.LENGTH_SHORT)
					.show();
			return -1;
		}
		return splitExactly;
	}

	public boolean testJoinName() {
		if (filePath.endsWith(".01")) {
			return true;
		} else if (filePath.endsWith(".1")) {
			return true;
		} else if (filePath.endsWith(".001")) {
			return true;
		} else if (filePath.endsWith(".0001")) {
			return true;
		} else if (filePath.endsWith(".00001")) {
			return true;
		} else if (filePath.endsWith(".000001")) {
			return true;
		}
		Toast.makeText(getBaseContext(),
				"Join files are named .0001 .001 .01 etc", Toast.LENGTH_SHORT)
				.show();

		return false;
	}

	public void onActivityResult(final int requestCode, int resultCode,
			final Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_FILE) {
				filePath = data.getStringExtra(FileDialog.RESULT_PATH);
				// set the filename txt
				changeFileNameText(filePath);
				// md5 the file
				String md5String = md5(filePath);
				changeMD5Text(md5String);

				// enable the buttons
				Button splitButton = (Button) findViewById(R.id.splitbutton);
				splitButton.setEnabled(true);

				Button joinButton = (Button) findViewById(R.id.joinbutton);
				joinButton.setEnabled(true);

			} else if (requestCode == SELECT_MULTI_FILE) {
				String newFilePath = data
						.getStringExtra(FileDialog.RESULT_PATH);
				askForAnother(newFilePath);
			}
		} else if (resultCode == RESULT_CANCELED) {
		}
	}

	public void askForAnother(String newPath) {
		magicNamesVector.add(newPath);

		AlertDialog.Builder alert = new AlertDialog.Builder(magicContext);
		alert.setTitle("Add Another?");
		alert.setMessage("You Just Added " + newPath + ", which makes "
				+ magicNamesVector.size() + " items, add another?");

		// Set an EditText view to get user input
		// final EditText input = new EditText(context);
		// input.setInputType(InputType.TYPE_CLASS_NUMBER);
		// input.setText("1");
		// alert.setView(input);

		alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Intent intent = new Intent(magicContext, FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				startActivityForResult(intent, SELECT_MULTI_FILE);
			}

		});

		alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				askToJoin();
			}
		});

		alert.show();

	}

	public void askToJoin() {

		AlertDialog.Builder alert = new AlertDialog.Builder(magicContext);
		alert.setTitle("Join Files?");
		alert.setMessage("Join The Following Files?");

		// Set an EditText view to get user input
		final EditText input = new EditText(magicContext);
		input.setEnabled(false);
		input.setLines(10);
		input.setVerticalScrollBarEnabled(true);

		String alertBoxString = "";

		for (int i = 0; i < magicNamesVector.size(); i++) {
			try {
				alertBoxString += magicNamesVector.get(i) + "\n";
			} catch (Exception e) {

			}
		}
		input.setText(alertBoxString);
		alert.setView(input);

		alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				magicJoin();
				dialog.dismiss();
			}

		});

		alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
				magicNamesVector.clear();
			}
		});

		alert.show();

	}

	public void magicJoin() {
		lasterror = 0;
		new Thread(new Runnable() {
			public void run() {
				InputStream is = null;
				OutputStream os = null;

				String fileString = "";
			

				String nextFilePath = fileString;
				byte[] kilobuffer = new byte[1024];

				for(int i = 0;i<magicNamesVector.size();i++){

					try {
						nextFilePath = (String) magicNamesVector
								.get(i);
					} catch (Exception e1) {
						lasterror = 1;
					}
					
					if(i == 0) {
						// new name
						newFileName = nextFilePath + "-MULTIJOINED";
					}
					
					try {
						is = new FileInputStream(nextFilePath);
					} catch (FileNotFoundException e) {
						lasterror = 1;
					}
					
					
					// now try to open the first output file
					try {
						os = new FileOutputStream(newFileName);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						lasterror = 1;
					}
					
					
					try {
						while (is.read(kilobuffer) > 0) {
							try {
								os.write(kilobuffer);
							} catch (IOException e) {
								lasterror = 1;
								break;
							}

						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						lasterror = 1;
					}

					if (newFileName.equalsIgnoreCase(fileString)) {
						lasterror = 1;
					}
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				runOnUiThread(new Runnable() {
					public void run() {
						if (lasterror == 1) {
							Toast.makeText(
									magicContext,
									"I/O Error "
											+ newFileName,
									Toast.LENGTH_SHORT).show();
							return;
						} else {
							Toast.makeText(
									magicContext,
									"Successfuly Joined Into File "
											+ newFileName, Toast.LENGTH_SHORT)
									.show();
						}
					}
				});

			}

		}).start();

	}

	public void changeFileNameText(String newFileName) {
		TextView t = (TextView) findViewById(R.id.fileText);
		t.setText(newFileName);
	}

	public void changeMD5Text(String newMD5) {
		TextView t = (TextView) findViewById(R.id.mdfive);
		t.setText(newMD5);
	}

	public void updateFileSizeDialog(long fileSizeInBytes) {
		float fileSizeInKilos = fileSizeInBytes / 1024;
		float fileSizeInMegs = fileSizeInKilos / 1024;
		TextView t = (TextView) findViewById(R.id.fileSize);
		t.setText(fileSizeInMegs + " Megabytes");
	}

	public String md5(String fileString) {
		try {
			fileSizeInBytes = 0;
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			InputStream is = null;
			try {
				is = new FileInputStream(fileString);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			byte[] buffer = new byte[8 * 1024];
			int read;
			try {
				while ((read = is.read(buffer)) > 0) {
					fileSizeInBytes += read;
					digest.update(buffer, 0, read);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			updateFileSizeDialog(fileSizeInBytes);
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

}// end of activity