package com.cs252.bookmixer.bookmix;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    DatabaseAdapter db;

    ArrayAdapter<Book> bookAdapter;
    ArrayList<Book> selectedItems;

    ListView listView;
    TextView outputTextView;
    Button generateButton;
    ProgressDialog progressDialog;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the db
        db = new DatabaseAdapter(this);
        //db.resetDB();
        db.createDatabase();
        db.open();

        // instantiate progressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("A message");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "selected: " + tab.getText());

        if (tab.getText().equals(getString(R.string.title_section2))) {

            // update the textview to reflect what's selected
            Log.d(TAG, "Updating result textview");

            StringBuilder resultText = new StringBuilder();
            resultText.append("Selected Items: \n\n");
            for (Book b : selectedItems) {
                resultText.append(b.toString());
                resultText.append("\n");
            }

            outputTextView.setText(resultText.toString());
        }

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "unselected: " + tab.getText());

        // (sloppily) match text to check what tab we're on
        if (tab.getText().equals(getString(R.string.title_section1))) {
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            selectedItems = new ArrayList<Book>();

            for (int i = 0; i < checked.size(); i++) {
                int position = checked.keyAt(i);
                if (checked.valueAt(i)) {
                    Log.d(TAG, "Selected item: " + bookAdapter.getItem(position).toString());
                    selectedItems.add(bookAdapter.getItem(position));
                }
            }
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return new BookSelectFragment();
            } else if (position == 1) {
                return new MashupFragment();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() { // number of total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null; // ya dun goofed
        }
    }

    // fragment with listview to select books
    public class BookSelectFragment extends Fragment {

        private void setListViewHandler(View view) {
            listView = (ListView) view.findViewById(R.id.bookList);

            // convert list of books into array[]
            List<Book> list = db.getAllBooks();
            Book[] books = list.toArray(new Book[list.size()]);

            // set adapter
            bookAdapter = new ArrayAdapter<Book>(super.getActivity(),
                    android.R.layout.simple_list_item_multiple_choice, books);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // able to select multiples
            listView.setAdapter(bookAdapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating book select fragment");
            View rootView = inflater.inflate(R.layout.fragment_bookselect, container, false);
            setListViewHandler(rootView);
            return rootView;
        }
    }

    // fragment for generating the mashups
    public class MashupFragment extends Fragment {

        private void mashUpBooks(View view) {
            // TODO mash them up after all books are done downloading
        }

        // handler for the generate button
        // we download text from gutenberg here
        private void setGenerateButtonHandler(View view) {
            generateButton = (Button) view.findViewById(R.id.generate_button);
            generateButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Mashing up selected books");

                    // first, download books if necessary
                    for (Book book : selectedItems) {
                        Log.d(TAG, "Selected: " + book.toString() +
                                " isDownloaded? " + book.is_downloaded());

                        if (!book.is_downloaded()) { // then download it!
                            Log.d(TAG, "Must DL: "+ book.toString());
                            new DownloadTextTask(getActivity()).execute(book);  // launch async task
                        }
                    }
                }
            });
        }

        private class DownloadTextTask extends AsyncTask<Book, Integer, Book> {
            static final String TAG = "DownloadTextTask: ";

            private Context context;
            private PowerManager.WakeLock mWakeLock;

            public DownloadTextTask(Context context) {
                this.context = context;
            }

            @Override
            protected Book doInBackground(Book ... books) {
                InputStream input = null;
                HttpURLConnection connection = null;
                String downloadedText;
                Book bookWithText;

                progressDialog.setMessage("Downloading " + books[0].get_title());
                try {
                    Log.d(TAG, "attempting dl from url: " + books[0].getURL());
                    URL url = new URL(books[0].getURL());
                    connection = (HttpURLConnection) url.openConnection();
                    populateDesktopHttpHeaders(connection);
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                    }

                    // to display download percentage
                    // might be -1: server did not report the length
                    //List values = connection.getHeaderFields().get("content-Length");
                    long fileSize = 0;
                    try {
                        fileSize = Long.parseLong(connection.getHeaderField("Content-Length"));
                    } catch (NumberFormatException e) {
                    }
                    Log.d(TAG, "fileSize: " + fileSize);

                    // actually download the file
                    input = connection.getInputStream();
                    BufferedReader br = null;
                    StringBuilder sb = new StringBuilder();
                    String line;
                    long total = 0;
                    int count;
                    br = new BufferedReader(new InputStreamReader(input));
                    System.out.print("writing to buffered reader");
                    while ((line = br.readLine()) != null) {
                        // publish progress...
                        total += line.length();
                        if (fileSize > 0) {// only if total length is known
                            publishProgress((int) (total * 100 / fileSize));
                        }
                        sb.append(line);
                    }
                    // done with buffered reader

                    // put the text in the book
                    Log.d(TAG, "writing to output string");
                    downloadedText = sb.toString();
                    bookWithText = books[0];
                    bookWithText.set_text(downloadedText);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null; // :(
                } finally { // clean up the connection
                    Log.d(TAG, "executing 'finally'");
                    try {
                        if (input != null) {
                            Log.d(TAG, "closing input");
                            input.close();
                        }
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                    if (connection != null) {
                        Log.d(TAG, "disconnecting connection");
                        connection.disconnect();
                    }
                }
                Log.d(TAG, "returning from downloader");
                return bookWithText;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // take CPU lock to prevent CPU from going off even if the user
                // presses the power button during download
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
                mWakeLock.acquire();
                progressDialog.show();
            }

            @Override
            protected void onProgressUpdate(Integer... progress) {
                super.onProgressUpdate(progress);
                // if we get here, length is known, now set indeterminate to false
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgress(progress[0]);
            }

            @Override
            protected void onPostExecute(Book result) {
                Log.d(TAG, "doing postExecute");

                mWakeLock.release();
                progressDialog.dismiss();

                Toast toast = new Toast(getApplicationContext());
                if (result == null) {
                    // null because AsyncTask hasn't done its task
                    //Log.v("download error", result.get_title());
                    toast.makeText(context,"Download error", Toast.LENGTH_LONG).show();

                } else {
                    Log.v("File downloaded", "null");
                    outputTextView.setText(result.toString());
                    toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
                }
                //outputTextView.setText("Done downloading " + result.get_title() + " ");
            }


        }

        private void setTextViewAdapter(View view) {
            outputTextView = (TextView) view.findViewById(R.id.result_text);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating mashup fragment");
            View rootView = inflater.inflate(R.layout.fragment_mashup, container, false);

            setTextViewAdapter(rootView);
            setGenerateButtonHandler(rootView);

            return rootView;
        }
    }

    private static void populateDesktopHttpHeaders(URLConnection urlCon) {
        // add custom header in order to be easily detected
        urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        urlCon.setRequestProperty("Accept-Language",
                "el-gr,el;q=0.8,en-us;q=0.5,en;q=0.3");
        urlCon.setRequestProperty("Accept-Charset",
                "ISO-8859-7,utf-8;q=0.7,*;q=0.7");
    }
}
